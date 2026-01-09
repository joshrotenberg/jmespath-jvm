package io.jmespath.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmespath.JmesPathException;
import io.jmespath.Runtime;
import io.jmespath.Type;
import io.jmespath.function.DefaultFunctionRegistry;
import io.jmespath.function.FunctionRegistry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Runtime implementation for Jackson's JsonNode.
 *
 * <p>This allows you to use jmespath-jvm directly with Jackson's JsonNode
 * without converting to/from Map/List.
 *
 * <p>Example usage:
 * <pre>{@code
 * ObjectMapper mapper = new ObjectMapper();
 * JsonNode data = mapper.readTree("{\"name\": \"Alice\", \"age\": 30}");
 *
 * JacksonRuntime runtime = new JacksonRuntime();
 * Expression<JsonNode> expr = JmesPath.compile("name");
 * JsonNode result = expr.evaluate(runtime, data);
 * // result.asText() returns "Alice"
 * }</pre>
 *
 * @see io.jmespath.JmesPath
 * @see io.jmespath.Expression
 */
public class JacksonRuntime implements Runtime<JsonNode> {

    private final ObjectMapper objectMapper;
    private final JsonNodeFactory nodeFactory;
    private final FunctionRegistry functionRegistry;

    /**
     * Creates a JacksonRuntime with a default ObjectMapper.
     */
    public JacksonRuntime() {
        this(new ObjectMapper());
    }

    /**
     * Creates a JacksonRuntime with the given ObjectMapper.
     *
     * @param objectMapper the ObjectMapper to use for JSON parsing
     */
    public JacksonRuntime(ObjectMapper objectMapper) {
        this(objectMapper, new DefaultFunctionRegistry());
    }

    /**
     * Creates a JacksonRuntime with the given ObjectMapper and function registry.
     *
     * @param objectMapper the ObjectMapper to use for JSON parsing
     * @param functionRegistry the function registry for custom functions
     */
    public JacksonRuntime(ObjectMapper objectMapper, FunctionRegistry functionRegistry) {
        this.objectMapper = objectMapper;
        this.nodeFactory = objectMapper.getNodeFactory();
        this.functionRegistry = functionRegistry;
    }

