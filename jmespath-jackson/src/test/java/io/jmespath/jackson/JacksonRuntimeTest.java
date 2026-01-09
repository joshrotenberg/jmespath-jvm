package io.jmespath.jackson;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmespath.Expression;
import io.jmespath.JmesPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for JacksonRuntime.
 */
public class JacksonRuntimeTest {

    private static final String SAMPLE_JSON =
        "{\n" +
        "  \"name\": \"Alice\",\n" +
        "  \"age\": 30,\n" +
        "  \"active\": true,\n" +
        "  \"scores\": [85, 90, 78, 92],\n" +
        "  \"address\": {\n" +
        "    \"city\": \"Seattle\",\n" +
        "    \"state\": \"WA\"\n" +
        "  },\n" +
        "  \"tags\": [\"developer\", \"java\", \"jmespath\"]\n" +
        "}";

    private static final String PEOPLE_JSON =
        "{\n" +
        "  \"people\": [\n" +
        "    {\"name\": \"Alice\", \"age\": 30, \"active\": true},\n" +
        "    {\"name\": \"Bob\", \"age\": 25, \"active\": false},\n" +
        "    {\"name\": \"Charlie\", \"age\": 35, \"active\": true},\n" +
        "    {\"name\": \"Diana\", \"age\": 28, \"active\": true}\n" +
        "  ]\n" +
        "}";

    private JacksonRuntime runtime;
    private ObjectMapper mapper;
    private JsonNode data;
    private JsonNode peopleData;

    @BeforeEach
    void setUp() throws Exception {
        runtime = new JacksonRuntime();
        mapper = runtime.getObjectMapper();
        data = mapper.readTree(SAMPLE_JSON);
        peopleData = mapper.readTree(PEOPLE_JSON);
    }

    @Test
    @DisplayName("Simple property access")
    void simplePropertyAccess() {
        Expression<JsonNode> expr = JmesPath.compile("name");
        JsonNode result = expr.evaluate(runtime, data);

        assertTrue(result.isTextual());
        assertEquals("Alice", result.asText());
    }

    @Test
    @DisplayName("Nested property access")
    void nestedPropertyAccess() {
        Expression<JsonNode> expr = JmesPath.compile("address.city");
        JsonNode result = expr.evaluate(runtime, data);

        assertEquals("Seattle", result.asText());
    }

    @Test
    @DisplayName("Array index access")
    void arrayIndexAccess() {
        Expression<JsonNode> expr = JmesPath.compile("scores[0]");
        JsonNode result = expr.evaluate(runtime, data);

        assertEquals(85, result.asInt());
    }

    @Test
    @DisplayName("Negative array index")
    void negativeArrayIndex() {
        Expression<JsonNode> expr = JmesPath.compile("scores[-1]");
        JsonNode result = expr.evaluate(runtime, data);

        assertEquals(92, result.asInt());
    }

    @Test
    @DisplayName("Array slicing")
    void arraySlicing() {
        Expression<JsonNode> expr = JmesPath.compile("scores[1:3]");
        JsonNode result = expr.evaluate(runtime, data);

        assertTrue(result.isArray());
        assertEquals(2, result.size());
        assertEquals(90, result.get(0).asInt());
        assertEquals(78, result.get(1).asInt());
    }

