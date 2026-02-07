package com.icuxika.markdown.stream.render.core.parser.block;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.InlineParser;

/**
 * Parser for a specific block node (e.g. BlockQuote, List, custom block).
 */
public interface BlockParser {

    /**
     * Is container.
     *
     * @return true if the block is a container (can contain other blocks), false if it is a leaf.
     */
    boolean isContainer();

    /**
     * Try continue.
     *
     * @param state
     *            parser state
     * @return Check if the block can continue with the current line.
     */
    BlockContinue tryContinue(ParserState state);

    /**
     * Add line.
     *
     * @param line
     *            line content
     */
    void addLine(CharSequence line);

    /**
     * Parse inlines.
     *
     * @param inlineParser
     *            inline parser
     */
    void parseInlines(InlineParser inlineParser);

    /**
     * Called when the block is closed.
     */
    void closeBlock();

    /**
     * Get block node.
     *
     * @return the AST node corresponding to this block
     */
    Node getBlock();
}
