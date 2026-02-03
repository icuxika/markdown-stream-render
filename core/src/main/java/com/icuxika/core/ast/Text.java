package com.icuxika.core.ast;

public class Text extends Inline {
    private String literal;

    public Text(String literal) {
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
