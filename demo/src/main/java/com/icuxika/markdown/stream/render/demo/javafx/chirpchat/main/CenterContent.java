package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.main;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.messages.MessagesPane;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.User;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.notifications.NotificationsPane;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.profile.ProfilePane;
import javafx.scene.layout.StackPane;

public class CenterContent extends StackPane {

	private final ChirpChatApp app;
	private final User currentUser;
	private HomeFeed homeFeed;
	private MessagesPane messagesPane;
	private ProfilePane profilePane;
	private NotificationsPane notificationsPane;

	public CenterContent(ChirpChatApp app, User currentUser) {
		this.app = app;
		this.currentUser = currentUser;
		initializeUI();
	}

	private void initializeUI() {
		this.getStyleClass().add("center-content");

		homeFeed = new HomeFeed(app, currentUser);
		messagesPane = new MessagesPane(app, currentUser);
		profilePane = new ProfilePane(app, currentUser);
		notificationsPane = new NotificationsPane(app);

		showHome();
	}

	public void showHome() {
		this.getChildren().setAll(homeFeed);
	}

	public void showMessages() {
		this.getChildren().setAll(messagesPane);
	}

	public void showProfile(User user) {
		profilePane.setUser(user);
		this.getChildren().setAll(profilePane);
	}

	public void showNotifications() {
		this.getChildren().setAll(notificationsPane);
	}
}
