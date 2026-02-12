package com.icuxika.markdown.stream.render.demo.javafx.modernchat;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TopNavBar extends HBox {

	private static final double NAV_HEIGHT = 60;

	private final ToggleGroup modelToggleGroup = new ToggleGroup();
	private Button themeToggleButton;
	private Consumer<Void> onThemeToggle;

	public TopNavBar() {
		initializeUI();
	}

	public TopNavBar(Runnable onThemeToggle) {
		this.onThemeToggle = v -> onThemeToggle.run();
		initializeUI();
	}

	private void initializeUI() {
		this.getStyleClass().add("top-nav-bar");
		this.setAlignment(Pos.CENTER_LEFT);
		this.setPadding(new Insets(0, 24, 0, 24));
		this.setPrefHeight(NAV_HEIGHT);
		this.setMinHeight(NAV_HEIGHT);
		this.setMaxHeight(NAV_HEIGHT);

		HBox leftSection = createLeftSection();
		HBox centerSection = createCenterSection();
		HBox rightSection = createRightSection();

		Region spacer1 = new Region();
		HBox.setHgrow(spacer1, Priority.ALWAYS);

		Region spacer2 = new Region();
		HBox.setHgrow(spacer2, Priority.ALWAYS);

		this.getChildren().addAll(leftSection, spacer1, centerSection, spacer2, rightSection);
	}

	private HBox createLeftSection() {
		HBox section = new HBox();
		section.setAlignment(Pos.CENTER_LEFT);

		Label breadcrumb = new Label("Workspace > Design Project");
		breadcrumb.getStyleClass().add("breadcrumb");

		section.getChildren().add(breadcrumb);
		return section;
	}

	private HBox createCenterSection() {
		HBox section = new HBox(4);
		section.getStyleClass().add("model-selector");
		section.setAlignment(Pos.CENTER);

		ToggleButton gpt4Button = createModelButton("GPT-4o", true);
		ToggleButton claudeButton = createModelButton("Claude 3.5", false);
		ToggleButton geminiButton = createModelButton("Gemini", false);

		section.getChildren().addAll(gpt4Button, claudeButton, geminiButton);
		return section;
	}

	private ToggleButton createModelButton(String text, boolean selected) {
		ToggleButton button = new ToggleButton(text);
		button.getStyleClass().add("model-toggle-button");
		button.setToggleGroup(modelToggleGroup);
		button.setSelected(selected);
		return button;
	}

	private HBox createRightSection() {
		HBox section = new HBox(12);
		section.setAlignment(Pos.CENTER_RIGHT);

		themeToggleButton = new Button("🌙");
		themeToggleButton.getStyleClass().add("theme-toggle-button");
		themeToggleButton.setOnAction(e -> {
			if (onThemeToggle != null) {
				onThemeToggle.accept(null);
			}
		});

		Button shareButton = createActionButton("Share");
		Button menuButton = createActionButton("⋮");

		section.getChildren().addAll(themeToggleButton, shareButton, menuButton);
		return section;
	}

	private Button createActionButton(String text) {
		Button button = new Button(text);
		button.getStyleClass().add("action-button");
		return button;
	}

	public String getSelectedModel() {
		ToggleButton selected = (ToggleButton) modelToggleGroup.getSelectedToggle();
		return selected != null ? selected.getText() : "GPT-4o";
	}

	public void updateThemeIcon(boolean isDarkMode) {
		if (themeToggleButton != null) {
			themeToggleButton.setText(isDarkMode ? "☀️" : "🌙");
		}
	}

	public void setOnThemeToggle(Consumer<Void> callback) {
		this.onThemeToggle = callback;
	}
}
