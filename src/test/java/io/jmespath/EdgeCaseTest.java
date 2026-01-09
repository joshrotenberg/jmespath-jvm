package io.jmespath;

import static org.junit.jupiter.api.Assertions.*;

import io.jmespath.runtime.MapRuntime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for edge cases identified from compliance test failures.
 */
public class EdgeCaseTest {

    private MapRuntime runtime;

    @BeforeEach
    void setUp() {
        runtime = new MapRuntime();
    }

    private Object eval(String expression, String json) {
        Expression<Object> expr = JmesPath.compile(expression);
        Object data = JmesPath.parseJson(json);
        return expr.evaluate(runtime, data);
    }

    @Nested
    class SliceProjections {

        // [:2].a should slice first, then project .a over results

        @Test
        void sliceThenProject() {
            // Given: [{a:1}, {a:2}, {a:3}]
            // [:2] -> [{a:1}, {a:2}]
            // [:2].a -> [1, 2]
            Object result = eval(
                "[:2].a",
                "[{\"a\": 1}, {\"a\": 2}, {\"a\": 3}]"
            );
            assertNotNull(result, "[:2].a should not return null");
            assertTrue(result instanceof List, "Result should be a list");
            List<?> list = (List<?>) result;
            assertEquals(2, list.size());
        }

        @Test
        void sliceReverseThenProject() {
            // [::-1].a reverses then projects
            Object result = eval(
                "[::-1].a",
                "[{\"a\": 1}, {\"a\": 2}, {\"a\": 3}]"
            );
            assertNotNull(result, "[::-1].a should not return null");
            assertTrue(result instanceof List);
            List<?> list = (List<?>) result;
            assertEquals(3, list.size());
            // Should be reversed: [3, 2, 1]
        }

        @Test
        void sliceWithMissingProperty() {
            // [:2].b where b doesn't exist should return []
            Object result = eval(
                "[:2].b",
                "[{\"a\": 1}, {\"a\": 2}, {\"a\": 3}]"
            );
            assertNotNull(result, "[:2].b should return empty array, not null");
            assertTrue(result instanceof List);
            List<?> list = (List<?>) result;
            assertEquals(0, list.size());
        }
    }

    @Nested
    class NestedProjections {

        // foo[*][0][0] - wildcard followed by multiple indices

        @Test
        void wildcardThenIndex() {
            // foo[*][0] where foo is array of arrays
            String json = "{\"foo\": [[1, 2], [3, 4], [5, 6]]}";
            Object result = eval("foo[*][0]", json);
            assertNotNull(result);
            assertTrue(result instanceof List);
            List<?> list = (List<?>) result;
            assertEquals(3, list.size()); // [1, 3, 5]
        }

        @Test
        void wildcardThenTwoIndices() {
            // foo[*][0][0] - needs to project [0][0] over each element
            String json =
                "{\"foo\": [[[1, 2], [3, 4]], [[5, 6], [7, 8]], [[9, 10], [11, 12]]]}";
            Object result = eval("foo[*][0][0]", json);
            assertNotNull(result);
            assertTrue(result instanceof List);
            List<?> list = (List<?>) result;
            assertEquals(3, list.size()); // [1, 5, 9]
        }

        @Test
        void projectThenIndexThenProperty() {
            // foo[*].bar[0].kind
            String json =
                "{\"foo\": [{\"bar\": [{\"kind\": \"basic\"}]}, {\"bar\": [{\"kind\": \"advanced\"}]}]}";
            Object result = eval("foo[*].bar[0].kind", json);
            assertNotNull(result, "foo[*].bar[0].kind should not return null");
            assertTrue(result instanceof List);
            List<?> list = (List<?>) result;
            assertEquals(2, list.size()); // ["basic", "advanced"]
        }
    }

    @Nested
    class FlattenAfterFilter {

        // reservations[].instances[?bar==`1`][]

