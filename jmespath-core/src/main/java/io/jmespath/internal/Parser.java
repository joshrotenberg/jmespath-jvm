package io.jmespath.internal;

import io.jmespath.JmesPathException;
import io.jmespath.internal.node.*;
import io.jmespath.internal.node.LetNode.Binding;
import java.util.ArrayList;
import java.util.List;

/**
 * Pratt parser for JMESPath expressions.
 *
 * <p>This parser uses the Pratt parsing technique (top-down operator precedence)
 * to handle operator precedence correctly without explicit grammar rules.
 *
 * <p>Key concepts:
 * <ul>
 *   <li><b>nud</b> (null denotation) - how a token behaves at the start of an expression</li>
 *   <li><b>led</b> (left denotation) - how a token behaves after a left operand</li>
 *   <li><b>lbp</b> (left binding power) - the precedence of an infix operator</li>
 * </ul>
 *
 * <p>Operator precedence (lowest to highest):
 * <ol>
 *   <li>| (pipe)</li>
 *   <li>|| (or)</li>
 *   <li>&amp;&amp; (and)</li>
 *   <li>! (not)</li>
 *   <li>== != &lt; &lt;= &gt; &gt;= (comparisons)</li>
 *   <li>[] (flatten)</li>
 *   <li>[n] [*] [?] [:] (bracket expressions)</li>
 *   <li>. (dot)</li>
 * </ol>
 */
public final class Parser {

    // Binding powers (precedence levels)
    private static final int BP_PIPE = 1;
    private static final int BP_OR = 2;
    private static final int BP_AND = 3;
    private static final int BP_NOT = 4;
    private static final int BP_COMPARISON = 5;
    private static final int BP_FLATTEN = 6;
    private static final int BP_BRACKET = 7;
    private static final int BP_DOT = 8;

    private final Lexer lexer;
    private Token current;

    /**
     * Creates a parser for the given expression.
     *
     * @param expression the JMESPath expression to parse
     */
    public Parser(String expression) {
        this.lexer = new Lexer(expression);
        this.current = lexer.next();
    }

    /**
     * Parses the expression and returns the AST root node.
     *
     * @return the root node
     * @throws JmesPathException if the expression is invalid
     */
    public Node parse() {
        Node result = expression(0);
        if (!current.is(TokenType.EOF)) {
            throw error("Unexpected token: " + current.type());
        }
        return result;
    }

    /**
     * Parses an expression with the given minimum binding power.
     */
    private Node expression(int minBp) {
        Node left = nud();

        while (lbp(current) > minBp) {
            left = led(left);
        }

        return left;
    }

    /**
     * Null denotation - handles tokens at the start of an expression.
     */
    private Node nud() {
        Token token = current;

        switch (token.type()) {
            case IDENTIFIER:
                advance();
                return new IdentifierNode(token.stringValue(), false);
            case QUOTED_IDENTIFIER:
                advance();
                return new IdentifierNode(token.stringValue(), true);
            case NUMBER:
                // Numbers can only appear in bracket expressions
                throw error("Unexpected number outside of bracket");
            case AT:
                advance();
                return CurrentNode.INSTANCE;
            case STAR:
                advance();
                return parseObjectProjection(null);
            case LBRACKET:
                advance();
                return parseBracket(null);
            case LBRACE:
                advance();
                return parseMultiSelectHash();
            case LPAREN:
                advance();
                Node inner = expression(0);
                expect(TokenType.RPAREN);
                return inner;
            case NOT:
                advance();
                // NOT binds tightly - only consumes the next atom/sub-expression
                // Use BP_BRACKET so it doesn't consume comparisons
                Node negated = expression(BP_BRACKET);
                return new NotNode(negated);
            case AMPERSAND:
                advance();
                Node expr = expression(BP_NOT);
                return new ExpressionRefNode(expr);
            case LITERAL:
                advance();
                return new LiteralNode(token.stringValue());
            case RAW_STRING:
                advance();
                return new RawStringNode(token.stringValue());
            case VARIABLE:
                // Variable reference $name (JEP-18)
                advance();
                return new VariableRefNode(token.stringValue());
            case LET:
                // Let expression (JEP-18)
                advance();
                return parseLetExpression();
            case FLATTEN:
                advance();
                return parseFlatten(null);
            case FILTER:
                // [? at start of expression - parse the filter condition
                advance();
                Node filterCond = expression(0);
                expect(TokenType.RBRACKET);
                return parseFilterRhs(null, filterCond);
            case PIPE:
            case OR:
            case AND:
            case EQ:
            case NE:
            case LT:
            case LE:
            case GT:
            case GE:
            case RBRACKET:
            case RPAREN:
            case RBRACE:
            case COMMA:
            case COLON:
            case EOF:
                throw error("Unexpected token: " + token.type());
            default:
                throw error("Unexpected token: " + token.type());
        }
    }

