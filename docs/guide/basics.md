# Basic Queries

## Property Access

Access object properties with dot notation:

```java
// {"name": "Alice", "address": {"city": "Seattle"}}

JmesPath.search("name", data);           // "Alice"
JmesPath.search("address.city", data);   // "Seattle"
JmesPath.search("missing", data);        // null
```

### Quoted Identifiers

For property names with special characters, use quotes:

```java
// {"foo-bar": 1, "with spaces": 2}

JmesPath.search("\"foo-bar\"", data);      // 1
JmesPath.search("\"with spaces\"", data);  // 2
```

## Array Access

### Index Access

```java
// {"items": ["a", "b", "c", "d", "e"]}

JmesPath.search("items[0]", data);   // "a" (first)
JmesPath.search("items[2]", data);   // "c" (third)
JmesPath.search("items[-1]", data);  // "e" (last)
JmesPath.search("items[-2]", data);  // "d" (second to last)
```

### Slicing

Extract a range of elements with `[start:stop:step]`:

```java
// {"items": [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]}

JmesPath.search("items[0:5]", data);    // [0, 1, 2, 3, 4]
JmesPath.search("items[5:]", data);     // [5, 6, 7, 8, 9]
JmesPath.search("items[:3]", data);     // [0, 1, 2]
JmesPath.search("items[::2]", data);    // [0, 2, 4, 6, 8] (every other)
JmesPath.search("items[::-1]", data);   // [9, 8, 7, ...] (reversed)
```

## Wildcard Expressions

### List Wildcard `[*]`

Apply an expression to every element:

```java
// {"people": [{"name": "Alice"}, {"name": "Bob"}]}

JmesPath.search("people[*].name", data);  // ["Alice", "Bob"]
```

### Object Wildcard `*`

Get all values from an object:

```java
// {"a": 1, "b": 2, "c": 3}

JmesPath.search("*", data);  // [1, 2, 3]
```

### Nested Wildcards

```java
// {
//   "departments": {
//     "engineering": {"headcount": 50},
//     "sales": {"headcount": 30}
//   }
// }

JmesPath.search("departments.*.headcount", data);  // [50, 30]
```

## Flatten Operator `[]`

Flatten nested arrays by one level:

```java
// {"matrix": [[1, 2], [3, 4], [5, 6]]}

JmesPath.search("matrix[]", data);  // [1, 2, 3, 4, 5, 6]
```

Commonly used to flatten projection results:

```java
// {
//   "reservations": [
//     {"instances": [{"id": "i-1"}, {"id": "i-2"}]},
//     {"instances": [{"id": "i-3"}]}
//   ]
// }

// Without flatten - nested arrays
JmesPath.search("reservations[*].instances[*].id", data);
// [["i-1", "i-2"], ["i-3"]]

// With flatten - single array
JmesPath.search("reservations[].instances[].id", data);
// ["i-1", "i-2", "i-3"]
```

## Multi-Select

### Multi-Select List `[expr, expr, ...]`

Create an array from multiple expressions:

```java
// {"name": "Alice", "age": 30, "city": "Seattle"}

JmesPath.search("[name, age]", data);  // ["Alice", 30]
```

### Multi-Select Hash `{key: expr, ...}`

Create an object with renamed keys:

```java
JmesPath.search("{fullName: name, years: age}", data);
// {"fullName": "Alice", "years": 30}
```

Combine with projections:

```java
// {"people": [{"firstName": "Alice", "lastName": "Smith"}]}

JmesPath.search("people[*].{name: firstName, surname: lastName}", data);
// [{"name": "Alice", "surname": "Smith"}]
```

## Current Node `@`

Reference the current node being evaluated:

```java
// {"items": [1, 2, 3]}

JmesPath.search("items[*] | sort(@)", data);  // [1, 2, 3]
```

Useful in filter expressions:

```java
// {"values": [1, 5, 3, 8, 2]}

JmesPath.search("values[?@ > `3`]", data);  // [5, 8]
```

## Next Steps

- [Filters](filters.md) - Filter expressions with conditions
- [Projections](projections.md) - Advanced projection patterns
- [Pipes](pipes.md) - Chaining expressions
