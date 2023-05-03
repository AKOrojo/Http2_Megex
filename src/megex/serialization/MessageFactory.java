/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/

package megex.serialization;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.twitter.hpack.Encoder;
import com.twitter.hpack.Decoder;

import com.twitter.hpack.HeaderListener;


/**

 This class represents a factory for creating different types of messages.
 It provides methods to create various types of messages, such as data, headers, settings, and window update messages.
 It also uses a MessageDecoder to decode the incoming byte message into a message object.
 */
public class MessageFactory {
    private static final Charset CHARENC = StandardCharsets.US_ASCII;
    private static final int MAXHEADERSZ = 1024;
    private static final int MAXHEADERTBLSZ = 1024;

    private final Decoder decoder;
    private final Encoder encoder;

    public MessageFactory() {
        this.decoder = new Decoder(MAXHEADERSZ, MAXHEADERTBLSZ);
        this.encoder = new Encoder(MAXHEADERTBLSZ);
    }

    /**
     * Decodes a byte array into a Message object.
     *
     * @param msgBytes The byte array to decode.
     * @return The decoded Message object.
     * @throws BadAttributeException if the byte array is null or has an invalid format.
     * @throws IOException           if an I/O error occurs.
     */
    public Message decode(byte[] msgBytes) throws BadAttributeException, IOException {

        checkIfMsgBytesIsValid(msgBytes);

        // Extract the first 6 bytes from msgBytes, which contain the message header
        byte[] header = new byte[6];
        System.arraycopy(msgBytes, 0, header, 0, 6);

        // Parse the message type, flags, stream ID, and length from the header
        byte type = header[0];
        byte flags = header[1];
        int streamID = getStreamID(header, 2) & 0x7fffffff; // ignore the reserved bit
        int length = msgBytes.length - 6;

        // Check if msgBytes is too short to contain the entire message
        if (msgBytes.length < 6 + length) {
            throw new BadAttributeException("Invalid message format", "message");
        }

        // Extract the payload from msgBytes
        byte[] payload = new byte[length];
        System.arraycopy(msgBytes, 6, payload, 0, length);

        // Decode the message based on its type and return the resulting Message object
        Message message = null;
        switch (type) {
            case 0x0 -> { // DATA
                if (streamID <= 0) {
                    throw new BadAttributeException("Stream ID must be non-negative", "streamID");
                }

                if (streamID > 0) {
                    boolean endStream = (flags & 0x1) != 0;
                    if ((flags & 0x8) != 0) {
                        throw new BadAttributeException("Bad Flag for Data Frame", "flags");
                    }
                    message = new Data(streamID, endStream, payload);
                }
            }

            case 0x1 -> { // HEADERS
                if (streamID <= 0) {
                    throw new BadAttributeException("Stream ID must be non-negative", "streamID");
                }
                boolean endStream = (flags & 0x1) != 0;
                if ((flags & 0x4) == 0 || (flags & 0x8) != 0) {
                    throw new BadAttributeException("Invalid Flags for Headers Frame", "flags");
                }

                // If the 0x20 flag is set, ignore the first 5 bytes of the header block
                if ((flags & 0x20) != 0) {
                    payload = Arrays.copyOfRange(payload, 5, payload.length);
                }

                // Decompress the header name/value pairs using HPACK compression
                ByteArrayInputStream in = new ByteArrayInputStream(payload);
                Headers headersMsg = new Headers(streamID, endStream);
                try {
                    decoder.decode(in, (name, value, sensitive) -> {
                        try {
                            String decodedName = b2s(name).toLowerCase(); // Ensure the name is in lowercase
                            headersMsg.addValue(decodedName, b2s(value));
                        } catch (BadAttributeException e) {
                            throw new WrapperException(e);
                        }
                    });
                    decoder.endHeaderBlock();
                } catch (WrapperException e) {
                    throw (BadAttributeException) e.getCause();
                } catch (IOException e) {
                    if (e.getMessage().equals("illegal index value")) {
                        throw new BadAttributeException("Illegal index value", "headers");
                    } else {
                        throw e;
                    }
                }
                message = headersMsg;
            }
                case 0x4 -> { // SETTINGS
                if (streamID == 0) {
                    message = new Settings();
                } else {
                    throw new BadAttributeException("Invalid Flag", "message");
                }
            }
            case 0x8 -> { // WINDOW_UPDATE
                if (flags == 0) {
                    int increment = getIncrement(payload) & 0x7fffffff; // ignore the reserved bit
                    message = new Window_Update(streamID, increment);
                } else {
                    throw new BadAttributeException("Invalid Flag", "message");
                }
            }
            default -> throw new BadAttributeException("Unknown message type ", String.format("{0}", type));
        }

        // Return the decoded message object
        return message;
    }

    private static class WrapperException extends RuntimeException {
        public WrapperException(Throwable cause) {
            super(cause);
        }
    }
    private static String b2s(byte[] b) {
        return new String(b, CHARENC);
    }

