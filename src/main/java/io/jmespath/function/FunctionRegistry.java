package io.jmespath.function;

import io.jmespath.Runtime;

import java.util.List;

/**
 * Registry of JMESPath functions.
 *
 * <p>This interface is used by FunctionCallNode to invoke functions.
 * The default implementation contains all 26 standard JMESPath functions.
 *
 * <p>Custom functions can be registered for domain-specific extensions.
 */
public interface FunctionRegistry {

    /**
     * Calls a function by name with the given arguments.
     *
     * @param <T> the JSON value type
     * @param name the function name
     * @param runtime the runtime adapter
     * @param current the current JSON value (for expression references)
     * @param arguments the evaluated arguments (may include ExpressionRefNode for lazy args)
     * @return the function result
     */
    <T> T call(String name, Runtime<T> runtime, T current, List<Object> arguments);

    /**
     * Returns true if a function with the given name exists.
     *
     * @param name the function name
     * @return true if the function exists
     */
    boolean hasFunction(String name);
}
