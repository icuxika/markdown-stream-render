package com.icuxika.markdown.stream.render.core.parser.block;

public interface ParserState {

    /**
     * Get current line.
     *
     * @return the current line content
     */
    CharSequence getLine();

    /**
     * Get current index. /** Get current index.
     *
     * @return the current index within the line
     */
    int getIndex();

    /**
     * Get next non-space index. /** Get next non-space index.
     *
     * @return the index of the next non-space character
     */
    int getNextNonSpaceIndex();

    /**
     * Get indent. /** Get indent.
     *
     * @return the indentation of the next non-space character
     */
    int getIndent();

    /**
     * Is blank. /** Is blank.
     *
     * @return true if the current line is blank (contains only whitespace)
     */
    boolean isBlank();

    /**
     * Get active block parser. /** Get active block parser.
     *
     * @return the deepest open block parser
     */
    BlockParser getActiveBlockParser();
}
