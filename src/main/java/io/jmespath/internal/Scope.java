package io.jmespath.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a lexical scope for variable bindings (JEP-18).
 *
 * <p>Scopes are immutable and form a chain - each scope has an optional
 * parent scope. Variable lookup proceeds from the current scope up
 * through parent scopes until a binding is found.
 *
 * <p>Example:
 * <pre>{@code
 * Scope<Object> outer = Scope.empty();
 * outer = outer.bind("x", 10);
 *
 * Scope<Object> inner = outer.child();
 * inner = inner.bind("y", 20);
 *
 * inner.get("y"); // returns 20
 * inner.get("x"); // returns 10 (from parent)
 * inner.get("z"); // returns null
 * }</pre>
 *
 * @param <T> the JSON value type
 */
public final class Scope<T> {

    private static final Scope<?> EMPTY = new Scope<>(null, new HashMap<String, Object>());

    private final Scope<T> parent;
    private final Map<String, T> bindings;

    private Scope(Scope<T> parent, Map<String, T> bindings) {
        this.parent = parent;
        this.bindings = bindings;
    }

    /**
     * Returns an empty scope with no bindings.
     *
     * @param <T> the JSON value type
     * @return an empty scope
     */
    @SuppressWarnings("unchecked")
    public static <T> Scope<T> empty() {
        return (Scope<T>) EMPTY;
    }

    /**
     * Creates a new scope with the given variable bound.
     *
     * <p>This does not modify the current scope - it returns a new scope
     * with the additional binding.
     *
     * @param name the variable name (without $ prefix)
     * @param value the value to bind
     * @return a new scope with the binding
     */
    public Scope<T> bind(String name, T value) {
        Map<String, T> newBindings = new HashMap<>(bindings);
        newBindings.put(name, value);
        return new Scope<>(parent, newBindings);
    }

    /**
     * Creates a child scope with this scope as parent.
     *
     * <p>The child scope starts with no bindings but can look up
     * variables in this (parent) scope.
     *
     * @return a new child scope
     */
    public Scope<T> child() {
        return new Scope<>(this, new HashMap<String, T>());
    }

    /**
     * Looks up a variable in this scope and parent scopes.
     *
     * @param name the variable name (without $ prefix)
     * @return the bound value, or null if not found
     */
    public T get(String name) {
        if (bindings.containsKey(name)) {
            return bindings.get(name);
        }
        if (parent != null) {
            return parent.get(name);
        }
        return null;
    }

    /**
     * Checks if a variable is bound in this scope or parent scopes.
     *
     * @param name the variable name (without $ prefix)
     * @return true if the variable is bound
     */
    public boolean contains(String name) {
        if (bindings.containsKey(name)) {
            return true;
        }
        if (parent != null) {
            return parent.contains(name);
        }
        return false;
    }

    /**
     * Returns the parent scope, or null if this is a root scope.
     *
     * @return the parent scope
     */
    public Scope<T> getParent() {
        return parent;
    }

    /**
     * Returns true if this scope has no bindings and no parent.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return bindings.isEmpty() && parent == null;
    }
}
