package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
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

import java.util.Map;
import java.util.TreeMap;

public class BatchFxDemo extends Application {

    private JavaFxRenderer renderer;
    private ScrollPane outputScroll;
    private VBox outputBox;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        MarkdownTheme theme = new MarkdownTheme();

        // Input Area
        CodeArea inputArea = new CodeArea();
        inputArea.setLineNumbersEnabled(true);
        inputArea.setSyntaxDecorator(new MarkdownSyntaxDecorator());
        // inputArea.setWrapText(true); // CodeArea might not support wrapping the same way

        // Load template.md
        try (java.io.InputStream is = getClass().getResourceAsStream("/template.md")) {
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
        });

        // Toolbar for controls
        ToolBar toolBar = new ToolBar(renderBtn, themeBtn);

        // Layout
        VBox leftBox = new VBox(toolBar, inputArea);
        javafx.scene.layout.VBox.setVgrow(inputArea, javafx.scene.layout.Priority.ALWAYS);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftBox, outputScroll);
        splitPane.setDividerPositions(0.5);

        root.setCenter(splitPane);

        // Initial Render
        render(getText(inputArea), outputScroll);

        // Sync Scroll Listener
        inputArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int lineNumber = newVal.index(); // Corrected
                syncScrollToPreview(lineNumber);
            }
        });

        Scene scene = new Scene(root, 1024, 768);

        // Apply theme
        theme.apply(scene);

        primaryStage.setTitle("Markdown Stream Renderer - JavaFX Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void appendText(CodeArea area, String text) {
        area.appendText(text);
    }

    private String getText(CodeArea area) {
        // common API
        return area.getText();
    }

    private void render(String markdown, ScrollPane outputScroll) {
        MarkdownParser parser = MarkdownParser.builder()
                .blockParserFactory(new BlockPluginDemo.AdmonitionBlockParserFactory())
                .inlineParserFactory(new InlinePluginDemo.MathParserFactory())
                .build();

        JavaFxRenderer renderer = JavaFxRenderer.builder()
                .nodeRendererFactory(context -> new BlockPluginDemo.AdmonitionJavaFxRenderer(context))
                .nodeRendererFactory(context -> new InlinePluginDemo.MathJavaFxRenderer(context))
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
        if (renderer == null) return;

        TreeMap<Integer, javafx.scene.Node> map = renderer.getLineToNodeMap();
        if (map.isEmpty()) return;

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
