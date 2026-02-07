package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxStreamRenderer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JavaFxStreamDemo extends Application {

    private StreamMarkdownParser parser;
    private ScheduledExecutorService executor;
    private TextArea logArea;
    private ListView<com.icuxika.markdown.stream.render.core.ast.Heading> tocList;
    private VBox outputBox;
    private Button fastButton;
    private Button slowButton;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Output Area
        outputBox = new VBox();
        outputBox.setSpacing(10);
        outputBox.getStyleClass().add("markdown-root");

        ScrollPane scrollPane = new ScrollPane(outputBox);
        scrollPane.setFitToWidth(true);
        // root.setCenter(scrollPane); // Replaced by SplitPane

        // Sidebar TOC
        tocList = new ListView<>();
        tocList.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(com.icuxika.markdown.stream.render.core.ast.Heading item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    StringBuilder sb = new StringBuilder();
                    // Indent based on level
                    for (int i = 1; i < item.getLevel(); i++) {
                        sb.append("  ");
                    }

                    // Get text content
                    Node child = item.getFirstChild();
                    while (child != null) {
                        if (child instanceof com.icuxika.markdown.stream.render.core.ast.Text) {
                            sb.append(((com.icuxika.markdown.stream.render.core.ast.Text) child).getLiteral());
                        }
                        child = child.getNext();
                    }
                    setText(sb.toString());
                }
            }
        });

        tocList.setOnMouseClicked(e -> {
            com.icuxika.markdown.stream.render.core.ast.Heading selected = tocList.getSelectionModel()
                    .getSelectedItem();
            if (selected != null) {
                String anchorId = selected.getAnchorId();
                if (anchorId != null) {
                    // Find node with this userData in outputBox
                    for (javafx.scene.Node n : outputBox.getChildren()) {
                        // We need to search recursively if wrapped in containers?
                        // JavaFxStreamRenderer wraps Heading in TextFlow or VBox?
                        // Heading is rendered as TextFlow added to outputBox (or container stack).
                        // Let's check userData of children of outputBox.
                        if (anchorId.equals(n.getUserData())) {
                            // Scroll to this node
                            // Calculate Vvalue
                            double y = n.getBoundsInParent().getMinY();
                            // If parent is not scrollPane content, we need to transform coords?
                            // outputBox is direct content of scrollPane.
                            double height = outputBox.getHeight();
                            double viewportHeight = scrollPane.getViewportBounds().getHeight();

                            // Normalize y to 0..1
                            // Vvalue = (y) / (totalHeight - viewportHeight)
                            double verticalPos = y / (height - viewportHeight);
                            if (verticalPos < 0) {
                                verticalPos = 0;
                            }
                            if (verticalPos > 1) {
                                verticalPos = 1;
                            }
                            scrollPane.setVvalue(verticalPos);
                            break;
                        }
                    }
                }
            }
        });

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(tocList, scrollPane);
        splitPane.setDividerPositions(0.25);
        root.setCenter(splitPane);

        // Control Area
        fastButton = new Button("Fast Stream (LLM Simulation)");
        slowButton = new Button("Slow Stream (Typewriter)");

        logArea = new TextArea();
        logArea.setPrefHeight(150);
        logArea.setEditable(false);
        logArea.setPromptText("Logs will appear here...");

        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
        buttonBox.getChildren().addAll(fastButton, slowButton);

        VBox bottomBox = new VBox(10);
        bottomBox.getChildren().addAll(buttonBox, logArea);
        root.setBottom(bottomBox);

        // Load CSS (reuse batch demo logic implicitly by renderer but let's ensure
        // style)
        // Actually JavaFxStreamRenderer should attach stylesheets?
        // Let's check JavaFxStreamRenderer. It usually relies on Scene stylesheets or
        // adds them.
        // In BatchDemo we added stylesheet to Scene. Here we should too.

        // ... (inside setup)

        fastButton.setOnAction(e -> {
            disableButtons(true, fastButton, slowButton);
            outputBox.getChildren().clear();
            tocList.getItems().clear();
            logArea.clear();
            // LLM style: 10-50ms delay, 2-10 chars chunk
            startStreaming(10, 50, 2, 10);
        });

        slowButton.setOnAction(e -> {
            disableButtons(true, fastButton, slowButton);
            outputBox.getChildren().clear();
            tocList.getItems().clear();
            logArea.clear();
            // Typewriter style: 50-150ms delay, 1 char chunk
            startStreaming(50, 150, 1, 1);
        });

        Scene scene = new Scene(root, 800, 800);
        // Use JavaFxStreamRenderer.class to load resources from the javafx module to
        // avoid module encapsulation issues
        // when running via Launcher (which might be in a different module context)
        scene.getStylesheets().add(JavaFxStreamRenderer.class
                .getResource("/com/icuxika/markdown/stream/render/javafx/css/markdown.css").toExternalForm());
        scene.getStylesheets().add(JavaFxStreamRenderer.class
                .getResource("/com/icuxika/markdown/stream/render/javafx/css/light.css").toExternalForm());
        // Extensions CSS
        scene.getStylesheets()
                .add(JavaFxStreamRenderer.class
                        .getResource("/com/icuxika/markdown/stream/render/javafx/css/extensions/admonition.css")
                        .toExternalForm());
        scene.getStylesheets().add(JavaFxStreamRenderer.class
                .getResource("/com/icuxika/markdown/stream/render/javafx/css/extensions/math.css").toExternalForm());

        // Re-init parser with logging renderer
        // initParser(realRenderer); // Moved to startStreaming

        primaryStage.setTitle("Markdown Stream JavaFX Demo - LLM Token Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();

        outputBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    private void initParser() {
        // Setup Renderer and Parser
        JavaFxStreamRenderer realRenderer = new JavaFxStreamRenderer(outputBox);
        realRenderer.setOnLinkClick(url -> {
            log("Link Clicked: " + url);
            getHostServices().showDocument(url);
        });
        realRenderer.setOnHeadingRendered(heading -> {
            Platform.runLater(() -> tocList.getItems().add(heading));
        });

        StreamMarkdownParser.Builder parserBuilder = StreamMarkdownParser.builder().renderer(realRenderer);

        CoreExtension.addDefaults(parserBuilder);
        parser = parserBuilder.build();
    }

    private void disableButtons(boolean disable, Button... buttons) {
        for (Button b : buttons) {
            b.setDisable(disable);
        }
        // If enabling, we might want to store references to re-enable later
        // For simplicity, we'll look them up or pass callback
        // This method is just for initial click
    }

    private void startStreaming(int minDelay, int maxDelay, int minChunk, int maxChunk) {
        String content = "";
        try (java.io.InputStream is = getClass().getResourceAsStream("/comprehensive.md")) {
            if (is != null) {
                content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } else {
                // Fallback if comprehensive.md not found
                try (java.io.InputStream is2 = getClass().getResourceAsStream("/template.md")) {
                    if (is2 != null) {
                        content = new String(is2.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    } else {
                        content = "# Error\n\nNo template found.";
                    }
                }
            }
        } catch (Exception e) {
            content = "# Error\n\n" + e.getMessage();
        }

        final String finalContent = content;

        initParser();

        executor = Executors.newSingleThreadScheduledExecutor();

        // Use a recursive runnable to schedule next chunk with random delay
        Runnable task = new Runnable() {
            int index = 0;
            java.util.Random random = new java.util.Random();

            @Override
            public void run() {
                if (index < finalContent.length()) {
                    int remaining = finalContent.length() - index;
                    int chunkSize = random.nextInt(maxChunk - minChunk + 1) + minChunk;
                    if (chunkSize > remaining) {
                        chunkSize = remaining;
                    }

                    String chunk = finalContent.substring(index, index + chunkSize);
                    index += chunkSize;

                    // Push chunk
                    // Platform.runLater(() -> log("PUSH: " + escape(chunk))); // Optional logging
                    try {
                        parser.push(chunk);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Schedule next
                    int delay = random.nextInt(maxDelay - minDelay + 1) + minDelay;
                    executor.schedule(this, delay, TimeUnit.MILLISECONDS);
                } else {
                    log("Stream Closed");
                    parser.close();
                    executor.shutdown();
                    Platform.runLater(() -> {
                        // Re-enable buttons? (Need references, skipping for demo simplicity)
                        log("Done. Restart app to run again.");
                    });
                }
            }
        };

        executor.schedule(task, 0, TimeUnit.MILLISECONDS);
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
