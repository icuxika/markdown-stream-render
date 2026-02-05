package com.icuxika.markdown.stream.render.core.extension.admonition;

import com.icuxika.markdown.stream.render.core.ast.Block;
import com.icuxika.markdown.stream.render.core.ast.Visitor;

public class AdmonitionBlock extends Block {
    private String type;
    private String title;

    public AdmonitionBlock(String type, String title) {
        this.type = type;
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void accept(Visitor visitor) {
        // Standard Visitor doesn't support custom nodes.
        // Our Renderer architecture uses NodeRendererMap instead of Visitor traversal for children.
    }
}
