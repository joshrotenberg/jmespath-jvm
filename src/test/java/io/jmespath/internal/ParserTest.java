package io.jmespath.internal;

import static org.junit.jupiter.api.Assertions.*;

import io.jmespath.JmesPathException;
import io.jmespath.internal.node.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for the JMESPath parser.
 */
class ParserTest {

    private Node parse(String expression) {
        Parser parser = new Parser(expression);
        return parser.parse();
    }

    @Nested
    class BasicExpressions {

        @Test
        void identifier() {
            Node node = parse("foo");
            assertTrue(node instanceof IdentifierNode);
            assertEquals("foo", ((IdentifierNode) node).getName());
        }

        @Test
        void quotedIdentifier() {
            Node node = parse("\"foo bar\"");
            assertTrue(node instanceof IdentifierNode);
            assertEquals("foo bar", ((IdentifierNode) node).getName());
        }

        @Test
        void currentNode() {
            Node node = parse("@");
            assertSame(CurrentNode.INSTANCE, node);
        }

        @Test
        void rawString() {
            Node node = parse("'hello world'");
            assertTrue(node instanceof RawStringNode);
            assertEquals("hello world", ((RawStringNode) node).getValue());
        }

        @Test
        void literal() {
            Node node = parse("`true`");
            assertTrue(node instanceof LiteralNode);
            assertEquals("true", ((LiteralNode) node).getJson());
        }

        @Test
        void jsonLiteral() {
            Node node = parse("`{\"a\": 1}`");
            assertTrue(node instanceof LiteralNode);
            assertEquals("{\"a\": 1}", ((LiteralNode) node).getJson());
        }
    }

    @Nested
    class SubExpressions {

        @Test
        void simpleSubExpression() {
            Node node = parse("foo.bar");
            assertTrue(node instanceof SubExpressionNode);
            SubExpressionNode sub = (SubExpressionNode) node;

            assertTrue(sub.getLeft() instanceof IdentifierNode);
            assertEquals("foo", ((IdentifierNode) sub.getLeft()).getName());

            assertTrue(sub.getRight() instanceof IdentifierNode);
            assertEquals("bar", ((IdentifierNode) sub.getRight()).getName());
        }

        @Test
        void chainedSubExpression() {
            Node node = parse("foo.bar.baz");
            assertTrue(node instanceof SubExpressionNode);
            SubExpressionNode outer = (SubExpressionNode) node;

            assertTrue(outer.getLeft() instanceof SubExpressionNode);
            assertTrue(outer.getRight() instanceof IdentifierNode);
            assertEquals("baz", ((IdentifierNode) outer.getRight()).getName());
        }

        @Test
        void subExpressionWithQuotedIdentifier() {
            Node node = parse("foo.\"bar baz\"");
            assertTrue(node instanceof SubExpressionNode);
            SubExpressionNode sub = (SubExpressionNode) node;
            assertEquals(
                "bar baz",
                ((IdentifierNode) sub.getRight()).getName()
            );
        }
    }

    @Nested
    class IndexExpressions {

        @Test
        void simpleIndex() {
            Node node = parse("foo[0]");
            assertTrue(node instanceof IndexExpressionNode);
            IndexExpressionNode idx = (IndexExpressionNode) node;

            assertTrue(idx.getLeft() instanceof IdentifierNode);
            assertTrue(idx.getBracket() instanceof IndexNode);
            assertEquals(0, ((IndexNode) idx.getBracket()).getIndex());
        }

        @Test
        void negativeIndex() {
            Node node = parse("foo[-1]");
            assertTrue(node instanceof IndexExpressionNode);
            IndexExpressionNode idx = (IndexExpressionNode) node;
            assertEquals(-1, ((IndexNode) idx.getBracket()).getIndex());
        }

        @Test
        void chainedIndex() {
            Node node = parse("foo[0][1]");
            assertTrue(node instanceof IndexExpressionNode);
        }

        @Test
        void bareIndex() {
            Node node = parse("[0]");
            assertTrue(node instanceof IndexNode);
            assertEquals(0, ((IndexNode) node).getIndex());
        }
    }

    @Nested
    class SliceExpressions {