    /**
     * Returns the ObjectMapper used by this runtime.
     *
     * @return the ObjectMapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    // === Type Checking ===

    @Override
    public boolean isNull(JsonNode value) {
        return value == null || value.isNull();
    }

    @Override
    public boolean isBoolean(JsonNode value) {
        return value != null && value.isBoolean();
    }

    @Override
    public boolean isNumber(JsonNode value) {
        return value != null && value.isNumber();
    }

    @Override
    public boolean isString(JsonNode value) {
        return value != null && value.isTextual();
    }

    @Override
    public boolean isArray(JsonNode value) {
        return value != null && value.isArray();
    }

    @Override
    public boolean isObject(JsonNode value) {
        return value != null && value.isObject();
    }

    @Override
    public Type typeOf(JsonNode value) {
        if (value == null || value.isNull()) {
            return Type.NULL;
        }
        if (value.isBoolean()) {
            return Type.BOOLEAN;
        }
        if (value.isNumber()) {
            return Type.NUMBER;
        }
        if (value.isTextual()) {
            return Type.STRING;
        }
        if (value.isArray()) {
            return Type.ARRAY;
        }
        if (value.isObject()) {
            return Type.OBJECT;
        }
        return Type.NULL;
    }

    // === Value Extraction ===

    @Override
    public JsonNode getProperty(JsonNode object, String name) {
        if (object == null || !object.isObject()) {
            return null;
        }
        return object.get(name);
    }

    @Override
    public JsonNode getIndex(JsonNode array, int index) {
        if (array == null || !array.isArray()) {
            return null;
        }
        if (index < 0 || index >= array.size()) {
            return null;
        }
        return array.get(index);
    }

    @Override
    public int getArrayLength(JsonNode array) {
        if (array == null || !array.isArray()) {
            return 0;
        }
        return array.size();
    }

    @Override
    public Iterable<JsonNode> getArrayElements(JsonNode array) {
        if (array == null || !array.isArray()) {
            return new ArrayList<JsonNode>();
        }
        List<JsonNode> elements = new ArrayList<JsonNode>();
        for (JsonNode element : array) {
            elements.add(element);
        }
        return elements;
    }

    @Override
    public Iterable<String> getObjectKeys(JsonNode object) {
        if (object == null || !object.isObject()) {
            return new ArrayList<String>();
        }
        List<String> keys = new ArrayList<String>();
        Iterator<String> fieldNames = object.fieldNames();
        while (fieldNames.hasNext()) {
            keys.add(fieldNames.next());
        }
        return keys;
    }

    @Override
    public Iterable<JsonNode> getObjectValues(JsonNode object) {
        if (object == null || !object.isObject()) {
            return new ArrayList<JsonNode>();
        }
        List<JsonNode> values = new ArrayList<JsonNode>();
        Iterator<JsonNode> elements = object.elements();
        while (elements.hasNext()) {
            values.add(elements.next());
        }
        return values;
    }

    // === Conversion to Java Types ===

    @Override
    public boolean toBoolean(JsonNode value) {
        if (value != null && value.isBoolean()) {
            return value.booleanValue();
        }
        return false;
    }

    @Override
    public Number toNumber(JsonNode value) {
        if (value != null && value.isNumber()) {
            return value.numberValue();
        }
        return null;
    }

    @Override
    public String toString(JsonNode value) {
        if (value == null || value.isNull()) {
            return "null";
        }
        if (value.isTextual()) {
            return value.textValue();
        }
        return value.toString();
    }

    // === Value Construction ===

    @Override
    public JsonNode createNull() {
        return nodeFactory.nullNode();
    }

    @Override
    public JsonNode createBoolean(boolean value) {
        return nodeFactory.booleanNode(value);
    }

    @Override
    public JsonNode createNumber(Number value) {
        if (value instanceof Integer) {
            return nodeFactory.numberNode(value.intValue());
        }
        if (value instanceof Long) {
            return nodeFactory.numberNode(value.longValue());
        }
        if (value instanceof Float) {
            return nodeFactory.numberNode(value.floatValue());
        }
        return nodeFactory.numberNode(value.doubleValue());
    }

    @Override
    public JsonNode createString(String value) {
        return nodeFactory.textNode(value);
    }

    @Override
    public JsonNode createArray(List<JsonNode> elements) {
        ArrayNode array = nodeFactory.arrayNode();
        for (JsonNode element : elements) {
            array.add(element);
        }
        return array;
    }

    @Override
    public JsonNode createObject(Map<String, JsonNode> properties) {
        ObjectNode object = nodeFactory.objectNode();
        for (Map.Entry<String, JsonNode> entry : properties.entrySet()) {
            object.set(entry.getKey(), entry.getValue());
        }
        return object;
    }

    // === Comparison ===

    @Override
    public int compare(JsonNode a, JsonNode b) {
        if (isNull(a) && isNull(b)) {
            return 0;
        }
        if (isNull(a)) {
            return -1;
        }
        if (isNull(b)) {
            return 1;
        }

        // Numbers
        if (isNumber(a) && isNumber(b)) {
            double da = a.doubleValue();
            double db = b.doubleValue();
            return Double.compare(da, db);
        }

        // Strings
        if (isString(a) && isString(b)) {
            return a.textValue().compareTo(b.textValue());
        }

        // Type mismatch
        return typeOf(a).ordinal() - typeOf(b).ordinal();
    }

    @Override
    public boolean deepEquals(JsonNode a, JsonNode b) {
        if (a == b) {
            return true;
        }
        if (isNull(a) && isNull(b)) {
            return true;
        }
        if (isNull(a) || isNull(b)) {
            return false;
        }
        return a.equals(b);
    }

    @Override
    public boolean isTruthy(JsonNode value) {
        if (value == null || value.isNull()) {
            return false;
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        if (value.isTextual()) {
            return !value.textValue().isEmpty();
        }
        if (value.isArray()) {
            return value.size() > 0;
        }
        if (value.isObject()) {
            return value.size() > 0;
        }
        // Numbers are truthy
        return true;
    }

    // === JSON Parsing ===

    @Override
    public JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new JmesPathException("Failed to parse JSON: " + e.getMessage());
        }
    }

    // === Function Registry ===

    @Override
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    @Override
    public boolean isSilentTypeErrors() {
        return false;
    }
}
