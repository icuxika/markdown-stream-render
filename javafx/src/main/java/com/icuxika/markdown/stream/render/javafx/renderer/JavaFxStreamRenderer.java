package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.BlockQuote;
import com.icuxika.markdown.stream.render.core.ast.BulletList;
import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.ast.Heading;
import com.icuxika.markdown.stream.render.core.ast.ListItem;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.OrderedList;
import com.icuxika.markdown.stream.render.core.extension.admonition.AdmonitionBlock;
import com.icuxika.markdown.stream.render.core.renderer.StreamMarkdownTypingRenderer;
import java.util.Stack;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * JavaFX 流式渲染器.
 * <p>
 * 将接收到的 AST 节点实时转换为 JavaFX 节点并追加到 UI 中。
 * </p>
 */
public class JavaFxStreamRenderer implements StreamMarkdownTypingRenderer {

    private final VBox root;
    private final JavaFxRenderer internalRenderer;
    private final Stack<Pane> containerStack = new Stack<>();
    private java.util.function.Consumer<String> onLinkClick;
    private volatile Node latestPreviewNode;
    private final java.util.concurrent.atomic.AtomicBoolean isPreviewDirty = new java.util.concurrent.atomic.AtomicBoolean(
            false);
    private final VBox previewHolder = new VBox();
    private Pane previewHolderParent;

    /**
     * Constructor.
     *
     * @param root
     *            root VBox
     */
    public JavaFxStreamRenderer(VBox root) {
        this.root = root;
        this.internalRenderer = new JavaFxRenderer();
        // initStyles(); // Do NOT force load styles on root if using theme manager
    }

    /**
     * Constructor with builder.
     *
     * @param root
     *            root VBox
     * @param builder
     *            builder
     */
    public JavaFxStreamRenderer(VBox root, JavaFxRenderer.Builder builder) {
        this.root = root;
        this.internalRenderer = builder.build();
        // initStyles(); // Do NOT force load styles on root if using theme manager
    }

    /**
     * Set link click handler.
     *
     * @param onLinkClick
     *            handler
     */
    public void setOnLinkClick(java.util.function.Consumer<String> onLinkClick) {
        this.onLinkClick = onLinkClick;
        // Also update internal renderer to use this handler for inline links rendered
        // later
        internalRenderer.setOnLinkClick(onLinkClick);
    }

    private void initStyles() {
        // Copy stylesheets from internal renderer
        // Internal renderer no longer adds default stylesheet by default (if modified).
        // But extensions might be added.
        for (String sheet : this.internalRenderer.getRoot().getStylesheets()) {
            if (!root.getStylesheets().contains(sheet)) {
                root.getStylesheets().add(sheet);
            }
        }

        if (!root.getStyleClass().contains("markdown-root")) {
            root.getStyleClass().add("markdown-root");
        }

        // REMOVED: Do not hardcode markdown.css here.
        /*
         * java.net.URL cssUrl = getClass().getResource( "/com/icuxika/markdown/stream/render/javafx/css/markdown.css");
         * if (cssUrl != null) { String cssPath = cssUrl.toExternalForm(); if (!root.getStylesheets().contains(cssPath))
         * { root.getStylesheets().add(cssPath); } }
         */
    }

    // Batching queue
    private final java.util.concurrent.ConcurrentLinkedQueue<Runnable> pendingUpdates = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final java.util.concurrent.atomic.AtomicBoolean isUpdateScheduled = new java.util.concurrent.atomic.AtomicBoolean(
            false);
    // Batch interval in milliseconds (e.g. 16ms for ~60fps)
    private static final long BATCH_INTERVAL_MS = 16;

    @Override
    public void renderNode(Node node) {
        pendingUpdates.offer(() -> {
            clearPreviewOnFxThread();
            latestPreviewNode = null;
            isPreviewDirty.set(false);
            renderNodeOnFxThread(node);
        });
        scheduleUpdate();
    }

    @Override
    public void openBlock(Node node) {
        pendingUpdates.offer(() -> {
            clearPreviewOnFxThread();
            openBlockOnFxThread(node);
        });
        scheduleUpdate();
    }

    @Override
    public void closeBlock(Node node) {
        pendingUpdates.offer(() -> {
            clearPreviewOnFxThread();
            closeBlockOnFxThread(node);
        });
        scheduleUpdate();
    }

