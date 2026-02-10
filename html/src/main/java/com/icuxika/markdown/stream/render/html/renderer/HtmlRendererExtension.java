package com.icuxika.markdown.stream.render.html.renderer;

import com.icuxika.markdown.stream.render.core.Extension;

/**
 * Extension for {@link HtmlRenderer}.
 */
public interface HtmlRendererExtension extends Extension {
	/**
	 * Extend the renderer builder.
	 *
	 * @param builder
	 *            the renderer builder
	 */
	void extend(HtmlRenderer.Builder builder);
}
