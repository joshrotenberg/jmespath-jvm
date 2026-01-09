package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a multi-select list expression.
 *
 * <p>Evaluates multiple expressions and collects the results into an array.
 *
 * <p>Example: {@code [foo, bar, baz]} produces an array of three values.
 */
public final class MultiSelectListNode implements Node {

    private final List<Node> elements;

    /**
     * Creates a multi-select list node.
     *
     * @param elements the expressions to evaluate
     */
    public MultiSelectListNode(List<Node> elements) {
        if (elements == null || elements.isEmpty()) {
            throw new IllegalArgumentException(
                "elements cannot be null or empty"
            );
        }
        this.elements = new ArrayList<Node>(elements);
    }

    /**
     * Returns the list of expressions.
     *
     * @return the expressions
     */
    public List<Node> getElements() {
        return elements;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        int size = elements.size();
        List<T> results = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            results.add(elements.get(i).evaluate(runtime, current, scope));
        }
        return runtime.createArray(results);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(elements.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
