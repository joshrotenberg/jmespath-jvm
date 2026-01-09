package io.jmespath.function.builtin;

import io.jmespath.JmesPathException;
import io.jmespath.Runtime;
import io.jmespath.Type;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;
import io.jmespath.internal.node.ExpressionRefNode;
import java.util.List;

/**
 * Implements the max_by() function.
 * Returns the element with the maximum value from applying an expression.
 * All extracted values must be of the same type (either all strings or all numbers).
 */
public final class MaxByFunction implements Function {

    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ARRAY)
        .required(ArgumentType.EXPRESSION_REF)
        .build();

    @Override
    public String getName() {
        return "max_by";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T array = (T) args.get(0);
        ExpressionRefNode expr = (ExpressionRefNode) args.get(1);

        T maxElement = null;
        T maxValue = null;
        Type expectedType = null;

        for (T elem : runtime.getArrayElements(array)) {
            T value = expr.evaluateRef(runtime, elem);

            Type valueType = runtime.typeOf(value);
            if (valueType != Type.STRING && valueType != Type.NUMBER) {
                throw new JmesPathException(
                    "max_by() requires expression to evaluate to string or number, got " +
                        valueType,
                    JmesPathException.ErrorType.TYPE
                );
            }
            if (expectedType == null) {
                expectedType = valueType;
            } else if (valueType != expectedType) {
                throw new JmesPathException(
                    "max_by() requires all values to be of the same type, got " +
                        expectedType +
                        " and " +
                        valueType,
                    JmesPathException.ErrorType.TYPE
                );
            }

            if (maxElement == null) {
                maxElement = elem;
                maxValue = value;
            } else if (runtime.compare(value, maxValue) > 0) {
                maxElement = elem;
                maxValue = value;
            }
        }

        if (maxElement == null) {
            return runtime.createNull();
        }
        return maxElement;
    }
}
