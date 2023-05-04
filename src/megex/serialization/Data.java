/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/
package megex.serialization;

import java.util.Arrays;
import java.util.Objects;

/**

 The Data class represents a Data message in a custom protocol.
 A Data message consists of a stream ID, an end flag, and payload data.
 It extends the Message class and throws BadAttributeException if necessary.
 */

public class Data extends Message {
    private boolean isEnd;
    private byte[] data;
    private static final int MAX_DATA_LENGTH = 16384; // 1 MB

    private static final int CODECONSTANT = 0X0;

    /**
     * Constructor for creating a new Data instance.
     *
     * @param streamID the stream ID for the message
     * @param isEnd    true if this is the last frame for the stream, false otherwise
     * @param data     the message payload data
     * @throws BadAttributeException if the streamID is negative or zero,
     *                               if the data is null, or if the length of the data exceeds the maximum allowed length
     */
    public Data(int streamID, boolean isEnd, byte[] data) throws BadAttributeException {
        checkStreamID(streamID);
        setData(data);
        setEnd(isEnd);
    }

    /**
     * Throws an exception if the streamID is negative or zero.
     *
     * @param streamID the stream ID for the message
     * @throws BadAttributeException if the streamID is negative or zero
     */
    private void checkStreamID(int streamID) throws BadAttributeException {
        if (streamID <= 0) {
            throw new BadAttributeException("Stream ID must be non-negative and non-zero", "streamID");
        }
        this.streamID = streamID;
    }

    /**
     * Getter method for the end flag.
     *
     * @return true if this is the last frame for the stream, false otherwise
     */
    public boolean isEnd() {
        return isEnd;
    }

    /**
     * Setter method for the end flag.
     *
     * @param end true if this is the last frame for the stream, false otherwise
     */
    public void setEnd(boolean end) {
        isEnd = end;
    }

    /**
     * Getter method for the message payload data.
     *
     * @return the message payload data as a byte array
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Setter method for the message payload data.
     *
     * @param data the message payload data as a byte array
     * @throws BadAttributeException if the data is null or if the length of the data exceeds the maximum allowed length
     */
    public void setData(byte[] data) throws BadAttributeException {
        if (data == null) {
            throw new BadAttributeException("Data cannot be null", "data");
        }

        if (data.length > MAX_DATA_LENGTH) {
            throw new BadAttributeException("Data length exceeds maximum limit", "data");
        }

        this.data = data;
    }

    /**
     * Overrides the setStreamID method to throw an exception if the streamID is negative or zero.
     *
     * @param streamID the stream ID for the message
     * @throws BadAttributeException if the streamID is negative or zero
     */
    @Override
    public void setStreamID(int streamID) throws BadAttributeException {
        checkStreamID(streamID);
    }

    /**
     * Returns a string representation of the Data object.
     *
     * @return a string representation of the Data object, including the stream ID, end flag, and payload data length
     */
    @Override
    public String toString() {
        return "Data: StreamID=" + getStreamID() + " isEnd=" + isEnd + " data=" + data.length;
    }

    /**
     * Overrides the equals method to compare the Data object with another object.
     *
     * @param obj the object to compare
     * @return true if the two objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Data)) {
            return false;
        }
        Data other = (Data) obj;
        return this.code == other.code
                && this.streamID == other.streamID
                && this.isEnd == other.isEnd
                && Arrays.equals(this.data, other.data);
    }

    /**
     * Overrides the hashCode method to return the hash code of the Data object.
     *
     * @return the hash code of the Data object
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(isEnd, streamID);
        result = 31 * result + Arrays.hashCode(data);
        return result;
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
