package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * Implements the to_number() function.
 * Converts a value to a number.
 * Returns null if conversion is not possible.
 */
public final class ToNumberFunction implements Function {
    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ANY)
        .build();

    @Override
    public String getName() {
        return "to_number";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T value = (T) args.get(0);

        if (runtime.isNumber(value)) {
            return value;
        }

        if (runtime.isString(value)) {
            String str = runtime.toString(value);
            try {
                if (str.contains(".") || str.contains("e") || str.contains("E")) {
                    return runtime.createNumber(Double.parseDouble(str));
                } else {
                    return runtime.createNumber(Long.parseLong(str));
                }
            } catch (NumberFormatException e) {
                return runtime.createNull();
            }
        }

        return runtime.createNull();
    }
}
