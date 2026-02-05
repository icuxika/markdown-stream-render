package com.icuxika.markdown.stream.render.html.renderer;

import com.icuxika.markdown.stream.render.core.ast.*;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParserOptions;
import com.icuxika.markdown.stream.render.core.renderer.IMarkdownRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlRenderer implements IMarkdownRenderer, NodeRendererContext {
    private final StringBuilder sb = new StringBuilder();
    private final HtmlWriter htmlWriter;
    private final MarkdownParserOptions options;
    private final List<NodeRenderer> nodeRenderers = new ArrayList<>();
    private final Map<Class<? extends Node>, NodeRenderer> rendererMap = new HashMap<>();

    public HtmlRenderer(Builder builder) {
        this.options = builder.options;
        this.sb.append(""); // Or initialize if needed
        this.htmlWriter = new HtmlWriter(sb);

        // Add core renderer (last fallback)
        // Correct order: Core must be added FIRST to the factories list, OR extensions must be processed AFTER core.
        // Current implementation: Builder.nodeRendererFactories contains user extensions.
        // We want user extensions to override core.
        // So we add core factory to the beginning of the list, or iterate it first.

        List<NodeRendererFactory> allFactories = new ArrayList<>();
        allFactories.add(context -> new CoreHtmlNodeRenderer(context));
        allFactories.addAll(builder.nodeRendererFactories);

        // Create renderers
        for (NodeRendererFactory factory : allFactories) {
            NodeRenderer renderer = factory.create(this);
            nodeRenderers.add(renderer);
            for (Class<? extends Node> nodeType : renderer.getNodeTypes()) {
                rendererMap.put(nodeType, renderer);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MarkdownParserOptions options = new MarkdownParserOptions();
        private List<NodeRendererFactory> nodeRendererFactories = new ArrayList<>();

        public Builder options(MarkdownParserOptions options) {
            this.options = options;
            return this;
        }

        public Builder nodeRendererFactory(NodeRendererFactory factory) {
            this.nodeRendererFactories.add(factory);
            return this;
        }

        public HtmlRenderer build() {
            // Ensure Core is the first one, so others can override
            // But wait, if we iterate list and put into map, the LAST one in the list wins.
            // So Core should be FIRST in the list.
            // But we don't have Core available here statically easily without creating circular deps or exposing CoreHtmlNodeRenderer public.
            // We will handle core insertion in constructor or let constructor handle it.
            // Let's rely on constructor adding Core FIRST.
            return new HtmlRenderer(this);
        }
    }

    // Deprecated constructors for backward compatibility
    public HtmlRenderer() {
        this(new Builder());
    }

    public HtmlRenderer(MarkdownParserOptions options) {
        this(new Builder().options(options));
    }

    @Override
    public HtmlWriter getWriter() {
        return htmlWriter;
    }

    @Override
    public void renderChildren(Node parent) {
        Node child = parent.getFirstChild();
        while (child != null) {
            Node next = child.getNext();
            render(child);
            child = next;
        }
    }

    public void render(Node node) {
        NodeRenderer renderer = rendererMap.get(node.getClass());
        if (renderer != null) {
            renderer.render(node);
        } else {
            // Fallback or generic traversal?
            // If no renderer found, maybe just visit children? 
            // Or do nothing? CommonMark spec usually ignores unknown nodes.
            // For now, visit children to ensure traversal continues if it's a container
            renderChildren(node);
        }
    }

    @Override
    public Object getResult() {
        return sb.toString();
    }

    @Override
    public void visit(Document document) {
        renderChildren(document);
    }

    // Delegate all visits to the renderer map logic
    // But IMarkdownRenderer extends Visitor, so we must implement visit(Type).
    // We can just redirect all visit calls to 'render(node)'.
    // But 'render(node)' is what calls 'visit' in the Visitor pattern... 
    // Wait, the Visitor pattern in IMarkdownRenderer calls 'node.accept(visitor)'.
    // 'node.accept' calls 'visitor.visit(this)'.
    // So if we implement visit(Paragraph p), we should delegate to our map.

    @Override
    public void visit(HtmlBlock node) {
        render(node);
    }

    @Override
    public void visit(HtmlInline node) {
        render(node);
    }

    @Override
    public void visit(Paragraph node) {
        render(node);
    }

    @Override
    public void visit(Heading node) {
        render(node);
    }

    @Override
    public void visit(Text node) {
        render(node);
    }

    @Override
    public void visit(SoftBreak node) {
        render(node);
    }

    @Override
    public void visit(HardBreak node) {
        render(node);
    }

    @Override
    public void visit(Emphasis node) {
        render(node);
    }

    @Override
    public void visit(StrongEmphasis node) {
        render(node);
    }

    @Override
    public void visit(BlockQuote node) {
        render(node);
    }

    @Override
    public void visit(BulletList node) {
        render(node);
    }

    @Override
    public void visit(OrderedList node) {
        render(node);
    }

    @Override
    public void visit(ListItem node) {
        render(node);
    }

    @Override
    public void visit(Code node) {
        render(node);
    }

    @Override
    public void visit(ThematicBreak node) {
        render(node);
    }

    @Override
    public void visit(CodeBlock node) {
        render(node);
    }

    @Override
    public void visit(Link node) {
        render(node);
    }

    @Override
    public void visit(Image node) {
        render(node);
    }

    @Override
    public void visit(Table node) {
        render(node);
    }

    @Override
    public void visit(TableHead node) {
        render(node);
    }

    @Override
    public void visit(TableBody node) {
        render(node);
    }

    @Override
    public void visit(TableRow node) {
        render(node);
    }

    @Override
    public void visit(TableCell node) {
        render(node);
    }

    @Override
    public void visit(Strikethrough node) {
        render(node);
    }

    public MarkdownParserOptions getOptions() {
        return options;
    }
}
