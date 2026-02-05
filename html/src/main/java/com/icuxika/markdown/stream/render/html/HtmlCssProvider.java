package com.icuxika.markdown.stream.render.html;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HtmlCssProvider {

    public static String getMarkdownCss() {
        return loadCss("css/markdown.css");
    }

    public static String getAdmonitionCss() {
        return loadCss("css/extensions/admonition.css");
    }

    public static String getMathCss() {
        return loadCss("css/extensions/math.css");
    }

    public static String getAllCss() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMarkdownCss()).append("\n");
        sb.append(getAdmonitionCss()).append("\n");
        sb.append(getMathCss()).append("\n");
        return sb.toString();
    }

    private static String loadCss(String path) {
        try (InputStream is = HtmlCssProvider.class.getResourceAsStream(path)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "/* Error loading CSS: " + path + " */";
        }
        return "/* CSS file not found: " + path + " */";
    }
}
