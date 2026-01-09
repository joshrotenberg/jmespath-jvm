package io.jmespath.runtime;

import io.jmespath.JmesPathException;
import io.jmespath.Runtime;
import io.jmespath.RuntimeConfiguration;
import io.jmespath.Type;
import io.jmespath.function.DefaultFunctionRegistry;
import io.jmespath.function.FunctionRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A zero-dependency Runtime implementation using Java's Map and List.
 *
 * <p>This runtime represents JSON values as:
 * <ul>
 *   <li>null - Java null</li>
 *   <li>boolean - Java Boolean</li>
 *   <li>number - Java Number (Integer, Long, Double)</li>
 *   <li>string - Java String</li>
 *   <li>array - Java List&lt;Object&gt;</li>
 *   <li>object - Java Map&lt;String, Object&gt;</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * MapRuntime runtime = new MapRuntime();
 * Map<String, Object> data = new HashMap<>();
 * data.put("name", "Alice");
 * data.put("age", 30);
 *
 * Expression<Object> expr = JmesPath.compile("name");
 * Object result = expr.evaluate(runtime, data);  // "Alice"
 * }</pre>
 */
public class MapRuntime implements Runtime<Object> {

    private final RuntimeConfiguration configuration;

    /**
     * Creates a new MapRuntime with the default configuration.
     */
    public MapRuntime() {
        this.configuration = RuntimeConfiguration.defaultConfiguration();
    }

