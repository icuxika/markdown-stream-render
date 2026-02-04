package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.ast.Image;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import com.icuxika.markdown.stream.render.core.renderer.HtmlWriter;
import com.icuxika.markdown.stream.render.core.renderer.NodeRenderer;
import com.icuxika.markdown.stream.render.core.renderer.NodeRendererContext;

import java.io.StringReader;
import java.util.Collections;
import java.util.Set;

/**
 * Demonstrates how to extend HtmlRenderer with a custom NodeRenderer.
 * This example replaces the standard Image rendering with a <figure> tag.
 */
public class PluginDemo {

    public static void main(String[] args) {
        String markdown = "Here is an image:\n\n![Alt Text](https://example.com/image.png \"Title\")";

        System.out.println("--- Original Renderer ---");
        HtmlRenderer originalRenderer = HtmlRenderer.builder().build();
        MarkdownParser parser = new MarkdownParser();
        try {
            parser.parse(new StringReader(markdown), originalRenderer);
            System.out.println(originalRenderer.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n--- Custom Figure Renderer ---");
        HtmlRenderer customRenderer = HtmlRenderer.builder()
                .nodeRendererFactory(context -> new FigureNodeRenderer(context))
                .build();

        try {
            parser.parse(new StringReader(markdown), customRenderer);
            System.out.println(customRenderer.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Custom renderer that renders Images as <figure> elements.
     */
    static class FigureNodeRenderer implements NodeRenderer {
        private final HtmlWriter html;
        private final NodeRendererContext context;

        public FigureNodeRenderer(NodeRendererContext context) {
            this.html = context.getWriter();
            this.context = context;
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Collections.singleton(Image.class);
        }

        @Override
        public void render(Node node) {
            Image image = (Image) node;
            html.tag("figure");
            html.line();

            // Image tag
            java.util.Map<String, String> attrs = new java.util.HashMap<>();
            attrs.put("src", image.getDestination());
            attrs.put("alt", "image"); // simplified alt
            html.tag("img", attrs, true);
            html.line();

            // Caption
            if (image.getTitle() != null) {
                html.tag("figcaption");
                html.text(image.getTitle());
                html.closeTag("figcaption");
                html.line();
            }

            html.closeTag("figure");
            html.line();
        }
    }
}
