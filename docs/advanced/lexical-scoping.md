# Lexical Scoping (JEP-18)

jmespath-jvm implements [JEP-18](https://github.com/jmespath/jmespath.jep/blob/main/proposals/jep-018-lexical-scoping.md), adding `let` expressions and variable references to JMESPath.

## The Problem

Standard JMESPath has no way to:

- Reference the root document from within nested expressions
- Store intermediate results for reuse
- Reference parent data from within filter expressions

Consider finding people whose age is above the group's average:

```json
{
  "people": [
    {"name": "Alice", "age": 30},
    {"name": "Bob", "age": 25},
    {"name": "Carol", "age": 35}
  ]
}
```

In standard JMESPath, you can't compare each person's age to `avg(people[*].age)` because inside the filter, you've lost access to the root.

## Let Expressions

The `let` keyword binds values to variables:

```
let $avg = avg(people[*].age)
in people[?age > $avg].name
```

Result: `["Alice", "Carol"]`

### Syntax

```
let $var1 = expr1, $var2 = expr2, ... in body
```

- Variable names start with `$`
- Multiple bindings separated by commas
- Bindings evaluated left-to-right (later can reference earlier)
- Body expression can use all bound variables

### Examples

**Multiple bindings:**

```
let $total = sum(items[*].price),
    $count = length(items)
in { total: $total, average: `$total / $count` }
```

**Chained bindings:**

```
let $prices = items[*].price,
    $avg = avg($prices),
    $max = max($prices)
in { average: $avg, max: $max, range: `$max - $avg` }
```

## Root Reference

The special variable `$` always refers to the root document:

```java
// Given: {"users": [{"name": "Alice", "role": "admin"}], "adminCount": 1}
String expr = "users[?role == 'admin'] | length(@) == $.adminCount";
```

This compares the filtered array length against the root's `adminCount` field.

### Root Reference in Filters

```json
{
  "threshold": 100,
  "items": [
    {"name": "A", "value": 150},
    {"name": "B", "value": 50},
    {"name": "C", "value": 200}
  ]
}
```

```
items[?value > $.threshold].name
```

Result: `["A", "C"]`

## Combining Let and Root

```json
{
  "config": {"minAge": 21},
  "people": [
    {"name": "Alice", "age": 30},
    {"name": "Bob", "age": 18},
    {"name": "Carol", "age": 25}
  ]
}
```

```
let $min = $.config.minAge
in $.people[?age >= $min].name
```

Result: `["Alice", "Carol"]`

## Scope Rules

Variables follow lexical scoping:

1. Inner `let` expressions can shadow outer variables
2. Variables are only visible in their `in` body
3. Function bodies have their own scope

**Shadowing example:**

```
let $x = `1`
in let $x = `2`
   in $x
```

Result: `2`

**Scope isolation:**

```
let $x = `1`
in [$x, (let $x = `2` in $x), $x]
```

Result: `[1, 2, 1]`

## Practical Examples

### Compare to Group Statistics

```json
{
  "scores": [85, 90, 78, 92, 88]
}
```

Find scores above average:

```
let $avg = avg(scores)
in scores[? @ > $avg]
```

Result: `[90, 92, 88]`

### Cross-Reference Data

```json
{
  "users": [
    {"id": 1, "name": "Alice"},
    {"id": 2, "name": "Bob"}
  ],
  "posts": [
    {"authorId": 1, "title": "Hello"},
    {"authorId": 1, "title": "World"},
    {"authorId": 2, "title": "Test"}
  ]
}
```

Count posts per user:

```
let $posts = $.posts
in users[*].{
  name: name,
  postCount: length($posts[?authorId == $.id])
}
```

### Threshold Filtering

```json
{
  "settings": {"minPrice": 10, "maxPrice": 100},
  "products": [
    {"name": "A", "price": 5},
    {"name": "B", "price": 50},
    {"name": "C", "price": 150}
  ]
}
```

```
let $min = $.settings.minPrice,
    $max = $.settings.maxPrice
in $.products[?price >= $min && price <= $max].name
```

Result: `["B"]`

## Java Usage

```java
JmesPath jmespath = JmesPath.create();

// Let expression
Expression<Object> expr = jmespath.compile(
    "let $avg = avg(scores) in scores[? @ > $avg]"
);

Map<String, Object> data = Map.of(
    "scores", List.of(85, 90, 78, 92, 88)
);

Object result = expr.evaluate(data);
// [90, 92, 88]
```

```java
// Root reference
Expression<Object> expr = jmespath.compile(
    "items[?value > $.threshold].name"
);

Map<String, Object> data = Map.of(
    "threshold", 100,
    "items", List.of(
        Map.of("name", "A", "value", 150),
        Map.of("name", "B", "value", 50)
    )
);

Object result = expr.evaluate(data);
// ["A"]
```

## Performance Notes

- Variable bindings are evaluated once when entering the `let` scope
- References to variables are simple lookups (no re-evaluation)
- The root reference `$` is always available at zero cost
