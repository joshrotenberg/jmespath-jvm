package io.jmespath;

import static org.junit.jupiter.api.Assertions.*;

import io.jmespath.runtime.MapRuntime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for JEP-18 lexical scoping feature.
 *
 * <p>Lexical scoping adds the ability to define variables with let expressions
 * and reference them with $variable syntax.
 */
public class LexicalScopingTest {

    private MapRuntime runtime;

    @BeforeEach
    void setUp() {
        runtime = new MapRuntime();
    }

    @Nested
    class BasicLetExpressions {

        @Test
        void simpleLetBinding() {
            // let $x = foo in $x
            Object data = map("foo", "bar");
            Object result = evaluate("let $x = foo in $x", data);
            assertEquals("bar", result);
        }

        @Test
        void letWithLiteralValue() {
            // let $x = `42` in $x
            Object data = map("foo", "bar");
            Object result = evaluate("let $x = `42` in $x", data);
            assertEquals(42, result);
        }

        @Test
        void letWithNestedAccess() {
            // let $name = person.name in $name
            Object data = map("person", map("name", "Alice", "age", 30));
            Object result = evaluate("let $name = person.name in $name", data);
            assertEquals("Alice", result);
        }

        @Test
        void multipleBindings() {
            // let $a = foo, $b = bar in [$a, $b]
            Object data = map("foo", 1, "bar", 2);
            Object result = evaluate(
                "let $a = foo, $b = bar in [$a, $b]",
                data
            );
            assertEquals(Arrays.asList(1, 2), result);
        }

        @Test
        void bindingsCanReferenceEarlierBindings() {
            // let $x = foo, $y = $x in $y
            Object data = map("foo", "hello");
            Object result = evaluate("let $x = foo, $y = $x in $y", data);
            assertEquals("hello", result);
        }

        @Test
        void variableInBody() {
            // let $threshold = `10` in items[?value > $threshold]
            Object data = map(
                "items",
                Arrays.asList(
                    map("value", 5),
                    map("value", 15),
                    map("value", 8),
                    map("value", 20)
                )
            );
            Object result = evaluate(
                "let $threshold = `10` in items[?value > $threshold]",
                data
            );
            List<?> list = (List<?>) result;
            assertEquals(2, list.size());
        }
    }

    @Nested
    class VariableScoping {

        @Test
        void variableNotInScopeReturnsNull() {
            Object data = map("foo", "bar");
            Object result = evaluate("$undefined", data);
            assertNull(result);
        }

        @Test
        void nestedLetShadowing() {
            // let $x = `1` in (let $x = `2` in $x)
            Object data = map();
            Object result = evaluate(
                "let $x = `1` in (let $x = `2` in $x)",
                data
            );
            assertEquals(2, result);
        }

        @Test
        void outerScopeAccessible() {
            // let $outer = `1` in (let $inner = `2` in [$outer, $inner])
            Object data = map();
            Object result = evaluate(
                "let $outer = `1` in (let $inner = `2` in [$outer, $inner])",
                data
            );
            assertEquals(Arrays.asList(1, 2), result);
        }

        @Test
        void shadowingDoesNotAffectOuter() {
            // let $x = `1` in [(let $x = `2` in $x), $x]
            Object data = map();
            Object result = evaluate(
                "let $x = `1` in [(let $x = `2` in $x), $x]",
                data
            );
            assertEquals(Arrays.asList(2, 1), result);
        }
    }

    @Nested
    class VariablesInProjections {

        @Test
        void variableInFilter() {
            // let $min = `18` in people[?age >= $min].name
            Object data = map(
                "people",
                Arrays.asList(
                    map("name", "Alice", "age", 25),
                    map("name", "Bob", "age", 15),
                    map("name", "Charlie", "age", 30)
                )
            );
            Object result = evaluate(
                "let $min = `18` in people[?age >= $min].name",
                data
            );
            assertEquals(Arrays.asList("Alice", "Charlie"), result);
        }

        @Test
        void variableInMapExpression() {
            // Verify variables work alongside projections
            Object data = map(
                "items",
                Arrays.asList("a", "b", "c"),
                "prefix",
                "test"
            );
            Object result = evaluate("let $p = prefix in items[*]", data);
            assertEquals(Arrays.asList("a", "b", "c"), result);
        }

        @Test
        void variableInNestedFilter() {
            // let $target = first_choice in states[?name == $target].cities
            Object data = map(
                "first_choice",
                "WA",
                "states",
                Arrays.asList(
                    map(
                        "name",
                        "WA",
                        "cities",
                        Arrays.asList("Seattle", "Tacoma")
                    ),
                    map("name", "CA", "cities", Arrays.asList("LA", "SF")),
                    map("name", "WA", "cities", Arrays.asList("Spokane"))
                )
            );
            Object result = evaluate(
                "let $target = first_choice in states[?name == $target].cities",
                data
            );
            // Two WA states, flattened
            List<?> list = (List<?>) result;
            assertEquals(2, list.size());
        }
    }

    @Nested
    class VariablesWithFunctions {

        @Test
        void variableAsArgument() {
            // let $arr = items in length($arr)
            Object data = map("items", Arrays.asList(1, 2, 3, 4, 5));
            Object result = evaluate("let $arr = items in length($arr)", data);
            assertEquals(5, result);
        }

