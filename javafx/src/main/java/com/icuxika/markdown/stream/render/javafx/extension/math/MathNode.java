package com.icuxika.markdown.stream.render.javafx.extension.math;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.Visitor;

public class MathNode extends Node {
    private String content;

    public MathNode(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public void accept(Visitor visitor) {
        // Visitor pattern not used for custom leaf nodes usually
    }
}
