package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the reverse() function.
 * Reverses an array or string.
 */
public final class ReverseFunction implements Function {
    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ARRAY, ArgumentType.STRING)
        .build();

    @Override
    public String getName() {
        return "reverse";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T value = (T) args.get(0);

        if (runtime.isString(value)) {
            String str = runtime.toString(value);
            StringBuilder sb = new StringBuilder(str.length());
            for (int i = str.length() - 1; i >= 0; i--) {
                sb.append(str.charAt(i));
            }
            return runtime.createString(sb.toString());
        }

        // Array
        List<T> elements = new ArrayList<T>();
        for (T elem : runtime.getArrayElements(value)) {
            elements.add(elem);
        }

        List<T> reversed = new ArrayList<T>(elements.size());
        for (int i = elements.size() - 1; i >= 0; i--) {
            reversed.add(elements.get(i));
        }

        return runtime.createArray(reversed);
    }
}
