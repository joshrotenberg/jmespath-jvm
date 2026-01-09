package io.jmespath.internal.node;

import io.jmespath.Runtime;
import io.jmespath.function.FunctionRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a function call expression.
 *
 * <p>Evaluates the arguments and invokes the named function.
 *
 * <p>Example: {@code length(foo)}, {@code sort_by(people, &age)}
 */
public final class FunctionCallNode implements Node {

    private final String name;
    private final List<Node> arguments;

    /**
     * Creates a function call node.
     *
     * @param name the function name
     * @param arguments the argument expressions
     */
    public FunctionCallNode(String name, List<Node> arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;
        this.arguments = arguments != null ? new ArrayList<Node>(arguments) : new ArrayList<Node>();
    }

    /**
     * Returns the function name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the argument expressions.
     *
     * @return the arguments
     */
    public List<Node> getArguments() {
        return arguments;
    }

    @Override
    public <T> T evaluate(Runtime<T> runtime, T current) {
        // Get the function registry from the runtime
        FunctionRegistry registry = runtime.getFunctionRegistry();
        if (registry == null) {
            throw new IllegalStateException("No function registry available");
        }

        // Evaluate arguments (but expression refs are passed as-is)
        List<Object> evaluatedArgs = new ArrayList<Object>();
        for (int i = 0; i < arguments.size(); i++) {
            Node arg = arguments.get(i);
            if (arg instanceof ExpressionRefNode) {
                // Pass expression reference as-is for lazy evaluation
                evaluatedArgs.add(arg);
            } else {
                evaluatedArgs.add(arg.evaluate(runtime, current));
            }
        }

        return registry.call(name, runtime, current, evaluatedArgs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(arguments.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}