    /**
     * Left denotation - handles tokens after a left operand.
     */
    private Node led(Node left) {
        Token token = current;

        switch (token.type()) {
            case DOT:
                advance();
                return parseSubExpression(left);
            case PIPE:
                advance();
                Node pipeRight = expression(BP_PIPE);
                return new PipeNode(left, pipeRight);
            case OR:
                advance();
                Node orRight = expression(BP_OR);
                return new OrNode(left, orRight);
            case AND:
                advance();
                Node andRight = expression(BP_AND);
                return new AndNode(left, andRight);
            case EQ:
                advance();
                return new ComparisonNode(
                    ComparisonNode.Operator.EQ,
                    left,
                    expression(BP_COMPARISON)
                );
            case NE:
                advance();
                return new ComparisonNode(
                    ComparisonNode.Operator.NE,
                    left,
                    expression(BP_COMPARISON)
                );
            case LT:
                advance();
                return new ComparisonNode(
                    ComparisonNode.Operator.LT,
                    left,
                    expression(BP_COMPARISON)
                );
            case LE:
                advance();
                return new ComparisonNode(
                    ComparisonNode.Operator.LE,
                    left,
                    expression(BP_COMPARISON)
                );
            case GT:
                advance();
                return new ComparisonNode(
                    ComparisonNode.Operator.GT,
                    left,
                    expression(BP_COMPARISON)
                );
            case GE:
                advance();
                return new ComparisonNode(
                    ComparisonNode.Operator.GE,
                    left,
                    expression(BP_COMPARISON)
                );
            case LBRACKET:
                advance();
                return parseBracketAfterProjection(left);
            case FLATTEN:
                advance();
                return parseFlattenAfterProjection(left);
            case LPAREN:
                // Function call - left must be an unquoted identifier
                if (!(left instanceof IdentifierNode)) {
                    throw error("Expected function name before '('");
                }
                IdentifierNode funcIdent = (IdentifierNode) left;
                if (funcIdent.isQuoted()) {
                    throw error("Function name cannot be a quoted identifier");
                }
                advance();
                String funcName = funcIdent.getName();
                return parseFunctionCall(funcName);
            case FILTER:
                // [? after an expression - parse filter
                advance();
                return parseFilterAfterProjection(left);
            default:
                throw error(
                    "Unexpected token in infix position: " + token.type()
                );
        }
    }

    /**
     * Returns the left binding power of a token.
     */
    private int lbp(Token token) {
        switch (token.type()) {
            case PIPE:
                return BP_PIPE;
            case OR:
                return BP_OR;
            case AND:
                return BP_AND;
            case EQ:
            case NE:
            case LT:
            case LE:
            case GT:
            case GE:
                return BP_COMPARISON;
            case FLATTEN:
                return BP_FLATTEN;
            case FILTER:
            case LBRACKET:
            case LPAREN:
                return BP_BRACKET;
            case DOT:
                return BP_DOT;
            default:
                return 0;
        }
    }

    /**
     * Parses a sub-expression (after dot).
     * After a dot, we can have: identifier, quoted identifier, *, {multi-select-hash},
     * or [multi-select-list] (but NOT index/slice - those must be without dot).
     */
    private Node parseSubExpression(Node left) {
        if (current.is(TokenType.STAR)) {
            advance();
            return parseObjectProjection(left);
        }

        if (current.is(TokenType.LBRACKET)) {
            advance();
            // After dot, brackets must contain a multi-select list (expressions), not index/slice
            Node multiSelect = parseMultiSelectListOnly(left);
            return multiSelect;
        }

        if (current.is(TokenType.LBRACE)) {
            advance();
            Node hash = parseMultiSelectHash();
            if (left.isProjection()) {
                return extendProjection(left, hash);
            }
            return new SubExpressionNode(left, hash);
        }

        // Must be an identifier
        if (
            !current.is(TokenType.IDENTIFIER) &&
            !current.is(TokenType.QUOTED_IDENTIFIER)
        ) {
            throw error("Expected identifier after '.'");
        }

        String name = current.stringValue();
        advance();
        Node right = new IdentifierNode(name);

        // If left is a projection, extend it instead of creating SubExpressionNode
        if (left.isProjection()) {
            return extendProjection(left, right);
        }

        return new SubExpressionNode(left, right);
    }

