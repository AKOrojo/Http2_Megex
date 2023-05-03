package megex.serialization.test;

import megex.serialization.BadAttributeException;
import megex.serialization.Settings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SettingsTest {
    @Test
    public void testCreateSettings() throws BadAttributeException {
        Settings settings = new Settings();
        assertEquals(settings.getCode(), 4);
        assertEquals(settings.getStreamID(), 0);
    }

    @Test
    public void testSetStreamID() throws BadAttributeException {
        Settings settings = new Settings();
        settings.setStreamID(0);
        assertEquals(settings.getStreamID(), 0);
    }

    @Test
    public void testToString() throws BadAttributeException {
        Settings settings = new Settings();
        assertEquals(settings.toString(), "Settings: StreamID=0");
    }
}
