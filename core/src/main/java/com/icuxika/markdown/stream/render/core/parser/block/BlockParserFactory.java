package com.icuxika.markdown.stream.render.core.parser.block;

/**
 * Interface for factories that can create block parsers.
 */
public interface BlockParserFactory {

	/**
	 * Try to start a new block at the current position.
	 *
	 * @param state
	 *            the parser state
	 * @param matchedBlockParser
	 *            the last matched block parser (parent of the new block if started)
	 * @return result indicating if a block was started
	 */
	BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser);
}
