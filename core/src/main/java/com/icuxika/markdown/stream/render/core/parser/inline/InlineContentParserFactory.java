package com.icuxika.markdown.stream.render.core.parser.inline;

import java.util.Set;

/**
 * Factory for creating inline content parsers.
 */
public interface InlineContentParserFactory {

    /**
     * @return the set of characters that trigger this parser
     */
    Set<Character> getTriggerCharacters();

    /**
     * Create a parser instance.
     */
    InlineContentParser create();
}
