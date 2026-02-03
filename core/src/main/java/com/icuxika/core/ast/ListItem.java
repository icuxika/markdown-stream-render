package com.icuxika.core.ast;

public class ListItem extends Block {
    private boolean endsWithBlankLine = false;

    public boolean isEndsWithBlankLine() {
        return endsWithBlankLine;
    }

    public void setEndsWithBlankLine(boolean endsWithBlankLine) {
        this.endsWithBlankLine = endsWithBlankLine;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
