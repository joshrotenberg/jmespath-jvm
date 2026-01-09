package io.jmespath;

/**
 * The six JMESPath types.
 *
 * <p>JMESPath defines six types that all values must belong to:
 * <ul>
 *   <li>{@link #NULL} - The null/nil value</li>
 *   <li>{@link #BOOLEAN} - true or false</li>
 *   <li>{@link #NUMBER} - Numeric values (integer or floating point)</li>
 *   <li>{@link #STRING} - Text strings</li>
 *   <li>{@link #ARRAY} - Ordered sequences of values</li>
 *   <li>{@link #OBJECT} - Unordered collections of key-value pairs</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * Type t = runtime.typeOf(value);
 * if (t == Type.ARRAY) {
 *     // iterate elements
 * }
 * }</pre>
 *
 * @see <a href="https://jmespath.org/specification.html#types">JMESPath Types</a>
 */
public enum Type {

    /**
     * The null type. Represents the absence of a value.
     */
    NULL("null"),

    /**
     * Boolean type. Either true or false.
     */
    BOOLEAN("boolean"),

    /**
     * Numeric type. Can be integer or floating point.
     */
    NUMBER("number"),

    /**
     * String type. A sequence of characters.
     */
    STRING("string"),

    /**
     * Array type. An ordered sequence of values.
     */
    ARRAY("array"),

    /**
     * Object type. An unordered collection of key-value pairs.
     */
    OBJECT("object");

    private final String name;

    Type(String name) {
        this.name = name;
    }

    /**
     * Returns the JMESPath type name as used in expressions and the type() function.
     *
     * @return the type name (e.g., "string", "number", "array")
     */
    public String typeName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
