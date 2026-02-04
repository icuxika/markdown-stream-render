package com.icuxika.markdown.stream.render.javafx;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.extension.admonition.AdmonitionBlockParserFactory;
import com.icuxika.markdown.stream.render.javafx.extension.math.MathParserFactory;

/**
 * Utility class to configure Markdown Parsers with JavaFX default extensions.
 */
public class MarkdownExtensions {

    public static void addDefaults(MarkdownParser.Builder builder) {
        builder.blockParserFactory(new AdmonitionBlockParserFactory());
        builder.inlineParserFactory(new MathParserFactory());
    }

    public static void addDefaults(StreamMarkdownParser.Builder builder) {
        builder.blockParserFactory(new AdmonitionBlockParserFactory());
        builder.inlineParserFactory(new MathParserFactory());
    }
}
