package io.jmespath.function.extension;

import io.jmespath.Runtime;
import io.jmespath.function.ArgumentType;
import io.jmespath.function.Function;
import io.jmespath.function.FunctionBuilder;
import io.jmespath.function.Signature;

import java.util.ArrayList;
import java.util.List;

/**
 * String manipulation extensions for JMESPath.
 *
 * <p>This module provides common string functions not in the JMESPath spec:
 * <ul>
 *   <li>upper - Convert to uppercase</li>
 *   <li>lower - Convert to lowercase</li>
 *   <li>trim - Remove leading/trailing whitespace</li>
 *   <li>split - Split string by delimiter</li>
 *   <li>replace - Replace occurrences of a substring</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * ExtensionRegistry registry = ExtensionRegistry.builder()
 *     .withDefaults()
 *     .withModule(StringExtensions.functions())
 *     .build();
 *
 * Object result = JmesPath.search("upper(name)", data, registry);
 * }</pre>
 */
public final class StringExtensions {

    private StringExtensions() {}

    /**
     * Returns all string extension functions.
     */
    public static List<Function> functions() {
        List<Function> fns = new ArrayList<Function>();
        fns.add(upper());
        fns.add(lower());
        fns.add(trim());
        fns.add(trimLeft());
        fns.add(trimRight());
        fns.add(split());
        fns.add(replace());
        fns.add(padLeft());
        fns.add(padRight());
        return fns;
    }

