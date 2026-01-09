package io.jmespath;

/**
 * A compiled JMESPath expression.
 *
 * <p>Expressions are compiled once and can be evaluated many times
 * against different data. They are immutable and thread-safe.
 *
 * <p>Example usage:
 * <pre>{@code
 * Expression<Object> expr = JmesPath.compile("people[?age > `18`].name");
 *
 * // Evaluate against different data
 * Object result1 = expr.evaluate(runtime, data1);
 * Object result2 = expr.evaluate(runtime, data2);
 * }</pre>
 *
 * @param <T> the JSON value type used by the runtime
 */
public interface Expression<T> {

    /**
     * Evaluates this expression against the given data.
     *
     * @param runtime the runtime adapter for JSON operations
     * @param data the JSON data to query
     * @return the query result
     * @throws JmesPathException if evaluation fails
     */
    T evaluate(Runtime<T> runtime, T data);

    /**
     * Returns the original expression string.
     *
     * @return the expression string
     */
    String getExpression();
}
