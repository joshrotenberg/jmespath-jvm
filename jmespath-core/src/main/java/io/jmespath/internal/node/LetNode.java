package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a let expression for lexical scoping (JEP-18).
 *
 * <p>Let expressions bind variables to values and evaluate an expression
 * in the extended scope.
 *
 * <p>Syntax: {@code let $var1 = expr1, $var2 = expr2 in body}
 *
 * <p>Example: {@code let $x = foo.bar in items[?name == $x]}
 */
public final class LetNode implements Node {

    /**
     * A variable binding in a let expression.
     */
    public static final class Binding {

        private final String name;
        private final Node value;

        /**
         * Creates a binding.
         *
         * @param name the variable name (without $ prefix)
         * @param value the expression for the value
         */
        public Binding(String name, Node value) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException(
                    "name cannot be null or empty"
                );
            }
            if (value == null) {
                throw new IllegalArgumentException("value cannot be null");
            }
            this.name = name;
            this.value = value;
        }

        /**
         * Returns the variable name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the value expression.
         *
         * @return the value node
         */
        public Node getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "$" + name + " = " + value;
        }
    }

    private final List<Binding> bindings;
    private final Node body;

    /**
     * Creates a let node.
     *
     * @param bindings the variable bindings
     * @param body the expression to evaluate with bindings
     */
    public LetNode(List<Binding> bindings, Node body) {
        if (bindings == null || bindings.isEmpty()) {
            throw new IllegalArgumentException(
                "bindings cannot be null or empty"
            );
        }
        if (body == null) {
            throw new IllegalArgumentException("body cannot be null");
        }
        this.bindings = new ArrayList<Binding>(bindings);
        this.body = body;
    }

    /**
     * Returns the variable bindings.
     *
     * @return the bindings
     */
    public List<Binding> getBindings() {
        return bindings;
    }

    /**
     * Returns the body expression.
     *
     * @return the body node
     */
    public Node getBody() {
        return body;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        // Create a new scope with all bindings
        // Each binding is evaluated in the scope that includes previous bindings
        Scope<T> newScope = scope;
        for (Binding binding : bindings) {
            T value = binding.getValue().evaluate(runtime, current, newScope);
            newScope = newScope.bind(binding.getName(), value);
        }

        // Evaluate body in the extended scope
        return body.evaluate(runtime, current, newScope);
    }

    @Override
    public boolean isProjection() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("let ");
        for (int i = 0; i < bindings.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(bindings.get(i));
        }
        sb.append(" in ").append(body);
        return sb.toString();
    }
}
