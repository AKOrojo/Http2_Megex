/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/

package megex.serialization;

/**
 * Represents a message in a custom protocol.
 */
public abstract class Message {

    private static final int MAX_STREAM_ID = 0x7fffffff;
    /**
     * The code of the message.
     */
    protected byte code;

    /**
     * The stream ID of the message.
     */
    protected int streamID;

    /**
     * Gets the code of the message.
     *
     * @return The code of the message.
     */
    public byte getCode() {
        return code;
    }

    /**
     * Gets the stream ID of the message.
     *
     * @return The stream ID of the message.
     */
    public int getStreamID() {
        return streamID;
    }

    /**
     * Sets the stream ID of the message.
     *
     * @param streamID The new stream ID to set.
     * @throws BadAttributeException If the stream ID is null or negative.
     */
    public void setStreamID(int streamID) throws BadAttributeException {
        // Check if the stream ID is null or negative, and throw an exception if it is
        if (streamID < 0 || streamID > MAX_STREAM_ID) {
            throw new BadAttributeException("Invalid stream ID: " + streamID, "streamID");
        }

        // Set the stream ID
        this.streamID = streamID;
    }

    @Override
    public abstract String toString();


}
