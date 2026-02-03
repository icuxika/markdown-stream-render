package com.icuxika.markdown.stream.render.core.ast;

public class Image extends Inline {
    private String destination;
    private String title;

    public Image(String destination, String title) {
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
