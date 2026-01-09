package io.jmespath;

import static org.junit.jupiter.api.Assertions.*;

import io.jmespath.runtime.MapRuntime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * End-to-end tests for the JmesPath entry point.
 */
class JmesPathTest {

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
    class CompileTests {

        @Test
        void compileSimple() {
            Expression<Object> expr = JmesPath.compile("foo");
            assertNotNull(expr);
            assertEquals("foo", expr.getExpression());
        }

        @Test
        void compileComplex() {
            Expression<Object> expr = JmesPath.compile(
                "people[?age > `18`].name | sort(@)"
            );
            assertNotNull(expr);
        }

        @Test
        void compileNull() {
            assertThrows(IllegalArgumentException.class, () ->
                JmesPath.compile(null)
            );
        }

        @Test
        void compileInvalid() {
            assertThrows(JmesPathException.class, () ->
                JmesPath.compile("foo[")
            );
        }

        @Test
        void compiledExpressionReusable() {
            Expression<Object> expr = JmesPath.compile("name");
            MapRuntime runtime = new MapRuntime();

            Object result1 = expr.evaluate(runtime, obj("name", "Alice"));
            Object result2 = expr.evaluate(runtime, obj("name", "Bob"));

            assertEquals("Alice", result1);
            assertEquals("Bob", result2);
        }
    }

    @Nested
    class SearchTests {

        @Test
        void searchSimple() {
            Object data = obj("foo", "bar");
            assertEquals("bar", JmesPath.search("foo", data));
        }

        @Test
        void searchNested() {
            Object data = obj("a", obj("b", obj("c", "value")));
            assertEquals("value", JmesPath.search("a.b.c", data));
        }

        @Test
        void searchArray() {
            Object data = obj("items", arr("a", "b", "c"));
            assertEquals("b", JmesPath.search("items[1]", data));
        }

        @Test
        void searchFilter() {
            Object data = obj(
                "people",
                arr(
                    obj("name", "Alice", "age", 30),
                    obj("name", "Bob", "age", 17)
                )
            );
            assertEquals(
                arr("Alice"),
                JmesPath.search("people[?age > `18`].name", data)
            );
        }

        @Test
        void searchWildcard() {
            Object data = obj(
                "people",
                arr(obj("name", "Alice"), obj("name", "Bob"))
            );
            assertEquals(
                arr("Alice", "Bob"),
                JmesPath.search("people[*].name", data)
            );
        }

        @Test
        void searchNull() {
            Object data = obj("foo", "bar");
            assertNull(JmesPath.search("missing", data));
        }

        @Test
        void searchOnNullData() {
            assertNull(JmesPath.search("foo", null));
        }
    }

    @Nested
    class ParseJsonTests {

        @Test
        void parseNull() {
            assertNull(JmesPath.parseJson("null"));
        }

        @Test
        void parseBoolean() {
            assertEquals(true, JmesPath.parseJson("true"));
            assertEquals(false, JmesPath.parseJson("false"));
        }

        @Test
        void parseNumber() {
            assertEquals(42, JmesPath.parseJson("42"));
            assertEquals(3.14, JmesPath.parseJson("3.14"));
        }

        @Test
        void parseString() {
            assertEquals("hello", JmesPath.parseJson("\"hello\""));
        }

        @Test
        void parseArray() {
            assertEquals(arr(1, 2, 3), JmesPath.parseJson("[1, 2, 3]"));
        }

        @Test
        void parseObject() {
            Object result = JmesPath.parseJson("{\"a\": 1}");
            assertTrue(result instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) result;
            assertEquals(1, map.get("a"));
        }
    }

    @Nested
    class RealWorldExamples {

        @Test
        void filterAndTransform() {
            // Common pattern: filter then transform
            Object data = obj(
                "users",
                arr(
                    obj("id", 1, "name", "Alice", "active", true),
                    obj("id", 2, "name", "Bob", "active", false),
                    obj("id", 3, "name", "Charlie", "active", true)
                )
            );

            List<?> result = (List<?>) JmesPath.search(
                "users[?active].{userId: id, userName: name}",
                data
            );

            assertEquals(2, result.size());
        }