    /**
     * upper(string) - Convert string to uppercase.
     */
    public static Function upper() {
        return FunctionBuilder.create("upper")
            .args(ArgumentType.STRING)
            .category("string")
            .description("Convert string to uppercase")
            .signatureDoc("string -> string")
            .example("upper('hello') -> \"HELLO\"", "Basic uppercase")
            .body(new FunctionBuilder.FunctionBody() {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                    String s = runtime.toString((T) args.get(0));
                    return runtime.createString(s.toUpperCase());
                }
            })
            .build();
    }

    /**
     * lower(string) - Convert string to lowercase.
     */
    public static Function lower() {
        return FunctionBuilder.create("lower")
            .args(ArgumentType.STRING)
            .category("string")
            .description("Convert string to lowercase")
            .signatureDoc("string -> string")
            .example("lower('HELLO') -> \"hello\"", "Basic lowercase")
            .body(new FunctionBuilder.FunctionBody() {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                    String s = runtime.toString((T) args.get(0));
                    return runtime.createString(s.toLowerCase());
                }
            })
            .build();
    }

    /**
     * trim(string) - Remove leading and trailing whitespace.
     */
    public static Function trim() {
        return FunctionBuilder.create("trim")
            .args(ArgumentType.STRING)
            .category("string")
            .description("Remove leading and trailing whitespace")
            .signatureDoc("string -> string")
            .example("trim('  hello  ') -> \"hello\"", "Remove whitespace")
            .body(new FunctionBuilder.FunctionBody() {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                    String s = runtime.toString((T) args.get(0));
                    return runtime.createString(s.trim());
                }
            })
            .build();
    }

    /**
     * trim_left(string) - Remove leading whitespace.
     */
    public static Function trimLeft() {
        return FunctionBuilder.create("trim_left")
            .args(ArgumentType.STRING)
            .category("string")
            .description("Remove leading whitespace")
            .signatureDoc("string -> string")
            .example("trim_left('  hello') -> \"hello\"", "Remove leading whitespace")
            .body(new FunctionBuilder.FunctionBody() {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                    String s = runtime.toString((T) args.get(0));
                    int start = 0;
                    while (start < s.length() && Character.isWhitespace(s.charAt(start))) {
                        start++;
                    }
                    return runtime.createString(s.substring(start));
                }
            })
            .build();
    }

    /**
     * trim_right(string) - Remove trailing whitespace.
     */
    public static Function trimRight() {
        return FunctionBuilder.create("trim_right")
            .args(ArgumentType.STRING)
            .category("string")
            .description("Remove trailing whitespace")
            .signatureDoc("string -> string")
            .example("trim_right('hello  ') -> \"hello\"", "Remove trailing whitespace")
            .body(new FunctionBuilder.FunctionBody() {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                    String s = runtime.toString((T) args.get(0));
                    int end = s.length();
                    while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) {
                        end--;
                    }
                    return runtime.createString(s.substring(0, end));
                }
            })
            .build();
    }

    /**
     * split(string, delimiter) - Split string by delimiter.
     */
    public static Function split() {
        return new Function() {
            @Override
            public String getName() {
                return "split";
            }

            @Override
            public Signature getSignature() {
                return Signature.builder()
                    .required(ArgumentType.STRING)
                    .required(ArgumentType.STRING)
                    .build();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                String s = runtime.toString((T) args.get(0));
                String delimiter = runtime.toString((T) args.get(1));

                List<T> result = new ArrayList<T>();
                if (delimiter.isEmpty()) {
                    // Split into characters
                    for (int i = 0; i < s.length(); i++) {
                        result.add(runtime.createString(String.valueOf(s.charAt(i))));
                    }
                } else {
                    int start = 0;
                    int idx;
                    while ((idx = s.indexOf(delimiter, start)) != -1) {
                        result.add(runtime.createString(s.substring(start, idx)));
                        start = idx + delimiter.length();
                    }
                    result.add(runtime.createString(s.substring(start)));
                }
                return runtime.createArray(result);
            }
        };
    }

    /**
     * replace(string, old, new) - Replace occurrences of a substring.
     */
    public static Function replace() {
        return new Function() {
            @Override
            public String getName() {
                return "replace";
            }

            @Override
            public Signature getSignature() {
                return Signature.builder()
                    .required(ArgumentType.STRING)
                    .required(ArgumentType.STRING)
                    .required(ArgumentType.STRING)
                    .build();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                String s = runtime.toString((T) args.get(0));
                String oldStr = runtime.toString((T) args.get(1));
                String newStr = runtime.toString((T) args.get(2));
                return runtime.createString(s.replace(oldStr, newStr));
            }
        };
    }

    /**
     * pad_left(string, width, char) - Pad string on left to width.
     */
    public static Function padLeft() {
        return new Function() {
            @Override
            public String getName() {
                return "pad_left";
            }

            @Override
            public Signature getSignature() {
                return Signature.builder()
                    .required(ArgumentType.STRING)
                    .required(ArgumentType.NUMBER)
                    .required(ArgumentType.STRING)
                    .build();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                String s = runtime.toString((T) args.get(0));
                int width = runtime.toNumber((T) args.get(1)).intValue();
                String pad = runtime.toString((T) args.get(2));

                if (s.length() >= width || pad.isEmpty()) {
                    return runtime.createString(s);
                }

                StringBuilder sb = new StringBuilder();
                while (sb.length() + s.length() < width) {
                    sb.append(pad);
                }
                // Trim if we added too much
                if (sb.length() + s.length() > width) {
                    sb.setLength(width - s.length());
                }
                sb.append(s);
                return runtime.createString(sb.toString());
            }
        };
    }

    /**
     * pad_right(string, width, char) - Pad string on right to width.
     */
    public static Function padRight() {
        return new Function() {
            @Override
            public String getName() {
                return "pad_right";
            }

            @Override
            public Signature getSignature() {
                return Signature.builder()
                    .required(ArgumentType.STRING)
                    .required(ArgumentType.NUMBER)
                    .required(ArgumentType.STRING)
                    .build();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                String s = runtime.toString((T) args.get(0));
                int width = runtime.toNumber((T) args.get(1)).intValue();
                String pad = runtime.toString((T) args.get(2));

                if (s.length() >= width || pad.isEmpty()) {
                    return runtime.createString(s);
                }

                StringBuilder sb = new StringBuilder(s);
                while (sb.length() < width) {
                    sb.append(pad);
                }
                // Trim if we added too much
                if (sb.length() > width) {
                    sb.setLength(width);
                }
                return runtime.createString(sb.toString());
            }
        };
    }
}
