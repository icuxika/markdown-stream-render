package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParser;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParserFactory;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineParserState;
import com.icuxika.markdown.stream.render.core.parser.inline.ParsedInline;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import com.icuxika.markdown.stream.render.core.renderer.HtmlWriter;
import com.icuxika.markdown.stream.render.core.renderer.NodeRenderer;
import com.icuxika.markdown.stream.render.core.renderer.NodeRendererContext;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRendererContext;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import java.util.Collections;
import java.util.Set;

/**
 * Demonstrates how to add a custom Inline Parser (Math).
 * Syntax: $E=mc^2$
 */
public class InlinePluginDemo {

    public static void main(String[] args) {
        String markdown = "Here is an inline math equation: $E=mc^2$. And another one: $a^2 + b^2 = c^2$.";

        System.out.println("--- Markdown ---");
        System.out.println(markdown);
        System.out.println("----------------");

        MarkdownParser parser = MarkdownParser.builder()
                .inlineParserFactory(new MathParserFactory())
                .build();

        // 1. HTML Rendering
        HtmlRenderer htmlRenderer = HtmlRenderer.builder()
                .nodeRendererFactory(context -> new MathRenderer(context))
                .build();

        com.icuxika.markdown.stream.render.core.ast.Document doc = parser.parse(markdown);
        doc.accept(htmlRenderer);
        System.out.println("--- HTML Output ---");
        System.out.println(htmlRenderer.getResult());

        // 2. JavaFX Rendering (Code Example)
        System.out.println("--- JavaFX Output ---");
        System.out.println("See VisualPluginDemo.java for JavaFX rendering result.");
    }

    // ... (AST Node, Math Parser, HTML Renderer remain the same)
    // --- AST Node ---
    public static class MathNode extends Node {
        private String content;

        public MathNode(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        @Override
        public void accept(com.icuxika.markdown.stream.render.core.ast.Visitor visitor) {
            // Visitor doesn't know about MathNode, so we can't call visit(this).
            // We could add a generic visit(Node) to Visitor or handle it here?
            // Actually, for custom nodes, the standard Visitor pattern is tricky unless we extend Visitor.
            // But since our renderer uses NodeRendererMap, accept() is mainly for traversal.
            // If we don't need traversal for MathNode (it has no children), we can leave it empty.
        }
    }

    // --- Inline Parser ---
    public static class MathParser implements InlineContentParser {

        @Override
        public ParsedInline tryParse(String input, int index, InlineParserState state) {
            // Check for opening '$'
            // The factory triggers on '$', so we know input[index] is '$'

            // Look for closing '$'
            int start = index + 1;
            int i = start;
            while (i < input.length()) {
                char c = input.charAt(i);
                if (c == '$') {
                    // Found closer
                    String content = input.substring(start, i);
                    if (content.isEmpty()) return ParsedInline.none(); // $$ is empty or handled as Text?

                    return ParsedInline.of(new MathNode(content), i + 1);
                }
                if (c == '\n') {
                    // Math shouldn't span lines usually in this simple syntax
                    // But maybe yes? Let's say no for simple inline math.
                    break;
                }
                i++;
            }

            return ParsedInline.none();
        }

    }

    // --- Inline Parser Factory ---
    public static class MathParserFactory implements InlineContentParserFactory {
        @Override
        public Set<Character> getTriggerCharacters() {
            return Collections.singleton('$');
        }

        @Override
        public InlineContentParser create() {
            return new MathParser();
        }
    }

    // --- HTML Renderer ---
    public static class MathRenderer implements NodeRenderer {
        private final HtmlWriter html;

        public MathRenderer(NodeRendererContext context) {
            this.html = context.getWriter();
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Collections.singleton(MathNode.class);
        }

        @Override
        public void render(Node node) {
            MathNode math = (MathNode) node;
            html.tag("span", Collections.singletonMap("class", "math"));
            html.text("\\(" + math.getContent() + "\\)"); // LaTeX style
            html.closeTag("span");
        }
    }

    // --- JavaFX Renderer ---
    public static class MathJavaFxRenderer implements JavaFxNodeRenderer {
        private final JavaFxNodeRendererContext context;

        public MathJavaFxRenderer(JavaFxNodeRendererContext context) {
            this.context = context;
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Collections.singleton(MathNode.class);
        }

        @Override
        public void render(Node node) {
            MathNode math = (MathNode) node;

            // In a real app, you might use a WebView or specific Math library.
            // Here we just style it to look like math.
            Label label = new Label(math.getContent());
            label.setFont(Font.font("Times New Roman", FontPosture.ITALIC, 16));
            label.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 0 4;");

            context.getCurrentContainer().getChildren().add(label);
        }
    }
}
