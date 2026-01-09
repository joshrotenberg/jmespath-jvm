package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * Implements the max() function.
 * Returns the maximum value in an array of numbers or strings.
 */
public final class MaxFunction implements Function {
    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ARRAY_NUMBER, ArgumentType.ARRAY_STRING)
        .build();

    @Override
    public String getName() {
        return "max";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T array = (T) args.get(0);

        T maxValue = null;
        for (T elem : runtime.getArrayElements(array)) {
            if (maxValue == null) {
                maxValue = elem;
            } else if (runtime.compare(elem, maxValue) > 0) {
                maxValue = elem;
            }
        }

        if (maxValue == null) {
            return runtime.createNull();
        }
        return maxValue;
    }
}
