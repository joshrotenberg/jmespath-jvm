package io.jmespath.internal;

import static org.junit.jupiter.api.Assertions.*;

import io.jmespath.JmesPathException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for the JMESPath lexer.
 */
class LexerTest {

    // Helper to tokenize an entire expression
    private List<Token> tokenize(String input) {
        Lexer lexer = new Lexer(input);
        List<Token> tokens = new ArrayList<Token>();
        Token token;
        while ((token = lexer.next()).type() != TokenType.EOF) {
            tokens.add(token);
        }
        return tokens;
    }

    // Helper to get single token (excluding EOF)
    private Token single(String input) {
        Lexer lexer = new Lexer(input);
        Token token = lexer.next();
        assertEquals(
            TokenType.EOF,
            lexer.next().type(),
            "Expected only one token before EOF"
        );
        return token;
    }

    @Nested
    class SingleCharacterTokens {

        @Test
        void dot() {
            Token t = single(".");
            assertEquals(TokenType.DOT, t.type());
            assertEquals(".", t.text());
        }

        @Test
        void star() {
            Token t = single("*");
            assertEquals(TokenType.STAR, t.type());
            assertEquals("*", t.text());
        }

        @Test
        void at() {
            Token t = single("@");
            assertEquals(TokenType.AT, t.type());
            assertEquals("@", t.text());
        }

        @Test
        void leftBracket() {
            Token t = single("[");
            assertEquals(TokenType.LBRACKET, t.type());
        }

        @Test
        void rightBracket() {
            Token t = single("]");
            assertEquals(TokenType.RBRACKET, t.type());
        }

        @Test
        void leftBrace() {
            Token t = single("{");
            assertEquals(TokenType.LBRACE, t.type());
        }

        @Test
        void rightBrace() {
            Token t = single("}");
            assertEquals(TokenType.RBRACE, t.type());
        }

        @Test
        void leftParen() {
            Token t = single("(");
            assertEquals(TokenType.LPAREN, t.type());
        }

        @Test
        void rightParen() {
            Token t = single(")");
            assertEquals(TokenType.RPAREN, t.type());
        }

        @Test
        void comma() {
            Token t = single(",");
            assertEquals(TokenType.COMMA, t.type());
        }

        @Test
        void colon() {
            Token t = single(":");
            assertEquals(TokenType.COLON, t.type());
        }

        @Test
        void ampersand() {
            Token t = single("&");
            assertEquals(TokenType.AMPERSAND, t.type());
        }
    }

    @Nested
    class TwoCharacterTokens {

        @Test
        void pipe() {
            Token t = single("|");
            assertEquals(TokenType.PIPE, t.type());
            assertEquals("|", t.text());
        }

        @Test
        void or() {
            Token t = single("||");
            assertEquals(TokenType.OR, t.type());
            assertEquals("||", t.text());
        }

        @Test
        void and() {
            Token t = single("&&");
            assertEquals(TokenType.AND, t.type());
            assertEquals("&&", t.text());
        }

        @Test
        void not() {
            Token t = single("!");
            assertEquals(TokenType.NOT, t.type());
            assertEquals("!", t.text());
        }

        @Test
        void eq() {
            Token t = single("==");
            assertEquals(TokenType.EQ, t.type());
            assertEquals("==", t.text());
        }

        @Test
        void ne() {
            Token t = single("!=");
            assertEquals(TokenType.NE, t.type());
            assertEquals("!=", t.text());
        }

        @Test
        void lt() {
            Token t = single("<");
            assertEquals(TokenType.LT, t.type());
        }

        @Test
        void le() {
            Token t = single("<=");
            assertEquals(TokenType.LE, t.type());
        }

        @Test
        void gt() {
            Token t = single(">");
            assertEquals(TokenType.GT, t.type());
        }

        @Test
        void ge() {
            Token t = single(">=");
            assertEquals(TokenType.GE, t.type());
        }

        @Test
        void flatten() {
            Token t = single("[]");
            assertEquals(TokenType.FLATTEN, t.type());
            assertEquals("[]", t.text());
        }
    }