    /**
     * Handles bracket expression after a projection.
     * If left is a projection, we extend its RHS for most bracket expressions,
     * but flatten ([]) should wrap the entire projection.
     */
    private Node parseBracketAfterProjection(Node left) {
        // Check if this is an empty bracket (flatten)
        if (current.is(TokenType.RBRACKET)) {
            advance();
            // Flatten wraps the entire projection, doesn't extend RHS
            return parseFlatten(left);
        }

        if (left.isProjection()) {
            // Parse what's in the bracket as a projection target
            Node bracketExpr = parseBracket(null);
            // Extend the projection's right side
            return extendProjection(left, bracketExpr);
        }
        return parseBracket(left);
    }

    /**
     * Handles flatten after a projection.
     * Unlike other operations, flatten WRAPS the projection result rather than
     * extending the projection's RHS. This is because [] flattens the array
     * produced by the projection, not each element within it.
     */
    private Node parseFlattenAfterProjection(Node left) {
        // Flatten always wraps the left side - it flattens the result
        // of the entire preceding expression
        return parseFlatten(left);
    }

    /**
     * Handles filter [?...] after a projection.
     * If left is a projection, we extend its RHS with the filter.
     */
    private Node parseFilterAfterProjection(Node left) {
        Node condition = expression(0);
        expect(TokenType.RBRACKET);

        if (left.isProjection()) {
            // Create a filter node with null left (it will be applied to each element)
            Node filterNode = parseFilterRhs(null, condition);
            return extendProjection(left, filterNode);
        }
        return parseFilterRhs(left, condition);
    }

    /**
     * Extends a projection's right side with additional expression.
     */
    private Node extendProjection(Node projection, Node extension) {
        if (projection instanceof ProjectionNode) {
            ProjectionNode proj = (ProjectionNode) projection;
            Node newRight;
            if (proj.getRight() == null) {
                newRight = extension;
            } else {
                // Chain the extension after the existing right
                newRight = chainNodes(proj.getRight(), extension);
            }
            return new ProjectionNode(proj.getLeft(), newRight);
        } else if (projection instanceof FilterNode) {
            FilterNode filter = (FilterNode) projection;
            Node newRight;
            if (filter.getRight() == null) {
                newRight = extension;
            } else {
                newRight = chainNodes(filter.getRight(), extension);
            }
            return new FilterNode(
                filter.getLeft(),
                filter.getCondition(),
                newRight
            );
        } else if (projection instanceof FlattenNode) {
            // FlattenNode doesn't have a right side - wrap in ProjectionNode
            return new ProjectionNode(projection, extension);
        } else if (projection instanceof ObjectProjectionNode) {
            ObjectProjectionNode objProj = (ObjectProjectionNode) projection;
            Node newRight;
            if (objProj.getRight() == null) {
                newRight = extension;
            } else {
                newRight = chainNodes(objProj.getRight(), extension);
            }
            return new ObjectProjectionNode(objProj.getLeft(), newRight);
        }
        // Fallback - shouldn't happen
        return new SubExpressionNode(projection, extension);
    }

    /**
     * Chains two nodes together appropriately.
     */
    private Node chainNodes(Node left, Node right) {
        if (right instanceof IndexNode) {
            return new IndexExpressionNode(left, right);
        } else if (right instanceof SliceNode) {
            return new IndexExpressionNode(left, right);
        } else if (right instanceof FlattenNode) {
            // FlattenNode only has expression (left side) - chain left to it
            FlattenNode flatten = (FlattenNode) right;
            Node flattenLeft = flatten.getExpression();
            Node newFlattenLeft =
                flattenLeft == null
                    ? left
                    : new SubExpressionNode(left, flattenLeft);
            return new FlattenNode(newFlattenLeft);
        } else if (right instanceof ProjectionNode) {
            ProjectionNode proj = (ProjectionNode) right;
            // Wrap left with the projection
            Node newLeft =
                proj.getLeft() == null
                    ? left
                    : new SubExpressionNode(left, proj.getLeft());
            return new ProjectionNode(newLeft, proj.getRight());
        }
        return new SubExpressionNode(left, right);
    }

