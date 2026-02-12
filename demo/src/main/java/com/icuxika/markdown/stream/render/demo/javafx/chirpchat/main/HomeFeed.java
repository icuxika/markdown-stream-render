package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.main;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.Post;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class HomeFeed extends VBox {

	private final ChirpChatApp app;
	private final User currentUser;
	private final List<Post> posts = new ArrayList<>();
	private VBox feedContainer;

	public HomeFeed(ChirpChatApp app, User currentUser) {
		this.app = app;
		this.currentUser = currentUser;
		initializeUI();
		loadSamplePosts();
	}

	private void initializeUI() {
		this.getStyleClass().add("center-content");

		HBox header = createHeader();
		VBox composer = createComposer();
		feedContainer = new VBox();
		feedContainer.getStyleClass().add("feed-container");

		this.getChildren().addAll(header, composer, feedContainer);
	}

	private HBox createHeader() {
		HBox header = new HBox();
		header.getStyleClass().add("content-header");
		header.setAlignment(Pos.CENTER_LEFT);

		Label title = new Label("Home");
		title.getStyleClass().add("content-title");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button settingsBtn = new Button("⚙");
		settingsBtn.getStyleClass().add("icon-button");

		header.getChildren().addAll(title, spacer, settingsBtn);
		return header;
	}

	private VBox createComposer() {
		VBox composer = new VBox();
		composer.getStyleClass().add("post-composer");
		composer.setSpacing(12);

		HBox row = new HBox(12);
		row.setAlignment(Pos.TOP_LEFT);

		Circle avatar = new Circle(24);
		avatar.setFill(Color.web("#1DA1F2"));

		VBox content = new VBox(8);
		HBox.setHgrow(content, Priority.ALWAYS);

		TextArea input = new TextArea();
		input.setPromptText("What's happening?!");
		input.getStyleClass().add("composer-input");
		input.setWrapText(true);
		input.setPrefRowCount(2);
		input.setMaxWidth(Double.MAX_VALUE);
		VBox.setVgrow(input, Priority.ALWAYS);

		HBox toolbar = new HBox(8);
		toolbar.getStyleClass().add("composer-toolbar");
		toolbar.setAlignment(Pos.CENTER_LEFT);

		Button imageBtn = createComposerIcon("🖼");
		Button gifBtn = createComposerIcon("GIF");
		Button pollBtn = createComposerIcon("📊");
		Button emojiBtn = createComposerIcon("😊");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button chirpBtn = new Button("Chirp");
		chirpBtn.getStyleClass().add("chirp-button");
		chirpBtn.setOnAction(e -> {
			String text = input.getText().trim();
			if (!text.isEmpty()) {
				createPost(text);
				input.clear();
			}
		});

		toolbar.getChildren().addAll(imageBtn, gifBtn, pollBtn, emojiBtn, spacer, chirpBtn);

		content.getChildren().addAll(input, toolbar);
		row.getChildren().addAll(avatar, content);

		composer.getChildren().add(row);
		return composer;
	}

	private Button createComposerIcon(String icon) {
		Button btn = new Button(icon);
		btn.getStyleClass().add("composer-icon");
		return btn;
	}

	private void loadSamplePosts() {
		User user1 = new User(1, "johndoe", "John Doe", "john@example.com");
		user1.setVerified(true);

		User user2 = new User(2, "janesmith", "Jane Smith", "jane@example.com");

		User user3 = new User(3, "devmaster", "Dev Master", "dev@example.com");
		user3.setVerified(true);

		Post post1 = new Post(1, user1,
				"Just launched a new feature! Check it out and let me know what you think. 🚀\n\n#coding #startup");
		post1.setLikeCount(42);
		post1.setRetweetCount(12);
		post1.setReplyCount(8);
		post1.setViewCount(1250);
		post1.setCreatedAt(LocalDateTime.now().minusHours(2));

		Post post2 = new Post(2, user2,
				"Beautiful sunset today! Sometimes you need to take a break and appreciate nature. 🌅");
		post2.setLikeCount(128);
		post2.setRetweetCount(34);
		post2.setReplyCount(15);
		post2.setViewCount(3400);
		post2.setCreatedAt(LocalDateTime.now().minusHours(5));

		Post post3 = new Post(3, user3,
				"Hot take: The best code is the code you don't have to write.\n\nSimplicity wins every time. What do you think?");
		post3.setLikeCount(256);
		post3.setRetweetCount(89);
		post3.setReplyCount(45);
		post3.setViewCount(5600);
		post3.setCreatedAt(LocalDateTime.now().minusHours(8));

		posts.add(post1);
		posts.add(post2);
		posts.add(post3);

		renderPosts();
	}

	private void createPost(String content) {
		Post newPost = new Post(System.currentTimeMillis(), currentUser, content);
		newPost.setCreatedAt(LocalDateTime.now());
		posts.add(0, newPost);
		renderPosts();
	}

	private void renderPosts() {
		feedContainer.getChildren().clear();
		for (Post post : posts) {
			VBox postCard = createPostCard(post);
			feedContainer.getChildren().add(postCard);
		}
	}

	private VBox createPostCard(Post post) {
		VBox card = new VBox();
		card.getStyleClass().add("post-card");
		card.setSpacing(8);

		HBox header = new HBox(8);
		header.setAlignment(Pos.CENTER_LEFT);

		Circle avatar = new Circle(20);
		avatar.setFill(Color.web("#1DA1F2"));

		VBox authorInfo = new VBox(2);

		HBox nameRow = new HBox(4);
		nameRow.setAlignment(Pos.CENTER_LEFT);

		Label name = new Label(post.getAuthor().getDisplayName());
		name.getStyleClass().add("post-author-name");

		if (post.getAuthor().isVerified()) {
			Label verified = new Label("✓");
			verified.setStyle(
					"-fx-background-color: #1DA1F2; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 1px 4px;");
			nameRow.getChildren().addAll(name, verified);
		} else {
			nameRow.getChildren().add(name);
		}

		Label handle = new Label(post.getAuthor().getHandle());
		handle.getStyleClass().add("post-author-handle");

		Label time = new Label("· " + post.getFormattedTime());
		time.getStyleClass().add("post-time");

		authorInfo.getChildren().addAll(nameRow, handle);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label moreIcon = new Label("⋯");
		moreIcon.getStyleClass().add("icon-button");

		header.getChildren().addAll(avatar, authorInfo, spacer, moreIcon);

		Label content = new Label(post.getContent());
		content.getStyleClass().add("post-content");
		content.setWrapText(true);
		content.setMaxWidth(Double.MAX_VALUE);

		HBox actions = new HBox(4);
		actions.getStyleClass().add("post-actions");
		actions.setAlignment(Pos.CENTER_LEFT);

		HBox replyAction = createActionItem("💬", post.getReplyCount(), false);
		HBox retweetAction = createActionItem("🔄", post.getRetweetCount(), post.isRetweeted());
		HBox likeAction = createActionItem("❤️", post.getLikeCount(), post.isLiked());
		HBox viewAction = createActionItem("👁", post.getViewCount(), false);
		HBox shareAction = createActionItem("📤", 0, false);

		likeAction.setOnMouseClicked(e -> {
			post.setLiked(!post.isLiked());
			if (post.isLiked()) {
				post.setLikeCount(post.getLikeCount() + 1);
			} else {
				post.setLikeCount(post.getLikeCount() - 1);
			}
			renderPosts();
		});

		actions.getChildren().addAll(replyAction, retweetAction, likeAction, viewAction, shareAction);

		card.getChildren().addAll(header, content, actions);
		return card;
	}

	private HBox createActionItem(String icon, int count, boolean active) {
		HBox item = new HBox(4);
		item.getStyleClass().add("action-button");
		if (active) {
			item.getStyleClass().add("action-liked");
		}
		item.setAlignment(Pos.CENTER_LEFT);

		Label iconLabel = new Label(icon);
		iconLabel.getStyleClass().add("action-icon");

		if (count > 0) {
			Label countLabel = new Label(String.valueOf(count));
			countLabel.getStyleClass().add("action-count");
			item.getChildren().addAll(iconLabel, countLabel);
		} else {
			item.getChildren().add(iconLabel);
		}

		return item;
	}
}
