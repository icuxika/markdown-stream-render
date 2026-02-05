package com.icuxika.markdown.stream.render.core;

import com.icuxika.markdown.stream.render.core.extension.admonition.AdmonitionBlockParserFactory;
import com.icuxika.markdown.stream.render.core.extension.math.MathParserFactory;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;

public class CoreExtension {

    public static void addDefaults(MarkdownParser.Builder builder) {
        builder.blockParserFactory(new AdmonitionBlockParserFactory());
        builder.inlineParserFactory(new MathParserFactory());
    }

    public static void addDefaults(StreamMarkdownParser.Builder builder) {
        builder.blockParserFactory(new AdmonitionBlockParserFactory());
        builder.inlineParserFactory(new MathParserFactory());
    }
}
