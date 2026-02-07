package com.icuxika.markdown.stream.render.core.parser.inline;

/**
 * Parses inline content (e.g., emphasis, code spans, custom syntax).
 */
public interface InlineContentParser {

    /**
     * Try to parse inline content starting at the current position.
     *
     * @param input
     *            the input string
     * @param index
     *            current index in input
     * @param state
     *            parser state
     * @return result indicating if content was parsed
     */
    ParsedInline tryParse(String input, int index, InlineParserState state);
}
