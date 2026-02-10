package com.icuxika.markdown.stream.render.demo.javafx;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfx.incubator.scene.control.richtext.CodeArea;

public class BatchRenderDemo extends Application {

	private JavaFxRenderer renderer;
	private ScrollPane outputScroll;
	private VBox outputBox;

	@Override
	public void start(Stage primaryStage) {
		final MarkdownTheme theme = new MarkdownTheme();

		// Input Area
		CodeArea inputArea = new CodeArea();
		inputArea.setLineNumbersEnabled(true);
		inputArea.setSyntaxDecorator(new MarkdownSyntaxDecorator());
		// Force wrap text if possible, or leave as is.
		// CodeArea styling for input area itself (background, text color) needs to be
		// managed.
		// We can bind its style or add a listener to theme.

		// Load comprehensive.md (or template.md fallback)
		try (java.io.InputStream is = getClass().getResourceAsStream("/comprehensive.md")) {
			if (is != null) {
				String template = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
				appendText(inputArea, template);
			} else {
				appendText(inputArea, "# Hello Markdown\n\nType **markdown** here and click Render.");
			}
		} catch (java.io.IOException e) {
			appendText(inputArea, "# Error Loading Template\n\n" + e.getMessage());
		}

		// Output Area
		outputScroll = new ScrollPane();
		outputScroll.setFitToWidth(true);
		outputBox = new VBox();
		outputScroll.setContent(outputBox);

		// Render Button (or auto-render)
		Button renderBtn = new Button("Render");
		renderBtn.setOnAction(e -> render(getText(inputArea), outputScroll));

		// Theme Switch Button
		Button themeBtn = new Button("Switch Theme");
		themeBtn.setOnAction(e -> {
			if (theme.getTheme() == MarkdownTheme.Theme.LIGHT) {
				theme.setTheme(MarkdownTheme.Theme.DARK);
			} else {
				theme.setTheme(MarkdownTheme.Theme.LIGHT);
			}
			// Explicitly re-render to apply new theme to the generated nodes?
			// The nodes use CSS variables, so they should update automatically IF they are
			// attached to the Scene.
			// But if the Scene stylesheet changes, nodes should pick it up.
			// Let's verify if 'outputBox' children pick up the change.
			// They should.
			// Maybe inputArea needs help?
			// CodeArea (RichTextFX) often has its own CSS logic.
			if (theme.getTheme() == MarkdownTheme.Theme.DARK) {
				inputArea.setStyle("-fx-background-color: #0d1117; -fx-text-fill: #c9d1d9;");
			} else {
				inputArea.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #24292f;");
			}
		});

		Button loadBtn = new Button("Load File");
		loadBtn.setOnAction(e -> {
			javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
			fileChooser.getExtensionFilters()
					.add(new javafx.stage.FileChooser.ExtensionFilter("Markdown Files", "*.md", "*.markdown"));
			java.io.File file = fileChooser.showOpenDialog(primaryStage);
			if (file != null) {
				loadFile(file, inputArea);
			}
		});

		Button exportBtn = new Button("Export HTML");
		exportBtn.setOnAction(e -> {
			javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
			fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("HTML Files", "*.html"));
			java.io.File file = fileChooser.showSaveDialog(primaryStage);
			if (file != null) {
				exportHtml(file);
			}
		});

		// Toolbar for controls
		ToolBar toolBar = new ToolBar(renderBtn, themeBtn, loadBtn, exportBtn);

		// Layout
		VBox leftBox = new VBox(toolBar, inputArea);
		javafx.scene.layout.VBox.setVgrow(inputArea, javafx.scene.layout.Priority.ALWAYS);

		SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(leftBox, outputScroll);
		splitPane.setDividerPositions(0.5);

		BorderPane root = new BorderPane();
		root.setCenter(splitPane);

		Scene scene = new Scene(root, 1024, 768);

		// Apply theme
		theme.apply(scene);

