package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.main;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
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

public class RightSidebar extends VBox {

	private final ChirpChatApp app;

	public RightSidebar(ChirpChatApp app) {
		this.app = app;
		initializeUI();
	}

	private void initializeUI() {
		this.getStyleClass().add("right-sidebar");
		this.setSpacing(16);
		this.setPadding(new Insets(8, 0, 8, 0));

		TextField searchBox = new TextField();
		searchBox.setPromptText("Search ChirpChat");
		searchBox.getStyleClass().add("search-box");
		searchBox.setMaxWidth(Double.MAX_VALUE);

		VBox trendsCard = createTrendsCard();
		VBox whoToFollowCard = createWhoToFollowCard();

		this.getChildren().addAll(searchBox, trendsCard, whoToFollowCard);
	}

	private VBox createTrendsCard() {
		VBox card = new VBox();
		card.getStyleClass().add("sidebar-card");

		Label title = new Label("Trends for you");
		title.getStyleClass().add("sidebar-card-title");

		VBox trends = new VBox(0);
		trends.setPadding(new Insets(8, 0, 0, 0));

		trends.getChildren().addAll(
				createTrendItem("Technology", "#JavaFX", "12.5K"),
				createTrendItem("Programming", "#Coding", "8.2K"),
				createTrendItem("Trending", "#OpenSource", "5.1K"),
				createTrendItem("Tech", "#Kotlin", "3.4K"));

		Label showMore = new Label("Show more");
		showMore.getStyleClass().add("auth-link");
		showMore.setPadding(new Insets(8, 0, 0, 0));

		card.getChildren().addAll(title, trends, showMore);
		return card;
	}

	private VBox createTrendItem(String category, String name, String count) {
		VBox item = new VBox(2);
		item.getStyleClass().add("trend-item");
		item.setPadding(new Insets(12, 0, 12, 0));

		Label categoryLabel = new Label(category + " · Trending");
		categoryLabel.getStyleClass().add("trend-category");

		Label nameLabel = new Label(name);
		nameLabel.getStyleClass().add("trend-name");

		Label countLabel = new Label(count + " Chirps");
		countLabel.getStyleClass().add("trend-count");

		item.getChildren().addAll(categoryLabel, nameLabel, countLabel);
		return item;
	}

	private VBox createWhoToFollowCard() {
		VBox card = new VBox();
		card.getStyleClass().add("sidebar-card");

		Label title = new Label("Who to follow");
		title.getStyleClass().add("sidebar-card-title");

		VBox suggestions = new VBox(0);
		suggestions.setPadding(new Insets(8, 0, 0, 0));

		suggestions.getChildren().addAll(
				createFollowSuggestion("Sarah Chen", "sarahchen", true),
				createFollowSuggestion("Alex Rivera", "alexrivera", false),
				createFollowSuggestion("Tech Weekly", "techweekly", true));

		Label showMore = new Label("Show more");
		showMore.getStyleClass().add("auth-link");
		showMore.setPadding(new Insets(8, 0, 0, 0));

		card.getChildren().addAll(title, suggestions, showMore);
		return card;
	}

	private HBox createFollowSuggestion(String name, String handle, boolean verified) {
		HBox item = new HBox(12);
		item.getStyleClass().add("follow-suggestion");
		item.setAlignment(Pos.CENTER_LEFT);
		item.setPadding(new Insets(12, 0, 12, 0));

		Circle avatar = new Circle(24);
		avatar.setFill(Color.web("#1DA1F2"));

		VBox info = new VBox(2);
		HBox.setHgrow(info, Priority.ALWAYS);

		HBox nameRow = new HBox(4);
		nameRow.setAlignment(Pos.CENTER_LEFT);

		Label nameLabel = new Label(name);
		nameLabel.getStyleClass().add("user-name");

		if (verified) {
			Label verifiedBadge = new Label("✓");
			verifiedBadge.setStyle(
					"-fx-background-color: #1DA1F2; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 1px 4px;");
			nameRow.getChildren().addAll(nameLabel, verifiedBadge);
		} else {
			nameRow.getChildren().add(nameLabel);
		}

		Label handleLabel = new Label("@" + handle);
		handleLabel.getStyleClass().add("user-handle");

		info.getChildren().addAll(nameRow, handleLabel);

		Button followBtn = new Button("Follow");
		followBtn.getStyleClass().add("follow-button");

		item.getChildren().addAll(avatar, info, followBtn);
		return item;
	}
}
