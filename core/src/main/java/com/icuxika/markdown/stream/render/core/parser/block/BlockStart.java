package com.icuxika.markdown.stream.render.core.parser.block;

/**
 * Result of {@link BlockParserFactory#tryStart}.
 */
public class BlockStart {

	private final BlockParser[] blockParsers;
	private final int newIndex;
	private final int newIndent;
	private final boolean replaceActiveBlockParser;

	private BlockStart(BlockParser[] blockParsers, int newIndex, int newIndent, boolean replaceActiveBlockParser) {
		this.blockParsers = blockParsers;
		this.newIndex = newIndex;
		this.newIndent = newIndent;
		this.replaceActiveBlockParser = replaceActiveBlockParser;
	}

	public static BlockStart none() {
		return null;
	}

	public static BlockStart of(BlockParser... blockParsers) {
		return new BlockStart(blockParsers, -1, -1, false);
	}

	public BlockStart atIndex(int newIndex) {
		return new BlockStart(blockParsers, newIndex, newIndent, replaceActiveBlockParser);
	}

	public BlockStart atIndent(int newIndent) {
		return new BlockStart(blockParsers, newIndex, newIndent, replaceActiveBlockParser);
	}

	public BlockStart replaceActiveBlockParser() {
		return new BlockStart(blockParsers, newIndex, newIndent, true);
	}

	public BlockParser[] getBlockParsers() {
		return blockParsers;
	}

	public int getNewIndex() {
		return newIndex;
	}

	public int getNewIndent() {
		return newIndent;
	}

	public boolean isReplaceActiveBlockParser() {
		return replaceActiveBlockParser;
	}
}
