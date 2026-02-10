package com.icuxika.markdown.stream.render.html.extension.math;

import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRendererExtension;

/**
 * Math HTML extension.
 */
public class MathHtmlExtension implements HtmlRendererExtension {

	private MathHtmlExtension() {
	}

	public static MathHtmlExtension create() {
		return new MathHtmlExtension();
	}

	@Override
	public void extend(HtmlRenderer.Builder builder) {
		builder.nodeRendererFactory(MathHtmlRenderer::new);
	}
}
