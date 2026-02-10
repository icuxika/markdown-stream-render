package com.icuxika.markdown.stream.render.demo.javafx;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;
import com.icuxika.markdown.stream.render.javafx.renderer.MarkdownListCell;
import com.icuxika.markdown.stream.render.javafx.renderer.VirtualJavaFxStreamRenderer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Virtual Stream Render Demo.
 * <p>
 * A combination of StreamRenderDemo (LLM/Typewriter simulation) and
 * VirtualListDemo (Virtualized ListView).
 * Demonstrates smooth streaming experience with virtualization.
 * </p>
 */
public class VirtualStreamRenderDemo extends Application {

	private StreamMarkdownParser parser;
	private VirtualJavaFxStreamRenderer streamRenderer; // Added field
	private ScheduledExecutorService executor;
	private TextArea logArea;
	private ListView<com.icuxika.markdown.stream.render.core.ast.Heading> tocList;
	private ListView<Node> markdownListView;
	private ObservableList<Node> markdownNodes;
	private Button fastButton;
	private Button slowButton;

	private VBox activeStreamBox;

	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(10));

		// 1. Markdown Content Area
		// Main container holding History (ListView) and Active Stream (VBox)
		VBox contentArea = new VBox();
		contentArea.setStyle("-fx-background-color: transparent;");
		VBox.setVgrow(contentArea, Priority.ALWAYS);

		// History (Virtualized)
		markdownNodes = FXCollections.observableArrayList();
		markdownListView = new ListView<>(markdownNodes);
		VBox.setVgrow(markdownListView, Priority.ALWAYS);
		markdownListView.setStyle(
				"-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-padding: 0;");
		markdownListView.setCellFactory(param -> new MarkdownListCell());

		// Active Stream Container
		activeStreamBox = new VBox();
		activeStreamBox.setStyle("-fx-background-color: transparent; -fx-padding: 0 10px;"); // Align padding with list
																								// cells
		// Ensure it doesn't grow infinitely, but wraps content
		activeStreamBox.setFillWidth(true);

		// Wrap activeStreamBox in a ScrollPane to prevent it from affecting the
		// ListView scrollbar layout
		// Or better, keep it as is but ensure it doesn't cause layout jitter.
		// The jitter happens because VBox size changes rapidly.
		// Since activeStreamBox is below ListView, its height change might affect
		// scrollbar if not handled.
		// BUT, in VBox (contentArea), ListView takes VGrow.ALWAYS. activeStreamBox
		// takes remaining space?
		// No, VBox stacks them. ListView takes available space, activeStreamBox takes
		// its preferred height.
		// If activeStreamBox grows, ListView shrinks. This causes scrollbar to jump.

		// FIX: Use a StackPane or AnchorPane to overlay activeStreamBox? No, it must be
		// below.
		// FIX: Use a transparent placeholder in ListView? No.
		// FIX: The issue is likely that the scrollbar belongs to ListView.
		// When activeStreamBox grows, ListView height shrinks, causing scrollbar to
		// resize/move.

		// To fix "scrollbar jitter" (visual noise):
		// We can put activeStreamBox INSIDE the ListView as a footer?
		// ListView doesn't support footers natively.

		// Alternative: Make activeStreamBox overlay the bottom of ListView?
		// No, that covers content.

		// Simple fix for "red box moving":
		// The user mentioned "red box" which implies debug borders or selection?
		// "red box内的临时组件一直在动" -> "temporary components in red box are moving".
		// "右侧滚动条的位置...看起来很乱" -> "Right scrollbar position ... looks messy".

		// If the scrollbar is on the ListView, and ListView resizes, the scrollbar
		// resizes.
		// If we want a stable scrollbar, we should put the ScrollBar on the CONTAINER
		// (contentArea).
		// But ListView has its own scrollbar.

		// Maybe we can wrap both in a ScrollPane and make ListView non-scrollable
		// (height = prefHeight)?
		// No, that defeats virtualization.

		// Correct approach for chat-like interface:
		// The "Active" part should be part of the scrollable area.
		// Since we can't easily add it to ListView, we might accept some jitter OR:
		// Use a custom cell for the "active" item?
		// VirtualJavaFxStreamRenderer currently separates them.

		// Let's try to stabilize the layout by giving activeStreamBox a fixed minimum
		// height or
		// ensuring the transition doesn't cause massive layout shifts.
		// But "temporary components moving" suggests the internal layout of
		// activeStreamBox is updating.

		// If the user means the scrollbar of the ListView is jumping up and down:
		// This is because activeStreamBox changes height -> ListView changes height.
		// Solution: Overlay activeStreamBox at the bottom of the StackPane, with a
		// transparent background?
		// And add padding to ListView bottom?

		// Let's try the Overlay approach:
		// StackPane
		// Layer 1: ListView (with bottom padding equal to max active height)
		// Layer 2: ActiveBox (aligned to bottom, consuming mouse events?)

		// BUT ActiveBox height is variable.

		// Let's stick to VBox but maybe remove the "red box" if it was a debug
		// artifact?
		// The user said "red box", maybe I left a border style somewhere?
		// I don't see red border in my code. Maybe it's default focus?
		// "临时组件" -> Temporary component.

		// Let's assume the user means the activeStreamBox itself.
		// To stabilize the scrollbar, we can try to make the activeStreamBox PART of
		// the ListView.
		// But that requires mixed cell types and is complex.

		// Let's try to minimize the impact.
		// Actually, if we use a BorderPane for contentArea:
		// Center: ListView
		// Bottom: ActiveBox
		// This is what we have (VBox).

		// If we want a single scrollbar for both, we can't use ListView virtualization
		// easily for the active part.

		// Let's try to make the ActiveBox NOT push the ListView.
		// We can put them in a StackPane.
		// ListView takes full space.
		// ActiveBox is at the bottom, with a solid background (or transparent).
		// But then ActiveBox covers the last items of ListView.
		// We can auto-scroll ListView such that the last item is above ActiveBox.

		// Let's switch to StackPane layout.
		// This keeps ListView size constant (full height), so scrollbar is stable.
		// We just need to ensure we scroll enough to see content above the ActiveBox.

		javafx.scene.layout.StackPane stackContent = new javafx.scene.layout.StackPane();
		stackContent.setStyle("-fx-background-color: transparent;");
		VBox.setVgrow(stackContent, Priority.ALWAYS);

		markdownListView = new ListView<>(markdownNodes);
		markdownListView.setStyle(
				"-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-padding: 0;");
		markdownListView.setCellFactory(param -> new MarkdownListCell());

		activeStreamBox = new VBox();
		activeStreamBox.setStyle(
				"-fx-background-color: rgba(255, 255, 255, 0.8); -fx-padding: 10px; -fx-background-radius: 5px;");
		activeStreamBox.setFillWidth(true);
		activeStreamBox.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE); // Don't stretch

		// Align ActiveBox to bottom
		javafx.scene.layout.StackPane.setAlignment(activeStreamBox, javafx.geometry.Pos.BOTTOM_CENTER);
		javafx.scene.layout.StackPane.setMargin(activeStreamBox, new Insets(0, 0, 10, 0)); // Some margin

		// Add to stack: ListView first (behind), then ActiveBox (front)
		stackContent.getChildren().addAll(markdownListView, activeStreamBox);

		// We need to add padding to the bottom of ListView so content isn't hidden by
		// ActiveBox?
		// Or we just rely on scrollToBottom.
		// When ActiveBox is present, it might cover the last item.
		// We can add a "Spacer" node to the list? No, model should be pure.
		// We can set ListView padding?
		markdownListView.setPadding(new Insets(0, 0, 200, 0)); // Large bottom padding to accommodate ActiveBox

		contentArea.getChildren().add(stackContent);

		// 2. Sidebar TOC
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
				// Scroll to item in markdownListView
				// We need to find the index of this heading node in markdownNodes
				// Note: Heading node in TOC is the SAME object as in markdownNodes?
				// Yes, parser yields AST nodes.
				// However, Heading might be wrapped or be a child of Document.
				// VirtualJavaFxStreamRenderer adds top-level nodes to the list.
				// Heading IS a top-level node usually.
				int index = markdownNodes.indexOf(selected);
				if (index >= 0) {
					markdownListView.scrollTo(index);
					markdownListView.getSelectionModel().select(index);
				}
			}
		});

		markdownNodes.addListener((javafx.collections.ListChangeListener.Change<? extends Node> c) -> {
			while (c.next()) {
				if (c.wasAdded()) {
					for (Node node : c.getAddedSubList()) {
						if (node instanceof com.icuxika.markdown.stream.render.core.ast.Heading) {
							tocList.getItems().add((com.icuxika.markdown.stream.render.core.ast.Heading) node);
						}
					}
				}
			}
		});

		SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(tocList, contentArea);
		splitPane.setDividerPositions(0.25);
		root.setCenter(splitPane);

		// 3. Control Area
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

		// Actions
		fastButton.setOnAction(e -> {
			disableButtons(true, fastButton, slowButton);
			markdownNodes.clear();
			tocList.getItems().clear();
			logArea.clear();
			// LLM style: 10-50ms delay, 2-10 chars chunk
			startStreaming(10, 50, 2, 10);
		});

		slowButton.setOnAction(e -> {
			disableButtons(true, fastButton, slowButton);
			markdownNodes.clear();
			tocList.getItems().clear();
			logArea.clear();
			// Typewriter style: 50-150ms delay, 1 char chunk
			startStreaming(50, 150, 1, 1);
		});

		Scene scene = new Scene(root, 1000, 800);

		// CSS for ListView transparency
		scene.getStylesheets().add("data:text/css,"
				+ ".list-cell:filled:selected, .list-cell:filled:selected:focused { "
				+ "-fx-background-color: transparent; "
				+ "-fx-text-fill: inherit; "
				+ "-fx-border-color: transparent; } "
				+ ".list-cell { -fx-background-color: transparent; -fx-padding: 0px; }");

		MarkdownTheme theme = new MarkdownTheme();
		theme.apply(scene);

		primaryStage.setTitle("Markdown Virtualized Stream Demo");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void initParser() {
		// Setup Virtual Renderer with Hybrid Mode
		streamRenderer = new VirtualJavaFxStreamRenderer(
				markdownNodes,
				activeStreamBox,
				() -> {
					// Scroll Request Callback
					Platform.runLater(() -> {
						// Scroll to the absolute bottom (past the list view, to the active box)
						// Since ListView is inside VBox, we can't easily scroll the VBox unless it's in
						// a ScrollPane.
						// BUT, the ListView takes all space.

						// WAIT: If ListView grows, activeBox is pushed down.
						// If contentArea height is fixed (by SplitPane), ListView takes available
						// space.
						// ActiveBox will be at bottom.
						// We need to make sure ListView scrolls to its bottom.
						if (!markdownNodes.isEmpty()) {
							markdownListView.scrollTo(markdownNodes.size() - 1);
						}
					});
				},
				// Link handler
				url -> getHostServices().showDocument(url));

		// Setup TOC listener (same as before)
		// VirtualJavaFxStreamRenderer doesn't support listeners like
		// 'onHeadingRendered' directly yet.
		// We can add a wrapper or modify VirtualJavaFxStreamRenderer.
		// Or, since we have the list, we can listen to list changes?
		// But list contains generic Nodes. We'd have to check type.

		// Let's add a ListChangeListener to markdownNodes
		// Note: This might be called frequently.
		// A better way is to enhance VirtualJavaFxStreamRenderer or use a Proxy.
		// For this demo, let's just inspect nodes as they are added.

		// Actually, let's subclass VirtualJavaFxStreamRenderer anonymously to intercept
		// openBlock?
		// No, renderNode is where it's finalized?
		// openBlock is called when block starts.

		// Let's just monitor the observable list.
		// It's efficient enough for TOC updates.

		/*
		 * markdownNodes.addListener((javafx.collections.ListChangeListener.Change<?
		 * extends Node> c) -> {
		 * while (c.next()) {
		 * if (c.wasAdded()) {
		 * for (Node node : c.getAddedSubList()) {
		 * if (node instanceof com.icuxika.markdown.stream.render.core.ast.Heading) {
		 * tocList.getItems().add((com.icuxika.markdown.stream.render.core.ast.Heading)
		 * node);
		 * }
		 * }
		 * }
		 * }
		 * });
		 */
		// The listener above causes ConcurrentModificationException or similar if not
		// careful with threading?
		// ObservableList events fire on the thread that modified it.
		// Our renderer modifies it on FX thread (via runLater batching).
		// So it's safe.

		// HOWEVER, we need to clear the listener when re-initializing?
		// Easier to add listener ONCE in start().

		StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(streamRenderer);
		parser = builder.build();
	}

	private void disableButtons(boolean disable, Button... buttons) {
		for (Button b : buttons) {
			b.setDisable(disable);
		}
	}

	private void startStreaming(int minDelay, int maxDelay, int minChunk, int maxChunk) {
		// Reuse content loading logic from StreamRenderDemo
		String content = "";
		try (java.io.InputStream is = getClass().getResourceAsStream("/comprehensive.md")) {
			if (is != null) {
				content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
			} else {
				// Fallback
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

		// Add listener for TOC (clear old one first if possible, but here we just
		// re-add logic)
		// Actually better to add listener to list once in start()
		// Let's do that in start() instead.

		executor = Executors.newSingleThreadScheduledExecutor();

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

					try {
						parser.push(chunk);
					} catch (Exception e) {
						e.printStackTrace();
					}

					int delay = random.nextInt(maxDelay - minDelay + 1) + minDelay;
					executor.schedule(this, delay, TimeUnit.MILLISECONDS);
				} else {
					log("Stream Closed");
					parser.close();
					if (streamRenderer != null) {
						streamRenderer.finish();
					}
					executor.shutdown();
					Platform.runLater(() -> {
						log("Done. Restart app to run again.");
						// disableButtons(false, fastButton, slowButton);
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
