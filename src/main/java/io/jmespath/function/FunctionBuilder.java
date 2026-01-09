package io.jmespath.function;

import io.jmespath.Runtime;

import java.util.List;

/**
 * Fluent builder for creating JMESPath functions without boilerplate.
 *
 * <p>This builder provides a concise way to define custom functions,
 * especially useful for simple transformations.
 *
 * <p>Example - simple string function:
 * <pre>{@code
 * Function upper = FunctionBuilder.create("upper")
 *     .args(ArgumentType.STRING)
 *     .body(new FunctionBody() {
 *         public <T> T call(Runtime<T> rt, List<Object> args, T current) {
 *             String s = rt.toString((T) args.get(0));
 *             return rt.createString(s.toUpperCase());
 *         }
 *     })
 *     .build();
 * }</pre>
 *
 * <p>Example - with metadata:
 * <pre>{@code
 * Function upper = FunctionBuilder.create("upper")
 *     .args(ArgumentType.STRING)
 *     .category("string")
 *     .description("Convert string to uppercase")
 *     .example("upper('hello') -> \"HELLO\"", "Basic usage")
 *     .body(...)
 *     .build();
 * }</pre>
 *
 * <p>Example - variadic function:
 * <pre>{@code
 * Function concat = FunctionBuilder.create("concat")
 *     .required(ArgumentType.STRING)
 *     .variadic(ArgumentType.STRING)
 *     .body(...)
 *     .build();
 * }</pre>
 */
public final class FunctionBuilder {

    private final String name;
    private Signature.Builder signatureBuilder = Signature.builder();
    private FunctionMetadata.Builder metadataBuilder;
    private FunctionBody body;

    private FunctionBuilder(String name) {
        this.name = name;
        this.metadataBuilder = FunctionMetadata.builder(name);
    }

    /**
     * Creates a new function builder.
     *
     * @param name the function name
     * @return a new builder
     */
    public static FunctionBuilder create(String name) {
        return new FunctionBuilder(name);
    }

    /**
     * Sets the argument types (shorthand for required args with union types).
     *
     * @param types the accepted types (any of these types is accepted)
     */
    public FunctionBuilder args(ArgumentType... types) {
        signatureBuilder.required(types);
        return this;
    }

    /**
     * Adds a required argument.
     *
     * @param types the accepted types for this argument
     */
    public FunctionBuilder required(ArgumentType... types) {
        signatureBuilder.required(types);
        return this;
    }

    /**
     * Adds an optional argument.
     *
     * @param types the accepted types for this argument
     */
    public FunctionBuilder optional(ArgumentType... types) {
        signatureBuilder.optional(types);
        return this;
    }

    /**
     * Adds a variadic argument (zero or more additional arguments).
     *
     * @param types the accepted types for variadic arguments
     */
    public FunctionBuilder variadic(ArgumentType... types) {
        signatureBuilder.variadic(types);
        return this;
    }

    /**
     * Sets the category for documentation.
     */
    public FunctionBuilder category(String category) {
        metadataBuilder.category(category);
        return this;
    }

    /**
     * Sets the description for documentation.
     */
    public FunctionBuilder description(String description) {
        metadataBuilder.description(description);
        return this;
    }

    /**
     * Sets the signature description for documentation.
     */
    public FunctionBuilder signatureDoc(String signature) {
        metadataBuilder.signature(signature);
        return this;
    }

    /**
     * Adds a usage example.
     */
    public FunctionBuilder example(String code, String description) {
        metadataBuilder.example(code, description);
        return this;
    }

    /**
     * Adds an alias for this function.
     */
    public FunctionBuilder alias(String alias) {
        metadataBuilder.alias(alias);
        return this;
    }

    /**
     * Sets the function body.
     */
    public FunctionBuilder body(FunctionBody body) {
        this.body = body;
        return this;
    }

    /**
     * Builds the function.
     *
     * @return the built function
     * @throws IllegalStateException if no body was provided
     */
    public Function build() {
        if (body == null) {
            throw new IllegalStateException("Function body is required");
        }

        final String fnName = name;
        final Signature sig = signatureBuilder.build();
        final FunctionBody fnBody = body;

        return new Function() {
            @Override
            public String getName() {
                return fnName;
            }

            @Override
            public Signature getSignature() {
                return sig;
            }

            @Override
            public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                return fnBody.call(runtime, args, current);
            }
        };
    }

    /**
     * Builds the function and returns it with its metadata.
     *
     * @return a holder containing both function and metadata
     */
    public FunctionWithMetadata buildWithMetadata() {
        return new FunctionWithMetadata(build(), metadataBuilder.build());
    }

    /**
     * The function implementation.
     */
    public interface FunctionBody {
        /**
         * Executes the function.
         *
         * @param <T> the JSON value type
         * @param runtime the runtime adapter
         * @param args the validated arguments
         * @param current the current JSON value
         * @return the function result
         */
        <T> T call(Runtime<T> runtime, List<Object> args, T current);
    }

    /**
     * Holder for a function and its metadata.
     */
    public static final class FunctionWithMetadata {
        private final Function function;
        private final FunctionMetadata metadata;

        public FunctionWithMetadata(Function function, FunctionMetadata metadata) {
            this.function = function;
            this.metadata = metadata;
        }

        public Function getFunction() {
            return function;
        }

        public FunctionMetadata getMetadata() {
            return metadata;
        }
    }
}
