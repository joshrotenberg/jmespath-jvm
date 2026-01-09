package io.jmespath.internal.node;

import io.jmespath.Runtime;

/**
 * Represents a logical OR expression.
 *
 * <p>Returns the left value if it's truthy, otherwise returns the right value.
 * This is similar to the "elvis operator" or null coalescing.
 *
 * <p>Example: {@code foo || bar} returns foo if truthy, else bar.
 */
public final class OrNode implements Node {

    private final Node left;
    private final Node right;

    /**
     * Creates an OR node.
     *
     * @param left the left expression
     * @param right the right expression
     */
    public OrNode(Node left, Node right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("left and right cannot be null");
        }
        this.left = left;
        this.right = right;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        T leftResult = left.evaluate(runtime, current);
        if (runtime.isTruthy(leftResult)) {
            return leftResult;
        }
        return right.evaluate(runtime, current);
    }

    @Override
    public String toString() {
        return "(" + left + " || " + right + ")";
    }
}
