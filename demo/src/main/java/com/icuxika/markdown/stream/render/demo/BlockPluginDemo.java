package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.ast.Block;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.Visitor;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.block.*;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import com.icuxika.markdown.stream.render.core.renderer.HtmlWriter;
import com.icuxika.markdown.stream.render.core.renderer.NodeRenderer;
import com.icuxika.markdown.stream.render.core.renderer.NodeRendererContext;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRendererContext;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Demonstrates how to add a custom Block Parser (Admonition).
 * Syntax:
 * !!! type "Title"
 * Content
 * !!!
 */
public class BlockPluginDemo {

    public static void main(String[] args) {
        String markdown =
                "Here is an admonition:\n" +
                        "\n" +
                        "!!! info \"Note Title\"\n" +
                        "    This is the content of the admonition.\n" +
                        "    It can contain **bold** text.\n" +
                        "\n" +
                        "End of document.";

        System.out.println("--- Markdown ---");
        System.out.println(markdown);
        System.out.println("----------------");

        MarkdownParser parser = MarkdownParser.builder()
                .blockParserFactory(new AdmonitionBlockParserFactory())
                .build();

        // 1. HTML Rendering
        HtmlRenderer htmlRenderer = HtmlRenderer.builder()
                .nodeRendererFactory(context -> new AdmonitionRenderer(context))
                .build();

        com.icuxika.markdown.stream.render.core.ast.Document doc = parser.parse(markdown);
        doc.accept(htmlRenderer);
        System.out.println("--- HTML Output ---");
        System.out.println(htmlRenderer.getResult());

        // 2. JavaFX Rendering (Code Example)
        // Since we cannot launch a JavaFX application easily in this console demo without blocking,
        // we present the renderer class below.
        // See VisualPluginDemo for the running JavaFX application.
        System.out.println("--- JavaFX Output ---");
        System.out.println("See VisualPluginDemo.java for JavaFX rendering result.");
    }

    // --- AST Node ---
    public static class AdmonitionBlock extends Block {
        private String type;
        private String title;

        public AdmonitionBlock(String type, String title) {
            this.type = type;
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public void accept(Visitor visitor) {
            // Standard Visitor doesn't support custom nodes.
            // Our Renderer architecture uses NodeRendererMap instead of Visitor traversal for children.
        }
    }

    // --- Block Parser ---
    public static class AdmonitionParser extends AbstractBlockParser {

        private final AdmonitionBlock block;

        public AdmonitionParser(String type, String title) {
            this.block = new AdmonitionBlock(type, title);
        }

        @Override
        public Block getBlock() {
            return block;
        }

        @Override
        public boolean isContainer() {
            return true;
        }

        @Override
        public BlockContinue tryContinue(ParserState state) {
            // Simple check: content must be indented by 4 spaces
            // OR if it's a blank line, it continues?
            // Let's implement Python-Markdown style: indented content.

            if (state.isBlank()) {
                return BlockContinue.atIndex(state.getNextNonSpaceIndex());
            }

            int indent = state.getIndent();
            if (indent >= 4) {
                return BlockContinue.atIndex(state.getIndex() + 4);
            }

            return BlockContinue.none();
        }
    }

    // --- Block Parser Factory ---
    public static class AdmonitionBlockParserFactory implements BlockParserFactory {
        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            CharSequence line = state.getLine();
            int nextNonSpace = state.getNextNonSpaceIndex();
            // Check for "!!!"
            // line.subSequence(nextNonSpace, line.length())

            // Regex check
            String currentLine = line.toString().substring(nextNonSpace);
            Pattern pattern = Pattern.compile("^!!!\\s+(\\w+)(?:\\s+\"(.*)\")?$");
            Matcher matcher = pattern.matcher(currentLine);

            if (matcher.matches()) {
                String type = matcher.group(1);
                String title = matcher.group(2);
                return BlockStart.of(new AdmonitionParser(type, title))
                        .atIndex(state.getIndex() + currentLine.length()); // Consume whole line
            }

            return BlockStart.none();
        }
    }

    // --- HTML Renderer ---
    public static class AdmonitionRenderer implements NodeRenderer {
        private final HtmlWriter html;
        private final NodeRendererContext context;

        public AdmonitionRenderer(NodeRendererContext context) {
            this.html = context.getWriter();
            this.context = context;
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Collections.singleton(AdmonitionBlock.class);
        }

        @Override
        public void render(Node node) {
            AdmonitionBlock admonition = (AdmonitionBlock) node;
            java.util.Map<String, String> attrs = new java.util.HashMap<>();
            attrs.put("class", "admonition " + admonition.getType());

            html.tag("div", attrs);
            html.line();

            if (admonition.getTitle() != null) {
                html.tag("p", Collections.singletonMap("class", "admonition-title"));
                html.text(admonition.getTitle());
                html.closeTag("p");
                html.line();
            }

            context.renderChildren(node);

            html.closeTag("div");
            html.line();
        }
    }

    // --- JavaFX Renderer ---
    public static class AdmonitionJavaFxRenderer implements JavaFxNodeRenderer {
        private final JavaFxNodeRendererContext context;

        public AdmonitionJavaFxRenderer(JavaFxNodeRendererContext context) {
            this.context = context;
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Collections.singleton(AdmonitionBlock.class);
        }

        @Override
        public void render(Node node) {
            AdmonitionBlock admonition = (AdmonitionBlock) node;

            VBox box = new VBox();
            box.setPadding(new Insets(10));
            box.setStyle("-fx-border-color: #ddd; -fx-border-width: 0 0 0 4; -fx-background-color: #f9f9f9;");

            // Set color based on type
            String color = "#448aff"; // Info blue
            if ("warning".equals(admonition.getType())) color = "#ffc107";
            else if ("error".equals(admonition.getType())) color = "#f44336";

            box.setStyle(box.getStyle() + "-fx-border-left-color: " + color + ";");

            if (admonition.getTitle() != null) {
                Label titleLabel = new Label(admonition.getTitle());
                titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                titleLabel.setTextFill(Color.web(color));
                titleLabel.setPadding(new Insets(0, 0, 5, 0));
                box.getChildren().add(titleLabel);
            }

            context.getCurrentContainer().getChildren().add(box);

            // Render children into this box
            context.pushContainer(box);
            context.renderChildren(node);
            context.popContainer();
        }
    }
}
