package io.jmespath.function;

import io.jmespath.JmesPathException;
import io.jmespath.Runtime;
import io.jmespath.Type;
import io.jmespath.internal.node.ExpressionRefNode;
import io.jmespath.internal.node.FunctionCallNode.ScopedExpressionRef;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes and validates function arguments.
 *
 * <p>A signature specifies the expected types for each argument position.
 * Arguments can be required, optional, or variadic.
 *
 * <p>Example signatures:
 * <pre>{@code
 * // length(subject) - one required argument of type string, array, or object
 * Signature.of(ArgumentType.STRING, ArgumentType.ARRAY, ArgumentType.OBJECT);
 *
 * // contains(subject, search) - two required arguments
 * Signature.builder()
 *     .required(ArgumentType.STRING, ArgumentType.ARRAY)
 *     .required(ArgumentType.ANY)
 *     .build();
 *
 * // not_null(arg, ...) - one or more arguments
 * Signature.builder()
 *     .required(ArgumentType.ANY)
 *     .variadic(ArgumentType.ANY)
 *     .build();
 * }</pre>
 */
public final class Signature {

    private final List<Argument> arguments;
    private final boolean hasVariadic;

    private Signature(List<Argument> arguments, boolean hasVariadic) {
        this.arguments = arguments;
        this.hasVariadic = hasVariadic;
    }

    /**
     * Creates a signature with a single required argument accepting the given types.
     *
     * @param types the accepted types (any of these types is valid)
     * @return the signature
     */
    public static Signature of(ArgumentType... types) {
        List<Argument> args = new ArrayList<Argument>();
        args.add(new Argument(types, false));
        return new Signature(args, false);
    }

    /**
     * Creates a new signature builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Validates arguments against this signature.
     *
     * @param <T> the JSON value type
     * @param functionName the function name (for error messages)
     * @param runtime the runtime
     * @param args the arguments to validate
     * @throws JmesPathException if validation fails
     */
    public <T> void validate(
        String functionName,
        Runtime<T> runtime,
        List<Object> args
    ) {
        int minArgs = 0;
        int maxArgs = hasVariadic ? Integer.MAX_VALUE : arguments.size();

        for (int i = 0; i < arguments.size(); i++) {
            if (!arguments.get(i).optional) {
                minArgs = i + 1;
            }
        }

        // Check arity
        if (args.size() < minArgs) {
            throw new JmesPathException(
                functionName +
                    "() takes at least " +
                    minArgs +
                    " argument(s), got " +
                    args.size(),
                JmesPathException.ErrorType.ARITY
            );
        }
        if (args.size() > maxArgs) {
            throw new JmesPathException(
                functionName +
                    "() takes at most " +
                    maxArgs +
                    " argument(s), got " +
                    args.size(),
                JmesPathException.ErrorType.ARITY
            );
        }

        // Check types
        for (int i = 0; i < args.size(); i++) {
            Argument arg;
            if (i < arguments.size()) {
                arg = arguments.get(i);
            } else if (hasVariadic) {
                arg = arguments.get(arguments.size() - 1);
            } else {
                break;
            }

            Object value = args.get(i);
            if (!matchesType(runtime, value, arg.types)) {
                throw new JmesPathException(
                    functionName +
                        "() argument " +
                        (i + 1) +
                        " must be " +
                        describeTypes(arg.types) +
                        ", got " +
                        describeValue(runtime, value),
                    JmesPathException.ErrorType.TYPE
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> boolean matchesType(
        Runtime<T> runtime,
        Object value,
        ArgumentType[] types
    ) {
        for (int i = 0; i < types.length; i++) {
            ArgumentType type = types[i];
            switch (type) {
                case ANY:
                    return true;
                case STRING:
                    if (runtime.isString((T) value)) return true;
                    break;
                case NUMBER:
                    if (runtime.isNumber((T) value)) return true;
                    break;
                case BOOLEAN:
                    if (runtime.isBoolean((T) value)) return true;
                    break;
                case ARRAY:
                    if (runtime.isArray((T) value)) return true;
                    break;
                case OBJECT:
                    if (runtime.isObject((T) value)) return true;
                    break;
                case NULL:
                    if (runtime.isNull((T) value)) return true;
                    break;
                case ARRAY_NUMBER:
                    if (isArrayOf(runtime, (T) value, Type.NUMBER)) return true;
                    break;
                case ARRAY_STRING:
                    if (isArrayOf(runtime, (T) value, Type.STRING)) return true;
                    break;
                case EXPRESSION_REF:
                    if (value instanceof ExpressionRefNode) return true;
                    if (value instanceof ScopedExpressionRef) return true;
                    break;
            }
        }
        return false;
    }

    private <T> boolean isArrayOf(
        Runtime<T> runtime,
        T value,
        Type elementType
    ) {
        if (!runtime.isArray(value)) {
            return false;
        }
        for (T elem : runtime.getArrayElements(value)) {
            if (runtime.typeOf(elem) != elementType) {
                return false;
            }
        }
        return true;
    }

    private String describeTypes(ArgumentType[] types) {
        if (types.length == 1) {
            return types[0].description();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                if (i == types.length - 1) {
                    sb.append(" or ");
                } else {
                    sb.append(", ");
                }
            }
            sb.append(types[i].description());
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> String describeValue(Runtime<T> runtime, Object value) {
        if (value instanceof ExpressionRefNode) {
            return "expression";
        }
        if (value instanceof ScopedExpressionRef) {
            return "expression";
        }
        return runtime.typeOf((T) value).typeName();
    }

    /**
     * A single argument in a signature.
     */
    private static final class Argument {

        final ArgumentType[] types;
        final boolean optional;

        Argument(ArgumentType[] types, boolean optional) {
            this.types = types;
            this.optional = optional;
        }
    }

    /**
     * Builder for creating signatures.
     */
    public static final class Builder {

        private final List<Argument> arguments = new ArrayList<Argument>();
        private boolean hasVariadic = false;

        private Builder() {}

        /**
         * Adds a required argument accepting the given types.
         *
         * @param types the accepted types
         * @return this builder
         */
        public Builder required(ArgumentType... types) {
            if (hasVariadic) {
                throw new IllegalStateException(
                    "Cannot add arguments after variadic"
                );
            }
            arguments.add(new Argument(types, false));
            return this;
        }

        /**
         * Adds an optional argument accepting the given types.
         *
         * @param types the accepted types
         * @return this builder
         */
        public Builder optional(ArgumentType... types) {
            if (hasVariadic) {
                throw new IllegalStateException(
                    "Cannot add arguments after variadic"
                );
            }
            arguments.add(new Argument(types, true));
            return this;
        }

        /**
         * Adds a variadic argument accepting any number of additional arguments.
         *
         * <p>This must be the last argument in the signature.
         *
         * @param types the accepted types for each additional argument
         * @return this builder
         */
        public Builder variadic(ArgumentType... types) {
            if (hasVariadic) {
                throw new IllegalStateException(
                    "Only one variadic argument allowed"
                );
            }
            arguments.add(new Argument(types, true));
            hasVariadic = true;
            return this;
        }

        /**
         * Builds the signature.
         *
         * @return the signature
         */
        public Signature build() {
            return new Signature(
                new ArrayList<Argument>(arguments),
                hasVariadic
            );
        }
    }
}
