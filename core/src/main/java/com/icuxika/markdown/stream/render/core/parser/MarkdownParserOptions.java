package com.icuxika.markdown.stream.render.core.parser;

public class MarkdownParserOptions {
    private boolean gfm = true; // Default to GFM as requested by user previously
    private boolean safeMode = false; // Disallow raw HTML
    private boolean generateHeadingIds = false; // Generate id attributes for headings

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

    public boolean isGenerateHeadingIds() {
        return generateHeadingIds;
    }

    public void setGenerateHeadingIds(boolean generateHeadingIds) {
        this.generateHeadingIds = generateHeadingIds;
    }
}
