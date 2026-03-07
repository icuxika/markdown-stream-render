package com.icuxika.markdown.stream.render.javafx.renderer;

import java.util.Set;

import com.icuxika.markdown.stream.render.core.ast.Node;

public interface JavaFxNodeRenderer {

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
   *            the node to render
   */
  void render(Node node);
}
