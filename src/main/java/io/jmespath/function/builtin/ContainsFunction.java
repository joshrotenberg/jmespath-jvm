package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * contains(subject, search) - Returns true if subject contains search.
 *
 * <p>For strings, checks if search is a substring.
 * For arrays, checks if search is an element (using deep equality).
 */
public final class ContainsFunction implements Function {

    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.STRING, ArgumentType.ARRAY)
        .required(ArgumentType.ANY)
        .build();

    @Override
    public String getName() {
        return "contains";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T subject = (T) args.get(0);
        T search = (T) args.get(1);

        if (runtime.isString(subject)) {
            if (!runtime.isString(search)) {
                return runtime.createBoolean(false);
            }
            String subjectStr = runtime.toString(subject);
            String searchStr = runtime.toString(search);
            return runtime.createBoolean(subjectStr.contains(searchStr));
        }

        if (runtime.isArray(subject)) {
            for (T elem : runtime.getArrayElements(subject)) {
                if (runtime.deepEquals(elem, search)) {
                    return runtime.createBoolean(true);
                }
            }
            return runtime.createBoolean(false);
        }

        return runtime.createBoolean(false);
    }
}
