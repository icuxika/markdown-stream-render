package com.icuxika.markdown.stream.render.benchmark;

import com.icuxika.markdown.stream.render.core.ast.BlockQuote;
import com.icuxika.markdown.stream.render.core.ast.BulletList;
import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.ast.ListItem;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.OrderedList;
import com.icuxika.markdown.stream.render.core.extension.admonition.AdmonitionBlock;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxStreamRenderer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class MarkdownParserBenchmark {

	@Param({ "SMALL", "MEDIUM", "LARGE" })
	private String size;

	private String markdownInput;
	private MarkdownParser parser;
	private Document preParsedDoc;

	// JavaFX initialization control
	private static final AtomicBoolean jfxInitialized = new AtomicBoolean(false);

	/**
	 * Setup benchmark data.
	 */
	@Setup
	public void setup() {
		// Initialize JavaFX Platform if not already started
		if (!jfxInitialized.getAndSet(true)) {
			try {
				Platform.startup(() -> {
				});
			} catch (IllegalStateException e) {
				// Platform already started, ignore
			}
		}

		parser = new MarkdownParser();

		switch (size) {
			case "SMALL":
				markdownInput = "# Hello\nThis is a small test.";
				break;
			case "MEDIUM":
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < 100; i++) {
					sb.append("## Section ").append(i).append("\n");
					sb.append("This is paragraph ").append(i).append(" with **bold** and *italic* text.\n");
					sb.append("- List item 1\n- List item 2\n");
					sb.append("| Header 1 | Header 2 |\n| --- | --- |\n| Cell 1 | Cell 2 |\n");
					sb.append("- [ ] Task item 1\n- [x] Task item 2\n\n");
				}
				markdownInput = sb.toString();
				break;
			case "LARGE":
				StringBuilder sb2 = new StringBuilder();
				for (int i = 0; i < 1000; i++) {
					sb2.append("## Section ").append(i).append("\n");
					sb2.append("This is paragraph ").append(i).append(" with **bold** and *italic* text.\n");
					sb2.append("- List item 1\n- List item 2\n");
					sb2.append("| Header 1 | Header 2 |\n| --- | --- |\n| Cell 1 | Cell 2 |\n");
					sb2.append("- [ ] Task item 1\n- [x] Task item 2\n\n");
					sb2.append("```java\nSystem.out.println(\"Code block\");\n```\n\n");
				}
				markdownInput = sb2.toString();
				break;
			default:
				markdownInput = "";
				break;
		}

		preParsedDoc = parser.parse(markdownInput);
	}

	/**
	 * Benchmark parsing only.
	 *
	 * @return parsed document
	 */
	@Benchmark
	public Document parseOnly() {
		return parser.parse(markdownInput);
	}

	/**
	 * Benchmark HTML rendering only (from pre-parsed AST).
	 *
	 * @return HTML string
	 */
	@Benchmark
	public String renderHtmlOnly() {
		// HtmlRenderer is stateful (StringBuilder), so we must create a new one each
		// time
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		preParsedDoc.accept(renderer);
		return (String) renderer.getResult();
	}

	/**
	 * Benchmark parsing and HTML rendering.
	 *
	 * @return HTML string
	 */
	@Benchmark
	public String parseAndRenderHtml() {
		Document doc = parser.parse(markdownInput);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		doc.accept(renderer);
		return (String) renderer.getResult();
	}

	/**
	 * Benchmark JavaFX rendering only (from pre-parsed AST).
	 * 
	 * Note: This measures the time to create the scene graph nodes. It does not
	 * measure the actual rendering to the screen (layout/pulse), which happens on
	 * the JavaFX Application Thread. However, node creation is a significant part
	 * of the cost.
	 */
	@Benchmark
	public Object renderJavaFxOnly() {
		// JavaFxRenderer creates its own root VBox
		JavaFxRenderer renderer = new JavaFxRenderer();
		preParsedDoc.accept(renderer);
		return renderer.getResult();
	}

	/**
	 * Benchmark parsing and JavaFX rendering.
	 */
	@Benchmark
	public Object parseAndRenderJavaFx() {
		Document doc = parser.parse(markdownInput);
		JavaFxRenderer renderer = new JavaFxRenderer();
		doc.accept(renderer);
		return renderer.getResult();
	}

	/**
	 * Benchmark JavaFX Stream Rendering.
	 * 
	 * This measures the time to queue all rendering tasks AND execute them on the
	 * JavaFX thread. It simulates a stream parser by traversing the AST and calling
	 * the stream renderer's methods.
	 */
	@Benchmark
	public Object renderJavaFxStream() throws InterruptedException {
		VBox root = new VBox();
		JavaFxStreamRenderer renderer = new JavaFxStreamRenderer(root);

		// Use latch to wait for FX thread completion
		CountDownLatch latch = new CountDownLatch(1);

		// Simulate stream events
		simulateStream(preParsedDoc, renderer);

		// Schedule a task at the end of the queue to signal completion
		Platform.runLater(latch::countDown);

		// Wait for rendering to complete
		latch.await();

		return root;
	}

	private void simulateStream(Node node, JavaFxStreamRenderer renderer) {
		// Container blocks: open -> children -> close
		if (node instanceof Document || node instanceof BlockQuote || node instanceof BulletList
				|| node instanceof OrderedList || node instanceof ListItem || node instanceof AdmonitionBlock) {
			renderer.openBlock(node);
			Node child = node.getFirstChild();
			while (child != null) {
				simulateStream(child, renderer);
				child = child.getNext();
			}
			renderer.closeBlock(node);
		} else {
			// Leaf blocks: just render
			renderer.renderNode(node);
		}
	}
}
