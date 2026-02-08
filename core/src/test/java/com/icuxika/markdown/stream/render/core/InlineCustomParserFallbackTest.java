package com.icuxika.markdown.stream.render.core;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.Text;
import com.icuxika.markdown.stream.render.core.extension.math.MathParserFactory;
import com.icuxika.markdown.stream.render.core.parser.InlineParser;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParserOptions;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParserFactory;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class InlineCustomParserFallbackTest {

	@Test
	public void customParserReturningNoneMustNotHang() {
		String input = "We can parse inline math: $E=mc^2 and continue.";
		List<InlineContentParserFactory> factories = List.of(new MathParserFactory());

		List<Node> nodes = assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
			InlineParser parser = new InlineParser(input, Collections.emptyMap(), new MarkdownParserOptions(),
					factories);
			return parser.parse();
		});

		String text = flattenText(nodes);
		assertTrue(text.contains("$E=mc^2"));
		assertTrue(text.contains("continue."));
	}

	private static String flattenText(List<Node> nodes) {
		StringBuilder sb = new StringBuilder();
		for (Node n : nodes) {
			appendText(n, sb);
		}
		return sb.toString();
	}

	private static void appendText(Node node, StringBuilder sb) {
		if (node instanceof Text t) {
			sb.append(t.getLiteral());
		}
		Node child = node.getFirstChild();
		while (child != null) {
			appendText(child, sb);
			child = child.getNext();
		}
	}
}
