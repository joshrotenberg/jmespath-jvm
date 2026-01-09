package io.jmespath.internal;

import io.jmespath.Expression;
import io.jmespath.Runtime;
import io.jmespath.internal.node.Node;

/**
 * Implementation of Expression that wraps a parsed AST.
 *
 * <p>Instances are immutable and thread-safe.
 *
 * @param <T> the JSON value type
 */
public final class CompiledExpression<T> implements Expression<T> {

    private final String expressionString;
    private final Node root;

    /**
     * Creates a compiled expression.
     *
     * @param expressionString the original expression string
     * @param root the root AST node
     */
    public CompiledExpression(String expressionString, Node root) {
        if (expressionString == null) {
            throw new IllegalArgumentException(
                "expressionString cannot be null"
            );
        }
        if (root == null) {
            throw new IllegalArgumentException("root cannot be null");
        }
        this.expressionString = expressionString;
        this.root = root;
    }

    @Override
    public T evaluate(Runtime<T> runtime, T data) {
        if (runtime == null) {
            throw new IllegalArgumentException("runtime cannot be null");
        }
        return root.evaluate(runtime, data);
    }

    @Override
    public String getExpression() {
        return expressionString;
    }

    /**
     * Returns the root AST node.
     *
     * <p>This is primarily for debugging and testing.
     *
     * @return the root node
     */
    public Node getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return "Expression[" + expressionString + "]";
    }
}
