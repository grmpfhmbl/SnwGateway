package utils;

/**
 * Exception thrown, when DataFrame from XBee could not be parsed.
 */
public class DataFrameParseException extends Exception {
    /**
     * Constructs a <code>DataFrameParseException</code> with no detail message.
     */
    public DataFrameParseException() {
        super();
    }

    /**
     * Constructs a <code>DataFrameParseException</code> with the
     * specified detail message.
     *
     * @param s the detail message.
     */
    public DataFrameParseException(String s) {
        super(s);
    }

    /**
     * Constructs a <code>DataFrameParseException</code> with the
     * specified detail message and <code>Throwable</code> as cause.
     *
     * @param s the detail message.
     * @param cause cause of exception
     */
    public DataFrameParseException(String s, Throwable cause) {
        super(s, cause);
    }

}
