# Custom Runtimes

Integrate jmespath-jvm with any JSON library by implementing the `Runtime` interface.

## Why Custom Runtimes?

The default `MapRuntime` uses `Map<String, Object>` and `List<Object>`. But you might want to:

- Use Jackson's `JsonNode` directly
- Use Gson's `JsonElement`
- Work with your own domain objects
- Optimize for your specific use case

## The Runtime Interface

```java
public interface Runtime<T> {
    
    // Type checking
    boolean isNull(T value);
    boolean isBoolean(T value);
    boolean isNumber(T value);
    boolean isString(T value);
    boolean isArray(T value);
    boolean isObject(T value);
    Type typeOf(T value);
    
    // Value extraction
    T getProperty(T object, String name);
    T getIndex(T array, int index);
    int getArrayLength(T array);
    Iterable<T> getArrayElements(T array);
    Iterable<String> getObjectKeys(T object);
    Iterable<T> getObjectValues(T object);
    
    // Conversion to Java types
    boolean toBoolean(T value);
    Number toNumber(T value);
    String toString(T value);
    
    // Value construction
    T createNull();
    T createBoolean(boolean value);
    T createNumber(Number value);
    T createString(String value);
    T createArray(List<T> elements);
    T createObject(Map<String, T> properties);
    
    // Comparison
    int compare(T a, T b);
    boolean deepEquals(T a, T b);
    boolean isTruthy(T value);
    
    // JSON parsing (for literals)
    T parseJson(String json);
    
    // Function registry
    FunctionRegistry getFunctionRegistry();
}
```

## Jackson Example

```java
public class JacksonRuntime implements Runtime<JsonNode> {
    
    private final ObjectMapper mapper = new ObjectMapper();
    private final FunctionRegistry functions;
    
    public JacksonRuntime() {
        this.functions = new DefaultFunctionRegistry();
    }
    
    public JacksonRuntime(FunctionRegistry functions) {
        this.functions = functions;
    }
    
    // Type checking
    
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
        if (value == null || value.isNull()) return Type.NULL;
        if (value.isBoolean()) return Type.BOOLEAN;
        if (value.isNumber()) return Type.NUMBER;
        if (value.isTextual()) return Type.STRING;
        if (value.isArray()) return Type.ARRAY;
        if (value.isObject()) return Type.OBJECT;
        return Type.NULL;
    }
    
    // Value extraction
    
    @Override
    public JsonNode getProperty(JsonNode object, String name) {
        if (object == null || !object.isObject()) return null;
        return object.get(name);
    }
    
    @Override
    public JsonNode getIndex(JsonNode array, int index) {
        if (array == null || !array.isArray()) return null;
        if (index < 0 || index >= array.size()) return null;
        return array.get(index);
    }
    
    @Override
    public int getArrayLength(JsonNode array) {
        if (array == null || !array.isArray()) return 0;
        return array.size();
    }
    
    @Override
    public Iterable<JsonNode> getArrayElements(JsonNode array) {
        if (array == null || !array.isArray()) {
            return Collections.emptyList();
        }
        return array;
    }
    
    @Override
    public Iterable<String> getObjectKeys(JsonNode object) {
        if (object == null || !object.isObject()) {
            return Collections.emptyList();
        }
        List<String> keys = new ArrayList<>();
        object.fieldNames().forEachRemaining(keys::add);
        return keys;
    }
    
    @Override
    public Iterable<JsonNode> getObjectValues(JsonNode object) {
        if (object == null || !object.isObject()) {
            return Collections.emptyList();
        }
        List<JsonNode> values = new ArrayList<>();
        object.elements().forEachRemaining(values::add);
        return values;
    }
    
    // Conversion
    
    @Override
    public boolean toBoolean(JsonNode value) {
        return value != null && value.isBoolean() && value.booleanValue();
    }
    
    @Override
    public Number toNumber(JsonNode value) {
        if (value == null || !value.isNumber()) return null;
        return value.numberValue();
    }
    
    @Override
    public String toString(JsonNode value) {
        if (value == null) return "null";
        if (value.isTextual()) return value.textValue();
        return value.toString();
    }
    
    // Value construction
    
    @Override
    public JsonNode createNull() {
        return NullNode.getInstance();
    }
    
    @Override
    public JsonNode createBoolean(boolean value) {
        return BooleanNode.valueOf(value);
    }
    
    @Override
    public JsonNode createNumber(Number value) {
        if (value instanceof Integer) {
            return IntNode.valueOf(value.intValue());
        }
        if (value instanceof Long) {
            return LongNode.valueOf(value.longValue());
        }
        return DoubleNode.valueOf(value.doubleValue());
    }
    
    @Override
    public JsonNode createString(String value) {
        return TextNode.valueOf(value);
    }
    
    @Override
    public JsonNode createArray(List<JsonNode> elements) {
        ArrayNode array = mapper.createArrayNode();
        array.addAll(elements);
        return array;
    }
    
    @Override
    public JsonNode createObject(Map<String, JsonNode> properties) {
        ObjectNode obj = mapper.createObjectNode();
        obj.setAll(properties);
        return obj;
    }
    
    // Comparison
    
    @Override
    public int compare(JsonNode a, JsonNode b) {
        if (isNumber(a) && isNumber(b)) {
            return Double.compare(
                a.doubleValue(), 
                b.doubleValue()
            );
        }
        if (isString(a) && isString(b)) {
            return a.textValue().compareTo(b.textValue());
        }
        return 0;
    }
    
    @Override
    public boolean deepEquals(JsonNode a, JsonNode b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    @Override
    public boolean isTruthy(JsonNode value) {
        if (value == null || value.isNull()) return false;
        if (value.isBoolean()) return value.booleanValue();
        if (value.isTextual()) return !value.textValue().isEmpty();
        if (value.isArray()) return value.size() > 0;
        if (value.isObject()) return value.size() > 0;
        return true;
    }
    
    // JSON parsing
    
    @Override
    public JsonNode parseJson(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new JmesPathException("Invalid JSON: " + e.getMessage());
        }
    }
    
    // Functions
    
    @Override
    public FunctionRegistry getFunctionRegistry() {
        return functions;
    }
}
```

## Using Custom Runtime

```java
// Create your runtime
JacksonRuntime runtime = new JacksonRuntime();

// Parse your data
JsonNode data = runtime.parseJson("{\"name\": \"Alice\"}");

// Compile and evaluate
Expression<JsonNode> expr = JmesPath.compile("name");
JsonNode result = expr.evaluate(runtime, data);

// Result is a JsonNode
String name = result.textValue();  // "Alice"
```

## Thread Safety

Your runtime should be thread-safe if you plan to share it across threads. The built-in `MapRuntime` is thread-safe.

Key considerations:

- Make the runtime stateless if possible
- Use thread-safe collections if you need state
- The `FunctionRegistry` is effectively immutable after setup

## Performance Tips

1. **Avoid allocations** in hot paths like `getProperty` and `getIndex`
2. **Cache** frequently accessed values
3. **Use primitive types** where possible in comparisons
4. **Pre-size collections** in `createArray` and `createObject`