    /**
     * Parses a bracket expression.
     */
    private Node parseBracket(Node left) {
        // Empty bracket [] means flatten
        if (current.is(TokenType.RBRACKET)) {
            advance();
            return new FlattenNode(left);
        }

        // [* means wildcard projection ONLY if followed by ]
        // Otherwise it could be a multi-select list like [*.*]
        if (current.is(TokenType.STAR)) {
            Token next = lexer.peek();
            if (next.is(TokenType.RBRACKET)) {
                advance(); // consume *
                expect(TokenType.RBRACKET);
                return parseProjectionRhs(left);
            }
            // Not [*], so fall through to multi-select list parsing
        }

        // Note: [? is now handled as a single FILTER token in nud()/led()
        // If we see QUESTION here, it's invalid (space between [ and ?)
        if (current.is(TokenType.QUESTION)) {
            throw error(
                "Unexpected '?' - filter expressions must use '[?' without space"
            );
        }

        // Otherwise it's an index, slice, or multi-select list
        return parseBracketContent(left);
    }

    /**
     * Parses bracket content (index, slice, or multi-select).
     * Multi-select lists are allowed when left is null (start of expression)
     * but not when left is an expression (foo[abc] is invalid).
     */
    private Node parseBracketContent(Node left) {
        // Check if this could be a multi-select list (not a number or colon)
        if (!current.is(TokenType.NUMBER) && !current.is(TokenType.COLON)) {
            // Multi-select is only allowed at the start of an expression (left == null)
            // foo[abc] is invalid - multi-select lists require a dot: foo.[abc]
            if (left != null) {
                throw error("Expected number or slice in bracket expression");
            }
            return parseMultiSelectList(left);
        }

        // It's an index or slice
        Integer start = null;
        Integer stop = null;
        Integer step = null;
        boolean isSlice = false;

        // Parse start
        if (current.is(TokenType.NUMBER)) {
            start = current.numberValue().intValue();
            advance();
        }

        // Check for colon (slice)
        if (current.is(TokenType.COLON)) {
            isSlice = true;
            advance();

            // Parse stop
            if (current.is(TokenType.NUMBER)) {
                stop = current.numberValue().intValue();
                advance();
            }

            // Check for second colon (step)
            if (current.is(TokenType.COLON)) {
                advance();
                if (current.is(TokenType.NUMBER)) {
                    step = current.numberValue().intValue();
                    advance();
                }
            }
        }

        expect(TokenType.RBRACKET);

        if (isSlice) {
            Node slice = new SliceNode(start, stop, step);
            // Slice creates a projection - even when left is null (start of expression)
            return parseSliceRhs(left, slice);
        } else {
            // Simple index
            if (start == null) {
                throw error("Expected index value");
            }
            Node index = new IndexNode(start);
            if (left == null) {
                return index;
            }
            return new IndexExpressionNode(left, index);
        }
    }

    /**
     * Parses a multi-select list.
     */
    private Node parseMultiSelectList(Node left) {
        List<Node> elements = new ArrayList<Node>();
        elements.add(expression(0));

        while (current.is(TokenType.COMMA)) {
            advance();
            elements.add(expression(0));
        }

        expect(TokenType.RBRACKET);

        Node multiSelect = new MultiSelectListNode(elements);
        if (left == null) {
            return multiSelect;
        }
        return new SubExpressionNode(left, multiSelect);
    }

    /**
     * Parses a multi-select list that appears after a dot.
     * This validates that it's truly a multi-select (expressions), not an index/slice.
     * e.g., foo.[bar] is valid, foo.[0] is not.
     */
    private Node parseMultiSelectListOnly(Node left) {
        // After a dot, we only allow expressions in brackets, not numbers/colons (index/slice)
        if (current.is(TokenType.NUMBER) || current.is(TokenType.COLON)) {
            throw error(
                "Expected expression in multi-select list after '.', not index/slice"
            );
        }

        // Also check for empty brackets (flatten) - not allowed after dot
        if (current.is(TokenType.RBRACKET)) {
            throw error("Empty brackets not allowed after '.'");
        }

        return parseMultiSelectList(left);
    }

    /**
     * Parses a multi-select hash.
     */
    private Node parseMultiSelectHash() {
        List<MultiSelectHashNode.Entry> entries = new ArrayList<
            MultiSelectHashNode.Entry
        >();

        // Parse first entry
        entries.add(parseHashEntry());

        while (current.is(TokenType.COMMA)) {
            advance();
            entries.add(parseHashEntry());
        }

        expect(TokenType.RBRACE);
        return new MultiSelectHashNode(entries);
    }