    @Nested
    class Identifiers {

        @Test
        void simpleIdentifier() {
            Token t = single("foo");
            assertEquals(TokenType.IDENTIFIER, t.type());
            assertEquals("foo", t.text());
            assertEquals("foo", t.stringValue());
        }

        @Test
        void identifierWithNumbers() {
            Token t = single("foo123");
            assertEquals(TokenType.IDENTIFIER, t.type());
            assertEquals("foo123", t.stringValue());
        }

        @Test
        void identifierWithUnderscore() {
            Token t = single("foo_bar");
            assertEquals(TokenType.IDENTIFIER, t.type());
            assertEquals("foo_bar", t.stringValue());
        }

        @Test
        void identifierStartingWithUnderscore() {
            Token t = single("_foo");
            assertEquals(TokenType.IDENTIFIER, t.type());
            assertEquals("_foo", t.stringValue());
        }

        @Test
        void singleCharIdentifier() {
            Token t = single("a");
            assertEquals(TokenType.IDENTIFIER, t.type());
            assertEquals("a", t.stringValue());
        }

        @Test
        void mixedCaseIdentifier() {
            Token t = single("FooBar");
            assertEquals(TokenType.IDENTIFIER, t.type());
            assertEquals("FooBar", t.stringValue());
        }
    }

    @Nested
    class QuotedIdentifiers {

        @Test
        void simpleQuotedIdentifier() {
            Token t = single("\"foo\"");
            assertEquals(TokenType.QUOTED_IDENTIFIER, t.type());
            assertEquals("\"foo\"", t.text());
            assertEquals("foo", t.stringValue());
        }

        @Test
        void quotedIdentifierWithSpaces() {
            Token t = single("\"foo bar\"");
            assertEquals(TokenType.QUOTED_IDENTIFIER, t.type());
            assertEquals("foo bar", t.stringValue());
        }

        @Test
        void quotedIdentifierWithEscapedQuote() {
            Token t = single("\"foo\\\"bar\"");
            assertEquals(TokenType.QUOTED_IDENTIFIER, t.type());
            assertEquals("foo\"bar", t.stringValue());
        }

        @Test
        void quotedIdentifierWithNewline() {
            Token t = single("\"foo\\nbar\"");
            assertEquals(TokenType.QUOTED_IDENTIFIER, t.type());
            assertEquals("foo\nbar", t.stringValue());
        }

        @Test
        void quotedIdentifierWithTab() {
            Token t = single("\"foo\\tbar\"");
            assertEquals(TokenType.QUOTED_IDENTIFIER, t.type());
            assertEquals("foo\tbar", t.stringValue());
        }

        @Test
        void quotedIdentifierWithBackslash() {
            Token t = single("\"foo\\\\bar\"");
            assertEquals(TokenType.QUOTED_IDENTIFIER, t.type());
            assertEquals("foo\\bar", t.stringValue());
        }

        @Test
        void quotedIdentifierWithUnicode() {
            Token t = single("\"\\u0041\\u0042\"");
            assertEquals(TokenType.QUOTED_IDENTIFIER, t.type());
            assertEquals("AB", t.stringValue());
        }

        @Test
        void emptyQuotedIdentifier() {
            Token t = single("\"\"");
            assertEquals(TokenType.QUOTED_IDENTIFIER, t.type());
            assertEquals("", t.stringValue());
        }

        @Test
        void unterminatedQuotedIdentifier() {
            JmesPathException e = assertThrows(JmesPathException.class, () ->
                single("\"foo")
            );
            assertTrue(e.getMessage().contains("Unterminated"));
        }

        @Test
        void invalidEscapeSequence() {
            JmesPathException e = assertThrows(JmesPathException.class, () ->
                single("\"foo\\x\"")
            );
            assertTrue(e.getMessage().contains("Invalid escape"));
        }
    }

    @Nested
    class RawStrings {

        @Test
        void simpleRawString() {
            Token t = single("'foo'");
            assertEquals(TokenType.RAW_STRING, t.type());
            assertEquals("'foo'", t.text());
            assertEquals("foo", t.stringValue());
        }

