package io.jmespath.internal.node;

import io.jmespath.Runtime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents array slice access.
 *
 * <p>Extracts a portion of an array using start:stop:step syntax.
 * All parameters are optional:
 * <ul>
 *   <li>start - beginning index (default: 0 or end if step is negative)</li>
 *   <li>stop - ending index, exclusive (default: length or -length-1 if step is negative)</li>
 *   <li>step - increment (default: 1, must not be 0)</li>
 * </ul>
 *
 * <p>Negative indices count from the end.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code [0:5]} - first 5 elements</li>
 *   <li>{@code [::2]} - every other element</li>
 *   <li>{@code [::-1]} - reversed array</li>
 * </ul>
 */
public final class SliceNode implements Node {

    private final Integer start;
    private final Integer stop;
    private final Integer step;

    /**
     * Creates a slice node.
     *
     * @param start start index (null for default)
     * @param stop stop index (null for default)
     * @param step step value (null for default of 1)
     */
    public SliceNode(Integer start, Integer stop, Integer step) {
        this.start = start;
        this.stop = stop;
        this.step = step;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        if (!runtime.isArray(current)) {
            return runtime.createNull();
        }

        int length = runtime.getArrayLength(current);
        int actualStep = (step != null) ? step : 1;

        if (actualStep == 0) {
            // Step of 0 is invalid - throw error
            throw new io.jmespath.JmesPathException(
                "slice step cannot be 0",
                io.jmespath.JmesPathException.ErrorType.VALUE
            );
        }

        int actualStart;
        int actualStop;

        if (actualStep > 0) {
            actualStart = (start != null)
                ? adjustIndex(start, length, true)
                : 0;
            actualStop = (stop != null)
                ? adjustIndex(stop, length, true)
                : length;
        } else {
            actualStart = (start != null)
                ? adjustIndex(start, length, true)
                : length - 1;
            // For negative step, stop can be -1 to include index 0
            actualStop = (stop != null) ? adjustIndex(stop, length, false) : -1;
        }

        List<T> result = new ArrayList<T>();

        if (actualStep > 0) {
            for (
                int i = actualStart;
                i < actualStop && i < length;
                i += actualStep
            ) {
                if (i >= 0) {
                    result.add(runtime.getIndex(current, i));
                }
            }
        } else {
            for (
                int i = actualStart;
                i > actualStop && i >= 0;
                i += actualStep
            ) {
                if (i < length) {
                    result.add(runtime.getIndex(current, i));
                }
            }
        }

        return runtime.createArray(result);
    }

    /**
     * Adjusts an index for array bounds.
     *
     * @param index the index to adjust
     * @param length the array length
     * @param clampToZero if true, clamp negative results to 0; if false, allow -1 for stop in reverse slices
     * @return the adjusted index
     */
    private int adjustIndex(int index, int length, boolean clampToZero) {
        if (index < 0) {
            int adjusted = length + index;
            if (clampToZero) {
                return Math.max(0, adjusted);
            }
            // For stop in reverse slices, allow -1 to include index 0
            return Math.max(-1, adjusted);
        }
        return index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (start != null) {
            sb.append(start);
        }
        sb.append(":");
        if (stop != null) {
            sb.append(stop);
        }
        if (step != null) {
            sb.append(":").append(step);
        }
        sb.append("]");
        return sb.toString();
    }
}
