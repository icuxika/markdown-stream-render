package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.notifications;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class NotificationsPane extends VBox {

	private final ChirpChatApp app;
	private HBox tabBar;
	private int activeTab = 0;

	public NotificationsPane(ChirpChatApp app) {
		this.app = app;
		initializeUI();
	}

	private void initializeUI() {
		this.getStyleClass().add("center-content");

		HBox header = createHeader();
		tabBar = createTabBar();
		VBox notifications = createNotificationsList();

		this.getChildren().addAll(header, tabBar, notifications);
	}

	private HBox createHeader() {
		HBox header = new HBox();
		header.getStyleClass().add("content-header");
		header.setAlignment(Pos.CENTER_LEFT);

		Label title = new Label("Notifications");
		title.getStyleClass().add("content-title");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button settingsBtn = new Button("⚙");
		settingsBtn.getStyleClass().add("icon-button");

		header.getChildren().addAll(title, spacer, settingsBtn);
		return header;
	}

	private HBox createTabBar() {
		HBox tabs = new HBox();
		tabs.getStyleClass().add("tab-bar");
		tabs.setAlignment(Pos.CENTER_LEFT);
		tabs.setPadding(new Insets(0, 16, 0, 16));

		Label allTab = new Label("All");
		allTab.getStyleClass().add("tab-button");
		allTab.getStyleClass().add("tab-button-active");
		allTab.setOnMouseClicked(e -> setActiveTab(0));

		Label mentionsTab = new Label("Mentions");
		mentionsTab.getStyleClass().add("tab-button");
		mentionsTab.setOnMouseClicked(e -> setActiveTab(1));

		Label verifiedTab = new Label("Verified");
		verifiedTab.getStyleClass().add("tab-button");
		verifiedTab.setOnMouseClicked(e -> setActiveTab(2));

		tabs.getChildren().addAll(allTab, mentionsTab, verifiedTab);
		return tabs;
	}

	private void setActiveTab(int index) {
		activeTab = index;
		for (int i = 0; i < tabBar.getChildren().size(); i++) {
			Label tab = (Label) tabBar.getChildren().get(i);
			tab.getStyleClass().remove("tab-button-active");
			if (i == index) {
				tab.getStyleClass().add("tab-button-active");
			}
		}
	}

	private VBox createNotificationsList() {
		VBox container = new VBox();

		container.getChildren().addAll(
				createNotificationItem("❤️", "John Doe", "liked your post", "Just launched a new feature! 🚀", true),
				createNotificationItem("🔄", "Jane Smith", "retweeted your post", "Working on exciting projects!",
						false),
				createNotificationItem("👤", "Dev Master", "followed you", null, true),
				createNotificationItem("💬", "Sarah Chen", "mentioned you",
						"@you What do you think about this approach?", false),
				createNotificationItem("❤️", "Alex Rivera", "and 5 others liked your post",
						"Beautiful sunset today! 🌅", false),
				createNotificationItem("👤", "Tech Weekly", "followed you", null, false),
				createNotificationItem("🔄", "Code Ninja", "retweeted your post", "Hot take: Simplicity wins!", true));

		return container;
	}

	private HBox createNotificationItem(String icon, String user, String action, String preview, boolean unread) {
		HBox item = new HBox(12);
		item.getStyleClass().add("notification-item");
		if (unread) {
			item.getStyleClass().add("notification-unread");
		}
		item.setAlignment(Pos.TOP_LEFT);
		item.setPadding(new Insets(12, 16, 12, 16));

		Label iconLabel = new Label(icon);
		iconLabel.getStyleClass().add("notification-icon");
		iconLabel.setMinWidth(40);
		iconLabel.setAlignment(Pos.CENTER);

		VBox content = new VBox(4);
		HBox.setHgrow(content, Priority.ALWAYS);

		HBox avatars = new HBox(-8);
		avatars.setPadding(new Insets(0, 0, 4, 0));

		Circle avatar1 = new Circle(16);
		avatar1.setFill(Color.web("#1DA1F2"));
		avatars.getChildren().add(avatar1);

		VBox textContent = new VBox(4);

		Label mainText = new Label(user + " " + action);
		mainText.getStyleClass().add("notification-text");
		mainText.setWrapText(true);

		textContent.getChildren().add(mainText);

		if (preview != null) {
			Label previewLabel = new Label(preview);
			previewLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -text-muted;");
			previewLabel.setWrapText(true);
			previewLabel.setMaxWidth(300);
			textContent.getChildren().add(previewLabel);
		}

		content.getChildren().addAll(avatars, textContent);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		if (action.contains("followed")) {
			Button followBack = new Button("Follow");
			followBack.getStyleClass().add("follow-button");
			item.getChildren().addAll(iconLabel, content, spacer, followBack);
		} else {
			item.getChildren().addAll(iconLabel, content, spacer);
		}

		return item;
	}
}
