package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object/hash wildcard projection (*).
 *
 * <p>When applied to an object, extracts all values and applies
 * the right expression to each value.
 *
 * <p>Example: {@code foo.*} gets all values from the "foo" object.
 */
public final class ObjectProjectionNode implements Node {

    private final Node left;
    private final Node right;

    /**
     * Creates an object projection node.
     *
     * @param left the expression producing the object (can be null)
     * @param right the expression to apply to each value (can be null)
     */
    public ObjectProjectionNode(Node left, Node right) {
        this.left = left;
        this.right = right;
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
     * Returns the right (projected) expression.
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
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        T base = current;
        if (left != null) {
            base = left.evaluate(runtime, current, scope);
        }

        if (!runtime.isObject(base)) {
            return runtime.createNull();
        }

        List<T> results = new ArrayList<T>();
        for (T value : runtime.getObjectValues(base)) {
            T result;
            if (right != null) {
                result = right.evaluate(runtime, value, scope);
            } else {
                result = value;
            }
            if (!runtime.isNull(result)) {
                results.add(result);
            }
        }

        return runtime.createArray(results);
    }

    @Override
    public String toString() {
        if (left == null && right == null) {
            return "*";
        }
        if (left == null) {
            return "*." + right;
        }
        if (right == null) {
            return left + ".*";
        }
        return left + ".*." + right;
    }
}
