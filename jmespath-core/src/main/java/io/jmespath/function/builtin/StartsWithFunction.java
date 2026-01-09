package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * starts_with(subject, prefix) - Returns true if subject starts with prefix.
 */
public final class StartsWithFunction implements Function {

    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.STRING)
        .required(ArgumentType.STRING)
        .build();

    @Override
    public String getName() {
        return "starts_with";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        String subject = runtime.toString((T) args.get(0));
        String prefix = runtime.toString((T) args.get(1));
        return runtime.createBoolean(subject.startsWith(prefix));
    }
}
