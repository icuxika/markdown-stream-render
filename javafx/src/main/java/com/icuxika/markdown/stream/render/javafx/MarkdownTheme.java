package com.icuxika.markdown.stream.render.javafx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class MarkdownTheme {

    public enum Theme {
        LIGHT("light.css"), DARK("dark.css");

        private final String cssFile;

        Theme(String cssFile) {
            this.cssFile = cssFile;
        }

        public String getCssFile() {
            return cssFile;
        }
    }

    private static final String CSS_BASE_PATH = "/com/icuxika/markdown/stream/render/javafx/css/";
    private static final String MARKDOWN_CSS = "markdown.css";

    private final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(Theme.LIGHT);

    /**
     * Constructor.
     */
    public MarkdownTheme() {
        currentTheme.addListener((obs, oldTheme, newTheme) -> {
            // This is a bit tricky because we need to know what to update.
            // Usually we bind a Scene or Parent to this theme manager.
        });
    }

    /**
     * Get the theme property.
     *
     * @return theme property
     */
    public ObjectProperty<Theme> themeProperty() {
        return currentTheme;
    }

    /**
     * Set the current theme.
     *
     * @param theme
     *            new theme
     */
    public void setTheme(Theme theme) {
        currentTheme.set(theme);
    }

    /**
     * Get the current theme.
     *
     * @return current theme
     */
    public Theme getTheme() {
        return currentTheme.get();
    }

    /**
     * Applies the markdown stylesheets to the given Scene. It binds the scene's stylesheets to the theme property.
     */
    public void apply(Scene scene) {
        scene.getStylesheets().add(getClass().getResource(CSS_BASE_PATH + MARKDOWN_CSS).toExternalForm());
        updateThemeStyle(scene.getStylesheets(), currentTheme.get());

        currentTheme.addListener((obs, oldTheme, newTheme) -> {
            updateThemeStyle(scene.getStylesheets(), newTheme);
        });
    }

    /**
     * Applies the markdown stylesheets to the given Parent (e.g. VBox).
     */
    public void apply(Parent parent) {
        parent.getStylesheets().add(getClass().getResource(CSS_BASE_PATH + MARKDOWN_CSS).toExternalForm());
        updateThemeStyle(parent.getStylesheets(), currentTheme.get());

        currentTheme.addListener((obs, oldTheme, newTheme) -> {
            updateThemeStyle(parent.getStylesheets(), newTheme);
        });
    }

    private void updateThemeStyle(javafx.collections.ObservableList<String> stylesheets, Theme theme) {
        // Remove old theme stylesheets
        for (Theme t : Theme.values()) {
            String url = getClass().getResource(CSS_BASE_PATH + t.getCssFile()).toExternalForm();
            stylesheets.remove(url);
        }
        // Add new theme stylesheet
        String newUrl = getClass().getResource(CSS_BASE_PATH + theme.getCssFile()).toExternalForm();
        if (!stylesheets.contains(newUrl)) {
            stylesheets.add(newUrl);
        }
    }
}
