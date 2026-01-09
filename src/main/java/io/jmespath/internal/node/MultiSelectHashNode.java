package io.jmespath.internal.node;

import io.jmespath.Runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a multi-select hash expression.
 *
 * <p>Evaluates multiple key-value pairs and collects the results into an object.
 *
 * <p>Example: {@code {name: foo, age: bar}} produces an object with two properties.
 */
public final class MultiSelectHashNode implements Node {

    /**
     * A key-value pair in a multi-select hash.
     */
    public static final class Entry {
        private final String key;
        private final Node value;

        /**
         * Creates an entry.
         *
         * @param key the property name
         * @param value the expression for the value
         */
        public Entry(String key, Node value) {
            if (key == null || value == null) {
                throw new IllegalArgumentException("key and value cannot be null");
            }
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the key.
         *
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns the value expression.
         *
         * @return the value node
         */
        public Node getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + ": " + value;
        }
    }

    private final List<Entry> entries;

    /**
     * Creates a multi-select hash node.
     *
     * @param entries the key-value pairs
     */
    public MultiSelectHashNode(List<Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("entries cannot be null or empty");
        }
        this.entries = new ArrayList<Entry>(entries);
    }

    /**
     * Returns the entries.
     *
     * @return the key-value pairs
     */
    public List<Entry> getEntries() {
        return entries;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        Map<String, T> result = new LinkedHashMap<String, T>();
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            T value = entry.getValue().evaluate(runtime, current);
            result.put(entry.getKey(), value);
        }
        return runtime.createObject(result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(entries.get(i));
        }
        sb.append("}");
        return sb.toString();
    }
}