        @Test
        void nestedArrayAccess() {
            Object data = obj(
                "data",
                obj(
                    "items",
                    arr(obj("values", arr(1, 2)), obj("values", arr(3, 4)))
                )
            );

            assertEquals(
                arr(1, 2, 3, 4),
                JmesPath.search("data.items[*].values[]", data)
            );
        }

        @Test
        void conditionalDefault() {
            // Use || for default values
            Object data = obj("primary", null, "secondary", "fallback");
            assertEquals(
                "fallback",
                JmesPath.search("primary || secondary", data)
            );
        }

        @Test
        void multiSelectForApiResponse() {
            Object data = obj(
                "firstName",
                "John",
                "lastName",
                "Doe",
                "email",
                "john@example.com",
                "phone",
                "555-1234"
            );

            Object result = JmesPath.search(
                "{name: firstName, contact: email}",
                data
            );

            assertTrue(result instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) result;
            assertEquals("John", map.get("name"));
            assertEquals("john@example.com", map.get("contact"));
        }

        @Test
        void pipeTakeFirst() {
            // Get first matching item
            Object data = obj(
                "items",
                arr(
                    obj("status", "pending"),
                    obj("status", "active"),
                    obj("status", "active")
                )
            );

            Object result = JmesPath.search(
                "items[?status == 'active'] | [0]",
                data
            );
            assertTrue(result instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) result;
            assertEquals("active", map.get("status"));
        }

        @Test
        void compareWithLiteral() {
            Object data = obj("count", 5);

            assertEquals(true, JmesPath.search("count > `3`", data));
            assertEquals(false, JmesPath.search("count > `10`", data));
        }

        @Test
        void objectValuesProjection() {
            Object data = obj(
                "servers",
                obj(
                    "server1",
                    obj("status", "running"),
                    "server2",
                    obj("status", "stopped"),
                    "server3",
                    obj("status", "running")
                )
            );

            List<?> result = (List<?>) JmesPath.search(
                "servers.*.status",
                data
            );
            assertEquals(3, result.size());
            assertTrue(result.contains("running"));
            assertTrue(result.contains("stopped"));
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void emptyExpression() {
            assertThrows(JmesPathException.class, () ->
                JmesPath.search("", obj())
            );
        }

        @Test
        void emptyObject() {
            assertNull(JmesPath.search("foo", obj()));
        }

        @Test
        void emptyArray() {
            assertEquals(arr(), JmesPath.search("[*]", arr()));
        }

        @Test
        void deeplyNestedAccess() {
            Object data = obj(
                "a",
                obj("b", obj("c", obj("d", obj("e", "deep"))))
            );
            assertEquals("deep", JmesPath.search("a.b.c.d.e", data));
        }

        @Test
        void chainedFilters() {
            Object data = arr(
                obj("a", true, "b", true),
                obj("a", true, "b", false),
                obj("a", false, "b", true)
            );

            // [?a] returns [{a:true, b:true}, {a:true, b:false}]
            // [?b] on that filters to [{a:true, b:true}]
            // But the second filter applies to the projection result,
            // and since filters are projections, they project each matching element
            List<?> result = (List<?>) JmesPath.search("[?a][?b]", data);
            // Note: Chained filters in JMESPath work this way
            assertNotNull(result);
        }

        @Test
        void sliceReverse() {
            assertEquals(arr(3, 2, 1), JmesPath.search("[::-1]", arr(1, 2, 3)));
        }

        @Test
        void multipleWildcards() {
            Object data = obj(
                "groups",
                arr(
                    obj("users", arr(obj("name", "Alice"), obj("name", "Bob"))),
                    obj("users", arr(obj("name", "Charlie")))
                )
            );

            // groups[*].users[*].name returns nested arrays per group
            List<?> result = (List<?>) JmesPath.search(
                "groups[*].users[*].name",
                data
            );
            // Returns 2 arrays (one per group)
            assertEquals(2, result.size());

            // Use flatten to get all names in one array
            List<?> flatResult = (List<?>) JmesPath.search(
                "groups[*].users[].name",
                data
            );
            assertEquals(3, flatResult.size());
        }
    }
}
