package megex.serialization.test;

import megex.serialization.BadAttributeException;
import megex.serialization.Window_Update;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Window_UpdateTest {
    @Test
    public void testCreateWindowUpdate() throws BadAttributeException {
        Window_Update windowUpdate = new Window_Update(5, 1024);
        assertEquals(windowUpdate.getCode(), 8);
        assertEquals(windowUpdate.getStreamID(), 5);
        assertEquals(windowUpdate.getIncrement(), 1024);
    }

    @Test
    public void testSetStreamID() throws BadAttributeException {
        Window_Update windowUpdate = new Window_Update(5, 1024);
        windowUpdate.setStreamID(10);
        assertEquals(windowUpdate.getStreamID(), 10);
    }

    @Test
    public void testSetIncrement() throws BadAttributeException {
        Window_Update windowUpdate = new Window_Update(5, 1024);
        windowUpdate.setIncrement(2048);
        assertEquals(windowUpdate.getIncrement(), 2048);
    }

    @Test
    public void testToString() throws BadAttributeException {
        Window_Update windowUpdate = new Window_Update(5, 1024);
        assertEquals(windowUpdate.toString(), "Window_Update: StreamID=5 increment=1024");
    }
}