    /**

     Checks whether the given byte array is null or too short to contain a valid message.

     @param msgBytes the byte array to be checked.

     @throws BadAttributeException if the byte array is null or too short to contain a valid message.
     */
    private void checkIfMsgBytesIsValid(byte[] msgBytes) throws BadAttributeException {
        // Check if msgBytes is null
        if (msgBytes == null) {
            throw new NullPointerException("Message bytes cannot be null");
        }

        // Check if msgBytes is too short to contain a valid message
        if (msgBytes.length < 6) {
            throw new BadAttributeException("Invalid message format", "message");
        }
    }

    /**

     Encodes a Message object into a byte array.

     @param msg The Message object to encode.

     @return The encoded byte array.

     @throws NullPointerException if the Message object is null.

     @throws IllegalArgumentException if the Message object has an unknown type.

     @throws IOException if an I/O error occurs during compression.
     */
    public byte[] encode(Message msg) throws IOException, BadAttributeException {
        // Check if msg is null
        if (msg == null) {
            throw new NullPointerException("Message cannot be null");
        }

        // Create a header array and set the first 3 bytes based on the message type
        byte[] header = new byte[9];
        header[0] = msg.getCode();
        switch (msg.getCode()) {
            case 0x0: // DATA
                if (((Data) msg).isEnd()) {
                    setEndStreamFlag(header);
                }
                break;
            case 0x1: // HEADERS
                if (((Headers) msg).isEnd()) {
                    setEndStreamFlag(header);
                }
                header[1] |= 0x4; // set the required flag (0x4) for HEADERS frame
                break;
            case 0x4: // SETTINGS
                header[1] |= 0x1; // set the flags field to Ox1
                break;
            case 0x8: // WINDOW_UPDATE
            // no flags for WINDOW_UPDATE
                break;
            default:
                throw new BadAttributeException("Unknown message type", String.format("0x%02X", header[0]));
        }

        // Set the stream ID in the header
        setStreamID(header, msg.getStreamID());

        // Get the payload from the message
        byte[] payload = getPayload(msg);

        // Compress the payload using HPACK compression
        // payload = Encoder.encode(payload);

        // Concatenate the header and payload into a single byte array
        byte[] result = new byte[6 + payload.length];
        System.arraycopy(header, 0, result, 0, 6);
        System.arraycopy(payload, 0, result, 6, payload.length);

        // Return the encoded message as a byte array
        return result;
    }

    /**

     Returns the payload of the message as a byte array.
     @param msg The message to extract the payload from.
     @return The payload of the message.
     @throws IOException if an I/O error occurs.
     */
    private byte[] getPayload(Message msg) throws IOException {
        byte[] payload = new byte[0];
        if (msg instanceof Data) {
            payload = ((Data) msg).getData();
        } else if (msg instanceof Window_Update) {
            int reservedBitMask = 0x7fffffff;
            int payloadInt = (((Window_Update) msg).getIncrement() & reservedBitMask);
            payload = getIncrementBytes(payloadInt);
        } else if (msg instanceof Headers) {
            Headers headers = (Headers) msg;

            // encode header list into header block
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Set<String> names = headers.getNames();
            for (String name : names) {
                String value = headers.getValue(name);
                encoder.encodeHeader(out, s2b(name), s2b(value), false);
            }

            payload = out.toByteArray();


        }
        return payload;
    }

    private static byte[] s2b(String v) {
            return v.getBytes(CHARENC);
    }

    /**
     * Sets the end stream flag in the message header.
     *
     * @param header The message header.
     */
    private void setEndStreamFlag(byte[] header) {
        header[1] |= 0x1;
    }

    /**
     * Sets the stream ID in the message header.
     *
     * @param header   The message header.
     * @param streamID The stream ID to set.
     */
    private void setStreamID(byte[] header, int streamID) {
        // Set the R bit to 0
        header[2] = (byte) ((streamID >>> 24) & 0x7f);
        header[3] = (byte) (streamID >>> 16);
        header[4] = (byte) (streamID >>> 8);
        header[5] = (byte) (streamID);
    }


    /**
     * Returns the increment value as a byte array.
     *
     * @param increment The increment value.
     * @return The increment value as a byte array.
     */
    private byte[] getIncrementBytes(int increment) {
        return new byte[]{(byte) ((increment >>> 24) & 0x7F), (byte) (increment >>> 16), (byte) (increment >>> 8), (byte) increment};
    }


    /**
     * Extracts the increment value from the payload of a WINDOW_UPDATE message.
     *
     * @param payload The payload of the WINDOW_UPDATE message.
     * @return The increment value.
     */
    private int getIncrement(byte[] payload) throws BadAttributeException {
        if (payload.length < 4) {
            throw new BadAttributeException("Payload is too short", "payload");
        }
        return ((payload[0] & 0xff) << 24) | ((payload[1] & 0xff) << 16) | ((payload[2] & 0xff) << 8) | (payload[3] & 0xff);
    }


    /**
     * Extracts the stream ID value from the header of a message.
     *
     * @param header The header of the message.
     * @param offset The offset in the header where the stream ID value begins.
     * @return The extracted stream ID value.
     */
    private int getStreamID(byte[] header, int offset) {
        return ((header[offset] & 0xff) << 24) | ((header[offset + 1] & 0xff) << 16) |
                ((header[offset + 2] & 0xff) << 8) | (header[offset + 3] & 0xff);
    }
}

