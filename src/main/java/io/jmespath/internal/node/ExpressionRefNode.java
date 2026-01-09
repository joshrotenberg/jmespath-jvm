package io.jmespath.internal.node;

import io.jmespath.Runtime;

/**
 * Represents an expression reference (&amp;expression).
 *
 * <p>Expression references are used to pass expressions to functions
 * like sort_by, max_by, etc. The expression is not evaluated immediately;
 * instead, it's passed as a callable to the function.
 *
 * <p>Example: {@code sort_by(people, &age)} sorts by the age property.
 */
public final class ExpressionRefNode implements Node {

    private final Node expression;

    /**
     * Creates an expression reference node.
     *
     * @param expression the referenced expression
     */
    public ExpressionRefNode(Node expression) {
        if (expression == null) {
            throw new IllegalArgumentException("expression cannot be null");
        }
        this.expression = expression;
    }

    /**
     * Returns the referenced expression node.
     *
     * @return the expression
     */
    public Node getExpressionNode() {
        return expression;
    }

    /**
     * Evaluates the referenced expression against a value.
     *
     * <p>This method is used by functions that accept expression references.
     *
     * @param <T> the JSON value type
     * @param runtime the runtime
     * @param value the value to evaluate against
     * @return the result
     */
    public <T> T evaluateRef(Runtime<T> runtime, T value) {
        return expression.evaluate(runtime, value);
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        // Expression references don't directly evaluate to a JSON value.
        // They're handled specially by function call nodes.
        // Returning null here - the actual behavior depends on usage context.
        return runtime.createNull();
    }

    @Override
    public String toString() {
        return "&" + expression;
    }
}
