package io.jmespath;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for all 26 built-in JMESPath functions.
 */
class FunctionTest {

    private Object eval(String expr, String json) {
        return JmesPath.search(expr, JmesPath.parseJson(json));
    }

    private void assertNumberEquals(long expected, Object actual) {
        assertTrue(
            actual instanceof Number,
            "Expected number but got " +
                (actual == null ? "null" : actual.getClass())
        );
        assertEquals(expected, ((Number) actual).longValue());
    }

    private void assertDoubleEquals(double expected, Object actual) {
        assertTrue(
            actual instanceof Number,
            "Expected number but got " +
                (actual == null ? "null" : actual.getClass())
        );
        assertEquals(expected, ((Number) actual).doubleValue(), 0.0001);
    }

    @Nested
    class AbsTests {

        @Test
        void positiveNumber() {
            assertEquals(5L, eval("abs(@)", "5"));
        }

        @Test
        void negativeNumber() {
            assertEquals(5L, eval("abs(@)", "-5"));
        }

        @Test
        void negativeDouble() {
            assertEquals(3.14, eval("abs(@)", "-3.14"));
        }
    }

    @Nested
    class AvgTests {

        @Test
        void numbersArray() {
            assertEquals(2.0, eval("avg(@)", "[1, 2, 3]"));
        }

        @Test
        void emptyArray() {
            assertNull(eval("avg(@)", "[]"));
        }
    }

    @Nested
    class CeilTests {

        @Test
        void roundsUp() {
            assertEquals(2L, eval("ceil(@)", "1.1"));
        }

        @Test
        void negativeCeil() {
            assertEquals(-1L, eval("ceil(@)", "-1.9"));
        }
    }

    @Nested
    class FloorTests {

        @Test
        void roundsDown() {
            assertEquals(1L, eval("floor(@)", "1.9"));
        }

        @Test
        void negativeFloor() {
            assertEquals(-2L, eval("floor(@)", "-1.1"));
        }
    }

    @Nested
    class SumTests {

        @Test
        void numbersArray() {
            assertDoubleEquals(6.0, eval("sum(@)", "[1, 2, 3]"));
        }

        @Test
        void emptyArray() {
            assertDoubleEquals(0.0, eval("sum(@)", "[]"));
        }
    }

    @Nested
    class LengthTests {

        @Test
        void stringLength() {
            assertNumberEquals(5, eval("length(@)", "\"hello\""));
        }

        @Test
        void arrayLength() {
            assertNumberEquals(3, eval("length(@)", "[1, 2, 3]"));
        }

        @Test
        void objectLength() {
            assertNumberEquals(2, eval("length(@)", "{\"a\": 1, \"b\": 2}"));
        }
    }

    @Nested
    class TypeTests {

        @Test
        void nullType() {
            assertEquals("null", eval("type(@)", "null"));
        }

        @Test
        void booleanType() {
            assertEquals("boolean", eval("type(@)", "true"));
        }

        @Test
        void numberType() {
            assertEquals("number", eval("type(@)", "42"));
        }

        @Test
        void stringType() {
            assertEquals("string", eval("type(@)", "\"hello\""));
        }

        @Test
        void arrayType() {
            assertEquals("array", eval("type(@)", "[1, 2]"));
        }

        @Test
        void objectType() {
            assertEquals("object", eval("type(@)", "{\"a\": 1}"));
        }
    }

    @Nested
    class KeysTests {

        @Test
        void objectKeys() {
            Object result = eval("keys(@)", "{\"a\": 1, \"b\": 2}");
            assertTrue(result instanceof java.util.List);
            java.util.List<?> keys = (java.util.List<?>) result;
            assertEquals(2, keys.size());
            assertTrue(keys.contains("a"));
            assertTrue(keys.contains("b"));
        }
    }

    @Nested
    class ValuesTests {

        @Test
        void objectValues() {
            Object result = eval("values(@)", "{\"a\": 1, \"b\": 2}");
            assertTrue(result instanceof java.util.List);
            java.util.List<?> values = (java.util.List<?>) result;
            assertEquals(2, values.size());
            // Check that values contain 1 and 2 (handling Integer vs Long)
            assertTrue(containsNumber(values, 1), "values should contain 1");
            assertTrue(containsNumber(values, 2), "values should contain 2");
        }

