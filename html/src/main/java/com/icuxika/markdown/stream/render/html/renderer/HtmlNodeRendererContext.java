package com.icuxika.markdown.stream.render.html.renderer;

import com.icuxika.markdown.stream.render.core.ast.Node;

/**
 * Context passed to {@link HtmlNodeRenderer} during rendering.
 * Provides access to the writer and sub-rendering capabilities.
 */
public interface HtmlNodeRendererContext {

    /**
     * @return the writer to write HTML to
     */
    HtmlWriter getWriter();

    /**
     * Render the children of the given node.
     *
     * @param parent the parent node whose children should be rendered
     */
    void renderChildren(Node parent);
}
