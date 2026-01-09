package io.jmespath.internal.node;

import io.jmespath.Runtime;

/**
 * Represents bracket access with an expression, used for chaining.
 *
 * <p>Evaluates the left expression, then applies the bracket expression
 * to the result.
 *
 * <p>Example: {@code foo[0]} where left is "foo" and bracket is index 0.
 */
public final class IndexExpressionNode implements Node {

    private final Node left;
    private final Node bracket;

    /**
     * Creates an index expression node.
     *
     * @param left the left expression (can be null for bare bracket)
     * @param bracket the bracket expression
     */
    public IndexExpressionNode(Node left, Node bracket) {
        if (bracket == null) {
            throw new IllegalArgumentException("bracket cannot be null");
        }
        this.left = left;
        this.bracket = bracket;
    }

    /**
     * Returns the left expression.
     *
     * @return the left node, or null
     */
    public Node getLeft() {
        return left;
    }

    /**
     * Returns the bracket expression.
     *
     * @return the bracket node
     */
    public Node getBracket() {
        return bracket;
    }

    @Override
    public boolean isProjection() {
        return bracket.isProjection();
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        T base = current;
        if (left != null) {
            base = left.evaluate(runtime, current);
            if (runtime.isNull(base)) {
                return runtime.createNull();
            }
        }
        return bracket.evaluate(runtime, base);
    }

    @Override
    public String toString() {
        if (left == null) {
            return bracket.toString();
        }
        return left.toString() + bracket.toString();
    }
}
