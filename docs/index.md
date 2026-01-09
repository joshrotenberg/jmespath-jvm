# jmespath-jvm

A fast, spec-compliant [JMESPath](https://jmespath.org/) implementation for the JVM with zero dependencies.

## Why jmespath-jvm?

- **Fast** - 4-10x faster parsing, 30-70% faster evaluation than alternatives
- **Complete** - Full JMESPath spec plus JEP-12 (raw strings) and JEP-18 (lexical scoping)
- **Zero dependencies** - Just the JDK, nothing else
- **Extensible** - Add custom functions easily
- **Any JSON library** - Jackson, Gson, or plain Maps - your choice

## Quick Example

```java
import io.jmespath.JmesPath;

// Given this JSON data:
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

=== "Maven"

    ```xml
    <dependency>
        <groupId>io.github.joshrotenberg</groupId>
        <artifactId>jmespath-jvm</artifactId>
        <version>0.1.0</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy
    implementation 'io.github.joshrotenberg:jmespath-jvm:0.1.0'
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
- [API Reference](https://jmespath.github.io/jmespath-jvm/api/) - Javadoc documentation
