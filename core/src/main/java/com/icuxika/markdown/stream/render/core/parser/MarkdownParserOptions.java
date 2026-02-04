package com.icuxika.markdown.stream.render.core.parser;

public class MarkdownParserOptions {
    private boolean gfm = true; // Default to GFM as requested by user previously
    private boolean safeMode = false; // Disallow raw HTML

    public boolean isGfm() {
        return gfm;
    }

    public void setGfm(boolean gfm) {
        this.gfm = gfm;
    }

    public boolean isSafeMode() {
        return safeMode;
    }

    public void setSafeMode(boolean safeMode) {
        this.safeMode = safeMode;
    }
}
