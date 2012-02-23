package net.infotrek.util.logging.jdk14;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Hani Suleiman
 */
public class TerseFormatter extends Formatter {
    /**
     * An array of strings containing only white spaces. Strings' lengths are
     * equal to their index + 1 in the <code>spacesFactory</code> array.
     * For example, <code>spacesFactory[4]</code> contains a string of
     * length 5.  Strings are constructed only when first needed.
     */
    private static final String[] spacesFactory = new String[20];

    /**
     * The string to write at the begining of all log headers (e.g. "[FINE core]")
     */
    private static final String PREFIX = "";

    /**
     * The string to write at the end of every log header (e.g. "[FINE core]").
     * It should includes the spaces between the header and the message body.
     */
    private static final String SUFFIX = " - ";

    /**
     * The minimum amount of spaces to use for writting level and module name
     * before the message.
     */
    private static final int MARGIN = 12;
    private final String lineSeparator = System.getProperty("line.separator", "\n");

    /**
     * The line separator for the message body. This line always begin with
     * {@link #lineSeparator}, followed by some amount of spaces in order to
     * align the message.
     */
    private String bodyLineSeparator = lineSeparator;
    private final long startMillis;

    /**
     * Buffer for formatting messages. We will reuse this
     * buffer in order to reduce memory allocations.
     */
    private final StringBuffer buffer;

    public TerseFormatter() {
        this.startMillis = System.currentTimeMillis();
        buffer = new StringBuffer();
        buffer.append(PREFIX);
    }

    /**
     * Format the given log record and return the formatted string.
     *
     * @param  record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(final LogRecord record) {
        buffer.setLength(PREFIX.length());
        buffer.append(record.getMillis() - startMillis);
        buffer.append(' ');
        buffer.append(record.getLevel().getLocalizedName());

        int offset = buffer.length();
        buffer.append(spaces(MARGIN - offset));

        String logger = record.getLoggerName();
        buffer.append(' ');
        buffer.append(logger);
        buffer.append(SUFFIX);

        final int margin = buffer.length();

        if (bodyLineSeparator.length() != (lineSeparator.length() + margin)) {
            bodyLineSeparator = lineSeparator + spaces(margin);
        }

        buffer.append(formatMessage(record)).append('\n');

        if (record.getThrown() != null) {
            StringWriter trace = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(trace));
            buffer.append(trace);
        }

        return buffer.toString();
    }

    /**
     * Returns a string of the specified length filled with white spaces.
     * This method tries to return a pre-allocated string if possible.
     *
     * @param  length The string length. Negative values are clamped to 0.
     * @return A string of length <code>length</code> filled with white spaces.
     */
    private static String spaces(int length) {
        // No need to synchronize.  In the unlikely event of two threads
        // calling this method at the same time and the two calls creating a
        // new string, the String.intern() call will take care of
        // canonicalizing the strings.
        final int last = spacesFactory.length - 1;

        if (length < 0) {
            length = 0;
        }

        if (length <= last) {
            if (spacesFactory[length] == null) {
                if (spacesFactory[last] == null) {
                    char[] blanks = new char[last];

                    for (int i = 0; i < last; i++)
                        blanks[i] = ' ';

                    spacesFactory[last] = new String(blanks).intern();
                }

                spacesFactory[length] = spacesFactory[last].substring(0, length).intern();
            }

            return spacesFactory[length];
        } else {
            char[] blanks = new char[length];

            for (int i = 0; i < length; i++)
                blanks[i] = ' ';

            return new String(blanks);
        }
    }
}

