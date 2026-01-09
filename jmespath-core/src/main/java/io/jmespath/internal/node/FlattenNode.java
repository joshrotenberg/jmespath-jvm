package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a flatten operation ([]).
 *
 * <p>Flattens one level of nesting in an array. Elements that are arrays
 * have their contents merged into the result; non-array elements are
 * kept as-is.
 *
 * <p>Example: {@code [[1,2],[3,4]]} flattens to {@code [1,2,3,4]}
 */
public final class FlattenNode implements Node {

    private final Node expression;

    /**
     * Creates a flatten node.
     *
     * @param expression the expression to flatten (can be null for bare [])
     */
    public FlattenNode(Node expression) {
        this.expression = expression;
    }

    /**
     * Returns the expression being flattened.
     *
     * @return the expression, or null
     */
    public Node getExpression() {
        return expression;
    }

    @Override
    public boolean isProjection() {
        return true;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        T base = current;
        if (expression != null) {
            base = expression.evaluate(runtime, current, scope);
        }

        if (!runtime.isArray(base)) {
            return runtime.createNull();
        }

        // Estimate flattened size - assume some nesting
        int baseLen = runtime.getArrayLength(base);
        List<T> result = new ArrayList<T>(baseLen * 2);

        for (T element : runtime.getArrayElements(base)) {
            if (runtime.isArray(element)) {
                // Flatten one level
                for (T inner : runtime.getArrayElements(element)) {
                    result.add(inner);
                }
            } else {
                result.add(element);
            }
        }

        return runtime.createArray(result);
    }

    @Override
    public String toString() {
        if (expression == null) {
            return "[]";
        }
        return expression + "[]";
    }
}
