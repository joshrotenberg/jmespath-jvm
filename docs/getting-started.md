# Getting Started

## Installation

Choose the module that matches your JSON library:

=== "Jackson"

    ```xml
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>jmespath-jackson</artifactId>
        <version>1.0.3</version>
    </dependency>
    ```

    ```groovy
    implementation 'io.github.joshrotenberg:jmespath-jackson:1.0.3'
    ```

=== "Gson"

    ```xml
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>jmespath-gson</artifactId>
        <version>1.0.3</version>
    </dependency>
    ```

    ```groovy
    implementation 'io.github.joshrotenberg:jmespath-gson:1.0.3'
    ```

=== "Core (Map/List)"

    ```xml
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>jmespath-core</artifactId>
        <version>1.0.3</version>
    </dependency>
    ```

    ```groovy
    implementation 'io.github.joshrotenberg:jmespath-core:1.0.3'
    ```

## Basic Usage

### With Jackson

```java
import io.jmespath.JmesPath;
import io.jmespath.Expression;
import io.jmespath.jackson.JacksonRuntime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper mapper = new ObjectMapper();
JacksonRuntime runtime = new JacksonRuntime();

JsonNode data = mapper.readTree("""
    {"name": "Alice", "age": 30}
    """);

// One-liner query
JsonNode name = JmesPath.search(runtime, "name", data);
// "Alice"

// Compiled expression (for repeated use)
Expression<JsonNode> expr = JmesPath.compile("name");
JsonNode result = expr.evaluate(runtime, data);
```

### With Gson

```java
import io.jmespath.JmesPath;
import io.jmespath.Expression;
import io.jmespath.gson.GsonRuntime;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

GsonRuntime runtime = new GsonRuntime();

JsonElement data = JsonParser.parseString("""
    {"name": "Alice", "age": 30}
    """);

// One-liner query
JsonElement name = JmesPath.search(runtime, "name", data);

// Compiled expression (for repeated use)
Expression<JsonElement> expr = JmesPath.compile("name");
JsonElement result = expr.evaluate(runtime, data);
```

### With Core (Map/List)

```java
import io.jmespath.JmesPath;
import io.jmespath.Expression;
import java.util.*;

// Create some data
Map<String, Object> data = new HashMap<>();
data.put("name", "Alice");
data.put("age", 30);

// One-liner query
String name = (String) JmesPath.search("name", data);  // "Alice"

// Compiled expression (for repeated use)
Expression<Object> expr = JmesPath.compile("name");
Object result = expr.search(data);
```

### Compiled Expressions

For repeated queries, compile once and reuse:

=== "Jackson"

    ```java
    Expression<JsonNode> expr = JmesPath.compile("people[?active].name");

    // Use it many times
    JsonNode result1 = expr.evaluate(runtime, dataset1);
    JsonNode result2 = expr.evaluate(runtime, dataset2);
    JsonNode result3 = expr.evaluate(runtime, dataset3);
    ```

=== "Gson"

    ```java
    Expression<JsonElement> expr = JmesPath.compile("people[?active].name");

    // Use it many times
    JsonElement result1 = expr.evaluate(runtime, dataset1);
    JsonElement result2 = expr.evaluate(runtime, dataset2);
    JsonElement result3 = expr.evaluate(runtime, dataset3);
    ```

=== "Core"

    ```java
    Expression<Object> expr = JmesPath.compile("people[?active].name");

    // Use it many times
    Object result1 = expr.search(dataset1);
    Object result2 = expr.search(dataset2);
    Object result3 = expr.search(dataset3);
    ```

Compiled expressions are:

- **Immutable** - safe to share across threads
- **Reusable** - no parsing overhead on each call
- **Fast** - optimized AST ready to evaluate

### Working with Arrays

```java
// Sample data
// {
//   "people": [
//     {"name": "Alice", "age": 25},
//     {"name": "Bob", "age": 30}
//   ]
// }

// Get all names
JmesPath.search("people[*].name", data);
// ["Alice", "Bob"]

// Get first person's name
JmesPath.search("people[0].name", data);
// "Alice"

// Get last person
JmesPath.search("people[-1]", data);
// {"name": "Bob", "age": 30}
```

### Filtering

```java
// People over 25
JmesPath.search("people[?age > `25`]", data);

// People named Alice
JmesPath.search("people[?name == 'Alice']", data);

// Combine conditions
JmesPath.search("people[?age > `20` && age < `30`]", data);
```

!!! note "Literals"
    Use backticks for JSON literals (`` `25` ``) and single quotes for raw strings (`'Alice'`).

### Using Functions

```java
// Count items
JmesPath.search("length(people)", data);  // 2

// Sort by age
JmesPath.search("sort_by(people, &age)", data);

// Get the oldest person's name
JmesPath.search("max_by(people, &age).name", data);

// Join names with comma
JmesPath.search("join(', ', people[*].name)", data);  // "Alice, Bob"
```

## Error Handling

```java
import io.jmespath.JmesPathException;

try {
    JmesPath.compile("invalid[expression");
} catch (JmesPathException e) {
    System.out.println("Error: " + e.getMessage());
    System.out.println("Position: " + e.getPosition());
    System.out.println("Line: " + e.getLine());
    System.out.println("Column: " + e.getColumn());
}
```

## Next Steps

- [Basic Queries](guide/basics.md) - Property access, indexes, slicing
- [Filters](guide/filters.md) - Filter expressions in depth
- [Functions](guide/functions.md) - All 26 built-in functions
