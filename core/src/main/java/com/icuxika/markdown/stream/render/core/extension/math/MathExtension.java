package com.icuxika.markdown.stream.render.core.extension.math;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.ParserExtension;

/**
 * Math extension.
 */
public class MathExtension implements ParserExtension {

	private MathExtension() {
	}

	public static MathExtension create() {
		return new MathExtension();
	}

	@Override
	public void extend(MarkdownParser.Builder builder) {
		builder.inlineParserFactory(new MathParserFactory());
	}
}
