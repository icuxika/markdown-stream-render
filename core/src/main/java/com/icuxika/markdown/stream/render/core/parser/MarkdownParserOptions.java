package com.icuxika.markdown.stream.render.core.parser;

public class MarkdownParserOptions {
    private boolean gfm = true; // Default to GFM as requested by user previously

    public boolean isGfm() {
        return gfm;
    }

    public void setGfm(boolean gfm) {
        this.gfm = gfm;
    }
}
