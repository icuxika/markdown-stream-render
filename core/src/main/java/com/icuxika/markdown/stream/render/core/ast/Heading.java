package com.icuxika.markdown.stream.render.core.ast;

public class Heading extends Block {
    private int level;

    public Heading(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
