package com.icuxika.markdown.stream.render.demo.javafx;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;
import com.icuxika.markdown.stream.render.javafx.renderer.MarkdownListCell;
import com.icuxika.markdown.stream.render.javafx.renderer.VirtualJavaFxStreamRenderer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Virtual List Demo.
 * <p>
 * Demonstrates high-performance rendering of large Markdown documents using
 * JavaFX ListView virtualization.
 * </p>
 */
public class VirtualListDemo extends Application {

	private StreamMarkdownParser parser;
	private ScheduledExecutorService executor;
	private ListView<Node> listView;
	private ObservableList<Node> markdownNodes;
	private Label statusLabel;
	private VBox activeStreamBox;

	private VirtualJavaFxStreamRenderer streamRenderer;

	@Override
	public void start(Stage primaryStage) {
		// 1. Data Model
		markdownNodes = FXCollections.observableArrayList();

		// 2. Layout & Containers
		javafx.scene.layout.StackPane stackContent = new javafx.scene.layout.StackPane();
		stackContent.setStyle("-fx-background-color: transparent;");

		// ListView (History)
		listView = new ListView<>(markdownNodes);
		VBox.setVgrow(listView, Priority.ALWAYS);
		listView.setStyle(
				"-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-padding: 0;");
		// Add bottom padding to ListView to avoid content being hidden by
		// activeStreamBox
		listView.setPadding(new javafx.geometry.Insets(0, 0, 200, 0));

		// 3. Custom Cell Factory
		listView.setCellFactory(param -> new MarkdownListCell());

		// Active Stream Box (Overlay at bottom)
		activeStreamBox = new VBox();
		activeStreamBox.setStyle(
				"-fx-background-color: rgba(255, 255, 255, 0.8); -fx-padding: 10px; -fx-background-radius: 5px;");
		activeStreamBox.setFillWidth(true);
		activeStreamBox.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

		// Align ActiveBox to bottom
		javafx.scene.layout.StackPane.setAlignment(activeStreamBox, javafx.geometry.Pos.BOTTOM_CENTER);
		javafx.scene.layout.StackPane.setMargin(activeStreamBox, new javafx.geometry.Insets(0, 0, 10, 0));

		stackContent.getChildren().addAll(listView, activeStreamBox);

		// 4. Controls
		Button startBtn = new Button("Start Stress Test (1000 Sections)");
		startBtn.setOnAction(e -> startStressTest());

		statusLabel = new Label("Ready");

		// 5. Layout
		VBox topBox = new VBox(10, startBtn, statusLabel);
		topBox.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");

		BorderPane root = new BorderPane();
		root.setTop(topBox);
		root.setCenter(stackContent);

		Scene scene = new Scene(root, 1000, 800);

		// Disable selection visual feedback via CSS
		scene.getStylesheets().add("data:text/css,"
				+ ".list-cell:filled:selected, .list-cell:filled:selected:focused { "
				+ "-fx-background-color: transparent; "
				+ "-fx-text-fill: inherit; "
				+ "-fx-border-color: transparent; } "
				+ ".list-cell { -fx-background-color: transparent; -fx-padding: 0px; }");

		// Apply theme (loads CSS)
		MarkdownTheme theme = new MarkdownTheme();
		theme.apply(scene);

		primaryStage.setTitle("Markdown Virtualization Demo (ListView)");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void initParser() {
		// Setup Parser
		streamRenderer = new VirtualJavaFxStreamRenderer(markdownNodes, activeStreamBox, () -> {
			// Request refresh of visible cells
			// Note: We don't need to auto-scroll in stress test usually, but for demo it's
			// nice.
			// However, auto-scrolling can impact performance measurement.
			Platform.runLater(() -> {
				// listView.refresh();
				// Optional: Auto-scroll
				if (!markdownNodes.isEmpty()) {
					listView.scrollTo(markdownNodes.size() - 1);
				}
			});
		});

		StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(streamRenderer);
		parser = builder.build();
	}

	private void startStressTest() {
		markdownNodes.clear();
		initParser();

		statusLabel.setText("Generating content...");

		// Load content from file instead of generating synthetic data
		String content = "";
		try (java.io.InputStream is = getClass().getResourceAsStream("/huge_stress_test.md")) {
			if (is != null) {
				content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
			} else {
				// Fallback to comprehensive.md if huge one not found
				try (java.io.InputStream is2 = getClass().getResourceAsStream("/comprehensive.md")) {
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

		statusLabel.setText("Streaming content...");

		if (executor != null && !executor.isShutdown()) {
			executor.shutdownNow();
		}
		executor = Executors.newSingleThreadScheduledExecutor();

		// Simulate very fast network stream
		Runnable task = new Runnable() {
			int index = 0;
			final int CHUNK_SIZE = 1024; // 1KB chunks

			@Override
			public void run() {
				try {
					if (index < finalContent.length()) {
						int end = Math.min(index + CHUNK_SIZE, finalContent.length());
						String chunk = finalContent.substring(index, end);
						index = end;

						parser.push(chunk);

						Platform.runLater(
								() -> statusLabel
										.setText("Processed: " + index + " / " + finalContent.length() + " chars"));

						// Very short delay to simulate network but keep it fast
						executor.schedule(this, 1, TimeUnit.MILLISECONDS);
					} else {
						parser.close();
						if (streamRenderer != null) {
							streamRenderer.finish();
						}
						Platform.runLater(() -> statusLabel.setText("Done! Items: " + markdownNodes.size()));
						executor.shutdown();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		executor.schedule(task, 0, TimeUnit.MILLISECONDS);
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
