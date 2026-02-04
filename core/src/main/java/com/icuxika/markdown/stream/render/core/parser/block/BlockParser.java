package com.icuxika.markdown.stream.render.core.parser.block;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.InlineParser;

/**
 * Parser for a specific block node (e.g. BlockQuote, List, custom block).
 */
public interface BlockParser {

    /**
     * Return true if the block is a container (can contain other blocks), false if it is a leaf.
     */
    boolean isContainer();

    /**
     * Check if the block can continue with the current line.
     */
    BlockContinue tryContinue(ParserState state);

    /**
     * Add a line to the block (if it's a leaf block).
     */
    void addLine(CharSequence line);

    /**
     * Parse inlines for the block (if applicable).
     */
    void parseInlines(InlineParser inlineParser);

    /**
     * Called when the block is closed.
     */
    void closeBlock();

    /**
     * @return the AST node corresponding to this block
     */
    Node getBlock();
}
