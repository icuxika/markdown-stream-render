package com.icuxika.markdown.stream.render.core.extension.admonition;

import com.icuxika.markdown.stream.render.core.ast.Block;
import com.icuxika.markdown.stream.render.core.parser.block.AbstractBlockParser;
import com.icuxika.markdown.stream.render.core.parser.block.BlockContinue;
import com.icuxika.markdown.stream.render.core.parser.block.ParserState;

public class AdmonitionParser extends AbstractBlockParser {

    private final AdmonitionBlock block;

    public AdmonitionParser(String type, String title) {
        this.block = new AdmonitionBlock(type, title);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (state.isBlank()) {
            return BlockContinue.atIndex(state.getNextNonSpaceIndex());
        }

        int indent = state.getIndent();
        if (indent >= 4) {
            return BlockContinue.atIndex(state.getIndex() + 4);
        }

        return BlockContinue.none();
    }
}
