package com.icuxika.markdown.stream.render.core.parser.block;

/**
 * Result of {@link BlockParser#tryContinue}.
 */
public class BlockContinue {

	private final int newIndex;
	private final int newIndent;
	private final boolean finalize;

	private BlockContinue(int newIndex, int newIndent, boolean finalize) {
		this.newIndex = newIndex;
		this.newIndent = newIndent;
		this.finalize = finalize;
	}

	public static BlockContinue none() {
		return null;
	}

	public static BlockContinue atIndex(int newIndex) {
		return new BlockContinue(newIndex, -1, false);
	}

	public static BlockContinue atIndex(int newIndex, int newIndent) {
		return new BlockContinue(newIndex, newIndent, false);
	}

	public static BlockContinue finished() {
		return new BlockContinue(-1, -1, true);
	}

	public int getNewIndex() {
		return newIndex;
	}

	public int getNewIndent() {
		return newIndent;
	}

	public boolean isFinalize() {
		return finalize;
	}
}
