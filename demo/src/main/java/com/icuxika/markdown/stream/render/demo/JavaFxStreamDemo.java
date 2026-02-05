package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.IStreamMarkdownRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxStreamRenderer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JavaFxStreamDemo extends Application {

    private StreamMarkdownParser parser;
    private ScheduledExecutorService executor;
    private TextArea logArea;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Output Area
        VBox outputBox = new VBox();
        outputBox.setSpacing(10);
        outputBox.getStyleClass().add("markdown-root");

        ScrollPane scrollPane = new ScrollPane(outputBox);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        // Control Area
        VBox bottomBox = new VBox(10);
        Button startButton = new Button("Start Streaming Simulation (Slow)");

        logArea = new TextArea();
        logArea.setPrefHeight(150);
        logArea.setEditable(false);
        logArea.setPromptText("Logs will appear here...");

        bottomBox.getChildren().addAll(startButton, logArea);
        root.setBottom(bottomBox);

        // Setup Renderer and Parser
        JavaFxStreamRenderer realRenderer = new JavaFxStreamRenderer(outputBox);

        // Proxy Renderer for Logging
        IStreamMarkdownRenderer loggingRenderer = new IStreamMarkdownRenderer() {
            @Override
            public void renderNode(Node node) {
                log("RENDER: " + node.getClass().getSimpleName());
                realRenderer.renderNode(node);
            }

            @Override
            public void openBlock(Node node) {
                log("OPEN: " + node.getClass().getSimpleName());
                realRenderer.openBlock(node);
            }

            @Override
            public void closeBlock(Node node) {
                log("CLOSE: " + node.getClass().getSimpleName());
                realRenderer.closeBlock(node);
            }
        };

        StreamMarkdownParser.Builder parserBuilder = StreamMarkdownParser.builder()
                .renderer(loggingRenderer);

        CoreExtension.addDefaults(parserBuilder);

        parser = parserBuilder.build();

        startButton.setOnAction(e -> {
            startButton.setDisable(true);
            outputBox.getChildren().clear();
            logArea.clear();
            startStreaming();
        });

        Scene scene = new Scene(root, 800, 800);

        primaryStage.setTitle("Markdown Stream JavaFX Demo - Incremental Verification");
        primaryStage.setScene(scene);
        primaryStage.show();

        outputBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    private void startStreaming() {
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

        String[] chunks = content.split("(?<=\\n)");

        executor = Executors.newSingleThreadScheduledExecutor();
        final int[] index = {0};
        final String[] finalChunks = chunks;

        // Slow down to 200ms to visualize incremental parsing
        executor.scheduleAtFixedRate(() -> {
            if (index[0] < finalChunks.length) {
                String chunk = finalChunks[index[0]];
                // Log push event
                Platform.runLater(() -> log("PUSH Chunk " + index[0] + ": " + escape(chunk)));

                try {
                    parser.push(chunk);
                    index[0]++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                log("Stream Closed");
                parser.close();
                executor.shutdown();
                Platform.runLater(() -> {
                    // button re-enable logic if stored in field
                });
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    private void log(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        Platform.runLater(() -> {
            logArea.appendText("[" + time + "] " + msg + "\n");
        });
    }

    private String escape(String s) {
        return s.replace("\n", "\\n").replace("\r", "\\r");
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
