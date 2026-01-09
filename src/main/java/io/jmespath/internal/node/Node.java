package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;

/**
 * Base interface for all AST nodes in a JMESPath expression.
 *
 * <p>Each node represents a syntactic element of the JMESPath grammar.
 * Nodes are immutable after construction and can be safely shared
 * across threads.
 *
 * <p>Evaluation is performed by calling {@link #evaluate(Runtime, Object, Scope)}
 * with a runtime adapter, the current JSON value, and a variable scope.
 */
public interface Node {
    /**
     * Evaluates this node against the given value with variable scope.
     *
     * @param <T> the JSON value type used by the runtime
     * @param runtime the runtime adapter for JSON operations
     * @param current the current JSON value being evaluated
     * @param scope the current variable scope for lexical bindings
     * @return the result of evaluating this node
     */
    <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope);

    /**
     * Returns true if this node creates a projection.
     *
     * @return true if this is a projection node
     */
    boolean isProjection();
}
