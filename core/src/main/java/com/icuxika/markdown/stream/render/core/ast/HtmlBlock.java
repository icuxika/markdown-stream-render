package com.icuxika.markdown.stream.render.core.ast;

public class HtmlBlock extends Block {
    private String literal;

    public HtmlBlock(String literal) {
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
