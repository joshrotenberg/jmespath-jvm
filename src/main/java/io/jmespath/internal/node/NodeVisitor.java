package io.jmespath.internal.node;

/**
 * Visitor interface for traversing AST nodes.
 *
 * <p>Used for debugging, optimization, or other tree traversal needs.
 */
public interface NodeVisitor {

    /**
     * Visits a node.
     *
     * @param node the node to visit
     */
    void visit(Node node);
}
