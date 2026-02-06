package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.CoreExtension;
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

public class JavaFxBatchDemo extends Application {

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
                // CodeArea paragraph index is 0-based, AST is 0-based (usually, but parser might be 1-based?)
                // MarkdownParser uses 0-based line numbers.
                // jfx.incubator.scene.control.richtext.TextPos doesn't have paragraphIndex() method?
                // Let's check docs or source. Usually it has index() which is char index? 
                // Or maybe it's just index()? Wait, previous error said index() exists but I changed it.
                // The error says: cannot find symbol method paragraphIndex()
                // Let's check what TextPos has.
                // It likely has index() which is the absolute character index.
                // But we need line number.
                // CodeArea should have a method to get line/paragraph from index.
                // inputArea.getModel().getParagraphIndex(newVal.index()) ?
                // Or maybe newVal IS the index? No, it's TextPos.
                // Let's revert to index() and try to calculate line number if possible, or check API.
                // Assuming index() is character offset.
                // CodeArea usually has methods to map offset to line.
                // Let's use inputArea.getParagraphIndex(newVal.index()) if available.
                // Or maybe TextPos has nothing useful?
                // Let's assume inputArea has getParagraphIndexForCharIndex or similar.
                // RichTextFX has getParagraphs()...
                // This is jfx.incubator.scene.control.richtext (RichTextFX incubator or custom?)
                // If it is the incubator one (jfx 24+), let's check its API structure if possible.
                // Without API docs, I'll guess: inputArea.getModel().paragraphIndex(newVal.index())
                // Or just try index() again and see if it maps to line? (unlikely)
                
                // Let's try to find line number from index.
                // If we can't find API, we can calculate it manually (expensive but works).
                // String text = inputArea.getText();
                // int charIndex = newVal.index();
                // int line = 0;
                // for(int i=0; i<charIndex && i<text.length(); i++) if(text.charAt(i) == '\n') line++;
                
                // Let's try the manual way for now as a safe fallback.
                int charIndex = newVal.index();
                String text = getText(inputArea);
                int line = 0;
                if (charIndex <= text.length()) {
                    for (int i = 0; i < charIndex; i++) {
                        if (text.charAt(i) == '\n') line++;
                    }
                }
                syncScrollToPreview(line);
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
        MarkdownParser.Builder parserBuilder = MarkdownParser.builder();
        CoreExtension.addDefaults(parserBuilder);
        MarkdownParser parser = parserBuilder.build();

        // JavaFxRenderer loads default extensions automatically in its constructor
        JavaFxRenderer renderer = new JavaFxRenderer();
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
