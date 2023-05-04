package megex.serialization.test;

import megex.serialization.BadAttributeException;
import megex.serialization.Data;
import megex.serialization.Message;
import megex.serialization.Settings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessageTest {
    @Test
    public void testGetCode() throws BadAttributeException {
        Settings message = new Settings();
        byte code = message.getCode();
        assertEquals(4, code);
    }

    @Test
    public void testGetStreamID() throws BadAttributeException {
        Settings message = new Settings();
        int streamID = message.getStreamID();
        assertEquals(0, streamID);
    }

    @Test
    public void testSetStreamID() throws BadAttributeException {
        Settings message = new Settings();
        message.setStreamID(0);
        assertEquals(0, message.getStreamID());
    }

    @Test
    public void testSetStreamIDInvalid() throws BadAttributeException {
        Settings message = new Settings();
        assertThrows(BadAttributeException.class, () -> message.setStreamID(-5));
    }

}
