package com.icuxika.markdown.stream.render.core.parser;

import com.icuxika.markdown.stream.render.core.Extension;

/**
 * Extension for {@link MarkdownParser}.
 */
public interface ParserExtension extends Extension {
	/**
	 * Extend the parser builder.
	 *
	 * @param builder
	 *            the parser builder
	 */
	void extend(MarkdownParser.Builder builder);
}
