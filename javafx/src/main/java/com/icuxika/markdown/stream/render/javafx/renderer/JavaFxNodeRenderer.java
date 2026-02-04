package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.Node;

import java.util.Set;

public interface JavaFxNodeRenderer {

    /**
     * @return the set of node types that this renderer handles
     */
    Set<Class<? extends Node>> getNodeTypes();

    /**
     * Render the specified node.
     *
     * @param node the node to render
     */
    void render(Node node);
}
