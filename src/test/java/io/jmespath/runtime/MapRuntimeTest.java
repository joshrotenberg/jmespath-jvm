package io.jmespath.runtime;

import io.jmespath.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MapRuntime.
 */
class MapRuntimeTest {

    private MapRuntime runtime;

    @BeforeEach
    void setUp() {
        runtime = new MapRuntime();
    }

    @Nested
    class TypeChecking {

        @Test
        void isNull() {
            assertTrue(runtime.isNull(null));
            assertFalse(runtime.isNull("hello"));
            assertFalse(runtime.isNull(0));
        }

        @Test
        void isBoolean() {
            assertTrue(runtime.isBoolean(true));
            assertTrue(runtime.isBoolean(false));
            assertFalse(runtime.isBoolean("true"));
            assertFalse(runtime.isBoolean(1));
        }

        @Test
        void isNumber() {
            assertTrue(runtime.isNumber(42));
            assertTrue(runtime.isNumber(3.14));
            assertTrue(runtime.isNumber(100L));
            assertFalse(runtime.isNumber("42"));
        }

        @Test
        void isString() {
            assertTrue(runtime.isString("hello"));
            assertTrue(runtime.isString(""));
            assertFalse(runtime.isString(42));
        }

        @Test
        void isArray() {
            assertTrue(runtime.isArray(new ArrayList<Object>()));
            assertTrue(runtime.isArray(Arrays.asList(1, 2, 3)));
            assertFalse(runtime.isArray(new HashMap<String, Object>()));
        }

        @Test
        void isObject() {
            assertTrue(runtime.isObject(new HashMap<String, Object>()));
            assertFalse(runtime.isObject(new ArrayList<Object>()));
        }

        @Test
        void typeOf() {
            assertEquals(Type.NULL, runtime.typeOf(null));
            assertEquals(Type.BOOLEAN, runtime.typeOf(true));
            assertEquals(Type.NUMBER, runtime.typeOf(42));
            assertEquals(Type.STRING, runtime.typeOf("hello"));
            assertEquals(Type.ARRAY, runtime.typeOf(new ArrayList<Object>()));
            assertEquals(Type.OBJECT, runtime.typeOf(new HashMap<String, Object>()));
        }
    }

    @Nested
    class ValueExtraction {

        @Test
        void getProperty() {
            Map<String, Object> obj = new HashMap<String, Object>();
            obj.put("name", "Alice");
            obj.put("age", 30);

            assertEquals("Alice", runtime.getProperty(obj, "name"));
            assertEquals(30, runtime.getProperty(obj, "age"));
            assertNull(runtime.getProperty(obj, "missing"));
        }

        @Test
        void getPropertyFromNonObject() {
            assertNull(runtime.getProperty("not an object", "key"));
            assertNull(runtime.getProperty(null, "key"));
        }

        @Test
        void getIndex() {
            List<Object> arr = Arrays.asList("a", "b", "c");

            assertEquals("a", runtime.getIndex(arr, 0));
            assertEquals("b", runtime.getIndex(arr, 1));
            assertEquals("c", runtime.getIndex(arr, 2));
            assertNull(runtime.getIndex(arr, 3));
            assertNull(runtime.getIndex(arr, -1));
        }

        @Test
        void getArrayLength() {
            assertEquals(0, runtime.getArrayLength(new ArrayList<Object>()));
            assertEquals(3, runtime.getArrayLength(Arrays.asList(1, 2, 3)));
            assertEquals(0, runtime.getArrayLength("not an array"));
        }

        @Test
        void getArrayElements() {
            List<Object> arr = Arrays.asList(1, 2, 3);
            List<Object> result = new ArrayList<Object>();
            for (Object elem : runtime.getArrayElements(arr)) {
                result.add(elem);
            }
            assertEquals(Arrays.asList(1, 2, 3), result);
        }

        @Test
        void getObjectKeys() {
            Map<String, Object> obj = new LinkedHashMap<String, Object>();
            obj.put("a", 1);
            obj.put("b", 2);

            List<String> keys = new ArrayList<String>();
            for (String key : runtime.getObjectKeys(obj)) {
                keys.add(key);
            }
            assertEquals(Arrays.asList("a", "b"), keys);
        }

