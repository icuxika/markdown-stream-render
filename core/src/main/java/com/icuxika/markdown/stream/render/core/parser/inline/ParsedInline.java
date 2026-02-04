package com.icuxika.markdown.stream.render.core.parser.inline;

import com.icuxika.markdown.stream.render.core.ast.Node;

/**
 * Result of {@link InlineContentParser#tryParse}.
 */
public class ParsedInline {

    private final Node node;
    private final int newIndex;

    private ParsedInline(Node node, int newIndex) {
        this.node = node;
        this.newIndex = newIndex;
    }

    public static ParsedInline none() {
        return null;
    }

    public static ParsedInline of(Node node, int newIndex) {
        return new ParsedInline(node, newIndex);
    }

    public Node getNode() {
        return node;
    }

    public int getNewIndex() {
        return newIndex;
    }
}
