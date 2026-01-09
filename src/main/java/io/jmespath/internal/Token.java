package io.jmespath.internal;

/**
 * A lexical token from a JMESPath expression.
 *
 * <p>Tokens are immutable and contain:
 * <ul>
 *   <li>The token type</li>
 *   <li>The literal text from the input</li>
 *   <li>The parsed value (for strings, numbers, identifiers)</li>
 *   <li>Position information for error reporting</li>
 * </ul>
 */
public final class Token {

    private final TokenType type;
    private final String text;
    private final Object value;
    private final int position;
    private final int line;
    private final int column;

    /**
     * Creates a token without a parsed value.
     *
     * @param type the token type
     * @param text the literal text from the input
     * @param position character offset (0-based)
     * @param line line number (1-based)
     * @param column column number (1-based)
     */
    public Token(TokenType type, String text, int position, int line, int column) {
        this(type, text, null, position, line, column);
    }

    /**
     * Creates a token with a parsed value.
     *
     * @param type the token type
     * @param text the literal text from the input
     * @param value the parsed value (String for identifiers/strings, Number for numbers)
     * @param position character offset (0-based)
     * @param line line number (1-based)
     * @param column column number (1-based)
     */
    public Token(TokenType type, String text, Object value, int position, int line, int column) {
        this.type = type;
        this.text = text;
        this.value = value;
        this.position = position;
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the token type.
     *
     * @return the type
     */
    public TokenType type() {
        return type;
    }

    /**
     * Returns the literal text from the input.
     *
     * @return the text
     */
    public String text() {
        return text;
    }

    /**
     * Returns the parsed value.
     *
     * <p>For IDENTIFIER and QUOTED_IDENTIFIER, this is the identifier name (String).
     * For NUMBER, this is the numeric value (Number).
     * For STRING and RAW_STRING, this is the string content (String).
     * For LITERAL, this is the JSON literal content (String).
     * For other token types, this is null.
     *
     * @return the parsed value, or null
     */
    public Object value() {
        return value;
    }

    /**
     * Returns the string value of this token.
     *
     * @return the value as a String
     * @throws ClassCastException if the value is not a String
     */
    public String stringValue() {
        return (String) value;
    }

    /**
     * Returns the numeric value of this token.
     *
     * @return the value as a Number
     * @throws ClassCastException if the value is not a Number
     */
    public Number numberValue() {
        return (Number) value;
    }

    /**
     * Returns the character position (0-based).
     *
     * @return the position
     */
    public int position() {
        return position;
    }

    /**
     * Returns the line number (1-based).
     *
     * @return the line
     */
    public int line() {
        return line;
    }

    /**
     * Returns the column number (1-based).
     *
     * @return the column
     */
    public int column() {
        return column;
    }

    /**
     * Returns true if this token is of the given type.
     *
     * @param t the type to check
     * @return true if types match
     */
    public boolean is(TokenType t) {
        return this.type == t;
    }

    /**
     * Returns true if this token is any of the given types.
     *
     * @param types the types to check
     * @return true if this token matches any type
     */
    public boolean isAny(TokenType... types) {
        for (int i = 0; i < types.length; i++) {
            if (this.type == types[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (value != null) {
            return type + "(" + value + ")@" + position;
        }
        return type + "@" + position;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Token other = (Token) obj;
        if (type != other.type) {
            return false;
        }
        if (value == null) {
            return other.value == null;
        }
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
