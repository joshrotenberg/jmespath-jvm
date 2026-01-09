package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * sum(array[number]) - Returns the sum of an array of numbers.
 */
public final class SumFunction implements Function {

    private static final Signature SIGNATURE = Signature.of(ArgumentType.ARRAY_NUMBER);

    @Override
    public String getName() {
        return "sum";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T array = (T) args.get(0);
        double sum = 0;

        for (T elem : runtime.getArrayElements(array)) {
            sum += runtime.toNumber(elem).doubleValue();
        }

        if (sum == Math.floor(sum) && sum < Long.MAX_VALUE && sum > Long.MIN_VALUE) {
            return runtime.createNumber((long) sum);
        }
        return runtime.createNumber(sum);
    }
}