		// Initial Input Area Style
		inputArea.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #24292f;");

		primaryStage.setTitle("Markdown Stream Renderer - JavaFX Demo");
		primaryStage.setScene(scene);
		primaryStage.show();

		// Initial Render
		render(getText(inputArea), outputScroll);

		// Sync Scroll Listener
		inputArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				// Let's try the manual way for now as a safe fallback.
				int charIndex = newVal.index();
				String text = getText(inputArea);
				int line = 0;
				if (charIndex <= text.length()) {
					for (int i = 0; i < charIndex; i++) {
						if (text.charAt(i) == '\n') {
							line++;
						}
					}
				}
				syncScrollToPreview(line);
			}
		});
	}

	private void appendText(CodeArea area, String text) {
		area.appendText(text);
	}

	private String getText(CodeArea area) {
		// common API
		return area.getText();
	}

	private void loadFile(java.io.File file, CodeArea area) {
		try {
			String content = java.nio.file.Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
			area.clear();
			area.appendText(content);
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	private void exportHtml(java.io.File file) {
		// TODO: Implement HTML export
		System.out.println("Export HTML to " + file.getAbsolutePath());
	}

	private void render(String markdown, ScrollPane outputScroll) {
		// Use the new extension mechanism
		List<com.icuxika.markdown.stream.render.core.Extension> extensions = java.util.Arrays.asList(
				com.icuxika.markdown.stream.render.core.extension.admonition.AdmonitionExtension.create(),
				com.icuxika.markdown.stream.render.core.extension.math.MathExtension.create(),
				com.icuxika.markdown.stream.render.html.extension.admonition.AdmonitionHtmlExtension.create(),
				com.icuxika.markdown.stream.render.html.extension.math.MathHtmlExtension.create(),
				com.icuxika.markdown.stream.render.javafx.extension.admonition.AdmonitionJavaFxExtension.create(),
				com.icuxika.markdown.stream.render.javafx.extension.math.MathJavaFxExtension.create());

		MarkdownParser parser = MarkdownParser.builder()
				.extensions(extensions)
				.build();

		JavaFxRenderer renderer = JavaFxRenderer.builder()
				.extensions(extensions)
				.build();

		this.renderer = renderer;

		try {
			parser.parse(new java.io.StringReader(markdown), renderer);
			VBox result = (VBox) renderer.getResult();
			outputBox = result;
			outputScroll.setContent(result);
		} catch (java.io.IOException e) {
			e.printStackTrace();
			// Show error in output
			VBox errorBox = new VBox();
			errorBox.getChildren().add(new javafx.scene.control.Label("Error rendering markdown: " + e.getMessage()));
			outputScroll.setContent(errorBox);
		}
	}

	private void syncScrollToPreview(int lineNumber) {
		if (renderer == null) {
			return;
		}

		TreeMap<Integer, javafx.scene.Node> map = renderer.getLineToNodeMap();
		if (map.isEmpty()) {
			return;
		}

		// Find the closest line number <= current line number
		Map.Entry<Integer, javafx.scene.Node> entry = map.floorEntry(lineNumber);
		if (entry != null) {
			javafx.scene.Node node = entry.getValue();
			scrollToNode(node);
		}
	}

	private void scrollToNode(javafx.scene.Node node) {
		// Calculate Y position of the node relative to the content
		double y = 0;
		javafx.scene.Node current = node;
		while (current != null && current != outputBox) {
			y += current.getBoundsInParent().getMinY();
			current = current.getParent();
		}

		// ScrollPane height (viewport)
		double viewportHeight = outputScroll.getViewportBounds().getHeight();
		double contentHeight = outputBox.getBoundsInLocal().getHeight();

		double maxScroll = contentHeight - viewportHeight;
		if (maxScroll <= 0) {
			outputScroll.setVvalue(0);
		} else {
			outputScroll.setVvalue(Math.max(0, Math.min(1, y / maxScroll)));
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