    /**
     * Creates a new MapRuntime with the given configuration.
     *
     * @param configuration the runtime configuration
     */
    public MapRuntime(RuntimeConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration cannot be null");
        }
        this.configuration = configuration;
    }

    /**
     * Creates a new MapRuntime with the given function registry.
     *
     * @param functionRegistry the function registry
     */
    public MapRuntime(FunctionRegistry functionRegistry) {
        this.configuration = RuntimeConfiguration.builder()
            .withFunctionRegistry(functionRegistry)
            .build();
    }

    /**
     * Returns the runtime configuration.
     *
     * @return the configuration
     */
    public RuntimeConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public FunctionRegistry getFunctionRegistry() {
        return configuration.getFunctionRegistry();
    }

    @Override
    public boolean isSilentTypeErrors() {
        return configuration.isSilentTypeErrors();
    }

    // === Type Checking ===

    @Override
    public boolean isNull(Object value) {
        return value == null;
    }

    @Override
    public boolean isBoolean(Object value) {
        return value instanceof Boolean;
    }

    @Override
    public boolean isNumber(Object value) {
        return value instanceof Number;
    }

    @Override
    public boolean isString(Object value) {
        return value instanceof String;
    }

    @Override
    public boolean isArray(Object value) {
        return value instanceof List;
    }

    @Override
    public boolean isObject(Object value) {
        return value instanceof Map;
    }

    @Override
    public Type typeOf(Object value) {
        if (value == null) {
            return Type.NULL;
        }
        if (value instanceof Boolean) {
            return Type.BOOLEAN;
        }
        if (value instanceof Number) {
            return Type.NUMBER;
        }
        if (value instanceof String) {
            return Type.STRING;
        }
        if (value instanceof List) {
            return Type.ARRAY;
        }
        if (value instanceof Map) {
            return Type.OBJECT;
        }
        // Unknown type - treat as null
        return Type.NULL;
    }

    // === Value Extraction ===

    @Override
    @SuppressWarnings("unchecked")
    public Object getProperty(Object object, String name) {
        if (!(object instanceof Map)) {
            return null;
        }
        Map<String, Object> map = (Map<String, Object>) object;
        return map.get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getIndex(Object array, int index) {
        if (!(array instanceof List)) {
            return null;
        }
        List<Object> list = (List<Object>) array;
        if (index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int getArrayLength(Object array) {
        if (!(array instanceof List)) {
            return 0;
        }
        return ((List<Object>) array).size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Object> getArrayElements(Object array) {
        if (!(array instanceof List)) {
            return Collections.emptyList();
        }
        return (List<Object>) array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<String> getObjectKeys(Object object) {
        if (!(object instanceof Map)) {
            return Collections.emptyList();
        }
        Map<String, Object> map = (Map<String, Object>) object;
        return map.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Object> getObjectValues(Object object) {
        if (!(object instanceof Map)) {
            return Collections.emptyList();
        }
        Map<String, Object> map = (Map<String, Object>) object;
        return map.values();
    }

    // === Conversion to Java Types ===

    @Override
    public boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    @Override
    public Number toNumber(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        }
        return null;
    }

    @Override
    public String toString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value == null) {
            return "null";
        }
        return String.valueOf(value);
    }

    // === Value Construction ===

    @Override
    public Object createNull() {
        return null;
    }

    @Override
    public Object createBoolean(boolean value) {
        return value;
    }

    @Override
    public Object createNumber(Number value) {
        return value;
    }

    @Override
    public Object createString(String value) {
        return value;
    }

    @Override
    public Object createArray(List<Object> elements) {
        return new ArrayList<Object>(elements);
    }

    @Override
    public Object createObject(Map<String, Object> properties) {
        return new LinkedHashMap<String, Object>(properties);
    }

    // === Comparison ===

    @Override
    @SuppressWarnings("unchecked")
    public int compare(Object a, Object b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }

        // Numbers
        if (a instanceof Number && b instanceof Number) {
            double da = ((Number) a).doubleValue();
            double db = ((Number) b).doubleValue();
            return Double.compare(da, db);
        }

        // Strings
        if (a instanceof String && b instanceof String) {
            return ((String) a).compareTo((String) b);
        }

        // Type mismatch - arbitrary but consistent ordering
        return typeOf(a).ordinal() - typeOf(b).ordinal();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean deepEquals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        // Same type check
        Type typeA = typeOf(a);
        Type typeB = typeOf(b);
        if (typeA != typeB) {
            return false;
        }

        switch (typeA) {
            case NULL:
                return true;
            case BOOLEAN:
                return a.equals(b);
            case NUMBER:
                // Compare as doubles to handle int/long/double equality
                return ((Number) a).doubleValue() == ((Number) b).doubleValue();
            case STRING:
                return a.equals(b);
            case ARRAY:
                List<Object> listA = (List<Object>) a;
                List<Object> listB = (List<Object>) b;
                if (listA.size() != listB.size()) {
                    return false;
                }
                for (int i = 0; i < listA.size(); i++) {
                    if (!deepEquals(listA.get(i), listB.get(i))) {
                        return false;
                    }
                }
                return true;
            case OBJECT:
                Map<String, Object> mapA = (Map<String, Object>) a;
                Map<String, Object> mapB = (Map<String, Object>) b;
                if (mapA.size() != mapB.size()) {
                    return false;
                }
                for (Map.Entry<String, Object> entry : mapA.entrySet()) {
                    if (!mapB.containsKey(entry.getKey())) {
                        return false;
                    }
                    if (
                        !deepEquals(entry.getValue(), mapB.get(entry.getKey()))
                    ) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        if (value instanceof List) {
            return !((List<Object>) value).isEmpty();
        }
        if (value instanceof Map) {
            return !((Map<String, Object>) value).isEmpty();
        }
        // Numbers are truthy
        return true;
    }

    // === JSON Parsing ===

    @Override
    public Object parseJson(String json) {
        return new SimpleJsonParser(json).parse();
    }

    /**
     * A minimal JSON parser for literal expressions.
     *
     * <p>This is intentionally simple - it handles the JSON subset
     * needed for JMESPath literals without external dependencies.
     */
    private static class SimpleJsonParser {

        private final String input;
        private int pos;

        SimpleJsonParser(String input) {
            this.input = input;
            this.pos = 0;
        }

        Object parse() {
            skipWhitespace();
            Object result = parseValue();
            skipWhitespace();
            if (pos < input.length()) {
                throw new JmesPathException(
                    "Unexpected character at position " + pos
                );
            }
            return result;
        }

        private Object parseValue() {
            skipWhitespace();
            if (pos >= input.length()) {
                throw new JmesPathException("Unexpected end of JSON");
            }

            char c = input.charAt(pos);
            switch (c) {
                case 'n':
                    return parseNull();
                case 't':
                    return parseTrue();
                case 'f':
                    return parseFalse();
                case '"':
                    return parseString();
                case '[':
                    return parseArray();
                case '{':
                    return parseObject();
                case '-':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    return parseNumber();
                default:
                    throw new JmesPathException(
                        "Unexpected character '" + c + "' at position " + pos
                    );
            }
        }

        private Object parseNull() {
            expect("null");
            return null;
        }

        private Object parseTrue() {
            expect("true");
            return Boolean.TRUE;
        }

        private Object parseFalse() {
            expect("false");
            return Boolean.FALSE;
        }

        private void expect(String s) {
            for (int i = 0; i < s.length(); i++) {
                if (pos >= input.length() || input.charAt(pos) != s.charAt(i)) {
                    throw new JmesPathException(
                        "Expected '" + s + "' at position " + pos
                    );
                }
                pos++;
            }
        }

        private String parseString() {
            pos++; // skip opening quote
            StringBuilder sb = new StringBuilder();
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (c == '"') {
                    pos++;
                    return sb.toString();
                }
                if (c == '\\') {
                    pos++;
                    if (pos >= input.length()) {
                        throw new JmesPathException("Unexpected end in string");
                    }
                    char escaped = input.charAt(pos);
                    switch (escaped) {
                        case '"':
                            sb.append('"');
                            break;
                        case '\\':
                            sb.append('\\');
                            break;
                        case '/':
                            sb.append('/');
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            pos++;
                            if (pos + 4 > input.length()) {
                                throw new JmesPathException(
                                    "Invalid unicode escape"
                                );
                            }
                            String hex = input.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 3; // +1 below
                            break;
                        default:
                            throw new JmesPathException(
                                "Invalid escape sequence"
                            );
                    }
                    pos++;
                } else {
                    sb.append(c);
                    pos++;
                }
            }
            throw new JmesPathException("Unterminated string");
        }

        private Number parseNumber() {
            int start = pos;
            if (input.charAt(pos) == '-') {
                pos++;
            }
            while (pos < input.length() && isDigit(input.charAt(pos))) {
                pos++;
            }
            boolean isFloat = false;
            if (pos < input.length() && input.charAt(pos) == '.') {
                isFloat = true;
                pos++;
                while (pos < input.length() && isDigit(input.charAt(pos))) {
                    pos++;
                }
            }
            if (
                pos < input.length() &&
                (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')
            ) {
                isFloat = true;
                pos++;
                if (
                    pos < input.length() &&
                    (input.charAt(pos) == '+' || input.charAt(pos) == '-')
                ) {
                    pos++;
                }
                while (pos < input.length() && isDigit(input.charAt(pos))) {
                    pos++;
                }
            }
            String numStr = input.substring(start, pos);
            if (isFloat) {
                return Double.parseDouble(numStr);
            } else {
                try {
                    return Integer.parseInt(numStr);
                } catch (NumberFormatException e) {
                    return Long.parseLong(numStr);
                }
            }
        }

        private List<Object> parseArray() {
            pos++; // skip [
            List<Object> result = new ArrayList<Object>();
            skipWhitespace();
            if (pos < input.length() && input.charAt(pos) == ']') {
                pos++;
                return result;
            }
            result.add(parseValue());
            skipWhitespace();
            while (pos < input.length() && input.charAt(pos) == ',') {
                pos++;
                result.add(parseValue());
                skipWhitespace();
            }
            if (pos >= input.length() || input.charAt(pos) != ']') {
                throw new JmesPathException("Expected ']'");
            }
            pos++;
            return result;
        }

        private Map<String, Object> parseObject() {
            pos++; // skip {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            skipWhitespace();
            if (pos < input.length() && input.charAt(pos) == '}') {
                pos++;
                return result;
            }
            parseObjectEntry(result);
            skipWhitespace();
            while (pos < input.length() && input.charAt(pos) == ',') {
                pos++;
                parseObjectEntry(result);
                skipWhitespace();
            }
            if (pos >= input.length() || input.charAt(pos) != '}') {
                throw new JmesPathException("Expected '}'");
            }
            pos++;
            return result;
        }

        private void parseObjectEntry(Map<String, Object> result) {
            skipWhitespace();
            if (pos >= input.length() || input.charAt(pos) != '"') {
                throw new JmesPathException("Expected string key");
            }
            String key = parseString();
            skipWhitespace();
            if (pos >= input.length() || input.charAt(pos) != ':') {
                throw new JmesPathException("Expected ':'");
            }
            pos++;
            Object value = parseValue();
            result.put(key, value);
        }

        private void skipWhitespace() {
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    pos++;
                } else {
                    break;
                }
            }
        }

        private boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }
    }
}