        @Test
        void fullSlice() {
            Node node = parse("[0:5:2]");
            // Slices are projections, wrapped in ProjectionNode
            assertTrue(node instanceof ProjectionNode);
        }

        @Test
        void sliceWithLeft() {
            Node node = parse("foo[0:5]");
            // Slice creates a projection
            assertTrue(node instanceof ProjectionNode);
        }

        @Test
        void sliceWithStart() {
            Node node = parse("[1:]");
            assertTrue(node instanceof ProjectionNode);
        }

        @Test
        void sliceWithStop() {
            Node node = parse("[:5]");
            assertTrue(node instanceof ProjectionNode);
        }

        @Test
        void sliceWithStep() {
            Node node = parse("[::2]");
            assertTrue(node instanceof ProjectionNode);
        }

        @Test
        void reverseSlice() {
            Node node = parse("[::-1]");
            assertTrue(node instanceof ProjectionNode);
        }
    }

    @Nested
    class Projections {

        @Test
        void wildcardProjection() {
            Node node = parse("foo[*]");
            assertTrue(node instanceof ProjectionNode);
        }

        @Test
        void wildcardWithRhs() {
            Node node = parse("foo[*].bar");
            assertTrue(node instanceof ProjectionNode);
            ProjectionNode proj = (ProjectionNode) node;
            assertTrue(proj.getRight() instanceof IdentifierNode);
        }

        @Test
        void objectProjection() {
            Node node = parse("foo.*");
            assertTrue(node instanceof ObjectProjectionNode);
        }

        @Test
        void bareWildcard() {
            Node node = parse("[*]");
            assertTrue(node instanceof ProjectionNode);
        }

        @Test
        void bareStar() {
            Node node = parse("*");
            assertTrue(node instanceof ObjectProjectionNode);
        }

        @Test
        void chainedProjections() {
            Node node = parse("foo[*].bar[*].baz");
            // Should be nested projections
            assertNotNull(node);
        }
    }

    @Nested
    class FlattenExpressions {

        @Test
        void simpleFlatten() {
            Node node = parse("foo[]");
            // Flatten creates a projection-like structure
            assertNotNull(node);
        }

        @Test
        void bareFlatten() {
            Node node = parse("[]");
            assertTrue(node instanceof FlattenNode);
        }

        @Test
        void flattenWithRhs() {
            Node node = parse("foo[].bar");
            assertNotNull(node);
        }
    }

    @Nested
    class FilterExpressions {

        @Test
        void simpleFilter() {
            Node node = parse("foo[?bar]");
            assertTrue(node instanceof FilterNode);
        }

        @Test
        void filterWithComparison() {
            Node node = parse("foo[?age > `18`]");
            assertTrue(node instanceof FilterNode);
            FilterNode filter = (FilterNode) node;
            assertTrue(filter.getCondition() instanceof ComparisonNode);
        }

        @Test
        void filterWithAnd() {
            Node node = parse("foo[?bar && baz]");
            assertTrue(node instanceof FilterNode);
        }

        @Test
        void filterWithRhs() {
            Node node = parse("foo[?active].name");
            assertTrue(node instanceof FilterNode);
        }

        @Test
        void bareFilter() {
            Node node = parse("[?foo]");
            assertTrue(node instanceof FilterNode);
        }
    }

    @Nested
    class Comparisons {

        @Test
        void equals() {
            Node node = parse("foo == bar");
            assertTrue(node instanceof ComparisonNode);
            assertEquals(
                ComparisonNode.Operator.EQ,
                ((ComparisonNode) node).getOperator()
            );
        }

        @Test
        void notEquals() {
            Node node = parse("foo != bar");
            assertTrue(node instanceof ComparisonNode);
            assertEquals(
                ComparisonNode.Operator.NE,
                ((ComparisonNode) node).getOperator()
            );
        }

        @Test
        void lessThan() {
            Node node = parse("foo < bar");
            assertTrue(node instanceof ComparisonNode);
            assertEquals(
                ComparisonNode.Operator.LT,
                ((ComparisonNode) node).getOperator()
            );
        }

        @Test
        void lessOrEqual() {
            Node node = parse("foo <= bar");
            assertTrue(node instanceof ComparisonNode);
            assertEquals(
                ComparisonNode.Operator.LE,
                ((ComparisonNode) node).getOperator()
            );
        }

