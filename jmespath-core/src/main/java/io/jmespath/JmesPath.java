package io.jmespath;

import io.jmespath.function.DefaultFunctionRegistry;
import io.jmespath.function.FunctionRegistry;
import io.jmespath.internal.CompiledExpression;
import io.jmespath.internal.Parser;
import io.jmespath.internal.node.Node;
import io.jmespath.runtime.MapRuntime;

/**
 * Main entry point for JMESPath operations.
 *
 * <p>JMESPath is a query language for JSON. This class provides static
 * methods for compiling and evaluating JMESPath expressions.
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * // Compile once, evaluate many times
 * Expression<Object> expr = JmesPath.compile("people[?age > `18`].name");
 *
 * MapRuntime runtime = new MapRuntime();
 * Object result = expr.evaluate(runtime, data);
 * }</pre>
 *
 * <h2>Quick Evaluation</h2>
 * <pre>{@code
 * // One-off evaluation with built-in runtime
 * Object result = JmesPath.search("foo.bar", data);
 * }</pre>
 *
 * <h2>Custom Runtime</h2>
 * <pre>{@code
 * // Use with Jackson, Gson, or other JSON libraries
 * Runtime<JsonNode> runtime = new JacksonRuntime();
 * Expression<JsonNode> expr = JmesPath.compile("users[*].name");
 * JsonNode result = expr.evaluate(runtime, jsonData);
 * }</pre>
 *
 * @see Expression
 * @see Runtime
 * @see <a href="https://jmespath.org/">JMESPath Specification</a>
 */
public final class JmesPath {

    private static final FunctionRegistry DEFAULT_FUNCTION_REGISTRY =
        new DefaultFunctionRegistry();
    private static final MapRuntime DEFAULT_RUNTIME = new MapRuntime(
        DEFAULT_FUNCTION_REGISTRY
    );

    // Prevent instantiation
    private JmesPath() {}

    /**
     * Compiles a JMESPath expression.
     *
     * <p>The compiled expression can be evaluated multiple times against
     * different data. It is immutable and thread-safe.
     *
     * @param <T> the JSON value type
     * @param expression the JMESPath expression
     * @return the compiled expression
     * @throws JmesPathException if the expression is invalid
     *
     * @see Expression#evaluate(Runtime, Object)
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> compile(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("expression cannot be null");
        }
        Parser parser = new Parser(expression);
        Node root = parser.parse();
        return (Expression<T>) new CompiledExpression<Object>(expression, root);
    }

    /**
     * Evaluates a JMESPath expression against the given data using the
     * default MapRuntime.
     *
     * <p>This is a convenience method for one-off queries. For repeated
     * queries, use {@link #compile(String)} to avoid re-parsing.
     *
     * <p>The data should be composed of standard Java types:
     * <ul>
     *   <li>null</li>
     *   <li>Boolean</li>
     *   <li>Number (Integer, Long, Double)</li>
     *   <li>String</li>
     *   <li>List&lt;Object&gt;</li>
     *   <li>Map&lt;String, Object&gt;</li>
     * </ul>
     *
     * @param expression the JMESPath expression
     * @param data the data to query
     * @return the query result
     * @throws JmesPathException if the expression is invalid or evaluation fails
     */
    public static Object search(String expression, Object data) {
        return search(expression, data, DEFAULT_FUNCTION_REGISTRY);
    }

    /**
     * Evaluates a JMESPath expression against the given data using the
     * default MapRuntime with a custom function registry.
     *
     * @param expression the JMESPath expression
     * @param data the data to query
     * @param functionRegistry the function registry (can be null)
     * @return the query result
     * @throws JmesPathException if the expression is invalid or evaluation fails
     */
    public static Object search(
        String expression,
        Object data,
        FunctionRegistry functionRegistry
    ) {
        Expression<Object> compiled = compile(expression);
        MapRuntime runtime = new MapRuntime(functionRegistry);
        return compiled.evaluate(runtime, data);
    }

    /**
     * Parses JSON into a structure suitable for the default MapRuntime.
     *
     * <p>This is a convenience method for parsing JSON literals. The result
     * can be used directly with {@link #search(String, Object)}.
     *
     * @param json the JSON string to parse
     * @return the parsed value
     * @throws JmesPathException if the JSON is invalid
     */
    public static Object parseJson(String json) {
        return DEFAULT_RUNTIME.parseJson(json);
    }

    /**
     * Returns the default MapRuntime instance.
     *
     * <p>This runtime is shared and should not be modified.
     *
     * @return the default runtime
     */
    public static MapRuntime getDefaultRuntime() {
        return DEFAULT_RUNTIME;
    }
}
