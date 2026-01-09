package io.jmespath;

import static org.junit.jupiter.api.Assertions.*;

import io.jmespath.internal.CompiledExpression;
import io.jmespath.internal.Parser;
import io.jmespath.internal.Scope;
import io.jmespath.internal.node.Node;
import io.jmespath.runtime.MapRuntime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * End-to-end evaluation tests.
 */
class EvaluationTest {

    private MapRuntime runtime;

    @BeforeEach
    void setUp() {
        runtime = new MapRuntime();
    }

    private Object evaluate(String expression, Object data) {
        Parser parser = new Parser(expression);
        Node root = parser.parse();
        return root.evaluate(runtime, data, Scope.empty());
    }

    private Map<String, Object> obj(Object... kvs) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < kvs.length; i += 2) {
            map.put((String) kvs[i], kvs[i + 1]);
        }
        return map;
    }

    private List<Object> arr(Object... items) {
        return Arrays.asList(items);
    }

    @Nested
    class IdentifierExpressions {

        @Test
        void simpleIdentifier() {
            Object data = obj("foo", "bar");
            assertEquals("bar", evaluate("foo", data));
        }

        @Test
        void missingProperty() {
            Object data = obj("foo", "bar");
            assertNull(evaluate("baz", data));
        }

        @Test
        void identifierOnNonObject() {
            assertNull(evaluate("foo", "string"));
            assertNull(evaluate("foo", 42));
            assertNull(evaluate("foo", null));
        }

        @Test
        void nestedIdentifier() {
            Object data = obj("foo", obj("bar", "baz"));
            assertEquals("baz", evaluate("foo.bar", data));
        }

        @Test
        void deeplyNested() {
            Object data = obj("a", obj("b", obj("c", obj("d", "value"))));
            assertEquals("value", evaluate("a.b.c.d", data));
        }
    }

    @Nested
    class IndexExpressions {

        @Test
        void simpleIndex() {
            Object data = obj("arr", arr("a", "b", "c"));
            assertEquals("a", evaluate("arr[0]", data));
            assertEquals("b", evaluate("arr[1]", data));
            assertEquals("c", evaluate("arr[2]", data));
        }

        @Test
        void negativeIndex() {
            Object data = obj("arr", arr("a", "b", "c"));
            assertEquals("c", evaluate("arr[-1]", data));
            assertEquals("b", evaluate("arr[-2]", data));
            assertEquals("a", evaluate("arr[-3]", data));
        }

        @Test
        void outOfBounds() {
            Object data = obj("arr", arr("a", "b"));
            assertNull(evaluate("arr[5]", data));
            assertNull(evaluate("arr[-5]", data));
        }

        @Test
        void indexOnNonArray() {
            Object data = obj("foo", "bar");
            assertNull(evaluate("foo[0]", data));
        }

        @Test
        void chainedIndex() {
            Object data = obj("matrix", arr(arr(1, 2), arr(3, 4)));
            assertEquals(1, evaluate("matrix[0][0]", data));
            assertEquals(4, evaluate("matrix[1][1]", data));
        }
    }

    @Nested
    class SliceExpressions {

        @Test
        void basicSlice() {
            Object data = arr(0, 1, 2, 3, 4);
            assertEquals(arr(0, 1, 2), evaluate("[0:3]", data));
        }

        @Test
        void sliceWithStep() {
            Object data = arr(0, 1, 2, 3, 4, 5);
            assertEquals(arr(0, 2, 4), evaluate("[::2]", data));
        }

        @Test
        void reverseSlice() {
            Object data = arr(0, 1, 2, 3, 4);
            assertEquals(arr(4, 3, 2, 1, 0), evaluate("[::-1]", data));
        }

        @Test
        void sliceFromEnd() {
            Object data = arr(0, 1, 2, 3, 4);
            assertEquals(arr(3, 4), evaluate("[-2:]", data));
        }

        @Test
        void sliceToEnd() {
            Object data = arr(0, 1, 2, 3, 4);
            assertEquals(arr(2, 3, 4), evaluate("[2:]", data));
        }
    }

    @Nested
    class WildcardProjections {

        @Test
        void arrayWildcard() {
            Object data = obj(
                "people",
                arr(obj("name", "Alice"), obj("name", "Bob"))
            );
            assertEquals(arr("Alice", "Bob"), evaluate("people[*].name", data));
        }

        @Test
        void objectWildcard() {
            Object data = obj("a", obj("val", 1), "b", obj("val", 2));
            List<?> result = (List<?>) evaluate("*.val", data);
            assertEquals(2, result.size());
            assertTrue(result.contains(1));
            assertTrue(result.contains(2));
        }

        @Test
        void wildcardFiltersNull() {
            Object data = obj(
                "arr",
                arr(
                    obj("name", "Alice"),
                    obj("age", 30) // no name
                )
            );
            // Only returns non-null values
            assertEquals(arr("Alice"), evaluate("arr[*].name", data));
        }
    }

    @Nested
    class FilterExpressions {

        @Test
        void simpleFilter() {
            Object data = arr(
                obj("name", "Alice", "active", true),
                obj("name", "Bob", "active", false)
            );
            List<?> result = (List<?>) evaluate("[?active]", data);
            assertEquals(1, result.size());
        }

        @Test
        void filterWithComparison() {
            Object data = arr(
                obj("name", "Alice", "age", 30),
                obj("name", "Bob", "age", 17)
            );
            List<?> result = (List<?>) evaluate("[?age > `18`].name", data);
            assertEquals(arr("Alice"), result);
        }

        @Test
        void filterWithEquals() {
            Object data = arr(
                obj("name", "Alice", "city", "NYC"),
                obj("name", "Bob", "city", "LA")
            );
            List<?> result = (List<?>) evaluate("[?city == 'NYC'].name", data);
            assertEquals(arr("Alice"), result);
        }

        @Test
        void filterWithAnd() {
            Object data = arr(
                obj("active", true, "age", 30),
                obj("active", true, "age", 17),
                obj("active", false, "age", 30)
            );
            List<?> result = (List<?>) evaluate(
                "[?active && age > `18`]",
                data
            );
            assertEquals(1, result.size());
        }
    }

    @Nested
    class FlattenExpressions {

        @Test
        void simpleFlatten() {
            Object data = arr(arr(1, 2), arr(3, 4));
            assertEquals(arr(1, 2, 3, 4), evaluate("[]", data));
        }

        @Test
        void flattenMixed() {
            Object data = arr(arr(1, 2), 3, arr(4));
            assertEquals(arr(1, 2, 3, 4), evaluate("[]", data));
        }

        @Test
        void flattenOnProperty() {
            Object data = obj("nested", arr(arr("a", "b"), arr("c")));
            assertEquals(arr("a", "b", "c"), evaluate("nested[]", data));
        }
    }

    @Nested
    class LogicalOperators {

        @Test
        void orReturnsFirstTruthy() {
            Object data = obj("a", null, "b", "value");
            assertEquals("value", evaluate("a || b", data));
        }

        @Test
        void orReturnsLeftIfTruthy() {
            Object data = obj("a", "first", "b", "second");
            assertEquals("first", evaluate("a || b", data));
        }

        @Test
        void andReturnsLeftIfFalsy() {
            Object data = obj("a", null, "b", "value");
            assertNull(evaluate("a && b", data));
        }

        @Test
        void andReturnsRightIfLeftTruthy() {
            Object data = obj("a", "first", "b", "second");
            assertEquals("second", evaluate("a && b", data));
        }

        @Test
        void notNegates() {
            Object data = obj("active", true);
            assertEquals(false, evaluate("!active", data));
            assertEquals(true, evaluate("!missing", data));
        }
    }

    @Nested
    class Comparisons {

        @Test
        void equalsTrue() {
            Object data = obj("a", 1, "b", 1);
            assertEquals(true, evaluate("a == b", data));
        }

        @Test
        void equalsFalse() {
            Object data = obj("a", 1, "b", 2);
            assertEquals(false, evaluate("a == b", data));
        }

        @Test
        void notEquals() {
            Object data = obj("a", 1, "b", 2);
            assertEquals(true, evaluate("a != b", data));
        }

        @Test
        void lessThan() {
            Object data = obj("a", 1, "b", 2);
            assertEquals(true, evaluate("a < b", data));
            assertEquals(false, evaluate("b < a", data));
        }

        @Test
        void greaterThan() {
            Object data = obj("a", 1, "b", 2);
            assertEquals(false, evaluate("a > b", data));
            assertEquals(true, evaluate("b > a", data));
        }

        @Test
        void stringComparison() {
            Object data = obj("a", "apple", "b", "banana");
            assertEquals(true, evaluate("a < b", data));
        }
    }

    @Nested
    class PipeExpressions {

        @Test
        void simplePipe() {
            Object data = obj("arr", arr("a", "b", "c"));
            assertEquals("a", evaluate("arr | [0]", data));
        }

        @Test
        void pipeStopsProjection() {
            Object data = obj(
                "arr",
                arr(obj("name", "Alice"), obj("name", "Bob"))
            );
            // Without pipe, [*].name would project
            // With pipe, [0] gets first element of the projected array
            assertEquals("Alice", evaluate("arr[*].name | [0]", data));
        }
    }

    @Nested
    class MultiSelectList {

        @Test
        void simpleMultiSelect() {
            Object data = obj("a", 1, "b", 2, "c", 3);
            assertEquals(arr(1, 2), evaluate("[a, b]", data));
        }

        @Test
        void multiSelectWithExpressions() {
            Object data = obj("person", obj("first", "John", "last", "Doe"));
            assertEquals(
                arr("John", "Doe"),
                evaluate("[person.first, person.last]", data)
            );
        }
    }

    @Nested
    class MultiSelectHash {

        @Test
        void simpleHash() {
            Object data = obj("firstName", "John", "lastName", "Doe");
            Object result = evaluate(
                "{first: firstName, last: lastName}",
                data
            );
            assertTrue(result instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) result;
            assertEquals("John", map.get("first"));
            assertEquals("Doe", map.get("last"));
        }

        @Test
        void hashWithExpressions() {
            Object data = obj("person", obj("name", "Alice", "age", 30));
            Object result = evaluate("{n: person.name, a: person.age}", data);
            assertTrue(result instanceof Map);
        }
    }

    @Nested
    class CurrentNode {

        @Test
        void atReturnsCurrentValue() {
            Object data = obj("foo", "bar");
            assertEquals(data, evaluate("@", data));
        }

        @Test
        void atInFilter() {
            Object data = arr(1, 2, 3, 4, 5);
            assertEquals(arr(4, 5), evaluate("[?@ > `3`]", data));
        }
    }

    @Nested
    class Literals {

        @Test
        void booleanLiteral() {
            assertEquals(true, evaluate("`true`", null));
            assertEquals(false, evaluate("`false`", null));
        }

        @Test
        void numberLiteral() {
            assertEquals(42, evaluate("`42`", null));
        }

        @Test
        void stringLiteral() {
            assertEquals("hello", evaluate("`\"hello\"`", null));
        }

        @Test
        void arrayLiteral() {
            assertEquals(arr(1, 2, 3), evaluate("`[1, 2, 3]`", null));
        }

        @Test
        void objectLiteral() {
            Object result = evaluate("`{\"a\": 1}`", null);
            assertTrue(result instanceof Map);
        }
    }

    @Nested
    class RawStrings {

        @Test
        void simpleRawString() {
            assertEquals("hello", evaluate("'hello'", null));
        }

        @Test
        void rawStringInComparison() {
            Object data = obj("name", "Alice");
            assertEquals(true, evaluate("name == 'Alice'", data));
        }
    }

    @Nested
    class ComplexExpressions {

        @Test
        void filterAndProject() {
            Object data = obj(
                "people",
                arr(
                    obj("name", "Alice", "age", 30, "active", true),
                    obj("name", "Bob", "age", 25, "active", false),
                    obj("name", "Charlie", "age", 35, "active", true)
                )
            );
            List<?> result = (List<?>) evaluate(
                "people[?active && age > `28`].name",
                data
            );
            assertEquals(arr("Alice", "Charlie"), result);
        }

        @Test
        void multipleProjections() {
            Object data = obj(
                "data",
                arr(obj("items", arr("a", "b")), obj("items", arr("c", "d")))
            );
            // This should flatten: [["a","b"],["c","d"]] -> ["a","b","c","d"]
            assertEquals(
                arr("a", "b", "c", "d"),
                evaluate("data[*].items[]", data)
            );
        }

        @Test
        void hashWithProjection() {
            Object data = obj(
                "items",
                arr(obj("id", 1, "name", "foo"), obj("id", 2, "name", "bar"))
            );
            List<?> result = (List<?>) evaluate(
                "items[*].{identifier: id, label: name}",
                data
            );
            assertEquals(2, result.size());
        }
    }
}