    @Override
    public void renderPreviewNode(Node node) {
        latestPreviewNode = node;
        isPreviewDirty.set(true);
        scheduleUpdate();
    }

    @Override
    public void clearPreview() {
        latestPreviewNode = null;
        isPreviewDirty.set(true);
        scheduleUpdate();
    }

    private void scheduleUpdate() {
        if (isUpdateScheduled.compareAndSet(false, true)) {
            // Schedule batch processing
            // Use a Timer or just Platform.runLater?
            // If we use Platform.runLater directly, it might still flood the event queue if
            // we call it too often.
            // But here we only call it ONCE until it runs.
            // So this is effectively debouncing/coalescing updates into the next FX pulse.
            // This is usually sufficient and better than a fixed timer.
            Platform.runLater(this::processBatch);
        }
    }

    private void processBatch() {
        isUpdateScheduled.set(false);
        // Process all pending updates in one go
        Runnable task;
        // Limit processing time? For now, process all.
        while ((task = pendingUpdates.poll()) != null) {
            task.run();
        }
        if (isPreviewDirty.getAndSet(false)) {
            Node node = latestPreviewNode;
            if (node == null) {
                clearPreviewOnFxThread();
            } else {
                renderPreviewNodeOnFxThread(node);
            }
        }
        root.requestLayout();
    }

    // TOC Support
    private java.util.function.Consumer<Heading> onHeadingRendered;

    /**
     * Set heading rendered handler.
     *
     * @param onHeadingRendered
     *            handler
     */
    public void setOnHeadingRendered(java.util.function.Consumer<Heading> onHeadingRendered) {
        this.onHeadingRendered = onHeadingRendered;
    }

    private void renderNodeOnFxThread(Node node) {
        // Prevent duplication for container blocks that are already handled in openBlock
        if (node instanceof Document || node instanceof BlockQuote || node instanceof BulletList
                || node instanceof OrderedList || node instanceof ListItem || node instanceof AdmonitionBlock) {
            return;
        }

        if (containerStack.isEmpty()) {
            // Should not happen if openBlock(Document) works, but fallback to root
            containerStack.push(root);
        }

        VBox tempContainer = new VBox();

        internalRenderer.pushContainer(tempContainer);
        try {
            internalRenderer.render(node);
        } finally {
            internalRenderer.popContainer();
        }

        // Notify TOC if Heading
        if (node instanceof Heading && onHeadingRendered != null) {
            onHeadingRendered.accept((Heading) node);

            // Map the rendered node (inside tempContainer) to the Heading ID for scrolling?
            // internalRenderer.render(node) creates a TextFlow and adds it to
            // tempContainer.
            // We need to access that TextFlow to use it as a scroll target.
            if (!tempContainer.getChildren().isEmpty()) {
                javafx.scene.Node renderedNode = tempContainer.getChildren().get(0);
                renderedNode.setUserData(((Heading) node).getAnchorId());
                // Also store it in a map if needed, but userData is convenient for lookup
            }
        }

        Pane parent = containerStack.peek();
        parent.getChildren().addAll(tempContainer.getChildren());
    }

    private void renderPreviewNodeOnFxThread(Node node) {
        if (containerStack.isEmpty()) {
            containerStack.push(root);
        }

        Pane parent = containerStack.peek();
        if (previewHolderParent != parent) {
            detachPreviewHolder();
            previewHolderParent = parent;
            previewHolder.getChildren().clear();
            parent.getChildren().add(previewHolder);
        }
        java.util.List<javafx.scene.Node> nodes = renderToFxNodes(node);
        previewHolder.getChildren().setAll(nodes);
        parent.requestLayout();
    }

    private java.util.List<javafx.scene.Node> renderToFxNodes(Node node) {
        VBox tempContainer = new VBox();
        internalRenderer.pushContainer(tempContainer);
        try {
            internalRenderer.render(node);
        } finally {
            internalRenderer.popContainer();
        }
        return java.util.List.copyOf(tempContainer.getChildren());
    }

    private void clearPreviewOnFxThread() {
        if (previewHolderParent == null) {
            previewHolder.getChildren().clear();
            return;
        }
        detachPreviewHolder();
    }

