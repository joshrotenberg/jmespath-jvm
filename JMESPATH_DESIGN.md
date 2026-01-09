# JMESPath Implementation Design

A zero-dependency JMESPath implementation for Java 7, designed as an optional extension for the Redis client.

## Overview

JMESPath is a query language for JSON. This implementation provides:

- **Spec-compliant core** - All 26 built-in functions, full grammar support
- **User-provided JSON adapter** - No JSON library dependency; users implement `JmesPathRuntime<T>` for their library
- **Compiled expressions** - Parse once, evaluate many times, thread-safe
- **Optional extensions** - Additional functions beyond the spec (string manipulation, math, datetime, etc.)

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         User Code                               │
│                                                                 │
│  JmesPathRuntime<JsonNode> runtime = new JacksonRuntime();     │
│  Expression<JsonNode> expr = JmesPath.compile("users[?active]");│
│  JsonNode result = expr.evaluate(runtime, data);                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      jmespath package                           │
│                                                                 │
│  ┌──────────┐    ┌──────────┐    ┌─────────────────────────┐   │
│  │  Lexer   │ →  │  Parser  │ →  │  AST Nodes              │   │
│  │          │    │  (Pratt) │    │  - immutable            │   │
│  └──────────┘    └──────────┘    │  - evaluate(runtime, T) │   │
│                                   └─────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  JmesPathRuntime<T> (interface user implements)         │   │
│  │                                                          │   │
│  │  Type checking:  isNull, isArray, isObject, isString    │   │
│  │  Extraction:     getProperty, getIndex, arrayElements   │   │
│  │  Construction:   createArray, createObject, createNull  │   │
│  │  Comparison:     compare, deepEquals, isTruthy          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  FunctionRegistry                                        │   │
│  │  - 26 standard built-ins                                 │   │
│  │  - extensible via register(Function)                     │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## Package Structure

```
com.redis.client.jmespath/
├── JmesPath.java                 # Entry point: compile(expr)
├── Expression.java               # Compiled expression
├── JmesPathRuntime.java          # Interface users implement
├── JmesPathException.java        # Parse/evaluation errors
├── JmesPathType.java             # Enum: NULL, BOOLEAN, NUMBER, STRING, ARRAY, OBJECT
│
├── internal/
│   ├── Lexer.java
│   ├── Token.java
│   ├── TokenType.java
│   ├── Parser.java               # Pratt parser
│   └── ast/
│       ├── AstNode.java
│       ├── IdentifierNode.java
│       ├── IndexNode.java
│       ├── SliceNode.java
│       ├── FilterNode.java
│       ├── FlattenNode.java
│       ├── ProjectionNode.java
│       ├── SubExpressionNode.java
│       ├── PipeNode.java
│       ├── OrNode.java
│       ├── AndNode.java
│       ├── NotNode.java
│       ├── ComparatorNode.java
│       ├── MultiSelectListNode.java
│       ├── MultiSelectHashNode.java
│       ├── FunctionCallNode.java
│       ├── LiteralNode.java
│       ├── CurrentNode.java
│       └── ExpressionRefNode.java
│
├── function/
│   ├── Function.java
│   ├── FunctionRegistry.java
│   └── builtin/                  # 26 standard functions
│       ├── AbsFunction.java
│       ├── LengthFunction.java
│       ├── SortByFunction.java
│       └── ...
│
└── extensions/                   # Optional
    ├── StringFunctions.java      # upper, lower, split, trim
    ├── MathFunctions.java        # round, sqrt, pow
    ├── DateTimeFunctions.java    # now, format_date
    └── ...
```

## Core Interface

```java
public interface JmesPathRuntime<T> {
    
    // === Type Checking ===
    boolean isNull(T value);
    boolean isBoolean(T value);
    boolean isNumber(T value);
    boolean isString(T value);
    boolean isArray(T value);
    boolean isObject(T value);
    JmesPathType typeOf(T value);
    
    // === Value Extraction ===
    T getProperty(T object, String name);
    T getIndex(T array, int index);  // supports negative
    int getArrayLength(T array);
    Iterable<T> getArrayElements(T array);
    Iterable<String> getObjectKeys(T object);
    Iterable<T> getObjectValues(T object);
    
    // === Conversion to Java Types ===
    boolean toBoolean(T value);
    Number toNumber(T value);
    String toString(T value);
    
    // === Value Construction ===
    T createNull();
    T createBoolean(boolean value);
    T createNumber(Number value);
    T createString(String value);
    T createArray(List<T> elements);
    T createObject(Map<String, T> properties);
    
    // === Comparison ===
    int compare(T a, T b);
    boolean deepEquals(T a, T b);
    boolean isTruthy(T value);  // JMESPath truthiness rules
    
    // === JSON Parsing (for literals) ===
    T parseJson(String json);
}
```

