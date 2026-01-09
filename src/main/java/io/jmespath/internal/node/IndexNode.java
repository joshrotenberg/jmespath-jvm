package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;

/**
 * Represents array index access.
 *
 * <p>Extracts an element from an array by index. Supports negative
 * indices which count from the end (-1 is last element).
 *
 * <p>Example: {@code [0]} gets the first element, {@code [-1]} gets the last.
 */
public final class IndexNode implements Node {

    private final int index;

    /**
     * Creates an index node.
     *
     * @param index the array index (can be negative)
     */
    public IndexNode(int index) {
        this.index = index;
    }

    /**
     * Returns the index value.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        if (!runtime.isArray(current)) {
            return runtime.createNull();
        }
        int length = runtime.getArrayLength(current);
        int actualIndex = index;
        if (actualIndex < 0) {
            actualIndex = length + actualIndex;
        }
        if (actualIndex < 0 || actualIndex >= length) {
            return runtime.createNull();
        }
        return runtime.getIndex(current, actualIndex);
    }

    @Override
    public String toString() {
        return "[" + index + "]";
    }
}
