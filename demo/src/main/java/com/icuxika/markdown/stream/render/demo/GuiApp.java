package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GuiApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Input Area
        TextArea inputArea = new TextArea();
        inputArea.setWrapText(true);

        // Load template.md
        try (java.io.InputStream is = getClass().getResourceAsStream("/template.md")) {
            if (is != null) {
                String template = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                inputArea.setText(template);
            } else {
                inputArea.setText("# Hello Markdown\n\nType **markdown** here and click Render.");
            }
        } catch (java.io.IOException e) {
            inputArea.setText("# Error Loading Template\n\n" + e.getMessage());
        }

        // Output Area
        ScrollPane outputScroll = new ScrollPane();
        outputScroll.setFitToWidth(true);
        VBox outputBox = new VBox();
        outputScroll.setContent(outputBox);

        // Render Button (or auto-render)
        Button renderBtn = new Button("Render");
        renderBtn.setOnAction(e -> render(inputArea.getText(), outputScroll));

        // Layout
        VBox leftBox = new VBox(inputArea, renderBtn);
        javafx.scene.layout.VBox.setVgrow(inputArea, javafx.scene.layout.Priority.ALWAYS);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftBox, outputScroll);
        splitPane.setDividerPositions(0.5);

        root.setCenter(splitPane);

        // Initial Render
        render(inputArea.getText(), outputScroll);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Markdown Stream Renderer - JavaFX Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void render(String markdown, ScrollPane outputScroll) {
        MarkdownParser parser = new MarkdownParser();
        JavaFxRenderer renderer = new JavaFxRenderer();
        try {
            parser.parse(new java.io.StringReader(markdown), renderer);
            VBox result = (VBox) renderer.getResult();
            outputScroll.setContent(result);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Show error in output
            VBox errorBox = new VBox();
            errorBox.getChildren().add(new javafx.scene.control.Label("Error rendering markdown: " + e.getMessage()));
            outputScroll.setContent(errorBox);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
