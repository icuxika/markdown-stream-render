package com.icuxika.markdown.stream.render.demo.javafx.modernchat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PromptLibraryPane extends BorderPane {

	private VBox categoryList;
	private FlowPane promptGrid;
	private final List<PromptCard> promptCards = new ArrayList<>();
	private int selectedCategory = 0;
	private Consumer<PromptItem> onPromptSelected;
	private Runnable onClose;

	public PromptLibraryPane() {
		this.getStyleClass().add("prompt-library-pane");
		initializeUI();
		loadSamplePrompts();
	}

	private void initializeUI() {
		HBox header = createHeader();
		categoryList = createCategoryList();
		promptGrid = new FlowPane();
		promptGrid.getStyleClass().add("prompt-grid");
		promptGrid.setHgap(16);
		promptGrid.setVgap(16);
		promptGrid.setPadding(new Insets(24));

		VBox leftPanel = new VBox(categoryList);
		leftPanel.getStyleClass().add("prompt-library-nav");

		this.setTop(header);
		this.setLeft(leftPanel);
		this.setCenter(promptGrid);

		selectCategory(0);
	}

	private HBox createHeader() {
		HBox header = new HBox();
		header.getStyleClass().add("prompt-library-header");
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(16, 20, 16, 20));

		Label title = new Label("Prompt Library");
		title.getStyleClass().add("prompt-library-title");

		TextField searchField = new TextField();
		searchField.setPromptText("Search prompts...");
		searchField.getStyleClass().add("prompt-search-field");
		searchField.setPrefWidth(300);
		HBox.setMargin(searchField, new Insets(0, 20, 0, 20));

		Button newPromptBtn = new Button("+ New Prompt");
		newPromptBtn.getStyleClass().add("new-prompt-button");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button closeButton = new Button("✕");
		closeButton.getStyleClass().add("settings-close-button");
		closeButton.setOnAction(e -> {
			if (onClose != null) {
				onClose.run();
			}
		});

		header.getChildren().addAll(title, searchField, newPromptBtn, spacer, closeButton);
		return header;
	}

	private VBox createCategoryList() {
		VBox nav = new VBox();
		nav.getStyleClass().add("prompt-category-list");
		nav.setPadding(new Insets(8));
		nav.setSpacing(4);

		String[][] categories = {
				{ "All Prompts", "42" },
				{ "⭐ Favorites", "8" },
				{ "💻 Code & Development", "12" },
				{ "✍️ Writing & Content", "10" },
				{ "📊 Analysis & Research", "7" },
				{ "📁 Custom Prompts", "5" }
		};

		for (int i = 0; i < categories.length; i++) {
			final int index = i;
			HBox item = createCategoryItem(categories[i][0], categories[i][1], i);
			nav.getChildren().add(item);
		}

		return nav;
	}

	private HBox createCategoryItem(String name, String count, int index) {
		HBox item = new HBox();
		item.getStyleClass().add("prompt-category-item");
		item.setAlignment(Pos.CENTER_LEFT);
		item.setPadding(new Insets(10, 12, 10, 12));

		Label nameLabel = new Label(name);
		nameLabel.getStyleClass().add("prompt-category-name");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label countLabel = new Label(count);
		countLabel.getStyleClass().add("prompt-category-count");

		item.getChildren().addAll(nameLabel, spacer, countLabel);
		item.setOnMouseClicked(e -> selectCategory(index));

		return item;
	}

	private void selectCategory(int index) {
		selectedCategory = index;

		for (int i = 0; i < categoryList.getChildren().size(); i++) {
			HBox item = (HBox) categoryList.getChildren().get(i);
			item.getStyleClass().remove("prompt-category-item-selected");
			if (i == index) {
				item.getStyleClass().add("prompt-category-item-selected");
			}
		}

		filterPrompts(index);
	}

	private void filterPrompts(int categoryIndex) {
		promptGrid.getChildren().clear();

		for (PromptCard card : promptCards) {
			boolean show = switch (categoryIndex) {
				case 0 -> true;
				case 1 -> card.prompt.favorite;
				case 2 -> "Development".equals(card.prompt.category);
				case 3 -> "Writing".equals(card.prompt.category);
				case 4 -> "Analysis".equals(card.prompt.category);
				case 5 -> "Custom".equals(card.prompt.category);
				default -> true;
			};

			if (show) {
				promptGrid.getChildren().add(card);
			}
		}
	}

	private void loadSamplePrompts() {
		PromptItem[] prompts = {
				new PromptItem("Code Review Assistant",
						"Analyze code for best practices, potential bugs, and improvement suggestions", "Development",
						true),
				new PromptItem("Blog Post Writer", "Generate engaging blog content on any topic with SEO optimization",
						"Writing", false),
				new PromptItem("Data Analysis Expert", "Help interpret and visualize data with clear explanations",
						"Analysis", true),
				new PromptItem("Meeting Summary", "Condense meeting notes into key points and action items", "General",
						false),
				new PromptItem("Debug Helper", "Identify and fix code errors with step-by-step solutions",
						"Development", true),
				new PromptItem("Email Composer", "Draft professional emails for any business context", "Writing",
						false),
				new PromptItem("API Documentation", "Generate comprehensive API documentation from code", "Development",
						false),
				new PromptItem("Research Assistant", "Summarize research papers and extract key findings", "Analysis",
						false)
		};

		for (PromptItem prompt : prompts) {
			PromptCard card = new PromptCard(prompt);
			card.setOnUse(() -> {
				if (onPromptSelected != null) {
					onPromptSelected.accept(prompt);
				}
			});
			promptCards.add(card);
		}

		filterPrompts(0);
	}

	public void setOnPromptSelected(Consumer<PromptItem> callback) {
		this.onPromptSelected = callback;
	}

	public void setOnClose(Runnable callback) {
		this.onClose = callback;
	}

	public static class PromptItem {
		public final String title;
		public final String description;
		public final String category;
		public boolean favorite;

		public PromptItem(String title, String description, String category, boolean favorite) {
			this.title = title;
			this.description = description;
			this.category = category;
			this.favorite = favorite;
		}
	}

	private static class PromptCard extends VBox {
		private final PromptItem prompt;

		PromptCard(PromptItem prompt) {
			this.prompt = prompt;
			initializeUI();
		}

		private void initializeUI() {
			this.getStyleClass().add("prompt-card");
			this.setPadding(new Insets(16));
			this.setSpacing(12);
			this.setPrefWidth(280);

			HBox header = new HBox();
			header.setAlignment(Pos.CENTER_LEFT);

			Label categoryTag = new Label(prompt.category);
			categoryTag.getStyleClass().add("prompt-category-tag");
			categoryTag.setStyle(getCategoryColor(prompt.category));

			Region spacer = new Region();
			HBox.setHgrow(spacer, Priority.ALWAYS);

			Label favoriteIcon = new Label(prompt.favorite ? "⭐" : "☆");
			favoriteIcon.getStyleClass().add("prompt-favorite-icon");
			favoriteIcon.setOnMouseClicked(e -> {
				prompt.favorite = !prompt.favorite;
				favoriteIcon.setText(prompt.favorite ? "⭐" : "☆");
			});

			header.getChildren().addAll(categoryTag, spacer, favoriteIcon);

			Label titleLabel = new Label(prompt.title);
			titleLabel.getStyleClass().add("prompt-card-title");

			Label descLabel = new Label(prompt.description);
			descLabel.getStyleClass().add("prompt-card-description");
			descLabel.setWrapText(true);

			HBox footer = new HBox();
			footer.setAlignment(Pos.CENTER_LEFT);
			footer.setSpacing(8);

			Button useButton = new Button("Use Prompt");
			useButton.getStyleClass().add("use-prompt-button");

			Region footerSpacer = new Region();
			HBox.setHgrow(footerSpacer, Priority.ALWAYS);

			Button menuButton = new Button("⋮");
			menuButton.getStyleClass().add("prompt-menu-button");

			footer.getChildren().addAll(useButton, footerSpacer, menuButton);

			this.getChildren().addAll(header, titleLabel, descLabel, footer);
		}

		private String getCategoryColor(String category) {
			return switch (category) {
				case "Development" -> "-fx-background-color: #3B82F6;";
				case "Writing" -> "-fx-background-color: #10B981;";
				case "Analysis" -> "-fx-background-color: #8B5CF6;";
				case "General" -> "-fx-background-color: #6B7280;";
				default -> "-fx-background-color: #6B7280;";
			};
		}

		void setOnUse(Runnable callback) {
			((Button) ((HBox) this.getChildren().get(3)).getChildren().get(0)).setOnAction(e -> callback.run());
		}
	}
}
