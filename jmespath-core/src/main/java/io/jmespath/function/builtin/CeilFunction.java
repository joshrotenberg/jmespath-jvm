package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * ceil(number) - Returns the ceiling of a number.
 */
public final class CeilFunction implements Function {

    private static final Signature SIGNATURE = Signature.of(ArgumentType.NUMBER);

    @Override
    public String getName() {
        return "ceil";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        Number num = runtime.toNumber((T) args.get(0));
        double ceil = Math.ceil(num.doubleValue());
        return runtime.createNumber((long) ceil);
    }
}
