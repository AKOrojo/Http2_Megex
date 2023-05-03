/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/
package megex.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A utility class for reading frames from an input stream.
 *
 * Instances of this class can be used to read frames from an input stream that
 * are encoded using the MegEx binary protocol. The `getFrame` method reads the
 * length of the next frame from the input stream and then reads the frame itself.
 * If the length of the frame is too large, the method throws an `IllegalArgumentException`.
 * If the end of the input stream is reached unexpectedly, the method throws an `EOFException`.
 *
 * Example usage:
 * <pre>{@code
 * InputStream in = ...; // create an input stream
 * Deframer deframer = new Deframer(in);
 * while (true) {
 *     byte[] frame = deframer.getFrame();
 *     // process the frame
 * }
 * }</pre>
 */
public class Deframer {
    // Input stream to read from
    private final InputStream in;
    private final int MAX_PAYLOAD_LENGTH = 16384;

    /**
     * Constructor for creating a new Deframer instance.
     *
     * @param in the input stream to read from
     * @throws NullPointerException if the input stream is null
     */
    public Deframer(InputStream in) {
        // Check if input stream is null
        if (in == null) {
            throw new NullPointerException("Input stream cannot be null");
        }
        this.in = in;
    }

    /**
     * Gets the next frame from the input stream.
     *
     * @return a byte array containing the next frame from the input stream
     * @throws IOException if an I/O error occurs
     * @throws EOFException if the end of the input stream has been reached unexpectedly
     * @throws IllegalArgumentException if the frame length is too large or negative
     */
    public byte[] getFrame() throws IOException {
        // Read the length of the frame
        int length = 0;
        for (int i = 0; i < 3; i++) {
            int b = in.read();
            // Check if end of stream has been reached
            if (b == -1) {
                throw new EOFException();
            }
            length = (length << 8) | b;
        }

        // Check that payload length is not negative or greater than maximum allowed
        int payloadLength = length;
        if (payloadLength < 0 || payloadLength > MAX_PAYLOAD_LENGTH) {
            throw new IllegalArgumentException("Invalid payload length: " + payloadLength);
        }

        int messageLength = length + 6;
        // Read the frame
        byte[] frame = new byte[messageLength];
        int totalBytesRead = 0;
        while (totalBytesRead < messageLength) {
            // Read bytes into the frame buffer
            int bytesRead = in.read(frame, totalBytesRead, messageLength - totalBytesRead);
            // Check if end of stream has been reached
            if (bytesRead < 0) {
                throw new EOFException("Premature end of input stream");
            }
            totalBytesRead += bytesRead;
        }

        // Return the frame
        return frame;
    }
}
