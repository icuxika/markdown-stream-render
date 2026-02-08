package com.icuxika.markdown.stream.render.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.extension.admonition.AdmonitionBlock;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.StreamMarkdownTypingRenderer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class StreamMarkdownParserEventTest {

	@Test
	public void streamingMustKeepOpenCloseBalancedForNestedContainers() {
		RecordingTypingRenderer renderer = new RecordingTypingRenderer();
		StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(renderer);
		CoreExtension.addDefaults(builder);
		StreamMarkdownParser parser = builder.build();

		assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
			parser.push("!!! info \"Ti");
			parser.push("tle\"\n");
			parser.push("    - item 1\n");
			parser.push("    - item 2");
			parser.close();
		});

		long opens = renderer.events.stream().filter(e -> e.kind == EventKind.OPEN).count();
		long closes = renderer.events.stream().filter(e -> e.kind == EventKind.CLOSE).count();
		assertTrue(closes <= opens);

		long admonitionOpen = renderer.events.stream()
				.filter(e -> e.kind == EventKind.OPEN && e.nodeType == AdmonitionBlock.class).count();
		long admonitionClose = renderer.events.stream()
				.filter(e -> e.kind == EventKind.CLOSE && e.nodeType == AdmonitionBlock.class).count();
		assertEquals(1, admonitionOpen);
		assertEquals(1, admonitionClose);
	}

	@Test
	public void typingPreviewMustBeClearedBeforeFinalNodeIsRendered() {
		RecordingTypingRenderer renderer = new RecordingTypingRenderer();
		StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(renderer);
		CoreExtension.addDefaults(builder);
		StreamMarkdownParser parser = builder.build();

		parser.push("Hello");
		assertTrue(renderer.events.stream().anyMatch(e -> e.kind == EventKind.PREVIEW));

		renderer.events.clear();
		parser.push(" world\n");
		parser.close();

		int firstRender = indexOf(renderer.events, EventKind.RENDER);
		int firstClear = indexOf(renderer.events, EventKind.CLEAR_PREVIEW);
		assertTrue(firstClear != -1);
		assertTrue(firstRender != -1);
		assertTrue(firstClear < firstRender);
	}

	private static int indexOf(List<Event> events, EventKind kind) {
		for (int i = 0; i < events.size(); i++) {
			if (events.get(i).kind == kind) {
				return i;
			}
		}
		return -1;
	}

	private enum EventKind {
		OPEN, CLOSE, RENDER, PREVIEW, CLEAR_PREVIEW
	}

	private static final class Event {
		private final EventKind kind;
		private final Class<?> nodeType;

		private Event(EventKind kind, Class<?> nodeType) {
			this.kind = kind;
			this.nodeType = nodeType;
		}
	}

	private static final class RecordingTypingRenderer implements StreamMarkdownTypingRenderer {
		private final List<Event> events = new ArrayList<>();

		@Override
		public void renderNode(Node node) {
			events.add(new Event(EventKind.RENDER, node != null ? node.getClass() : null));
		}

		@Override
		public void openBlock(Node node) {
			events.add(new Event(EventKind.OPEN, node != null ? node.getClass() : null));
		}

		@Override
		public void closeBlock(Node node) {
			events.add(new Event(EventKind.CLOSE, node != null ? node.getClass() : null));
		}

		@Override
		public void renderPreviewNode(Node node) {
			events.add(new Event(EventKind.PREVIEW, node != null ? node.getClass() : null));
		}

		@Override
		public void clearPreview() {
			events.add(new Event(EventKind.CLEAR_PREVIEW, null));
		}
	}
}
