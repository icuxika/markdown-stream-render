package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.*;
import com.icuxika.markdown.stream.render.core.renderer.IMarkdownRenderer;
import com.icuxika.markdown.stream.render.javafx.extension.admonition.AdmonitionJavaFxRenderer;
import com.icuxika.markdown.stream.render.javafx.extension.math.MathJavaFxRenderer;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.function.Consumer;

/**
 * JavaFX 渲染器实现。
 * <p>
 * 将 Markdown AST 渲染为 JavaFX 节点树（通常以 VBox 为根）。
 * 支持 CSS 样式定制和节点渲染扩展。
 * </p>
 */
public class JavaFxRenderer implements IMarkdownRenderer, JavaFxNodeRendererContext {
    private final VBox root = new VBox();
    private final Stack<Pane> blockStack = new Stack<>();

    // Map start line number to JavaFX Node
    private final TreeMap<Integer, javafx.scene.Node> lineToNodeMap = new TreeMap<>();

    // Link handling callback
    private Consumer<String> onLinkClick;

    // Renderer Registry
    private final List<JavaFxNodeRenderer> nodeRenderers = new ArrayList<>();
    private final Map<Class<? extends Node>, JavaFxNodeRenderer> rendererMap = new HashMap<>();

    public JavaFxRenderer() {
        this(new Builder());
    }

    public JavaFxRenderer(Builder builder) {
        blockStack.push(root);
        root.setSpacing(10);
        root.getStyleClass().add("markdown-root");

        // Load default stylesheet
        java.net.URL cssUrl = getClass().getResource("/com/icuxika/markdown/stream/render/javafx/css/markdown.css");
        if (cssUrl != null) {
            root.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Load extension stylesheets
        java.net.URL admCss = getClass().getResource("/com/icuxika/markdown/stream/render/javafx/css/extensions/admonition.css");
        if (admCss != null) root.getStylesheets().add(admCss.toExternalForm());

        java.net.URL mathCss = getClass().getResource("/com/icuxika/markdown/stream/render/javafx/css/extensions/math.css");
        if (mathCss != null) root.getStylesheets().add(mathCss.toExternalForm());

        // Add core renderer
        List<JavaFxNodeRendererFactory> allFactories = new ArrayList<>();
        allFactories.add(context -> new CoreJavaFxNodeRenderer(context, link -> {
            if (onLinkClick != null) onLinkClick.accept(link);
        }));

        // Add default extension renderers
        allFactories.add(context -> new AdmonitionJavaFxRenderer(context));
        allFactories.add(context -> new MathJavaFxRenderer(context));
        System.out.println("[INFO] JavaFxRenderer: Loaded default extensions (Admonition, Math)");

        allFactories.addAll(builder.nodeRendererFactories);

        // Initialize renderers
        for (JavaFxNodeRendererFactory factory : allFactories) {
            JavaFxNodeRenderer renderer = factory.create(this);
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
        private List<JavaFxNodeRendererFactory> nodeRendererFactories = new ArrayList<>();

        public Builder nodeRendererFactory(JavaFxNodeRendererFactory factory) {
            this.nodeRendererFactories.add(factory);
            return this;
        }

        public JavaFxRenderer build() {
            return new JavaFxRenderer(this);
        }
    }

    @Override
    public Object getResult() {
        return root;
    }

    public TreeMap<Integer, javafx.scene.Node> getLineToNodeMap() {
        return lineToNodeMap;
    }

    public void setOnLinkClick(Consumer<String> onLinkClick) {
        this.onLinkClick = onLinkClick;
    }

    // --- JavaFxNodeRendererContext Implementation ---

    @Override
    public void renderChildren(Node parent) {
        Node child = parent.getFirstChild();
        while (child != null) {
            Node next = child.getNext();
            render(child);
            child = next;
        }
    }

    void render(Node node) {
        JavaFxNodeRenderer renderer = rendererMap.get(node.getClass());
        if (renderer != null) {
            renderer.render(node);
        } else {
            renderChildren(node);
        }
    }

    @Override
    public Pane getCurrentContainer() {
        return blockStack.peek();
    }

    @Override
    public void pushContainer(Pane pane) {
        blockStack.push(pane);
    }

    @Override
    public Pane popContainer() {
        return blockStack.pop();
    }

    @Override
    public void registerNode(Node astNode, javafx.scene.Node fxNode) {
        if (astNode.getStartLine() >= 0) {
            lineToNodeMap.put(astNode.getStartLine(), fxNode);
            fxNode.setUserData(astNode);
        }
    }

    @Override
    public VBox getRoot() {
        return root;
    }

    // --- Visitor Implementation (Delegates to Renderer Map) ---

    @Override
    public void visit(Document document) {
        render(document);
    }

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
}
