package com.icuxika.markdown.stream.render.html.extension.admonition;

import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRendererExtension;

/**
 * Admonition HTML extension.
 */
public class AdmonitionHtmlExtension implements HtmlRendererExtension {

	private AdmonitionHtmlExtension() {
	}

	public static AdmonitionHtmlExtension create() {
		return new AdmonitionHtmlExtension();
	}

	@Override
	public void extend(HtmlRenderer.Builder builder) {
		builder.nodeRendererFactory(AdmonitionHtmlRenderer::new);
	}
}
