package io.jmespath;

import io.jmespath.runtime.MapRuntime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple benchmark tests comparing parsing and evaluation performance.
 *
 * These are not JMH microbenchmarks but give a rough comparison of performance.
 * Run with: mvn test -Dtest=BenchmarkTest -q
 */
public class BenchmarkTest {

    private static final int WARMUP_ITERATIONS = 1000;
    private static final int BENCHMARK_ITERATIONS = 10000;

    private final MapRuntime runtime = new MapRuntime();

    @Test
    void benchmarkSimpleField() {
        String expr = "foo";
        Object data = createNestedData();

        runBenchmark("Simple field (foo)", expr, data);
    }

    @Test
    void benchmarkDeepField() {
        String expr = "a.b.c.d.e";
        Object data = createNestedData();

        runBenchmark("Deep field (a.b.c.d.e)", expr, data);
    }

    @Test
    void benchmarkArrayIndex() {
        String expr = "items[0]";
        Object data = createArrayData(100);

        runBenchmark("Array index (items[0])", expr, data);
    }

    @Test
    void benchmarkArraySlice() {
        String expr = "items[0:10]";
        Object data = createArrayData(100);

        runBenchmark("Array slice (items[0:10])", expr, data);
    }

    @Test
    void benchmarkWildcard() {
        String expr = "items[*].value";
        Object data = createArrayData(100);

        runBenchmark("Wildcard (items[*].value)", expr, data);
    }

    @Test
    void benchmarkFilter() {
        String expr = "items[?value > `50`]";
        Object data = createArrayData(100);

        runBenchmark("Filter (items[?value > `50`])", expr, data);
    }

    @Test
    void benchmarkFunction() {
        String expr = "length(items)";
        Object data = createArrayData(100);

        runBenchmark("Function (length(items))", expr, data);
    }

    @Test
    void benchmarkSortBy() {
        String expr = "sort_by(items, &value)";
        Object data = createArrayData(50);

        runBenchmark("Sort by (sort_by(items, &value))", expr, data);
    }

    @Test
    void benchmarkComplexExpression() {
        String expr = "items[?value > `25`] | sort_by(@, &value) | [0:5].name";
        Object data = createArrayData(100);

        runBenchmark("Complex (filter | sort | slice)", expr, data);
    }

    @Test
    void benchmarkParseOnly() {
        String[] expressions = {
            "foo",
            "foo.bar.baz",
            "foo[0].bar[1].baz",
            "foo[*].bar[?x > `10`].baz",
            "sort_by(items[?active], &name) | [0:10]",
            "a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p"
        };

        System.out.println("\n=== Parse-only Benchmarks ===");
        for (String expr : expressions) {
            // Warmup
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                JmesPath.compile(expr);
            }

            // Benchmark
            long start = System.nanoTime();
            for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
                JmesPath.compile(expr);
            }
            long elapsed = System.nanoTime() - start;
            double avgNs = (double) elapsed / BENCHMARK_ITERATIONS;

            System.out.printf("  %-50s: %8.0f ns/op%n", truncate(expr, 50), avgNs);
        }
    }

    private void runBenchmark(String name, String expr, Object data) {
        // Compile once
        Expression<Object> compiled = JmesPath.compile(expr);

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            compiled.evaluate(runtime, data);
        }

        // Benchmark evaluation only
        long start = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            compiled.evaluate(runtime, data);
        }
        long elapsed = System.nanoTime() - start;
        double avgNs = (double) elapsed / BENCHMARK_ITERATIONS;

        // Benchmark parse + evaluate
        long parseStart = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            Expression<Object> e = JmesPath.compile(expr);
            e.evaluate(runtime, data);
        }
        long parseElapsed = System.nanoTime() - parseStart;
        double parseAvgNs = (double) parseElapsed / BENCHMARK_ITERATIONS;

        System.out.printf("%n=== %s ===%n", name);
        System.out.printf("  Evaluate only:     %8.0f ns/op%n", avgNs);
        System.out.printf("  Parse + Evaluate:  %8.0f ns/op%n", parseAvgNs);
        System.out.printf("  Parse overhead:    %8.0f ns/op (%.1f%%)%n",
            parseAvgNs - avgNs,
            (parseAvgNs - avgNs) / parseAvgNs * 100);
    }

    private Object createNestedData() {
        Map<String, Object> data = new HashMap<>();
        data.put("foo", "bar");

        Map<String, Object> nested = data;
        for (String key : new String[]{"a", "b", "c", "d", "e", "f", "g", "h"}) {
            Map<String, Object> child = new HashMap<>();
            nested.put(key, child);
            nested = child;
        }
        nested.put("value", 42);

        return data;
    }

    private Object createArrayData(int size) {
        Map<String, Object> data = new HashMap<>();
        List<Object> items = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", "item" + i);
            item.put("value", i);
            item.put("active", i % 2 == 0);
            items.add(item);
        }

        data.put("items", items);
        return data;
    }

    private String truncate(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }
}
