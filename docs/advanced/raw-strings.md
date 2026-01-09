# Raw Strings (JEP-12)

Raw string literals provide a simpler way to write string values without JSON escaping.

## The Problem

In standard JMESPath, string literals in filters require JSON syntax:

```java
// JSON literal - needs escaping
JmesPath.search("items[?path == `\"C:\\\\Users\\\\file.txt\"`]", data);
```

That's hard to read and easy to get wrong.

## The Solution

Raw strings use single quotes and don't interpret escape sequences:

```java
// Raw string - much cleaner
JmesPath.search("items[?path == 'C:\\Users\\file.txt']", data);
```

## Syntax

| Syntax | Result |
|--------|--------|
| `'hello'` | `hello` |
| `'foo\nbar'` | `foo\nbar` (literal backslash-n) |
| `'it\'s ok'` | `it's ok` (escaped single quote) |
| `'C:\path\to\file'` | `C:\path\to\file` |

The only escape sequence is `\'` for a literal single quote.

## Comparison with JSON Literals

| Raw String | JSON Literal | Value |
|------------|--------------|-------|
| `'hello'` | `` `"hello"` `` | `hello` |
| `'foo\nbar'` | `` `"foo\\nbar"` `` | `foo\nbar` |
| `'tab\there'` | `` `"tab\\there"` `` | `tab\there` |

JSON literals interpret escape sequences:

```java
// Raw string: literal \n characters
JmesPath.search("'hello\\nworld'", data);  // "hello\nworld"

// JSON literal: actual newline
JmesPath.search("`\"hello\\nworld\"`", data);  // "hello
                                                //  world"
```

## Use Cases

### File Paths

```java
// Windows paths
JmesPath.search("files[?path == 'C:\\Users\\docs\\file.txt']", data);

// Unix paths (no escaping needed either way)
JmesPath.search("files[?path == '/usr/local/bin']", data);
```

### Regular Expression Patterns

```java
// Regex with backslashes
JmesPath.search("items[?matches(name, '\\d{3}-\\d{4}')]", data);
```

### URLs and URIs

```java
JmesPath.search("links[?url == 'https://example.com/path?foo=bar&baz=qux']", data);
```

### Simple String Matching

```java
// Cleaner than JSON literals for basic strings
JmesPath.search("users[?name == 'Alice']", data);
JmesPath.search("items[?status == 'pending']", data);
```

## Escaping Single Quotes

To include a literal single quote, use `\'`:

```java
JmesPath.search("messages[?text == 'it\\'s working']", data);
// Matches: {"text": "it's working"}
```

## When to Use What

| Use Case | Recommended |
|----------|-------------|
| Simple strings | Raw string: `'hello'` |
| Strings with backslashes | Raw string: `'C:\path'` |
| Strings with single quotes | JSON literal: `` `"it's"` `` |
| Numbers, booleans, null | JSON literal: `` `42` ``, `` `true` `` |
| Complex JSON values | JSON literal: `` `{"key": "value"}` `` |

## Examples

```java
// Filter by status
JmesPath.search("orders[?status == 'shipped']", data);

// Filter by path pattern
JmesPath.search("files[?starts_with(path, '/var/log/')]", data);

// Combine with other conditions
JmesPath.search("items[?type == 'book' && price < `20`]", data);

// In projections
JmesPath.search("users[*].{name: name, role: 'user'}", data);
```
