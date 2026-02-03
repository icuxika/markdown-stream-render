package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StreamingGuiApp extends Application {

    private String fullMarkdown = "";
    private volatile int charIndex = 0;
    private final MarkdownParser parser = new MarkdownParser();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Load template
        try (InputStream is = getClass().getResourceAsStream("/template.md")) {
            if (is != null) {
                fullMarkdown = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                fullMarkdown = "# Error\nTemplate not found.";
            }
        } catch (Exception e) {
            fullMarkdown = "# Error\n" + e.getMessage();
        }

        VBox root = new VBox();
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(scrollPane, 800, 600);
        primaryStage.setTitle("Markdown Stream Renderer - JavaFX Streaming Demo");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Simulate Streaming
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (charIndex < fullMarkdown.length()) {
                // Add a chunk of characters (e.g., 2-5 chars to simulate fast typing)
                int chunkSize = 2 + (int)(Math.random() * 5);
                int endIndex = Math.min(charIndex + chunkSize, fullMarkdown.length());
                
                String currentText = fullMarkdown.substring(0, endIndex);
                charIndex = endIndex;

                Platform.runLater(() -> {
                    JavaFxRenderer renderer = new JavaFxRenderer();
                    try {
                        parser.parse(new java.io.StringReader(currentText), renderer);
                        Pane content = (Pane) renderer.getResult();
                        root.getChildren().setAll(content);
                        
                        // Auto-scroll to bottom
                        scrollPane.setVvalue(1.0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                executor.shutdown();
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
        
        primaryStage.setOnCloseRequest(e -> executor.shutdownNow());
    }
}
