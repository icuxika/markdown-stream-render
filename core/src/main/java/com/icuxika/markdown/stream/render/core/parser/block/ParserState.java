package com.icuxika.markdown.stream.render.core.parser.block;

public interface ParserState {

    /**
     * @return the current line content
     */
    CharSequence getLine();

    /**
     * @return the current index within the line
     */
    int getIndex();

    /**
     * @return the index of the next non-space character
     */
    int getNextNonSpaceIndex();

    /**
     * @return the indentation of the next non-space character
     */
    int getIndent();

    /**
     * @return true if the current line is blank (contains only whitespace)
     */
    boolean isBlank();

    /**
     * @return the deepest open block parser
     */
    BlockParser getActiveBlockParser();
}
