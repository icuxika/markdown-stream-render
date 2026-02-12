package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.messages;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.Conversation;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.Message;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.User;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MessagesPane extends HBox {

	private final ChirpChatApp app;
	private final User currentUser;
	private final List<Conversation> conversations = new ArrayList<>();
	private VBox conversationList;
	private VBox chatArea;
	private Conversation selectedConversation;

	public MessagesPane(ChirpChatApp app, User currentUser) {
		this.app = app;
		this.currentUser = currentUser;
		initializeUI();
		loadSampleConversations();
	}

	private void initializeUI() {
		this.getStyleClass().add("messages-container");

		conversationList = createConversationList();
		chatArea = createChatArea();

		this.getChildren().addAll(conversationList, chatArea);
	}

	private VBox createConversationList() {
		VBox container = new VBox();
		container.getStyleClass().add("conversation-list");

		HBox header = new HBox();
		header.getStyleClass().add("conversation-header");
		header.setAlignment(Pos.CENTER_LEFT);

		Label title = new Label("Messages");
		title.getStyleClass().add("conversation-title");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button newMsgBtn = new Button("✉");
		newMsgBtn.getStyleClass().add("icon-button");

		Button settingsBtn = new Button("⚙");
		settingsBtn.getStyleClass().add("icon-button");

		header.getChildren().addAll(title, spacer, newMsgBtn, settingsBtn);

		TextField searchField = new TextField();
		searchField.setPromptText("Search conversations");
		searchField.getStyleClass().add("search-box");
		searchField.setMaxWidth(Double.MAX_VALUE);
		HBox.setMargin(searchField, new Insets(0, 16, 8, 16));

		VBox conversationsContainer = new VBox();
		conversationsContainer.getStyleClass().add("conversations-container");

		ScrollPane scrollPane = new ScrollPane(conversationsContainer);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.getStyleClass().add("scroll-pane");
		VBox.setVgrow(scrollPane, Priority.ALWAYS);

		container.getChildren().addAll(header, searchField, scrollPane);
		return container;
	}

	private VBox createChatArea() {
		VBox container = new VBox();
		container.getStyleClass().add("chat-area");
		container.setAlignment(Pos.CENTER);

		Label placeholder = new Label("Select a conversation to start messaging");
		placeholder.getStyleClass().add("auth-text");
		placeholder.setStyle("-fx-text-fill: -text-muted;");

		container.getChildren().add(placeholder);
		return container;
	}

	private void loadSampleConversations() {
		User user1 = new User(2, "janesmith", "Jane Smith", "jane@example.com");
		User user2 = new User(3, "devmaster", "Dev Master", "dev@example.com");
		User user3 = new User(4, "sarahchen", "Sarah Chen", "sarah@example.com");

		Conversation conv1 = new Conversation(1, user1);
		conv1.addMessage(new Message(1, user1, currentUser, "Hey! How's the project going?"));
		conv1.addMessage(new Message(2, currentUser, user1, "It's going great! Just finished the UI."));
		conv1.addMessage(new Message(3, user1, currentUser, "Awesome! Can't wait to see it! 🎉"));
		conv1.setUnreadCount(1);

		Conversation conv2 = new Conversation(2, user2);
		conv2.addMessage(new Message(4, user2, currentUser, "Did you see the new framework update?"));
		conv2.addMessage(new Message(5, currentUser, user2, "Yes! It has some great improvements."));

		Conversation conv3 = new Conversation(3, user3);
		conv3.addMessage(new Message(6, user3, currentUser, "Meeting at 3pm tomorrow?"));
		conv3.addMessage(new Message(7, currentUser, user3, "Sounds good!"));
		conv3.setUnreadCount(2);

		conversations.add(conv1);
		conversations.add(conv2);
		conversations.add(conv3);

		renderConversations();
	}

	private void renderConversations() {
		VBox container = (VBox) ((ScrollPane) conversationList.getChildren().get(2)).getContent();
		container.getChildren().clear();

		for (Conversation conv : conversations) {
			HBox item = createConversationItem(conv);
			container.getChildren().add(item);
		}
	}

	private HBox createConversationItem(Conversation conv) {
		HBox item = new HBox(12);
		item.getStyleClass().add("conversation-item");
		item.setAlignment(Pos.CENTER_LEFT);
		item.setPadding(new Insets(12, 16, 12, 16));

		Circle avatar = new Circle(24);
		avatar.setFill(Color.web("#1DA1F2"));

		VBox info = new VBox(4);
		HBox.setHgrow(info, Priority.ALWAYS);

		HBox nameRow = new HBox(8);
		nameRow.setAlignment(Pos.CENTER_LEFT);

		Label name = new Label(conv.getOtherUser().getDisplayName());
		name.getStyleClass().add("conversation-preview-name");

		Label handle = new Label(conv.getOtherUser().getHandle());
		handle.getStyleClass().add("conversation-preview-handle");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label time = new Label(conv.getFormattedTime());
		time.getStyleClass().add("conversation-time");

		nameRow.getChildren().addAll(name, handle, spacer, time);

		HBox previewRow = new HBox(8);
		previewRow.setAlignment(Pos.CENTER_LEFT);

		Label preview = new Label(conv.getLastMessagePreview());
		preview.getStyleClass().add("conversation-preview-message");

		if (conv.getUnreadCount() > 0) {
			Circle unreadDot = new Circle(4);
			unreadDot.getStyleClass().add("unread-dot");
			previewRow.getChildren().addAll(preview, unreadDot);
		} else {
			previewRow.getChildren().add(preview);
		}

		info.getChildren().addAll(nameRow, previewRow);

		item.getChildren().addAll(avatar, info);

		item.setOnMouseClicked(e -> selectConversation(conv));

		return item;
	}

	private void selectConversation(Conversation conv) {
		selectedConversation = conv;
		conv.setUnreadCount(0);
		renderConversations();
		renderChatArea(conv);
	}

	private void renderChatArea(Conversation conv) {
		chatArea.getChildren().clear();
		chatArea.setAlignment(Pos.TOP_LEFT);

		HBox header = new HBox(12);
		header.getStyleClass().add("chat-header");
		header.setAlignment(Pos.CENTER_LEFT);

		Circle avatar = new Circle(20);
		avatar.setFill(Color.web("#1DA1F2"));

		VBox info = new VBox(2);

		Label name = new Label(conv.getOtherUser().getDisplayName());
		name.getStyleClass().add("user-name");

		Label handle = new Label(conv.getOtherUser().getHandle());
		handle.getStyleClass().add("user-handle");

		info.getChildren().addAll(name, handle);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button infoBtn = new Button("ℹ");
		infoBtn.getStyleClass().add("icon-button");

		Button moreBtn = new Button("⋯");
		moreBtn.getStyleClass().add("icon-button");

		header.getChildren().addAll(avatar, info, spacer, infoBtn, moreBtn);

		VBox messagesContainer = new VBox(8);
		messagesContainer.getStyleClass().add("chat-messages");
		messagesContainer.setPadding(new Insets(16));

		for (Message msg : conv.getMessages()) {
			HBox messageRow = createMessageBubble(msg);
			messagesContainer.getChildren().add(messageRow);
		}

		ScrollPane scrollPane = new ScrollPane(messagesContainer);
		scrollPane.setFitToWidth(true);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.getStyleClass().add("scroll-pane");
		VBox.setVgrow(scrollPane, Priority.ALWAYS);

		HBox inputArea = new HBox(12);
		inputArea.getStyleClass().add("message-input-area");
		inputArea.setAlignment(Pos.CENTER_LEFT);

		Button attachBtn = new Button("📎");
		attachBtn.getStyleClass().add("icon-button");

		Button gifBtn = new Button("GIF");
		gifBtn.getStyleClass().add("icon-button");

		Button emojiBtn = new Button("😊");
		emojiBtn.getStyleClass().add("icon-button");

		TextField input = new TextField();
		input.setPromptText("Start a new message");
		input.getStyleClass().add("message-input");
		HBox.setHgrow(input, Priority.ALWAYS);

		Button sendBtn = new Button("➤");
		sendBtn.getStyleClass().add("send-button");
		sendBtn.setOnAction(e -> {
			String text = input.getText().trim();
			if (!text.isEmpty()) {
				Message newMsg = new Message(System.currentTimeMillis(), currentUser, conv.getOtherUser(), text);
				conv.addMessage(newMsg);
				input.clear();
				renderChatArea(conv);
			}
		});

		inputArea.getChildren().addAll(attachBtn, gifBtn, emojiBtn, input, sendBtn);

		chatArea.getChildren().addAll(header, scrollPane, inputArea);
	}

	private HBox createMessageBubble(Message msg) {
		HBox row = new HBox();
		row.setAlignment(msg.getSender().getId() == currentUser.getId() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
		row.setPadding(new Insets(4, 0, 4, 0));

		VBox bubble = new VBox(4);
		bubble.getStyleClass().add("message-bubble");
		bubble.getStyleClass()
				.add(msg.getSender().getId() == currentUser.getId() ? "message-sent" : "message-received");
		bubble.setMaxWidth(350);

		Label text = new Label(msg.getContent());
		text.getStyleClass().add("message-text");
		text.setWrapText(true);

		Label time = new Label(msg.getFormattedTime());
		time.getStyleClass().add("message-time");

		bubble.getChildren().addAll(text, time);
		row.getChildren().add(bubble);

		return row;
	}
}
