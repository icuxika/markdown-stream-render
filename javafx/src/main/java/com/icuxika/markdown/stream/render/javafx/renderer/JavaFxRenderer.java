package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.BlockQuote;
import com.icuxika.markdown.stream.render.core.ast.BulletList;
import com.icuxika.markdown.stream.render.core.ast.Code;
import com.icuxika.markdown.stream.render.core.ast.CodeBlock;
import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.ast.Emphasis;
import com.icuxika.markdown.stream.render.core.ast.HardBreak;
import com.icuxika.markdown.stream.render.core.ast.Heading;
import com.icuxika.markdown.stream.render.core.ast.HtmlBlock;
import com.icuxika.markdown.stream.render.core.ast.HtmlInline;
import com.icuxika.markdown.stream.render.core.ast.Image;
import com.icuxika.markdown.stream.render.core.ast.Link;
import com.icuxika.markdown.stream.render.core.ast.ListItem;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.OrderedList;
import com.icuxika.markdown.stream.render.core.ast.Paragraph;
import com.icuxika.markdown.stream.render.core.ast.SoftBreak;
import com.icuxika.markdown.stream.render.core.ast.Strikethrough;
import com.icuxika.markdown.stream.render.core.ast.StrongEmphasis;
import com.icuxika.markdown.stream.render.core.ast.Table;
import com.icuxika.markdown.stream.render.core.ast.TableBody;
import com.icuxika.markdown.stream.render.core.ast.TableCell;
import com.icuxika.markdown.stream.render.core.ast.TableHead;
import com.icuxika.markdown.stream.render.core.ast.TableRow;
import com.icuxika.markdown.stream.render.core.ast.Text;
import com.icuxika.markdown.stream.render.core.ast.ThematicBreak;
import com.icuxika.markdown.stream.render.core.renderer.IMarkdownRenderer;
import com.icuxika.markdown.stream.render.javafx.extension.admonition.AdmonitionJavaFxRenderer;
import com.icuxika.markdown.stream.render.javafx.extension.math.MathJavaFxRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Consumer;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * JavaFX 渲染器实现.
 * <p>
 * 将 Markdown AST 渲染为 JavaFX 节点树（通常以 {@link VBox} 为根）。 支持 CSS 样式定制和节点渲染扩展。
 * </p>
 * <p>
 * 默认会自动加载核心渲染器以及 Admonition 和 Math 扩展渲染器。 样式文件位于 {@code /com/icuxika/markdown/stream/render/javafx/css/} 目录下。
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

    /**
     * Create a new renderer.
     */
    public JavaFxRenderer() {
        this(new Builder());
    }

    /**
     * Create a new renderer with builder.
     *
     * @param builder
     *            builder
     */
    public JavaFxRenderer(Builder builder) {
        blockStack.push(root);
        root.setSpacing(10);
        root.getStyleClass().add("markdown-root");

        // Load default stylesheet
        // root.getStylesheets().add(getClass().getResource("/com/icuxika/markdown/stream/render/javafx/css/markdown.css").toExternalForm());
        // REMOVED: Do not add default stylesheet to root directly.
        // Let the Scene or parent container manage the theme/stylesheets.
        // This allows switching themes at the Scene level to cascade down correctly.

        // However, we DO need the base structure/layout styles if they are not
        // theme-dependent?
        // Actually markdown.css contains structure AND default theme variables.
        // If we remove it here, the user MUST add it to the Scene.
        // For standalone usage, this might break things if user forgets.
        // But for theming support, adding it here makes it hard to override or remove
        // cleanly.
        // Compromise: Add it, but ensure MarkdownTheme can override/remove it?
        // MarkdownTheme manages Scene stylesheets. If root has its own stylesheet, it
        // takes precedence or merges?
        // JavaFX: User Agent Stylesheet < Scene Stylesheet < Parent Stylesheet < Node
        // Stylesheet.
        // Node stylesheet (root.getStylesheets()) has highest priority!
        // So if we add "markdown.css" (which sets light defaults) here,
        // adding "dark.css" to Scene will be overridden by this Node stylesheet for
        // variables defined in .root

        // SOLUTION: Do NOT add stylesheets here. Rely on user/MarkdownTheme to add them
        // to Scene or Parent.

        // Load extension stylesheets (Admonition, Math)
        // These define structural styles mostly, but also colors.
        // Ideally these should also be managed by theme manager or added to Scene.
        // For now, let's keep them but be aware they might need theming support too.
        java.net.URL admCss = getClass()
                .getResource("/com/icuxika/markdown/stream/render/javafx/css/extensions/admonition.css");
        if (admCss != null) {
            root.getStylesheets().add(admCss.toExternalForm());
        }

        java.net.URL mathCss = getClass()
                .getResource("/com/icuxika/markdown/stream/render/javafx/css/extensions/math.css");
        if (mathCss != null) {
            root.getStylesheets().add(mathCss.toExternalForm());
        }

        // Add core renderer
        List<JavaFxNodeRendererFactory> allFactories = new ArrayList<>();
        allFactories.add(context -> new CoreJavaFxNodeRenderer(context, link -> {
            if (onLinkClick != null) {
                onLinkClick.accept(link);
            }
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

    /**
     * Create a new builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for JavaFxRenderer.
     */
    public static class Builder {
        private List<JavaFxNodeRendererFactory> nodeRendererFactories = new ArrayList<>();

        /**
         * Add a node renderer factory.
         *
         * @param factory
         *            factory
         * @return this
         */
        public Builder nodeRendererFactory(JavaFxNodeRendererFactory factory) {
            this.nodeRendererFactories.add(factory);
            return this;
        }

        /**
         * Build the renderer.
         *
         * @return the renderer
         */
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
        // Also update CoreJavaFxNodeRenderer if it was already created.
        // But CoreJavaFxNodeRenderer is inside nodeRenderers list.
        // We need to find it and update its callback?
        // Actually, CoreJavaFxNodeRenderer is created with a lambda that captures
        // 'this.onLinkClick'.
        // Wait, line 63: "if (onLinkClick != null) onLinkClick.accept(link);"
        // This lambda captures 'this' (JavaFxRenderer instance) and accesses
        // 'this.onLinkClick'.
        // So updating 'this.onLinkClick' field is sufficient!
        // The lambda will look up the current value of the field when invoked.
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

    public void render(Node node) {
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
