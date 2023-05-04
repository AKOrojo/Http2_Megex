package megex.serialization.test;

import megex.serialization.BadAttributeException;
import megex.serialization.MessageFactory;
import megex.serialization.Window_Update;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DrDonahooTest {

    @DisplayName("decode")
    @Test
    void testDecode() throws IOException, BadAttributeException {
        MessageFactory f = new MessageFactory();
        Window_Update w = (Window_Update) f.decode(new byte[] { 8, 0, 0, 0, 0, 5, 0, 0, 0, 10 });
        assertAll(() -> assertEquals(5, w.getStreamID()), () -> assertEquals(10, w.getIncrement()),
                () -> assertEquals(8, w.getCode()));
    }
}
