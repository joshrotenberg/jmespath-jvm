package io.jmespath;

import io.jmespath.function.FunctionRegistry;
import java.util.List;
import java.util.Map;

/**
 * Adapter interface for JSON operations.
 *
 * <p>Implement this interface to use JMESPath with your JSON library
 * (Jackson, Gson, etc.) or with plain Java objects (Map/List).
 *
 * <p>The runtime provides all the operations needed to evaluate JMESPath
 * expressions: type checking, value extraction, value construction,
 * and comparison.
 *
 * <p>Example implementation for Jackson:
 * <pre>{@code
 * public class JacksonRuntime implements Runtime<JsonNode> {
 *     public boolean isNull(JsonNode value) {
 *         return value == null || value.isNull();
 *     }
 *     // ... other methods
 * }
 * }</pre>
 *
 * @param <T> the JSON value type used by the underlying library
 */
public interface Runtime<T> {
    // === Type Checking ===

    /**
     * Returns true if the value is null/nil.
     *
     * @param value the value to check
     * @return true if null
     */
    boolean isNull(T value);

    /**
     * Returns true if the value is a boolean.
     *
     * @param value the value to check
     * @return true if boolean
     */
    boolean isBoolean(T value);

    /**
     * Returns true if the value is a number.
     *
     * @param value the value to check
     * @return true if number
     */
    boolean isNumber(T value);

    /**
     * Returns true if the value is a string.
     *
     * @param value the value to check
     * @return true if string
     */
    boolean isString(T value);

    /**
     * Returns true if the value is an array.
     *
     * @param value the value to check
     * @return true if array
     */
    boolean isArray(T value);

    /**
     * Returns true if the value is an object.
     *
     * @param value the value to check
     * @return true if object
     */
    boolean isObject(T value);

    /**
     * Returns the JMESPath type of the value.
     *
     * @param value the value
     * @return the type
     */
    Type typeOf(T value);

    // === Value Extraction ===

    /**
     * Gets a property from an object.
     *
     * @param object the object
     * @param name the property name
     * @return the property value, or null if not found
     */
    T getProperty(T object, String name);

    /**
     * Gets an element from an array by index.
     *
     * <p>The index is guaranteed to be non-negative and within bounds.
     *
     * @param array the array
     * @param index the index
     * @return the element
     */
    T getIndex(T array, int index);

    /**
     * Returns the length of an array.
     *
     * @param array the array
     * @return the length
     */
    int getArrayLength(T array);

    /**
     * Returns an iterable over array elements.
     *
     * @param array the array
     * @return iterable of elements
     */
    Iterable<T> getArrayElements(T array);

    /**
     * Returns an iterable over object keys.
     *
     * @param object the object
     * @return iterable of keys
     */
    Iterable<String> getObjectKeys(T object);

    /**
     * Returns an iterable over object values.
     *
     * @param object the object
     * @return iterable of values
     */
    Iterable<T> getObjectValues(T object);

    // === Conversion to Java Types ===

    /**
     * Converts a boolean value to Java boolean.
     *
     * @param value the boolean value
     * @return the Java boolean
     */
    boolean toBoolean(T value);

    /**
     * Converts a number value to Java Number.
     *
     * @param value the number value
     * @return the Java Number
     */
    Number toNumber(T value);

    /**
     * Converts a string value to Java String.
     *
     * @param value the string value
     * @return the Java String
     */
    String toString(T value);

    // === Value Construction ===

    /**
     * Creates a null value.
     *
     * @return the null value
     */
    T createNull();

    /**
     * Creates a boolean value.
     *
     * @param value the Java boolean
     * @return the boolean value
     */
    T createBoolean(boolean value);

    /**
     * Creates a number value.
     *
     * @param value the Java Number
     * @return the number value
     */
    T createNumber(Number value);

    /**
     * Creates a string value.
     *
     * @param value the Java String
     * @return the string value
     */
    T createString(String value);

    /**
     * Creates an array value from a list.
     *
     * @param elements the elements
     * @return the array value
     */
    T createArray(List<T> elements);

    /**
     * Creates an object value from a map.
     *
     * @param properties the properties
     * @return the object value
     */
    T createObject(Map<String, T> properties);

    // === Comparison ===

    /**
     * Compares two values for ordering.
     *
     * <p>Used for comparison operators and sorting.
     * Returns negative if a &lt; b, zero if equal, positive if a &gt; b.
     *
     * @param a first value
     * @param b second value
     * @return comparison result
     */
    int compare(T a, T b);

    /**
     * Tests deep equality of two values.
     *
     * @param a first value
     * @param b second value
     * @return true if deeply equal
     */
    boolean deepEquals(T a, T b);

    /**
     * Tests JMESPath truthiness.
     *
     * <p>A value is truthy unless it is:
     * <ul>
     *   <li>null</li>
     *   <li>false</li>
     *   <li>empty string ""</li>
     *   <li>empty array []</li>
     *   <li>empty object {}</li>
     * </ul>
     *
     * @param value the value to test
     * @return true if truthy
     */
    boolean isTruthy(T value);

    // === JSON Parsing ===

    /**
     * Parses a JSON string into a value.
     *
     * <p>Used for literal expressions.
     *
     * @param json the JSON string
     * @return the parsed value
     */
    T parseJson(String json);

    // === Function Registry ===

    /**
     * Returns the function registry.
     *
     * <p>Used by function call nodes to invoke functions.
     *
     * @return the function registry
     */
    FunctionRegistry getFunctionRegistry();

    // === Configuration ===

    /**
     * Returns whether type errors should be silent (return null) or throw exceptions.
     *
     * <p>When true, operations that would normally throw a type error
     * (e.g., sort_by with mixed types) will return null instead.
     *
     * <p>Default implementations should return false.
     *
     * @return true if type errors should return null instead of throwing
     */
    default boolean isSilentTypeErrors() {
        return false;
    }
}