        @Test
        void variableInSortBy() {
            // Variables should work alongside sort_by
            Object data = map(
                "items",
                Arrays.asList(
                    map("name", "b", "val", 2),
                    map("name", "a", "val", 1),
                    map("name", "c", "val", 3)
                )
            );
            Object result = evaluate(
                "let $count = `3` in sort_by(items, &name)[*].name",
                data
            );
            assertEquals(Arrays.asList("a", "b", "c"), result);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void emptyBodyExpression() {
            // let $x = foo in @
            Object data = map("foo", "bar");
            Object result = evaluate("let $x = foo in @", data);
            assertEquals(data, result);
        }

        @Test
        void variableWithUnderscore() {
            // let $my_var = foo in $my_var
            Object data = map("foo", "value");
            Object result = evaluate("let $my_var = foo in $my_var", data);
            assertEquals("value", result);
        }

        @Test
        void variableWithNumbers() {
            // let $var1 = foo in $var1
            Object data = map("foo", "value");
            Object result = evaluate("let $var1 = foo in $var1", data);
            assertEquals("value", result);
        }

        @Test
        void chainedLetAndPipe() {
            // (let $x = foo in $x) | length(@)
            Object data = map("foo", "hello");
            Object result = evaluate("(let $x = foo in $x) | length(@)", data);
            assertEquals(5, result);
        }

        @Test
        void letInMultiSelectList() {
            // [foo, let $x = bar in $x, baz]
            Object data = map("foo", 1, "bar", 2, "baz", 3);
            Object result = evaluate("[foo, let $x = bar in $x, baz]", data);
            assertEquals(Arrays.asList(1, 2, 3), result);
        }

        @Test
        void letInMultiSelectHash() {
            // {a: foo, b: let $x = bar in $x}
            Object data = map("foo", 1, "bar", 2);
            Object result = evaluate("{a: foo, b: let $x = bar in $x}", data);
            Map<?, ?> resultMap = (Map<?, ?>) result;
            assertEquals(1, resultMap.get("a"));
            assertEquals(2, resultMap.get("b"));
        }
    }

    @Nested
    class RealWorldExamples {

        @Test
        void filterByParentValue() {
            // The motivating example from JEP-18:
            // Find cities in the user's home state
            Object data = map(
                "home_state",
                "WA",
                "states",
                Arrays.asList(
                    map(
                        "name",
                        "WA",
                        "cities",
                        Arrays.asList("Seattle", "Tacoma", "Spokane")
                    ),
                    map(
                        "name",
                        "CA",
                        "cities",
                        Arrays.asList("Los Angeles", "San Francisco")
                    ),
                    map(
                        "name",
                        "OR",
                        "cities",
                        Arrays.asList("Portland", "Eugene")
                    )
                )
            );
            Object result = evaluate(
                "let $home = home_state in states[?name == $home].cities[]",
                data
            );
            assertEquals(Arrays.asList("Seattle", "Tacoma", "Spokane"), result);
        }

        @Test
        void pairElementsWithMetadata() {
            // Pair array elements with metadata from parent
            Object data = map(
                "category",
                "fruit",
                "items",
                Arrays.asList("apple", "banana", "cherry")
            );
            Object result = evaluate(
                "let $cat = category in items[*].{name: @, category: $cat}",
                data
            );
            List<?> list = (List<?>) result;
            assertEquals(3, list.size());
            Map<?, ?> first = (Map<?, ?>) list.get(0);
            assertEquals("apple", first.get("name"));
            assertEquals("fruit", first.get("category"));
        }

        @Test
        void computedThreshold() {
            // Filter based on computed value
            Object data = map(
                "multiplier",
                2,
                "base",
                10,
                "values",
                Arrays.asList(5, 15, 25, 35)
            );
            // Can't compute in let without functions, but can use static threshold
            Object result = evaluate(
                "let $threshold = `20` in values[?@ > $threshold]",
                data
            );
            assertEquals(Arrays.asList(25, 35), result);
        }
    }

    @Nested
    class LexerAndParserTests {

        @Test
        void parseLetExpression() {
            // Verify parsing works
            Expression<Object> expr = JmesPath.compile("let $x = foo in $x");
            assertNotNull(expr);
        }

        @Test
        void parseMultipleBindings() {
            Expression<Object> expr = JmesPath.compile(
                "let $a = foo, $b = bar, $c = baz in [$a, $b, $c]"
            );
            assertNotNull(expr);
        }

        @Test
        void parseNestedLet() {
            Expression<Object> expr = JmesPath.compile(
                "let $x = `1` in let $y = `2` in [$x, $y]"
            );
            assertNotNull(expr);
        }

        @Test
        void parseSyntaxErrorMissingIn() {
            assertThrows(JmesPathException.class, () ->
                JmesPath.compile("let $x = foo $x")
            );
        }

        @Test
        void parseSyntaxErrorMissingEquals() {
            assertThrows(JmesPathException.class, () ->
                JmesPath.compile("let $x foo in $x")
            );
        }

        @Test
        void parseSyntaxErrorMissingVariable() {
            assertThrows(JmesPathException.class, () ->
                JmesPath.compile("let foo = bar in baz")
            );
        }
    }

    // Helper methods

    private Object evaluate(String expression, Object data) {
        Expression<Object> expr = JmesPath.compile(expression);
        return expr.evaluate(runtime, data);
    }

    private Map<String, Object> map(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }
}
