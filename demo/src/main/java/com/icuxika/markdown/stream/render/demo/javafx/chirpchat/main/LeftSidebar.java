package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.main;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.User;
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

public class LeftSidebar extends VBox {

	private final ChirpChatApp app;
	private final User currentUser;
	private VBox navItems;
	private int activeIndex = 0;

	private Runnable onHomeClick;
	private Runnable onMessagesClick;
	private Runnable onProfileClick;
	private Runnable onNotificationsClick;

	public LeftSidebar(ChirpChatApp app, User currentUser) {
		this.app = app;
		this.currentUser = currentUser;
		initializeUI();
	}

	private void initializeUI() {
		this.getStyleClass().add("left-sidebar");
		this.setAlignment(Pos.TOP_LEFT);
		this.setSpacing(4);
		this.setPadding(new Insets(8, 0, 8, 8));

		Label logo = new Label("🐦");
		logo.getStyleClass().add("app-logo");

		navItems = new VBox(4);
		navItems.setPadding(new Insets(16, 0, 16, 0));

		HBox homeNav = createNavItem("🏠", "Home", 0);
		HBox exploreNav = createNavItem("🔍", "Explore", 1);
		HBox notificationsNav = createNavItem("🔔", "Notifications", 2);
		HBox messagesNav = createNavItem("✉️", "Messages", 3);
		HBox bookmarksNav = createNavItem("🔖", "Bookmarks", 4);
		HBox profileNav = createNavItem("👤", "Profile", 5);

		navItems.getChildren().addAll(homeNav, exploreNav, notificationsNav, messagesNav, bookmarksNav, profileNav);

		Button postButton = new Button("Post");
		postButton.getStyleClass().add("post-button");
		postButton.setMaxWidth(Double.MAX_VALUE);
		VBox.setMargin(postButton, new Insets(16, 12, 16, 12));

		VBox userCard = createUserCard();

		Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);

		this.getChildren().addAll(logo, navItems, postButton, spacer, userCard);
	}

	private HBox createNavItem(String icon, String text, int index) {
		HBox item = new HBox(16);
		item.getStyleClass().add("nav-item");
		item.setAlignment(Pos.CENTER_LEFT);
		item.setPadding(new Insets(12, 16, 12, 16));

		Label iconLabel = new Label(icon);
		iconLabel.getStyleClass().add("nav-icon");

		Label textLabel = new Label(text);
		textLabel.getStyleClass().add("nav-text");

		if (index == 0) {
			item.getStyleClass().add("nav-item-active");
		}

		if (index == 2 || index == 3) {
			Label badge = new Label("3");
			badge.getStyleClass().add("nav-badge");
			item.getChildren().addAll(iconLabel, textLabel, badge);
		} else {
			item.getChildren().addAll(iconLabel, textLabel);
		}

		item.setOnMouseClicked(e -> {
			setActiveNav(index);
			handleNavClick(index);
		});

		return item;
	}

	private void setActiveNav(int index) {
		for (int i = 0; i < navItems.getChildren().size(); i++) {
			HBox item = (HBox) navItems.getChildren().get(i);
			item.getStyleClass().remove("nav-item-active");
			if (i == index) {
				item.getStyleClass().add("nav-item-active");
			}
		}
		activeIndex = index;
	}

	private void handleNavClick(int index) {
		switch (index) {
			case 0 -> {
				if (onHomeClick != null)
					onHomeClick.run();
			}
			case 2 -> {
				if (onNotificationsClick != null)
					onNotificationsClick.run();
			}
			case 3 -> {
				if (onMessagesClick != null)
					onMessagesClick.run();
			}
			case 5 -> {
				if (onProfileClick != null)
					onProfileClick.run();
			}
		}
	}

	private VBox createUserCard() {
		VBox card = new VBox();
		card.getStyleClass().add("user-card");
		card.setAlignment(Pos.CENTER_LEFT);
		card.setSpacing(8);
		card.setPadding(new Insets(12));

		HBox row = new HBox(12);
		row.setAlignment(Pos.CENTER_LEFT);

		Circle avatar = new Circle(20);
		avatar.setFill(Color.web("#1DA1F2"));

		VBox info = new VBox(2);

		Label name = new Label(currentUser.getDisplayName());
		name.getStyleClass().add("user-name");

		Label handle = new Label(currentUser.getHandle());
		handle.getStyleClass().add("user-handle");

		info.getChildren().addAll(name, handle);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label moreIcon = new Label("⋯");
		moreIcon.getStyleClass().add("nav-icon");

		row.getChildren().addAll(avatar, info, spacer, moreIcon);

		card.getChildren().add(row);

		card.setOnMouseClicked(e -> app.logout());

		return card;
	}

	public void setOnHomeClick(Runnable callback) {
		this.onHomeClick = callback;
	}

	public void setOnMessagesClick(Runnable callback) {
		this.onMessagesClick = callback;
	}

	public void setOnProfileClick(Runnable callback) {
		this.onProfileClick = callback;
	}

	public void setOnNotificationsClick(Runnable callback) {
		this.onNotificationsClick = callback;
	}
}
