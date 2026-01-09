package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * join(glue, array[string]) - Joins an array of strings with a separator.
 */
public final class JoinFunction implements Function {

    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.STRING)
        .required(ArgumentType.ARRAY_STRING)
        .build();

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        String glue = runtime.toString((T) args.get(0));
        T array = (T) args.get(1);

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (T elem : runtime.getArrayElements(array)) {
            if (!first) {
                sb.append(glue);
            }
            first = false;
            sb.append(runtime.toString(elem));
        }

        return runtime.createString(sb.toString());
    }
}
