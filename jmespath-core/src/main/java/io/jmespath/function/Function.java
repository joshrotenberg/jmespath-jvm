package io.jmespath.function;

import io.jmespath.Runtime;

import java.util.List;

/**
 * A JMESPath function.
 *
 * <p>Functions are called from JMESPath expressions using the syntax
 * {@code function_name(arg1, arg2, ...)}.
 *
 * <p>Implement this interface to create custom functions. Built-in
 * functions are provided in the {@code builtin} package.
 *
 * <p>Example custom function:
 * <pre>{@code
 * public class UpperFunction implements Function {
 *     public String getName() { return "upper"; }
 *
 *     public Signature getSignature() {
 *         return Signature.of(ArgumentType.STRING);
 *     }
 *
 *     public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
 *         String s = runtime.toString((T) args.get(0));
 *         return runtime.createString(s.toUpperCase());
 *     }
 * }
 * }</pre>
 */
public interface Function {

    /**
     * Returns the function name as used in expressions.
     *
     * @return the function name (e.g., "length", "sort_by")
     */
    String getName();

    /**
     * Returns the function signature for argument validation.
     *
     * @return the signature
     */
    Signature getSignature();

    /**
     * Calls the function with the given arguments.
     *
     * <p>Arguments have already been validated against the signature.
     * Expression reference arguments are passed as {@code ExpressionRefNode}
     * objects and should be evaluated using {@code evaluateRef()}.
     *
     * @param <T> the JSON value type
     * @param runtime the runtime adapter
     * @param args the validated arguments
     * @param current the current JSON value (for expression references)
     * @return the function result
     */
    <T> T call(Runtime<T> runtime, List<Object> args, T current);
}
