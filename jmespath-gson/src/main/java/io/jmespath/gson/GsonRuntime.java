package io.jmespath.gson;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.jmespath.JmesPathException;
import io.jmespath.Runtime;
import io.jmespath.Type;
import io.jmespath.function.DefaultFunctionRegistry;
import io.jmespath.function.FunctionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runtime implementation for Gson's JsonElement.
 *
 * <p>This allows you to use jmespath-jvm directly with Gson's JsonElement
 * without converting to/from Map/List.
 *
 * <p>Example usage:
 * <pre>{@code
 * JsonElement data = JsonParser.parseString("{\"name\": \"Alice\", \"age\": 30}");
 *
 * GsonRuntime runtime = new GsonRuntime();
 * Expression<JsonElement> expr = JmesPath.compile("name");
 * JsonElement result = expr.evaluate(runtime, data);
 * // result.getAsString() returns "Alice"
 * }</pre>
 *
 * @see io.jmespath.JmesPath
 * @see io.jmespath.Expression
 */
public class GsonRuntime implements Runtime<JsonElement> {

    private final Gson gson;
    private final FunctionRegistry functionRegistry;

    /**
     * Creates a GsonRuntime with a default Gson instance.
     */
    public GsonRuntime() {
        this(new Gson());
    }

    /**
     * Creates a GsonRuntime with the given Gson instance.
     *
     * @param gson the Gson instance to use
     */
    public GsonRuntime(Gson gson) {
        this(gson, new DefaultFunctionRegistry());
    }

    /**
     * Creates a GsonRuntime with the given Gson instance and function registry.
     *
     * @param gson the Gson instance to use
     * @param functionRegistry the function registry for custom functions
     */
    public GsonRuntime(Gson gson, FunctionRegistry functionRegistry) {
        this.gson = gson;
        this.functionRegistry = functionRegistry;
    }

    /**
     * Returns the Gson instance used by this runtime.
     *
     * @return the Gson instance
     */
    public Gson getGson() {
        return gson;
    }

    // === Type Checking ===

    @Override
    public boolean isNull(JsonElement value) {
        return value == null || value.isJsonNull();
    }

    @Override
    public boolean isBoolean(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean();
    }

    @Override
    public boolean isNumber(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber();
    }

    @Override
    public boolean isString(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
    }

    @Override
    public boolean isArray(JsonElement value) {
        return value != null && value.isJsonArray();
    }

    @Override
    public boolean isObject(JsonElement value) {
        return value != null && value.isJsonObject();
    }

    @Override
    public Type typeOf(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return Type.NULL;
        }
        if (value.isJsonPrimitive()) {
            JsonPrimitive primitive = value.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return Type.BOOLEAN;
            }
            if (primitive.isNumber()) {
                return Type.NUMBER;
            }
            if (primitive.isString()) {
                return Type.STRING;
            }
        }
        if (value.isJsonArray()) {
            return Type.ARRAY;
        }
        if (value.isJsonObject()) {
            return Type.OBJECT;
        }
        return Type.NULL;
    }

    // === Value Extraction ===

    @Override
    public JsonElement getProperty(JsonElement object, String name) {
        if (object == null || !object.isJsonObject()) {
            return null;
        }
        return object.getAsJsonObject().get(name);
    }

    @Override
    public JsonElement getIndex(JsonElement array, int index) {
        if (array == null || !array.isJsonArray()) {
            return null;
        }
        JsonArray arr = array.getAsJsonArray();
        if (index < 0 || index >= arr.size()) {
            return null;
        }
        return arr.get(index);
    }

    @Override
    public int getArrayLength(JsonElement array) {
        if (array == null || !array.isJsonArray()) {
            return 0;
        }
        return array.getAsJsonArray().size();
    }

    @Override
    public Iterable<JsonElement> getArrayElements(JsonElement array) {
        if (array == null || !array.isJsonArray()) {
            return new ArrayList<JsonElement>();
        }
        List<JsonElement> elements = new ArrayList<JsonElement>();
        for (JsonElement element : array.getAsJsonArray()) {
            elements.add(element);
        }
        return elements;
    }

    @Override
    public Iterable<String> getObjectKeys(JsonElement object) {
        if (object == null || !object.isJsonObject()) {
            return new ArrayList<String>();
        }
        return object.getAsJsonObject().keySet();
    }

    @Override
    public Iterable<JsonElement> getObjectValues(JsonElement object) {
        if (object == null || !object.isJsonObject()) {
            return new ArrayList<JsonElement>();
        }
        List<JsonElement> values = new ArrayList<JsonElement>();
        for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject().entrySet()) {
            values.add(entry.getValue());
        }
        return values;
    }

    // === Conversion to Java Types ===

    @Override
    public boolean toBoolean(JsonElement value) {
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
            return value.getAsBoolean();
        }
        return false;
    }

    @Override
    public Number toNumber(JsonElement value) {
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            return value.getAsNumber();
        }
        return null;
    }

    @Override
    public String toString(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return "null";
        }
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            return value.getAsString();
        }
        return value.toString();
    }

    // === Value Construction ===

    @Override
    public JsonElement createNull() {
        return JsonNull.INSTANCE;
    }

    @Override
    public JsonElement createBoolean(boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public JsonElement createNumber(Number value) {
        return new JsonPrimitive(value);
    }

    @Override
    public JsonElement createString(String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public JsonElement createArray(List<JsonElement> elements) {
        JsonArray array = new JsonArray();
        for (JsonElement element : elements) {
            array.add(element);
        }
        return array;
    }

    @Override
    public JsonElement createObject(Map<String, JsonElement> properties) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
            object.add(entry.getKey(), entry.getValue());
        }
        return object;
    }

    // === Comparison ===

    @Override
    public int compare(JsonElement a, JsonElement b) {
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
            double da = a.getAsDouble();
            double db = b.getAsDouble();
            return Double.compare(da, db);
        }

        // Strings
        if (isString(a) && isString(b)) {
            return a.getAsString().compareTo(b.getAsString());
        }

        // Type mismatch
        return typeOf(a).ordinal() - typeOf(b).ordinal();
    }

    @Override
    public boolean deepEquals(JsonElement a, JsonElement b) {
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
    public boolean isTruthy(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return false;
        }
        if (value.isJsonPrimitive()) {
            JsonPrimitive primitive = value.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (primitive.isString()) {
                return !primitive.getAsString().isEmpty();
            }
            // Numbers are truthy
            return true;
        }
        if (value.isJsonArray()) {
            return value.getAsJsonArray().size() > 0;
        }
        if (value.isJsonObject()) {
            return value.getAsJsonObject().size() > 0;
        }
        return true;
    }

    // === JSON Parsing ===

    @Override
    public JsonElement parseJson(String json) {
        try {
            return JsonParser.parseString(json);
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
