package com.icuxika.markdown.stream.render.core.parser.block;

/**
 * Wraps a matched block parser.
 */
public interface MatchedBlockParser {
	BlockParser getBlockParser();

	CharSequence getParagraphContent();
}
