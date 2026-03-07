package com.icuxika.markdown.stream.render.html.renderer;

import java.util.Set;

import com.icuxika.markdown.stream.render.core.ast.Node;

/**
 * Renders a specific set of node types.
 */
public interface HtmlNodeRenderer {

  /**
   * Get the set of node types that this renderer handles.
   *
   * @return the set of node types that this renderer handles
   */
  Set<Class<? extends Node>> getNodeTypes();

  /**
   * Render the specified node.
   *
   * @param node
   *            the node to render, guaranteed to be an instance of one of the
   *            types returned by
   *            {@link #getNodeTypes()}
   */
  void render(Node node);
}
