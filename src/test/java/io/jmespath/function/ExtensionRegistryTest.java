package io.jmespath.function;

import io.jmespath.JmesPath;
import io.jmespath.Runtime;
import io.jmespath.function.extension.StringExtensions;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the extension infrastructure.
 */
class ExtensionRegistryTest {

    @Nested
    class BuilderTests {

        @Test
        void emptyRegistry() {
            ExtensionRegistry registry = ExtensionRegistry.builder().build();
            assertEquals(0, registry.size());
            assertFalse(registry.hasFunction("length"));
        }

        @Test
        void withDefaults() {
            ExtensionRegistry registry = ExtensionRegistry.builder()
                .withDefaults()
                .build();

            assertEquals(26, registry.size());
            assertTrue(registry.hasFunction("length"));
            assertTrue(registry.hasFunction("sort_by"));
            assertTrue(registry.hasFunction("abs"));
        }

        @Test
        void registerCustomFunction() {
            Function customFn = FunctionBuilder.create("double")
                .args(ArgumentType.NUMBER)
                .body(new FunctionBuilder.FunctionBody() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                        Number n = runtime.toNumber((T) args.get(0));
                        return runtime.createNumber(n.doubleValue() * 2);
                    }
                })
                .build();

            ExtensionRegistry registry = ExtensionRegistry.builder()
                .withDefaults()
                .register(customFn)
                .build();

            assertEquals(27, registry.size());
            assertTrue(registry.hasFunction("double"));

            // Test it works
            Object result = JmesPath.search("double(@)", 21L, registry);
            assertEquals(42.0, result);
        }

        @Test
        void withModule() {
            ExtensionRegistry registry = ExtensionRegistry.builder()
                .withDefaults()
                .withModule(StringExtensions.functions())
                .build();

            assertTrue(registry.hasFunction("upper"));
            assertTrue(registry.hasFunction("lower"));
            assertTrue(registry.hasFunction("trim"));
            assertTrue(registry.hasFunction("split"));
        }

        @Test
        void aliasFunction() {
            ExtensionRegistry registry = ExtensionRegistry.builder()
                .withDefaults()
                .alias("size", "length")
                .build();

            assertTrue(registry.hasFunction("length"));
            assertTrue(registry.hasFunction("size"));

            // Both should work the same
            Object result1 = JmesPath.search("length(@)", "hello", registry);
            Object result2 = JmesPath.search("size(@)", "hello", registry);
            assertEquals(result1, result2);
        }

        @Test
        void composeRegistries() {
            ExtensionRegistry strings = ExtensionRegistry.builder()
                .withModule(StringExtensions.functions())
                .build();

            ExtensionRegistry combined = ExtensionRegistry.builder()
                .withDefaults()
                .withRegistry(strings)
                .build();

            assertTrue(combined.hasFunction("length"));  // from defaults
            assertTrue(combined.hasFunction("upper"));   // from strings
        }
    }

    @Nested
    class StringExtensionTests {

        private final ExtensionRegistry registry = ExtensionRegistry.builder()
            .withDefaults()
            .withModule(StringExtensions.functions())
            .build();

        @Test
        void upper() {
            Object result = JmesPath.search("upper(@)", "hello", registry);
            assertEquals("HELLO", result);
        }

        @Test
        void lower() {
            Object result = JmesPath.search("lower(@)", "HELLO", registry);
            assertEquals("hello", result);
        }

        @Test
        void trim() {
            Object result = JmesPath.search("trim(@)", "  hello  ", registry);
            assertEquals("hello", result);
        }

        @Test
        void trimLeft() {
            Object result = JmesPath.search("trim_left(@)", "  hello", registry);
            assertEquals("hello", result);
        }

        @Test
        void trimRight() {
            Object result = JmesPath.search("trim_right(@)", "hello  ", registry);
            assertEquals("hello", result);
        }

        @Test
        @SuppressWarnings("unchecked")
        void split() {
            Object result = JmesPath.search("split(@, ',')", "a,b,c", registry);
            assertTrue(result instanceof List);
            List<String> list = (List<String>) result;
            assertEquals(3, list.size());
            assertEquals("a", list.get(0));
            assertEquals("b", list.get(1));
            assertEquals("c", list.get(2));
        }

        @Test
        void replace() {
            Object result = JmesPath.search("replace(@, 'l', 'L')", "hello", registry);
            assertEquals("heLLo", result);
        }

        @Test
        void padLeft() {
            Object result = JmesPath.search("pad_left(@, `5`, '0')", "42", registry);
            assertEquals("00042", result);
        }

        @Test
        void padRight() {
            Object result = JmesPath.search("pad_right(@, `5`, '.')", "hi", registry);
            assertEquals("hi...", result);
        }

        @Test
        void chainedExtensions() {
            Object result = JmesPath.search("upper(trim(@))", "  hello  ", registry);
            assertEquals("HELLO", result);
        }
    }

    @Nested
    class FunctionBuilderTests {

        @Test
        void simpleFunction() {
            Function fn = FunctionBuilder.create("greet")
                .args(ArgumentType.STRING)
                .body(new FunctionBuilder.FunctionBody() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                        String name = runtime.toString((T) args.get(0));
                        return runtime.createString("Hello, " + name + "!");
                    }
                })
                .build();

            assertEquals("greet", fn.getName());

            ExtensionRegistry registry = ExtensionRegistry.builder()
                .register(fn)
                .build();

            Object result = JmesPath.search("greet(@)", "World", registry);
            assertEquals("Hello, World!", result);
        }

        @Test
        void functionWithMetadata() {
            FunctionBuilder.FunctionWithMetadata fnWithMeta = FunctionBuilder.create("example")
                .args(ArgumentType.ANY)
                .category("demo")
                .description("An example function")
                .signatureDoc("any -> string")
                .example("example('test') -> \"demo: test\"", "Basic usage")
                .body(new FunctionBuilder.FunctionBody() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                        return runtime.createString("demo");
                    }
                })
                .buildWithMetadata();

            FunctionMetadata meta = fnWithMeta.getMetadata();
            assertEquals("example", meta.getName());
            assertEquals("demo", meta.getCategory());
            assertEquals("An example function", meta.getDescription());
            assertEquals("any -> string", meta.getSignatureDescription());
            assertEquals(1, meta.getExamples().size());
        }

        @Test
        void variadicFunction() {
            Function concat = FunctionBuilder.create("concat_all")
                .required(ArgumentType.STRING)
                .variadic(ArgumentType.STRING)
                .body(new FunctionBuilder.FunctionBody() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                        StringBuilder sb = new StringBuilder();
                        for (Object arg : args) {
                            sb.append(runtime.toString((T) arg));
                        }
                        return runtime.createString(sb.toString());
                    }
                })
                .build();

            ExtensionRegistry registry = ExtensionRegistry.builder()
                .register(concat)
                .build();

            Object result = JmesPath.search("concat_all('a', 'b', 'c')", null, registry);
            assertEquals("abc", result);
        }
    }

    @Nested
    class MetadataTests {

        @Test
        void getFunctionNames() {
            ExtensionRegistry registry = ExtensionRegistry.builder()
                .withDefaults()
                .build();

            assertEquals(26, registry.getFunctionNames().size());
            assertTrue(registry.getFunctionNames().contains("length"));
        }

        @Test
        void getFunction() {
            ExtensionRegistry registry = ExtensionRegistry.builder()
                .withDefaults()
                .build();

            Function fn = registry.getFunction("length");
            assertNotNull(fn);
            assertEquals("length", fn.getName());
        }
    }
}
