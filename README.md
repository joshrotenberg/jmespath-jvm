# JMESPath Java

A zero-dependency, spec-compliant [JMESPath](https://jmespath.org/) implementation for Java 7+.

## Features

- **Zero dependencies** - No external runtime dependencies
- **Spec compliant** - Implements the full JMESPath specification
- **Java 8+** - Works on Java 8 and all later versions
- **Thread-safe** - Compiled expressions are immutable and thread-safe
- **Extensible** - Add custom functions via the extension API
- **JSON-agnostic** - Works with any JSON library via the Runtime interface

## Installation

### Maven

```xml
<dependency>
    <groupId>io.jmespath</groupId>
    <artifactId>jmespath</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.jmespath:jmespath:0.1.0'
```

## Quick Start

```java
import io.jmespath.JmesPath;

// Simple query
Object result = JmesPath.search("people[0].name", jsonData);

// Compile once, use many times
Expression<Object> expr = JmesPath.compile("people[?age > `21`].name");
Object result1 = expr.search(data1);
Object result2 = expr.search(data2);
```

## Usage

### Basic Queries

```java
import io.jmespath.JmesPath;

// Given JSON: {"name": "John", "age": 30}
String name = (String) JmesPath.search("name", data);  // "John"
Long age = (Long) JmesPath.search("age", data);        // 30

// Given JSON: {"people": [{"name": "Alice"}, {"name": "Bob"}]}
List<?> names = (List<?>) JmesPath.search("people[*].name", data);  // ["Alice", "Bob"]
```

### Compiled Expressions

For better performance when reusing expressions:

```java
import io.jmespath.Expression;
import io.jmespath.JmesPath;

// Compile once
Expression<Object> expr = JmesPath.compile("locations[?state == 'WA'].name | sort(@)");

// Use many times
Object result1 = expr.search(dataset1);
Object result2 = expr.search(dataset2);
```

### Filter Expressions

```java
// Filter people over 21
JmesPath.search("people[?age > `21`]", data);

// Filter by string value
JmesPath.search("people[?state == 'WA']", data);

// Combine filters
JmesPath.search("people[?age > `21` && state == 'WA']", data);
```

### Projections

```java
// List projection
JmesPath.search("people[*].name", data);

// Object projection (get all values)
JmesPath.search("*.age", data);

// Flatten projection
JmesPath.search("people[].addresses[]", data);
```

### Multi-select

```java
// Select specific fields as a list
JmesPath.search("people[*].[name, age]", data);

// Select as an object with new keys
JmesPath.search("people[*].{fullName: name, years: age}", data);
```

### Pipe Expressions

```java
// Chain operations
JmesPath.search("people[*].name | sort(@) | [0]", data);
```

### Built-in Functions

The library includes all 26 JMESPath standard functions:

| Function | Description |
|----------|-------------|
| `abs(n)` | Absolute value |
| `avg(arr)` | Average of numbers |
| `ceil(n)` | Ceiling |
| `contains(subject, search)` | Check if array/string contains value |
| `ends_with(str, suffix)` | Check string suffix |
| `floor(n)` | Floor |
| `join(glue, arr)` | Join strings |
| `keys(obj)` | Get object keys |
| `length(subject)` | Length of array/string/object |
| `map(expr, arr)` | Apply expression to each element |
| `max(arr)` | Maximum value |
| `max_by(arr, expr)` | Maximum by expression |
| `merge(obj1, obj2, ...)` | Merge objects |
| `min(arr)` | Minimum value |
| `min_by(arr, expr)` | Minimum by expression |
| `not_null(val1, val2, ...)` | First non-null value |
| `reverse(arr_or_str)` | Reverse array or string |
| `sort(arr)` | Sort array |
| `sort_by(arr, expr)` | Sort by expression |
| `starts_with(str, prefix)` | Check string prefix |
| `sum(arr)` | Sum of numbers |
| `to_array(val)` | Convert to array |
| `to_number(val)` | Convert to number |
| `to_string(val)` | Convert to string |
| `type(val)` | Get type name |
| `values(obj)` | Get object values |

```java
// Function examples
JmesPath.search("length(people)", data);
JmesPath.search("sort_by(people, &age)", data);
JmesPath.search("max_by(people, &age).name", data);
JmesPath.search("join(', ', people[*].name)", data);
```

## Custom Functions

You can extend JMESPath with custom functions:

### Using FunctionBuilder

```java
import io.jmespath.function.ExtensionRegistry;
import io.jmespath.function.FunctionBuilder;
import io.jmespath.function.ArgumentType;
import io.jmespath.JmesPath;
import io.jmespath.runtime.MapRuntime;

// Create a registry with default functions plus custom ones
ExtensionRegistry registry = ExtensionRegistry.withDefaults()
    .register(FunctionBuilder.function("double")
        .arg(ArgumentType.NUMBER)
        .body(new FunctionBuilder.FunctionBody() {
            @Override
            public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
                Number n = (Number) args.get(0);
                return runtime.createNumber(n.doubleValue() * 2);
            }
        })
        .build());

// Use the custom runtime
MapRuntime runtime = new MapRuntime(registry);
Expression<Object> expr = JmesPath.compile("double(age)");
Object result = expr.evaluate(runtime, data);
```

### Extension Modules

For organizing multiple related functions:

```java
import io.jmespath.function.extension.StringExtensions;

// Add string extension functions: upper, lower, trim, split, replace, etc.
ExtensionRegistry registry = ExtensionRegistry.withDefaults()
    .withModule(new StringExtensions());
```

### Available Extension Modules

- **StringExtensions** - String manipulation functions:
  - `upper(str)` - Convert to uppercase
  - `lower(str)` - Convert to lowercase  
  - `trim(str)` - Trim whitespace
  - `trim_left(str)` - Trim leading whitespace
  - `trim_right(str)` - Trim trailing whitespace
  - `split(str, delimiter)` - Split string into array
  - `replace(str, old, new)` - Replace occurrences
  - `pad_left(str, width, char)` - Left pad string
  - `pad_right(str, width, char)` - Right pad string

## Custom JSON Libraries

By default, JMESPath Java uses `Map<String, Object>` and `List<Object>` for JSON. You can integrate with any JSON library by implementing the `Runtime` interface:

```java
import io.jmespath.Runtime;

public class JacksonRuntime implements Runtime<JsonNode> {
    // Implement type checking
    @Override
    public boolean isNull(JsonNode value) {
        return value == null || value.isNull();
    }

    @Override
    public boolean isObject(JsonNode value) {
        return value != null && value.isObject();
    }

    // ... implement other methods
}
```

## Thread Safety

- `Expression` instances are immutable and thread-safe
- `JmesPath.compile()` can be called from multiple threads
- `Runtime` implementations should be thread-safe if shared

## Error Handling

```java
import io.jmespath.JmesPathException;

try {
    JmesPath.compile("invalid[expression");
} catch (JmesPathException e) {
    System.out.println("Error at position " + e.getPosition() + ": " + e.getMessage());
}
```

## JMESPath Specification

This implementation follows the [JMESPath specification](https://jmespath.org/specification.html). For a tutorial and examples, see the [JMESPath Tutorial](https://jmespath.org/tutorial.html).

## License

Dual-licensed under Apache 2.0 and MIT. Choose whichever fits your needs.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
