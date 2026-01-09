package io.jmespath.gson;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.jmespath.Expression;
import io.jmespath.JmesPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for GsonRuntime.
 */
public class GsonRuntimeTest {

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

    private GsonRuntime runtime;
    private JsonElement data;
    private JsonElement peopleData;

    @BeforeEach
    void setUp() {
        runtime = new GsonRuntime();
        data = JsonParser.parseString(SAMPLE_JSON);
        peopleData = JsonParser.parseString(PEOPLE_JSON);
    }

    @Test
    @DisplayName("Simple property access")
    void simplePropertyAccess() {
        Expression<JsonElement> expr = JmesPath.compile("name");
        JsonElement result = expr.evaluate(runtime, data);

        assertTrue(result.isJsonPrimitive());
        assertEquals("Alice", result.getAsString());
    }

    @Test
    @DisplayName("Nested property access")
    void nestedPropertyAccess() {
        Expression<JsonElement> expr = JmesPath.compile("address.city");
        JsonElement result = expr.evaluate(runtime, data);

        assertEquals("Seattle", result.getAsString());
    }

    @Test
    @DisplayName("Array index access")
    void arrayIndexAccess() {
        Expression<JsonElement> expr = JmesPath.compile("scores[0]");
        JsonElement result = expr.evaluate(runtime, data);

        assertEquals(85, result.getAsInt());
    }

    @Test
    @DisplayName("Negative array index")
    void negativeArrayIndex() {
        Expression<JsonElement> expr = JmesPath.compile("scores[-1]");
        JsonElement result = expr.evaluate(runtime, data);

        assertEquals(92, result.getAsInt());
    }

    @Test
    @DisplayName("Wildcard projection")
    void wildcardProjection() {
        Expression<JsonElement> expr = JmesPath.compile("people[*].name");
        JsonElement result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isJsonArray());
        assertEquals(4, result.getAsJsonArray().size());
    }

    @Test
    @DisplayName("Filter expression")
    void filterExpression() {
        Expression<JsonElement> expr = JmesPath.compile("people[?active].name");
        JsonElement result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isJsonArray());
        assertEquals(3, result.getAsJsonArray().size());
    }

    @Test
    @DisplayName("Filter with comparison")
    void filterWithComparison() {
        Expression<JsonElement> expr = JmesPath.compile(
            "people[?age > `28`].name"
        );
        JsonElement result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isJsonArray());
        assertEquals(2, result.getAsJsonArray().size());
    }

    @Test
    @DisplayName("Multi-select hash")
    void multiSelectHash() {
        Expression<JsonElement> expr = JmesPath.compile(
            "{userName: name, userAge: age}"
        );
        JsonElement result = expr.evaluate(runtime, data);

        assertTrue(result.isJsonObject());
        assertEquals(
            "Alice",
            result.getAsJsonObject().get("userName").getAsString()
        );
        assertEquals(30, result.getAsJsonObject().get("userAge").getAsInt());
    }

    @Test
    @DisplayName("Function: join")
    void functionJoin() {
        Expression<JsonElement> expr = JmesPath.compile("join(', ', tags)");
        JsonElement result = expr.evaluate(runtime, data);

        assertEquals("developer, java, jmespath", result.getAsString());
    }

    @Test
    @DisplayName("Function: sort_by")
    void functionSortBy() {
        Expression<JsonElement> expr = JmesPath.compile(
            "sort_by(people, &age)[0].name"
        );
        JsonElement result = expr.evaluate(runtime, peopleData);

        assertEquals("Bob", result.getAsString()); // youngest
    }

    @Test
    @DisplayName("Function: max_by")
    void functionMaxBy() {
        Expression<JsonElement> expr = JmesPath.compile(
            "max_by(people, &age).name"
        );
        JsonElement result = expr.evaluate(runtime, peopleData);

        assertEquals("Charlie", result.getAsString());
    }

    @Test
    @DisplayName("Function: contains")
    void functionContains() {
        Expression<JsonElement> expr = JmesPath.compile(
            "contains(tags, 'java')"
        );
        JsonElement result = expr.evaluate(runtime, data);

        assertTrue(result.getAsBoolean());
    }

    @Test
    @DisplayName("Function: length")
    void functionLength() {
        Expression<JsonElement> expr = JmesPath.compile("length(scores)");
        JsonElement result = expr.evaluate(runtime, data);

        assertEquals(4, result.getAsInt());
    }

    @Test
    @DisplayName("Function: avg")
    void functionAvg() {
        Expression<JsonElement> expr = JmesPath.compile("avg(scores)");
        JsonElement result = expr.evaluate(runtime, data);

        assertEquals(86.25, result.getAsDouble(), 0.001);
    }

    @Test
    @DisplayName("Pipe expression")
    void pipeExpression() {
        Expression<JsonElement> expr = JmesPath.compile(
            "people[?active] | [0].name"
        );
        JsonElement result = expr.evaluate(runtime, peopleData);

        assertEquals("Alice", result.getAsString());
    }

    @Test
    @DisplayName("Or expression")
    void orExpression() {
        Expression<JsonElement> expr = JmesPath.compile("address.zip || 'N/A'");
        JsonElement result = expr.evaluate(runtime, data);

        assertEquals("N/A", result.getAsString());
    }

    @Test
    @DisplayName("Not expression")
    void notExpression() {
        Expression<JsonElement> expr = JmesPath.compile(
            "people[?!active].name"
        );
        JsonElement result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isJsonArray());
        assertEquals(1, result.getAsJsonArray().size());
        assertEquals("Bob", result.getAsJsonArray().get(0).getAsString());
    }

    @Test
    @DisplayName("Lexical scoping (JEP-18)")
    void lexicalScoping() {
        Expression<JsonElement> expr = JmesPath.compile(
            "let $threshold = `28` in people[?age > $threshold].name"
        );
        JsonElement result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isJsonArray());
        assertEquals(2, result.getAsJsonArray().size());
    }

    @Test
    @DisplayName("Complex query")
    void complexQuery() {
        Expression<JsonElement> expr = JmesPath.compile(
            "people[?age >= `28` && active].{n: name, a: age} | sort_by(@, &a)"
        );
        JsonElement result = expr.evaluate(runtime, peopleData);

        assertTrue(result.isJsonArray());
        assertEquals(3, result.getAsJsonArray().size());
        // Should be sorted by age: Diana (28), Alice (30), Charlie (35)
        assertEquals(
            "Diana",
            result
                .getAsJsonArray()
                .get(0)
                .getAsJsonObject()
                .get("n")
                .getAsString()
        );
    }

    @Test
    @DisplayName("Type function")
    void typeFunction() {
        Expression<JsonElement> typeExpr = JmesPath.compile("type(name)");
        assertEquals("string", typeExpr.evaluate(runtime, data).getAsString());

        typeExpr = JmesPath.compile("type(age)");
        assertEquals("number", typeExpr.evaluate(runtime, data).getAsString());

        typeExpr = JmesPath.compile("type(active)");
        assertEquals("boolean", typeExpr.evaluate(runtime, data).getAsString());

        typeExpr = JmesPath.compile("type(scores)");
        assertEquals("array", typeExpr.evaluate(runtime, data).getAsString());

        typeExpr = JmesPath.compile("type(address)");
        assertEquals("object", typeExpr.evaluate(runtime, data).getAsString());
    }
}
