package com.icuxika.markdown.stream.render.demo.javafx.modernchat;

import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxStreamRenderer;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainContentPane extends BorderPane {

	private final TopNavBar topNavBar;
	private final ChatArea chatArea;
	private final InputArea inputArea;

	private final List<ChatHistory> chatHistories = new ArrayList<>();
	private int currentChatIndex = 0;

	private JavaFxStreamRenderer currentStreamRenderer;
	private StreamMarkdownParser currentStreamParser;
	private VBox currentStreamRoot;

	public MainContentPane() {
		this(null);
	}

	public MainContentPane(Runnable onThemeToggle) {
		this.getStyleClass().add("main-content");

		topNavBar = new TopNavBar(onThemeToggle);
		chatArea = new ChatArea();
		inputArea = new InputArea();

		initializeUI();
		setupEventHandlers();
		initializeChatHistories();
	}

	private void initializeUI() {
		this.setTop(topNavBar);
		this.setCenter(chatArea);
		this.setBottom(inputArea);
	}

	private void setupEventHandlers() {
		inputArea.setOnSendMessage(this::handleSendMessage);
	}

	private void initializeChatHistories() {
		chatHistories.add(new ChatHistory("Project Alpha Roadmap"));
		chatHistories.add(new ChatHistory("Market Analysis v2"));
		chatHistories.add(new ChatHistory("Python Script Optimization"));
		chatHistories.add(new ChatHistory("Landing Page Copywriting"));
	}

	private void handleSendMessage(String text) {
		chatArea.addUserMessage(text);

		chatHistories.get(currentChatIndex).addMessage("user", text);

		startStreamingResponse(text);
	}

	private void startStreamingResponse(String userMessage) {
		currentStreamRoot = new VBox();
		currentStreamRoot.getStyleClass().add("stream-message-root");

		currentStreamRenderer = new JavaFxStreamRenderer(currentStreamRoot);

		currentStreamParser = StreamMarkdownParser.builder()
				.renderer(currentStreamRenderer)
				.build();

		chatArea.addStreamContainer(currentStreamRoot);

		simulateStreamingResponse(userMessage);
	}

	private void simulateStreamingResponse(String userMessage) {
		String response = generateMockResponse(userMessage);
		StringBuilder accumulated = new StringBuilder();

		Thread streamThread = new Thread(() -> {
			String[] words = response.split(" ");
			for (int i = 0; i < words.length; i++) {
				final String word = (i == 0) ? words[i] : " " + words[i];
				accumulated.append(word);

				Platform.runLater(() -> {
					if (currentStreamParser != null) {
						currentStreamParser.push(word);
					}
				});

				try {
					Thread.sleep(30 + (long) (Math.random() * 50));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}

			Platform.runLater(() -> {
				if (currentStreamParser != null) {
					currentStreamParser.close();
				}
				currentStreamParser = null;
				currentStreamRenderer = null;
				currentStreamRoot = null;

				chatHistories.get(currentChatIndex).addMessage("assistant", accumulated.toString());
			});
		});

		streamThread.setDaemon(true);
		streamThread.start();
	}

	private String generateMockResponse(String userMessage) {
		String lowerMessage = userMessage.toLowerCase();

		if (lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
			return "Hello! Great to hear from you. How can I assist you today?\n\n" +
					"I'm here to help with any questions or tasks you might have.";
		} else if (lowerMessage.contains("code") || lowerMessage.contains("programming")) {
			return "I'd be happy to help with coding! Here's what I can assist with:\n\n" +
					"- **Code Review**: Analyze your code for improvements\n" +
					"- **Debugging**: Help identify and fix issues\n" +
					"- **Best Practices**: Suggest patterns and conventions\n" +
					"- **Documentation**: Help write clear docs\n\n" +
					"What specific aspect would you like to focus on?";
		} else if (lowerMessage.contains("project") || lowerMessage.contains("roadmap")) {
			return "Great question about project planning! Here's a suggested approach:\n\n" +
					"1. **Define Goals**: Clear objectives and success metrics\n" +
					"2. **Break Down**: Split into manageable phases\n" +
					"3. **Prioritize**: Use MoSCoW method (Must, Should, Could, Won't)\n" +
					"4. **Timeline**: Set realistic deadlines with buffer\n" +
					"5. **Review**: Regular check-ins and adjustments\n\n" +
					"Would you like me to elaborate on any of these points?";
		} else {
			return "Thank you for your message! I understand you're asking about: \"" + userMessage + "\"\n\n" +
					"Let me provide some thoughts:\n\n" +
					"- This is an interesting topic that deserves careful consideration\n" +
					"- There are multiple perspectives we could explore\n" +
					"- I'd be happy to dive deeper into any specific aspect\n\n" +
					"Feel free to ask follow-up questions or provide more context!";
		}
	}

	public void clearChat() {
		chatArea.clear();
		currentChatIndex = 0;
		inputArea.focusInput();
	}

	public void loadChatHistory(int index) {
		if (index >= 0 && index < chatHistories.size()) {
			currentChatIndex = index;
			chatArea.clear();

			ChatHistory history = chatHistories.get(index);
			for (ChatHistory.Message msg : history.getMessages()) {
				if ("user".equals(msg.role)) {
					chatArea.addUserMessage(msg.content);
				} else {
					chatArea.addAiMessage(msg.content);
				}
			}

			inputArea.focusInput();
		}
	}

	public void updateThemeIcon(boolean isDarkMode) {
		topNavBar.updateThemeIcon(isDarkMode);
	}

	public void setOnPromptLibraryClick(Runnable callback) {
		topNavBar.setOnPromptLibraryClick(callback);
	}

	public void setPromptText(String text) {
		inputArea.setText(text);
	}

	private static class ChatHistory {
		private final String title;
		private final List<Message> messages = new ArrayList<>();

		ChatHistory(String title) {
			this.title = title;
		}

		void addMessage(String role, String content) {
			messages.add(new Message(role, content));
		}

		List<Message> getMessages() {
			return messages;
		}

		private static class Message {
			final String role;
			final String content;

			Message(String role, String content) {
				this.role = role;
				this.content = content;
			}
		}
	}
}