## Pratt Parser

Operator precedence (lowest to highest):

| Level | Operators | Associativity |
|-------|-----------|---------------|
| 1 | `\|` (pipe) | Right |
| 2 | `\|\|` (or) | Left |
| 3 | `&&` (and) | Left |
| 4 | `!` (not) | Prefix |
| 5 | `== != < <= > >=` | Left |
| 6 | `[]` (flatten) | Left |
| 7 | `[n] [*] [?] [:]` | Left |
| 8 | `.` (dot) | Left |
| 9 | Atoms | - |

Key parsing concepts:

- **nud (null denotation)**: How token behaves at start of expression (prefix)
- **led (left denotation)**: How token behaves after left operand (infix)
- **Projections**: `[*]`, `[?...]`, `[]` create projection context where subsequent expressions apply to each element

## 26 Standard Functions

| Function | Signature |
|----------|-----------|
| `abs` | `abs(number) → number` |
| `avg` | `avg(array[number]) → number` |
| `ceil` | `ceil(number) → number` |
| `contains` | `contains(array\|string, any) → boolean` |
| `ends_with` | `ends_with(string, string) → boolean` |
| `floor` | `floor(number) → number` |
| `join` | `join(string, array[string]) → string` |
| `keys` | `keys(object) → array[string]` |
| `length` | `length(string\|array\|object) → number` |
| `map` | `map(&expr, array) → array` |
| `max` | `max(array[number\|string]) → number\|string` |
| `max_by` | `max_by(array, &expr) → any` |
| `merge` | `merge(object...) → object` |
| `min` | `min(array[number\|string]) → number\|string` |
| `min_by` | `min_by(array, &expr) → any` |
| `not_null` | `not_null(any...) → any` |
| `reverse` | `reverse(string\|array) → string\|array` |
| `sort` | `sort(array[number\|string]) → array` |
| `sort_by` | `sort_by(array, &expr) → array` |
| `starts_with` | `starts_with(string, string) → boolean` |
| `sum` | `sum(array[number]) → number` |
| `to_array` | `to_array(any) → array` |
| `to_number` | `to_number(any) → number\|null` |
| `to_string` | `to_string(any) → string` |
| `type` | `type(any) → string` |
| `values` | `values(object) → array` |

## Extension Functions (Optional)

### String Functions
```
upper(string) → string
lower(string) → string
trim(string) → string
split(string, string) → array[string]
replace(string, string, string) → string
substring(string, number, number?) → string
```

### Math Functions
```
round(number, number?) → number
sqrt(number) → number
pow(number, number) → number
median(array[number]) → number
```

### DateTime Functions
```
now() → number
format_date(number, string) → string
parse_date(string, string) → number
```

### Array Functions
```
first(array) → any
last(array) → any
unique(array) → array
range(number, number?, number?) → array[number]
```

## Redis Client Integration

```java
// Direct use
RedisJson json = client.json();
String data = json.get("user:1", "$");

JmesPathRuntime<Object> runtime = new MapRuntime();
Expression<Object> expr = JmesPath.compile("friends[?active].name | sort(@)");
Object result = expr.evaluate(runtime, parseJson(data));

// Or with typed results
public class JsonGetResult<T> {
    private final String raw;
    private final JmesPathRuntime<T> runtime;
    
    public T query(String jmesPath) {
        Expression<T> expr = JmesPath.compile(jmesPath);
        return expr.evaluate(runtime, runtime.parseJson(raw));
    }
}
```

## Implementation Notes

### Java 7 Constraints
- No lambdas → anonymous inner classes
- No Optional → null checks
- No streams → explicit iteration
- Diamond operator `<>` works
- Try-with-resources works

### Thread Safety
- `Expression` is immutable after construction
- `FunctionRegistry` is effectively immutable after configuration
- `JmesPathRuntime` thread-safety is user's responsibility

### Error Handling
- `JmesPathException` with position info for parse errors
- Clear messages for type mismatches at runtime
- Option for lenient mode (return null instead of throw)

## Test Compliance

Use official JMESPath compliance test suite from jmespath.org to verify:
- All expression types parse correctly
- All 26 built-ins behave correctly
- Edge cases (empty arrays, null propagation, etc.)

## Future Considerations

- Expression caching with weak references
- Streaming evaluation for large arrays
- Debug mode with evaluation trace
- Integration with Redis Search for server-side JMESPath (your Rust module!)
