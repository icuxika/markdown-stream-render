package com.icuxika.markdown.stream.render.core.ast;

public class OrderedList extends Block {
    private char delimiter; // '.' or ')'
    private int startNumber;
    private boolean tight = true;

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public int getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(int startNumber) {
        this.startNumber = startNumber;
    }

    public boolean isTight() {
        return tight;
    }

    public void setTight(boolean tight) {
        this.tight = tight;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
