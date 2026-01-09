# Filters

Filter expressions let you select array elements based on conditions.

## Basic Syntax

```
array[?condition]
```

The `?` indicates a filter expression. Only elements where the condition is truthy are included.

```java
// {"people": [
//   {"name": "Alice", "age": 25},
//   {"name": "Bob", "age": 35},
//   {"name": "Charlie", "age": 30}
// ]}

JmesPath.search("people[?age > `30`]", data);
// [{"name": "Bob", "age": 35}]

JmesPath.search("people[?age >= `30`]", data);
// [{"name": "Bob", "age": 35}, {"name": "Charlie", "age": 30}]
```

## Comparison Operators

| Operator | Description |
|----------|-------------|
| `==` | Equal |
| `!=` | Not equal |
| `<` | Less than |
| `<=` | Less than or equal |
| `>` | Greater than |
| `>=` | Greater than or equal |

```java
JmesPath.search("people[?name == 'Alice']", data);
JmesPath.search("people[?age != `25`]", data);
JmesPath.search("people[?age < `30`]", data);
```

## Logical Operators

### AND (`&&`)

Both conditions must be true:

```java
JmesPath.search("people[?age > `25` && age < `35`]", data);
// [{"name": "Charlie", "age": 30}]
```

### OR (`||`)

Either condition can be true:

```java
JmesPath.search("people[?age < `26` || age > `34`]", data);
// [{"name": "Alice", "age": 25}, {"name": "Bob", "age": 35}]
```

### NOT (`!`)

Negate a condition:

```java
JmesPath.search("people[?!(age > `30`)]", data);
// [{"name": "Alice", "age": 25}, {"name": "Charlie", "age": 30}]
```

## String Comparisons

```java
// {"items": [
//   {"name": "apple"},
//   {"name": "banana"},
//   {"name": "apricot"}
// ]}

// Exact match (use raw strings with single quotes)
JmesPath.search("items[?name == 'apple']", data);

// Use functions for partial matches
JmesPath.search("items[?starts_with(name, 'ap')]", data);
// [{"name": "apple"}, {"name": "apricot"}]

JmesPath.search("items[?contains(name, 'an')]", data);
// [{"name": "banana"}]
```

## Filtering Nested Data

```java
// {
//   "orders": [
//     {"id": 1, "items": [{"product": "A", "qty": 2}]},
//     {"id": 2, "items": [{"product": "B", "qty": 5}]}
//   ]
// }

// Orders with any item quantity > 3
JmesPath.search("orders[?items[?qty > `3`]]", data);
// [{"id": 2, ...}]
```

## Filtering with Functions

```java
// {"files": [
//   {"name": "doc.pdf", "size": 1024},
//   {"name": "image.png", "size": 2048},
//   {"name": "readme.txt", "size": 512}
// ]}

// Files ending with .pdf
JmesPath.search("files[?ends_with(name, '.pdf')]", data);

// Files larger than average
JmesPath.search("files[?size > avg(files[*].size)]", data);
```

## Chaining Filters with Projections

After filtering, project specific fields:

```java
// Get just the names of people over 30
JmesPath.search("people[?age > `30`].name", data);
// ["Bob"]

// Get specific fields
JmesPath.search("people[?age > `25`].{n: name, a: age}", data);
// [{"n": "Bob", "a": 35}, {"n": "Charlie", "a": 30}]
```

## Truthiness

In filter expressions, these values are **falsy**:

- `null`
- `false`
- Empty string `""`
- Empty array `[]`
- Empty object `{}`

Everything else is **truthy**.

```java
// {"items": [
//   {"name": "A", "active": true},
//   {"name": "B", "active": false},
//   {"name": "C", "active": null}
// ]}

JmesPath.search("items[?active]", data);
// [{"name": "A", "active": true}]

JmesPath.search("items[?!active]", data);
// [{"name": "B", ...}, {"name": "C", ...}]
```

## Literal Values

Use backticks for JSON literals in comparisons:

```java
// Numbers
JmesPath.search("items[?count > `10`]", data);

// Booleans
JmesPath.search("items[?active == `true`]", data);

// Null
JmesPath.search("items[?value == `null`]", data);

// Strings (alternative to raw strings)
JmesPath.search("items[?name == `\"Alice\"`]", data);
```

!!! tip "Raw Strings"
    For string comparisons, raw strings with single quotes are cleaner:
    ```java
    JmesPath.search("items[?name == 'Alice']", data);
    ```
