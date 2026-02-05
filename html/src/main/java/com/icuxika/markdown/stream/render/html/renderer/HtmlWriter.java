package com.icuxika.markdown.stream.render.html.renderer;

import java.io.IOException;

/**
 * Simple helper for writing HTML tags and attributes.
 */
public class HtmlWriter {

    private final Appendable buffer;

    public HtmlWriter(Appendable buffer) {
        this.buffer = buffer;
    }

    public void raw(String s) {
        try {
            buffer.append(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void text(String text) {
        try {
            // Basic HTML escaping
            buffer.append(escapeXml(text));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void tag(String name) {
        tag(name, java.util.Collections.emptyMap());
    }

    public void tag(String name, java.util.Map<String, String> attributes) {
        tag(name, attributes, false);
    }

    public void tag(String name, java.util.Map<String, String> attributes, boolean voidElement) {
        raw("<");
        raw(name);
        if (attributes != null && !attributes.isEmpty()) {
            // Do NOT sort attributes. Rely on caller's order (LinkedHashMap) to match CommonMark spec tests exact output.
            for (java.util.Map.Entry<String, String> entry : attributes.entrySet()) {
                raw(" ");
                raw(entry.getKey());
                raw("=\"");
                raw(escapeXml(entry.getValue()));
                raw("\"");
            }
        }
        if (voidElement) {
            raw(" />");
        } else {
            raw(">");
        }
    }

    public void closeTag(String name) {
        raw("</");
        raw(name);
        raw(">");
    }

    public void line() {
        raw("\n");
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
