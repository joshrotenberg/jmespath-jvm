package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the merge() function.
 * Merges multiple objects into a single object.
 * Later objects override properties from earlier objects.
 */
public final class MergeFunction implements Function {
    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.OBJECT)
        .variadic(ArgumentType.OBJECT)
        .build();

    @Override
    public String getName() {
        return "merge";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        Map<String, T> result = new LinkedHashMap<String, T>();

        for (Object arg : args) {
            T obj = (T) arg;
            for (String key : runtime.getObjectKeys(obj)) {
                result.put(key, runtime.getProperty(obj, key));
            }
        }

        return runtime.createObject(result);
    }
}
