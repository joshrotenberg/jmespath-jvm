package io.jmespath.internal.node;

import io.jmespath.Runtime;

/**
 * Represents a logical NOT expression.
 *
 * <p>Returns true if the expression is falsy, false if truthy.
 *
 * <p>Example: {@code !foo} returns true if foo is null, false, or empty.
 */
public final class NotNode implements Node {

    private final Node expression;

    /**
     * Creates a NOT node.
     *
     * @param expression the expression to negate
     */
    public NotNode(Node expression) {
        if (expression == null) {
            throw new IllegalArgumentException("expression cannot be null");
        }
        this.expression = expression;
    }

    /**
     * Returns the negated expression.
     *
     * @return the expression
     */
    public Node getExpression() {
        return expression;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        T result = expression.evaluate(runtime, current);
        return runtime.createBoolean(!runtime.isTruthy(result));
    }

    @Override
    public String toString() {
        return "!" + expression;
    }
}
