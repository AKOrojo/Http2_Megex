/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/
package megex.app.server;

import megex.serialization.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;
import java.util.concurrent.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A Runnable class responsible for handling client connections.
 */
public class ClientHandler implements Runnable {
    public static int MAXDATASIZE;
    public static int MINDATAINTERVAL;
    private final Socket clientSocket;
    private final String documentRoot;
    private final Logger logger;
    private final ConcurrentMap<Integer, Boolean> activeStreamIds;
    private final ConcurrentHashMap<Integer, AtomicInteger> streamWindowSizeMap = new ConcurrentHashMap<>();
    private static final String CLIENT_PREFACE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";
    private final ExecutorService threadPool;

    /**
     * Constructs a new ClientHandler.
     *
     * @param clientSocket   The socket connected to the client.
     * @param documentRoot   The root directory of the server.
     * @param logger         The logger for logging events.
     * @param MAXDATASIZE    The maximum size of data to be sent.
     * @param MINDATAINTERVAL The minimum interval between data transmissions.
     */
    public ClientHandler(Socket clientSocket, String documentRoot, Logger logger, int MAXDATASIZE, int MINDATAINTERVAL, int numThreads) {
        this.clientSocket = clientSocket;
        this.documentRoot = documentRoot;
        this.logger = logger;
        ClientHandler.MAXDATASIZE = MAXDATASIZE;
        ClientHandler.MINDATAINTERVAL = MINDATAINTERVAL;
        this.activeStreamIds = new ConcurrentHashMap<>();
        this.threadPool = Executors.newFixedThreadPool(numThreads);
        try {
            clientSocket.setSoTimeout(40 * 1000); // 40 seconds timeout
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Error while setting socket timeout", e);
        }
    }

