package com.icuxika.markdown.stream.render.javafx.extension.math;

import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRendererExtension;

/**
 * Math JavaFX extension.
 */
public class MathJavaFxExtension implements JavaFxRendererExtension {

	private MathJavaFxExtension() {
	}

	public static MathJavaFxExtension create() {
		return new MathJavaFxExtension();
	}

	@Override
	public void extend(JavaFxRenderer.Builder builder) {
		builder.nodeRendererFactory(MathJavaFxRenderer::new);
	}
}
