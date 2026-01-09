package io.jmespath.function.builtin;

import io.jmespath.JmesPathException;
import io.jmespath.Runtime;
import io.jmespath.Type;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;
import io.jmespath.internal.node.ExpressionRefNode;
import io.jmespath.internal.node.FunctionCallNode.ScopedExpressionRef;
import java.util.List;

/**
 * Implements the min_by() function.
 * Returns the element with the minimum value from applying an expression.
 * All extracted values must be of the same type (either all strings or all numbers).
 */
public final class MinByFunction implements Function {

    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ARRAY)
        .required(ArgumentType.EXPRESSION_REF)
        .build();

    @Override
    public String getName() {
        return "min_by";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T array = (T) args.get(0);
        Object exprArg = args.get(1);

        T minElement = null;
        T minValue = null;
        Type expectedType = null;

        for (T elem : runtime.getArrayElements(array)) {
            T value;
            if (exprArg instanceof ScopedExpressionRef) {
                value = ((ScopedExpressionRef<T>) exprArg).evaluate(
                    runtime,
                    elem
                );
            } else {
                value = ((ExpressionRefNode) exprArg).evaluateRef(
                    runtime,
                    elem
                );
            }

            Type valueType = runtime.typeOf(value);
            if (valueType != Type.STRING && valueType != Type.NUMBER) {
                if (runtime.isSilentTypeErrors()) {
                    return runtime.createNull();
                }
                throw new JmesPathException(
                    "min_by() requires expression to evaluate to string or number, got " +
                        valueType,
                    JmesPathException.ErrorType.TYPE
                );
            }
            if (expectedType == null) {
                expectedType = valueType;
            } else if (valueType != expectedType) {
                if (runtime.isSilentTypeErrors()) {
                    return runtime.createNull();
                }
                throw new JmesPathException(
                    "min_by() requires all values to be of the same type, got " +
                        expectedType +
                        " and " +
                        valueType,
                    JmesPathException.ErrorType.TYPE
                );
            }

            if (minElement == null) {
                minElement = elem;
                minValue = value;
            } else if (runtime.compare(value, minValue) < 0) {
                minElement = elem;
                minValue = value;
            }
        }

        if (minElement == null) {
            return runtime.createNull();
        }
        return minElement;
    }
}
