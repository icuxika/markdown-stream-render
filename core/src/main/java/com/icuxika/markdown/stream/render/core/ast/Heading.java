package com.icuxika.markdown.stream.render.core.ast;

public class Heading extends Block {
    private int level;
    private String anchorId; // For TOC navigation

    public Heading(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    
    public String getAnchorId() {
        return anchorId;
    }
    
    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
