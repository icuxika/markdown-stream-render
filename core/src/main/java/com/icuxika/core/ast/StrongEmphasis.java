package com.icuxika.core.ast;

public class StrongEmphasis extends Inline {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
