package io.jmespath.internal.node;

import io.jmespath.Runtime;

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
            throw new IllegalArgumentException("operator, left, and right cannot be null");
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
    public <T> T evaluate(Runtime<T> runtime, T current) {
        T leftValue = left.evaluate(runtime, current);
        T rightValue = right.evaluate(runtime, current);

        switch (operator) {
            case EQ:
                return runtime.createBoolean(runtime.deepEquals(leftValue, rightValue));
            case NE:
                return runtime.createBoolean(!runtime.deepEquals(leftValue, rightValue));
            case LT:
            case LE:
            case GT:
            case GE:
                return evaluateOrdering(runtime, leftValue, rightValue);
            default:
                return runtime.createNull();
        }
    }

    private <T> T evaluateOrdering(Runtime<T> runtime, T leftValue, T rightValue) {
        // Ordering comparisons only work on same types (number or string)
        if (runtime.isNumber(leftValue) && runtime.isNumber(rightValue)) {
            int cmp = runtime.compare(leftValue, rightValue);
            return runtime.createBoolean(checkOrdering(cmp));
        }
        if (runtime.isString(leftValue) && runtime.isString(rightValue)) {
            int cmp = runtime.compare(leftValue, rightValue);
            return runtime.createBoolean(checkOrdering(cmp));
        }
        // Type mismatch - return null
        return runtime.createNull();
    }

    private boolean checkOrdering(int cmp) {
        switch (operator) {
            case LT: return cmp < 0;
            case LE: return cmp <= 0;
            case GT: return cmp > 0;
            case GE: return cmp >= 0;
            default: return false;
        }
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}
