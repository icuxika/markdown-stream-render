package com.icuxika.markdown.stream.render.demo.javafx.modernchat;

import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxStreamRenderer;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ChatArea extends ScrollPane {

	private static final double MAX_CONTENT_WIDTH = 900;
	private static final double SPACING = 24;
	private static final double PADDING = 30;

	private final VBox messagesContainer;
	private JavaFxStreamRenderer currentStreamRenderer;
	private StreamMarkdownParser currentStreamParser;
	private VBox currentStreamRoot;
	private Consumer<String> linkHandler;

	public ChatArea() {
		messagesContainer = new VBox();
		initializeUI();
		loadWelcomeMessages();
	}

	private void initializeUI() {
		this.getStyleClass().add("chat-area");
		this.setFitToWidth(true);
		this.setHbarPolicy(ScrollBarPolicy.NEVER);
		this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

		messagesContainer.getStyleClass().add("messages-container");
		messagesContainer.setSpacing(SPACING);
		messagesContainer.setPadding(new Insets(PADDING));
		messagesContainer.setMaxWidth(MAX_CONTENT_WIDTH);

		this.setContent(messagesContainer);
		VBox.setVgrow(this, Priority.ALWAYS);

		messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
			Platform.runLater(() -> this.setVvalue(1.0));
		});
	}

	private void loadWelcomeMessages() {
		MessageComponents.AiMessage welcomeMsg = new MessageComponents.AiMessage();
		welcomeMsg.setContent("Hello! I'm your AI assistant. How can I help you today?\n\n" +
				"I can help you with:\n" +
				"- **Writing and editing** documents\n" +
				"- **Code review** and debugging\n" +
				"- **Research** and analysis\n" +
				"- **Creative brainstorming**\n\n" +
				"Feel free to ask me anything!");
		if (linkHandler != null) {
			welcomeMsg.setLinkHandler(linkHandler);
		}
		messagesContainer.getChildren().add(welcomeMsg);

		MessageComponents.StepCards stepCards = new MessageComponents.StepCards();
		messagesContainer.getChildren().add(stepCards);
	}

	public void addUserMessage(String text) {
		MessageComponents.UserMessage userMsg = new MessageComponents.UserMessage(text);
		messagesContainer.getChildren().add(userMsg);
		scrollToBottom();
	}

	public void addAiMessage(String markdown) {
		MessageComponents.AiMessage aiMsg = new MessageComponents.AiMessage();
		if (linkHandler != null) {
			aiMsg.setLinkHandler(linkHandler);
		}
		aiMsg.setContent(markdown);
		messagesContainer.getChildren().add(aiMsg);
		scrollToBottom();
	}

	public void startStreamingAiMessage() {
		currentStreamRoot = new VBox();
		currentStreamRoot.getStyleClass().add("stream-message-root");

		currentStreamRenderer = new JavaFxStreamRenderer(currentStreamRoot);
		if (linkHandler != null) {
			currentStreamRenderer.setOnLinkClick(linkHandler);
		}

		StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder()
				.renderer(currentStreamRenderer);
		currentStreamParser = builder.build();

		messagesContainer.getChildren().add(currentStreamRoot);
		scrollToBottom();
	}

	public void appendToStream(String token) {
		if (currentStreamParser != null) {
			currentStreamParser.push(token);
		}
	}

	public void finishStream() {
		if (currentStreamParser != null) {
			currentStreamParser.close();
		}
		currentStreamParser = null;
		currentStreamRenderer = null;
		currentStreamRoot = null;
	}

	public void clear() {
		messagesContainer.getChildren().clear();
		loadWelcomeMessages();
	}

	public void addStreamContainer(VBox container) {
		messagesContainer.getChildren().add(container);
		scrollToBottom();
	}

	public void setLinkHandler(Consumer<String> handler) {
		this.linkHandler = handler;
	}

	private void scrollToBottom() {
		Platform.runLater(() -> {
			messagesContainer.layout();
			this.layout();
			this.setVvalue(1.0);
		});
	}
}
