/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/
package megex.serialization;
import java.io.Serializable;

/**

 The BadAttributeException class is an exception thrown when an attribute is invalid.
 It extends the Exception class and implements the Serializable interface.
 */
public class BadAttributeException extends Exception implements Serializable {
    /**
     * The name of the invalid attribute.
     */
    private String attribute;
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a BadAttributeException with a message and an attribute name.
     *
     * @param message   the message describing the exception
     * @param attribute the name of the invalid attribute
     * @throws NullPointerException if either attribute or message is null
     */
    public BadAttributeException(String message, String attribute) throws NullPointerException {
        super(message);
        nullChecker(attribute, message);
    }

    /**
     * Constructs a BadAttributeException with a message, an attribute name, and a cause.
     *
     * @param message   the message describing the exception
     * @param attribute the name of the invalid attribute
     * @param cause     the cause of the exception
     * @throws NullPointerException if either attribute or message is null
     */
    public BadAttributeException(String message, String attribute, Throwable cause) throws NullPointerException {
        super(message, cause);
        nullChecker(attribute, message);
        this.attribute = attribute;
    }

    /**
     * Checks whether attribute or message is null, and throws a NullPointerException if either is null.
     *
     * @param attribute the name of the invalid attribute
     * @param message   the message describing the exception
     * @throws NullPointerException if either attribute or message is null
     */
    private void nullChecker(String attribute, String message) throws NullPointerException {
        if (attribute == null || message == null) {
            throw new NullPointerException("Attribute must not be null");
        }
        this.attribute = attribute;
    }

    /**
     * Returns the name of the invalid attribute.
     *
     * @return the name of the invalid attribute
     */
    public String getAttribute() {
        return attribute;
    }

}