package io.jmespath.internal.node;

import io.jmespath.Runtime;

/**
 * Represents a raw string literal.
 *
 * <p>Raw strings are single-quoted and don't support escape sequences
 * except for '' to represent a single quote.
 *
 * <p>Example: {@code 'hello world'}, {@code 'it''s a test'}
 */
public final class RawStringNode implements Node {

    private final String value;

    /**
     * Creates a raw string node.
     *
     * @param value the string value (already unescaped)
     */
    public RawStringNode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        this.value = value;
    }

    /**
     * Returns the string value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        return runtime.createString(value);
    }

    @Override
    public String toString() {
        return "'" + value.replace("'", "''") + "'";
    }
}
