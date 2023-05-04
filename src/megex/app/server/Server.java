/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/
package megex.app.server;

import tls.TLSFactory;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * Server is a multithreaded, SSL/TLS-secured HTTP server.
 */
public class Server {
    private final int port;
    String keystoreFile = "keystore.jks";
    String keystorePassword = "mypassword";
    private volatile boolean isShuttingDown = false;
    private final int numThreads;
    private final String documentRoot;
    private final ExecutorService threadPool;
    private final Logger logger;
    public static final int MAXDATASIZE = 500;
    public static final int MINDATAINTERVAL = 500;

    /**
     * Constructs a new Server instance.
     *
     * @param port         The port number to listen for incoming connections.
     * @param numThreads   The number of worker threads for handling client requests.
     * @param documentRoot The path to the server's document root directory.
     * @param logger       The Logger instance to use for logging.
     */
    public Server(int port, int numThreads, String documentRoot, Logger logger) {
        this.port = port;
        this.numThreads = numThreads;
        this.documentRoot = documentRoot;
        this.threadPool = Executors.newFixedThreadPool(numThreads);
        this.logger = logger;
    }

    /**
     * Starts the server, listening for incoming connections.
     */
    public void start() {
        try (ServerSocket serverSocket = TLSFactory.getServerListeningSocket(port, keystoreFile, keystorePassword)) {
            while (!isShuttingDown) {
                try {
                    Socket clientSocket = TLSFactory.getServerConnectedSocket(serverSocket);
                    threadPool.submit(new ClientHandler(clientSocket, documentRoot, logger, MAXDATASIZE, MINDATAINTERVAL, numThreads));
                } catch (SocketTimeoutException e) {
                    if (isShuttingDown) {
                        break;
                    }
                } catch (IOException e) {
                    if (!isShuttingDown) {
                        logger.log(Level.SEVERE, "Error while starting the server", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while starting the server", e);
            System.exit(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        // Configure logger for the server
        Logger logger = Logger.getLogger(Server.class.getName());
        logger.setUseParentHandlers(false); // Disable the default ConsoleHandler

        // Remove default ConsoleHandler from the root logger
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                rootLogger.removeHandler(handler);
            }
        }

        // Set up file handler for logging
        try {
            FileHandler fileHandler = new FileHandler("server.log");
            fileHandler.setFormatter(new MicrosecondFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to set up logger", e);
            System.exit(1);
        }

        // Validate command line arguments
        if (args.length < 3) {
            logger.log(Level.WARNING, "Error: Insufficient parameters provided. Usage: java Server <port> <num_threads> <document_root>");
            System.exit(-5);
        }

        // Parse and validate the port number
        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Error: Invalid port number. Usage: java Server <port> <num_threads> <document_root>");
            System.exit(-5);
        }

        // Parse and validate the number of threads
        int numThreads = 0;
        try {
            numThreads = Integer.parseInt(args[1]);
            if (numThreads < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Error: Invalid number of threads. Usage: java Server <port> <num_threads> <document_root>");
            System.exit(-5);
        }

        // Validate the document root directory
        String documentRoot = args[2];
        File documentRootFile = new File(documentRoot);
        if (!documentRootFile.exists() || !documentRootFile.isDirectory()) {
            logger.log(Level.WARNING, "Error: Invalid document root. Usage: java Server <port> <num_threads> <document_root>");
            System.exit(-5);
        }

        // Create and start the server
        Server server = new Server(port, numThreads, documentRoot, logger);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.isShuttingDown = true;
            logger.log(Level.INFO, "Shutting down the server...");
            server.threadPool.shutdown();
            try {
                server.threadPool.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // Do nothing
            }
        }));

        // Start the server
        server.start();
    }
}

