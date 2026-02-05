package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.*;
import com.icuxika.markdown.stream.render.core.extension.admonition.AdmonitionBlock;
import com.icuxika.markdown.stream.render.core.renderer.IStreamMarkdownRenderer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Stack;

/**
 * JavaFX 流式渲染器。
 * <p>
 * 将接收到的 AST 节点实时转换为 JavaFX 节点并追加到 UI 中。
 * </p>
 */
public class JavaFxStreamRenderer implements IStreamMarkdownRenderer {

    private final VBox root;
    private final JavaFxRenderer internalRenderer;
    private final Stack<Pane> containerStack = new Stack<>();

    public JavaFxStreamRenderer(VBox root) {
        this.root = root;
        this.internalRenderer = new JavaFxRenderer();
        initStyles();
    }

    public JavaFxStreamRenderer(VBox root, JavaFxRenderer.Builder builder) {
        this.root = root;
        this.internalRenderer = builder.build();
        initStyles();
    }

    private void initStyles() {
        // Copy stylesheets from internal renderer
        for (String sheet : this.internalRenderer.getRoot().getStylesheets()) {
            if (!root.getStylesheets().contains(sheet)) {
                root.getStylesheets().add(sheet);
            }
        }

        if (!root.getStyleClass().contains("markdown-root")) {
            root.getStyleClass().add("markdown-root");
        }

        java.net.URL cssUrl = getClass().getResource("/com/icuxika/markdown/stream/render/javafx/css/markdown.css");
        if (cssUrl != null) {
            String cssPath = cssUrl.toExternalForm();
            if (!root.getStylesheets().contains(cssPath)) {
                root.getStylesheets().add(cssPath);
            }
        }
    }

    @Override
    public void renderNode(Node node) {
        Platform.runLater(() -> renderNodeOnFxThread(node));
    }

    @Override
    public void openBlock(Node node) {
        Platform.runLater(() -> openBlockOnFxThread(node));
    }

    @Override
    public void closeBlock(Node node) {
        Platform.runLater(() -> closeBlockOnFxThread(node));
    }

    private void renderNodeOnFxThread(Node node) {
        if (containerStack.isEmpty()) {
            // Should not happen if openBlock(Document) works, but fallback to root
            containerStack.push(root);
        }

        Pane parent = containerStack.peek();
        VBox tempContainer = new VBox();

        internalRenderer.pushContainer(tempContainer);
        try {
            internalRenderer.render(node);
        } finally {
            internalRenderer.popContainer();
        }

        parent.getChildren().addAll(tempContainer.getChildren());
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
            // For other blocks (like Table), we don't open a container in stream mode
            // because they are rendered atomically in renderNode.
            // But we must push SOMETHING to keep stack balanced if closeBlock is called?
            // MarkdownParser calls onBlockStarted/Closed for Custom Blocks.
            // If we ignore it here, we must ignore it in closeBlock.
            // Let's push a dummy or null? No, that's dangerous.
            // Better: push the parent again? Or don't push anything and track ignored blocks?
            // Actually, Table is NOT treated as container in MarkdownParser (my previous analysis).
            // So onBlockStarted won't be called for Table.
            // It WILL be called for any Custom Block (via BlockParserFactory).
            // If we have a Custom Block we don't recognize, we should probably create a generic container?
            // Or just ignore.
            // If we ignore, we must match closeBlock.
            // Let's assume we only handle known containers.
            // But wait, if I don't push, closeBlock will pop the PARENT!
            // That would be catastrophic.
            // So I MUST push something or track that I ignored it.
            // Easiest: Push parent again (transparent).
            containerStack.push(parent);
        }
    }

    private void closeBlockOnFxThread(Node node) {
        if (!containerStack.isEmpty()) {
            containerStack.pop();
        }
    }

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
                String markerText = "\u2022";
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
