package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.Node;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

/**
 * A ListCell implementation for rendering Markdown Nodes.
 * <p>
 * This cell uses a reused {@link JavaFxRenderer} to render the Markdown Node. It ensures that the styles are applied
 * correctly.
 * </p>
 */
public class MarkdownListCell extends ListCell<Node> {

    private final JavaFxRenderer renderer;

    /**
     * Constructor.
     */
    public MarkdownListCell() {
        // Create a renderer with default configuration
        this.renderer = new JavaFxRenderer();

        // Ensure the root VBox has the markdown-root style class
        // (It is added by default in JavaFxRenderer constructor)

        // We can bind properties or listeners here if needed

        // Remove default ListCell padding/background if needed to let Markdown styles take over
        // setPadding(javafx.geometry.Insets.EMPTY);
        // setBackground(javafx.scene.layout.Background.EMPTY);
        getStyleClass().add("markdown-list-cell");
    }

    @Override
    protected void updateItem(Node item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            // Clear previous content
            renderer.clear();

            // Render the new item
            // Note: render(Node) will visit the node.
            // If the node is a block (Paragraph), it will be rendered into the root VBox.
            renderer.render(item);

            // Set the result as graphic
            VBox result = (VBox) renderer.getResult();
            setGraphic(result);

            // Ensure width constraint
            result.prefWidthProperty().bind(getListView().widthProperty().subtract(20)); // Subtract padding
        }
    }
}
