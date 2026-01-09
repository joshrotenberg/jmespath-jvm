package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * type(value) - Returns the type of a value as a string.
 */
public final class TypeFunction implements Function {

    private static final Signature SIGNATURE = Signature.of(ArgumentType.ANY);

    @Override
    public String getName() {
        return "type";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T value = (T) args.get(0);
        String typeName = runtime.typeOf(value).typeName();
        return runtime.createString(typeName);
    }
}
