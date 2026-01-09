package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the to_array() function.
 * Converts a value to an array.
 * If already an array, returns as-is.
 * Otherwise, wraps the value in a single-element array.
 */
public final class ToArrayFunction implements Function {
    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ANY)
        .build();

    @Override
    public String getName() {
        return "to_array";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T value = (T) args.get(0);

        if (runtime.isArray(value)) {
            return value;
        }

        List<T> result = new ArrayList<T>(1);
        result.add(value);
        return runtime.createArray(result);
    }
}
