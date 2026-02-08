package com.icuxika.markdown.stream.render.html;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HtmlCssProvider {

	/**
	 * Get the default Markdown CSS.
	 *
	 * @return CSS content
	 */
	public static String getMarkdownCss() {
		return loadCss("css/markdown.css");
	}

	/**
	 * Get the default theme CSS (light/dark variables via data-theme).
	 *
	 * @return CSS content
	 */
	public static String getThemeCss() {
		return loadCss("css/themes/themes.css");
	}

	/**
	 * Get the Admonition extension CSS.
	 *
	 * @return CSS content
	 */
	public static String getAdmonitionCss() {
		return loadCss("css/extensions/admonition.css");
	}

	/**
	 * Get the Math extension CSS.
	 *
	 * @return CSS content
	 */
	public static String getMathCss() {
		return loadCss("css/extensions/math.css");
	}

	/**
	 * Get all CSS combined.
	 *
	 * @return CSS content
	 */
	public static String getAllCss() {
		StringBuilder sb = new StringBuilder();
		sb.append(getThemeCss()).append("\n");
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
			return "/* Error loading CSS: " + path + " */";
		}
		return "/* CSS file not found: " + path + " */";
	}
}
