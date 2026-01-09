package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * length(subject) - Returns the length of a string, array, or object.
 */
public final class LengthFunction implements Function {

    private static final Signature SIGNATURE = Signature.of(
        ArgumentType.STRING, ArgumentType.ARRAY, ArgumentType.OBJECT
    );

    @Override
    public String getName() {
        return "length";
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
            String s = runtime.toString(value);
            return runtime.createNumber(s.length());
        }

        if (runtime.isArray(value)) {
            return runtime.createNumber(runtime.getArrayLength(value));
        }

        if (runtime.isObject(value)) {
            int count = 0;
            for (String key : runtime.getObjectKeys(value)) {
                count++;
            }
            return runtime.createNumber(count);
        }

        return runtime.createNull();
    }
}
