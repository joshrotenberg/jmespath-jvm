package io.jmespath;

/**
 * Exception thrown when JMESPath parsing or evaluation fails.
 *
 * <p>This exception covers all error cases:
 * <ul>
 *   <li>Syntax errors during parsing (invalid expression)</li>
 *   <li>Type errors during evaluation (wrong type for operation)</li>
 *   <li>Arity errors (wrong number of function arguments)</li>
 *   <li>Value errors (invalid argument values)</li>
 * </ul>
 *
 * <p>For parse errors, position information is available via {@link #getPosition()},
 * {@link #getLine()}, and {@link #getColumn()}.
 *
 * <p>Example:
 * <pre>{@code
 * try {
 *     Expression<Object> expr = JmesPath.compile("foo[");
 * } catch (JmesPathException e) {
 *     System.err.println("Error at column " + e.getColumn() + ": " + e.getMessage());
 * }
 * }</pre>
 */
public class JmesPathException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int position;
    private final int line;
    private final int column;
    private final ErrorType errorType;

    /**
     * The type of error that occurred.
     */
    public enum ErrorType {
        /** Syntax error during parsing */
        SYNTAX,
        /** Wrong type for an operation */
        TYPE,
        /** Wrong number of function arguments */
        ARITY,
        /** Invalid argument value */
        VALUE,
        /** Unknown function name */
        UNKNOWN_FUNCTION,
        /** General evaluation error */
        EVALUATION
    }

    /**
     * Creates a new exception with a message only.
     *
     * @param message the error message
     */
    public JmesPathException(String message) {
        this(message, ErrorType.EVALUATION, -1, -1, -1);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public JmesPathException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.EVALUATION;
        this.position = -1;
        this.line = -1;
        this.column = -1;
    }

    /**
     * Creates a new exception with error type and message.
     *
     * @param message the error message
     * @param errorType the type of error
     */
    public JmesPathException(String message, ErrorType errorType) {
        this(message, errorType, -1, -1, -1);
    }

    /**
     * Creates a new exception with full position information.
     *
     * @param message the error message
     * @param errorType the type of error
     * @param position character offset in the input (0-based)
     * @param line line number (1-based)
     * @param column column number (1-based)
     */
    public JmesPathException(String message, ErrorType errorType, int position, int line, int column) {
        super(formatMessage(message, position, line, column));
        this.errorType = errorType;
        this.position = position;
        this.line = line;
        this.column = column;
    }

    private static String formatMessage(String message, int position, int line, int column) {
        if (position < 0) {
            return message;
        }
        return message + " (line " + line + ", column " + column + ")";
    }

    /**
     * Returns the error type.
     *
     * @return the error type
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Returns the character position where the error occurred (0-based).
     *
     * @return the position, or -1 if not available
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns the line number where the error occurred (1-based).
     *
     * @return the line number, or -1 if not available
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column number where the error occurred (1-based).
     *
     * @return the column number, or -1 if not available
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns true if position information is available.
     *
     * @return true if position info is available
     */
    public boolean hasPosition() {
        return position >= 0;
    }
}
