package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.MarkdownListCell;
import com.icuxika.markdown.stream.render.javafx.renderer.VirtualJavaFxStreamRenderer;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VirtualListDemo extends Application {

    private StreamMarkdownParser parser;
    private ScheduledExecutorService executor;
    private ListView<Node> listView;
    private ObservableList<Node> markdownNodes;

    @Override
    public void start(Stage primaryStage) {
        MarkdownTheme theme = new MarkdownTheme();

        // 1. Data Model
        markdownNodes = FXCollections.observableArrayList();

        // 2. Virtualized ListView
        listView = new ListView<>(markdownNodes);
        VBox.setVgrow(listView, Priority.ALWAYS);

        // Remove default ListView border/background to blend with markdown styles
        // Also remove padding that might conflict with markdown-root padding
        listView.setStyle(
                "-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-padding: 0;");

        // 3. Custom Cell Factory
        listView.setCellFactory(param -> new MarkdownListCell());

        // 4. Controls
        Button startBtn = new Button("Start Streaming");
        startBtn.setOnAction(e -> startStreaming());

        // 5. Layout
        VBox topBox = new VBox(startBtn);
        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(listView);

        Scene scene = new Scene(root, 800, 600);

        // Disable selection visual feedback via CSS
        // We add a stylesheet to the scene or apply inline styles to cells?
        // Inline style on ListView works for the container, but Cell styles are in .list-cell
        // Let's add a custom stylesheet for this demo to override list-cell selection
        scene.getStylesheets().add("data:text/css," +
                ".list-cell:filled:selected, .list-cell:filled:selected:focused { " +
                "-fx-background-color: transparent; " +
                "-fx-text-fill: inherit; " +
                "-fx-border-color: transparent; } " +
                ".list-cell { -fx-background-color: transparent; -fx-padding: 0px; }");

        // Apply theme (loads CSS)
        theme.apply(scene);

        // Also load extension CSS manually if theme doesn't include them?
        // MarkdownTheme usually loads base markdown.css.
        // Let's ensure extensions are loaded.
        scene.getStylesheets().add(JavaFxRenderer.class
                .getResource("/com/icuxika/markdown/stream/render/javafx/css/extensions/admonition.css")
                .toExternalForm());
        scene.getStylesheets().add(JavaFxRenderer.class
                .getResource("/com/icuxika/markdown/stream/render/javafx/css/extensions/math.css").toExternalForm());

        primaryStage.setTitle("Markdown Virtualization Demo (ListView)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initParser() {
        // Setup Parser
        VirtualJavaFxStreamRenderer renderer = new VirtualJavaFxStreamRenderer(markdownNodes, () -> {
            // Request refresh of visible cells
            Platform.runLater(() -> {
                listView.refresh();
                // Auto-scroll to bottom
                listView.scrollTo(markdownNodes.size() - 1);
            });
        });

        StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(renderer);
        CoreExtension.addDefaults(builder);
        parser = builder.build();
    }

    private void startStreaming() {
        markdownNodes.clear();
        // Re-initialize parser to clear state
        initParser();

        String content = loadTemplate();
        final String finalContent = content;

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        executor = Executors.newSingleThreadScheduledExecutor();

        Runnable task = new Runnable() {
            int index = 0;
            java.util.Random random = new java.util.Random();

            @Override
            public void run() {
                if (index < finalContent.length()) {
                    int remaining = finalContent.length() - index;
                    int chunkSize = random.nextInt(50) + 10; // 10-60 chars
                    if (chunkSize > remaining) {
                        chunkSize = remaining;
                    }

                    String chunk = finalContent.substring(index, index + chunkSize);
                    index += chunkSize;

                    try {
                        parser.push(chunk);
                        // Force refresh periodically to show progress within blocks?
                        // Since we can't hook into internal parser state easily,
                        // we rely on renderer.renderNode() which is called on block finalize.
                        // However, we can force a refresh of the ListView here too?
                        // No, refreshing ListView won't help if Node content hasn't changed.
                        // And Node content (Text) is only added on finalize.
                        // So streaming char-by-char is limited by block boundaries in this architecture.
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    int delay = random.nextInt(20) + 5; // 5-25ms
                    executor.schedule(this, delay, TimeUnit.MILLISECONDS);
                } else {
                    parser.close();
                    executor.shutdown();
                }
            }
        };

        executor.schedule(task, 0, TimeUnit.MILLISECONDS);
    }

    private String loadTemplate() {
        try (InputStream is = getClass().getResourceAsStream("/comprehensive.md")) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "# Error\nCould not load template.";
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
