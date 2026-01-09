package io.jmespath.internal.node;

import io.jmespath.Runtime;

/**
 * Base interface for all AST nodes in a JMESPath expression.
 *
 * <p>Each node represents a syntactic element of the JMESPath grammar.
 * Nodes are immutable after construction and can be safely shared
 * across threads.
 *
 * <p>Evaluation is performed by calling {@link #evaluate(Runtime, Object)}
 * with a runtime adapter and the current JSON value.
 */
public interface Node {

    /**
     * Evaluates this node against the given value.
     *
     * @param <T> the JSON value type used by the runtime
     * @param runtime the runtime adapter for JSON operations
     * @param current the current JSON value being evaluated
     * @return the result of evaluating this node
     */
    <T> T evaluate(Runtime<T> runtime, T current);

    /**
     * Returns true if this node creates a projection.
     *
     * <p>Projections cause subsequent expressions to be applied
     * to each element of an array result, rather than to the
     * array as a whole.
     *
     * @return true if this is a projection node
     */
    default boolean isProjection() {
        return false;
    }

    /**
     * Accepts a visitor for tree traversal.
     *
     * @param visitor the visitor
     */
    default void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
