# Custom Functions

Extend jmespath-jvm with your own functions.

## Quick Start

```java
import io.jmespath.function.*;
import io.jmespath.runtime.MapRuntime;
import io.jmespath.JmesPath;

// Create a custom function
Function doubleIt = FunctionBuilder.function("double")
    .arg(ArgumentType.NUMBER)
    .body((runtime, args, current) -> {
        Number n = (Number) args.get(0);
        return runtime.createNumber(n.doubleValue() * 2);
    })
    .build();

// Register it
FunctionRegistry registry = DefaultFunctionRegistry.withExtension(doubleIt);
MapRuntime runtime = new MapRuntime(registry);

// Use it
Expression<Object> expr = JmesPath.compile("double(price)");
Object result = expr.evaluate(runtime, data);  // price * 2
```

## Function Builder API

### Argument Types

| Type | Accepts |
|------|---------|
| `ANY` | Any value |
| `STRING` | String values |
| `NUMBER` | Numeric values |
| `BOOLEAN` | Boolean values |
| `ARRAY` | Array values |
| `OBJECT` | Object values |
| `EXPRESSION` | Expression reference (`&expr`) |
| `ARRAY_STRING` | Array of strings |
| `ARRAY_NUMBER` | Array of numbers |

### Required Arguments

```java
FunctionBuilder.function("add")
    .arg(ArgumentType.NUMBER)
    .arg(ArgumentType.NUMBER)
    .body((runtime, args, current) -> {
        double a = ((Number) args.get(0)).doubleValue();
        double b = ((Number) args.get(1)).doubleValue();
        return runtime.createNumber(a + b);
    })
    .build();
```

### Optional Arguments

```java
FunctionBuilder.function("pad")
    .arg(ArgumentType.STRING)
    .arg(ArgumentType.NUMBER)
    .optionalArg(ArgumentType.STRING)  // optional padding char
    .body((runtime, args, current) -> {
        String s = (String) args.get(0);
        int width = ((Number) args.get(1)).intValue();
        String pad = args.size() > 2 ? (String) args.get(2) : " ";
        // ... padding logic
        return runtime.createString(result);
    })
    .build();
```

### Variadic Arguments

```java
FunctionBuilder.function("concat")
    .varargs(ArgumentType.STRING)
    .body((runtime, args, current) -> {
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append((String) arg);
        }
        return runtime.createString(sb.toString());
    })
    .build();
```

## Expression Arguments

For functions like `sort_by` that take expression references:

```java
FunctionBuilder.function("count_by")
    .arg(ArgumentType.ARRAY)
    .arg(ArgumentType.EXPRESSION)
    .body((runtime, args, current) -> {
        List<?> array = (List<?>) args.get(0);
        ExpressionRefNode expr = (ExpressionRefNode) args.get(1);
        
        Map<Object, Integer> counts = new HashMap<>();
        for (Object item : array) {
            Object key = expr.evaluate(runtime, item);
            counts.merge(key, 1, Integer::sum);
        }
        
        return runtime.createObject(counts);
    })
    .build();
```

## Implementing Function Interface

For more control, implement `Function` directly:

```java
public class UpperFunction implements Function {
    
    @Override
    public String getName() {
        return "upper";
    }
    
    @Override
    public Signature getSignature() {
        return new Signature(
            new Argument(ArgumentType.STRING)
        );
    }
    
    @Override
    public <T> T call(Runtime<T> runtime, List<Object> args, T current) {
        String s = (String) args.get(0);
        return runtime.createString(s.toUpperCase());
    }
}
```

## Registering Functions

### Single Function

```java
FunctionRegistry registry = DefaultFunctionRegistry.withExtension(myFunction);
```

### Multiple Functions

```java
FunctionRegistry registry = DefaultFunctionRegistry.withExtensions(
    func1, func2, func3
);
```

### Custom Registry

```java
FunctionRegistry registry = new FunctionRegistry();
registry.register(new LengthFunction());
registry.register(new SortFunction());
registry.register(myCustomFunction);
// Only these functions are available
```

## Using Custom Runtime

```java
// Create runtime with custom functions
MapRuntime runtime = new MapRuntime(registry);

// Compile expression
Expression<Object> expr = JmesPath.compile("upper(name)");

// Evaluate with custom runtime
Object result = expr.evaluate(runtime, data);
```

## Example: String Functions

```java
Function upper = FunctionBuilder.function("upper")
    .arg(ArgumentType.STRING)
    .body((runtime, args, current) -> 
        runtime.createString(((String) args.get(0)).toUpperCase()))
    .build();

Function lower = FunctionBuilder.function("lower")
    .arg(ArgumentType.STRING)
    .body((runtime, args, current) -> 
        runtime.createString(((String) args.get(0)).toLowerCase()))
    .build();

Function trim = FunctionBuilder.function("trim")
    .arg(ArgumentType.STRING)
    .body((runtime, args, current) -> 
        runtime.createString(((String) args.get(0)).trim()))
    .build();

FunctionRegistry registry = DefaultFunctionRegistry.withExtensions(
    upper, lower, trim
);
```

## Example: Math Functions

```java
Function round = FunctionBuilder.function("round")
    .arg(ArgumentType.NUMBER)
    .optionalArg(ArgumentType.NUMBER)
    .body((runtime, args, current) -> {
        double n = ((Number) args.get(0)).doubleValue();
        int places = args.size() > 1 ? ((Number) args.get(1)).intValue() : 0;
        double factor = Math.pow(10, places);
        return runtime.createNumber(Math.round(n * factor) / factor);
    })
    .build();

Function sqrt = FunctionBuilder.function("sqrt")
    .arg(ArgumentType.NUMBER)
    .body((runtime, args, current) -> {
        double n = ((Number) args.get(0)).doubleValue();
        return runtime.createNumber(Math.sqrt(n));
    })
    .build();
```

## Error Handling

Throw `JmesPathException` for runtime errors:

```java
FunctionBuilder.function("divide")
    .arg(ArgumentType.NUMBER)
    .arg(ArgumentType.NUMBER)
    .body((runtime, args, current) -> {
        double a = ((Number) args.get(0)).doubleValue();
        double b = ((Number) args.get(1)).doubleValue();
        if (b == 0) {
            throw new JmesPathException("divide: division by zero");
        }
        return runtime.createNumber(a / b);
    })
    .build();
```