        @Test
        void getObjectValues() {
            Map<String, Object> obj = new LinkedHashMap<String, Object>();
            obj.put("a", 1);
            obj.put("b", 2);

            List<Object> values = new ArrayList<Object>();
            for (Object value : runtime.getObjectValues(obj)) {
                values.add(value);
            }
            assertEquals(Arrays.asList(1, 2), values);
        }
    }

    @Nested
    class Conversion {

        @Test
        void toBoolean() {
            assertTrue(runtime.toBoolean(true));
            assertFalse(runtime.toBoolean(false));
            assertFalse(runtime.toBoolean("true")); // not a boolean
        }

        @Test
        void toNumber() {
            assertEquals(42, runtime.toNumber(42));
            assertEquals(3.14, runtime.toNumber(3.14));
            assertNull(runtime.toNumber("42"));
        }

        @Test
        void toStringConversion() {
            assertEquals("hello", runtime.toString("hello"));
            assertEquals("42", runtime.toString(42));
            assertEquals("null", runtime.toString(null));
        }
    }

    @Nested
    class ValueConstruction {

        @Test
        void createNull() {
            assertNull(runtime.createNull());
        }

        @Test
        void createBoolean() {
            assertEquals(true, runtime.createBoolean(true));
            assertEquals(false, runtime.createBoolean(false));
        }

        @Test
        void createNumber() {
            assertEquals(42, runtime.createNumber(42));
            assertEquals(3.14, runtime.createNumber(3.14));
        }

        @Test
        void createString() {
            assertEquals("hello", runtime.createString("hello"));
        }

        @Test
        void createArray() {
            List<Object> input = Arrays.asList(1, 2, 3);
            Object result = runtime.createArray(input);
            assertTrue(result instanceof List);
            assertEquals(input, result);
            // Should be a copy
            assertNotSame(input, result);
        }

        @Test
        void createObject() {
            Map<String, Object> input = new HashMap<String, Object>();
            input.put("a", 1);
            Object result = runtime.createObject(input);
            assertTrue(result instanceof Map);
            assertEquals(input, result);
            // Should be a copy
            assertNotSame(input, result);
        }
    }

    @Nested
    class Comparison {

        @Test
        void compareNumbers() {
            assertTrue(runtime.compare(1, 2) < 0);
            assertTrue(runtime.compare(2, 1) > 0);
            assertEquals(0, runtime.compare(1, 1));
            assertEquals(0, runtime.compare(1, 1.0));
        }

        @Test
        void compareStrings() {
            assertTrue(runtime.compare("a", "b") < 0);
            assertTrue(runtime.compare("b", "a") > 0);
            assertEquals(0, runtime.compare("a", "a"));
        }

        @Test
        void compareNulls() {
            assertEquals(0, runtime.compare(null, null));
            assertTrue(runtime.compare(null, 1) < 0);
            assertTrue(runtime.compare(1, null) > 0);
        }

        @Test
        void deepEqualsSimple() {
            assertTrue(runtime.deepEquals(null, null));
            assertTrue(runtime.deepEquals(true, true));
            assertTrue(runtime.deepEquals(42, 42));
            assertTrue(runtime.deepEquals("hello", "hello"));

            assertFalse(runtime.deepEquals(null, 1));
            assertFalse(runtime.deepEquals(1, 2));
            assertFalse(runtime.deepEquals("a", "b"));
        }

        @Test
        void deepEqualsNumbers() {
            // Integer and Double should be equal if values match
            assertTrue(runtime.deepEquals(1, 1.0));
            assertTrue(runtime.deepEquals(42, 42L));
        }

        @Test
        void deepEqualsArrays() {
            List<Object> a = Arrays.asList(1, 2, 3);
            List<Object> b = Arrays.asList(1, 2, 3);
            List<Object> c = Arrays.asList(1, 2);

            assertTrue(runtime.deepEquals(a, b));
            assertFalse(runtime.deepEquals(a, c));
        }

