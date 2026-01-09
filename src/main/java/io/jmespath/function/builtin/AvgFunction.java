package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * avg(array[number]) - Returns the average of an array of numbers.
 */
public final class AvgFunction implements Function {

    private static final Signature SIGNATURE = Signature.of(ArgumentType.ARRAY_NUMBER);

    @Override
    public String getName() {
        return "avg";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T array = (T) args.get(0);
        int count = 0;
        double sum = 0;

        for (T elem : runtime.getArrayElements(array)) {
            sum += runtime.toNumber(elem).doubleValue();
            count++;
        }

        if (count == 0) {
            return runtime.createNull();
        }

        return runtime.createNumber(sum / count);
    }
}
