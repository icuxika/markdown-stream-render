package com.icuxika.markdown.stream.render.javafx.renderer;

import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.renderer.StreamMarkdownTypingRenderer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

/**
 * Hybrid Virtual JavaFX Stream Renderer.
 * <p>
 * Combines "Virtualized List" (for history) and "Active Stream Container" (for
 * current block).
 * </p>
 * <p>
 * This renderer serializes ALL UI operations onto the JavaFX Application Thread
 * to ensure strict ordering and avoid concurrency issues between the Parser
 * thread and the UI thread.
 * </p>
 */
public class VirtualJavaFxStreamRenderer implements StreamMarkdownTypingRenderer {

	private final ObservableList<Node> historyItems;
	private final VBox activeContainer;
	private final Runnable requestScrollToBottom;
	private final Consumer<String> linkHandler;

	// Context for the currently active top-level block
	private static class ActiveBlockContext {
		final Node node;
		final VBox container;
		final JavaFxStreamRenderer renderer;

		ActiveBlockContext(Node node, VBox container, JavaFxStreamRenderer renderer) {
			this.node = node;
			this.container = container;
			this.renderer = renderer;
		}
	}

	// Only accessed on FX Thread
	private ActiveBlockContext currentContext;

	// Single queue for ALL UI tasks to maintain order
	private final Queue<Runnable> uiTaskQueue = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean isUiTaskScheduled = new AtomicBoolean(false);

	public VirtualJavaFxStreamRenderer(ObservableList<Node> historyItems, VBox activeContainer,
			Runnable requestScrollToBottom) {
		this(historyItems, activeContainer, requestScrollToBottom, null);
	}

	public VirtualJavaFxStreamRenderer(ObservableList<Node> historyItems, VBox activeContainer,
			Runnable requestScrollToBottom, Consumer<String> linkHandler) {
		this.historyItems = historyItems;
		this.activeContainer = activeContainer;
		this.requestScrollToBottom = requestScrollToBottom;
		this.linkHandler = linkHandler;

		// Initialize: If container is empty, hide it to prevent visual artifacts
		// (background, padding)
		Platform.runLater(this::updateActiveContainerVisibility);
	}

	// --- Public Interface (Called by Parser Thread) ---

	@Override
	public void openBlock(Node node) {
		if (node instanceof Document) {
			return;
		}
		scheduleUiTask(() -> openBlockOnFxThread(node));
	}

	@Override
	public void renderNode(Node node) {
		scheduleUiTask(() -> renderNodeOnFxThread(node));
	}

	@Override
	public void renderPreviewNode(Node node) {
		scheduleUiTask(() -> renderPreviewNodeOnFxThread(node));
	}

	@Override
	public void clearPreview() {
		scheduleUiTask(this::clearPreviewOnFxThread);
	}

	@Override
	public void closeBlock(Node node) {
		scheduleUiTask(() -> closeBlockOnFxThread(node));
	}

	// --- Task Scheduling ---

	private void scheduleUiTask(Runnable task) {
		uiTaskQueue.offer(task);
		if (isUiTaskScheduled.compareAndSet(false, true)) {
			Platform.runLater(this::processUiTasks);
		}
	}

	private void processUiTasks() {
		isUiTaskScheduled.set(false);
		Runnable task;

		// Time slicing: Process tasks for max 8ms per frame to maintain 60fps (16ms
		// total)
		long startTime = System.nanoTime();
		long maxDuration = 8_000_000; // 8ms

		while ((task = uiTaskQueue.poll()) != null) {
			task.run();

			// Check time limit
			if (System.nanoTime() - startTime > maxDuration) {
				// Time budget exceeded, schedule remaining tasks for next pulse
				if (!uiTaskQueue.isEmpty()) {
					if (isUiTaskScheduled.compareAndSet(false, true)) {
						Platform.runLater(this::processUiTasks);
					}
					break;
				}
			}
		}

		// If queue is empty, perform final cleanup check
		if (uiTaskQueue.isEmpty()) {
			checkAndCleanupOrphanedContext();
		}

		if (requestScrollToBottom != null) {
			requestScrollToBottom.run();
		}
	}

	private void checkAndCleanupOrphanedContext() {
		// If we have finished processing all tasks, but there is still an active
		// context,
		// and it seems like the stream has ended (no more tasks coming for a while),
		// we might want to force close it?
		// No, the parser might just be slow.

		// BUT, if the stream is truly done (e.g. parser.close() called), the parser
		// should have sent closeBlock.
		// If the parser logic is buggy or the structure is malformed, closeBlock might
		// be missed.

		// We can't know for sure if the stream ended here.
		// However, we can expose a `finish()` method on this renderer to be called when
		// stream ends.
	}

	private void updateActiveContainerVisibility() {
		if (activeContainer.getChildren().isEmpty()) {
			activeContainer.setVisible(false);
			activeContainer.setManaged(false);
		} else {
			activeContainer.setVisible(true);
			activeContainer.setManaged(true);
		}
	}

	/**
	 * Should be called when the stream is completely finished.
	 * Ensures any remaining active content is moved to history.
	 */
	public void finish() {
		scheduleUiTask(() -> {
			if (currentContext != null) {
				// Force close context
				Node orphanedNode = currentContext.node;
				VBox orphanedContainer = currentContext.container;
				historyItems.add(orphanedNode);
				activeContainer.getChildren().remove(orphanedContainer);
				currentContext = null;
			}
			// Final check for visibility
			updateActiveContainerVisibility();
		});
	}

	// --- FX Thread Logic ---

	private void openBlockOnFxThread(Node node) {
		if (node.getParent() instanceof Document) {
			// Safety: If there is an existing context, force close it to prevent leaks.
			// This happens if the parser misses a closeBlock call or structure is
			// malformed.
			if (currentContext != null) {
				// Force close previous context
				Node orphanedNode = currentContext.node;
				VBox orphanedContainer = currentContext.container;
				historyItems.add(orphanedNode);
				activeContainer.getChildren().remove(orphanedContainer);
				currentContext = null;
			}

			// Start of a new top-level block
			VBox itemContainer = new VBox();
			activeContainer.getChildren().add(itemContainer);

			// Ensure active container is visible when we add content
			updateActiveContainerVisibility();

			JavaFxStreamRenderer renderer = new JavaFxStreamRenderer(itemContainer);
			if (linkHandler != null) {
				renderer.setOnLinkClick(linkHandler);
			}

			currentContext = new ActiveBlockContext(node, itemContainer, renderer);
			currentContext.renderer.openBlock(node);
		} else {
			// Child block
			if (currentContext != null) {
				currentContext.renderer.openBlock(node);
			}
		}
	}

	private void renderNodeOnFxThread(Node node) {
		if (currentContext != null) {
			currentContext.renderer.renderNode(node);
		}
	}

	private void renderPreviewNodeOnFxThread(Node node) {
		if (currentContext != null) {
			currentContext.renderer.renderPreviewNode(node);
		}
	}

	private void clearPreviewOnFxThread() {
		if (currentContext != null) {
			currentContext.renderer.clearPreview();
		}
	}

	private void closeBlockOnFxThread(Node node) {
		if (currentContext != null) {
			currentContext.renderer.closeBlock(node);

			if (node == currentContext.node) {
				// Top-level block finished
				final Node finalNode = node;
				final VBox containerToRemove = currentContext.container;

				// 1. Add to history
				historyItems.add(finalNode);

				// 2. Remove from active area
				activeContainer.getChildren().remove(containerToRemove);

				// Hide active container if it becomes empty
				updateActiveContainerVisibility();

				// Clear context
				currentContext = null;
			}
		}
	}
}
