/**
 * Runtime implementations for JMESPath.
 *
 * <p>This package provides {@link io.jmespath.runtime.MapRuntime}, a zero-dependency
 * runtime that uses standard Java collections (Map, List) for JSON values.
 *
 * <h2>Using MapRuntime</h2>
 * <pre>{@code
 * MapRuntime runtime = new MapRuntime();
 *
 * Map<String, Object> data = new HashMap<>();
 * data.put("name", "Alice");
 * data.put("scores", Arrays.asList(85, 90, 78));
 *
 * Expression<Object> expr = JmesPath.compile("scores[0]");
 * Object result = expr.evaluate(runtime, data);  // 85
 * }</pre>
 *
 * <h2>Custom Runtimes</h2>
 * <p>Implement {@link io.jmespath.Runtime} to use JMESPath with Jackson, Gson,
 * or other JSON libraries. See the documentation for complete examples.
 *
 * @see io.jmespath.runtime.MapRuntime
 * @see io.jmespath.Runtime
 */
package io.jmespath.runtime;
