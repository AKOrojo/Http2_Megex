package megex.serialization.test;

import megex.serialization.Framer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class FramerTest {

    @Test
    public void testPutFrame() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Framer framer = new Framer(out);
        byte[] message = new byte[] {0x01, 0x02, 0x03, 0x04, 0x04, 0x04, 0x04};
        framer.putFrame(message);
        byte[] expectedMessage = new byte[] {0x00, 0x00, 0x01, 0x01, 0x02, 0x03, 0x04, 0x04, 0x04, 0x04};
        byte[] actualMessage = out.toByteArray();
        assertArrayEquals(expectedMessage, actualMessage);
    }
}
