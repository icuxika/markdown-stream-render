package com.icuxika.markdown.stream.render.javafx;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class MarkdownTheme {

	/**
	 * A markdown theme definition identified by an id and backed by a stylesheet
	 * URL.
	 */
	public static final class Theme {
		public static final Theme LIGHT = new Theme("light", MarkdownStyles.lightCssUrl());
		public static final Theme DARK = new Theme("dark", MarkdownStyles.darkCssUrl());

		private final String id;
		private final String cssUrl;

		/**
		 * Creates a theme.
		 *
		 * @param id
		 *            theme id (non-blank)
		 * @param cssUrl
		 *            theme stylesheet URL (non-blank)
		 */
		public Theme(String id, String cssUrl) {
			if (id == null || id.isBlank()) {
				throw new IllegalArgumentException("Theme id must not be blank");
			}
			if (cssUrl == null || cssUrl.isBlank()) {
				throw new IllegalArgumentException("Theme cssUrl must not be blank");
			}
			this.id = id;
			this.cssUrl = cssUrl;
		}

		/**
		 * Returns the theme id.
		 *
		 * @return theme id
		 */
		public String getId() {
			return id;
		}

		/**
		 * Returns the stylesheet URL.
		 *
		 * @return stylesheet URL
		 */
		public String getCssUrl() {
			return cssUrl;
		}
	}

	private final Map<String, Theme> registeredThemes = new LinkedHashMap<>();
	private final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(Theme.LIGHT);
	private boolean includeExtensions = true;

	/**
	 * Constructor.
	 */
	public MarkdownTheme() {
		registerTheme(Theme.LIGHT);
		registerTheme(Theme.DARK);
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
	 * Sets the current theme by id.
	 *
	 * @param id
	 *            theme id
	 */
	public void setTheme(String id) {
		Theme theme = registeredThemes.get(id);
		if (theme == null) {
			throw new IllegalArgumentException("Unknown theme id: " + id);
		}
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
	 * Returns a registered theme by id.
	 *
	 * @param id
	 *            theme id
	 * @return theme, or null if not registered
	 */
	public Theme getTheme(String id) {
		return registeredThemes.get(id);
	}

	/**
	 * Returns an unmodifiable view of all registered themes.
	 *
	 * @return registered themes
	 */
	public Map<String, Theme> getRegisteredThemes() {
		return Collections.unmodifiableMap(registeredThemes);
	}

	/**
	 * Registers a theme.
	 *
	 * @param theme
	 *            theme
	 */
	public void registerTheme(Theme theme) {
		registeredThemes.put(theme.getId(), theme);
	}

	/**
	 * Registers a theme and returns it.
	 *
	 * @param id
	 *            theme id
	 * @param cssUrl
	 *            theme stylesheet URL
	 * @return created theme
	 */
	public Theme registerTheme(String id, String cssUrl) {
		Theme theme = new Theme(id, cssUrl);
		registerTheme(theme);
		return theme;
	}

	/**
	 * Unregisters a theme by id. If it is the current theme, falls back to
	 * {@link Theme#LIGHT}.
	 *
	 * @param id
	 *            theme id
	 */
	public void unregisterTheme(String id) {
		registeredThemes.remove(id);
		Theme current = currentTheme.get();
		if (current != null && id != null && id.equals(current.getId())) {
			currentTheme.set(Theme.LIGHT);
		}
	}

	/**
	 * Sets whether extension stylesheets are applied together with base
	 * stylesheets.
	 *
	 * @param includeExtensions
	 *            whether to include extension stylesheets
	 */
	public void setIncludeExtensions(boolean includeExtensions) {
		this.includeExtensions = includeExtensions;
	}

	/**
	 * Returns whether extension stylesheets are applied together with base
	 * stylesheets.
	 *
	 * @return whether to include extension stylesheets
	 */
	public boolean isIncludeExtensions() {
		return includeExtensions;
	}

	/**
	 * Applies the markdown stylesheets to the given Scene. It binds the scene's
	 * stylesheets to the theme property.
	 */
	public void apply(Scene scene) {
		MarkdownStyles.applyBase(scene, includeExtensions);
		updateThemeStyle(scene.getStylesheets(), currentTheme.get());

		currentTheme.addListener((obs, oldTheme, newTheme) -> {
			updateThemeStyle(scene.getStylesheets(), newTheme);
		});
	}

	/**
	 * Applies the markdown stylesheets to the given Parent (e.g. VBox).
	 */
	public void apply(Parent parent) {
		MarkdownStyles.applyBase(parent, includeExtensions);
		updateThemeStyle(parent.getStylesheets(), currentTheme.get());

		currentTheme.addListener((obs, oldTheme, newTheme) -> {
			updateThemeStyle(parent.getStylesheets(), newTheme);
		});
	}

	private void updateThemeStyle(javafx.collections.ObservableList<String> stylesheets, Theme theme) {
		for (Theme t : registeredThemes.values()) {
			stylesheets.remove(t.getCssUrl());
		}
		if (theme != null) {
			MarkdownStyles.addIfAbsent(stylesheets, theme.getCssUrl());
		}
	}
}
