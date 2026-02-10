package com.icuxika.markdown.stream.render.javafx.extension.admonition;

import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRendererExtension;

/**
 * Admonition JavaFX extension.
 */
public class AdmonitionJavaFxExtension implements JavaFxRendererExtension {

	private AdmonitionJavaFxExtension() {
	}

	public static AdmonitionJavaFxExtension create() {
		return new AdmonitionJavaFxExtension();
	}

	@Override
	public void extend(JavaFxRenderer.Builder builder) {
		builder.nodeRendererFactory(AdmonitionJavaFxRenderer::new);
	}
}
