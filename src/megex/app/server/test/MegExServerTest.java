package megex.app.server.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import megex.app.client.Client;
import megex.app.server.Server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class MegExServerTest {
    private static final int SERVER_PORT = 8080;
    private static final int NUM_THREADS = 4; // Define a constant for the number of threads
    private static final String DOCUMENT_ROOT = System.getProperty("user.dir"); // Define a constant for the document root
    private static final String SERVER_ADDRESS = "localhost";
    private static Thread serverThread;



    @Test
    public void testFileRequest() throws IOException {
        String testFilePath = "/testfile.txt";
        Path testFile = Paths.get(System.getProperty("user.dir") + testFilePath);

        // Check if the test file exists on the server
        assertTrue(Files.exists(testFile), "Test file must exist on the server");

        // Request the test file using the provided client code
        String[] args = {SERVER_ADDRESS, String.valueOf(SERVER_PORT), testFilePath};
        Client.main(args);

        // Check if the test file has been successfully downloaded by the client
        File downloadedFile = new File("testfile.txt");
        assertTrue(downloadedFile.exists(), "Downloaded file must exist");
        assertEquals(Files.size(testFile), downloadedFile.length(), "Downloaded file size must match the original file size");

        // Clean up the downloaded file
        assertTrue(downloadedFile.delete(), "Downloaded file must be deleted");
    }

    @AfterAll
    public static void stopServer() {
        serverThread.interrupt();
    }
}
