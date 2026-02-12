package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.profile;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class ProfilePane extends VBox {

	private final ChirpChatApp app;
	private User user;

	public ProfilePane(ChirpChatApp app, User user) {
		this.app = app;
		this.user = user;
		initializeUI();
	}

	private void initializeUI() {
		this.getStyleClass().add("center-content");

		HBox header = createHeader();
		VBox profileHeader = createProfileHeader();
		HBox tabs = createTabs();
		VBox posts = createPostsSection();

		this.getChildren().addAll(header, profileHeader, tabs, posts);
	}

	private HBox createHeader() {
		HBox header = new HBox();
		header.getStyleClass().add("content-header");
		header.setAlignment(Pos.CENTER_LEFT);

		Button backBtn = new Button("←");
		backBtn.getStyleClass().add("icon-button");

		VBox titleBox = new VBox(2);
		titleBox.setPadding(new Insets(0, 0, 0, 24));

		Label name = new Label(user.getDisplayName());
		name.getStyleClass().add("content-title");

		Label postCount = new Label("12 posts");
		postCount.getStyleClass().add("auth-text");
		postCount.setStyle("-fx-text-fill: -text-muted; -fx-font-size: 13px;");

		titleBox.getChildren().addAll(name, postCount);

		header.getChildren().addAll(backBtn, titleBox);
		return header;
	}

	private VBox createProfileHeader() {
		VBox container = new VBox();

		Rectangle cover = new Rectangle();
		cover.getStyleClass().add("profile-cover");
		cover.setWidth(Double.MAX_VALUE);
		cover.setHeight(200);
		cover.setFill(Color.web("#1DA1F2"));

		HBox avatarContainer = new HBox();
		avatarContainer.getStyleClass().add("profile-avatar-container");

		Circle avatar = new Circle(56);
		avatar.getStyleClass().add("profile-avatar-lg");
		avatar.setFill(Color.web("#6366F1"));
		avatar.setStroke(Color.WHITE);
		avatar.setStrokeWidth(4);

		avatarContainer.getChildren().add(avatar);

		HBox buttonRow = new HBox();
		buttonRow.setAlignment(Pos.CENTER_RIGHT);
		buttonRow.setPadding(new Insets(16, 16, 0, 0));
		HBox.setHgrow(buttonRow, Priority.ALWAYS);

		Button editBtn = new Button("Edit profile");
		editBtn.getStyleClass().add("edit-profile-button");

		buttonRow.getChildren().add(editBtn);

		VBox info = new VBox(8);
		info.getStyleClass().add("profile-info");

		HBox nameRow = new HBox(4);
		nameRow.setAlignment(Pos.CENTER_LEFT);

		Label name = new Label(user.getDisplayName());
		name.getStyleClass().add("profile-name");

		if (user.isVerified()) {
			Label verified = new Label("✓");
			verified.setStyle(
					"-fx-background-color: #1DA1F2; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 2px 5px;");
			nameRow.getChildren().addAll(name, verified);
		} else {
			nameRow.getChildren().add(name);
		}

		Label handle = new Label(user.getHandle());
		handle.getStyleClass().add("profile-handle");

		Label bio = new Label(user.getBio() != null ? user.getBio() : "No bio yet");
		bio.getStyleClass().add("profile-bio");
		bio.setWrapText(true);
		bio.setMaxWidth(Double.MAX_VALUE);

		HBox meta = new HBox(16);
		meta.getStyleClass().add("profile-meta");

		if (user.getLocation() != null) {
			Label location = new Label("📍 " + user.getLocation());
			location.getStyleClass().add("auth-text");
			meta.getChildren().add(location);
		}

		Label joinDate = new Label("📅 Joined " + (user.getJoinDate() != null ? user.getJoinDate() : "January 2024"));
		joinDate.getStyleClass().add("auth-text");
		meta.getChildren().add(joinDate);

		HBox stats = new HBox(24);
		stats.getStyleClass().add("profile-stats");

		HBox following = new HBox(4);
		Label followingCount = new Label(String.valueOf(user.getFollowingCount()));
		followingCount.getStyleClass().add("profile-stat-count");
		Label followingLabel = new Label("Following");
		followingLabel.getStyleClass().add("profile-stat-label");
		following.getChildren().addAll(followingCount, followingLabel);

		HBox followers = new HBox(4);
		Label followersCount = new Label(String.valueOf(user.getFollowersCount()));
		followersCount.getStyleClass().add("profile-stat-count");
		Label followersLabel = new Label("Followers");
		followersLabel.getStyleClass().add("profile-stat-label");
		followers.getChildren().addAll(followersCount, followersLabel);

		stats.getChildren().addAll(following, followers);

		info.getChildren().addAll(nameRow, handle, bio, meta, stats);

		container.getChildren().addAll(cover, avatarContainer, buttonRow, info);
		return container;
	}

	private HBox createTabs() {
		HBox tabs = new HBox();
		tabs.getStyleClass().add("profile-tabs");
		tabs.setAlignment(Pos.CENTER_LEFT);

		Label postsTab = new Label("Posts");
		postsTab.getStyleClass().add("profile-tab");
		postsTab.getStyleClass().add("profile-tab-active");

		Label repliesTab = new Label("Replies");
		repliesTab.getStyleClass().add("profile-tab");

		Label highlightsTab = new Label("Highlights");
		highlightsTab.getStyleClass().add("profile-tab");

		Label mediaTab = new Label("Media");
		mediaTab.getStyleClass().add("profile-tab");

		Label likesTab = new Label("Likes");
		likesTab.getStyleClass().add("profile-tab");

		tabs.getChildren().addAll(postsTab, repliesTab, highlightsTab, mediaTab, likesTab);
		return tabs;
	}

	private VBox createPostsSection() {
		VBox container = new VBox();

		VBox pinnedPost = new VBox();
		pinnedPost.getStyleClass().add("post-card");

		HBox pinnedHeader = new HBox(4);
		pinnedHeader.setAlignment(Pos.CENTER_LEFT);
		pinnedHeader.setPadding(new Insets(0, 0, 8, 0));

		Label pinIcon = new Label("📌");
		pinIcon.setStyle("-fx-font-size: 12px;");
		Label pinnedLabel = new Label("Pinned");
		pinnedLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -text-muted;");

		pinnedHeader.getChildren().addAll(pinIcon, pinnedLabel);

		HBox postRow = new HBox(12);
		postRow.setAlignment(Pos.TOP_LEFT);

		Circle avatar = new Circle(20);
		avatar.setFill(Color.web("#6366F1"));

		VBox postContent = new VBox(4);

		HBox authorRow = new HBox(4);
		authorRow.setAlignment(Pos.CENTER_LEFT);

		Label authorName = new Label(user.getDisplayName());
		authorName.getStyleClass().add("post-author-name");

		Label authorHandle = new Label(user.getHandle());
		authorHandle.getStyleClass().add("post-author-handle");

		authorRow.getChildren().addAll(authorName, authorHandle);

		Label content = new Label(
				"Welcome to my profile! I'm excited to connect with you all. Let's build something amazing together! 🚀");
		content.getStyleClass().add("post-content");
		content.setWrapText(true);

		postContent.getChildren().addAll(authorRow, content);
		postRow.getChildren().addAll(avatar, postContent);

		pinnedPost.getChildren().addAll(pinnedHeader, postRow);

		VBox samplePost = new VBox();
		samplePost.getStyleClass().add("post-card");

		HBox sampleRow = new HBox(12);
		sampleRow.setAlignment(Pos.TOP_LEFT);

		Circle sampleAvatar = new Circle(20);
		sampleAvatar.setFill(Color.web("#6366F1"));

		VBox sampleContent = new VBox(4);

		HBox sampleAuthorRow = new HBox(4);
		sampleAuthorRow.setAlignment(Pos.CENTER_LEFT);

		Label sampleName = new Label(user.getDisplayName());
		sampleName.getStyleClass().add("post-author-name");

		Label sampleHandle = new Label(user.getHandle());
		sampleHandle.getStyleClass().add("post-author-handle");

		Label sampleTime = new Label("· 2h");
		sampleTime.getStyleClass().add("post-time");

		sampleAuthorRow.getChildren().addAll(sampleName, sampleHandle, sampleTime);

		Label sampleText = new Label("Working on some exciting new features! Stay tuned for updates. 💻✨");
		sampleText.getStyleClass().add("post-content");
		sampleText.setWrapText(true);

		HBox sampleActions = new HBox(16);
		sampleActions.getStyleClass().add("post-actions");
		sampleActions.setPadding(new Insets(8, 0, 0, 0));

		Label replyAction = new Label("💬 5");
		replyAction.getStyleClass().add("action-count");
		Label retweetAction = new Label("🔄 12");
		retweetAction.getStyleClass().add("action-count");
		Label likeAction = new Label("❤️ 48");
		likeAction.getStyleClass().add("action-count");

		sampleActions.getChildren().addAll(replyAction, retweetAction, likeAction);

		sampleContent.getChildren().addAll(sampleAuthorRow, sampleText, sampleActions);
		sampleRow.getChildren().addAll(sampleAvatar, sampleContent);

		samplePost.getChildren().add(sampleRow);

		container.getChildren().addAll(pinnedPost, samplePost);
		return container;
	}

	public void setUser(User user) {
		this.user = user;
		this.getChildren().clear();
		initializeUI();
	}
}
