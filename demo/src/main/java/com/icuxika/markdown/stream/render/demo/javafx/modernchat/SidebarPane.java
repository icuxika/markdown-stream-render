package com.icuxika.markdown.stream.render.demo.javafx.modernchat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class SidebarPane extends VBox {

	private static final double SIDEBAR_WIDTH = 280;
	private static final double PADDING = 20;

	private final VBox chatListContainer = new VBox();
	private final List<ChatItem> chatItems = new ArrayList<>();
	private int selectedIndex = 0;

	private Runnable onNewChat;
	private Consumer<Integer> onChatSelected;

	public SidebarPane() {
		initializeUI();
		loadSampleChats();
	}

	private void initializeUI() {
		this.getStyleClass().add("sidebar");
		this.setPrefWidth(SIDEBAR_WIDTH);
		this.setMinWidth(SIDEBAR_WIDTH);
		this.setMaxWidth(SIDEBAR_WIDTH);
		this.setPadding(new Insets(PADDING));
		this.setSpacing(16);

		VBox topSection = createTopSection();
		VBox middleSection = createMiddleSection();
		VBox bottomSection = createBottomSection();

		VBox.setVgrow(middleSection, Priority.ALWAYS);

		this.getChildren().addAll(topSection, middleSection, bottomSection);
	}

	private VBox createTopSection() {
		VBox section = new VBox(16);

		HBox logoBox = createLogoBox();
		Button newChatButton = createNewChatButton();
		TextField searchField = createSearchField();

		section.getChildren().addAll(logoBox, newChatButton, searchField);
		return section;
	}

	private HBox createLogoBox() {
		HBox logoBox = new HBox(10);
		logoBox.setAlignment(Pos.CENTER_LEFT);
		logoBox.getStyleClass().add("logo-box");

		Circle icon = new Circle(14);
		icon.setFill(Color.web("#2563EB"));
		icon.getStyleClass().add("logo-icon");

		Label titleLabel = new Label("AI Workspace");
		titleLabel.getStyleClass().add("logo-title");

		logoBox.getChildren().addAll(icon, titleLabel);
		return logoBox;
	}

	private Button createNewChatButton() {
		Button button = new Button("+ New Chat");
		button.getStyleClass().add("new-chat-button");
		button.setMaxWidth(Double.MAX_VALUE);
		button.setOnAction(e -> {
			if (onNewChat != null) {
				onNewChat.run();
			}
		});
		return button;
	}

	private TextField createSearchField() {
		TextField searchField = new TextField();
		searchField.setPromptText("Search chats...");
		searchField.getStyleClass().add("search-field");
		return searchField;
	}

	private VBox createMiddleSection() {
		VBox section = new VBox(8);
		section.getStyleClass().add("chat-list-section");

		Label todayLabel = createGroupLabel("TODAY");
		chatListContainer.getStyleClass().add("chat-list-container");
		chatListContainer.setSpacing(4);

		VBox.setVgrow(chatListContainer, Priority.ALWAYS);

		section.getChildren().addAll(todayLabel, chatListContainer);
		return section;
	}

	private Label createGroupLabel(String text) {
		Label label = new Label(text);
		label.getStyleClass().add("group-label");
		return label;
	}

	private VBox createBottomSection() {
		VBox section = new VBox(12);
		section.getStyleClass().add("sidebar-bottom");

		HBox settingsItem = createSettingsItem();
		HBox userCard = createUserCard();

		section.getChildren().addAll(settingsItem, userCard);
		return section;
	}

	private HBox createSettingsItem() {
		HBox item = new HBox(10);
		item.getStyleClass().add("sidebar-item");
		item.setAlignment(Pos.CENTER_LEFT);

		Label icon = new Label("⚙");
		icon.getStyleClass().add("sidebar-icon");

		Label text = new Label("Settings");
		text.getStyleClass().add("sidebar-item-text");

		item.getChildren().addAll(icon, text);
		return item;
	}

	private HBox createUserCard() {
		HBox card = new HBox(12);
		card.getStyleClass().add("user-card");
		card.setAlignment(Pos.CENTER_LEFT);

		Circle avatar = new Circle(18);
		avatar.setFill(Color.web("#6366F1"));
		avatar.getStyleClass().add("user-avatar");

		VBox userInfo = new VBox(2);
		Label userName = new Label("Sarah Jenkins");
		userName.getStyleClass().add("user-name");
		Label userPlan = new Label("Pro Plan");
		userPlan.getStyleClass().add("user-plan");
		userInfo.getChildren().addAll(userName, userPlan);

		card.getChildren().addAll(avatar, userInfo);
		return card;
	}

	private void loadSampleChats() {
		String[] chatTitles = {
				"Project Alpha Roadmap",
				"Market Analysis v2",
				"Python Script Optimization",
				"Landing Page Copywriting"
		};

		for (int i = 0; i < chatTitles.length; i++) {
			ChatItem item = new ChatItem(chatTitles[i], i);
			int finalI = i;
			item.setOnClick(() -> selectChat(finalI));
			chatItems.add(item);
			chatListContainer.getChildren().add(item);
		}

		selectChat(0);
	}

	private void selectChat(int index) {
		for (int i = 0; i < chatItems.size(); i++) {
			chatItems.get(i).setSelected(i == index);
		}
		selectedIndex = index;

		if (onChatSelected != null) {
			onChatSelected.accept(index);
		}
	}

	public void setOnNewChat(Runnable callback) {
		this.onNewChat = callback;
	}

	public void setOnChatSelected(Consumer<Integer> callback) {
		this.onChatSelected = callback;
	}

	private static class ChatItem extends HBox {
		private final int index;
		private boolean selected = false;
		private Runnable onClick;

		ChatItem(String title, int index) {
			this.index = index;
			initializeUI(title);
		}

		private void initializeUI(String title) {
			this.getStyleClass().add("chat-item");
			this.setAlignment(Pos.CENTER_LEFT);
			this.setSpacing(10);
			this.setPadding(new Insets(10, 16, 10, 16));

			Label icon = new Label("💬");
			icon.getStyleClass().add("chat-item-icon");

			Label titleLabel = new Label(title);
			titleLabel.getStyleClass().add("chat-item-title");

			this.getChildren().addAll(icon, titleLabel);

			this.setOnMouseClicked(e -> {
				if (onClick != null) {
					onClick.run();
				}
			});

			this.setOnMouseEntered(e -> {
				if (!selected) {
					this.getStyleClass().add("chat-item-hover");
				}
			});

			this.setOnMouseExited(e -> {
				this.getStyleClass().remove("chat-item-hover");
			});
		}

		void setSelected(boolean selected) {
			this.selected = selected;
			this.getStyleClass().removeAll("chat-item-selected", "chat-item-hover");
			if (selected) {
				this.getStyleClass().add("chat-item-selected");
			}
		}

		void setOnClick(Runnable callback) {
			this.onClick = callback;
		}
	}
}
