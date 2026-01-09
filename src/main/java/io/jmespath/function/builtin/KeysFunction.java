package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.ArrayList;
import java.util.List;

/**
 * keys(object) - Returns an array of the object's keys.
 */
public final class KeysFunction implements Function {

    private static final Signature SIGNATURE = Signature.of(ArgumentType.OBJECT);

    @Override
    public String getName() {
        return "keys";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T obj = (T) args.get(0);
        List<T> keys = new ArrayList<T>();

        for (String key : runtime.getObjectKeys(obj)) {
            keys.add(runtime.createString(key));
        }

        return runtime.createArray(keys);
    }
}
