package com.icuxika.markdown.stream.render.demo.javafx;

import com.icuxika.markdown.stream.render.core.ast.Block;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.Visitor;
import com.icuxika.markdown.stream.render.core.parser.InlineParser;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.ParserExtension;
import com.icuxika.markdown.stream.render.core.parser.block.BlockContinue;
import com.icuxika.markdown.stream.render.core.parser.block.BlockParser;
import com.icuxika.markdown.stream.render.core.parser.block.BlockParserFactory;
import com.icuxika.markdown.stream.render.core.parser.block.BlockStart;
import com.icuxika.markdown.stream.render.core.parser.block.MatchedBlockParser;
import com.icuxika.markdown.stream.render.core.parser.block.ParserState;
import com.icuxika.markdown.stream.render.html.renderer.HtmlNodeRenderer;
import com.icuxika.markdown.stream.render.html.renderer.HtmlNodeRendererContext;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRendererExtension;
import com.icuxika.markdown.stream.render.html.renderer.HtmlWriter;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxNodeRendererContext;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRendererExtension;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Demo for adding a custom extension (Greeting Block).
 * <p>
 * Syntax: `::: greeting [name]`
 * </p>
 */
public class ExtensionDemo extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		TextArea inputArea = new TextArea();
		inputArea.setText("# Custom Extension Demo\n\n::: greeting Trae\n\n::: greeting User\n");

		ScrollPane outputScroll = new ScrollPane();
		VBox outputBox = new VBox();
		outputScroll.setContent(outputBox);

		inputArea.textProperty().addListener((obs, oldVal, newVal) -> render(newVal, outputBox));

		SplitPane splitPane = new SplitPane(inputArea, outputScroll);
		Scene scene = new Scene(splitPane, 800, 600);

		primaryStage.setTitle("Custom Extension Demo");
		primaryStage.setScene(scene);
		primaryStage.show();

		render(inputArea.getText(), outputBox);
	}

	private void render(String markdown, VBox outputBox) {
		// Use the new simplified API
		// Default extensions (Admonition, Math) are loaded automatically.
		GreetingExtension greetingExtension = new GreetingExtension();

		MarkdownParser parser = MarkdownParser.builder()
				.extensions(greetingExtension)
				.build();

		JavaFxRenderer renderer = JavaFxRenderer.builder()
				.extensions(greetingExtension)
				.build();

		try {
			parser.parse(new java.io.StringReader(markdown), renderer);
			VBox result = (VBox) renderer.getResult();
			outputBox.getChildren().setAll(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// --- Custom Extension Implementation ---

	/**
	 * The unified extension class implementing all 3 interfaces.
	 */
	static class GreetingExtension implements ParserExtension, HtmlRendererExtension, JavaFxRendererExtension {
		@Override
		public void extend(MarkdownParser.Builder builder) {
			builder.blockParserFactory(new GreetingBlockParserFactory());
		}

		@Override
		public void extend(HtmlRenderer.Builder builder) {
			builder.nodeRendererFactory(GreetingHtmlRenderer::new);
		}

		@Override
		public void extend(JavaFxRenderer.Builder builder) {
			builder.nodeRendererFactory(GreetingJavaFxRenderer::new);
		}
	}

	/**
	 * Custom AST Node.
	 * Must extend Block because BlockParser.getBlock() returns Block.
	 */
	static class GreetingBlock extends Block {
		private final String name;

		public GreetingBlock(String name) {
			this.name = name;
		}

		@Override
		public void accept(Visitor visitor) {
			if (visitor instanceof HtmlRenderer) {
				((HtmlRenderer) visitor).visit(this);
			} else if (visitor instanceof JavaFxRenderer) {
				((JavaFxRenderer) visitor).visit(this);
			} else {
				// Fallback: CustomNode visit if we had it, or just visit children
				// But we need to satisfy Visitor interface if we were CustomNode.
				// Since we extend Block, Visitor has visit(Block) ? No, Block is abstract.
				// We must handle the visit manually or cast.
				// Actually, since we are a Block, and Visitor visits concrete types,
				// we are stuck unless Visitor has visit(GreetingBlock) (impossible)
				// or a generic visit(CustomBlock).

				// Let's pretend we are a CustomNode for the visitor if possible,
				// BUT we extend Block.
				// The Visitor interface now has visit(CustomNode).
				// We are NOT CustomNode (we extend Block which extends Node).
				// So we cannot call visitor.visit((CustomNode)this).

				// Workaround: Do nothing or visit children for default visitor
				Node child = getFirstChild();
				while (child != null) {
					child.accept(visitor);
					child = child.getNext();
				}
			}
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Parser Factory.
	 */
	static class GreetingBlockParserFactory implements BlockParserFactory {
		@Override
		public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
			String line = state.getLine().toString().substring(state.getNextNonSpaceIndex());
			Pattern pattern = Pattern.compile("^:::\\s+greeting\\s+(.*)$");
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				String name = matcher.group(1);
				return BlockStart.of(new GreetingParser(name)).atIndex(state.getIndex() + line.length());
			}
			return BlockStart.none();
		}
	}

	/**
	 * Parser Logic.
	 */
	static class GreetingParser implements BlockParser {
		private final GreetingBlock block;

		public GreetingParser(String name) {
			this.block = new GreetingBlock(name);
		}

		@Override
		public Block getBlock() {
			return (Block) block;
		}

		@Override
		public BlockContinue tryContinue(ParserState state) {
			// Single line block
			return BlockContinue.none();
		}

		@Override
		public boolean isContainer() {
			return false;
		}

		@Override
		public void addLine(CharSequence line) {
		}

		@Override
		public void parseInlines(InlineParser inlineParser) {
		}

		@Override
		public void closeBlock() {
		}
	}

	/**
	 * HTML Renderer.
	 */
	static class GreetingHtmlRenderer implements HtmlNodeRenderer {
		private final HtmlNodeRendererContext context;

		public GreetingHtmlRenderer(HtmlNodeRendererContext context) {
			this.context = context;
		}

		@Override
		public Set<Class<? extends Node>> getNodeTypes() {
			return Collections.singleton(GreetingBlock.class);
		}

		@Override
		public void render(Node node) {
			GreetingBlock greeting = (GreetingBlock) node;
			HtmlWriter html = context.getWriter();
			html.tag("div", Collections.singletonMap("class", "greeting-card"));
			html.text("👋 Hello, " + greeting.getName() + "!");
			html.closeTag("div");
		}
	}

	/**
	 * JavaFX Renderer.
	 */
	static class GreetingJavaFxRenderer implements JavaFxNodeRenderer {
		private final JavaFxNodeRendererContext context;

		public GreetingJavaFxRenderer(JavaFxNodeRendererContext context) {
			this.context = context;
		}

		@Override
		public Set<Class<? extends Node>> getNodeTypes() {
			return Collections.singleton(GreetingBlock.class);
		}

		@Override
		public void render(Node node) {
			GreetingBlock greeting = (GreetingBlock) node;
			Label label = new Label("👋 Hello, " + greeting.getName() + "!");
			label.setStyle(
					"-fx-font-size: 16px; -fx-padding: 10px; -fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-width: 0 0 0 4px;");
			context.getCurrentContainer().getChildren().add(label);
		}
	}
}
