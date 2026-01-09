package io.jmespath.function.builtin;

import io.jmespath.JmesPathException;
import io.jmespath.Runtime;
import io.jmespath.Type;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;
import io.jmespath.internal.node.ExpressionRefNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the sort_by() function.
 * Sorts an array by the result of applying an expression to each element.
 * All extracted values must be of the same type (either all strings or all numbers).
 */
public final class SortByFunction implements Function {

    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ARRAY)
        .required(ArgumentType.EXPRESSION_REF)
        .build();

    @Override
    public String getName() {
        return "sort_by";
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

        // Collect elements and their sort keys
        List<T> elements = new ArrayList<T>();
        List<T> keys = new ArrayList<T>();
        Type expectedType = null;

        for (T elem : runtime.getArrayElements(array)) {
            elements.add(elem);
            T keyValue = expr.evaluateRef(runtime, elem);
            keys.add(keyValue);

            Type keyType = runtime.typeOf(keyValue);
            if (keyType != Type.STRING && keyType != Type.NUMBER) {
                if (runtime.isSilentTypeErrors()) {
                    return runtime.createNull();
                }
                throw new JmesPathException(
                    "sort_by() requires expression to evaluate to string or number, got " +
                        keyType,
                    JmesPathException.ErrorType.TYPE
                );
            }
            if (expectedType == null) {
                expectedType = keyType;
            } else if (keyType != expectedType) {
                if (runtime.isSilentTypeErrors()) {
                    return runtime.createNull();
                }
                throw new JmesPathException(
                    "sort_by() requires all values to be of the same type, got " +
                        expectedType +
                        " and " +
                        keyType,
                    JmesPathException.ErrorType.TYPE
                );
            }
        }

        // Simple insertion sort by expression result
        for (int i = 1; i < elements.size(); i++) {
            T key = elements.get(i);
            T keyValue = keys.get(i);
            int j = i - 1;
            while (j >= 0) {
                T elemValue = keys.get(j);
                if (runtime.compare(elemValue, keyValue) <= 0) {
                    break;
                }
                elements.set(j + 1, elements.get(j));
                keys.set(j + 1, keys.get(j));
                j--;
            }
            elements.set(j + 1, key);
            keys.set(j + 1, keyValue);
        }

        return runtime.createArray(elements);
    }
}