        @Test
        void greaterThan() {
            Node node = parse("foo > bar");
            assertTrue(node instanceof ComparisonNode);
            assertEquals(
                ComparisonNode.Operator.GT,
                ((ComparisonNode) node).getOperator()
            );
        }

        @Test
        void greaterOrEqual() {
            Node node = parse("foo >= bar");
            assertTrue(node instanceof ComparisonNode);
            assertEquals(
                ComparisonNode.Operator.GE,
                ((ComparisonNode) node).getOperator()
            );
        }

        @Test
        void comparisonWithLiteral() {
            Node node = parse("age >= `18`");
            assertTrue(node instanceof ComparisonNode);
        }
    }

    @Nested
    class LogicalOperators {

        @Test
        void orExpression() {
            Node node = parse("foo || bar");
            assertTrue(node instanceof OrNode);
        }

        @Test
        void andExpression() {
            Node node = parse("foo && bar");
            assertTrue(node instanceof AndNode);
        }

        @Test
        void notExpression() {
            Node node = parse("!foo");
            assertTrue(node instanceof NotNode);
        }

        @Test
        void complexLogical() {
            Node node = parse("foo && bar || baz");
            // Should be (foo && bar) || baz due to precedence
            assertTrue(node instanceof OrNode);
            OrNode or = (OrNode) node;
            // Left should be AndNode
            assertNotNull(or);
        }

        @Test
        void notWithComparison() {
            Node node = parse("!foo == bar");
            // ! has higher precedence than ==
            assertTrue(node instanceof ComparisonNode);
        }
    }

    @Nested
    class PipeExpressions {

        @Test
        void simplePipe() {
            Node node = parse("foo | bar");
            assertTrue(node instanceof PipeNode);
        }

        @Test
        void chainedPipes() {
            Node node = parse("foo | bar | baz");
            // Pipe is right-associative: foo | (bar | baz)
            assertTrue(node instanceof PipeNode);
        }

        @Test
        void pipeWithFilter() {
            Node node = parse("foo[*] | [0]");
            assertTrue(node instanceof PipeNode);
        }
    }

    @Nested
    class MultiSelectList {

        @Test
        void simpleMultiSelect() {
            Node node = parse("[foo, bar]");
            assertTrue(node instanceof MultiSelectListNode);
            MultiSelectListNode ms = (MultiSelectListNode) node;
            assertEquals(2, ms.getElements().size());
        }

        @Test
        void multiSelectWithExpressions() {
            Node node = parse("[foo.bar, baz[0]]");
            assertTrue(node instanceof MultiSelectListNode);
        }

        @Test
        void nestedMultiSelect() {
            Node node = parse("[[foo, bar], [baz]]");
            assertTrue(node instanceof MultiSelectListNode);
        }
    }

    @Nested
    class MultiSelectHash {

        @Test
        void simpleHash() {
            Node node = parse("{a: foo, b: bar}");
            assertTrue(node instanceof MultiSelectHashNode);
            MultiSelectHashNode hash = (MultiSelectHashNode) node;
            assertEquals(2, hash.getEntries().size());
            assertEquals("a", hash.getEntries().get(0).getKey());
            assertEquals("b", hash.getEntries().get(1).getKey());
        }

        @Test
        void hashWithExpressions() {
            Node node = parse("{name: foo.bar, age: baz[0]}");
            assertTrue(node instanceof MultiSelectHashNode);
        }

        @Test
        void hashWithQuotedKeys() {
            Node node = parse("{\"key one\": foo}");
            assertTrue(node instanceof MultiSelectHashNode);
        }
    }

    @Nested
    class FunctionCalls {

        @Test
        void noArgFunction() {
            Node node = parse("now()");
            assertTrue(node instanceof FunctionCallNode);
            FunctionCallNode func = (FunctionCallNode) node;
            assertEquals("now", func.getName());
            assertEquals(0, func.getArguments().size());
        }

        @Test
        void singleArgFunction() {
            Node node = parse("length(foo)");
            assertTrue(node instanceof FunctionCallNode);
            FunctionCallNode func = (FunctionCallNode) node;
            assertEquals("length", func.getName());
            assertEquals(1, func.getArguments().size());
        }

