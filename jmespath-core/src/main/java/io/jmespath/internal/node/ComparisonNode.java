package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;

/**
 * Represents a comparison expression.
 *
 * <p>Compares two values using one of the comparison operators:
 * ==, !=, &lt;, &lt;=, &gt;, &gt;=
 *
 * <p>Ordering comparisons (&lt;, &lt;=, &gt;, &gt;=) only work on
 * numbers and strings of the same type.
 *
 * <p>Examples: {@code age > 18}, {@code name == "foo"}
 */
public final class ComparisonNode implements Node {

    /**
     * The comparison operator.
     */
    public enum Operator {
        EQ("=="),
        NE("!="),
        LT("<"),
        LE("<="),
        GT(">"),
        GE(">=");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    private final Operator operator;
    private final Node left;
    private final Node right;

    /**
     * Creates a comparison node.
     *
     * @param operator the comparison operator
     * @param left the left operand
     * @param right the right operand
     */
    public ComparisonNode(Operator operator, Node left, Node right) {
        if (operator == null || left == null || right == null) {
            throw new IllegalArgumentException(
                "operator, left, and right cannot be null"
            );
        }
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the comparison operator.
     *
     * @return the operator
     */
    public Operator getOperator() {
        return operator;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        T leftValue = left.evaluate(runtime, current, scope);
        T rightValue = right.evaluate(runtime, current, scope);

        // Fast path for equality - most common in filters
        if (operator == Operator.EQ) {
            return runtime.createBoolean(
                runtime.deepEquals(leftValue, rightValue)
            );
        }
        if (operator == Operator.NE) {
            return runtime.createBoolean(
                !runtime.deepEquals(leftValue, rightValue)
            );
        }

        // Ordering comparisons - check types once
        return evaluateOrdering(runtime, leftValue, rightValue);
    }

    private <T> T evaluateOrdering(
        Runtime<T> runtime,
        T leftValue,
        T rightValue
    ) {
        // Ordering comparisons only work on same types (number or string)
        // Check number first as it's more common in filters like "age > 18"
        if (runtime.isNumber(leftValue)) {
            if (!runtime.isNumber(rightValue)) {
                return runtime.createNull();
            }
            int cmp = runtime.compare(leftValue, rightValue);
            return runtime.createBoolean(checkOrdering(cmp));
        }
        if (runtime.isString(leftValue)) {
            if (!runtime.isString(rightValue)) {
                return runtime.createNull();
            }
            int cmp = runtime.compare(leftValue, rightValue);
            return runtime.createBoolean(checkOrdering(cmp));
        }
        // Not a comparable type
        return runtime.createNull();
    }

    private boolean checkOrdering(int cmp) {
        switch (operator) {
            case LT:
                return cmp < 0;
            case LE:
                return cmp <= 0;
            case GT:
                return cmp > 0;
            case GE:
                return cmp >= 0;
            default:
                return false;
        }
    }

    @Override
    public boolean isProjection() {
        return false;
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}