        private boolean containsNumber(java.util.List<?> list, long expected) {
            for (Object item : list) {
                if (
                    item instanceof Number &&
                    ((Number) item).longValue() == expected
                ) {
                    return true;
                }
            }
            return false;
        }
    }

    @Nested
    class ContainsTests {

        @Test
        void arrayContainsElement() {
            assertEquals(true, eval("contains(@, `2`)", "[1, 2, 3]"));
        }

        @Test
        void arrayNotContainsElement() {
            assertEquals(false, eval("contains(@, `5`)", "[1, 2, 3]"));
        }

        @Test
        void stringContainsSubstring() {
            assertEquals(true, eval("contains(@, 'ell')", "\"hello\""));
        }

        @Test
        void stringNotContainsSubstring() {
            assertEquals(false, eval("contains(@, 'xyz')", "\"hello\""));
        }
    }

    @Nested
    class StartsWithTests {

        @Test
        void stringStartsWith() {
            assertEquals(true, eval("starts_with(@, 'hel')", "\"hello\""));
        }

        @Test
        void stringNotStartsWith() {
            assertEquals(false, eval("starts_with(@, 'xyz')", "\"hello\""));
        }
    }

    @Nested
    class EndsWithTests {

        @Test
        void stringEndsWith() {
            assertEquals(true, eval("ends_with(@, 'llo')", "\"hello\""));
        }

        @Test
        void stringNotEndsWith() {
            assertEquals(false, eval("ends_with(@, 'xyz')", "\"hello\""));
        }
    }

    @Nested
    class JoinTests {

        @Test
        void joinStrings() {
            assertEquals(
                "a, b, c",
                eval("join(', ', @)", "[\"a\", \"b\", \"c\"]")
            );
        }

        @Test
        void joinWithEmptyGlue() {
            assertEquals("abc", eval("join('', @)", "[\"a\", \"b\", \"c\"]"));
        }
    }

    @Nested
    class MinTests {

        @Test
        void minNumbers() {
            assertNumberEquals(1, eval("min(@)", "[3, 1, 2]"));
        }

        @Test
        void minStrings() {
            assertEquals("a", eval("min(@)", "[\"c\", \"a\", \"b\"]"));
        }

        @Test
        void minEmptyArray() {
            assertNull(eval("min(@)", "[]"));
        }
    }

    @Nested
    class MaxTests {

        @Test
        void maxNumbers() {
            assertNumberEquals(3, eval("max(@)", "[1, 3, 2]"));
        }

        @Test
        void maxStrings() {
            assertEquals("c", eval("max(@)", "[\"a\", \"c\", \"b\"]"));
        }

        @Test
        void maxEmptyArray() {
            assertNull(eval("max(@)", "[]"));
        }
    }

    @Nested
    class ReverseTests {

        @Test
        void reverseArray() {
            java.util.List<?> result = (java.util.List<?>) eval(
                "reverse(@)",
                "[1, 2, 3]"
            );
            assertEquals(3, result.size());
            assertNumberEquals(3, result.get(0));
            assertNumberEquals(2, result.get(1));
            assertNumberEquals(1, result.get(2));
        }

        @Test
        void reverseString() {
            assertEquals("olleh", eval("reverse(@)", "\"hello\""));
        }
    }

    @Nested
    class SortTests {

        @Test
        void sortNumbers() {
            java.util.List<?> result = (java.util.List<?>) eval(
                "sort(@)",
                "[3, 1, 2]"
            );
            assertEquals(3, result.size());
            assertNumberEquals(1, result.get(0));
            assertNumberEquals(2, result.get(1));
            assertNumberEquals(3, result.get(2));
        }

        @Test
        void sortStrings() {
            java.util.List<?> result = (java.util.List<?>) eval(
                "sort(@)",
                "[\"c\", \"a\", \"b\"]"
            );
            assertEquals(3, result.size());
            assertEquals("a", result.get(0));
            assertEquals("b", result.get(1));
            assertEquals("c", result.get(2));
        }
    }

    @Nested
    class MergeTests {

        @Test
        @SuppressWarnings("unchecked")
        void mergeObjects() {
            java.util.Map<String, ?> result = (java.util.Map<String, ?>) eval(
                "merge(@[0], @[1])",
                "[{\"a\": 1}, {\"b\": 2}]"
            );
            assertEquals(2, result.size());
            assertNumberEquals(1, result.get("a"));
            assertNumberEquals(2, result.get("b"));
        }

