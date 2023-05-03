/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/

package megex.serialization;


import java.util.*;

public class Headers extends Message {
    private boolean end;
    private LinkedHashMap<String, String> headers;

    private static final int CODECONSTANT = 0x1;

    /**
     * Creates a new Headers message instance with the given stream ID and end flag.
     *
     * @param streamID The ID of the stream for the message.
     * @param end      The value of the END_STREAM flag.
     * @throws BadAttributeException if the stream ID is zero or negative.
     */
    public Headers(int streamID, boolean end) throws BadAttributeException {
        checkStreamID(streamID);
        setEnd(end);
        this.headers = new LinkedHashMap<>();
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
     * Returns the value of the END_STREAM flag for the message.
     *
     * @return The value of the END_STREAM flag.
     */
    public boolean isEnd() {
        return end;
    }

    /**
     * Sets the value of the END_STREAM flag for the message.
     *
     * @param end The value of the END_STREAM flag.
     */
    public void setEnd(boolean end) {
        this.end = end;
    }

    /**
     * Adds a name-value pair to the header block.
     *
     * @param name  The name of the header field.
     * @param value The value of the header field.
     * @throws BadAttributeException if the name or value is invalid.
     */
    public void addValue(String name, String value) throws BadAttributeException {
        if (name == null || name.isEmpty() || !name.matches("^[^A-Z][ -~]*$") || name.contains(";") || name.contains(" ")) {
            throw new BadAttributeException("Invalid header field name: " + name, "name");
        }

        if (getNames().contains(name)){
            headers.remove(name);
        }

        if (value == null || value.isEmpty() || !value.matches("^[ -~]*$")) {
            throw new BadAttributeException("Invalid header field value: " + value, "value");
        }


        headers.put(name.toLowerCase(), value);
    }

    /**
     * Returns the value of the header field with the given name, or null if the name is not found.
     *
     * @param name The name of the header field.
     * @return The value of the header field, or null if not found.
     */
    public String getValue(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return headers.get(name.toLowerCase());
    }


    /**
     * Returns a set of all the header field names in the header block.
     *
     * @return A set of all the header field names in the header block.
     */
    public Set<String> getNames() {
        return headers.keySet();
    }

    /**
     * Returns a string representation of the Headers message.
     *
     * @return A string representation of the Headers message.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Headers: StreamID=").append(getStreamID()).append(" isEnd=").append(end).append(" (");

        // Sort the header names in reverse order
        List<String> sortedNames = new ArrayList<>(headers.keySet());
        Collections.sort(sortedNames, Collections.reverseOrder());

        for (String name : sortedNames) {
            sb.append("[").append(name).append("=").append(headers.get(name)).append("]");
        }
        sb.append(")");
        return sb.toString();
    }
    /**
     * Generates a hash code for the Headers object.
     *
     * @return The hash code for the object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(end, headers, code, streamID);
    }

    /**
     * Compares the Headers object with another object for equality.
     *
     * @param o The object to compare to.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Headers)) {
            return false;
        }
        Headers headers1 = (Headers) o;
        return end == headers1.end && Objects.equals(headers, headers1.headers) && code == headers1.code && streamID == headers1.streamID;
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
