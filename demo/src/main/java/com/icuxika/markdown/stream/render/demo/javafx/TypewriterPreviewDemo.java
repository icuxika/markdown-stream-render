package com.icuxika.markdown.stream.render.demo.javafx;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxStreamRenderer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TypewriterPreviewDemo extends Application {

    private final TextArea sourceArea = new TextArea();
    private final VBox outputBox = new VBox(10);
    private final Label statusLabel = new Label();
    private final Slider delaySlider = new Slider(5, 200, 35);
    private final Slider chunkSlider = new Slider(1, 12, 1);
    private final CheckBox jitterCheck = new CheckBox("随机抖动");
    private final CheckBox finalizeCheck = new CheckBox("末尾追加空行触发最终块");
    private final Button startButton = new Button("Start");
    private final Button stopButton = new Button("Stop");
    private final Button resetButton = new Button("Reset");

    private ScheduledExecutorService executor;
    private ScheduledExecutorService watchdog;
    private volatile Thread streamThread;
    private StreamMarkdownParser parser;
    private String content;
    private int index;
    private volatile long delayMsSetting;
    private volatile int chunkSizeSetting;
    private volatile boolean jitterEnabled;
    private volatile boolean finalizeEnabled;
    private volatile long lastProgressNanos;
    private volatile int lastProgressIndex;

    @Override
    public void start(Stage stage) {
        outputBox.getStyleClass().add("markdown-root");

        sourceArea.setWrapText(true);
        sourceArea.setEditable(false);
        sourceArea.setPrefRowCount(12);
        sourceArea.setPromptText("Streaming source will appear here...");

        ScrollPane scrollPane = new ScrollPane(outputBox);
        scrollPane.setFitToWidth(true);

        VBox rightPane = new VBox(8, new Label("Rendered (Preview + Finalize)"), new Separator(), scrollPane);
        rightPane.setPadding(new Insets(10));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox leftPane = new VBox(8, new Label("Source (streamed chunks)"), new Separator(), sourceArea);
        leftPane.setPadding(new Insets(10));
        VBox.setVgrow(sourceArea, Priority.ALWAYS);

        SplitPane split = new SplitPane(leftPane, rightPane);
        split.setOrientation(Orientation.HORIZONTAL);
        split.setDividerPositions(0.4);

        BorderPane root = new BorderPane();
        root.setCenter(split);
        root.setBottom(buildControls(scrollPane));

        Scene scene = new Scene(root, 1000, 720);
        MarkdownTheme theme = new MarkdownTheme();
        theme.apply(scene);

        stage.setTitle("Typewriter Preview Demo (Char-level Incremental Rendering)");
        stage.setScene(scene);
        stage.show();

        reset();
    }

    private VBox buildControls(ScrollPane scrollPane) {
        delaySlider.setShowTickLabels(true);
        delaySlider.setShowTickMarks(true);
        delaySlider.setMajorTickUnit(50);
        delaySlider.setMinorTickCount(4);

        chunkSlider.setShowTickLabels(true);
        chunkSlider.setShowTickMarks(true);
        chunkSlider.setMajorTickUnit(1);
        chunkSlider.setMinorTickCount(0);
        chunkSlider.setSnapToTicks(true);

        jitterCheck.setSelected(true);
        finalizeCheck.setSelected(true);

        delayMsSetting = Math.round(delaySlider.getValue());
        chunkSizeSetting = (int) Math.round(chunkSlider.getValue());
        jitterEnabled = jitterCheck.isSelected();
        finalizeEnabled = finalizeCheck.isSelected();

        delaySlider.valueProperty().addListener((obs, o, n) -> delayMsSetting = Math.round(n.doubleValue()));
        chunkSlider.valueProperty().addListener((obs, o, n) -> chunkSizeSetting = (int) Math.round(n.doubleValue()));
        jitterCheck.selectedProperty().addListener((obs, o, n) -> jitterEnabled = Boolean.TRUE.equals(n));
        finalizeCheck.selectedProperty().addListener((obs, o, n) -> finalizeEnabled = Boolean.TRUE.equals(n));

        startButton.setOnAction(e -> startStreaming(scrollPane));
        stopButton.setOnAction(e -> stopStreaming());
        resetButton.setOnAction(e -> reset());

        stopButton.setDisable(true);

        HBox row1 = new HBox(10, startButton, stopButton, resetButton);
        HBox row2 = new HBox(12, new Label("Delay(ms):"), delaySlider, new Label("Chunk:"), chunkSlider, jitterCheck,
                finalizeCheck);
        HBox.setHgrow(delaySlider, Priority.ALWAYS);

        VBox box = new VBox(8, new Separator(), row1, row2, statusLabel);
        box.setPadding(new Insets(10));
        return box;
    }

    private void reset() {
        stopStreaming();
        sourceArea.clear();
        outputBox.getChildren().clear();
        statusLabel.setText("");

        content = loadContent();
        index = 0;

        JavaFxStreamRenderer renderer = new JavaFxStreamRenderer(outputBox);
        renderer.setOnLinkClick(url -> getHostServices().showDocument(url));

        StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(renderer);
        CoreExtension.addDefaults(builder);
        parser = builder.build();

        updateStatus();
    }

    private void startStreaming(ScrollPane scrollPane) {
        if (executor != null) {
            return;
        }
        startButton.setDisable(true);
        stopButton.setDisable(false);
        resetButton.setDisable(true);

        Random random = new Random();
        lastProgressNanos = System.nanoTime();
        lastProgressIndex = index;

        ThreadFactory threadFactory = r -> {
            Thread t = new Thread(r, "typewriter-stream-thread");
            t.setDaemon(true);
            streamThread = t;
            return t;
        };
        executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "typewriter-watchdog-thread");
            t.setDaemon(true);
            return t;
        });

        Runnable tick = new Runnable() {
            @Override
            public void run() {
                try {
                    if (index >= content.length()) {
                        if (finalizeEnabled) {
                            pushChunk("\n\n");
                        }
                        parser.close();
                        stopStreaming();
                        Platform.runLater(() -> {
                            startButton.setDisable(false);
                            resetButton.setDisable(false);
                            updateStatus();
                        });
                        return;
                    }

                    int chunkSize = Math.max(1, chunkSizeSetting);
                    int remaining = content.length() - index;
                    if (chunkSize > remaining) {
                        chunkSize = remaining;
                    }

                    String chunk = content.substring(index, index + chunkSize);
                    index += chunkSize;
                    lastProgressNanos = System.nanoTime();
                    lastProgressIndex = index;
                    pushChunk(chunk);

                    long baseDelay = Math.max(1, delayMsSetting);
                    long delay = baseDelay;
                    if (jitterEnabled) {
                        long jitter = (long) (baseDelay * 0.6);
                        delay = Math.max(1, baseDelay - jitter + random.nextInt((int) (2 * jitter + 1)));
                    }
                    executor.schedule(this, delay, TimeUnit.MILLISECONDS);
                } catch (Throwable t) {
                    t.printStackTrace();
                    Platform.runLater(() -> statusLabel.setText("Streaming error: " + t.getClass().getSimpleName()
                            + (t.getMessage() == null ? "" : (": " + t.getMessage()))));
                    stopStreaming();
                }
            }
        };

        executor.schedule(tick, 0, TimeUnit.MILLISECONDS);
        watchdog.scheduleWithFixedDelay(this::watchdogTick, 500, 500, TimeUnit.MILLISECONDS);

        outputBox.heightProperty().addListener((observable, oldValue, newValue) -> scrollPane.setVvalue(1.0));
    }

    private void watchdogTick() {
        if (executor == null) {
            return;
        }
        long elapsedMs = (System.nanoTime() - lastProgressNanos) / 1_000_000L;
        if (elapsedMs < 2000) {
            return;
        }
        Thread t = streamThread;
        StackTraceElement[] stack = t == null ? new StackTraceElement[0] : t.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append("Streaming stalled for ").append(elapsedMs).append("ms at index=").append(lastProgressIndex);
        if (lastProgressIndex >= 0 && content != null && lastProgressIndex < content.length()) {
            int end = Math.min(content.length(), lastProgressIndex + 80);
            sb.append("\nNext: ").append(content.substring(lastProgressIndex, end).replace("\n", "\\n")
                    .replace("\r", "\\r"));
        }
        if (stack.length > 0) {
            sb.append("\nThread: ").append(t.getName());
            int limit = Math.min(stack.length, 12);
            for (int i = 0; i < limit; i++) {
                sb.append("\n  at ").append(stack[i]);
            }
        }
        String msg = sb.toString();
        System.err.println(msg);
        Platform.runLater(() -> statusLabel.setText(msg));
        lastProgressNanos = System.nanoTime();
    }

    private void pushChunk(String chunk) {
        try {
            parser.push(chunk);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            sourceArea.appendText(chunk);
            updateStatus();
        });
    }

    private void stopStreaming() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        if (watchdog != null) {
            watchdog.shutdownNow();
            watchdog = null;
        }
        Platform.runLater(() -> {
            stopButton.setDisable(true);
            startButton.setDisable(false);
            resetButton.setDisable(false);
        });
    }

    private void updateStatus() {
        int total = content == null ? 0 : content.length();
        int done = Math.min(index, total);
        statusLabel.setText("Progress: " + done + " / " + total + " chars"
                + " | estimated duration ≈ " + estimateDuration(done, total));
    }

    private String estimateDuration(int done, int total) {
        if (total <= 0) {
            return "-";
        }
        double avgDelay = delaySlider.getValue();
        double avgChunk = Math.max(1.0, chunkSlider.getValue());
        double ticks = (total - done) / avgChunk;
        long ms = (long) Math.round(ticks * avgDelay);
        Duration d = Duration.ofMillis(ms);
        long seconds = d.toSeconds();
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long rem = seconds % 60;
        return minutes + "m " + rem + "s";
    }

    private String loadContent() {
        String base = null;
        try (var is = getClass().getResourceAsStream("/template.md")) {
            if (is != null) {
                base = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
        }

        if (base == null || base.isBlank()) {
            base = ""
                    + "# Typewriter Preview\n\n"
                    + "这是一段很长的文本，用于验证“没有换行时也能持续渲染”的打字机效果。"
                    + "你会看到右侧渲染区域在每个 chunk 到来时都会更新，而不是等到遇到换行或空行才出现。\n\n"
                    + "Inline: **bold**, _italic_, `code`, [link](https://example.com).\n\n"
                    + "End.";
        }

        String[] parts = base.split("\\R\\R", 2);
        String longParagraph = "这段内容刻意不包含换行："
                + "支持 **强调**、_斜体_、`代码` 与 [链接](https://example.com) 的实时增量渲染，"
                + "直到最后我们才会送入换行，以确保你能明显看到预览刷新效果。";

        return parts[0] + "\n\n" + longParagraph + "\n\n" + (parts.length > 1 ? parts[1] : "");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
