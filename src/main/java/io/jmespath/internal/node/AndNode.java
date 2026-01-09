package io.jmespath.internal.node;

import io.jmespath.Runtime;

/**
 * Represents a logical AND expression.
 *
 * <p>If the left value is falsy, returns the left value.
 * Otherwise, returns the right value.
 *
 * <p>Example: {@code foo && bar} returns foo if falsy, else evaluates and returns bar.
 */
public final class AndNode implements Node {

    private final Node left;
    private final Node right;

    /**
     * Creates an AND node.
     *
     * @param left the left expression
     * @param right the right expression
     */
    public AndNode(Node left, Node right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("left and right cannot be null");
        }
        this.left = left;
        this.right = right;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        T leftResult = left.evaluate(runtime, current);
        if (!runtime.isTruthy(leftResult)) {
            return leftResult;
        }
        return right.evaluate(runtime, current);
    }

    @Override
    public String toString() {
        return "(" + left + " && " + right + ")";
    }
}
