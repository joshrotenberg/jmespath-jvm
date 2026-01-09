package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * Implements the min() function.
 * Returns the minimum value in an array of numbers or strings.
 */
public final class MinFunction implements Function {
    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ARRAY_NUMBER, ArgumentType.ARRAY_STRING)
        .build();

    @Override
    public String getName() {
        return "min";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T array = (T) args.get(0);

        T minValue = null;
        for (T elem : runtime.getArrayElements(array)) {
            if (minValue == null) {
                minValue = elem;
            } else if (runtime.compare(elem, minValue) < 0) {
                minValue = elem;
            }
        }

        if (minValue == null) {
            return runtime.createNull();
        }
        return minValue;
    }
}
