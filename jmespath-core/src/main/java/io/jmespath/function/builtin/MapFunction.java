package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;
import io.jmespath.internal.node.ExpressionRefNode;
import io.jmespath.internal.node.FunctionCallNode.ScopedExpressionRef;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the map() function.
 * Applies an expression to each element of an array.
 */
public final class MapFunction implements Function {

    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.EXPRESSION_REF)
        .required(ArgumentType.ARRAY)
        .build();

    @Override
    public String getName() {
        return "map";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        Object exprArg = args.get(0);
        T array = (T) args.get(1);

        List<T> result = new ArrayList<T>();
        for (T elem : runtime.getArrayElements(array)) {
            T mapped;
            if (exprArg instanceof ScopedExpressionRef) {
                mapped = ((ScopedExpressionRef<T>) exprArg).evaluate(
                    runtime,
                    elem
                );
            } else {
                mapped = ((ExpressionRefNode) exprArg).evaluateRef(
                    runtime,
                    elem
                );
            }
            result.add(mapped);
        }

        return runtime.createArray(result);
    }
}