    /**
     * Parses a single hash entry.
     */
    private MultiSelectHashNode.Entry parseHashEntry() {
        // Key must be identifier or quoted identifier
        if (
            !current.is(TokenType.IDENTIFIER) &&
            !current.is(TokenType.QUOTED_IDENTIFIER)
        ) {
            throw error("Expected identifier as hash key");
        }
        String key = current.stringValue();
        advance();

        expect(TokenType.COLON);

        Node value = expression(0);
        return new MultiSelectHashNode.Entry(key, value);
    }

    /**
     * Parses an object projection.
     * Note: FLATTEN ([]) is NOT parsed as part of object projection RHS - it wraps the projection result.
     */
    private Node parseObjectProjection(Node left) {
        Node right = null;

        if (current.is(TokenType.DOT)) {
            advance();
            // After a dot, we use parseProjectionTargetAfterDot which doesn't allow brackets
            right = parseProjectionTargetAfterDot();
        } else if (current.is(TokenType.LBRACKET)) {
            // LBRACKET continues the projection's RHS, but FLATTEN does not
            right = parseProjectionTarget();
        }
        // FLATTEN is handled by the main parser loop - it wraps the projection

        return new ObjectProjectionNode(left, right);
    }

    /**
     * Parses the right-hand side after a wildcard [*].
     * Note: FLATTEN ([]) is NOT parsed as part of projection RHS - it wraps the projection result.
     */
    private Node parseProjectionRhs(Node left) {
        Node right = null;

        if (current.is(TokenType.DOT)) {
            advance();
            right = parseProjectionTargetAfterDot();
        } else if (current.is(TokenType.LBRACKET)) {
            // LBRACKET continues the projection's RHS, but FLATTEN does not
            right = parseProjectionTarget();
        }
        // FLATTEN is handled by the main parser loop - it wraps the projection

        return new ProjectionNode(left, right);
    }

    /**
     * Parses the right-hand side after a filter [?...].
     * Note: FLATTEN ([]) is NOT parsed as part of filter RHS - it wraps the filter result.
     */
    private Node parseFilterRhs(Node left, Node condition) {
        Node right = null;

        if (current.is(TokenType.DOT)) {
            advance();
            right = parseProjectionTargetAfterDot();
        } else if (current.is(TokenType.LBRACKET)) {
            // LBRACKET continues the filter's RHS, but FLATTEN does not
            right = parseProjectionTarget();
        }
        // FLATTEN is handled by the main parser loop - it wraps the filter

        return new FilterNode(left, condition, right);
    }

    /**
     * Parses the right-hand side after a slice.
     */
    private Node parseSliceRhs(Node left, Node slice) {
        // A slice on its own applies to left, then we may have projection rhs
        Node sliceExpr = new IndexExpressionNode(left, slice);

        if (current.is(TokenType.DOT)) {
            advance();
            Node right = parseProjectionTargetAfterDot();
            return new ProjectionNode(sliceExpr, right);
        } else if (current.is(TokenType.LBRACKET)) {
            Node right = parseProjectionTarget();
            return new ProjectionNode(sliceExpr, right);
        }
        // FLATTEN is handled by the main parser loop

        // Slice without projection - just return as projection for element access
        return new ProjectionNode(sliceExpr, null);
    }

    /**
     * Parses a flatten operation.
     * Note: Consecutive FLATTEN is handled by the main parser loop - it wraps this flatten.
     */
    private Node parseFlatten(Node left) {
        Node flatten = new FlattenNode(left);

        // Check for projection continuation
        if (current.is(TokenType.DOT)) {
            advance();
            Node right = parseProjectionTargetAfterDot();
            return new ProjectionNode(flatten, right);
        } else if (current.is(TokenType.LBRACKET)) {
            // LBRACKET continues the flatten's projection RHS
            Node right = parseProjectionTarget();
            return new ProjectionNode(flatten, right);
        }
        // FLATTEN is handled by the main parser loop - it wraps this flatten

        return flatten;
    }

