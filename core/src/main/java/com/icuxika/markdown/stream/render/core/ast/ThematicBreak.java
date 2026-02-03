package com.icuxika.markdown.stream.render.core.ast;

public class ThematicBreak extends Block {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