    /**
     * Handles the client connection, processing HTTP/2 requests and responses.
     */
    @Override
    public void run() {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));


            Framer framer = new Framer(outputStream);
            Deframer deframer = new Deframer(inputStream);
            MessageFactory messageFactory = new MessageFactory();


            // Check if the first thing received is the preface
            char[] prefaceBuffer = new char[CLIENT_PREFACE.length()];
            int bytesRead = reader.read(prefaceBuffer, 0, CLIENT_PREFACE.length());
            String receivedPreface = new String(prefaceBuffer);

            if (bytesRead != CLIENT_PREFACE.length() || !receivedPreface.equals(CLIENT_PREFACE)) {
                logger.log(Level.WARNING, "Bad preface: " + String.join("", new String(prefaceBuffer)));
                clientSocket.close();
                return;
            }

            logger.log(Level.WARNING, "Bad preface: " + Arrays.toString(prefaceBuffer));

            sendSettingsFrame(framer, messageFactory, logger);

            // Inside the run() method, after sending the Settings frame
            while (true) {
                try {
                    byte[] receivedFrame = deframer.getFrame();
                    Message message = messageFactory.decode(receivedFrame);
                    logger.log(Level.INFO, "Received frame: " + message);

                    if (message instanceof Headers) {
                        Runnable task = () -> {
                            try {
                                handleHeadersFrame((Headers) message, framer, messageFactory);
                            } catch (IOException | BadAttributeException e) {
                                logger.log(Level.SEVERE, "Error while handling Headers frame", e);
                            }
                        };
                        threadPool.submit(task);
                    } else if (message instanceof Settings) {
                        logger.log(Level.INFO, "Received Settings message: " + message);
                    } else if (message instanceof Window_Update) {
                        handleWindowUpdateFrame((Window_Update) message);
                    } else if (message instanceof Data) {
                        logger.log(Level.WARNING, "Unexpected Data message: " + message);
                    } else {
                        logger.log(Level.WARNING, "Unexpected message: " + message.toString());
                    }
                } catch (EOFException e) {
                    // End of stream, close the connection
                    break;
                } catch (SocketTimeoutException e) {
                    logger.log(Level.WARNING, "Connection timed out after 40 seconds of inactivity");
                    break;
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error while reading frame", e);
                    break;
                } catch (BadAttributeException e) {
                    logger.log(Level.SEVERE, "Error while decoding frame", e);
                    break;
                }
            }
            clientSocket.close();

        } catch (BadAttributeException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                logger.log(Level.INFO, "Closing connection");
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while closing the connection", e);
            }
        }
    }

    private static void sendSettingsFrame(Framer framer, MessageFactory messageFactory, Logger logger) throws IOException, BadAttributeException {
        Settings settingsFrame = new Settings();
        byte[] encodedSettingFrame = messageFactory.encode(settingsFrame);
        framer.putFrame(encodedSettingFrame);
        logger.log(Level.INFO, "Sent Settings frame: " + settingsFrame);
    }

    private void handleHeadersFrame(Headers headersFrame, Framer framer,MessageFactory messageFactory) throws IOException, BadAttributeException {
        int streamId = headersFrame.getStreamID();

        // Check for illegal stream ID
        if (streamId % 2 == 0 || streamId <= 0) {
            logger.log(Level.WARNING, "Illegal stream ID: " + headersFrame);
            return;
        }

        // Check for duplicate stream ID
        if (activeStreamIds.putIfAbsent(streamId, true) != null) {
            logger.log(Level.WARNING, "Duplicate request: " + headersFrame);
            return;
        }

        // Extract the requested path from the headers
        String path = headersFrame.getValue(":path");
        if (path == null) {
            logger.log(Level.WARNING, "No path");
            sendStatusHeaders(streamId, 400, framer, messageFactory); // :status 400
            // Terminate the stream
            return;
        }

        Path filePath = Paths.get(documentRoot, path);
        if (Files.isDirectory(filePath)) {
            logger.log(Level.WARNING, "Cannot request directory");
            sendStatusHeaders(streamId, 403, framer, messageFactory); // :status 403
            // Terminate the stream
            return;
        }

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            logger.log(Level.WARNING, "File not found");
            sendStatusHeaders(streamId, 404, framer, messageFactory); // :status 404
            // Terminate the stream
            return;
        }

        // Good stream ID
        sendStatusHeaders(streamId, 200, framer, messageFactory); // :status 200
        sendInitialWindowUpdate(streamId, framer, messageFactory);
        Path documentRootPath = Paths.get(documentRoot);
        Path relativeFilePath = documentRootPath.relativize(filePath);
        sendFile(streamId, relativeFilePath, framer, messageFactory);


    }

    private void sendStatusHeaders(int streamId, int statusCode, Framer framer, MessageFactory messageFactory) throws IOException, BadAttributeException {
        Headers responseHeaders = new Headers(streamId, true);
        responseHeaders.addValue(":status", Integer.toString(statusCode));
        byte[] encodedHeadersFrame = messageFactory.encode(responseHeaders);
        framer.putFrame(encodedHeadersFrame);
        logger.log(Level.INFO, "Sent Headers frame: " + responseHeaders);
    }

    private void sendFile(int streamId, Path filePath, Framer framer, MessageFactory messageFactory) throws IOException, BadAttributeException {
        logger.log(Level.INFO, "Attempting to send file: " + filePath);
        Path documentRootPath = Paths.get(documentRoot);
        Path combinedPath = documentRootPath.resolve(filePath).normalize();
        File file = combinedPath.toFile();

        try (InputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[MAXDATASIZE];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                boolean isEnd = (fileInputStream.available() == 0);
                byte[] payload = Arrays.copyOf(buffer, bytesRead);
                logger.log(Level.INFO, "Payload Created For Stream: " + streamId);

                AtomicInteger streamWindowSize = streamWindowSizeMap.computeIfAbsent(streamId, k -> new AtomicInteger(0));

                Data dataFrame = new Data(streamId, isEnd, payload);
                byte[] encodedDataFrame = messageFactory.encode(dataFrame);
                framer.putFrame(encodedDataFrame);
                logger.log(Level.INFO, "Sent Data frame: " + dataFrame); // Add this log statement

                streamWindowSize.addAndGet(-bytesRead);

                try {
                    Thread.sleep(MINDATAINTERVAL);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Error while waiting between data frames", e);
                }
            }
        }
    }


    private void handleWindowUpdateFrame(Window_Update windowUpdateFrame) {
        int streamId = windowUpdateFrame.getStreamID();
        int increment = windowUpdateFrame.getIncrement();
        streamWindowSizeMap.computeIfAbsent(streamId, k -> new AtomicInteger(0)).addAndGet(increment);
        logger.log(Level.INFO, "Received message: " + windowUpdateFrame);
    }

    private void sendInitialWindowUpdate(int streamId, Framer framer, MessageFactory messageFactory) throws IOException, BadAttributeException {
        Window_Update windowUpdateFrame = new Window_Update(streamId, MAXDATASIZE);
        byte[] encodedWindowUpdateFrame = messageFactory.encode(windowUpdateFrame);
        framer.putFrame(encodedWindowUpdateFrame);
        logger.log(Level.INFO, "Sent Window_Update frame: " + windowUpdateFrame);
    }

}
