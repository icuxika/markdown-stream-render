package com.icuxika.markdown.stream.render.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icuxika.markdown.stream.render.core.ast.HardBreak;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.SoftBreak;
import com.icuxika.markdown.stream.render.core.ast.Text;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.StreamMarkdownTypingRenderer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TypewriterIncrementalParsingTest {

	@Test
	public void previewUpdatesWhileLeafOpen() {
		List<String> events = new ArrayList<>();

		StreamMarkdownTypingRenderer renderer = new StreamMarkdownTypingRenderer() {
			@Override
			public void renderPreviewNode(Node node) {
				events.add("preview:" + extractText(node));
			}

			@Override
			public void clearPreview() {
				events.add("clear");
			}

			@Override
			public void renderNode(Node node) {
				events.add("final:" + extractText(node));
			}

			@Override
			public void openBlock(Node node) {
			}

			@Override
			public void closeBlock(Node node) {
			}
		};

		StreamMarkdownParser parser = StreamMarkdownParser.builder().renderer(renderer).build();

		parser.push("Hello");
		parser.push(" world");
		parser.push("\n");
		parser.push("Next");
		parser.push("\n");
		parser.push("\n");

		assertTrue(events.contains("preview:Hello"));
		assertTrue(events.contains("preview:Hello world"));
		assertTrue(events.contains("preview:Hello world\nNext"));
		assertTrue(events.stream().anyMatch(e -> e.startsWith("final:")));
		assertEquals("final:Hello world\nNext",
				events.stream().filter(e -> e.startsWith("final:")).reduce((a, b) -> b).orElse(""));
	}

	@Test
	public void previewUpdatesInsideListItemAcrossLines() {
		List<String> previews = new ArrayList<>();

		StreamMarkdownTypingRenderer renderer = new StreamMarkdownTypingRenderer() {
			@Override
			public void renderPreviewNode(Node node) {
				previews.add(extractText(node));
			}

			@Override
			public void clearPreview() {
			}

			@Override
			public void renderNode(Node node) {
			}

			@Override
			public void openBlock(Node node) {
			}

			@Override
			public void closeBlock(Node node) {
			}
		};

		StreamMarkdownParser parser = StreamMarkdownParser.builder().renderer(renderer).build();

		parser.push("## 9. Edge Cases\n");
		parser.push("* Long line: This is a very very very\n");
		parser.push("  very very very long line.\n");
		parser.push("\n");

		boolean sawFirst = previews.stream().anyMatch(p -> p.contains("Long line: This is a very very very"));
		boolean sawSecond = previews.stream()
				.anyMatch(p -> p.contains("Long line: This is a very very very\nvery very very long line."));
		assertTrue(sawFirst);
		assertTrue(sawSecond);
	}

	private static String extractText(Node node) {
		StringBuilder sb = new StringBuilder();
		extractTextRec(node, sb);
		return sb.toString();
	}

	private static void extractTextRec(Node node, StringBuilder sb) {
		Node child = node.getFirstChild();
		while (child != null) {
			if (child instanceof Text) {
				sb.append(((Text) child).getLiteral());
			} else if (child instanceof SoftBreak) {
				sb.append("\n");
			} else if (child instanceof HardBreak) {
				sb.append("\n");
			} else {
				extractTextRec(child, sb);
			}
			child = child.getNext();
		}
	}
}
