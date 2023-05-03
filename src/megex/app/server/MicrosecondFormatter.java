/************************************************
 *
 * Author: Abanisenioluwa K. Orojo
 * Assignment: Program 2
 * Class: CSI 5325
 *
 ************************************************/
package megex.app.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * MicrosecondFormatter is a custom log formatter that extends the java.util.logging.Formatter
 * class. It provides a custom format for log messages, including a timestamp with microsecond
 * precision.
 */
public class MicrosecondFormatter extends Formatter {
    // Define the DateTimeFormatter with the desired pattern (microsecond precision)
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    /**
     * Format the given LogRecord with a custom timestamp format and log level.
     *
     * @param record The LogRecord to be formatted.
     * @return The formatted log message as a String.
     */
    @Override
    public String format(LogRecord record) {
        // Get the current timestamp with LocalDateTime and format it with the defined DateTimeFormatter
        LocalDateTime dateTime = LocalDateTime.now();
        String timestamp = dateTime.format(dateTimeFormatter);

        // Return the formatted log message, including the timestamp, log level, and the log message
        return String.format("%s %s %s%n", timestamp, record.getLevel(), formatMessage(record));
    }
}
