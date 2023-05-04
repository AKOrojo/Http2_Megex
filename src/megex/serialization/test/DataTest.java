package megex.serialization.test;

import megex.serialization.BadAttributeException;
import megex.serialization.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataTest {

    @Test
    public void testData() throws BadAttributeException {
        int streamID = 5;
        boolean isEnd = true;
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};

        Data dataMessage = new Data(streamID, isEnd, data);

        // Test isEnd method
        assertEquals(isEnd, dataMessage.isEnd());

        // Test setEnd method
        isEnd = false;
        dataMessage.setEnd(isEnd);
        assertEquals(isEnd, dataMessage.isEnd());

        // Test getData method
        assertArrayEquals(data, dataMessage.getData());

        // Test setData method
        data = new byte[]{0x06, 0x07, 0x08, 0x09, 0x0A};
        dataMessage.setData(data);
        assertArrayEquals(data, dataMessage.getData());

        // Test toString method
        String expectedString = "Data: StreamID=" + streamID + " isEnd=" + isEnd + " data=" + data.length;
        assertEquals(expectedString, dataMessage.toString());
    }

}
