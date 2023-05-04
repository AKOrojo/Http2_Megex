package megex.serialization.test;

import megex.serialization.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.Signature;

import static org.junit.jupiter.api.Assertions.*;

class MessageFactoryTest {

    @Test
    void testDecodeBadMessage() {
        // create a sample byte array that is too short to be a valid message
        byte[] bytes = new byte[]{0x00, 0x01, 0x00, 0x00};

        // decode the byte array using the MessageFactory and expect a BadAttributeException to be thrown
        MessageFactory factory = new MessageFactory();
        assertThrows(BadAttributeException.class, () -> factory.decode(bytes));
    }

    @Test
    void testEncodeDataMessage() throws BadAttributeException, IOException {
        // create a sample Data message
        Data message = new Data(1, false, new byte[]{0x68, 0x65, 0x6c, 0x6c, 0x6f});

        // encode the message using the MessageFactory
        MessageFactory factory = new MessageFactory();
        byte[] bytes = factory.encode(message);

        // check that the encoded byte array matches the expected value
        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x68, 0x65, 0x6c, 0x6c, 0x6f}, bytes);
    }

    @Test
    void testEncodeDataMessage2() throws BadAttributeException, IOException {
        // create a sample Data message
        Data message = new Data(1, true, new byte[]{0x68, 0x65, 0x6c, 0x6c, 0x6f});

        // encode the message using the MessageFactory
        MessageFactory factory = new MessageFactory();
        byte[] bytes = factory.encode(message);

        // check that the encoded byte array matches the expected value
        assertArrayEquals(new byte[]{0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x68, 0x65, 0x6c, 0x6c, 0x6f}, bytes);
    }

    @Test
    void testEncodeSettingsMessage() throws BadAttributeException, IOException {
        // create a sample Settings message
        Settings message = new Settings();

        // encode the message using the MessageFactory
        MessageFactory factory = new MessageFactory();
        byte[] bytes = factory.encode(message);

        // check that the encoded byte array matches the expected value
        assertArrayEquals(new byte[]{0x04, 0x01, 0x00, 0x00, 0x00, 0x00}, bytes);
    }

    @Test
    void testEncodeWindowUpdateMessage() throws BadAttributeException, IOException {
        // create a sample Window_Update message
        Window_Update message = new Window_Update(8, 9);

        // encode the message using the MessageFactory
        MessageFactory factory = new MessageFactory();
        byte[] bytes = factory.encode(message);

        // check that the encoded byte array matches the expected value
        assertArrayEquals(new byte[]{0x08, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x09}, bytes);
    }

    @Test
    void testDecodeInvalidDataMessageFlag() {
        // create a sample byte array representing a DATA message with stream ID 123,
        // end stream flag set to true, and an invalid flag (bit 3 set)
        byte[] bytes = new byte[] {0x00, 0x08, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x05, 0x68, 0x65, 0x6c, 0x6c, 0x6f};

        // decode the byte array using the MessageFactory and expect a BadAttributeException to be thrown
        MessageFactory factory = new MessageFactory();
        assertThrows(BadAttributeException.class, () -> factory.decode(bytes));
    }

    @Test
    void testDecodeInvalidWindowUpdateMessageFlag() {
        // create a sample byte array representing a WINDOW_UPDATE message with stream ID 1 and an Flag
        byte[] bytes = new byte[] {0x08, 0x01, 0x00, 0x00, 0x00, 0x01};

        // decode the byte array using the MessageFactory and expect a BadAttributeException to be thrown
        MessageFactory factory = new MessageFactory();
        assertThrows(BadAttributeException.class, () -> factory.decode(bytes));
    }


    @Test
    void testDecodeUnknownMessageType() {
        // create a sample byte array representing an unknown message type (0xFF)
        byte[] bytes = new byte[] { (byte)0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        // decode the byte array using the MessageFactory and expect a BadAttributeException to be thrown
        MessageFactory factory = new MessageFactory();
        assertThrows(BadAttributeException.class, () -> factory.decode(bytes));
    }

    @Test
    void testEncodeWindowUpdateMessageLargeIncrement() throws IOException, BadAttributeException {
        // create a sample Window_Update message with a large increment value
        int streamID = 1;
        int increment = Integer.MAX_VALUE; // maximum int value
        Window_Update message = null;
        try {
            message = new Window_Update(streamID, increment);
        } catch (BadAttributeException e) {
            fail("BadAttributeException should not be thrown");
        }

        // encode the message using the MessageFactory
        MessageFactory factory = new MessageFactory();
        byte[] encodedBytes = factory.encode(message);

        // check that the encoded bytes represent a valid message with the correct type, stream ID, and payload
        byte[] expectedBytes = new byte[] {0x08, 0x00, 0x00, 0x00, 0x00, 0x01, 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        assertArrayEquals(expectedBytes, encodedBytes);
    }

}