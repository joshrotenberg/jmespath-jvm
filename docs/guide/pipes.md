# Pipes

The pipe operator `|` chains expressions together, passing the output of one as input to the next.

## Basic Syntax

```
expression | expression | expression
```

Each expression receives the result of the previous one:

```java
// {"items": [3, 1, 4, 1, 5, 9, 2, 6]}

JmesPath.search("items | sort(@) | reverse(@)", data);
// [9, 6, 5, 4, 3, 2, 1, 1]
```

## Stopping Projections

Pipes stop active projections. This is their most important use:

```java
// {"items": [{"a": 1}, {"a": 2}, {"a": 3}]}

// Without pipe - still in projection context
JmesPath.search("items[*].a", data);
// [1, 2, 3]

// With pipe - operate on the projected array
JmesPath.search("items[*].a | [0]", data);
// 1 (first element of the array)
```

Compare:

```java
// items[*].a[0] - tries to get [0] from each 'a' value (numbers have no index)
JmesPath.search("items[*].a[0]", data);  // [null, null, null]

// items[*].a | [0] - get [0] from the array of 'a' values
JmesPath.search("items[*].a | [0]", data);  // 1
```

## Common Patterns

### Sort and Slice

```java
// Get top 3 highest scores
JmesPath.search("scores | sort(@) | reverse(@) | [:3]", data);

// Get 3 oldest people's names
JmesPath.search("sort_by(people, &age) | reverse(@) | [:3] | [*].name", data);
```

### Filter Then Aggregate

```java
// Count active users
JmesPath.search("users[?active] | length(@)", data);

// Average score of passing grades
JmesPath.search("grades[?score >= `60`].score | avg(@)", data);
```

### Transform Then Filter

```java
// Get names, then filter by length
JmesPath.search("people[*].name | [?length(@) > `5`]", data);
```

### Multiple Transformations

```java
// Complex data pipeline
JmesPath.search(
    "orders[?status == 'completed'].items[] | " +
    "sort_by(@, &price) | " +
    "reverse(@) | " +
    "[:10] | " +
    "[*].{name: productName, cost: price}",
    data
);
```

## Current Node in Pipes

The `@` symbol represents the current value at each stage:

```java
// {"numbers": [5, 2, 8, 1, 9]}

JmesPath.search("numbers | sort(@)", data);    // [1, 2, 5, 8, 9]
JmesPath.search("numbers | reverse(@)", data); // [9, 1, 8, 2, 5]
JmesPath.search("numbers | max(@)", data);     // 9
JmesPath.search("numbers | length(@)", data);  // 5
```

## Right-to-Left Evaluation

Pipes evaluate left-to-right:

```java
// {"data": {"items": [1, 2, 3]}}

JmesPath.search("data.items | sort(@) | [0]", data);
// Evaluates as: data.items -> [1,2,3] -> sort -> [1,2,3] -> [0] -> 1
```

## Combining with Other Operators

### Pipes in Multi-Select

```java
JmesPath.search(
    "{sorted: items | sort(@), count: items | length(@)}",
    data
);
```

### Pipes After Filters

```java
JmesPath.search(
    "items[?price > `100`] | sort_by(@, &price) | [0].name",
    data
);
```

## When to Use Pipes

| Use Case | Example |
|----------|---------|
| Stop a projection | `items[*].x | [0]` |
| Chain functions | `data | sort(@) | reverse(@)` |
| Apply functions to filtered results | `items[?active] | length(@)` |
| Complex transformations | Multiple stages of processing |

## Pipes vs Dot

- **Dot (`.`)** continues into substructure, maintains projection
- **Pipe (`|`)** passes entire result to next expression, stops projection

```java
// These are different:
JmesPath.search("items[*].name[0]", data);   // first char of each name
JmesPath.search("items[*].name | [0]", data); // first name in array
```
