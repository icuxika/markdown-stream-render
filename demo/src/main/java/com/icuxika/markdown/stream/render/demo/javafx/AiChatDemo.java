package com.icuxika.markdown.stream.render.demo.javafx;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.demo.client.DeepSeekClient;
import com.icuxika.markdown.stream.render.javafx.MarkdownStyles;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxStreamRenderer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * AI Chat Demo using JavaFX Renderer.
 */
public class AiChatDemo extends Application {

	private final ScrollPane chatScrollPane = new ScrollPane();
	private final VBox chatBox = new VBox();
	private final TextArea inputArea = new TextArea();
	private final Button sendButton = new Button("Send");
	private DeepSeekClient client;
	private boolean isDarkMode = false;
	private final java.util.List<DeepSeekClient.ChatMessage> history = new java.util.ArrayList<>();

	private final CheckBox mockModeCbx = new CheckBox("Mock Mode");
	private String apiKey;

	/**
	 * Main entry point.
	 *
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		apiKey = System.getenv("DEEPSEEK_API_KEY");
		if (apiKey == null || apiKey.isBlank()) {
			apiKey = "YOUR_API_KEY_HERE";
			System.out.println("Warning: DEEPSEEK_API_KEY environment variable not set. Defaulting to Mock Mode.");
			mockModeCbx.setSelected(true);
		}

		recreateClient();
		mockModeCbx.setOnAction(e -> recreateClient());

		history.add(new DeepSeekClient.ChatMessage("system", "You are a helpful assistant."));

		// Setup ScrollPane and ChatBox
		chatBox.setPadding(new Insets(10));
		chatBox.setSpacing(10);
		chatBox.setStyle("-fx-background-color: transparent;");

		chatScrollPane.setContent(chatBox);
		chatScrollPane.setFitToWidth(true);
		chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		chatScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

		// Auto-scroll logic
		chatBox.heightProperty().addListener((observable, oldValue, newValue) -> {
			chatScrollPane.setVvalue(1.0);
		});

		// Input Area
		inputArea.setPromptText("Type a message... (Enter to send, Shift+Enter for new line)");
		inputArea.setPrefRowCount(1);
		inputArea.setWrapText(true);
		inputArea.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
				e.consume();
				sendMessage();
			}
		});
		HBox.setHgrow(inputArea, Priority.ALWAYS);

		sendButton.getStyleClass().add("button-primary");
		sendButton.setOnAction(e -> sendMessage());

		HBox inputContainer = new HBox(10, inputArea, sendButton);
		inputContainer.getStyleClass().add("input-container");
		inputContainer.setAlignment(Pos.BOTTOM_CENTER);

		VBox bottomBox = new VBox(inputContainer);
		bottomBox.setPadding(new Insets(20));
		bottomBox.setStyle("-fx-background-color: transparent;");

		// Toolbar
		Button themeBtn = new Button("Toggle Theme");
		themeBtn.setOnAction(e -> toggleTheme(primaryStage.getScene()));

		ToolBar toolBar = new ToolBar(themeBtn, new Separator(), mockModeCbx);
		toolBar.getStyleClass().add("toolbar");

		BorderPane root = new BorderPane();
		root.setTop(toolBar);
		root.setCenter(chatScrollPane);
		root.setBottom(bottomBox);

		Scene scene = new Scene(root, 900, 700);
		MarkdownStyles.applyBase(scene, true);
		scene.getStylesheets().add(getClass().getResource("/chat-modern.css").toExternalForm());

		primaryStage.setTitle("DeepSeek Chat - AI Native UI");
		primaryStage.setScene(scene);

		// Apply initial theme variables explicitly
		applyTheme(scene);

		primaryStage.show();

		inputArea.requestFocus();
	}

	private void toggleTheme(Scene scene) {
		isDarkMode = !isDarkMode;
		applyTheme(scene);
	}

	private void applyTheme(Scene scene) {
		if (isDarkMode) {
			scene.getRoot().setStyle(
					"-color-bg: #111827; "
							+ "-color-bg-alt: #1F2937; "
							+ "-color-text: #F9FAFB; "
							+ "-color-text-muted: #9CA3AF; "
							+ "-color-border: #374151; "
							+ "-color-primary: #3B82F6; "
							+ "-chat-user-bg: #374151; "
							+ "-chat-user-text: #F9FAFB; "
							+ "-chat-assistant-text: #E5E7EB;"
							+
							// Markdown Dark Mode Overrides
							"-md-fg-color: #F9FAFB; "
							+ "-md-bg-color: transparent; "
							+ "-md-code-bg-color: #1F2937; "
							+ // Slate 800
							"-md-border-color: #374151; "
							+ "-md-quote-bg-color: #1F2937; "
							+ "-md-quote-border-color: #374151; "
							+ "-md-table-header-bg: #1F2937; "
							+ "-md-table-row-odd-bg: transparent;"
							+
							// Code Highlight Dark Mode
							"-md-code-keyword: #ff7b72;"
							+ "-md-code-string: #a5d6ff;"
							+ "-md-code-comment: #8b949e;"
							+ "-md-code-json-key: #7ee787;"
							+ "-md-code-number: #79c0ff;"
							+ "-md-code-tag: #7ee787;"
							+ "-md-code-class: #ffa657;"
							+ "-md-code-id: #d2a8ff;"
							+ "-md-code-color: #79c0ff;"
							+ "-md-code-attr: #79c0ff;");
		} else {
			scene.getRoot().setStyle(
					"-color-bg: #FFFFFF; "
							+ "-color-bg-alt: #F9FAFB; "
							+ "-color-text: #111827; "
							+ "-color-text-muted: #6B7280; "
							+ "-color-border: #E5E7EB; "
							+ "-color-primary: #2563EB; "
							+ "-chat-user-bg: #E0E7FF; "
							+ "-chat-user-text: #1E1B4B; "
							+ "-chat-assistant-text: #111827;"
							+
							// Markdown Light Mode Overrides (Force visible background)
							"-md-fg-color: #111827; "
							+ "-md-bg-color: transparent; "
							+ "-md-code-bg-color: #F1F5F9; "
							+ // Slate 100
							"-md-border-color: #E5E7EB; "
							+ "-md-quote-bg-color: #F3F4F6; "
							+ "-md-quote-border-color: #E5E7EB; "
							+ "-md-table-header-bg: #F3F4F6; "
							+ "-md-table-row-odd-bg: transparent;");
		}
	}

	private void recreateClient() {
		client = new DeepSeekClient(apiKey, mockModeCbx.isSelected());
	}

	private void sendMessage() {
		String text = inputArea.getText().trim();
		if (text.isEmpty()) {
			return;
		}

		inputArea.clear();

		// Add user message bubble
		MarkdownBubble userBubble = new MarkdownBubble();
		userBubble.configure(true);
		userBubble.resetAndSetText(text);
		chatBox.getChildren().add(userBubble);

		history.add(new DeepSeekClient.ChatMessage("user", text));
		scrollToBottom();

		// Create AI message bubble
		MarkdownBubble aiBubble = new MarkdownBubble();
		aiBubble.configure(false);
		aiBubble.resetAndSetText("");
		chatBox.getChildren().add(aiBubble);
		scrollToBottom();

		StringBuilder assistantResponse = new StringBuilder();

		client.streamChat(history, token -> {
			aiBubble.append(token);
			assistantResponse.append(token);
		}, () -> {
			aiBubble.finish();
			Platform.runLater(
					() -> history.add(new DeepSeekClient.ChatMessage("assistant", assistantResponse.toString())));
		}, error -> Platform.runLater(() -> {
			aiBubble.append("\n\n**Error**: " + error.getMessage());
			aiBubble.finish();
			scrollToBottom();
		}));
	}

	private void scrollToBottom() {
		// Force layout update and scroll
		Platform.runLater(() -> {
			chatBox.layout();
			chatScrollPane.layout();
			chatScrollPane.setVvalue(1.0);
		});
	}

	// --- Inner Classes ---

	private class MarkdownBubble extends HBox {
		private final VBox contentBox = new VBox();
		private final VBox bubbleBox;
		private JavaFxRenderer staticRenderer;
		private JavaFxStreamRenderer streamRenderer;
		private StreamMarkdownParser streamParser;
		private VBox streamRoot;
		private final Circle avatar = new Circle(16);
		private String currentText = "";

		public MarkdownBubble() {
			this.setPadding(new Insets(5));
			this.setFillHeight(false);
			this.setSpacing(10);

			contentBox.setStyle("-fx-background-color: transparent;");
			contentBox.setMaxWidth(700);

			bubbleBox = new VBox(contentBox);
			bubbleBox.setPadding(new Insets(10));

			avatar.setStroke(Color.TRANSPARENT);
		}

		public void configure(boolean isUser) {
			this.getChildren().clear();
			if (isUser) {
				this.setAlignment(Pos.CENTER_RIGHT);
				bubbleBox.getStyleClass().removeAll("bubble-user", "bubble-ai");
				bubbleBox.getStyleClass().add("bubble-user");
				ensureStaticRenderer();
				this.getChildren().add(bubbleBox);
			} else {
				this.setAlignment(Pos.TOP_LEFT);
				bubbleBox.getStyleClass().removeAll("bubble-user", "bubble-ai");
				bubbleBox.getStyleClass().add("bubble-ai");
				avatar.setFill(Color.web("#6366F1"));
				ensureStreamRenderer();
				this.getChildren().addAll(avatar, bubbleBox);
			}
		}

		public void resetAndSetText(String text) {
			this.currentText = text != null ? text : "";
			if (streamParser != null) {
				resetStream();
				if (!currentText.isEmpty()) {
					streamParser.push(currentText);
					streamParser.close();
				}
			} else {
				renderStatic();
			}
		}

		public void append(String token) {
			if (token != null) {
				if (streamParser != null) {
					streamParser.push(token);
				} else {
					this.currentText += token;
					renderStatic();
				}
			}
		}

		public void finish() {
			if (streamParser != null) {
				streamParser.close();
			}
		}

		private void ensureStaticRenderer() {
			if (staticRenderer == null) {
				staticRenderer = new JavaFxRenderer();
				staticRenderer.setOnLinkClick(url -> AiChatDemo.this.getHostServices().showDocument(url));
			}
			streamParser = null;
			streamRenderer = null;
			streamRoot = null;
			contentBox.getChildren().setAll(staticRenderer.getRoot());
		}

		private void ensureStreamRenderer() {
			if (streamRoot == null) {
				streamRoot = new VBox();
				streamRoot.setStyle("-fx-background-color: transparent;");
			} else {
				streamRoot.getChildren().clear();
			}
			streamRenderer = new JavaFxStreamRenderer(streamRoot);
			streamRenderer.setOnLinkClick(url -> AiChatDemo.this.getHostServices().showDocument(url));
			contentBox.getChildren().setAll(streamRoot);
			resetStream();
		}

		private void resetStream() {
			StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(streamRenderer);
			CoreExtension.addDefaults(builder);
			streamParser = builder.build();
		}

		private void renderStatic() {
			ensureStaticRenderer();
			staticRenderer.clear();
			if (currentText.isEmpty()) {
				return;
			}

			MarkdownParser.Builder builder = MarkdownParser.builder();
			CoreExtension.addDefaults(builder);
			MarkdownParser parser = builder.build();
			Document doc = parser.parse(currentText);

			staticRenderer.render(doc);
		}
	}
}
