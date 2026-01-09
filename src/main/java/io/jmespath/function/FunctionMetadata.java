package io.jmespath.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata about a JMESPath function for documentation and introspection.
 *
 * <p>This class provides rich metadata similar to the function registry
 * used in jmespath_extensions for Rust.
 *
 * <p>Example:
 * <pre>{@code
 * FunctionMetadata meta = FunctionMetadata.builder("upper")
 *     .category("string")
 *     .description("Convert string to uppercase")
 *     .signature("string -> string")
 *     .example("upper('hello') -> \"HELLO\"", "Basic uppercase")
 *     .build();
 * }</pre>
 */
public final class FunctionMetadata {

    private final String name;
    private final String category;
    private final String description;
    private final String signatureDescription;
    private final List<Example> examples;
    private final List<String> aliases;
    private final boolean isStandard;

    private FunctionMetadata(Builder builder) {
        this.name = builder.name;
        this.category = builder.category;
        this.description = builder.description;
        this.signatureDescription = builder.signatureDescription;
        this.examples = Collections.unmodifiableList(new ArrayList<Example>(builder.examples));
        this.aliases = Collections.unmodifiableList(new ArrayList<String>(builder.aliases));
        this.isStandard = builder.isStandard;
    }

    /**
     * Returns the function name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the category (e.g., "string", "array", "datetime").
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns a description of what the function does.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a human-readable signature (e.g., "string, number -> string").
     */
    public String getSignatureDescription() {
        return signatureDescription;
    }

    /**
     * Returns usage examples.
     */
    public List<Example> getExamples() {
        return examples;
    }

    /**
     * Returns alternative names for this function.
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Returns true if this is a standard JMESPath function.
     */
    public boolean isStandard() {
        return isStandard;
    }

    /**
     * Creates a builder for function metadata.
     *
     * @param name the function name
     * @return a new builder
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * An example of function usage.
     */
    public static final class Example {
        private final String code;
        private final String description;

        public Example(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Builder for FunctionMetadata.
     */
    public static final class Builder {
        private final String name;
        private String category = "custom";
        private String description = "";
        private String signatureDescription = "";
        private List<Example> examples = new ArrayList<Example>();
        private List<String> aliases = new ArrayList<String>();
        private boolean isStandard = false;

        private Builder(String name) {
            this.name = name;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder signature(String signature) {
            this.signatureDescription = signature;
            return this;
        }

        public Builder example(String code, String description) {
            this.examples.add(new Example(code, description));
            return this;
        }

        public Builder alias(String alias) {
            this.aliases.add(alias);
            return this;
        }

        public Builder standard(boolean isStandard) {
            this.isStandard = isStandard;
            return this;
        }

        public FunctionMetadata build() {
            return new FunctionMetadata(this);
        }
    }
}
