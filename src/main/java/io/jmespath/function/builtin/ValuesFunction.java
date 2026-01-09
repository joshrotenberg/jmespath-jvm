package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.ArrayList;
import java.util.List;

/**
 * values(object) - Returns an array of the object's values.
 */
public final class ValuesFunction implements Function {

    private static final Signature SIGNATURE = Signature.of(ArgumentType.OBJECT);

    @Override
    public String getName() {
        return "values";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T obj = (T) args.get(0);
        List<T> values = new ArrayList<T>();

        for (T value : runtime.getObjectValues(obj)) {
            values.add(value);
        }

        return runtime.createArray(values);
    }
}
