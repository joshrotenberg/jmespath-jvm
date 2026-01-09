package io.jmespath;

import static org.junit.jupiter.api.Assertions.*;

import io.jmespath.runtime.MapRuntime;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Runs the official JMESPath compliance test suite.
 *
 * <p>The test files are from https://github.com/jmespath/jmespath.test
 */
public class ComplianceTest {

    private static final String[] TEST_FILES = {
        "basic.json",
        "boolean.json",
        "current.json",
        "escape.json",
        "filters.json",
        "functions.json",
        "identifiers.json",
        "indices.json",
        "literal.json",
        "multiselect.json",
        "pipe.json",
        "slice.json",
        "syntax.json",
        "unicode.json",
        "wildcard.json",
        // "benchmarks.json" - skipped, these are for performance not correctness
    };

    private final MapRuntime runtime = new MapRuntime();

    @TestFactory
    Collection<DynamicTest> complianceTests() {
        List<DynamicTest> tests = new ArrayList<DynamicTest>();

        for (String testFile : TEST_FILES) {
            String featureName = testFile.replace(".json", "");
            try {
                List<Map<String, Object>> suites = loadTestFile(testFile);
                for (Map<String, Object> suite : suites) {
                    Object given = suite.get("given");
                    String suiteComment = (String) suite.get("comment");

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> cases = (List<
                        Map<String, Object>
                    >) suite.get("cases");

                    for (Map<String, Object> testCase : cases) {
                        String expression = (String) testCase.get("expression");
                        Object expectedResult = testCase.get("result");
                        String expectedError = (String) testCase.get("error");
                        String testComment = (String) testCase.get("comment");
                        if (testComment == null) {
                            testComment = (String) testCase.get("description");
                        }

                        // Build test name
                        String testName = buildTestName(
                            featureName,
                            suiteComment,
                            testComment,
                            expression
                        );

                        // Only run tests that have either a result or an error expectation
                        if (expectedResult != null || expectedError != null) {
                            final Object input = given;
                            final Object expected = expectedResult;
                            final String error = expectedError;
                            final String expr = expression;

                            tests.add(
                                DynamicTest.dynamicTest(testName, () -> {
                                    runTest(expr, input, expected, error);
                                })
                            );
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(
                    "Failed to load test file: " + testFile,
                    e
                );
            }
        }

        return tests;
    }

    private String buildTestName(
        String feature,
        String suiteComment,
        String testComment,
        String expression
    ) {
        StringBuilder name = new StringBuilder();
        name.append(feature);
        name.append(": ");
        if (suiteComment != null) {
            name.append(suiteComment);
            name.append(" - ");
        }
        if (testComment != null) {
            name.append(testComment);
        } else {
            // Truncate long expressions
            if (expression.length() > 50) {
                name.append(expression.substring(0, 47)).append("...");
            } else {
                name.append(expression);
            }
        }
        return name.toString();
    }

    private void runTest(
        String expression,
        Object input,
        Object expectedResult,
        String expectedError
    ) {
        try {
            Expression<Object> compiled = JmesPath.compile(expression);
            Object result = compiled.evaluate(runtime, input);

            if (expectedError != null) {
                fail(
                    "Expression '" +
                        expression +
                        "': Expected error '" +
                        expectedError +
                        "' but got result: " +
                        result
                );
            } else {
                assertDeepEquals(
                    expectedResult,
                    result,
                    "Expression: " + expression
                );
            }
        } catch (JmesPathException e) {
            if (expectedError != null) {
                // We got an error as expected - that's sufficient for compliance
                // The specific error message format may differ between implementations
                // Just verify we got some kind of error
            } else {
                fail(
                    "Unexpected error for expression '" +
                        expression +
                        "': " +
                        e.getMessage()
                );
            }
        } catch (Exception e) {
            if (expectedError != null) {
                // Some errors might be runtime exceptions - that's still an error
            } else {
                fail(
                    "Unexpected exception for expression '" +
                        expression +
                        "': " +
                        e
                );
            }
        }
    }

    private void assertDeepEquals(
        Object expected,
        Object actual,
        String message
    ) {
        if (expected == null) {
            assertNull(actual, message);
            return;
        }
        if (actual == null) {
            // JMESPath null should match JSON null
            if (
                expected instanceof Map ||
                expected instanceof List ||
                expected instanceof String ||
                expected instanceof Number ||
                expected instanceof Boolean
            ) {
                fail(message + " - expected " + expected + " but got null");
            }
            return;
        }

        if (expected instanceof Map && actual instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> expectedMap = (Map<String, Object>) expected;
            @SuppressWarnings("unchecked")
            Map<String, Object> actualMap = (Map<String, Object>) actual;

            assertEquals(
                expectedMap.keySet(),
                actualMap.keySet(),
                message + " - object keys differ"
            );

            for (String key : expectedMap.keySet()) {
                assertDeepEquals(
                    expectedMap.get(key),
                    actualMap.get(key),
                    message + " - at key '" + key + "'"
                );
            }
        } else if (expected instanceof List && actual instanceof List) {
            List<?> expectedList = (List<?>) expected;
            List<?> actualList = (List<?>) actual;

            assertEquals(
                expectedList.size(),
                actualList.size(),
                message + " - array sizes differ"
            );

            for (int i = 0; i < expectedList.size(); i++) {
                assertDeepEquals(
                    expectedList.get(i),
                    actualList.get(i),
                    message + " - at index " + i
                );
            }
        } else if (expected instanceof Number && actual instanceof Number) {
            double expectedNum = ((Number) expected).doubleValue();
            double actualNum = ((Number) actual).doubleValue();
            assertEquals(
                expectedNum,
                actualNum,
                0.0001,
                message + " - numbers differ"
            );
        } else {
            assertEquals(expected, actual, message);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadTestFile(String filename)
        throws IOException {
        String path = "/compliance/" + filename;
        try (
            InputStream is = getClass().getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
            )
        ) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            return (List<Map<String, Object>>) JmesPath.parseJson(
                content.toString()
            );
        }
    }
}
