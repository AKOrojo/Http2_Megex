package megex.serialization.test;

import com.twitter.hpack.Encoder;
import megex.serialization.BadAttributeException;
import megex.serialization.Headers;
import megex.serialization.Message;
import megex.serialization.MessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HeadersTest {
    private Headers headers;
    private static final Charset CHARENC = StandardCharsets.US_ASCII;
    private static final int MAXHEADERTBLSZ = 1024;

    @BeforeEach
    void setUp() throws BadAttributeException {
        headers = new Headers(1, true);
    }

    @Test
    void testGetNamesEmpty() {
        assertEquals(0, headers.getNames().size());
    }

    @Test
    void testAddValue() throws BadAttributeException {
        headers.addValue("name1", "value1");
        headers.addValue("name2", "value2");
        headers.addValue("name3", "value3");
        assertEquals(3, headers.getNames().size());
        assertEquals("value1", headers.getValue("name1"));
        assertEquals("value2", headers.getValue("name2"));
        assertEquals("value3", headers.getValue("name3"));
    }

    @Test
    void testAddValueDuplicate() throws BadAttributeException {
        headers.addValue("name1", "value1");
        headers.addValue("name2", "value2");
        headers.addValue("name3", "value3");
        headers.addValue("name1", "value4");
        assertEquals(3, headers.getNames().size());
        assertEquals("value4", headers.getValue("name1"));
        assertEquals("value2", headers.getValue("name2"));
        assertEquals("value3", headers.getValue("name3"));
    }

    private static byte[] s2b(String v) {
        return v.getBytes(CHARENC);
    }
    @Test
    void testEncodeValidRewrite() throws BadAttributeException, IOException {
        int streamId = 4;
        boolean endStream = true;

        // Instantiate your Headers class
        Headers headers = new Headers(streamId, endStream);

        // Add initial header values to the Headers instance
        headers.addValue(":method", "GET");
        headers.addValue(":path", "/index");
        headers.addValue(":version", "HTTP/2.1");

        // Update (rewrite) header values in the Headers instance
        headers.addValue(":path", "/foo");
        headers.addValue(":version", "HTTP/1.1");

        // Instantiate the MessageFactory
        MessageFactory messageFactory = new MessageFactory();

        // Call the encode method using the MessageFactory to get the encoded byte array (actual)
        byte[] encoded = messageFactory.encode(headers);
        System.out.println("aaa"+Arrays.toString(encoded));

        // Create a LinkedHashMap to hold the expected header values
        LinkedHashMap<String, String> expectedHeaders = new LinkedHashMap<>();
        expectedHeaders.put(":method", "GET");
        expectedHeaders.put(":path", "/foo");
        expectedHeaders.put(":version", "HTTP/1.1");

        // Encode the expected header list into a header block using HPACK
        Encoder encoder = new Encoder(MAXHEADERTBLSZ);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (Map.Entry<String, String> entry : expectedHeaders.entrySet()) {
            encoder.encodeHeader(out, s2b(entry.getKey()), s2b(entry.getValue()), false);
        }

        byte[] payload = out.toByteArray();

        byte[] headersFrameHeader = new byte[]{1, 5, 0, 0, 0, 4};

        // Create a new byte array to hold the expected encoded data

        byte[] expectedEncoded = new byte[headersFrameHeader.length + payload.length];


        // Copy both arrays into the new array
        System.arraycopy(headersFrameHeader, 0, expectedEncoded, 0, headersFrameHeader.length);
        System.arraycopy(payload, 0, expectedEncoded, headersFrameHeader.length, payload.length);
//        System.out.printf(Arrays.toString(expectedEncoded));

        byte[] enc = {1, 5, 0, 0, 0, 4, 64, 68, -125, 98, 83, -97, 64, -122, -71, -36, -74, 32, -57, -85, -121, -57, -65, 126, -74, 2, -72, 127};
//
        Message x = messageFactory.decode(enc);
        System.out.println(x);
//        Message x = messageFactory.decode(expectedEncoded);
//        System.out.println(x);

        // Assert that the encoded byte array (actual) matches the expected byte array
        assertArrayEquals(expectedEncoded, encoded);
    }

}


