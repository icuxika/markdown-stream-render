package com.icuxika.markdown.stream.render.core.extension.admonition;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.ParserExtension;

/**
 * Admonition extension.
 */
public class AdmonitionExtension implements ParserExtension {

	private AdmonitionExtension() {
	}

	public static AdmonitionExtension create() {
		return new AdmonitionExtension();
	}

	@Override
	public void extend(MarkdownParser.Builder builder) {
		builder.blockParserFactory(new AdmonitionBlockParserFactory());
	}
}