    /**
     * Parses a projection target after a dot.
     * Allows multi-select lists but not index/slice.
     */
    private Node parseProjectionTargetAfterDot() {
        if (current.is(TokenType.LBRACKET)) {
            advance();
            // After dot, brackets must contain a multi-select list (expressions), not index/slice
            return parseMultiSelectListOnly(null);
        }

        if (current.is(TokenType.STAR)) {
            advance();
            return parseObjectProjection(null);
        }

        if (current.is(TokenType.LBRACE)) {
            advance();
            return parseMultiSelectHash();
        }

        if (
            current.is(TokenType.IDENTIFIER) ||
            current.is(TokenType.QUOTED_IDENTIFIER)
        ) {
            String name = current.stringValue();
            advance();
            Node ident = new IdentifierNode(name);

            // Check for function call
            if (current.is(TokenType.LPAREN)) {
                advance();
                return parseFunctionCall(name);
            }

            // Check for continued expression
            if (current.is(TokenType.DOT)) {
                advance();
                Node right = parseProjectionTargetAfterDot();
                return new SubExpressionNode(ident, right);
            }

            if (current.is(TokenType.LBRACKET)) {
                advance();
                return parseBracket(ident);
            }

            return ident;
        }

        throw error("Expected identifier after '.'");
    }

    /**
     * Parses a projection target (what comes after a projection).
     */
    private Node parseProjectionTarget() {
        if (current.is(TokenType.LBRACKET)) {
            advance();
            return parseBracket(null);
        }

        if (current.is(TokenType.FLATTEN)) {
            advance();
            return parseFlatten(null);
        }

        if (current.is(TokenType.STAR)) {
            advance();
            return parseObjectProjection(null);
        }

        if (current.is(TokenType.LBRACE)) {
            advance();
            return parseMultiSelectHash();
        }

        if (
            current.is(TokenType.IDENTIFIER) ||
            current.is(TokenType.QUOTED_IDENTIFIER)
        ) {
            String name = current.stringValue();
            advance();
            Node ident = new IdentifierNode(name);

            // Check for function call
            if (current.is(TokenType.LPAREN)) {
                advance();
                return parseFunctionCall(name);
            }

            // Check for continued expression
            if (current.is(TokenType.DOT)) {
                advance();
                Node right = parseProjectionTarget();
                return new SubExpressionNode(ident, right);
            }

            if (current.is(TokenType.LBRACKET)) {
                advance();
                return parseBracket(ident);
            }

            return ident;
        }

        throw error("Expected projection target");
    }

    /**
     * Parses a function call.
     */
    private Node parseFunctionCall(String name) {
        List<Node> args = new ArrayList<Node>();

        if (!current.is(TokenType.RPAREN)) {
            args.add(parseFunctionArg());

            while (current.is(TokenType.COMMA)) {
                advance();
                args.add(parseFunctionArg());
            }
        }

        expect(TokenType.RPAREN);
        return new FunctionCallNode(name, args);
    }

    /**
     * Parses a function argument.
     */
    private Node parseFunctionArg() {
        if (current.is(TokenType.AMPERSAND)) {
            advance();
            Node expr = expression(0);
            return new ExpressionRefNode(expr);
        }
        return expression(0);
    }

    /**
     * Parses a let expression (JEP-18).
     * Syntax: let $var1 = expr1, $var2 = expr2 in body
     */
    private Node parseLetExpression() {
        List<Binding> bindings = new ArrayList<Binding>();

        // Parse first binding
        bindings.add(parseLetBinding());

        // Parse additional bindings separated by comma
        while (current.is(TokenType.COMMA)) {
            advance();
            bindings.add(parseLetBinding());
        }

        // Expect 'in' keyword
        if (!current.is(TokenType.IN)) {
            throw error("Expected 'in' after let bindings");
        }
        advance();

        // Parse body expression
        Node body = expression(0);

        return new LetNode(bindings, body);
    }

    /**
     * Parses a single let binding: $name = expression
     */
    private Binding parseLetBinding() {
        // Expect variable
        if (!current.is(TokenType.VARIABLE)) {
            throw error("Expected variable in let binding");
        }
        String name = current.stringValue();
        advance();

        // Expect '='
        if (!current.is(TokenType.ASSIGN)) {
            throw error("Expected '=' after variable in let binding");
        }
        advance();

        // Parse value expression
        Node value = expression(0);

        return new Binding(name, value);
    }

    /**
     * Advances to the next token.
     */
    private void advance() {
        current = lexer.next();
    }

    /**
     * Expects the current token to be of a specific type and advances.
     */
    private void expect(TokenType type) {
        if (!current.is(type)) {
            throw error("Expected " + type + " but found " + current.type());
        }
        advance();
    }

    /**
     * Creates a parse error with position information.
     */
    private JmesPathException error(String message) {
        return new JmesPathException(
            message,
            JmesPathException.ErrorType.SYNTAX,
            current.position(),
            current.line(),
            current.column()
        );
    }
}
