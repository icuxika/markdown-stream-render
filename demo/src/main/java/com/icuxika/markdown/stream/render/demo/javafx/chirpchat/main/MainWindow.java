package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.main;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.User;
import javafx.scene.layout.BorderPane;

public class MainWindow extends BorderPane {

	private final ChirpChatApp app;
	private final User currentUser;
	private LeftSidebar leftSidebar;
	private CenterContent centerContent;
	private RightSidebar rightSidebar;

	public MainWindow(ChirpChatApp app, User currentUser) {
		this.app = app;
		this.currentUser = currentUser;
		initializeUI();
	}

	private void initializeUI() {
		this.getStyleClass().add("main-container");

		leftSidebar = new LeftSidebar(app, currentUser);
		centerContent = new CenterContent(app, currentUser);
		rightSidebar = new RightSidebar(app);

		leftSidebar.setOnHomeClick(() -> centerContent.showHome());
		leftSidebar.setOnMessagesClick(() -> centerContent.showMessages());
		leftSidebar.setOnProfileClick(() -> centerContent.showProfile(currentUser));
		leftSidebar.setOnNotificationsClick(() -> centerContent.showNotifications());

		this.setLeft(leftSidebar);
		this.setCenter(centerContent);
		this.setRight(rightSidebar);
	}
}
