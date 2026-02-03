package com.icuxika.core.ast;

public class BlockQuote extends Block {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
