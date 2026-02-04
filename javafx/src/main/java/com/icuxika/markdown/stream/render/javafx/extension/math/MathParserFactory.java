package com.icuxika.markdown.stream.render.javafx.extension.math;

import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParser;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParserFactory;

import java.util.Collections;
import java.util.Set;

public class MathParserFactory implements InlineContentParserFactory {
    @Override
    public Set<Character> getTriggerCharacters() {
        return Collections.singleton('$');
    }

    @Override
    public InlineContentParser create() {
        return new MathParser();
    }
}
