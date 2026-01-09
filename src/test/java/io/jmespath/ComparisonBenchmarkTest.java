package io.jmespath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.burt.jmespath.jackson.JacksonRuntime;
import io.jmespath.runtime.MapRuntime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Benchmark comparing our implementation against the archived burtcorp jmespath-java.
 *
 * Run with: mvn test -Dtest=ComparisonBenchmarkTest -Djacoco.skip=true -q
 */
public class ComparisonBenchmarkTest {

    private static final int WARMUP_ITERATIONS = 1000;
    private static final int BENCHMARK_ITERATIONS = 10000;

    // Our implementation
    private final MapRuntime ourRuntime = new MapRuntime();

    // Archived implementation (using Jackson runtime since JCF is not published)
    private final JacksonRuntime burtRuntime = new JacksonRuntime();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void comparisonBenchmark() throws Exception {
        Object ourData = createArrayData(100);
        JsonNode burtData = objectMapper.valueToTree(ourData);

        String[] expressions = {
            "foo",
            "a.b.c.d.e",
            "items[0]",
            "items[0:10]",
            "items[*].value",
            "items[?value > `50`]",
            "length(items)",
        };

        System.out.println("\n========================================");
        System.out.println("JMESPath Implementation Comparison");
        System.out.println("========================================");
        System.out.println("Iterations: " + BENCHMARK_ITERATIONS);
        System.out.println();
        System.out.printf(
            "%-35s %15s %15s %10s%n",
            "Expression",
            "Ours (ns/op)",
            "Burt (ns/op)",
            "Ratio"
        );
        System.out.println("-".repeat(80));

        for (String expr : expressions) {
            runComparison(expr, ourData, burtData);
        }

        System.out.println("-".repeat(80));
        System.out.println("\nRatio < 1.0 means our implementation is faster");
    }

    private void runComparison(String expr, Object ourData, JsonNode burtData) {
        // Compile expressions
        Expression<Object> ourExpr = JmesPath.compile(expr);
        io.burt.jmespath.Expression<JsonNode> burtExpr = burtRuntime.compile(
            expr
        );

        // Warmup - ours
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            ourExpr.evaluate(ourRuntime, ourData);
        }

        // Warmup - burt
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            burtExpr.search(burtData);
        }

        // Benchmark - ours
        long ourStart = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            ourExpr.evaluate(ourRuntime, ourData);
        }
        long ourElapsed = System.nanoTime() - ourStart;
        double ourNsPerOp = (double) ourElapsed / BENCHMARK_ITERATIONS;

        // Benchmark - burt
        long burtStart = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            burtExpr.search(burtData);
        }
        long burtElapsed = System.nanoTime() - burtStart;
        double burtNsPerOp = (double) burtElapsed / BENCHMARK_ITERATIONS;

        double ratio = ourNsPerOp / burtNsPerOp;
        String indicator = ratio < 1.0 ? " <-- faster" : "";

        System.out.printf(
            "%-35s %12.0f ns %12.0f ns %9.2fx%s%n",
            truncate(expr, 35),
            ourNsPerOp,
            burtNsPerOp,
            ratio,
            indicator
        );
    }

    @Test
    void parseComparisonBenchmark() {
        String[] expressions = {
            "foo",
            "foo.bar.baz",
            "foo[0].bar[1].baz",
            "foo[*].bar[?x > `10`].baz",
            "sort_by(items[?active], &name) | [0:10]",
            "a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p",
        };

        System.out.println("\n========================================");
        System.out.println("Parse-Only Comparison");
        System.out.println("========================================");
        System.out.println("Iterations: " + BENCHMARK_ITERATIONS);
        System.out.println();
        System.out.printf(
            "%-50s %12s %12s %10s%n",
            "Expression",
            "Ours (ns)",
            "Burt (ns)",
            "Ratio"
        );
        System.out.println("-".repeat(90));

        for (String expr : expressions) {
            // Warmup - ours
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                JmesPath.compile(expr);
            }

            // Warmup - burt
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                burtRuntime.compile(expr);
            }

            // Benchmark - ours
            long ourStart = System.nanoTime();
            for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
                JmesPath.compile(expr);
            }
            long ourElapsed = System.nanoTime() - ourStart;
            double ourNsPerOp = (double) ourElapsed / BENCHMARK_ITERATIONS;

            // Benchmark - burt
            long burtStart = System.nanoTime();
            for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
                burtRuntime.compile(expr);
            }
            long burtElapsed = System.nanoTime() - burtStart;
            double burtNsPerOp = (double) burtElapsed / BENCHMARK_ITERATIONS;

            double ratio = ourNsPerOp / burtNsPerOp;
            String indicator = ratio < 1.0 ? " <-- faster" : "";

            System.out.printf(
                "%-50s %9.0f ns %9.0f ns %9.2fx%s%n",
                truncate(expr, 50),
                ourNsPerOp,
                burtNsPerOp,
                ratio,
                indicator
            );
        }

        System.out.println("-".repeat(90));
    }

    @Test
    void largeDataBenchmark() throws Exception {
        System.out.println("\n========================================");
        System.out.println("Large Data Comparison");
        System.out.println("========================================");
        System.out.println();

        int[] sizes = { 100, 1000, 10000 };
        String expr = "items[?value > `50`].name";

        System.out.printf(
            "%-15s %15s %15s %10s%n",
            "Array Size",
            "Ours (ns/op)",
            "Burt (ns/op)",
            "Ratio"
        );
        System.out.println("-".repeat(60));

        for (int size : sizes) {
            Object ourData = createArrayData(size);
            JsonNode burtData = objectMapper.valueToTree(ourData);

            Expression<Object> ourExpr = JmesPath.compile(expr);
            io.burt.jmespath.Expression<JsonNode> burtExpr =
                burtRuntime.compile(expr);

            int iterations = size >= 10000 ? 100 : 1000;

            // Warmup
            for (int i = 0; i < iterations / 10; i++) {
                ourExpr.evaluate(ourRuntime, ourData);
                burtExpr.search(burtData);
            }

            // Benchmark - ours
            long ourStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                ourExpr.evaluate(ourRuntime, ourData);
            }
            long ourElapsed = System.nanoTime() - ourStart;
            double ourNsPerOp = (double) ourElapsed / iterations;

            // Benchmark - burt
            long burtStart = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                burtExpr.search(burtData);
            }
            long burtElapsed = System.nanoTime() - burtStart;
            double burtNsPerOp = (double) burtElapsed / iterations;

            double ratio = ourNsPerOp / burtNsPerOp;
            String indicator = ratio < 1.0 ? " <-- faster" : "";

            System.out.printf(
                "%-15d %12.0f ns %12.0f ns %9.2fx%s%n",
                size,
                ourNsPerOp,
                burtNsPerOp,
                ratio,
                indicator
            );
        }

        System.out.println("-".repeat(60));
    }

    private Object createArrayData(int size) {
        Map<String, Object> data = new HashMap<>();
        data.put("foo", "bar");

        // Nested structure for a.b.c.d.e
        Map<String, Object> nested = data;
        for (String key : new String[] {
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h",
        }) {
            Map<String, Object> child = new HashMap<>();
            nested.put(key, child);
            nested = child;
        }
        nested.put("value", 42);

        // Array data
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
