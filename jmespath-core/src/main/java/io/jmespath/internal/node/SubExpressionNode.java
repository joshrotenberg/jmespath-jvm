package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;

/**
 * Represents a sub-expression (dot notation).
 *
 * <p>Evaluates the left expression, then evaluates the right expression
 * against the result.
 *
 * <p>Example: {@code foo.bar} evaluates "foo", then "bar" on the result.
 */
public final class SubExpressionNode implements Node {

    private final Node left;
    private final Node right;

    /**
     * Creates a sub-expression node.
     *
     * @param left the left expression
     * @param right the right expression
     */
    public SubExpressionNode(Node left, Node right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("left and right cannot be null");
        }
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the left expression.
     *
     * @return the left node
     */
    public Node getLeft() {
        return left;
    }

    /**
     * Returns the right expression.
     *
     * @return the right node
     */
    public Node getRight() {
        return right;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        T leftResult = left.evaluate(runtime, current, scope);
        if (runtime.isNull(leftResult)) {
            return runtime.createNull();
        }
        return right.evaluate(runtime, leftResult, scope);
    }

    @Override
    public boolean isProjection() {
        return false;
    }

    @Override
    public String toString() {
        return left + "." + right;
    }
}
