package io.jmespath.function.builtin;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.Signature;

import java.util.List;

/**
 * Implements the to_string() function.
 * Converts a value to its JSON string representation.
 */
public final class ToStringFunction implements Function {
    private static final Signature SIGNATURE = Signature.builder()
        .required(ArgumentType.ANY)
        .build();

    @Override
    public String getName() {
        return "to_string";
    }

    @Override
    public Signature getSignature() {
        return SIGNATURE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        T value = (T) args.get(0);

        if (runtime.isString(value)) {
            return value;
        }

        // Convert to JSON representation
        String json = toJson(runtime, value);
        return runtime.createString(json);
    }

    private <T> String toJson(Runtime<T> runtime, T value) {
        if (runtime.isNull(value)) {
            return "null";
        }
        if (runtime.isBoolean(value)) {
            return runtime.toBoolean(value) ? "true" : "false";
        }
        if (runtime.isNumber(value)) {
            Number num = runtime.toNumber(value);
            if (num instanceof Double) {
                double d = num.doubleValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
            }
            return num.toString();
        }
        if (runtime.isString(value)) {
            return "\"" + escapeJson(runtime.toString(value)) + "\"";
        }
        if (runtime.isArray(value)) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            boolean first = true;
            for (T elem : runtime.getArrayElements(value)) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append(toJson(runtime, elem));
            }
            sb.append(']');
            return sb.toString();
        }
        if (runtime.isObject(value)) {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            boolean first = true;
            for (String key : runtime.getObjectKeys(value)) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append('"').append(escapeJson(key)).append("\":");
                sb.append(toJson(runtime, runtime.getProperty(value, key)));
            }
            sb.append('}');
            return sb.toString();
        }
        return "null";
    }

    private String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
