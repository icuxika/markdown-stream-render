package com.icuxika.core.ast;

public class Paragraph extends Block {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
