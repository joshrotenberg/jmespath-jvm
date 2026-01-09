package io.jmespath.function;

import io.jmespath.JmesPathException;
import io.jmespath.Runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of FunctionRegistry with all 26 standard functions.
 *
 * <p>This registry is thread-safe after construction. Custom functions can
 * be registered using {@link #register(Function)}.
 *
 * <p>Standard functions:
 * <ul>
 *   <li>Type functions: type</li>
 *   <li>Numeric functions: abs, avg, ceil, floor, sum</li>
 *   <li>String functions: contains, ends_with, join, length, starts_with</li>
 *   <li>Array functions: length, max, min, reverse, sort</li>
 *   <li>Object functions: keys, merge, values</li>
 *   <li>Conversion functions: to_array, to_number, to_string</li>
 *   <li>Expression functions: map, max_by, min_by, sort_by</li>
 *   <li>Utility functions: not_null</li>
 * </ul>
 */
public class DefaultFunctionRegistry implements FunctionRegistry {

    private final Map<String, Function> functions = new HashMap<String, Function>();

    /**
     * Creates a new registry with all standard functions.
     */
    public DefaultFunctionRegistry() {
        registerBuiltins();
    }

    /**
     * Registers a custom function.
     *
     * <p>If a function with the same name already exists, it is replaced.
     *
     * @param function the function to register
     */
    public void register(Function function) {
        if (function == null) {
            throw new IllegalArgumentException("function cannot be null");
        }
        functions.put(function.getName(), function);
    }

    @Override
    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(String name, Runtime<T> runtime, T current, List<Object> arguments) {
        Function function = functions.get(name);
        if (function == null) {
            throw new JmesPathException(
                "Unknown function: " + name + "()",
                JmesPathException.ErrorType.UNKNOWN_FUNCTION
            );
        }

        // Validate arguments
        function.getSignature().validate(name, runtime, arguments);

        // Call the function
        return function.call(runtime, arguments, current);
    }

    private void registerBuiltins() {
        // Import all builtin functions
        register(new io.jmespath.function.builtin.AbsFunction());
        register(new io.jmespath.function.builtin.AvgFunction());
        register(new io.jmespath.function.builtin.CeilFunction());
        register(new io.jmespath.function.builtin.ContainsFunction());
        register(new io.jmespath.function.builtin.EndsWithFunction());
        register(new io.jmespath.function.builtin.FloorFunction());
        register(new io.jmespath.function.builtin.JoinFunction());
        register(new io.jmespath.function.builtin.KeysFunction());
        register(new io.jmespath.function.builtin.LengthFunction());
        register(new io.jmespath.function.builtin.MapFunction());
        register(new io.jmespath.function.builtin.MaxFunction());
        register(new io.jmespath.function.builtin.MaxByFunction());
        register(new io.jmespath.function.builtin.MergeFunction());
        register(new io.jmespath.function.builtin.MinFunction());
        register(new io.jmespath.function.builtin.MinByFunction());
        register(new io.jmespath.function.builtin.NotNullFunction());
        register(new io.jmespath.function.builtin.ReverseFunction());
        register(new io.jmespath.function.builtin.SortFunction());
        register(new io.jmespath.function.builtin.SortByFunction());
        register(new io.jmespath.function.builtin.StartsWithFunction());
        register(new io.jmespath.function.builtin.SumFunction());
        register(new io.jmespath.function.builtin.ToArrayFunction());
        register(new io.jmespath.function.builtin.ToNumberFunction());
        register(new io.jmespath.function.builtin.ToStringFunction());
        register(new io.jmespath.function.builtin.TypeFunction());
        register(new io.jmespath.function.builtin.ValuesFunction());
    }
}
