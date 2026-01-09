package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * abs(number) - Returns the absolute value of a number.
 */
public final class AbsFunction implements Function {

    private static final Signature SIGNATURE = Signature.of(ArgumentType.NUMBER);

    @Override
    public String getName() {
        return "abs";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        Number num = runtime.toNumber((T) args.get(0));
        double abs = Math.abs(num.doubleValue());
        if (abs == Math.floor(abs) && abs < Long.MAX_VALUE) {
            return runtime.createNumber((long) abs);
        }
        return runtime.createNumber(abs);
    }
}
