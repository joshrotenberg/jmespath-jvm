# jmespath-jvm

A fast, spec-compliant [JMESPath](https://jmespath.org/) implementation for the JVM with zero dependencies in the core.

## Why jmespath-jvm?

- **Fast** - 4-10x faster parsing, 30-70% faster evaluation than alternatives
- **Complete** - Full JMESPath spec plus JEP-12 (raw strings) and JEP-18 (lexical scoping)
- **Zero dependencies** - Core library has no dependencies
- **Extensible** - Add custom functions easily
- **Any JSON library** - Jackson, Gson, or plain Maps - your choice

## Modules

| Module | Description |
|--------|-------------|
| `jmespath-core` | Core library, zero dependencies, uses Map/List |
| `jmespath-jackson` | Jackson integration with `JacksonRuntime` |
| `jmespath-gson` | Gson integration with `GsonRuntime` |

## Quick Example

=== "Jackson"

    ```java
    import io.jmespath.JmesPath;
    import io.jmespath.jackson.JacksonRuntime;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;

    ObjectMapper mapper = new ObjectMapper();
    JacksonRuntime runtime = new JacksonRuntime();

    JsonNode data = mapper.readTree("""
        {
          "people": [
            {"name": "Alice", "age": 25},
            {"name": "Bob", "age": 30},
            {"name": "Charlie", "age": 35}
          ]
        }
        """);

    // Find names of people over 28
    JsonNode result = JmesPath.search(runtime, "people[?age > `28`].name", data);
    // Returns: ["Bob", "Charlie"]
    ```

=== "Gson"

    ```java
    import io.jmespath.JmesPath;
    import io.jmespath.gson.GsonRuntime;
    import com.google.gson.JsonElement;
    import com.google.gson.JsonParser;

    GsonRuntime runtime = new GsonRuntime();

    JsonElement data = JsonParser.parseString("""
        {
          "people": [
            {"name": "Alice", "age": 25},
            {"name": "Bob", "age": 30},
            {"name": "Charlie", "age": 35}
          ]
        }
        """);

    // Find names of people over 28
    JsonElement result = JmesPath.search(runtime, "people[?age > `28`].name", data);
    // Returns: ["Bob", "Charlie"]
    ```

=== "Core (Map/List)"

    ```java
    import io.jmespath.JmesPath;

    // Given this JSON data as Map/List:
    // {
    //   "people": [
    //     {"name": "Alice", "age": 25},
    //     {"name": "Bob", "age": 30},
    //     {"name": "Charlie", "age": 35}
    //   ]
    // }

    // Find names of people over 28
    Object result = JmesPath.search("people[?age > `28`].name", data);
    // Returns: ["Bob", "Charlie"]
    ```

## Installation

=== "Jackson"

    ```xml
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>jmespath-jackson</artifactId>
        <version>1.0.3</version>
    </dependency>
    ```

=== "Gson"

    ```xml
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>jmespath-gson</artifactId>
        <version>1.0.3</version>
    </dependency>
    ```

=== "Core only"

    ```xml
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>jmespath-core</artifactId>
        <version>1.0.3</version>
    </dependency>
    ```

## What is JMESPath?

JMESPath is a query language for JSON. It lets you extract, transform, and filter data from JSON documents using a simple, declarative syntax.

```
people[?age > `21`].name | sort(@) | [0]
```

This expression:

1. Filters `people` to those with `age > 21`
2. Extracts just the `name` field from each
3. Sorts the names
4. Returns the first one

Learn more at [jmespath.org](https://jmespath.org/).

## Next Steps

- [Getting Started](getting-started.md) - Installation and first queries
- [User Guide](guide/basics.md) - Learn the query language
- [API Reference](https://joshrotenberg.github.io/jmespath-jvm/api/) - Javadoc documentation
