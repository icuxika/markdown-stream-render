package com.icuxika.markdown.stream.render.core.ast;

public class TableBody extends Block {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
