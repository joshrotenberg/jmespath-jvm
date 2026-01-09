package io.jmespath;

import io.jmespath.function.DefaultFunctionRegistry;
import io.jmespath.runtime.MapRuntime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RuntimeConfiguration and silent type errors.
 */
public class RuntimeConfigurationTest {

    @Nested
    class BuilderTests {

        @Test
        void defaultConfigurationHasSilentErrorsFalse() {
            RuntimeConfiguration config = RuntimeConfiguration.defaultConfiguration();
            assertFalse(config.isSilentTypeErrors());
        }

        @Test
        void builderCanEnableSilentErrors() {
            RuntimeConfiguration config = RuntimeConfiguration.builder()
                .withSilentTypeErrors(true)
                .build();
            assertTrue(config.isSilentTypeErrors());
        }

        @Test
        void builderCanSetFunctionRegistry() {
            DefaultFunctionRegistry registry = new DefaultFunctionRegistry();
            RuntimeConfiguration config = RuntimeConfiguration.builder()
                .withFunctionRegistry(registry)
                .build();
            assertSame(registry, config.getFunctionRegistry());
        }

        @Test
        void builderRejectsNullFunctionRegistry() {
            assertThrows(IllegalArgumentException.class, () -> {
                RuntimeConfiguration.builder().withFunctionRegistry(null);
            });
        }
    }

    @Nested
    class SilentTypeErrorsDisabledTests {

        private Object eval(String expression, String json) {
            MapRuntime runtime = new MapRuntime();
            Expression<Object> expr = JmesPath.compile(expression);
            Object data = JmesPath.parseJson(json);
            return expr.evaluate(runtime, data);
        }

        @Test
        void sortByMixedTypesThrows() {
            String json = "{\"items\": [{\"key\": \"a\"}, {\"key\": 1}]}";
            assertThrows(JmesPathException.class, () -> {
                eval("sort_by(items, &key)", json);
            });
        }

        @Test
        void minByMixedTypesThrows() {
            String json = "{\"items\": [{\"key\": \"a\"}, {\"key\": 1}]}";
            assertThrows(JmesPathException.class, () -> {
                eval("min_by(items, &key)", json);
            });
        }

        @Test
        void maxByMixedTypesThrows() {
            String json = "{\"items\": [{\"key\": \"a\"}, {\"key\": 1}]}";
            assertThrows(JmesPathException.class, () -> {
                eval("max_by(items, &key)", json);
            });
        }

        @Test
        void functionWithWrongArgumentTypeThrows() {
            String json = "{\"items\": 123}";
            assertThrows(JmesPathException.class, () -> {
                eval("length(items)", json);  // length expects string/array/object, not number
            });
        }
    }

    @Nested
    class SilentTypeErrorsEnabledTests {

        private Object eval(String expression, String json) {
            RuntimeConfiguration config = RuntimeConfiguration.builder()
                .withSilentTypeErrors(true)
                .build();
            MapRuntime runtime = new MapRuntime(config);
            Expression<Object> expr = JmesPath.compile(expression);
            Object data = runtime.parseJson(json);
            return expr.evaluate(runtime, data);
        }

        @Test
        void sortByMixedTypesReturnsNull() {
            String json = "{\"items\": [{\"key\": \"a\"}, {\"key\": 1}]}";
            Object result = eval("sort_by(items, &key)", json);
            assertNull(result);
        }

        @Test
        void minByMixedTypesReturnsNull() {
            String json = "{\"items\": [{\"key\": \"a\"}, {\"key\": 1}]}";
            Object result = eval("min_by(items, &key)", json);
            assertNull(result);
        }

        @Test
        void maxByMixedTypesReturnsNull() {
            String json = "{\"items\": [{\"key\": \"a\"}, {\"key\": 1}]}";
            Object result = eval("max_by(items, &key)", json);
            assertNull(result);
        }

        @Test
        void functionWithWrongArgumentTypeReturnsNull() {
            String json = "{\"items\": 123}";
            Object result = eval("length(items)", json);  // length expects string/array/object, not number
            assertNull(result);
        }

        @Test
        void sortByWithNonStringNonNumberReturnsNull() {
            String json = "{\"items\": [{\"key\": [1, 2]}, {\"key\": [3, 4]}]}";
            Object result = eval("sort_by(items, &key)", json);  // key is an array, not string/number
            assertNull(result);
        }

        @Test
        void validOperationsStillWork() {
            String json = "{\"items\": [{\"key\": \"b\"}, {\"key\": \"a\"}]}";
            Object result = eval("sort_by(items, &key)", json);
            assertNotNull(result);
        }
    }

    @Nested
    class MapRuntimeIntegrationTests {

        @Test
        void mapRuntimeDefaultsToNotSilent() {
            MapRuntime runtime = new MapRuntime();
            assertFalse(runtime.isSilentTypeErrors());
        }

        @Test
        void mapRuntimeRespectsConfiguration() {
            RuntimeConfiguration config = RuntimeConfiguration.builder()
                .withSilentTypeErrors(true)
                .build();
            MapRuntime runtime = new MapRuntime(config);
            assertTrue(runtime.isSilentTypeErrors());
        }

        @Test
        void mapRuntimeWithFunctionRegistryConstructor() {
            DefaultFunctionRegistry registry = new DefaultFunctionRegistry();
            MapRuntime runtime = new MapRuntime(registry);
            assertFalse(runtime.isSilentTypeErrors());
            assertSame(registry, runtime.getFunctionRegistry());
        }
    }
}
