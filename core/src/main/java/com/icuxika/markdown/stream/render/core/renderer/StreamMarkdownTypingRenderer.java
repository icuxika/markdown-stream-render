package com.icuxika.markdown.stream.render.core.renderer;

import com.icuxika.markdown.stream.render.core.ast.Node;

public interface StreamMarkdownTypingRenderer extends StreamMarkdownRenderer {
	void renderPreviewNode(Node node);

	void clearPreview();
}
