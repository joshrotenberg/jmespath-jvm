# Projections

Projections apply an expression to every element in an array, collecting results into a new array.

## List Projections `[*]`

The wildcard `[*]` creates a projection:

```java
// {"people": [
//   {"name": "Alice", "age": 25},
//   {"name": "Bob", "age": 30}
// ]}

JmesPath.search("people[*].name", data);
// ["Alice", "Bob"]

JmesPath.search("people[*].age", data);
// [25, 30]
```

## Object Projections `*`

Project over object values:

```java
// {"scores": {"math": 90, "science": 85, "english": 92}}

JmesPath.search("scores.*", data);
// [90, 85, 92]
```

## Slice Projections `[start:stop]`

Slices also create projections:

```java
// {"items": [
//   {"id": 1}, {"id": 2}, {"id": 3}, {"id": 4}, {"id": 5}
// ]}

JmesPath.search("items[:3].id", data);
// [1, 2, 3]

JmesPath.search("items[2:].id", data);
// [3, 4, 5]
```

## Filter Projections `[?expr]`

Filters are projections too:

```java
JmesPath.search("people[?age > `25`].name", data);
// ["Bob"]
```

## Flatten Projections `[]`

The flatten operator creates a projection that also flattens:

```java
// {"groups": [
//   {"members": ["Alice", "Bob"]},
//   {"members": ["Charlie"]}
// ]}

// With [*] - nested arrays
JmesPath.search("groups[*].members", data);
// [["Alice", "Bob"], ["Charlie"]]

// With [] - flattened
JmesPath.search("groups[].members", data);
// ["Alice", "Bob", "Charlie"]
```

## Projection Chaining

Projections can be chained:

```java
// {"data": [
//   {"items": [{"val": 1}, {"val": 2}]},
//   {"items": [{"val": 3}]}
// ]}

JmesPath.search("data[*].items[*].val", data);
// [[1, 2], [3]]

JmesPath.search("data[].items[].val", data);
// [1, 2, 3]
```

## Stopping Projections

Some expressions stop projections:

| Expression | Stops Projection? |
|------------|-------------------|
| `[n]` (index) | Yes |
| `[*]` (wildcard) | No |
| `[]` (flatten) | No |
| `[?expr]` (filter) | No |
| `[start:stop]` (slice) | No |
| `.identifier` | No |
| `| expr` (pipe) | Yes |

```java
// {"items": [[1, 2], [3, 4], [5, 6]]}

// [0] stops the projection
JmesPath.search("items[*][0]", data);
// [1, 3, 5]

// Pipe stops and restarts
JmesPath.search("items[*] | [0]", data);
// [1, 2]
```

## Null Handling

Projections skip null results:

```java
// {"people": [
//   {"name": "Alice", "nickname": "Ali"},
//   {"name": "Bob"},
//   {"name": "Charlie", "nickname": "Chuck"}
// ]}

JmesPath.search("people[*].nickname", data);
// ["Ali", "Chuck"]  -- Bob's null is skipped
```

## Multi-Select in Projections

Reshape data during projection:

```java
// {"users": [
//   {"firstName": "Alice", "lastName": "Smith", "age": 30},
//   {"firstName": "Bob", "lastName": "Jones", "age": 25}
// ]}

JmesPath.search("users[*].{name: firstName, years: age}", data);
// [{"name": "Alice", "years": 30}, {"name": "Bob", "years": 25}]

JmesPath.search("users[*].[firstName, lastName]", data);
// [["Alice", "Smith"], ["Bob", "Jones"]]
```

## Common Patterns

### Get All IDs

```java
JmesPath.search("items[*].id", data);
```

### Flatten Nested Lists

```java
JmesPath.search("orders[].lineItems[].productId", data);
```

### Filter Then Project

```java
JmesPath.search("users[?active].email", data);
```

### Project Then Sort

```java
JmesPath.search("items[*].name | sort(@)", data);
```
