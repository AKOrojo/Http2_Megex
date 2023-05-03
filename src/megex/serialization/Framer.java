/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/

package megex.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * A utility class for writing frames to an output stream.
 *
 * Instances of this class can be used to write frames to an output stream that
 * are encoded using the MegEx binary protocol. The `putFrame` method writes the length
 * of the message and the message itself to the output stream. If the message length
 * is too large, the method throws an `IllegalArgumentException`. If the message is null
 * or empty, the method throws an `IllegalArgumentException`.
 */
public class Framer {
    // The output stream to write to
    private final OutputStream out;
    private final int MAX_LENGTH = 16384;

    /**
     * Constructs a new `Framer` instance that writes to the specified output stream.
     *
     * @param out the output stream to write to
     * @throws NullPointerException if the output stream is null
     */
    public Framer(OutputStream out) {
        this.out = Objects.requireNonNull(out, "Output stream is null");
    }

    /**
     * Writes a frame to the output stream.
     *
     * The method writes the length of the message as a 3-byte big-endian integer,
     * followed by the message bytes. If the length of the message is greater than
     * 16384 bytes, or if the message is null or empty, the method throws an
     * `IllegalArgumentException`. If an I/O error occurs while writing to the output
     * stream, the method throws an `IOException`.
     *
     * @param message the message to write
     * @throws NullPointerException if the message is null
     * @throws IllegalArgumentException if the message is too long, too short, or empty
     * @throws IOException if an I/O error occurs while writing to the output stream
     */
    public void putFrame(byte[] message) throws IOException {
        Objects.requireNonNull(message, "Message is null");
        int payloadLength = message.length - 6;

        if (payloadLength > MAX_LENGTH || payloadLength < 0) {
            throw new IllegalArgumentException("Invalid message length: " + payloadLength);
        }

        writeMessageLength(payloadLength);
        out.write(message);
        out.flush();
    }

    /**
     * Writes the length of the message to the output stream as a 3-byte big-endian integer.
     *
     * @param length the length of the message
     * @throws IllegalArgumentException if the length is invalid (greater than 16384 or less than 0)
     * @throws IOException if an I/O error occurs while writing to the output stream
     */
    private void writeMessageLength(int length) throws IOException {
        if (length > MAX_LENGTH || length < 0) {
            throw new IllegalArgumentException("Invalid message length: " + length);
        }

        byte[] lengthBytes = new byte[3];
        lengthBytes[0] = (byte) ((length >> 16) & 0xFF); // write the first byte of the length
        lengthBytes[1] = (byte) ((length >> 8) & 0xFF); // write the second byte of the length
        lengthBytes[2] = (byte) (length & 0xFF); // write the third byte of the length

        out.write(lengthBytes);
    }

    public void putBytes(byte[] bytes) throws IOException {
        out.write(bytes);
    }
}
