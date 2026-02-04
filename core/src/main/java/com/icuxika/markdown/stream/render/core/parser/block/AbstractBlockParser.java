package com.icuxika.markdown.stream.render.core.parser.block;

import com.icuxika.markdown.stream.render.core.parser.InlineParser;

public abstract class AbstractBlockParser implements BlockParser {

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public void addLine(CharSequence line) {
        // Default: do nothing (containers don't store lines directly usually)
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        // Default: do nothing
    }

    @Override
    public void closeBlock() {
        // Default: do nothing
    }
}