        @Test
        void rawStringWithEscapedQuote() {
            // Escaped quote uses \' syntax per JMESPath spec
            Token t = single("'foo\\'bar'");
            assertEquals(TokenType.RAW_STRING, t.type());
            assertEquals("foo'bar", t.stringValue());
        }

        @Test
        void rawStringWithBackslash() {
            Token t = single("'foo\\bar'");
            assertEquals(TokenType.RAW_STRING, t.type());
            assertEquals("foo\\bar", t.stringValue()); // backslash is literal
        }

        @Test
        void emptyRawString() {
            Token t = single("''");
            assertEquals(TokenType.RAW_STRING, t.type());
            assertEquals("", t.stringValue());
        }

        @Test
        void unterminatedRawString() {
            JmesPathException e = assertThrows(JmesPathException.class, () ->
                single("'foo")
            );
            assertTrue(e.getMessage().contains("Unterminated"));
        }
    }

    @Nested
    class Literals {

        @Test
        void simpleLiteral() {
            Token t = single("`true`");
            assertEquals(TokenType.LITERAL, t.type());
            assertEquals("`true`", t.text());
            assertEquals("true", t.stringValue());
        }

        @Test
        void jsonObjectLiteral() {
            Token t = single("`{\"a\": 1}`");
            assertEquals(TokenType.LITERAL, t.type());
            assertEquals("{\"a\": 1}", t.stringValue());
        }

        @Test
        void jsonArrayLiteral() {
            Token t = single("`[1, 2, 3]`");
            assertEquals(TokenType.LITERAL, t.type());
            assertEquals("[1, 2, 3]", t.stringValue());
        }

        @Test
        void literalWithEscapedBacktick() {
            Token t = single("`foo\\`bar`");
            assertEquals(TokenType.LITERAL, t.type());
            assertEquals("foo`bar", t.stringValue());
        }

        @Test
        void unterminatedLiteral() {
            JmesPathException e = assertThrows(JmesPathException.class, () ->
                single("`foo")
            );
            assertTrue(e.getMessage().contains("Unterminated"));
        }
    }

    @Nested
    class Numbers {

        @Test
        void zero() {
            Token t = single("0");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals("0", t.text());
            assertEquals(0, t.numberValue().intValue());
        }

        @Test
        void positiveInteger() {
            Token t = single("123");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(123, t.numberValue().intValue());
        }

        @Test
        void negativeInteger() {
            Token t = single("-123");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals("-123", t.text());
            assertEquals(-123, t.numberValue().intValue());
        }

        @Test
        void largeInteger() {
            Token t = single("9999999999");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(9999999999L, t.numberValue().longValue());
        }

        @Test
        void floatingPoint() {
            Token t = single("3.14");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(3.14, t.numberValue().doubleValue(), 0.001);
        }

        @Test
        void negativeFloat() {
            Token t = single("-3.14");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(-3.14, t.numberValue().doubleValue(), 0.001);
        }

        @Test
        void exponent() {
            Token t = single("1e10");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(1e10, t.numberValue().doubleValue(), 0.001);
        }

        @Test
        void exponentUppercase() {
            Token t = single("1E10");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(1e10, t.numberValue().doubleValue(), 0.001);
        }

        @Test
        void negativeExponent() {
            Token t = single("1e-10");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(1e-10, t.numberValue().doubleValue(), 1e-20);
        }

        @Test
        void positiveExponent() {
            Token t = single("1e+10");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(1e10, t.numberValue().doubleValue(), 0.001);
        }

        @Test
        void floatWithExponent() {
            Token t = single("3.14e2");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(314.0, t.numberValue().doubleValue(), 0.001);
        }

        @Test
        void negativeOnly() {
            JmesPathException e = assertThrows(JmesPathException.class, () ->
                single("-")
            );
            assertTrue(e.getMessage().contains("Expected digit"));
        }
    }

    @Nested
    class Whitespace {

