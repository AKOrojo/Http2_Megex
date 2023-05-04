/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/

package megex.serialization;

import java.util.Objects;

/**

 The Settings class represents an HTTP/2 SETTINGS message.
 This class extends the Message class and overrides the toString() method to return a string representation of the object.
 */

public class Settings extends Message {
    private static final int CODECONSTANT = 0x4;

    /**
     * Constructor for creating a new Settings instance.
     *
     * @throws BadAttributeException if there is an error with the message attributes.
     */
    public Settings() throws BadAttributeException {

    }

    /**
     * Returns a string representation of the Settings object.
     * The returned string includes the stream ID.
     *
     * @return A string representation of the Settings object.
     */
    @Override
    public String toString() {
        return "Settings: StreamID=" + getStreamID();
    }

    /**
     * Compares the Settings object with another object for equality.
     *
     * @param obj The object to compare to.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Settings settings = (Settings) obj;
        return code == settings.code && getStreamID() == settings.getStreamID();
    }

    /**
     * Generates a hash code for the Settings object.
     *
     * @return The hash code for the object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(code, getStreamID());
    }

    /**
     * Sets the stream ID for the message.
     *
     * @param streamID The stream ID to set.
     * @throws BadAttributeException If the stream ID is invalid.
     */
    @Override
    public void setStreamID(int streamID) throws BadAttributeException {
        if (streamID != 0) {
            throw new BadAttributeException("Invalid stream ID", "streamID");
        }
        super.setStreamID(streamID);
    }

    /**
     * Returns the code associated with the specific implementation.
     *
     * @return the byte value representing the code constant
     */
    @Override
    public byte getCode() {
        return CODECONSTANT;
    }
}