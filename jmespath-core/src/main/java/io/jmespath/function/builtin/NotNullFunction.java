package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * Implements the not_null() function.
 * Returns the first non-null argument.
 */
public final class NotNullFunction implements Function {
    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ANY)
        .variadic(ArgumentType.ANY)
        .build();

    @Override
    public String getName() {
        return "not_null";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        for (Object arg : args) {
            T value = (T) arg;
            if (!runtime.isNull(value)) {
                return value;
            }
        }
        return runtime.createNull();
    }
}
