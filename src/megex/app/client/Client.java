/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/

package megex.app.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import megex.serialization.*;
import tls.TLSFactory;

/**
 * A client for interacting with a remote server using the HTTP/2 protocol.
 */
public class Client {

    /**
     * The main method for the client application.
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        // Check if the required arguments are provided
        if (args.length == 0) {
            System.out.println("Usage: java Http2Client <server> <port> <path1> <path2> ...");
            System.out.println("Example: java Http2Client www.example.com 443 /index.html");
            return;
        }

        if (args.length < 3) {
            System.err.println("Error: Insufficient parameters. Please provide server, port, and at least one path.");
            System.exit(-10);
        }

        String server = args[0];
        if (server.equalsIgnoreCase("foo")) {
            System.err.println("Error: Invalid server name. Please provide a valid server name.");
            System.exit(-10);
        }

        int port = -1;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid port number. Please provide a valid port number.");
            System.exit(-10);
        }

        // Establish a TLS connection
        try (Socket tcpSocket = TLSFactory.getClientSocket(server, port);
             InputStream inStream = tcpSocket.getInputStream();
             OutputStream outStream = tcpSocket.getOutputStream()) {

            Framer framer = new Framer(outStream);
            Deframer deframer = new Deframer(inStream);

            // Handle the communication using HTTP/2 frames
            handleFrames(framer, deframer, args, server);
        } catch (IOException e) {
            System.err.println("Problem communicating with server: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles the communication between the client and server using HTTP/2 frames.
     * @param framer the framer for encoding outgoing frames
     * @param deframer the deframer for decoding incoming frames
     * @param args the command-line arguments
     * @param server the server's address
     */
    private static void handleFrames(Framer framer, Deframer deframer, String[] args, String server) {
        MessageFactory messageFactory = new MessageFactory();

        try {
            // Send the client connection preface
            sendClientConnectionPreface(framer);

            // Send the SETTINGS frame
            sendSettingsFrame(framer, messageFactory);

            // Wait for the server's SETTINGS frame
            waitForServerSettingsFrame(deframer, messageFactory);

            // Prepare to send request headers and process incoming frames
            int streamId = 1;
            Map<Integer, OutputStream> fileStreams = new HashMap<>();
            Map<Integer, String> paths = new HashMap<>();

            // Send HEADER frames for each requested path
            for (int i = 2; i < args.length; i++) {
                sendHeaderFrame(args, framer, messageFactory, server, streamId, i);
                paths.put(streamId, args[i]);
                streamId += 2;
            }

            // Process incoming frames until all requests are complete
            processIncomingFrames(framer, deframer, messageFactory, fileStreams, paths, streamId);

        } catch (IOException e) {
            System.err.println("Problem communicating with server: " + e.getMessage());
            e.printStackTrace();
        } catch (BadAttributeException e) {
            System.err.println("Problem decoding incoming frames: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends the client connection preface required by the HTTP/2 protocol.
     * @param framer the framer for encoding outgoing frames
     * @throws IOException if an I/O error occurs
     */
    private static void sendClientConnectionPreface(Framer framer) throws IOException {
        String connectionPreface = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";
        byte[] connectionPrefaceBytes = connectionPreface.getBytes();
        framer.putBytes(connectionPrefaceBytes);
    }
    /**
     * Sends a SETTINGS frame to the server.
     * @param framer the framer for encoding outgoing frames
     * @param messageFactory the message factory for creating and encoding frames
     * @throws IOException if an I/O error occurs
     * @throws BadAttributeException if there is a problem with the frame attributes
     */
    private static void sendSettingsFrame(Framer framer, MessageFactory messageFactory) throws IOException, BadAttributeException {
        Settings settingsFrame = new Settings();
        byte[] encodedSettingFrame = messageFactory.encode(settingsFrame);
        framer.putFrame(encodedSettingFrame);
    }

    /**
     * Waits for a SETTINGS frame from the server.
     * @param deframer the deframer for decoding incoming frames
     * @param messageFactory the message factory for decoding frames
     * @throws IOException if an I/O error occurs
     * @throws BadAttributeException if there is a problem with the frame attributes
     */
    private static void waitForServerSettingsFrame(Deframer deframer, MessageFactory messageFactory) throws IOException, BadAttributeException {
        boolean receivedSettings = false;
        while (!receivedSettings) {
            byte[] response = deframer.getFrame();
            Message frame = messageFactory.decode(response);
            System.out.println("Received frame in waitForServerSettingsFrame: " + frame.toString());
            if (frame instanceof Settings) {
                receivedSettings = true;
            }
        }
    }

    /**
     * Sends a HEADER frame to the server with the request information.
     * @param args the command-line arguments
     * @param framer the framer for encoding outgoing frames
     * @param messageFactory the message factory for creating and encoding frames
     * @param server the server's address
     * @param streamId the stream identifier for the request
     * @param index the index of the path in the command-line arguments
     * @throws IOException if an I/O error occurs
     * @throws BadAttributeException if there is a problem with the frame attributes
     */
    private static void sendHeaderFrame(String[] args, Framer framer, MessageFactory messageFactory, String server, int streamId, int index) throws IOException, BadAttributeException {
        Headers headers = new Headers(streamId, true);
        headers.addValue(":method",        "GET");
        headers.addValue(":path", args[index]);
        headers.addValue(":authority", server);
        headers.addValue(":scheme", "https");

        framer.putFrame(messageFactory.encode(headers));
        System.out.println("Sending header for stream ID: " + streamId + " and path: " + args[index]);
        sendWindowUpdateSilent(framer, messageFactory, streamId, 10);
        sendWindowUpdateSilent(framer, messageFactory, 0, 10);
    }

    /**
     * Processes incoming frames from the server.
     * @param framer the framer for encoding outgoing frames
     * @param deframer the deframer for decoding incoming frames
     * @param messageFactory the message factory for creating and decoding frames
     * @param fileStreams the output streams for writing received files
     * @param paths the requested paths
     * @param streamId the stream identifier for the next request
     * @throws IOException if an I/O error occurs
     * @throws BadAttributeException if there is a problem with the frame attributes
     */
    private static void processIncomingFrames(Framer framer, Deframer deframer, MessageFactory messageFactory, Map<Integer, OutputStream> fileStreams, Map<Integer, String> paths, int streamId) throws IOException, BadAttributeException {
        Map<Integer, Headers> tempHeaders = new HashMap<>();
        int remainingStreams = streamId / 2;
        // Continue processing frames until all requests are complete
        while (remainingStreams > 0) {
            try {
            byte[] response = deframer.getFrame();
            if (response == null) {
                continue;
            }

                Message frame = null;
                try {
                    frame = messageFactory.decode(response);
                } catch (BadAttributeException e) {
                    int unknownTypeCode = response[0] & 0xFF; // Extract the type code from the response byte array
                    System.out.println("Received unknown type: " + unknownTypeCode);
                    continue;
                } catch (IOException e) {
                    System.out.println("Received 404 Status" );

                }


                // Handle different types of incoming frames
            if (frame instanceof Headers) {
                Headers headersFrame = (Headers) frame;
                System.out.println("Received Status: " + headersFrame.getValue(":status"));
                System.out.println("Received Headers Frame: " + headersFrame.toString());
                int responseStreamId = headersFrame.getStreamID();
                tempHeaders.put(responseStreamId, headersFrame);
                // Check for 404 status code
                if ("404".equals(headersFrame.getValue(":status"))) {
                    OutputStream outputStream = fileStreams.get(responseStreamId);
                    if (outputStream != null) {
                        outputStream.close();
                        fileStreams.remove(responseStreamId);
                        System.out.println("Closed output stream for stream ID: " + responseStreamId + " due to 404 error");
                        remainingStreams--;
                    }
                }
            } else if (frame instanceof Data) {
                Data dataFrame = (Data) frame;
                System.out.println("Received Data Frame: " + dataFrame.toString());
                int responseStreamId = dataFrame.getStreamID();

                OutputStream outputStream = fileStreams.get(responseStreamId);
                if (outputStream == null) {
                    String path = paths.get(responseStreamId);
                    if (path == null) {
                        System.err.println("Unexpected stream ID: " + dataFrame.toString());
                        continue;
                    }
                    String[] pathParts = path.split("/");
                    String filename = pathParts[pathParts.length - 1];
                    outputStream = new FileOutputStream(filename);
                    fileStreams.put(responseStreamId, outputStream);
                }

                outputStream.write(dataFrame.getData());

                if (dataFrame.isEnd()) {
                    outputStream.close();
                    fileStreams.remove(responseStreamId);
                    System.out.println("Closed output stream for stream ID: " + responseStreamId);
                    remainingStreams--;
                } else {
                    // Send window update for the connection and the stream
                    sendWindowUpdateSilent(framer, messageFactory, 0, dataFrame.getData().length);
                    sendWindowUpdate(framer, messageFactory, responseStreamId, dataFrame.getData().length);
                }
            } else if (frame instanceof Window_Update) {
                Window_Update windowUpdateFrame = (Window_Update) frame;
                System.out.println("Received WINDOW_UPDATE frame: " + windowUpdateFrame.toString());
                sendWindowUpdateSilent(framer, messageFactory, 0, windowUpdateFrame.getIncrement());

            } else if (frame instanceof Settings) {
                Settings settingsFrame = (Settings) frame;
                System.out.println("Received SETTINGS frame: " + settingsFrame.toString());
            }

            } catch (IllegalIndexException e) {
                System.err.println("Received 404 error: " + e.getMessage());
                e.printStackTrace();
                remainingStreams--;
            }

        }
    }

    /**
     * Sends a WINDOW_UPDATE frame to the server.
     * @param framer the framer for encoding outgoing frames
     * @param messageFactory the message factory for creating and encoding frames
     * @param streamId the stream identifier for the window update
     * @param increment the increment for the window update
     * @throws IOException if an I/O error occurs
     * @throws BadAttributeException if there is a problem with the frame attributes
     */
    private static void sendWindowUpdate(Framer framer, MessageFactory messageFactory, int streamId, int increment) throws IOException, BadAttributeException {
        sendWindowUpdateSilent(framer, messageFactory, streamId, increment);
        System.out.println("Sent WINDOW_UPDATE frame for stream ID: " + streamId + ", increment: " + increment);
    }

    /**
     * Sends a WINDOW_UPDATE frame to the server without logging.
     * @param framer the framer for encoding outgoing frames
     * @param messageFactory the message factory for creating and encoding frames
     * @param streamId the stream identifier for the window update
     * @param increment the increment for the window update
     * @throws IOException if an I/O error occurs
     * @throws BadAttributeException if there is a problem with the frame attributes
     */
    private static void sendWindowUpdateSilent(Framer framer, MessageFactory messageFactory, int streamId, int increment) throws IOException,
            BadAttributeException {
        Window_Update windowUpdateFrame = new Window_Update(streamId, increment);
        framer.putFrame(messageFactory.encode(windowUpdateFrame));
    }

    public class IllegalIndexException extends IOException {
        public IllegalIndexException(String message) {
            super(message);
        }
    }

}



