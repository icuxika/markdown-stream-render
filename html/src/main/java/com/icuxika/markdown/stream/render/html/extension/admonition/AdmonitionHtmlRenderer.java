package com.icuxika.markdown.stream.render.html.extension.admonition;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.extension.admonition.AdmonitionBlock;
import com.icuxika.markdown.stream.render.html.renderer.HtmlNodeRenderer;
import com.icuxika.markdown.stream.render.html.renderer.HtmlNodeRendererContext;
import com.icuxika.markdown.stream.render.html.renderer.HtmlWriter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AdmonitionHtmlRenderer implements HtmlNodeRenderer {
    private final HtmlNodeRendererContext context;

    public AdmonitionHtmlRenderer(HtmlNodeRendererContext context) {
        this.context = context;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.singleton(AdmonitionBlock.class);
    }

    @Override
    public void render(Node node) {
        AdmonitionBlock admonition = (AdmonitionBlock) node;
        HtmlWriter html = context.getWriter();

        Map<String, String> attrs = new HashMap<>();
        attrs.put("class", "admonition admonition-" + admonition.getType());

        html.tag("div", attrs);

        if (admonition.getTitle() != null) {
            Map<String, String> titleAttrs = new HashMap<>();
            titleAttrs.put("class", "admonition-title");
            html.tag("p", titleAttrs);
            html.text(admonition.getTitle());
            html.closeTag("p");
        }

        context.renderChildren(node);
        html.closeTag("div");
    }
}