        @Test
        void skipsSpaces() {
            List<Token> tokens = tokenize("  foo  ");
            assertEquals(1, tokens.size());
            assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
            assertEquals("foo", tokens.get(0).stringValue());
        }

        @Test
        void skipsTabs() {
            List<Token> tokens = tokenize("\tfoo\t");
            assertEquals(1, tokens.size());
            assertEquals("foo", tokens.get(0).stringValue());
        }

        @Test
        void skipsNewlines() {
            List<Token> tokens = tokenize("\nfoo\n");
            assertEquals(1, tokens.size());
            assertEquals("foo", tokens.get(0).stringValue());
        }

        @Test
        void skipsCarriageReturn() {
            List<Token> tokens = tokenize("\r\nfoo\r\n");
            assertEquals(1, tokens.size());
            assertEquals("foo", tokens.get(0).stringValue());
        }

        @Test
        void mixedWhitespace() {
            List<Token> tokens = tokenize("  \t\n  foo  \t\n  ");
            assertEquals(1, tokens.size());
            assertEquals("foo", tokens.get(0).stringValue());
        }

        @Test
        void whitespaceInTokenSequence() {
            List<Token> tokens = tokenize("foo . bar");
            assertEquals(3, tokens.size());
            assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
            assertEquals(TokenType.DOT, tokens.get(1).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(2).type());
        }
    }

    @Nested
    class Positions {

        @Test
        void tracksPosition() {
            Lexer lexer = new Lexer("foo");
            Token t = lexer.next();
            assertEquals(0, t.position());
            assertEquals(1, t.line());
            assertEquals(1, t.column());
        }

        @Test
        void tracksPositionAfterWhitespace() {
            Lexer lexer = new Lexer("  foo");
            Token t = lexer.next();
            assertEquals(2, t.position());
            assertEquals(1, t.line());
            assertEquals(3, t.column());
        }

        @Test
        void tracksLineNumbers() {
            Lexer lexer = new Lexer("foo\nbar");
            Token t1 = lexer.next();
            Token t2 = lexer.next();
            assertEquals(1, t1.line());
            assertEquals(2, t2.line());
        }

        @Test
        void tracksColumnAfterNewline() {
            Lexer lexer = new Lexer("foo\n  bar");
            Token t1 = lexer.next();
            Token t2 = lexer.next();
            assertEquals(1, t1.column());
            assertEquals(3, t2.column()); // after 2 spaces
        }
    }

    @Nested
    class ComplexExpressions {

        @Test
        void simplePropertyAccess() {
            List<Token> tokens = tokenize("foo.bar");
            assertEquals(3, tokens.size());
            assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
            assertEquals("foo", tokens.get(0).stringValue());
            assertEquals(TokenType.DOT, tokens.get(1).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(2).type());
            assertEquals("bar", tokens.get(2).stringValue());
        }

        @Test
        void indexAccess() {
            List<Token> tokens = tokenize("foo[0]");
            assertEquals(4, tokens.size());
            assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
            assertEquals(TokenType.LBRACKET, tokens.get(1).type());
            assertEquals(TokenType.NUMBER, tokens.get(2).type());
            assertEquals(0, tokens.get(2).numberValue().intValue());
            assertEquals(TokenType.RBRACKET, tokens.get(3).type());
        }

        @Test
        void negativeIndexAccess() {
            List<Token> tokens = tokenize("foo[-1]");
            assertEquals(4, tokens.size());
            assertEquals(TokenType.NUMBER, tokens.get(2).type());
            assertEquals(-1, tokens.get(2).numberValue().intValue());
        }

        @Test
        void sliceExpression() {
            List<Token> tokens = tokenize("foo[0:10:2]");
            assertEquals(8, tokens.size());
            assertEquals(TokenType.NUMBER, tokens.get(2).type());
            assertEquals(TokenType.COLON, tokens.get(3).type());
            assertEquals(TokenType.NUMBER, tokens.get(4).type());
            assertEquals(TokenType.COLON, tokens.get(5).type());
            assertEquals(TokenType.NUMBER, tokens.get(6).type());
        }

