package com.icuxika.markdown.stream.render.core.ast;

public class CodeBlock extends Block {
    private String literal;
    private String info;

    public CodeBlock(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
