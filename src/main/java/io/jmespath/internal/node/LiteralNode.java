package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.internal.Scope;

/**
 * Represents a JSON literal value.
 *
 * <p>Literals are backtick-delimited JSON values that are parsed
 * at evaluation time using the runtime's JSON parser.
 *
 * <p>Example: {@code `true`}, {@code `{"a": 1}`}, {@code `[1, 2, 3]`}
 */
public final class LiteralNode implements Node {

    private final String json;

    /**
     * Creates a literal node.
     *
     * @param json the JSON string to parse
     */
    public LiteralNode(String json) {
        if (json == null) {
            throw new IllegalArgumentException("json cannot be null");
        }
        this.json = json;
    }

    /**
     * Returns the JSON string.
     *
     * @return the JSON
     */
    public String getJson() {
        return json;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        return runtime.parseJson(json);
    }

    @Override
    public String toString() {
        return "`" + json + "`";
    }
}
