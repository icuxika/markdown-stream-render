package com.icuxika.markdown.stream.render.javafx.extension.admonition;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.extension.admonition.AdmonitionBlock;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRendererContext;
import java.util.Collections;
import java.util.Set;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AdmonitionJavaFxRenderer implements JavaFxNodeRenderer {
	private final JavaFxNodeRendererContext context;

	public AdmonitionJavaFxRenderer(JavaFxNodeRendererContext context) {
		this.context = context;
	}

	@Override
	public Set<Class<? extends Node>> getNodeTypes() {
		return Collections.singleton(AdmonitionBlock.class);
	}

	@Override
	public void render(Node node) {
		AdmonitionBlock admonition = (AdmonitionBlock) node;

		VBox box = new VBox();
		box.getStyleClass().add("markdown-admonition");
		box.getStyleClass().add("markdown-admonition-" + admonition.getType());

		// CSS file handles base styles (border, padding, background)
		// Type-specific colors are handled by specific CSS classes

		if (admonition.getTitle() != null) {
			Label titleLabel = new Label(admonition.getTitle());
			titleLabel.getStyleClass().add("markdown-admonition-title");
			box.getChildren().add(titleLabel);
		}

		context.getCurrentContainer().getChildren().add(box);

		// Render children into this box
		context.pushContainer(box);
		context.renderChildren(node);
		context.popContainer();
	}
}
