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

 The Window_Update class represents an HTTP/2 WINDOW_UPDATE message.
 This class extends the Message class and overrides the toString() method to return a string representation of the object.
 The class contains methods to set and get the increment value of the message.
 */
public class Window_Update extends Message {
    private static final int WINDOW_UPDATE_CODE = 0x8;
    private int increment;

    /**
     * Constructs a new `Window_Update` instance with the given stream ID and increment value.
     *
     * @param streamID  The ID of the stream for the message.
     * @param increment The increment value.
     * @throws BadAttributeException if the increment value is negative.
     */
    public Window_Update(int streamID, int increment) throws BadAttributeException {
        checkStreamID(streamID);
        setIncrement(increment);
        this.streamID = streamID;
    }

    /**
     * Returns the increment value for the message.
     *
     * @return The increment value.
     */
    public int getIncrement() {
        return increment;
    }

    /**
     * Sets the increment value for the message.
     *
     * @param increment The increment value.
     * @throws BadAttributeException if the increment value is negative or zero.
     */
    public void setIncrement(int increment) throws BadAttributeException {
        if (increment <= 0) {
            throw new BadAttributeException("Increment must be positive", "increment");
        }
        this.increment = increment;
    }

    /**
     * Returns a string representation of the message.
     *
     * @return A string representation of the message.
     */
    @Override
    public String toString() {
        return "Window_Update: StreamID=" + getStreamID() + " increment=" + increment;
    }

    /**
     * Compares the Window_Update object with another object for equality.
     *
     * @param o The object to compare to.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Window_Update)) {
            return false;
        }
        Window_Update that = (Window_Update) o;
        return streamID == that.streamID && increment == that.increment;
    }

    /**
     * Generates a hash code for the Window_Update object.
     *
     * @return The hash code for the object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(streamID, increment);
    }

    /**
     * Checks if the stream ID is valid.
     *
     * @param streamID The stream ID to check.
     * @throws BadAttributeException If the stream ID is invalid.
     */
    private void checkStreamID(int streamID) throws BadAttributeException {
        if (streamID < 0) {
            throw new BadAttributeException("Stream ID must be non-negative", "streamID");
        }
        this.streamID = streamID;
    }

    /**
     * Returns the code associated with the specific implementation.
     *
     * @return the byte value representing the code constant
     */
    @Override
    public byte getCode() {
        return WINDOW_UPDATE_CODE;
    }
}