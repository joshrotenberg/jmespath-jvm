package io.jmespath.internal;

import io.jmespath.JmesPathException;

/**
 * Tokenizer for JMESPath expressions.
 *
 * <p>The lexer reads a JMESPath expression character by character and produces
 * a sequence of tokens. It handles:
 * <ul>
 *   <li>Single and multi-character operators</li>
 *   <li>Unquoted identifiers</li>
 *   <li>Quoted identifiers (with escape sequences)</li>
 *   <li>Numeric literals (integers and floats)</li>
 *   <li>String literals (with escape sequences)</li>
 *   <li>Raw string literals</li>
 *   <li>JSON literals (backtick-delimited)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * Lexer lexer = new Lexer("foo.bar[0]");
 * Token token;
 * while ((token = lexer.next()).type() != TokenType.EOF) {
 *     System.out.println(token);
 * }
 * }</pre>
 */
public final class Lexer {

    private final String input;
    private int pos;
    private int line;
    private int column;

    /**
     * Creates a new lexer for the given input.
     *
     * @param input the JMESPath expression to tokenize
     */
    public Lexer(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input cannot be null");
        }
        this.input = input;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
    }

    /**
     * Returns the next token from the input.
     *
     * <p>When the input is exhausted, returns a token of type EOF.
     * Subsequent calls continue to return EOF.
     *
     * @return the next token
     * @throws JmesPathException if the input contains invalid syntax
     */
    public Token next() {
        skipWhitespace();

        if (pos >= input.length()) {
            return makeToken(TokenType.EOF, "");
        }

        int startPos = pos;
        int startLine = line;
        int startColumn = column;
        char c = input.charAt(pos);

        switch (c) {
            case '.':
                advance();
                return new Token(
                    TokenType.DOT,
                    ".",
                    startPos,
                    startLine,
                    startColumn
                );
            case '*':
                advance();
                return new Token(
                    TokenType.STAR,
                    "*",
                    startPos,
                    startLine,
                    startColumn
                );
            case '@':
                advance();
                return new Token(
                    TokenType.AT,
                    "@",
                    startPos,
                    startLine,
                    startColumn
                );
            case '&':
                advance();
                if (pos < input.length() && input.charAt(pos) == '&') {
                    advance();
                    return new Token(
                        TokenType.AND,
                        "&&",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                return new Token(
                    TokenType.AMPERSAND,
                    "&",
                    startPos,
                    startLine,
                    startColumn
                );
            case '|':
                advance();
                if (pos < input.length() && input.charAt(pos) == '|') {
                    advance();
                    return new Token(
                        TokenType.OR,
                        "||",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                return new Token(
                    TokenType.PIPE,
                    "|",
                    startPos,
                    startLine,
                    startColumn
                );
            case '!':
                advance();
                if (pos < input.length() && input.charAt(pos) == '=') {
                    advance();
                    return new Token(
                        TokenType.NE,
                        "!=",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                return new Token(
                    TokenType.NOT,
                    "!",
                    startPos,
                    startLine,
                    startColumn
                );
            case '=':
                advance();
                if (pos < input.length() && input.charAt(pos) == '=') {
                    advance();
                    return new Token(
                        TokenType.EQ,
                        "==",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                // Single '=' for let bindings (JEP-18)
                return new Token(
                    TokenType.ASSIGN,
                    "=",
                    startPos,
                    startLine,
                    startColumn
                );
            case '<':
                advance();
                if (pos < input.length() && input.charAt(pos) == '=') {
                    advance();
                    return new Token(
                        TokenType.LE,
                        "<=",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                return new Token(
                    TokenType.LT,
                    "<",
                    startPos,
                    startLine,
                    startColumn
                );
            case '>':
                advance();
                if (pos < input.length() && input.charAt(pos) == '=') {
                    advance();
                    return new Token(
                        TokenType.GE,
                        ">=",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                return new Token(
                    TokenType.GT,
                    ">",
                    startPos,
                    startLine,
                    startColumn
                );
            case '[':
                advance();
                if (pos < input.length() && input.charAt(pos) == ']') {
                    advance();
                    return new Token(
                        TokenType.FLATTEN,
                        "[]",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                if (pos < input.length() && input.charAt(pos) == '?') {
                    advance();
                    return new Token(
                        TokenType.FILTER,
                        "[?",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                return new Token(
                    TokenType.LBRACKET,
                    "[",
                    startPos,
                    startLine,
                    startColumn
                );
            case ']':
                advance();
                return new Token(
                    TokenType.RBRACKET,
                    "]",
                    startPos,
                    startLine,
                    startColumn
                );
            case '{':
                advance();
                return new Token(
                    TokenType.LBRACE,
                    "{",
                    startPos,
                    startLine,
                    startColumn
                );
            case '}':
                advance();
                return new Token(
                    TokenType.RBRACE,
                    "}",
                    startPos,
                    startLine,
                    startColumn
                );
            case '(':
                advance();
                return new Token(
                    TokenType.LPAREN,
                    "(",
                    startPos,
                    startLine,
                    startColumn
                );
            case ')':
                advance();
                return new Token(
                    TokenType.RPAREN,
                    ")",
                    startPos,
                    startLine,
                    startColumn
                );
            case ',':
                advance();
                return new Token(
                    TokenType.COMMA,
                    ",",
                    startPos,
                    startLine,
                    startColumn
                );
            case ':':
                advance();
                return new Token(
                    TokenType.COLON,
                    ":",
                    startPos,
                    startLine,
                    startColumn
                );
            case '?':
                advance();
                return new Token(
                    TokenType.QUESTION,
                    "?",
                    startPos,
                    startLine,
                    startColumn
                );
            case '"':
                return quotedIdentifier(startPos, startLine, startColumn);
            case '\'':
                return rawString(startPos, startLine, startColumn);
            case '`':
                return literal(startPos, startLine, startColumn);
            case '-':
                // Negative number
                return number(startPos, startLine, startColumn);
            case '$':
                // Variable reference (JEP-18)
                return variable(startPos, startLine, startColumn);
            default:
                if (isDigit(c)) {
                    return number(startPos, startLine, startColumn);
                }
                if (isAlpha(c) || c == '_') {
                    return identifier(startPos, startLine, startColumn);
                }
                throw error(
                    "Unexpected character: '" + c + "'",
                    startPos,
                    startLine,
                    startColumn
                );
        }
    }

    /**
     * Peeks at the next token without consuming it.
     *
     * @return the next token
     */
    public Token peek() {
        int savedPos = pos;
        int savedLine = line;
        int savedColumn = column;
        Token token = next();
        pos = savedPos;
        line = savedLine;
        column = savedColumn;
        return token;
    }

    private void skipWhitespace() {
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
            } else if (c == '\n') {
                advance();
                line++;
                column = 1;
            } else {
                break;
            }
        }
    }

    private void advance() {
        if (pos < input.length()) {
            pos++;
            column++;
        }
    }

    private Token makeToken(TokenType type, String text) {
        return new Token(type, text, pos, line, column);
    }

    private Token identifier(int startPos, int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (isAlpha(c) || isDigit(c) || c == '_') {
                sb.append(c);
                advance();
            } else {
                break;
            }
        }
        String name = sb.toString();

        // Check for keywords (JEP-18)
        if (name.equals("let")) {
            return new Token(
                TokenType.LET,
                name,
                startPos,
                startLine,
                startColumn
            );
        }
        if (name.equals("in")) {
            return new Token(
                TokenType.IN,
                name,
                startPos,
                startLine,
                startColumn
            );
        }

        return new Token(
            TokenType.IDENTIFIER,
            name,
            name,
            startPos,
            startLine,
            startColumn
        );
    }

    private Token variable(int startPos, int startLine, int startColumn) {
        advance(); // consume '$'
        StringBuilder sb = new StringBuilder();

        // Variable name must start with alpha or underscore
        if (pos >= input.length()) {
            throw error(
                "Expected variable name after '$'",
                startPos,
                startLine,
                startColumn
            );
        }

        char first = input.charAt(pos);
        if (!isAlpha(first) && first != '_') {
            throw error(
                "Variable name must start with letter or underscore",
                startPos,
                startLine,
                startColumn
            );
        }

        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (isAlpha(c) || isDigit(c) || c == '_') {
                sb.append(c);
                advance();
            } else {
                break;
            }
        }

        String name = sb.toString();
        return new Token(
            TokenType.VARIABLE,
            "$" + name,
            name,
            startPos,
            startLine,
            startColumn
        );
    }

    private Token quotedIdentifier(
        int startPos,
        int startLine,
        int startColumn
    ) {
        advance(); // consume opening quote
        StringBuilder sb = new StringBuilder();
        StringBuilder text = new StringBuilder("\"");

        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '"') {
                text.append(c);
                advance();
                return new Token(
                    TokenType.QUOTED_IDENTIFIER,
                    text.toString(),
                    sb.toString(),
                    startPos,
                    startLine,
                    startColumn
                );
            }
            if (c == '\\') {
                text.append(c);
                advance();
                if (pos >= input.length()) {
                    throw error(
                        "Unexpected end of input in quoted identifier",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                char escaped = input.charAt(pos);
                text.append(escaped);
                sb.append(
                    escapeChar(escaped, startPos, startLine, startColumn)
                );
                advance();
            } else {
                text.append(c);
                sb.append(c);
                advance();
            }
        }
        throw error(
            "Unterminated quoted identifier",
            startPos,
            startLine,
            startColumn
        );
    }

    private Token rawString(int startPos, int startLine, int startColumn) {
        advance(); // consume opening quote
        StringBuilder sb = new StringBuilder();
        StringBuilder text = new StringBuilder("'");

        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '\'') {
                text.append(c);
                advance();
                return new Token(
                    TokenType.RAW_STRING,
                    text.toString(),
                    sb.toString(),
                    startPos,
                    startLine,
                    startColumn
                );
            } else if (c == '\\') {
                // Check for escaped single quote (\')
                if (pos + 1 < input.length() && input.charAt(pos + 1) == '\'') {
                    text.append("\\'");
                    sb.append('\'');
                    advance();
                    advance();
                } else {
                    // Backslash followed by anything else is kept literally
                    text.append(c);
                    sb.append(c);
                    advance();
                }
            } else {
                text.append(c);
                sb.append(c);
                advance();
            }
        }
        throw error(
            "Unterminated raw string",
            startPos,
            startLine,
            startColumn
        );
    }

    private Token literal(int startPos, int startLine, int startColumn) {
        advance(); // consume opening backtick
        StringBuilder sb = new StringBuilder();
        StringBuilder text = new StringBuilder("`");

        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '`') {
                text.append(c);
                advance();
                return new Token(
                    TokenType.LITERAL,
                    text.toString(),
                    sb.toString(),
                    startPos,
                    startLine,
                    startColumn
                );
            }
            if (c == '\\') {
                text.append(c);
                advance();
                if (pos >= input.length()) {
                    throw error(
                        "Unexpected end of input in literal",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                char next = input.charAt(pos);
                text.append(next);
                if (next == '`') {
                    sb.append('`');
                } else {
                    // Keep the backslash for JSON parsing
                    sb.append('\\');
                    sb.append(next);
                }
                advance();
            } else {
                text.append(c);
                sb.append(c);
                advance();
            }
        }
        throw error("Unterminated literal", startPos, startLine, startColumn);
    }

    private Token number(int startPos, int startLine, int startColumn) {
        StringBuilder text = new StringBuilder();
        boolean hasDecimal = false;
        boolean hasExponent = false;

        // Handle negative sign
        if (pos < input.length() && input.charAt(pos) == '-') {
            text.append('-');
            advance();
        }

        // Must have at least one digit
        if (pos >= input.length() || !isDigit(input.charAt(pos))) {
            throw error(
                "Expected digit after '-'",
                startPos,
                startLine,
                startColumn
            );
        }

        // Integer part
        while (pos < input.length() && isDigit(input.charAt(pos))) {
            text.append(input.charAt(pos));
            advance();
        }

        // Optional decimal part
        if (pos < input.length() && input.charAt(pos) == '.') {
            // Check if followed by digit (not a dot operator)
            if (pos + 1 < input.length() && isDigit(input.charAt(pos + 1))) {
                hasDecimal = true;
                text.append('.');
                advance();
                while (pos < input.length() && isDigit(input.charAt(pos))) {
                    text.append(input.charAt(pos));
                    advance();
                }
            }
        }

        // Optional exponent
        if (
            pos < input.length() &&
            (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')
        ) {
            hasExponent = true;
            text.append(input.charAt(pos));
            advance();
            if (
                pos < input.length() &&
                (input.charAt(pos) == '+' || input.charAt(pos) == '-')
            ) {
                text.append(input.charAt(pos));
                advance();
            }
            if (pos >= input.length() || !isDigit(input.charAt(pos))) {
                throw error(
                    "Expected digit in exponent",
                    startPos,
                    startLine,
                    startColumn
                );
            }
            while (pos < input.length() && isDigit(input.charAt(pos))) {
                text.append(input.charAt(pos));
                advance();
            }
        }

        String numStr = text.toString();
        Number value;
        if (hasDecimal || hasExponent) {
            value = Double.parseDouble(numStr);
        } else {
            // Try to fit in an int, fall back to long
            try {
                value = Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                value = Long.parseLong(numStr);
            }
        }
        return new Token(
            TokenType.NUMBER,
            numStr,
            value,
            startPos,
            startLine,
            startColumn
        );
    }

    private char escapeChar(
        char c,
        int startPos,
        int startLine,
        int startColumn
    ) {
        switch (c) {
            case '"':
                return '"';
            case '\\':
                return '\\';
            case '/':
                return '/';
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'u':
                // pos is pointing at 'u', we need to read 4 hex digits after it
                // But the caller will advance() after us, so we need to:
                // 1. Read chars at pos+1 through pos+4
                // 2. Advance pos by 4 (caller will do the 5th advance for 'u')
                if (pos + 5 > input.length()) {
                    throw error(
                        "Invalid unicode escape",
                        startPos,
                        startLine,
                        startColumn
                    );
                }
                String hex = input.substring(pos + 1, pos + 5);
                for (int i = 0; i < 4; i++) {
                    char h = hex.charAt(i);
                    if (!isHexDigit(h)) {
                        throw error(
                            "Invalid unicode escape: \\u" + hex,
                            startPos,
                            startLine,
                            startColumn
                        );
                    }
                }
                pos += 4;
                column += 4;
                return (char) Integer.parseInt(hex, 16);
            default:
                throw error(
                    "Invalid escape sequence: \\" + c,
                    startPos,
                    startLine,
                    startColumn
                );
        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private JmesPathException error(
        String message,
        int startPos,
        int startLine,
        int startColumn
    ) {
        return new JmesPathException(
            message,
            JmesPathException.ErrorType.SYNTAX,
            startPos,
            startLine,
            startColumn
        );
    }
}
