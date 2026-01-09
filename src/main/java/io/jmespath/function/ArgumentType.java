package io.jmespath.function;

/**
 * Types for function argument validation.
 *
 * <p>These types are used in function signatures to specify
 * what types of values each argument accepts.
 */
public enum ArgumentType {

    /**
     * Any JSON value (no type restriction).
     */
    ANY,

    /**
     * A string value.
     */
    STRING,

    /**
     * A number value.
     */
    NUMBER,

    /**
     * A boolean value.
     */
    BOOLEAN,

    /**
     * An array value.
     */
    ARRAY,

    /**
     * An object value.
     */
    OBJECT,

    /**
     * Null value.
     */
    NULL,

    /**
     * An array of numbers.
     */
    ARRAY_NUMBER,

    /**
     * An array of strings.
     */
    ARRAY_STRING,

    /**
     * An expression reference (&amp;expr).
     */
    EXPRESSION_REF;

    /**
     * Returns a human-readable description.
     *
     * @return the description
     */
    public String description() {
        switch (this) {
            case ANY: return "any";
            case STRING: return "string";
            case NUMBER: return "number";
            case BOOLEAN: return "boolean";
            case ARRAY: return "array";
            case OBJECT: return "object";
            case NULL: return "null";
            case ARRAY_NUMBER: return "array of numbers";
            case ARRAY_STRING: return "array of strings";
            case EXPRESSION_REF: return "expression";
            default: return name().toLowerCase();
        }
    }
}
