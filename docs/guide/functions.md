# Functions

jmespath-jvm includes all 26 standard JMESPath functions.

## Numeric Functions

### abs

Absolute value.

```java
JmesPath.search("abs(`-5`)", data);  // 5
```

### avg

Average of an array of numbers.

```java
JmesPath.search("avg(scores)", data);  // e.g., 85.5
```

### ceil

Round up to nearest integer.

```java
JmesPath.search("ceil(`4.2`)", data);  // 5
```

### floor

Round down to nearest integer.

```java
JmesPath.search("floor(`4.8`)", data);  // 4
```

### sum

Sum of an array of numbers.

```java
JmesPath.search("sum(prices)", data);  // e.g., 150
```

## String Functions

### contains

Check if a string contains a substring (or array contains an element).

```java
JmesPath.search("contains('hello world', 'world')", data);  // true
JmesPath.search("contains(tags, 'urgent')", data);  // true/false
```

### ends_with

Check if a string ends with a suffix.

```java
JmesPath.search("ends_with(filename, '.pdf')", data);  // true/false
```

### starts_with

Check if a string starts with a prefix.

```java
JmesPath.search("starts_with(name, 'Dr.')", data);  // true/false
```

### join

Join an array of strings with a separator.

```java
JmesPath.search("join(', ', names)", data);  // "Alice, Bob, Charlie"
```

### length

Length of a string, array, or object.

```java
JmesPath.search("length(name)", data);    // string length
JmesPath.search("length(items)", data);   // array length
JmesPath.search("length(@)", data);       // object key count
```

## Array Functions

### reverse

Reverse an array or string.

```java
JmesPath.search("reverse(items)", data);   // [c, b, a]
JmesPath.search("reverse('hello')", data); // "olleh"
```

### sort

Sort an array of strings or numbers.

```java
JmesPath.search("sort(names)", data);    // alphabetical
JmesPath.search("sort(scores)", data);   // numerical
```

### sort_by

Sort an array of objects by a key expression.

```java
JmesPath.search("sort_by(people, &age)", data);
JmesPath.search("sort_by(people, &name)", data);
```

!!! note "Expression References"
    The `&` prefix creates an expression reference, evaluated for each element.

### max / min

Get maximum or minimum value.

```java
JmesPath.search("max(scores)", data);  // highest score
JmesPath.search("min(ages)", data);    // lowest age
```

### max_by / min_by

Get element with maximum or minimum value by expression.

```java
JmesPath.search("max_by(people, &age)", data);        // oldest person object
JmesPath.search("max_by(people, &age).name", data);   // oldest person's name
JmesPath.search("min_by(products, &price)", data);    // cheapest product
```

### map

Apply an expression to each element.

```java
JmesPath.search("map(&age, people)", data);  // [25, 30, 35]
```

## Object Functions

### keys

Get object keys as an array.

```java
JmesPath.search("keys(@)", data);  // ["name", "age", "city"]
```

### values

Get object values as an array.

```java
JmesPath.search("values(@)", data);  // ["Alice", 30, "Seattle"]
```

### merge

Merge multiple objects.

```java
JmesPath.search("merge(defaults, overrides)", data);
JmesPath.search("merge(`{\"a\": 1}`, `{\"b\": 2}`)", data);  // {"a": 1, "b": 2}
```

## Conversion Functions

### to_array

Convert a value to an array.

```java
JmesPath.search("to_array('hello')", data);  // ["hello"]
JmesPath.search("to_array(items)", data);    // items (if already array)
```

### to_number

Convert to a number.

```java
JmesPath.search("to_number('42')", data);     // 42
JmesPath.search("to_number('invalid')", data); // null
```

### to_string

Convert to a string.

```java
JmesPath.search("to_string(`42`)", data);     // "42"
JmesPath.search("to_string(`true`)", data);   // "true"
```

### type

Get the type name.

```java
JmesPath.search("type(name)", data);    // "string"
JmesPath.search("type(age)", data);     // "number"
JmesPath.search("type(items)", data);   // "array"
JmesPath.search("type(@)", data);       // "object"
JmesPath.search("type(`null`)", data);  // "null"
JmesPath.search("type(`true`)", data);  // "boolean"
```

## Utility Functions

### not_null

Return the first non-null argument.

```java
JmesPath.search("not_null(preferred_name, nickname, name)", data);
```

## Common Patterns

### Get Top N

```java
JmesPath.search("sort_by(items, &score) | reverse(@) | [:5]", data);
```

### Count Matches

```java
JmesPath.search("length(items[?active])", data);
```

### Check If Any Match

```java
JmesPath.search("length(items[?status == 'error']) > `0`", data);
```

### Get Unique Property Values

```java
// Using sort to deduplicate (works for sorted data)
JmesPath.search("sort(items[*].category)", data);
```

### Aggregate Calculations

```java
// Total value
JmesPath.search("sum(items[*].price)", data);

// Average rating for active items
JmesPath.search("avg(items[?active].rating)", data);
```
