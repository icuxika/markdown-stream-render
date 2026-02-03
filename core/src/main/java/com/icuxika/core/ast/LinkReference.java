package com.icuxika.core.ast;

public class LinkReference {
    private final String label;
    private final String destination;
    private final String title;

    public LinkReference(String label, String destination, String title) {
        this.label = label;
        this.destination = destination;
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public String getDestination() {
        return destination;
    }

    public String getTitle() {
        return title;
    }
}
