package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;

/**
 * Represents a variable reference ($name) in a JEP-18 lexical scope.
 *
 * <p>Variable references look up a bound value in the current scope.
 * If the variable is not found, returns null.
 *
 * <p>Example: {@code $foo} looks up the variable "foo" in scope.
 */
public final class VariableRefNode implements Node {

    private final String name;

    /**
     * Creates a variable reference node.
     *
     * @param name the variable name (without $ prefix)
     */
    public VariableRefNode(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        this.name = name;
    }

    /**
     * Returns the variable name (without $ prefix).
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        T value = scope.get(name);
        if (value == null) {
            return runtime.createNull();
        }
        return value;
    }

    @Override
    public boolean isProjection() {
        return false;
    }

    @Override
    public String toString() {
        return "$" + name;
    }
}
