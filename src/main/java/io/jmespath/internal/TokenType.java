package io.jmespath.internal;

/**
 * Token types for the JMESPath lexer.
 *
 * <p>Each token type represents a lexical element of the JMESPath grammar.
 */
public enum TokenType {
    // Single character tokens
    DOT("."),
    STAR("*"),
    LBRACKET("["),
    RBRACKET("]"),
    LBRACE("{"),
    RBRACE("}"),
    LPAREN("("),
    RPAREN(")"),
    COMMA(","),
    COLON(":"),
    AT("@"),
    AMPERSAND("&"),
    QUESTION("?"),

    // One or two character tokens
    PIPE("|"),
    OR("||"),
    AND("&&"),
    NOT("!"),
    ASSIGN("="),
    EQ("=="),
    NE("!="),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">="),
    FLATTEN("[]"),
    FILTER("[?"),

    // Literals and identifiers
    IDENTIFIER("identifier"),
    QUOTED_IDENTIFIER("quoted_identifier"),
    NUMBER("number"),
    STRING("string"),
    RAW_STRING("raw_string"),
    LITERAL("literal"),
    VARIABLE("variable"),

    // Keywords (JEP-18 lexical scoping)
    LET("let"),
    IN("in"),

    // Special
    EOF("end of expression"),
    ERROR("error");

    private final String description;

    TokenType(String description) {
        this.description = description;
    }

    /**
     * Returns a human-readable description of this token type.
     *
     * @return the description
     */
    public String description() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
