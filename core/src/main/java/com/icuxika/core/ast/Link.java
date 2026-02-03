package com.icuxika.core.ast;

public class Link extends Inline {
    private String destination;
    private String title;

    public Link(String destination, String title) {
        this.destination = destination;
        this.title = title;
    }

    public String getDestination() {
        return destination;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
