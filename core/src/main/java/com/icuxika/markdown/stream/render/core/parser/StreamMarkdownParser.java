package com.icuxika.markdown.stream.render.core.parser;

import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.ast.Heading;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.Paragraph;
import com.icuxika.markdown.stream.render.core.ast.TableCell;
import com.icuxika.markdown.stream.render.core.ast.Text;
import com.icuxika.markdown.stream.render.core.parser.block.BlockParserFactory;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParserFactory;
import com.icuxika.markdown.stream.render.core.renderer.StreamMarkdownRenderer;
import java.util.ArrayList;
import java.util.List;

/**
 * 流式 Markdown 解析器.
 * <p>
 * 支持通过 {@link #push(String)} 方法增量输入文本，并实时触发渲染事件。
 * </p>
 */
public class StreamMarkdownParser {

    private final MarkdownParserOptions options;
    private final List<BlockParserFactory> blockParserFactories;
    private final List<InlineContentParserFactory> inlineParserFactories;
    private final StreamMarkdownRenderer renderer;

    // Internal State
    private final Document doc;
    private final MarkdownParser.BlockParserState state;
    private final StringBuilder buffer = new StringBuilder();
    private int lineNumber = 0;

    private StreamMarkdownParser(Builder builder) {
        this.options = builder.options;
        this.blockParserFactories = builder.blockParserFactories;
        this.inlineParserFactories = builder.inlineParserFactories;
        this.renderer = builder.renderer;

        this.doc = new Document();
        this.doc.setStartLine(0);

        this.state = new MarkdownParser.BlockParserState(blockParserFactories, options);
        this.state.setOnBlockFinalized(this::onBlockFinalized);
        this.state.setOnBlockStarted(this::onBlockStarted);
        this.state.setOnBlockClosed(this::onBlockClosed);
    }

    /**
     * 推送新的文本片段.
     *
     * @param text
     *            Markdown 文本片段
     */
    public void push(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        buffer.append(text);

        // Process full lines
        int start = 0;
        int len = buffer.length();
        for (int i = 0; i < len; i++) {
            char c = buffer.charAt(i);
            if (c == '\n' || c == '\r') {
                String line = buffer.substring(start, i);

                // Handle CRLF
                if (c == '\r' && i + 1 < len && buffer.charAt(i + 1) == '\n') {
                    i++;
                }

                processLine(line);
                start = i + 1;
            }
        }

        // Remove processed part from buffer
        if (start > 0) {
            buffer.delete(0, start);
        }
    }

    /**
     * 结束流式输入. 处理缓冲区中剩余的文本，并关闭所有打开的块。
     */
    public void close() {
        if (buffer.length() > 0) {
            processLine(buffer.toString());
            buffer.setLength(0);
        }
        state.finalizeBlock(doc, lineNumber);
    }

    private void processLine(String line) {
        String expanded = expandTabs(line);
        state.processLine(doc, expanded, line, lineNumber++);
    }

    private void onBlockFinalized(Node node) {
        // Try to extract Link Reference Definitions
        if (node instanceof Paragraph) {
            Paragraph p = (Paragraph) node;
            Node firstChild = p.getFirstChild();
            if (firstChild instanceof Text) {
                Text textNode = (Text) firstChild;
                String content = textNode.getLiteral();
                if (content != null) {
                    int consumed = MarkdownParser.parseLinkReferenceDefinitions(content, doc);
                    if (consumed > 0) {
                        if (consumed >= content.length()) {
                            // Fully consumed, remove the paragraph content
                            // We cannot unlink the node itself because it's already in the renderer's
                            // stack/tree
                            // But we can clear its children so it renders as empty?
                            // Or we should update the text node?
                            textNode.setLiteral("");
                            // If we empty the text node, traverseAndParseInlines will see empty text.
                            // But the paragraph node still exists.
                            // Renderer.renderNode(p) will be called.
                            // If p has empty text child, it might render empty block.

                            // Better: unlink the text node?
                            textNode.unlink();
                        } else {
                            String remaining = content.substring(consumed);
                            if (remaining.trim().isEmpty()) {
                                textNode.unlink();
                            } else {
                                textNode.setLiteral(remaining.trim());
                            }
                        }
                    }
                }
            }
        }

        // Recursive inline parsing for complex blocks like Table
        traverseAndParseInlines(node);

        if (renderer != null) {
            renderer.renderNode(node);
        }
    }

    private void onBlockStarted(Node node) {
        if (renderer != null) {
            renderer.openBlock(node);
        }
    }

    private void onBlockClosed(Node node) {
        if (renderer != null) {
            renderer.closeBlock(node);
        }
    }

    private void traverseAndParseInlines(Node node) {
        if (node instanceof Paragraph || node instanceof Heading || node instanceof TableCell) {
            MarkdownParser.processInlineContainerStatic(doc, node, options, inlineParserFactories);
        }

        Node child = node.getFirstChild();
        while (child != null) {
            traverseAndParseInlines(child);
            child = child.getNext();
        }
    }

    private String expandTabs(String s) {
        if (s.indexOf('\t') == -1) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        int col = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\t') {
                int toAdd = 4 - (col % 4);
                for (int j = 0; j < toAdd; j++) {
                    sb.append(' ');
                }
                col += toAdd;
            } else {
                sb.append(c);
                col++;
            }
        }
        return sb.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MarkdownParserOptions options = new MarkdownParserOptions();
        private List<BlockParserFactory> blockParserFactories = new ArrayList<>();
        private List<InlineContentParserFactory> inlineParserFactories = new ArrayList<>();
        private StreamMarkdownRenderer renderer;

        public Builder options(MarkdownParserOptions options) {
            this.options = options;
            return this;
        }

        public Builder blockParserFactory(BlockParserFactory factory) {
            this.blockParserFactories.add(factory);
            return this;
        }

        public Builder inlineParserFactory(InlineContentParserFactory factory) {
            this.inlineParserFactories.add(factory);
            return this;
        }

        public Builder renderer(StreamMarkdownRenderer renderer) {
            this.renderer = renderer;
            return this;
        }

        public StreamMarkdownParser build() {
            return new StreamMarkdownParser(this);
        }
    }
}
