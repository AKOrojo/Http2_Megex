package megex.serialization.test;

import megex.serialization.BadAttributeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class BadAttributeExceptionTest {

    @Test
    public void testConstructorWithMessageAndAttribute() {
        String message = "Problem with attribute";
        String attribute = "age";
        BadAttributeException exception = new BadAttributeException(message, attribute);
        assertEquals(message, exception.getMessage());
        assertEquals(attribute, exception.getAttribute());
    }

    @Test
    public void testConstructorWithMessageAttributeAndCause() {
        String message = "Problem with attribute";
        String attribute = "age";
        Throwable cause = new IllegalArgumentException("Invalid age value");
        BadAttributeException exception = new BadAttributeException(message, attribute, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(attribute, exception.getAttribute());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithNullMessage() {
        assertThrows(NullPointerException.class, () -> new BadAttributeException(null, "age"));
    }

    @Test
    public void testConstructorWithNullAttributes() {
        assertThrows(NullPointerException.class, () -> new BadAttributeException("Problem with attribute", null));
    }

}
