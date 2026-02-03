package com.icuxika.core.ast;

public class HtmlInline extends Node {
    private String literal;

    public HtmlInline(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
