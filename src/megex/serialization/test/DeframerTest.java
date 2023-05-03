package megex.serialization.test;

import megex.serialization.Deframer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


public class DeframerTest {

    @Test
    public void testGetFrame() throws IOException {
        // Input stream of test data
        byte[] input = new byte[]{0x00, 0x00, 0x01, 0x01, 0x02, 0x03, 0x04, 0x05, 0x05, 0x05};
        InputStream in = new ByteArrayInputStream(input);

        // Create a new instance of the Deframer class
        Deframer deframer = new Deframer(in);

        // Expected output of the test
        byte[] expected = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x05, 0x05};
        // Get the actual output by calling the getFrame() method
        byte[] actual = deframer.getFrame();

        // Compare the expected and actual output and check if they are equal
        assertArrayEquals(expected, actual);

    }

    @Test
    public void testGetFrame2() throws IOException {
        // Input stream of test data
        byte[] input = new byte[]{0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF};
        InputStream in = new ByteArrayInputStream(input);

        // Create a new instance of the Deframer class
        Deframer deframer = new Deframer(in);

        // Expected output of the test
        byte[] expected = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF};
        // Get the actual output by calling the getFrame() method
        byte[] actual = deframer.getFrame();

        // Compare the expected and actual output and check if they are equal
        assertArrayEquals(expected, actual);

    }
}
