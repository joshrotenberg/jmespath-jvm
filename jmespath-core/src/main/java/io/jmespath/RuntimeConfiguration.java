package io.jmespath;

import io.jmespath.function.DefaultFunctionRegistry;
import io.jmespath.function.FunctionRegistry;

/**
 * Configuration options for JMESPath runtime behavior.
 *
 * <p>Use the builder to create configurations:
 * <pre>{@code
 * RuntimeConfiguration config = RuntimeConfiguration.builder()
 *     .withSilentTypeErrors(true)
 *     .withFunctionRegistry(customRegistry)
 *     .build();
 * }</pre>
 *
 * <p>Key options:
 * <ul>
 *   <li><b>silentTypeErrors</b> - When true, type errors return null instead of throwing.
 *       This matches the behavior of some JMESPath implementations.</li>
 *   <li><b>functionRegistry</b> - Custom function registry for adding or replacing functions.</li>
 * </ul>
 */
public final class RuntimeConfiguration {

    private final FunctionRegistry functionRegistry;
    private final boolean silentTypeErrors;

    private RuntimeConfiguration(Builder builder) {
        this.functionRegistry = builder.functionRegistry;
        this.silentTypeErrors = builder.silentTypeErrors;
    }

    /**
     * Returns the function registry to use.
     *
     * @return the function registry
     */
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    /**
     * Returns whether type errors should be silent (return null) or throw exceptions.
     *
     * @return true if type errors should return null
     */
    public boolean isSilentTypeErrors() {
        return silentTypeErrors;
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the default configuration.
     *
     * <p>Default settings:
     * <ul>
     *   <li>silentTypeErrors = false</li>
     *   <li>functionRegistry = DefaultFunctionRegistry</li>
     * </ul>
     *
     * @return the default configuration
     */
    public static RuntimeConfiguration defaultConfiguration() {
        return builder().build();
    }

    /**
     * Builder for RuntimeConfiguration.
     */
    public static final class Builder {
        private FunctionRegistry functionRegistry;
        private boolean silentTypeErrors;

        private Builder() {
            this.functionRegistry = new DefaultFunctionRegistry();
            this.silentTypeErrors = false;
        }

        /**
         * Builds the configuration.
         *
         * @return the configuration
         */
        public RuntimeConfiguration build() {
            return new RuntimeConfiguration(this);
        }

        /**
         * Sets the function registry.
         *
         * @param functionRegistry the registry to use
         * @return this builder
         */
        public Builder withFunctionRegistry(FunctionRegistry functionRegistry) {
            if (functionRegistry == null) {
                throw new IllegalArgumentException("functionRegistry cannot be null");
            }
            this.functionRegistry = functionRegistry;
            return this;
        }

        /**
         * Sets whether type errors should be silent.
         *
         * <p>When true, operations that would normally throw a type error
         * (e.g., accessing a property on a non-object) will return null instead.
         *
         * @param silentTypeErrors true to return null on type errors
         * @return this builder
         */
        public Builder withSilentTypeErrors(boolean silentTypeErrors) {
            this.silentTypeErrors = silentTypeErrors;
            return this;
        }
    }
}
