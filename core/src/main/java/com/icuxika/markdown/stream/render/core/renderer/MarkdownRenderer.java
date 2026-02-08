package com.icuxika.markdown.stream.render.core.renderer;

import com.icuxika.markdown.stream.render.core.ast.Visitor;

/**
 * Markdown Renderer Interface. Inherits from {@link Visitor}, generating target
 * format (e.g. HTML, JavaFX Node) by
 * traversing AST nodes.
 */
public interface MarkdownRenderer extends Visitor {
	/**
	 * Get the rendering result.
	 *
	 * @return rendered object (type depends on implementation, e.g. String or
	 *         javafx.scene.Node)
	 */
	Object getResult();
}
