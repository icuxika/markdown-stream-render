package com.icuxika.markdown.stream.render.html.renderer;

/**
 * Factory for creating a {@link NodeRenderer}.
 */
public interface NodeRendererFactory {

    /**
     * Create a new node renderer context.
     *
     * @param context the context for rendering (e.g. provides access to HTML writer)
     * @return a node renderer
     */
    NodeRenderer create(NodeRendererContext context);
}