        @Test
        void multiArgFunction() {
            Node node = parse("contains(foo, bar)");
            assertTrue(node instanceof FunctionCallNode);
            FunctionCallNode func = (FunctionCallNode) node;
            assertEquals("contains", func.getName());
            assertEquals(2, func.getArguments().size());
        }

        @Test
        void functionWithExpressionRef() {
            Node node = parse("sort_by(foo, &bar)");
            assertTrue(node instanceof FunctionCallNode);
            FunctionCallNode func = (FunctionCallNode) node;
            assertEquals(2, func.getArguments().size());
            assertTrue(func.getArguments().get(1) instanceof ExpressionRefNode);
        }

        @Test
        void nestedFunctions() {
            Node node = parse("length(keys(foo))");
            assertTrue(node instanceof FunctionCallNode);
        }
    }

    @Nested
    class ExpressionReferences {

        @Test
        void simpleRef() {
            Node node = parse("&foo");
            assertTrue(node instanceof ExpressionRefNode);
        }

        @Test
        void refWithSubExpression() {
            Node node = parse("&foo.bar");
            assertTrue(node instanceof ExpressionRefNode);
        }
    }

    @Nested
    class Parentheses {

        @Test
        void groupedExpression() {
            Node node = parse("(foo)");
            assertTrue(node instanceof IdentifierNode);
        }

        @Test
        void groupedLogical() {
            Node node = parse("(foo || bar) && baz");
            assertTrue(node instanceof AndNode);
        }

        @Test
        void nestedParentheses() {
            Node node = parse("((foo))");
            assertTrue(node instanceof IdentifierNode);
        }
    }

    @Nested
    class ComplexExpressions {

        @Test
        void filterWithProjection() {
            Node node = parse("people[?age > `18`].name");
            assertNotNull(node);
        }

        @Test
        void complexQuery() {
            Node node = parse(
                "locations[?state == 'WA'].name | sort(@) | {WashingtonCities: join(', ', @)}"
            );
            assertNotNull(node);
        }

        @Test
        void multipleFilters() {
            Node node = parse("foo[?bar][?baz]");
            assertNotNull(node);
        }

        @Test
        void projectionWithHash() {
            Node node = parse("people[*].{name: name, age: age}");
            assertNotNull(node);
        }
    }

    @Nested
    class ErrorCases {

        @Test
        void emptyExpression() {
            assertThrows(JmesPathException.class, () -> parse(""));
        }

        @Test
        void unclosedBracket() {
            assertThrows(JmesPathException.class, () -> parse("foo[0"));
        }

        @Test
        void unclosedParen() {
            assertThrows(JmesPathException.class, () -> parse("length(foo"));
        }

        @Test
        void unclosedBrace() {
            assertThrows(JmesPathException.class, () -> parse("{a: foo"));
        }

        @Test
        void danglingDot() {
            assertThrows(JmesPathException.class, () -> parse("foo."));
        }

        @Test
        void danglingPipe() {
            assertThrows(JmesPathException.class, () -> parse("foo |"));
        }

        @Test
        void invalidOperator() {
            assertThrows(JmesPathException.class, () -> parse("foo = bar"));
        }

        @Test
        void trailingTokens() {
            assertThrows(JmesPathException.class, () -> parse("foo bar"));
        }
    }

    @Nested
    class Precedence {

        @Test
        void dotOverPipe() {
            // foo.bar | baz should be (foo.bar) | baz
            Node node = parse("foo.bar | baz");
            assertTrue(node instanceof PipeNode);
            PipeNode pipe = (PipeNode) node;
            assertTrue(pipe.getLeft() instanceof SubExpressionNode);
        }

        @Test
        void andOverOr() {
            // foo || bar && baz should be foo || (bar && baz)
            Node node = parse("foo || bar && baz");
            assertTrue(node instanceof OrNode);
        }

        @Test
        void comparisonOverLogical() {
            // foo == bar && baz == qux should be (foo == bar) && (baz == qux)
            Node node = parse("foo == bar && baz == qux");
            assertTrue(node instanceof AndNode);
        }
    }
}