        @Test
        void wildcardProjection() {
            // foo[*].bar = foo, [, *, ], ., bar = 6 tokens
            List<Token> tokens = tokenize("foo[*].bar");
            assertEquals(6, tokens.size());
            assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
            assertEquals(TokenType.LBRACKET, tokens.get(1).type());
            assertEquals(TokenType.STAR, tokens.get(2).type());
            assertEquals(TokenType.RBRACKET, tokens.get(3).type());
            assertEquals(TokenType.DOT, tokens.get(4).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(5).type());
        }

        @Test
        void filterExpression() {
            // foo[?bar == `true`] = foo, [?, bar, ==, `true`, ] = 6 tokens
            // [? is a single FILTER token
            List<Token> tokens = tokenize("foo[?bar == `true`]");
            assertEquals(6, tokens.size());
            assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
            assertEquals(TokenType.FILTER, tokens.get(1).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(2).type());
            assertEquals("bar", tokens.get(2).stringValue());
            assertEquals(TokenType.EQ, tokens.get(3).type());
            assertEquals(TokenType.LITERAL, tokens.get(4).type());
            assertEquals(TokenType.RBRACKET, tokens.get(5).type());
        }

        @Test
        void functionCall() {
            List<Token> tokens = tokenize("length(foo)");
            assertEquals(4, tokens.size());
            assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
            assertEquals("length", tokens.get(0).stringValue());
            assertEquals(TokenType.LPAREN, tokens.get(1).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(2).type());
            assertEquals(TokenType.RPAREN, tokens.get(3).type());
        }

