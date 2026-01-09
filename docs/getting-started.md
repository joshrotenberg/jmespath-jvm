# Getting Started

## Installation

Add jmespath-jvm to your project:

=== "Maven"

    ```xml
    <dependency>
        <groupId>io.jmespath</groupId>
        <artifactId>jmespath-jvm</artifactId>
        <version>0.1.0</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy
    implementation 'io.jmespath:jmespath-jvm:0.1.0'
    ```

## Basic Usage

### One-liner Queries

The simplest way to use jmespath-jvm:

```java
import io.jmespath.JmesPath;
import java.util.*;

// Create some data
Map<String, Object> data = new HashMap<>();
data.put("name", "Alice");
data.put("age", 30);

// Query it
String name = (String) JmesPath.search("name", data);  // "Alice"
```

### Compiled Expressions

For repeated queries, compile once and reuse:

```java
import io.jmespath.JmesPath;
import io.jmespath.Expression;

// Compile the expression
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
List<?> names = (List<?>) JmesPath.search("people[*].name", data);
// ["Alice", "Bob"]

// Get first person's name
String first = (String) JmesPath.search("people[0].name", data);
// "Alice"

// Get last person
Map<?,?> last = (Map<?,?>) JmesPath.search("people[-1]", data);
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
