package com.icuxika.markdown.stream.render.core.renderer;

import com.icuxika.markdown.stream.render.core.ast.Node;

/**
 * Stream Markdown Renderer Interface.
 */
public interface StreamMarkdownRenderer {
	/**
	 * Render a node.
	 *
	 * @param node
	 *            node
	 */
	void renderNode(Node node);

	/**
	 * Open a block.
	 *
	 * @param node
	 *            node
	 */
	void openBlock(Node node);

	/**
	 * Close a block.
	 *
	 * @param node
	 *            node
	 */
	void closeBlock(Node node);
}
