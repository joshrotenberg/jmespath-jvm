package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implements the sort() function.
 * Sorts an array of numbers or strings.
 */
public final class SortFunction implements Function {
    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ARRAY_NUMBER, ArgumentType.ARRAY_STRING)
        .build();

    @Override
    public String getName() {
        return "sort";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T array = (T) args.get(0);

        final List<T> elements = new ArrayList<T>();
        for (T elem : runtime.getArrayElements(array)) {
            elements.add(elem);
        }

        // Simple insertion sort - no dependencies
        for (int i = 1; i < elements.size(); i++) {
            T key = elements.get(i);
            int j = i - 1;
            while (j >= 0 && runtime.compare(elements.get(j), key) > 0) {
                elements.set(j + 1, elements.get(j));
                j--;
            }
            elements.set(j + 1, key);
        }

        return runtime.createArray(elements);
    }
}
