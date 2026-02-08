package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public interface JavaFxNodeRendererContext {

	/**
	 * Render children of the given node.
	 */
	void renderChildren(Node parent);

	/**
	 * Get the current container to add nodes to.
	 */
	Pane getCurrentContainer();

	/**
	 * Push a new container onto the stack.
	 */
	void pushContainer(Pane pane);

	/**
	 * Pop the current container from the stack.
	 */
	Pane popContainer();

	/**
	 * Register a mapping between an AST node and a JavaFX node.
	 */
	void registerNode(Node astNode, javafx.scene.Node fxNode);

	/**
	 * Get the root container.
	 */
	VBox getRoot();
}