        @Test
        void multiSelectList() {
            List<Token> tokens = tokenize("[foo, bar]");
            assertEquals(5, tokens.size());
            assertEquals(TokenType.LBRACKET, tokens.get(0).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
            assertEquals(TokenType.COMMA, tokens.get(2).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(3).type());
            assertEquals(TokenType.RBRACKET, tokens.get(4).type());
        }

        @Test
        void multiSelectHash() {
            List<Token> tokens = tokenize("{a: foo, b: bar}");
            assertEquals(9, tokens.size());
            assertEquals(TokenType.LBRACE, tokens.get(0).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
            assertEquals(TokenType.COLON, tokens.get(2).type());
        }

        @Test
        void pipeExpression() {
            List<Token> tokens = tokenize("foo | bar");
            assertEquals(3, tokens.size());
            assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
            assertEquals(TokenType.PIPE, tokens.get(1).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(2).type());
        }

        @Test
        void orExpression() {
            List<Token> tokens = tokenize("foo || bar");
            assertEquals(3, tokens.size());
            assertEquals(TokenType.OR, tokens.get(1).type());
        }

        @Test
        void andExpression() {
            List<Token> tokens = tokenize("foo && bar");
            assertEquals(3, tokens.size());
            assertEquals(TokenType.AND, tokens.get(1).type());
        }

        @Test
        void notExpression() {
            List<Token> tokens = tokenize("!foo");
            assertEquals(2, tokens.size());
            assertEquals(TokenType.NOT, tokens.get(0).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
        }

        @Test
        void comparisonExpression() {
            List<Token> tokens = tokenize("foo < 10");
            assertEquals(3, tokens.size());
            assertEquals(TokenType.LT, tokens.get(1).type());
        }

        @Test
        void expressionReference() {
            List<Token> tokens = tokenize("&foo");
            assertEquals(2, tokens.size());
            assertEquals(TokenType.AMPERSAND, tokens.get(0).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
        }

        @Test
        void currentNode() {
            List<Token> tokens = tokenize("@.foo");
            assertEquals(3, tokens.size());
            assertEquals(TokenType.AT, tokens.get(0).type());
            assertEquals(TokenType.DOT, tokens.get(1).type());
            assertEquals(TokenType.IDENTIFIER, tokens.get(2).type());
        }

        @Test
        void flattenOperator() {
            List<Token> tokens = tokenize("foo[]");
            assertEquals(2, tokens.size());
            assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
            assertEquals(TokenType.FLATTEN, tokens.get(1).type());
        }

        @Test
        void complexExpression() {
            String expr =
                "people[?age > `18`].{name: name, city: address.city} | sort_by(@, &name)";
            List<Token> tokens = tokenize(expr);
            // Just verify it tokenizes without error and has expected structure
            assertTrue(tokens.size() > 10);
            assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
            assertEquals("people", tokens.get(0).stringValue());
        }
    }

    @Nested
    class ErrorCases {

        @Test
        void singleEquals() {
            JmesPathException e = assertThrows(JmesPathException.class, () ->
                single("=")
            );
            assertTrue(e.getMessage().contains("Expected '='"));
            assertEquals(JmesPathException.ErrorType.SYNTAX, e.getErrorType());
        }

        @Test
        void unexpectedCharacter() {
            JmesPathException e = assertThrows(JmesPathException.class, () ->
                single("$")
            );
            assertTrue(e.getMessage().contains("Unexpected character"));
            assertEquals(JmesPathException.ErrorType.SYNTAX, e.getErrorType());
        }

        @Test
        void invalidUnicode() {
            JmesPathException e = assertThrows(JmesPathException.class, () ->
                single("\"\\uXXXX\"")
            );
            assertTrue(e.getMessage().contains("Invalid unicode"));
        }

        @Test
        void truncatedUnicode() {
            JmesPathException e = assertThrows(JmesPathException.class, () ->
                single("\"\\u12\"")
            );
            assertTrue(e.getMessage().contains("Invalid unicode"));
        }

        @Test
        void errorIncludesPosition() {
            try {
                tokenize("foo.bar.$baz");
                fail("Expected exception");
            } catch (JmesPathException e) {
                assertTrue(e.hasPosition());
                assertEquals(8, e.getPosition());
                assertTrue(e.getMessage().contains("column"));
            }
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void emptyInput() {
            Lexer lexer = new Lexer("");
            assertEquals(TokenType.EOF, lexer.next().type());
        }

        @Test
        void onlyWhitespace() {
            Lexer lexer = new Lexer("   \n\t  ");
            assertEquals(TokenType.EOF, lexer.next().type());
        }

        @Test
        void nullInput() {
            assertThrows(IllegalArgumentException.class, () -> new Lexer(null));
        }

        @Test
        void multipleEofCalls() {
            Lexer lexer = new Lexer("");
            assertEquals(TokenType.EOF, lexer.next().type());
            assertEquals(TokenType.EOF, lexer.next().type());
            assertEquals(TokenType.EOF, lexer.next().type());
        }

        @Test
        void peek() {
            Lexer lexer = new Lexer("foo.bar");
            Token peeked = lexer.peek();
            assertEquals(TokenType.IDENTIFIER, peeked.type());
            assertEquals("foo", peeked.stringValue());

            // peek should not consume
            Token next = lexer.next();
            assertEquals(TokenType.IDENTIFIER, next.type());
            assertEquals("foo", next.stringValue());
        }
    }

    @Nested
    class TokenMethods {

        @Test
        void isMethod() {
            Token t = single("foo");
            assertTrue(t.is(TokenType.IDENTIFIER));
            assertFalse(t.is(TokenType.DOT));
        }

        @Test
        void isAnyMethod() {
            Token t = single("foo");
            assertTrue(t.isAny(TokenType.IDENTIFIER, TokenType.DOT));
            assertTrue(t.isAny(TokenType.DOT, TokenType.IDENTIFIER));
            assertFalse(t.isAny(TokenType.DOT, TokenType.STAR));
        }

        @Test
        void tokenEquality() {
            Token t1 = single("foo");
            Token t2 = single("foo");
            Token t3 = single("bar");

            assertEquals(t1, t2);
            assertNotEquals(t1, t3);
        }

        @Test
        void tokenToString() {
            Token t = single("foo");
            String s = t.toString();
            // TokenType.toString() returns description like "identifier", not "IDENTIFIER"
            assertTrue(s.contains("identifier"));
            assertTrue(s.contains("foo"));
        }
    }
}
