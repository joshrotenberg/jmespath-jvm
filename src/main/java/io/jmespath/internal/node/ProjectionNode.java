package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a projection operation.
 *
 * <p>Projections evaluate an expression against each element of an array
 * and collect the non-null results into a new array.
 *
 * <p>This is the base class for wildcard projections ([*]) and is used
 * to implement projection chaining.
 */
public final class ProjectionNode implements Node {

    private final Node left;
    private final Node right;

    /**
     * Creates a projection node.
     *
     * @param left the expression that produces the array (or null for simple projection)
     * @param right the expression to apply to each element
     */
    public ProjectionNode(Node left, Node right) {
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
     * @return the right node
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

        if (!runtime.isArray(base)) {
            return runtime.createNull();
        }

        // Pre-size for expected output
        int len = runtime.getArrayLength(base);
        List<T> results = new ArrayList<T>(len > 0 ? len : 1);

        // Split paths to avoid null checks in hot loop
        if (right != null) {
            for (T element : runtime.getArrayElements(base)) {
                T result = right.evaluate(runtime, element, scope);
                if (result != null) {
                    results.add(result);
                }
            }
        } else {
            // No right side - just filter nulls from elements
            for (T element : runtime.getArrayElements(base)) {
                if (element != null) {
                    results.add(element);
                }
            }
        }

        return runtime.createArray(results);
    }

    @Override
    public String toString() {
        if (left == null && right == null) {
            return "[*]";
        }
        if (left == null) {
            return "[*]." + right;
        }
        if (right == null) {
            return left + "[*]";
        }
        return left + "[*]." + right;
    }
}
