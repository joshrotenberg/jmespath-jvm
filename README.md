# jmespath-jvm

A fast, spec-compliant [JMESPath](https://jmespath.org/) implementation for the JVM with zero dependencies in the core.

## Highlights

- **Fast** - 4-10x faster parsing, 30-70% faster evaluation than alternatives
- **Complete** - Full spec + JEP-12 (raw strings) + JEP-18 (lexical scoping)
- **Zero dependencies** - Core library has no dependencies
- **Extensible** - Add custom functions easily
- **Any JSON library** - Jackson, Gson, or plain Maps - your choice

## Modules

| Module | Description |
|--------|-------------|
| `jmespath-core` | Core library, zero dependencies, uses Map/List |
| `jmespath-jackson` | Jackson integration with `JacksonRuntime` |
| `jmespath-gson` | Gson integration with `GsonRuntime` |

## Installation

### With Jackson (recommended)

```xml
<dependency>
    <groupId>io.github.joshrotenberg</groupId>
    <artifactId>jmespath-jackson</artifactId>
    <version>1.0.3</version>
</dependency>
```

### With Gson

```xml
<dependency>
    <groupId>io.github.joshrotenberg</groupId>
    <artifactId>jmespath-gson</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Core only (no JSON library)

```xml
<dependency>
    <groupId>io.github.joshrotenberg</groupId>
    <artifactId>jmespath-core</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Quick Start

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
    {
      "people": [
        {"name": "Alice", "age": 25},
        {"name": "Bob", "age": 30}
      ]
    }
    """);

// One-liner
JsonNode result = JmesPath.search(runtime, "people[?age > `25`].name", data);
// ["Bob"]

// Compile once, run many
Expression<JsonNode> expr = JmesPath.compile("people[*].name");
JsonNode names = expr.evaluate(runtime, data);
// ["Alice", "Bob"]
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
    {
      "people": [
        {"name": "Alice", "age": 25},
        {"name": "Bob", "age": 30}
      ]
    }
    """);

// One-liner
JsonElement result = JmesPath.search(runtime, "people[?age > `25`].name", data);

// Compile once, run many
Expression<JsonElement> expr = JmesPath.compile("people[*].name");
JsonElement names = expr.evaluate(runtime, data);
```

### Core (Map/List)

```java
import io.jmespath.JmesPath;

// Using Map/List data structures
Object result = JmesPath.search("people[?age > `21`].name", data);

// Compile once, run many
Expression<Object> expr = JmesPath.compile("locations[?state == 'WA'].name | sort(@)");
Object r1 = expr.search(data1);
Object r2 = expr.search(data2);
```

## Examples

```java
// Filters
JmesPath.search("people[?age > `21` && state == 'WA']", data);

// Projections
JmesPath.search("people[*].name", data);
JmesPath.search("reservations[].instances[].id", data);

// Multi-select
JmesPath.search("people[*].{name: name, age: age}", data);

// Functions
JmesPath.search("sort_by(people, &age) | [-1].name", data);
JmesPath.search("max_by(people, &age).name", data);

// Raw strings (JEP-12) - no escaping needed
JmesPath.search("data | [?pattern == 'foo\\bar']", data);

// Lexical scoping (JEP-18) - reference parent values in filters
JmesPath.search("let $min = `18` in people[?age >= $min].name", data);
```

## Built-in Functions

All 26 standard functions: `abs`, `avg`, `ceil`, `contains`, `ends_with`, `floor`, `join`, `keys`, `length`, `map`, `max`, `max_by`, `merge`, `min`, `min_by`, `not_null`, `reverse`, `sort`, `sort_by`, `starts_with`, `sum`, `to_array`, `to_number`, `to_string`, `type`, `values`

## Custom Functions

```java
FunctionRegistry registry = DefaultFunctionRegistry.withExtension(
    FunctionBuilder.function("double")
        .arg(ArgumentType.NUMBER)
        .body((runtime, args, current) -> {
            Number n = (Number) args.get(0);
            return runtime.createNumber(n.doubleValue() * 2);
        })
        .build()
);

MapRuntime runtime = new MapRuntime(registry);
Expression<Object> expr = JmesPath.compile("double(price)");
expr.evaluate(runtime, data);
```

## Why Another JMESPath Library?

The original [burtcorp/jmespath-java](https://github.com/burtcorp/jmespath-java) was archived in 2022 and is no longer maintained. It served the community well, but:

- Uses ANTLR for parsing (heavyweight dependency, slower)
- Missing JEP-12 (raw strings) and JEP-18 (lexical scoping)
- No longer receiving updates or bug fixes

This library is a clean-room implementation with a hand-written Pratt parser, zero dependencies in the core, and full spec compliance including the latest JMESPath enhancements.

## Performance

Compared to [burtcorp/jmespath-java](https://github.com/burtcorp/jmespath-java):

| Operation | Improvement |
|-----------|-------------|
| Parsing | 4-10x faster |
| Projections | 50-80% faster |
| Filters | 30-50% faster |
| Large data (10k items) | 25-30% faster |

Hand-written Pratt parser, no ANTLR overhead.

## License

Apache-2.0 OR MIT - your choice.

## Links

- [Documentation](https://joshrotenberg.github.io/jmespath-jvm/)
- [JMESPath Specification](https://jmespath.org/specification.html)
- [JMESPath Tutorial](https://jmespath.org/tutorial.html)