        @Test
        void filterThenFlatten() {
            String json =
                "{\"reservations\": [{\"instances\": [{\"foo\": 1, \"bar\": 1}, {\"foo\": 2, \"bar\": 2}]}]}";
            Object result = eval("reservations[].instances[?bar==`1`][]", json);
            assertNotNull(result);
            assertTrue(result instanceof List);
            List<?> list = (List<?>) result;
            assertEquals(1, list.size());
            // Should contain {foo: 1, bar: 1}
        }

        @Test
        void simpleFlatten() {
            Object result = eval("foo[]", "{\"foo\": [[1, 2], [3, 4]]}");
            assertNotNull(result);
            assertTrue(result instanceof List);
            List<?> list = (List<?>) result;
            assertEquals(4, list.size()); // [1, 2, 3, 4]
        }

        @Test
        void flattenThenIndex() {
            // foo[][0][0]
            String json =
                "{\"foo\": [[[\"one\", \"two\"]], [[\"three\", \"four\"]]]}";
            Object result = eval("foo[][0][0]", json);
            // After flatten: [["one", "two"], ["three", "four"]]
            // Then [0][0] should project over each...
            assertNotNull(result);
        }
    }

    @Nested
    class RawStringEscaping {

        // 'foo\'bar' - escaped single quote in raw string

        @Test
        void escapedSingleQuote() {
            // Raw string with escaped quote
            Object result = eval("'foo\\'bar'", "{}");
            assertEquals("foo'bar", result);
        }

        @Test
        void simpleRawString() {
            Object result = eval("'hello'", "{}");
            assertEquals("hello", result);
        }

        @Test
        void rawStringWithBackslash() {
            // In raw strings, backslash is literal (only \' is an escape)
            // So 'foo\\bar' evaluates to "foo\\bar" (two backslashes)
            Object result = eval("'foo\\\\bar'", "{}");
            assertEquals("foo\\\\bar", result);
        }
    }

    @Nested
    class TypeValidation {

        // sort_by, min_by, max_by should error on mixed types

        @Test
        void sortByMixedTypesShouldError() {
            String json =
                "{\"people\": [{\"name\": \"a\", \"age\": 10}, {\"name\": 3, \"age\": 20}]}";
            // sort_by on name where one is string "a" and one is number 3
            // Should throw invalid-type error
            assertThrows(JmesPathException.class, () -> {
                eval("sort_by(people, &name)", json);
            });
        }

        @Test
        void minByShouldErrorOnMixedTypes() {
            String json =
                "{\"people\": [{\"name\": \"a\", \"age\": 10}, {\"name\": 3, \"age\": 20}]}";
            assertThrows(JmesPathException.class, () -> {
                eval("min_by(people, &name)", json);
            });
        }

        @Test
        void maxByShouldErrorOnMixedTypes() {
            String json =
                "{\"people\": [{\"name\": \"a\", \"age\": 10}, {\"name\": 3, \"age\": 20}]}";
            assertThrows(JmesPathException.class, () -> {
                eval("max_by(people, &name)", json);
            });
        }
    }

    @Nested
    class SyntaxErrorCases {

        // Expressions that should be syntax errors but we accept

        @Test
        void wildcardDotWildcard() {
            // [*.*] - should this be valid?
            // Compliance says syntax error, let's verify our behavior
            try {
                Object result = eval("[*.*]", "[{\"a\": 1}]");
                // If we get here without error, note what we return
                System.out.println("[*.*] returned: " + result);
            } catch (JmesPathException e) {
                // This is expected
            }
        }
    }

    @Nested
    class SliceEdgeCases {

        @Test
        void negativeStepWithLargeRange() {
            // foo[10:-20:-1] on a 20-element array
            String json =
                "{\"foo\": [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19]}";
            Object result = eval("foo[10:-20:-1]", json);
            assertNotNull(result);
            assertTrue(result instanceof List);
            List<?> list = (List<?>) result;
            // Should be [10,9,8,7,6,5,4,3,2,1,0] = 11 elements? or 10?
            System.out.println(
                "foo[10:-20:-1] size: " + list.size() + " = " + list
            );
        }
    }
}
