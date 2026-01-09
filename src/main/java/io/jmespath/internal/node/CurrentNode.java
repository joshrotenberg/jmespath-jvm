package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;

/**
 * Represents the current node reference (@).
 *
 * <p>Returns the current value being evaluated, unchanged.
 *
 * <p>Example: {@code @} returns the entire current document.
 */
public final class CurrentNode implements Node {

    /** Singleton instance since this node has no state. */
    public static final CurrentNode INSTANCE = new CurrentNode();

    private CurrentNode() {}

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        return current;
    }

    @Override
    public boolean isProjection() {
        return false;
    }

    @Override
    public String toString() {
        return "@";
    }
}
