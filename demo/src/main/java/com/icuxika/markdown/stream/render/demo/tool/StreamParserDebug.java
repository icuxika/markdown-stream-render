package com.icuxika.markdown.stream.render.demo.tool;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.StreamMarkdownRenderer;

/**
 * Stream Parser Debug Tool.
 */
public class StreamParserDebug {

	/**
	 * Main.
	 *
	 * @param args
	 *            args
	 */
	public static void main(String[] args) {
		String markdown = "- Item 1\n"
				+ "- Item 2\n"
				+ "- Item 3\n";

		StreamMarkdownRenderer renderer = new StreamMarkdownRenderer() {
			@Override
			public void renderNode(Node node) {
				// System.out.println("RENDER: " + node.getClass().getSimpleName());
			}

			@Override
			public void openBlock(Node node) {
				System.out.println("OPEN: " + node.getClass().getSimpleName());
			}

			@Override
			public void closeBlock(Node node) {
				System.out.println("CLOSE: " + node.getClass().getSimpleName());
			}
		};

		// StreamMarkdownParser parser =
		// StreamMarkdownParser.builder().renderer(renderer).build();
		// CoreExtension.addDefaults(StreamMarkdownParser.builder().renderer(renderer));
		// This line does nothing to parser instance!

		// Correct way to add defaults
		StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(renderer);
		StreamMarkdownParser parser = builder.build();

		System.out.println("--- Start Parsing ---");
		parser.push(markdown);
		parser.close();
		System.out.println("--- End Parsing ---");
	}
}
