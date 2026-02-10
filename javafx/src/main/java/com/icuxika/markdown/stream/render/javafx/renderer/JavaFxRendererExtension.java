package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.Extension;

/**
 * Extension for {@link JavaFxRenderer}.
 */
public interface JavaFxRendererExtension extends Extension {
	/**
	 * Extend the renderer builder.
	 *
	 * @param builder
	 *            the renderer builder
	 */
	void extend(JavaFxRenderer.Builder builder);
}