    @Test
    @DisplayName("Wildcard projection")
    void wildcardProjection() {
        Expression<JsonNode> expr = JmesPath.compile("people[*].name");
        JsonNode result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isArray());
        assertEquals(4, result.size());
        assertEquals("Alice", result.get(0).asText());
    }

    @Test
    @DisplayName("Filter expression")
    void filterExpression() {
        Expression<JsonNode> expr = JmesPath.compile("people[?active].name");
        JsonNode result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isArray());
        assertEquals(3, result.size()); // Alice, Charlie, Diana
    }

    @Test
    @DisplayName("Filter with comparison")
    void filterWithComparison() {
        Expression<JsonNode> expr = JmesPath.compile(
            "people[?age > `28`].name"
        );
        JsonNode result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isArray());
        assertEquals(2, result.size()); // Alice (30), Charlie (35)
    }

    @Test
    @DisplayName("Multi-select hash")
    void multiSelectHash() {
        Expression<JsonNode> expr = JmesPath.compile(
            "{userName: name, userAge: age}"
        );
        JsonNode result = expr.evaluate(runtime, data);

        assertTrue(result.isObject());
        assertEquals("Alice", result.get("userName").asText());
        assertEquals(30, result.get("userAge").asInt());
    }

    @Test
    @DisplayName("Multi-select list")
    void multiSelectList() {
        Expression<JsonNode> expr = JmesPath.compile("[name, age]");
        JsonNode result = expr.evaluate(runtime, data);

        assertTrue(result.isArray());
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).asText());
        assertEquals(30, result.get(1).asInt());
    }

    @Test
    @DisplayName("Function: length")
    void functionLength() {
        Expression<JsonNode> expr = JmesPath.compile("length(scores)");
        JsonNode result = expr.evaluate(runtime, data);

        assertEquals(4, result.asInt());
    }

    @Test
    @DisplayName("Function: sort and reverse")
    void functionSortReverse() {
        Expression<JsonNode> expr = JmesPath.compile("reverse(sort(scores))");
        JsonNode result = expr.evaluate(runtime, data);

        assertTrue(result.isArray());
        assertEquals(92, result.get(0).asInt());
        assertEquals(90, result.get(1).asInt());
    }

    @Test
    @DisplayName("Function: max_by")
    void functionMaxBy() {
        Expression<JsonNode> expr = JmesPath.compile(
            "max_by(people, &age).name"
        );
        JsonNode result = expr.evaluate(runtime, peopleData);

        assertEquals("Charlie", result.asText());
    }

    @Test
    @DisplayName("Function: min_by")
    void functionMinBy() {
        Expression<JsonNode> expr = JmesPath.compile(
            "min_by(people, &age).name"
        );
        JsonNode result = expr.evaluate(runtime, peopleData);

        assertEquals("Bob", result.asText());
    }

    @Test
    @DisplayName("Function: sort_by")
    void functionSortBy() {
        Expression<JsonNode> expr = JmesPath.compile(
            "sort_by(people, &age)[0].name"
        );
        JsonNode result = expr.evaluate(runtime, peopleData);

        assertEquals("Bob", result.asText()); // youngest
    }

    @Test
    @DisplayName("Function: join")
    void functionJoin() {
        Expression<JsonNode> expr = JmesPath.compile("join(', ', tags)");
        JsonNode result = expr.evaluate(runtime, data);

        assertEquals("developer, java, jmespath", result.asText());
    }

    @Test
    @DisplayName("Function: contains")
    void functionContains() {
        Expression<JsonNode> expr = JmesPath.compile("contains(tags, 'java')");
        JsonNode result = expr.evaluate(runtime, data);

        assertTrue(result.asBoolean());
    }

    @Test
    @DisplayName("Function: avg")
    void functionAvg() {
        Expression<JsonNode> expr = JmesPath.compile("avg(scores)");
        JsonNode result = expr.evaluate(runtime, data);

        assertEquals(86.25, result.asDouble(), 0.001);
    }

    @Test
    @DisplayName("Function: sum")
    void functionSum() {
        Expression<JsonNode> expr = JmesPath.compile("sum(scores)");
        JsonNode result = expr.evaluate(runtime, data);

        assertEquals(345, result.asInt());
    }

    @Test
    @DisplayName("Pipe expression")
    void pipeExpression() {
        Expression<JsonNode> expr = JmesPath.compile(
            "people[?active] | [0].name"
        );
        JsonNode result = expr.evaluate(runtime, peopleData);

        assertEquals("Alice", result.asText());
    }

    @Test
    @DisplayName("Or expression")
    void orExpression() {
        Expression<JsonNode> expr = JmesPath.compile("address.zip || 'N/A'");
        JsonNode result = expr.evaluate(runtime, data);

        assertEquals("N/A", result.asText());
    }

    @Test
    @DisplayName("And expression")
    void andExpression() {
        Expression<JsonNode> expr = JmesPath.compile("active && name");
        JsonNode result = expr.evaluate(runtime, data);

        assertEquals("Alice", result.asText());
    }

    @Test
    @DisplayName("Not expression")
    void notExpression() {
        Expression<JsonNode> expr = JmesPath.compile("people[?!active].name");
        JsonNode result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isArray());
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).asText());
    }

    @Test
    @DisplayName("Lexical scoping (JEP-18)")
    void lexicalScoping() {
        Expression<JsonNode> expr = JmesPath.compile(
            "let $threshold = `28` in people[?age > $threshold].name"
        );
        JsonNode result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isArray());
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Flatten projection")
    void flattenProjection() {
        String json = "{\"data\": [[1, 2], [3, 4], [5, 6]]}";
        try {
            JsonNode flattenData = mapper.readTree(json);
            Expression<JsonNode> expr = JmesPath.compile("data[]");
            JsonNode result = expr.evaluate(runtime, flattenData);

            assertTrue(result.isArray());
            assertEquals(6, result.size());
        } catch (Exception e) {
            fail("Failed to parse JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Keys function")
    void keysFunction() {
        Expression<JsonNode> expr = JmesPath.compile("keys(address)");
        JsonNode result = expr.evaluate(runtime, data);

        assertTrue(result.isArray());
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Values function")
    void valuesFunction() {
        Expression<JsonNode> expr = JmesPath.compile("values(address)");
        JsonNode result = expr.evaluate(runtime, data);

        assertTrue(result.isArray());
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Type function")
    void typeFunction() {
        Expression<JsonNode> typeExpr = JmesPath.compile("type(name)");
        assertEquals("string", typeExpr.evaluate(runtime, data).asText());

        typeExpr = JmesPath.compile("type(age)");
        assertEquals("number", typeExpr.evaluate(runtime, data).asText());

        typeExpr = JmesPath.compile("type(active)");
        assertEquals("boolean", typeExpr.evaluate(runtime, data).asText());

        typeExpr = JmesPath.compile("type(scores)");
        assertEquals("array", typeExpr.evaluate(runtime, data).asText());

        typeExpr = JmesPath.compile("type(address)");
        assertEquals("object", typeExpr.evaluate(runtime, data).asText());
    }

    @Test
    @DisplayName("Complex query")
    void complexQuery() {
        Expression<JsonNode> expr = JmesPath.compile(
            "people[?age >= `28` && active].{n: name, a: age} | sort_by(@, &a)"
        );
        JsonNode result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isArray());
        assertEquals(3, result.size());
        // Should be sorted by age: Diana (28), Alice (30), Charlie (35)
        assertEquals("Diana", result.get(0).get("n").asText());
        assertEquals("Alice", result.get(1).get("n").asText());
        assertEquals("Charlie", result.get(2).get("n").asText());
    }
}
