# jmespath-jvm

A fast, spec-compliant [JMESPath](https://jmespath.org/) implementation for the JVM with zero dependencies.

## Highlights

- **Fast** - 4-10x faster parsing, 30-70% faster evaluation than alternatives
- **Complete** - Full spec + JEP-12 (raw strings) + JEP-18 (lexical scoping)
- **Zero dependencies** - Just the JDK, nothing else
- **Extensible** - Add custom functions easily
- **Any JSON library** - Jackson, Gson, or plain Maps - your choice

## Installation

```xml
<dependency>
    <groupId>io.jmespath</groupId>
    <artifactId>jmespath-jvm</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

```java
import io.jmespath.JmesPath;

// One-liner
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

## Custom JSON Libraries

Default uses `Map`/`List`. For Jackson, Gson, etc., implement `Runtime<T>`:

```java
public class JacksonRuntime implements Runtime<JsonNode> {
    @Override
    public boolean isObject(JsonNode value) {
        return value != null && value.isObject();
    }
    
    @Override
    public JsonNode getProperty(JsonNode object, String name) {
        return object.get(name);
    }
    
    // ... other methods
}
```

## Performance

Compared to the archived [burtcorp/jmespath-java](https://github.com/burtcorp/jmespath-java):

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

- [Documentation](https://jmespath.github.io/jmespath-jvm/)
- [JMESPath Specification](https://jmespath.org/specification.html)
- [JMESPath Tutorial](https://jmespath.org/tutorial.html)
