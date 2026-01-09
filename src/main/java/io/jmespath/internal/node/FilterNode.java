package io.jmespath.internal.node;

import io.jmespath.Runtime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a filter expression ([?...]).
 *
 * <p>Filters an array by evaluating a condition against each element
 * and keeping only elements where the condition is truthy.
 *
 * <p>Example: {@code [?age > 18]} keeps only elements where age > 18.
 */
public final class FilterNode implements Node {

    private final Node left;
    private final Node condition;
    private final Node right;

    /**
     * Creates a filter node.
     *
     * @param left the expression producing the array to filter (can be null)
     * @param condition the filter condition
     * @param right the expression to apply after filtering (can be null)
     */
    public FilterNode(Node left, Node condition, Node right) {
        if (condition == null) {
            throw new IllegalArgumentException("condition cannot be null");
        }
        this.left = left;
        this.condition = condition;
        this.right = right;
    }

    /**
     * Returns the left expression (the array to filter).
     *
     * @return the left node, or null
     */
    public Node getLeft() {
        return left;
    }

    /**
     * Returns the filter condition.
     *
     * @return the condition node
     */
    public Node getCondition() {
        return condition;
    }

    /**
     * Returns the right expression (projection after filter).
     *
     * @return the right node, or null
     */
    public Node getRight() {
        return right;
    }

    @Override
    public boolean isProjection() {
        return true;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        T base = current;
        if (left != null) {
            base = left.evaluate(runtime, current);
        }

        if (!runtime.isArray(base)) {
            return runtime.createNull();
        }

        List<T> filtered = new ArrayList<T>();
        for (T element : runtime.getArrayElements(base)) {
            T conditionResult = condition.evaluate(runtime, element);
            if (runtime.isTruthy(conditionResult)) {
                if (right != null) {
                    T projected = right.evaluate(runtime, element);
                    if (!runtime.isNull(projected)) {
                        filtered.add(projected);
                    }
                } else {
                    filtered.add(element);
                }
            }
        }

        return runtime.createArray(filtered);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (left != null) {
            sb.append(left);
        }
        sb.append("[?").append(condition).append("]");
        if (right != null) {
            sb.append(".").append(right);
        }
        return sb.toString();
    }
}
