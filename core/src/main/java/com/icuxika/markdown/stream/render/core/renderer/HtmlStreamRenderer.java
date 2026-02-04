package com.icuxika.markdown.stream.render.core.renderer;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.*;

import java.io.IOException;

/**
 * HTML 流式渲染器。
 * <p>
 * 将接收到的 AST 节点直接转换为 HTML 字符串并写入输出流。
 * 注意：由于是流式输出，它不维护完整的 HTML 结构（如 body 标签），只输出片段。
 * </p>
 */
public class HtmlStreamRenderer implements IStreamMarkdownRenderer {

    private final Appendable out;

    public HtmlStreamRenderer(Appendable out) {
        this.out = out;
    }

    @Override
    public void renderNode(Node node) {
        try {
            // Prevent duplicate rendering:
            // Only render Leaf Nodes (nodes that contain text/inlines directly, or are atomic like ThematicBreak).
            // Container nodes (BlockQuote, List, ListItem) are skipped here because their children 
            // will be (or have been) rendered individually.
            // 
            // Note: This produces a "flattened" stream without proper <ul>/<blockquote> wrappers.
            // For a full HTML document, one would need to track state or use a different event model.
            // This implementation is sufficient for "chat-like" streaming where content appears sequentially.
            
            if (node instanceof Paragraph || node instanceof Heading || node instanceof CodeBlock 
                    || node instanceof HtmlBlock || node instanceof ThematicBreak) {
                render(node);
                if (out instanceof java.io.Flushable) {
                    ((java.io.Flushable) out).flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to output", e);
        }
    }

    private void render(Node node) throws IOException {
        if (node instanceof Document) {
            // Do nothing for document root in stream
        } else if (node instanceof Paragraph) {
            out.append("<p>");
            renderInlines(node);
            out.append("</p>\n");
        } else if (node instanceof Heading) {
            Heading h = (Heading) node;
            out.append("<h").append(String.valueOf(h.getLevel())).append(">");
            renderInlines(node);
            out.append("</h").append(String.valueOf(h.getLevel())).append(">\n");
        } else if (node instanceof BlockQuote) {
            out.append("<blockquote>\n");
            // BlockQuote is a container. In streaming, we might receive children separately?
            // Wait, onBlockFinalized is called when the block is closed.
            // For container blocks (BlockQuote, List), their children are already in the tree?
            // Yes, because we finalize from leaf up.
            // But if we finalize BlockQuote, its children have already been finalized and potentially rendered?
            // Ah, this is the tricky part of "streaming containers".
            // If we use `onBlockFinalized`, we get the event when the container closes.
            // By then, its children are already fully parsed.
            // If we render the container THEN, we duplicate the children if we also rendered them individually.
            
            // Strategy:
            // 1. Only render LEAF blocks (Paragraph, Heading, CodeBlock, ThematicBreak, HtmlBlock).
            // 2. For Container blocks (BlockQuote, List), we only render their start/end tags?
            // But we can't easily insert "<blockquote>" before the children if the children were already rendered to the stream.
            
            // Refined Strategy for Stream Rendering:
            // - The StreamParser emits events for finalized blocks.
            // - Leaf blocks are finalized first.
            // - Container blocks are finalized last.
            // If we output to a linear stream (console/network), we can't "wrap" previous output.
            
            // Alternative:
            // We only support streaming of "Top Level" blocks?
            // Or we treat container blocks as "modifiers" for future blocks?
            // But BlockQuote syntax "> " is per line.
            
            // Let's look at how browsers do it. They have a stack of open elements.
            // But here we are generating HTML string.
            
            // Compromise for this task:
            // Simple Stream Renderer basically just renders LEAF nodes.
            // It ignores container structure in the output stream (flattens it) 
            // OR we accept that for BlockQuote/List, we might need to buffer or just render the content.
            // BUT, if we want to show structure, we need to know when a container STARTS.
            // `onBlockFinalized` is too late for opening tags.
            
            // To support Containers properly in a Stream, we need `onBlockStarted` event.
            // But `StreamMarkdownParser` currently only has `onBlockFinalized`.
            
            // For now, let's implement a "Simple Leaf Renderer" which is good enough for 
            // text streaming (like ChatGPT response which is mostly paragraphs/code blocks).
            // Complex nesting might look flattened.
            
            // Actually, `BlockParserState` knows when a block starts.
            // Adding `onBlockStarted` to parser is ideal but requires more changes.
            
            // Let's implement full rendering recursively for the finalized node.
            // AND we need to make sure we don't render children twice.
            // Wait, if `Paragraph` is child of `BlockQuote`.
            // 1. Paragraph finishes -> renderNode(Paragraph)
            // 2. BlockQuote finishes -> renderNode(BlockQuote) -> renders children (Paragraph) AGAIN.
            
            // Fix:
            // The renderer should ignore Container blocks if it's strictly appending leaves.
            // OR The parser should only emit "Top Level" blocks? No, that defeats streaming.
            
            // Decision:
            // We will only render Leaf Nodes in `renderNode`.
            // Container nodes will be ignored or just used for context?
            // If we ignore BlockQuote, we lose the styling.
            
            // To do it right:
            // We need `onBlockStarted` (open tag) and `onBlockEnded` (close tag).
            // I will update `StreamMarkdownParser` to support `onBlockStarted`?
            // Or just stick to leaf nodes for now as a "v1" of streaming.
            // Most "streaming markdown" libraries just re-render the whole HTML on every chunk for correctness.
            // But here we want "incremental append".
            
            // Let's stick to: Render Leaf Nodes.
            // If a leaf node has a parent that is a BlockQuote, we can prepend "> " or wrap it?
            // No, HTML doesn't work like that.
            
            // Okay, for HTML demo, let's just render Leaf Nodes.
            // It will demonstrate the "text appearing" effect.
            // Handling `<ul>` and `<blockquote>` properly requires state tracking (is ul open?).
            
            // renderChildren(node); // This method does not exist in this class. We should use renderInlines for recursion if needed, or implement renderChildren.
            // But for BlockQuote, we are skipping it in streaming leaf mode.
            out.append("</blockquote>\n");
        } else if (node instanceof CodeBlock) {
            out.append("<pre><code>");
            out.append(escapeXml(((CodeBlock) node).getLiteral()));
            out.append("</code></pre>\n");
        } else if (node instanceof HtmlBlock) {
            out.append(((HtmlBlock) node).getLiteral());
        } else if (node instanceof ThematicBreak) {
            out.append("<hr />\n");
        } else if (node instanceof BulletList || node instanceof OrderedList) { // List types
             // Ignore list containers for stream? 
             // Or maybe we just render <li> items when ListItem finishes.
        } else if (node instanceof ListItem) {
            out.append("<li>");
            // renderInlines(node); // This is wrong, ListItem has block children (Paragraph).
            // If we render ListItem, we render its children paragraphs.
            // But those paragraphs were already rendered when they finished!
            out.append("</li>\n");
        }
    }
    
    // Correct approach for Streaming HTML:
    // We maintain a state of "Open Tags".
    // But we only get "Finalized" events.
    // So we assume the structure is:
    // P -> render P
    // P -> render P
    // We lose the wrapping `<blockquote>` or `<ul>`.
    
    // For this demo, let's implement a robust "Leaf Renderer".
    // We check `node.getParent()` to see context, maybe insert a visual indicator?
    // No, let's just render the node itself.
    
    private void renderInlines(Node parent) throws IOException {
        Node child = parent.getFirstChild();
        while (child != null) {
            renderInline(child);
            child = child.getNext();
        }
    }

    private void renderInline(Node child) throws IOException {
        if (child instanceof Text) {
            out.append(escapeXml(((Text) child).getLiteral()));
        } else if (child instanceof Emphasis) {
            out.append("<em>");
            renderInlines(child);
            out.append("</em>");
        } else if (child instanceof StrongEmphasis) {
            out.append("<strong>");
            renderInlines(child);
            out.append("</strong>");
        } else if (child instanceof Code) {
            out.append("<code>");
            out.append(escapeXml(((Code) child).getLiteral()));
            out.append("</code>");
        } else if (child instanceof SoftBreak) {
            out.append("\n");
        } else if (child instanceof HardBreak) {
            out.append("<br />\n");
        } else if (child instanceof Link) {
            Link l = (Link) child;
            out.append("<a href=\"").append(escapeXml(l.getDestination())).append("\">");
            renderInlines(child);
            out.append("</a>");
        } else if (child instanceof Image) {
            Image i = (Image) child;
            out.append("<img src=\"").append(escapeXml(i.getDestination())).append("\" alt=\"placeholder\" />");
        } else if (child instanceof HtmlInline) {
            out.append(((HtmlInline) child).getLiteral());
        }
        // Handle custom nodes (fallback to children)
        else {
            renderInlines(child);
        }
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
