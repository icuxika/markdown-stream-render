package com.icuxika.core.ast;

public class SoftBreak extends Inline {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
