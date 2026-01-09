package io.jmespath.function;

import io.jmespath.JmesPathException;
import io.jmespath.Runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A composable function registry for custom extensions.
 *
 * <p>ExtensionRegistry allows building custom function sets by:
 * <ul>
 *   <li>Starting from scratch with only custom functions</li>
 *   <li>Extending the default registry with additional functions</li>
 *   <li>Composing multiple extension modules together</li>
 * </ul>
 *
 * <p>Example - adding custom functions:
 * <pre>{@code
 * ExtensionRegistry registry = ExtensionRegistry.builder()
 *     .withDefaults()  // include all 26 standard functions
 *     .register(new UpperFunction())
 *     .register(new LowerFunction())
 *     .build();
 *
 * Object result = JmesPath.search("upper(name)", data, registry);
 * }</pre>
 *
 * <p>Example - composing extension modules:
 * <pre>{@code
 * ExtensionRegistry registry = ExtensionRegistry.builder()
 *     .withDefaults()
 *     .withModule(StringExtensions.functions())
 *     .withModule(DateTimeExtensions.functions())
 *     .build();
 * }</pre>
 *
 * <p>Example - custom functions only (no standard functions):
 * <pre>{@code
 * ExtensionRegistry registry = ExtensionRegistry.builder()
 *     .register(new MyFunction1())
 *     .register(new MyFunction2())
 *     .build();
 * }</pre>
 */
public final class ExtensionRegistry implements FunctionRegistry {

    private final Map<String, Function> functions;
    private final Map<String, FunctionMetadata> metadata;
    private final Map<String, List<Function>> byCategory;

    private ExtensionRegistry(Builder builder) {
        this.functions = Collections.unmodifiableMap(new LinkedHashMap<String, Function>(builder.functions));
        this.metadata = Collections.unmodifiableMap(new HashMap<String, FunctionMetadata>(builder.metadata));

        // Build category index
        Map<String, List<Function>> catIndex = new HashMap<String, List<Function>>();
        for (Map.Entry<String, FunctionMetadata> entry : metadata.entrySet()) {
            String cat = entry.getValue().getCategory();
            if (!catIndex.containsKey(cat)) {
                catIndex.put(cat, new ArrayList<Function>());
            }
            Function fn = functions.get(entry.getKey());
            if (fn != null) {
                catIndex.get(cat).add(fn);
            }
        }
        this.byCategory = Collections.unmodifiableMap(catIndex);
    }

    @Override
    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    @Override
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

    /**
     * Returns all registered function names.
     */
    public Collection<String> getFunctionNames() {
        return functions.keySet();
    }

    /**
     * Returns the function with the given name, or null if not found.
     */
    public Function getFunction(String name) {
        return functions.get(name);
    }

    /**
     * Returns metadata for the given function, or null if not found.
     */
    public FunctionMetadata getMetadata(String name) {
        return metadata.get(name);
    }

    /**
     * Returns all registered categories.
     */
    public Collection<String> getCategories() {
        return byCategory.keySet();
    }

    /**
     * Returns all functions in a category.
     */
    public List<Function> getFunctionsByCategory(String category) {
        List<Function> result = byCategory.get(category);
        return result != null ? result : Collections.<Function>emptyList();
    }

    /**
     * Returns the number of registered functions.
     */
    public int size() {
        return functions.size();
    }

    /**
     * Creates a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a registry with only the default functions.
     */
    public static ExtensionRegistry defaults() {
        return builder().withDefaults().build();
    }

    /**
     * Builder for ExtensionRegistry.
     */
    public static final class Builder {
        private final Map<String, Function> functions = new LinkedHashMap<String, Function>();
        private final Map<String, FunctionMetadata> metadata = new HashMap<String, FunctionMetadata>();

        private Builder() {}

        /**
         * Includes all 26 standard JMESPath functions.
         */
        public Builder withDefaults() {
            DefaultFunctionRegistry defaults = new DefaultFunctionRegistry();
            // We need to get the functions from DefaultFunctionRegistry
            // For now, just re-register them
            registerBuiltins();
            return this;
        }

        /**
         * Registers a function.
         *
         * <p>If a function with the same name already exists, it is replaced.
         */
        public Builder register(Function function) {
            if (function == null) {
                throw new IllegalArgumentException("function cannot be null");
            }
            functions.put(function.getName(), function);
            return this;
        }

        /**
         * Registers a function with metadata.
         */
        public Builder register(Function function, FunctionMetadata meta) {
            register(function);
            if (meta != null) {
                metadata.put(function.getName(), meta);
            }
            return this;
        }

        /**
         * Registers all functions from a module (collection of functions).
         */
        public Builder withModule(Iterable<Function> module) {
            for (Function fn : module) {
                register(fn);
            }
            return this;
        }

        /**
         * Registers all functions from another registry.
         */
        public Builder withRegistry(ExtensionRegistry other) {
            for (String name : other.getFunctionNames()) {
                Function fn = other.getFunction(name);
                FunctionMetadata meta = other.getMetadata(name);
                register(fn, meta);
            }
            return this;
        }

        /**
         * Registers an alias for an existing function.
         */
        public Builder alias(String alias, String existingFunctionName) {
            Function fn = functions.get(existingFunctionName);
            if (fn == null) {
                throw new IllegalArgumentException("Function not found: " + existingFunctionName);
            }
            // Create an alias wrapper
            final Function target = fn;
            functions.put(alias, new Function() {
                @Override
                public String getName() {
                    return alias;
                }

                @Override
                public Signature getSignature() {
                    return target.getSignature();
                }

                @Override
                public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                    return target.call(runtime, args, current);
                }
            });
            return this;
        }

        /**
         * Builds the registry.
         */
        public ExtensionRegistry build() {
            return new ExtensionRegistry(this);
        }

        private void registerBuiltins() {
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
}
