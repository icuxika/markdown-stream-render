package com.icuxika.markdown.stream.render.javafx.extension.math;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.extension.math.MathNode;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRendererContext;
import java.util.Collections;
import java.util.Set;
import javafx.scene.control.Label;

public class MathJavaFxRenderer implements JavaFxNodeRenderer {
    private final JavaFxNodeRendererContext context;

    public MathJavaFxRenderer(JavaFxNodeRendererContext context) {
        this.context = context;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.singleton(MathNode.class);
    }

    @Override
    public void render(Node node) {
        MathNode math = (MathNode) node;

        Label label = new Label(math.getContent());
        label.getStyleClass().add("markdown-math");

        // Base styles moved to CSS
        // -fx-font-family: "Times New Roman";
        // -fx-font-style: italic;

        context.getCurrentContainer().getChildren().add(label);
    }
}
