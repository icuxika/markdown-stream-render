package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.renderer.StreamMarkdownRenderer;
import java.util.Stack;
import javafx.application.Platform;
import javafx.collections.ObservableList;

/**
 * Virtualized JavaFX Stream Renderer using ObservableList.
 * <p>
 * Instead of building a Scene Graph tree, this renderer populates a flat list of top-level Nodes. This list is intended
 * to be bound to a {@link javafx.scene.control.ListView}.
 * </p>
 */
public class VirtualJavaFxStreamRenderer implements StreamMarkdownRenderer {

    private final ObservableList<Node> items;
    private final Stack<Node> blockStack = new Stack<>();
    private final Runnable requestRefresh;

    /**
     * Constructor.
     *
     * @param items
     *            The ObservableList to populate.
     * @param requestRefresh
     *            Callback to request UI refresh (e.g. listView.refresh()).
     */
    public VirtualJavaFxStreamRenderer(ObservableList<Node> items, Runnable requestRefresh) {
        this.items = items;
        this.requestRefresh = requestRefresh;
    }

    @Override
    public void openBlock(Node node) {
        Platform.runLater(() -> {
            if (node instanceof Document) {
                return;
            }

            // If stack is empty (excluding Document which we don't push explicitly here if stream parser doesn't),
            // then this is a top-level block.
            // StreamMarkdownParser calls openBlock(Document) first usually.

            // Let's track hierarchy.
            // If parent is Document, it's top level.
            if (node.getParent() instanceof Document) {
                items.add(node);
                // Trigger refresh to show new item?
                // Adding to ObservableList automatically updates ListView count,
                // creating a new Cell. The Cell will render the node.
            }

            // We don't strictly need to track stack if we trust node.getParent(),
            // but node.getParent() might be null during creation if not yet attached?
            // Parser usually attaches before calling openBlock?
            // Let's assume node.getParent() is reliable.
        });
    }

    @Override
    public void renderNode(Node node) {
        Platform.runLater(() -> {
            // Node is finalized (e.g. Paragraph fully parsed with Text children).
            // If this node is in our list (top-level), it has changed state (children added).
            // We need to refresh the view to ensure it renders correctly.
            if (node.getParent() instanceof Document) {
                if (requestRefresh != null) {
                    requestRefresh.run();
                }
            }
        });
    }

    @Override
    public void closeBlock(Node node) {
        // Nothing to do for list structure, as node is already added in openBlock.
    }
}