    private void detachPreviewHolder() {
        if (previewHolderParent != null) {
            previewHolder.getChildren().clear();
            previewHolderParent.getChildren().remove(previewHolder);
            previewHolderParent.requestLayout();
            previewHolderParent = null;
        }
    }

    private void openBlockOnFxThread(Node node) {
        if (node instanceof Document) {
            containerStack.push(root);
            return;
        }

        if (containerStack.isEmpty()) {
            containerStack.push(root);
        }

        Pane parent = containerStack.peek();
        Pane newContainer = null;
        Pane contentContainer = null; // Container for children, if different from newContainer

        if (node instanceof BlockQuote) {
            VBox quoteBox = new VBox();
            quoteBox.getStyleClass().add("markdown-quote");
            newContainer = quoteBox;
            contentContainer = quoteBox;
        } else if (node instanceof BulletList || node instanceof OrderedList) {
            VBox listBox = new VBox();
            listBox.getStyleClass().add("markdown-list");
            listBox.setSpacing(5);
            newContainer = listBox;
            contentContainer = listBox;
        } else if (node instanceof ListItem) {
            HBox itemBox = new HBox();
            itemBox.setSpacing(5);
            itemBox.getStyleClass().add("markdown-list-item");

            javafx.scene.Node marker = createListMarker((ListItem) node);
            if (marker != null) {
                itemBox.getChildren().add(marker);
            }

            VBox contentBox = new VBox();
            contentBox.getStyleClass().add("markdown-list-content");
            HBox.setHgrow(contentBox, Priority.ALWAYS);
            itemBox.getChildren().add(contentBox);

            newContainer = itemBox;
            contentContainer = contentBox;
        } else if (node instanceof AdmonitionBlock) {
            AdmonitionBlock ab = (AdmonitionBlock) node;
            VBox admonitionBox = new VBox();
            admonitionBox.getStyleClass().addAll("admonition", "admonition-" + ab.getType().toLowerCase());

            if (ab.getTitle() != null && !ab.getTitle().isEmpty()) {
                Label titleLabel = new Label(ab.getTitle());
                titleLabel.getStyleClass().add("admonition-title");
                admonitionBox.getChildren().add(titleLabel);
            }

            VBox contentBox = new VBox();
            contentBox.getStyleClass().add("admonition-content");
            admonitionBox.getChildren().add(contentBox);

            newContainer = admonitionBox;
            contentContainer = contentBox;
        }

        if (newContainer != null) {
            parent.getChildren().add(newContainer);
            containerStack.push(contentContainer);
        } else {
            // Leaf blocks or non-container blocks (Paragraph, CodeBlock, Heading, Table, HtmlBlock)
            // do not receive closeBlock from MarkdownParser (they are not added to openContainers stack),
            // so we must NOT push to containerStack to avoid imbalance (popping the parent prematurely or never
            // popping).
        }
    }

    private void closeBlockOnFxThread(Node node) {
        if (!containerStack.isEmpty()) {
            containerStack.pop();
        }
    }

    @SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
    private javafx.scene.Node createListMarker(ListItem listItem) {
        Node list = listItem.getParent();
        if (list instanceof OrderedList) {
            OrderedList ol = (OrderedList) list;
            int index = ol.getStartNumber();
            Node prev = listItem.getPrevious();
            while (prev != null) {
                index++;
                prev = prev.getPrevious();
            }

            String markerText = index + String.valueOf(ol.getDelimiter());
            Label markerLabel = new Label(markerText);
            markerLabel.getStyleClass().add("markdown-list-marker");
            markerLabel.setMinWidth(20);
            markerLabel.setAlignment(Pos.TOP_RIGHT);
            return markerLabel;
        } else if (list instanceof BulletList) {
            if (listItem.isTask()) {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(listItem.isChecked());
                checkBox.setDisable(true);
                checkBox.getStyleClass().add("markdown-task-checkbox");
                return checkBox;
            } else {
                String markerText = "•";
                Label markerLabel = new Label(markerText);
                markerLabel.getStyleClass().add("markdown-list-marker");
                markerLabel.setMinWidth(20);
                markerLabel.setAlignment(Pos.TOP_RIGHT);
                return markerLabel;
            }
        }
        return null;
    }
}
