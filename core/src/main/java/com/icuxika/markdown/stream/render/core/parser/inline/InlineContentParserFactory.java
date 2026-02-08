package com.icuxika.markdown.stream.render.core.parser.inline;

import java.util.Set;

/**
 * Factory for creating inline content parsers.
 */
public interface InlineContentParserFactory {

	/**
	 * Get trigger characters. /** Get trigger characters.
	 *
	 * @return the set of characters that trigger this parser
	 */
	Set<Character> getTriggerCharacters();

	/**
	 * Create a parser instance.
	 *
	 * @return a new parser instance
	 */
	InlineContentParser create();
}
