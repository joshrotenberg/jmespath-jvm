/**
 * JMESPath query language for Java.
 *
 * <p>This package provides a fast, spec-compliant JMESPath implementation
 * with zero dependencies.
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * // Compile and evaluate
 * Expression<Object> expr = JmesPath.compile("people[?age > `21`].name");
 * Object result = expr.evaluate(new MapRuntime(), data);
 *
 * // Or use the convenience method
 * Object result = JmesPath.search("people[0].name", data);
 * }</pre>
 *
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link io.jmespath.JmesPath} - Entry point for compiling and evaluating expressions</li>
 *   <li>{@link io.jmespath.Expression} - A compiled, reusable expression</li>
 *   <li>{@link io.jmespath.Runtime} - Interface for JSON library adapters</li>
 *   <li>{@link io.jmespath.Type} - The six JMESPath types</li>
 *   <li>{@link io.jmespath.JmesPathException} - All errors (parse, type, evaluation)</li>
 * </ul>
 *
 * <h2>Using with Jackson, Gson, etc.</h2>
 * <p>Implement the {@link io.jmespath.Runtime} interface to use JMESPath
 * with your JSON library. See the documentation for examples.
 *
 * @see io.jmespath.JmesPath
 * @see <a href="https://jmespath.org/">JMESPath Specification</a>
 */
package io.jmespath;
