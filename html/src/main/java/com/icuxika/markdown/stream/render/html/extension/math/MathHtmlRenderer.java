package com.icuxika.markdown.stream.render.html.extension.math;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.extension.math.MathNode;
import com.icuxika.markdown.stream.render.html.renderer.HtmlWriter;
import com.icuxika.markdown.stream.render.html.renderer.NodeRenderer;
import com.icuxika.markdown.stream.render.html.renderer.NodeRendererContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MathHtmlRenderer implements NodeRenderer {
    private final NodeRendererContext context;

    public MathHtmlRenderer(NodeRendererContext context) {
        this.context = context;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.singleton(MathNode.class);
    }

    @Override
    public void render(Node node) {
        MathNode math = (MathNode) node;
        HtmlWriter html = context.getWriter();

        Map<String, String> attrs = new HashMap<>();
        attrs.put("class", "markdown-math");

        html.tag("span", attrs);
        html.text(math.getContent());
        html.closeTag("span");
    }
}
