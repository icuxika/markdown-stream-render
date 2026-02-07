package com.icuxika.markdown.stream.render.html.renderer;

import java.io.IOException;

/**
 * Simple helper for writing HTML tags and attributes.
 */
public class HtmlWriter {

    private final Appendable buffer;
    private boolean escapeGt = true;

    /**
     * Constructor.
     *
     * @param buffer
     *            output buffer
     */
    public HtmlWriter(Appendable buffer) {
        this.buffer = buffer;
    }

    /**
     * Set whether to escape >.
     *
     * @param escapeGt
     *            true to escape
     */
    public void setEscapeGt(boolean escapeGt) {
        this.escapeGt = escapeGt;
    }

    /**
     * Write raw string.
     *
     * @param s
     *            string
     */
    public void raw(String s) {
        try {
            buffer.append(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write text (escaped).
     *
     * @param text
     *            text
     */
    public void text(String text) {
        try {
            // Basic HTML escaping
            buffer.append(escapeXml(text));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write tag.
     *
     * @param name
     *            tag name
     */
    public void tag(String name) {
        tag(name, java.util.Collections.emptyMap());
    }

    /**
     * Write tag with attributes.
     *
     * @param name
     *            tag name
     * @param attributes
     *            attributes
     */
    public void tag(String name, java.util.Map<String, String> attributes) {
        tag(name, attributes, false);
    }

    /**
     * Write tag with attributes and void flag.
     *
     * @param name
     *            tag name
     * @param attributes
     *            attributes
     * @param voidElement
     *            true if void element
     */
    public void tag(String name, java.util.Map<String, String> attributes, boolean voidElement) {
        raw("<");
        raw(name);
        if (attributes != null && !attributes.isEmpty()) {
            // Do NOT sort attributes. Rely on caller's order (LinkedHashMap) to match
            // CommonMark spec tests exact output.
            for (java.util.Map.Entry<String, String> entry : attributes.entrySet()) {
                raw(" ");
                raw(entry.getKey());
                raw("=\"");
                raw(escapeXml(entry.getValue()));
                raw("\"");
            }
        }
        if (voidElement) {
            // Special handling for GFM task list input which is rendered as HTML5 loose (no
            // slash)
            // while CommonMark core elements (br, hr, img) use XHTML style (with slash).
            if ("input".equals(name)) {
                raw(">");
            } else {
                raw(" />");
            }
        } else {
            raw(">");
        }
    }

    /**
     * Close tag.
     *
     * @param name
     *            tag name
     */
    public void closeTag(String name) {
        raw("</");
        raw(name);
        raw(">");
    }

    /**
     * Write new line.
     */
    public void line() {
        raw("\n");
    }

    private String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<' :
                    sb.append("&lt;");
                    break;
                case '>' :
                    if (escapeGt) {
                        sb.append("&gt;");
                    } else {
                        sb.append(">");
                    }
                    break;
                case '&' :
                    sb.append("&amp;");
                    break;
                case '"' :
                    sb.append("&quot;");
                    break;
                default :
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
