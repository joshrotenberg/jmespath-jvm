package io.jmespath.internal.node;

import io.jmespath.Runtime;

/**
 * Represents a property access by identifier name.
 *
 * <p>Extracts a named property from an object. If the current value
 * is not an object or the property doesn't exist, returns null.
 *
 * <p>Example: {@code foo} extracts the "foo" property.
 */
public final class IdentifierNode implements Node {

    private final String name;
    private final boolean quoted;

    /**
     * Creates an identifier node.
     *
     * @param name the property name
     */
    public IdentifierNode(String name) {
        this(name, false);
    }

    /**
     * Creates an identifier node.
     *
     * @param name the property name
     * @param quoted whether the identifier was quoted
     */
    public IdentifierNode(String name, boolean quoted) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;
        this.quoted = quoted;
    }

    /**
     * Returns the identifier name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this identifier was quoted.
     *
     * @return true if quoted
     */
    public boolean isQuoted() {
        return quoted;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        if (!runtime.isObject(current)) {
            return runtime.createNull();
        }
        return runtime.getProperty(current, name);
    }

    @Override
    public String toString() {
        return name;
    }
}
