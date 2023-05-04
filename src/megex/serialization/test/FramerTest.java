package megex.serialization.test;

import megex.serialization.Framer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FramerTest {
    private static final int MAX_LENGTH = 16384;
    private Framer framer;
    private OutputStream outputStream;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        framer = new Framer(outputStream);
    }

    @Test
    void constructor_nullOutputStream_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Framer(null));
    }

    @Test
    void putFrame_nullMessage_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> framer.putFrame(null));
    }

    @Test
    void putFrame_emptyMessage_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> framer.putFrame(new byte[0]));
    }

    @Test
    void putFrame_tooLongMessage_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> framer.putFrame(new byte[MAX_LENGTH + 7]));
    }

    @Test
    void putFrame_validMessage_writesToOutputStream() throws IOException {
        byte[] message = new byte[10];
        Arrays.fill(message, (byte) 65); // Fill the message with ASCII 'A'

        framer.putFrame(message);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        byte[] expectedBytes = {0, 0, 4, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65};

        assertArrayEquals(expectedBytes, outputStreamBytes);
    }

    @Test
    void putBytes_nullBytes_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> framer.putBytes(null));
    }

    @Test
    void putBytes_validBytes_writesToOutputStream() throws IOException {
        byte[] bytes = new byte[] {1, 2, 3, 4, 5};
        framer.putBytes(bytes);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        assertArrayEquals(bytes, outputStreamBytes);
    }
    @Test
    void putFrame_maxLengthMessage_writesToOutputStream() throws IOException {
        byte[] message = new byte[MAX_LENGTH + 6];
        Arrays.fill(message, (byte) 65); // Fill the message with ASCII 'A'

        framer.putFrame(message);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        byte[] expectedBytes = new byte[MAX_LENGTH + 9]; // 3 bytes for the length + MAX_LENGTH + 6 for the message
        expectedBytes[0] = (byte) ((MAX_LENGTH >> 16) & 0xFF);
        expectedBytes[1] = (byte) ((MAX_LENGTH >> 8) & 0xFF);
        expectedBytes[2] = (byte) (MAX_LENGTH & 0xFF);
        Arrays.fill(expectedBytes, 3, expectedBytes.length, (byte) 65);

        assertArrayEquals(expectedBytes, outputStreamBytes);
    }

    @Test
    void putFrame_minLengthMessage_writesToOutputStream() throws IOException {
        byte[] message = new byte[6]; // Minimum payload length (6 bytes, 0 payload)
        Arrays.fill(message, (byte) 65); // Fill the message with ASCII 'A'

        framer.putFrame(message);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        byte[] expectedBytes = {0, 0, 0, 65, 65, 65, 65, 65, 65};

        assertArrayEquals(expectedBytes, outputStreamBytes);
    }

    @Test
    void putFrame_multiBytePayloadLength_writesToOutputStream() throws IOException {
        int payloadLength = 300; // Arbitrary payload length larger than 255 (to use more than one byte)
        byte[] message = new byte[payloadLength + 6];
        Arrays.fill(message, (byte) 65); // Fill the message with ASCII 'A'

        framer.putFrame(message);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        byte[] expectedBytes = new byte[payloadLength + 9]; // 3 bytes for the length + payloadLength + 6 for the message
        expectedBytes[0] = (byte) ((payloadLength >> 16) & 0xFF);
        expectedBytes[1] = (byte) ((payloadLength >> 8) & 0xFF);
        expectedBytes[2] = (byte) (payloadLength & 0xFF);
        Arrays.fill(expectedBytes, 3, expectedBytes.length, (byte) 65);

        assertArrayEquals(expectedBytes, outputStreamBytes);
    }

    @Test
    void putFrame_negativePayloadLength_throwsIllegalArgumentException() {
        byte[] message = new byte[5]; // Payload length is -1
        assertThrows(IllegalArgumentException.class, () -> framer.putFrame(message));
    }

    @Test
    void putFrame_largeNegativePayloadLength_throwsIllegalArgumentException() {
        byte[] message = new byte[1]; // Payload length is -5
        assertThrows(IllegalArgumentException.class, () -> framer.putFrame(message));
    }

    @Test
    void putFrame_zeroPayloadLength_writesToOutputStream() throws IOException {
        byte[] message = new byte[6]; // Payload length is 0
        Arrays.fill(message, (byte) 65); // Fill the message with ASCII 'A'

        framer.putFrame(message);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        byte[] expectedBytes = {0, 0, 0, 65, 65, 65, 65, 65, 65};

        assertArrayEquals(expectedBytes, outputStreamBytes);
    }

    @Test
    void putFrame_payloadLengthOne_writesToOutputStream() throws IOException {
        byte[] message = new byte[7]; // Payload length is 1
        Arrays.fill(message, (byte) 65); // Fill the message with ASCII 'A'

        framer.putFrame(message);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        byte[] expectedBytes = {0, 0, 1, 65, 65, 65, 65, 65, 65, 65};

        assertArrayEquals(expectedBytes, outputStreamBytes);
    }

    @Test
    void putFrame_payloadLength255_writesToOutputStream() throws IOException {
        byte[] message = new byte[255 + 6]; // Payload length is 255
        Arrays.fill(message, (byte) 65); // Fill the message with ASCII 'A'

        framer.putFrame(message);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        byte[] expectedBytes = new byte[255 + 9];
        expectedBytes[0] = 0;
        expectedBytes[1] = 0;
        expectedBytes[2] = (byte) 255;
        Arrays.fill(expectedBytes, 3, expectedBytes.length, (byte) 65);

        assertArrayEquals(expectedBytes, outputStreamBytes);
    }

    @Test
    void putFrame_messageLengthOne_throwsIllegalArgumentException() {
        byte[] message = new byte[1]; // Message length is 1
        assertThrows(IllegalArgumentException.class, () -> framer.putFrame(message));
    }

    @Test
    void putFrame_messageLengthFive_throwsIllegalArgumentException() {
        byte[] message = new byte[5]; // Message length is 5
        assertThrows(IllegalArgumentException.class, () -> framer.putFrame(message));
    }

    @Test
    void putBytes_singleByte_writesToOutputStream() throws IOException {
        byte[] bytes = new byte[] {65}; // Single byte 'A'

        framer.putBytes(bytes);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        byte[] expectedBytes = {65};

        assertArrayEquals(expectedBytes, outputStreamBytes);
    }

    @Test
    void putBytes_multipleBytes_writesToOutputStream() throws IOException {
        byte[] bytes = new byte[] {65, 66, 67}; // Multiple bytes 'A', 'B', 'C'

        framer.putBytes(bytes);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        byte[] expectedBytes = {65, 66, 67};

        assertArrayEquals(expectedBytes, outputStreamBytes);
    }

    @Test
    void putBytes_emptyArray_doesNotWriteToOutputStream() throws IOException {
        byte[] bytes = new byte[0]; // Empty array

        framer.putBytes(bytes);

        byte[] outputStreamBytes = ((ByteArrayOutputStream) outputStream).toByteArray();
        byte[] expectedBytes = {};

        assertArrayEquals(expectedBytes, outputStreamBytes);
    }

}
