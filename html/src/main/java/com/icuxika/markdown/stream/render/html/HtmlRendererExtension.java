package com.icuxika.markdown.stream.render.html;

import com.icuxika.markdown.stream.render.html.extension.admonition.AdmonitionHtmlRenderer;
import com.icuxika.markdown.stream.render.html.extension.math.MathHtmlRenderer;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;

public class HtmlRendererExtension {

    public static void addDefaults(HtmlRenderer.Builder builder) {
        builder.nodeRendererFactory(AdmonitionHtmlRenderer::new);
        builder.nodeRendererFactory(MathHtmlRenderer::new);
    }
}
