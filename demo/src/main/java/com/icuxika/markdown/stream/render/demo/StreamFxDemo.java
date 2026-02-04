package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownExtensions;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxStreamRenderer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StreamFxDemo extends Application {

    private StreamMarkdownParser parser;
    private ScheduledExecutorService executor;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Output Area
        VBox outputBox = new VBox();
        outputBox.setSpacing(10);
        // outputBox.setStyle("-fx-background-color: white;"); // Removed to use CSS
        outputBox.getStyleClass().add("markdown-root"); // Add root style class
        
        // Load Stylesheet - MOVED TO SCENE
        
        ScrollPane scrollPane = new ScrollPane(outputBox);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        // Control Area
        Button startButton = new Button("Start Streaming Simulation");
        root.setBottom(startButton);

        // Setup Renderer and Parser
        // 1. Initialize Stream Renderer (Defaults will be loaded automatically)
        JavaFxStreamRenderer renderer = new JavaFxStreamRenderer(outputBox);
        
        // 2. Initialize Parser with default extensions
        StreamMarkdownParser.Builder parserBuilder = StreamMarkdownParser.builder()
                .renderer(renderer);
        
        // Load default extension parsers
        MarkdownExtensions.addDefaults(parserBuilder);
        
        parser = parserBuilder.build();

        startButton.setOnAction(e -> {
            startButton.setDisable(true);
            outputBox.getChildren().clear();
            startStreaming();
        });

        Scene scene = new Scene(root, 800, 600);
        
        // Load Stylesheet (Add to Scene to ensure .root variables work)
        java.net.URL cssUrl = getClass().getResource("/com/icuxika/markdown/stream/render/javafx/css/markdown.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Warning: CSS file not found!");
        }

        primaryStage.setTitle("Markdown Stream JavaFX Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Auto-scroll logic
        outputBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    private void startStreaming() {
        // Read template.md from resources
        String content = "";
        try (java.io.InputStream is = getClass().getResourceAsStream("/template.md")) {
            if (is != null) {
                content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } else {
                content = "# Error\n\nCould not load template.md";
            }
        } catch (Exception e) {
            content = "# Error\n\n" + e.getMessage();
        }

        // Split content into chunks (e.g., by lines to simulate typing/streaming)
        // We use lookahead to keep delimiters or just split by newlines
        String[] chunks = content.split("(?<=\\n)");
        
        executor = Executors.newSingleThreadScheduledExecutor();
        final int[] index = {0};
        final String[] finalChunks = chunks;

        executor.scheduleAtFixedRate(() -> {
            if (index[0] < finalChunks.length) {
                String chunk = finalChunks[index[0]++];
                try {
                    parser.push(chunk);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                parser.close();
                executor.shutdown();
                Platform.runLater(() -> {
                    // Re-enable button after done (optional, requires reference to button)
                });
            }
        }, 0, 50, TimeUnit.MILLISECONDS); // Faster speed (50ms) for larger file
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
