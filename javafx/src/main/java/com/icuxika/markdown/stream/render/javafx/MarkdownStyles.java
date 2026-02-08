package com.icuxika.markdown.stream.render.javafx;

import javafx.scene.Parent;
import javafx.scene.Scene;

public final class MarkdownStyles {

    private static final String CSS_BASE_PATH = "/com/icuxika/markdown/stream/render/javafx/css/";
    private static final String MARKDOWN_CSS = CSS_BASE_PATH + "markdown.css";
    private static final String LIGHT_CSS = CSS_BASE_PATH + "light.css";
    private static final String DARK_CSS = CSS_BASE_PATH + "dark.css";
    private static final String ADMONITION_CSS = CSS_BASE_PATH + "extensions/admonition.css";
    private static final String MATH_CSS = CSS_BASE_PATH + "extensions/math.css";

    private MarkdownStyles() {
    }

    public static String markdownCssUrl() {
        return MarkdownStyles.class.getResource(MARKDOWN_CSS).toExternalForm();
    }

    public static String lightCssUrl() {
        return MarkdownStyles.class.getResource(LIGHT_CSS).toExternalForm();
    }

    public static String darkCssUrl() {
        return MarkdownStyles.class.getResource(DARK_CSS).toExternalForm();
    }

    public static String admonitionCssUrl() {
        return MarkdownStyles.class.getResource(ADMONITION_CSS).toExternalForm();
    }

    public static String mathCssUrl() {
        return MarkdownStyles.class.getResource(MATH_CSS).toExternalForm();
    }

    public static void applyBase(Scene scene, boolean includeExtensions) {
        applyBase(scene.getStylesheets(), includeExtensions);
    }

    public static void applyBase(Parent parent, boolean includeExtensions) {
        applyBase(parent.getStylesheets(), includeExtensions);
    }

    static void applyBase(javafx.collections.ObservableList<String> stylesheets, boolean includeExtensions) {
        addIfAbsent(stylesheets, markdownCssUrl());
        if (includeExtensions) {
            addIfAbsent(stylesheets, admonitionCssUrl());
            addIfAbsent(stylesheets, mathCssUrl());
        }
    }

    static void addIfAbsent(javafx.collections.ObservableList<String> stylesheets, String url) {
        if (!stylesheets.contains(url)) {
            stylesheets.add(url);
        }
    }
}