        @Test
        @SuppressWarnings("unchecked")
        void mergeOverwrites() {
            java.util.Map<String, ?> result = (java.util.Map<String, ?>) eval(
                "merge(@[0], @[1])",
                "[{\"a\": 1}, {\"a\": 2}]"
            );
            assertEquals(1, result.size());
            assertNumberEquals(2, result.get("a"));
        }
    }

    @Nested
    class NotNullTests {

        @Test
        void firstNonNull() {
            assertNumberEquals(
                1,
                eval("not_null(foo, bar, baz)", "{\"baz\": 1}")
            );
        }

        @Test
        void allNull() {
            assertNull(eval("not_null(foo, bar)", "{}"));
        }
    }

    @Nested
    class ToArrayTests {

        @Test
        void arrayPassthrough() {
            java.util.List<?> result = (java.util.List<?>) eval(
                "to_array(@)",
                "[1, 2]"
            );
            assertEquals(2, result.size());
        }

        @Test
        void wrapNonArray() {
            java.util.List<?> result = (java.util.List<?>) eval(
                "to_array(@)",
                "42"
            );
            assertEquals(1, result.size());
            assertNumberEquals(42, result.get(0));
        }
    }

    @Nested
    class ToNumberTests {

        @Test
        void numberPassthrough() {
            assertNumberEquals(42, eval("to_number(@)", "42"));
        }

        @Test
        void stringToNumber() {
            assertNumberEquals(42, eval("to_number(@)", "\"42\""));
        }

        @Test
        void stringToDouble() {
            assertEquals(3.14, eval("to_number(@)", "\"3.14\""));
        }

        @Test
        void invalidStringReturnsNull() {
            assertNull(eval("to_number(@)", "\"not a number\""));
        }
    }

    @Nested
    class ToStringTests {

        @Test
        void stringPassthrough() {
            assertEquals("hello", eval("to_string(@)", "\"hello\""));
        }

        @Test
        void numberToString() {
            assertEquals("42", eval("to_string(@)", "42"));
        }

        @Test
        void booleanToString() {
            assertEquals("true", eval("to_string(@)", "true"));
        }

        @Test
        void nullToString() {
            assertEquals("null", eval("to_string(@)", "null"));
        }

        @Test
        void arrayToString() {
            assertEquals("[1,2,3]", eval("to_string(@)", "[1, 2, 3]"));
        }
    }

    @Nested
    class MapTests {

        @Test
        void mapExpression() {
            java.util.List<?> result = (java.util.List<?>) eval(
                "map(&name, @)",
                "[{\"name\": \"a\"}, {\"name\": \"b\"}]"
            );
            assertEquals(2, result.size());
            assertEquals("a", result.get(0));
            assertEquals("b", result.get(1));
        }
    }

    @Nested
    class SortByTests {

        @Test
        @SuppressWarnings("unchecked")
        void sortByField() {
            java.util.List<?> result = (java.util.List<?>) eval(
                "sort_by(@, &age)",
                "[{\"name\": \"b\", \"age\": 30}, {\"name\": \"a\", \"age\": 20}]"
            );
            assertEquals(2, result.size());
            java.util.Map<String, ?> first = (java.util.Map<
                String,
                ?
            >) result.get(0);
            assertEquals("a", first.get("name"));
        }
    }

    @Nested
    class MinByTests {

        @Test
        @SuppressWarnings("unchecked")
        void minByField() {
            java.util.Map<String, ?> result = (java.util.Map<String, ?>) eval(
                "min_by(@, &age)",
                "[{\"name\": \"b\", \"age\": 30}, {\"name\": \"a\", \"age\": 20}]"
            );
            assertEquals("a", result.get("name"));
        }
    }

    @Nested
    class MaxByTests {

        @Test
        @SuppressWarnings("unchecked")
        void maxByField() {
            java.util.Map<String, ?> result = (java.util.Map<String, ?>) eval(
                "max_by(@, &age)",
                "[{\"name\": \"b\", \"age\": 30}, {\"name\": \"a\", \"age\": 20}]"
            );
            assertEquals("b", result.get("name"));
        }
    }
}