        @Test
        void deepEqualsObjects() {
            Map<String, Object> a = new HashMap<String, Object>();
            a.put("x", 1);
            a.put("y", 2);

            Map<String, Object> b = new HashMap<String, Object>();
            b.put("x", 1);
            b.put("y", 2);

            Map<String, Object> c = new HashMap<String, Object>();
            c.put("x", 1);

            assertTrue(runtime.deepEquals(a, b));
            assertFalse(runtime.deepEquals(a, c));
        }

        @Test
        void deepEqualsNested() {
            Map<String, Object> a = new HashMap<String, Object>();
            a.put("arr", Arrays.asList(1, 2));

            Map<String, Object> b = new HashMap<String, Object>();
            b.put("arr", Arrays.asList(1, 2));

            Map<String, Object> c = new HashMap<String, Object>();
            c.put("arr", Arrays.asList(1, 3));

            assertTrue(runtime.deepEquals(a, b));
            assertFalse(runtime.deepEquals(a, c));
        }
    }

    @Nested
    class Truthiness {

        @Test
        void nullIsFalsy() {
            assertFalse(runtime.isTruthy(null));
        }

        @Test
        void booleans() {
            assertTrue(runtime.isTruthy(true));
            assertFalse(runtime.isTruthy(false));
        }

        @Test
        void strings() {
            assertTrue(runtime.isTruthy("hello"));
            assertFalse(runtime.isTruthy(""));
        }

        @Test
        void arrays() {
            assertTrue(runtime.isTruthy(Arrays.asList(1, 2)));
            assertFalse(runtime.isTruthy(new ArrayList<Object>()));
        }

        @Test
        void objects() {
            Map<String, Object> nonEmpty = new HashMap<String, Object>();
            nonEmpty.put("a", 1);

            assertTrue(runtime.isTruthy(nonEmpty));
            assertFalse(runtime.isTruthy(new HashMap<String, Object>()));
        }

        @Test
        void numbersAreTruthy() {
            assertTrue(runtime.isTruthy(0));
            assertTrue(runtime.isTruthy(1));
            assertTrue(runtime.isTruthy(-1));
            assertTrue(runtime.isTruthy(0.0));
        }
    }

    @Nested
    class JsonParsing {

        @Test
        void parseNull() {
            assertNull(runtime.parseJson("null"));
        }

        @Test
        void parseBoolean() {
            assertEquals(true, runtime.parseJson("true"));
            assertEquals(false, runtime.parseJson("false"));
        }

        @Test
        void parseNumber() {
            assertEquals(42, runtime.parseJson("42"));
            assertEquals(-17, runtime.parseJson("-17"));
            assertEquals(3.14, runtime.parseJson("3.14"));
            assertEquals(1e10, runtime.parseJson("1e10"));
        }

        @Test
        void parseString() {
            assertEquals("hello", runtime.parseJson("\"hello\""));
            assertEquals("hello world", runtime.parseJson("\"hello world\""));
            assertEquals("tab\ttab", runtime.parseJson("\"tab\\ttab\""));
            assertEquals("new\nline", runtime.parseJson("\"new\\nline\""));
        }

        @Test
        void parseArray() {
            assertEquals(Arrays.asList(1, 2, 3), runtime.parseJson("[1, 2, 3]"));
            assertEquals(new ArrayList<Object>(), runtime.parseJson("[]"));
        }

        @Test
        void parseObject() {
            Object result = runtime.parseJson("{\"a\": 1, \"b\": 2}");
            assertTrue(result instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) result;
            assertEquals(1, map.get("a"));
            assertEquals(2, map.get("b"));
        }

        @Test
        void parseNested() {
            Object result = runtime.parseJson("{\"arr\": [1, 2], \"obj\": {\"x\": true}}");
            assertTrue(result instanceof Map);
        }

        @Test
        void parseWithWhitespace() {
            assertEquals(Arrays.asList(1, 2), runtime.parseJson("  [ 1 , 2 ]  "));
        }
    }
}
