/**
 * JMESPath function infrastructure and built-in functions.
 *
 * <p>This package provides:
 * <ul>
 *   <li>The {@link io.jmespath.function.Function} interface for custom functions</li>
 *   <li>{@link io.jmespath.function.FunctionBuilder} for creating functions fluently</li>
 *   <li>{@link io.jmespath.function.Signature} for argument validation</li>
 *   <li>All 26 built-in JMESPath functions in the {@code builtin} subpackage</li>
 * </ul>
 *
 * <h2>Creating Custom Functions</h2>
 * <pre>{@code
 * Function upper = FunctionBuilder.create("upper")
 *     .args(ArgumentType.STRING)
 *     .body((runtime, args, current) -> {
 *         String s = runtime.toString(args.get(0));
 *         return runtime.createString(s.toUpperCase());
 *     })
 *     .build();
 *
 * FunctionRegistry registry = new DefaultFunctionRegistry();
 * registry.register(upper);
 * }</pre>
 *
 * @see io.jmespath.function.Function
 * @see io.jmespath.function.FunctionBuilder
 */
package io.jmespath.function;
