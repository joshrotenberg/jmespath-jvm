package io.jmespath.internal.node;

import io.jmespath.JmesPathException;
import io.jmespath.Runtime;
import io.jmespath.internal.Scope;
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

    // Use primitives with boolean flags instead of boxed Integer
    private final int start;
    private final int stop;
    private final int step;
    private final boolean hasStart;
    private final boolean hasStop;
    private final boolean positiveStep;

    /**
     * Creates a slice node.
     *
     * @param start start index (null for default)
     * @param stop stop index (null for default)
     * @param step step value (null for default of 1)
     */
    public SliceNode(Integer start, Integer stop, Integer step) {
        this.hasStart = (start != null);
        this.hasStop = (stop != null);
        this.start = hasStart ? start : 0;
        this.stop = hasStop ? stop : 0;

        int stepVal = (step != null) ? step : 1;
        if (stepVal == 0) {
            throw new JmesPathException(
                "slice step cannot be 0",
                JmesPathException.ErrorType.VALUE
            );
        }
        this.step = stepVal;
        this.positiveStep = stepVal > 0;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current, Scope<T> scope) {
        if (!runtime.isArray(current)) {
            return runtime.createNull();
        }

        int length = runtime.getArrayLength(current);
        if (length == 0) {
            return runtime.createArray(new ArrayList<T>(0));
        }

        int actualStart;
        int actualStop;

        if (positiveStep) {
            actualStart = hasStart ? adjustIndex(start, length) : 0;
            actualStop = hasStop ? adjustIndex(stop, length) : length;

            // Clamp bounds
            if (actualStart < 0) actualStart = 0;
            if (actualStop > length) actualStop = length;
            if (actualStart >= actualStop) {
                return runtime.createArray(new ArrayList<T>(0));
            }

            // Pre-size result list
            int capacity = (actualStop - actualStart + step - 1) / step;
            List<T> result = new ArrayList<T>(capacity);

            for (int i = actualStart; i < actualStop; i += step) {
                result.add(runtime.getIndex(current, i));
            }

            return runtime.createArray(result);
        } else {
            // Negative step
            actualStart = hasStart ? adjustIndex(start, length) : length - 1;
            actualStop = hasStop
                ? adjustIndexForNegativeStep(stop, length)
                : -1;

            // Clamp bounds
            if (actualStart >= length) actualStart = length - 1;
            if (actualStart <= actualStop) {
                return runtime.createArray(new ArrayList<T>(0));
            }

            // Pre-size result list (step is negative, so negate for division)
            int capacity = (actualStart - actualStop - step - 1) / (-step);
            List<T> result = new ArrayList<T>(capacity);

            for (int i = actualStart; i > actualStop && i >= 0; i += step) {
                result.add(runtime.getIndex(current, i));
            }

            return runtime.createArray(result);
        }
    }

    /**
     * Adjusts a negative index to absolute position.
     */
    private static int adjustIndex(int index, int length) {
        return index < 0 ? length + index : index;
    }

    /**
     * Adjusts index for stop position in negative step slices.
     * Allows -1 result to include index 0.
     */
    private static int adjustIndexForNegativeStep(int index, int length) {
        if (index < 0) {
            int adjusted = length + index;
            return adjusted < -1 ? -1 : adjusted;
        }
        return index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (hasStart) {
            sb.append(start);
        }
        sb.append(":");
        if (hasStop) {
            sb.append(stop);
        }
        if (step != 1) {
            sb.append(":").append(step);
        }
        sb.append("]");
        return sb.toString();
    }
}
