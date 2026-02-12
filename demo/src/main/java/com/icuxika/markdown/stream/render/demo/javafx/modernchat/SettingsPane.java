package com.icuxika.markdown.stream.render.demo.javafx.modernchat;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SettingsPane extends BorderPane {

	private VBox navigationList;
	private VBox contentArea;
	private int selectedCategory = 0;

	private Consumer<Boolean> onThemeChange;
	private Runnable onClose;

	public SettingsPane() {
		this.getStyleClass().add("settings-pane");
		initializeUI();
	}

	private void initializeUI() {
		HBox header = createHeader();
		navigationList = createNavigationList();
		contentArea = new VBox();
		contentArea.getStyleClass().add("settings-content");

		VBox leftPanel = new VBox(navigationList);
		leftPanel.getStyleClass().add("settings-nav-panel");

		this.setTop(header);
		this.setLeft(leftPanel);
		this.setCenter(contentArea);

		selectCategory(0);
	}

	private HBox createHeader() {
		HBox header = new HBox();
		header.getStyleClass().add("settings-header");
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(16, 20, 16, 20));

		Label title = new Label("Settings");
		title.getStyleClass().add("settings-title");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button closeButton = new Button("✕");
		closeButton.getStyleClass().add("settings-close-button");
		closeButton.setOnAction(e -> {
			if (onClose != null) {
				onClose.run();
			}
		});

		header.getChildren().addAll(title, spacer, closeButton);
		return header;
	}

	private VBox createNavigationList() {
		VBox nav = new VBox();
		nav.getStyleClass().add("settings-nav");
		nav.setPadding(new Insets(8));
		nav.setSpacing(4);

		String[] categories = {
				"General",
				"Appearance",
				"AI Models",
				"Keyboard Shortcuts",
				"Data & Privacy",
				"About"
		};

		for (int i = 0; i < categories.length; i++) {
			final int index = i;
			Label item = new Label(categories[i]);
			item.getStyleClass().add("settings-nav-item");
			item.setOnMouseClicked(e -> selectCategory(index));
			nav.getChildren().add(item);
		}

		return nav;
	}

	private void selectCategory(int index) {
		selectedCategory = index;

		for (int i = 0; i < navigationList.getChildren().size(); i++) {
			navItem(i).getStyleClass().remove("settings-nav-item-selected");
			if (i == index) {
				navItem(i).getStyleClass().add("settings-nav-item-selected");
			}
		}

		contentArea.getChildren().clear();

		switch (index) {
			case 0 -> showGeneralSettings();
			case 1 -> showAppearanceSettings();
			case 2 -> showAiModelsSettings();
			case 3 -> showKeyboardShortcuts();
			case 4 -> showDataPrivacySettings();
			case 5 -> showAboutSettings();
		}
	}

	private Label navItem(int index) {
		return (Label) navigationList.getChildren().get(index);
	}

	private void showGeneralSettings() {
		contentArea.getChildren().clear();

		VBox section = createSection("General Settings");

		HBox themeRow = createSettingRow("Theme", () -> {
			ComboBox<String> combo = new ComboBox<>();
			combo.getItems().addAll("Light", "Dark", "System");
			combo.setValue("Light");
			combo.getStyleClass().add("settings-combo");
			combo.setOnAction(e -> {
				String selected = combo.getValue();
				if (onThemeChange != null) {
					onThemeChange.accept("Dark".equals(selected));
				}
			});
			return combo;
		});

		HBox languageRow = createSettingRow("Language", () -> {
			ComboBox<String> combo = new ComboBox<>();
			combo.getItems().addAll("English", "中文", "日本語", "Español");
			combo.setValue("English");
			combo.getStyleClass().add("settings-combo");
			return combo;
		});

		HBox fontSizeRow = createSettingRow("Font Size", () -> {
			HBox box = new HBox(12);
			box.setAlignment(Pos.CENTER_LEFT);

			Slider slider = new Slider(12, 20, 14);
			slider.setShowTickLabels(true);
			slider.setShowTickMarks(true);
			slider.setMajorTickUnit(4);
			slider.setPrefWidth(200);
			slider.getStyleClass().add("settings-slider");

			Label valueLabel = new Label("14px");
			valueLabel.getStyleClass().add("settings-value-label");
			slider.valueProperty().addListener((obs, old, val) -> valueLabel.setText(val.intValue() + "px"));

			box.getChildren().addAll(slider, valueLabel);
			return box;
		});

		HBox soundRow = createToggleRow("Enable Sound Effects", true);
		HBox autoScrollRow = createToggleRow("Auto-scroll to Bottom", true);
		HBox timestampsRow = createToggleRow("Show Timestamps", false);

		section.getChildren().addAll(themeRow, languageRow, fontSizeRow, soundRow, autoScrollRow, timestampsRow);
		contentArea.getChildren().add(section);
	}

	private void showAppearanceSettings() {
		contentArea.getChildren().clear();

		VBox section = createSection("Appearance");

		HBox bubbleStyleRow = createSettingRow("Message Style", () -> {
			HBox box = new HBox(8);
			box.setAlignment(Pos.CENTER_LEFT);

			Button bubblesBtn = new Button("Bubbles");
			bubblesBtn.getStyleClass().addAll("settings-style-button", "settings-style-button-selected");

			Button flatBtn = new Button("Flat");
			flatBtn.getStyleClass().add("settings-style-button");

			bubblesBtn.setOnAction(e -> {
				bubblesBtn.getStyleClass().add("settings-style-button-selected");
				flatBtn.getStyleClass().remove("settings-style-button-selected");
			});

			flatBtn.setOnAction(e -> {
				flatBtn.getStyleClass().add("settings-style-button-selected");
				bubblesBtn.getStyleClass().remove("settings-style-button-selected");
			});

			box.getChildren().addAll(bubblesBtn, flatBtn);
			return box;
		});

		HBox codeThemeRow = createSettingRow("Code Theme", () -> {
			ComboBox<String> combo = new ComboBox<>();
			combo.getItems().addAll("GitHub Dark", "One Dark", "Monokai", "Solarized");
			combo.setValue("GitHub Dark");
			combo.getStyleClass().add("settings-combo");
			return combo;
		});

		section.getChildren().addAll(bubbleStyleRow, codeThemeRow);
		contentArea.getChildren().add(section);
	}

	private void showAiModelsSettings() {
		contentArea.getChildren().clear();

		VBox modelsSection = createSection("Available Models");

		VBox gptCard = createModelCard("GPT-4o", "Connected", true, true);
		VBox claudeCard = createModelCard("Claude 3.5", "Connected", true, false);
		VBox geminiCard = createModelCard("Gemini", "Not configured", false, false);

		modelsSection.getChildren().addAll(gptCard, claudeCard, geminiCard);

		VBox paramsSection = createSection("Model Parameters");

		HBox tempRow = createSliderRow("Temperature", 0, 2, 0.7);
		HBox tokensRow = createSettingRow("Max Tokens", () -> {
			ComboBox<String> combo = new ComboBox<>();
			combo.getItems().addAll("1024", "2048", "4096", "8192");
			combo.setValue("2048");
			combo.getStyleClass().add("settings-combo");
			return combo;
		});
		HBox topPRow = createSliderRow("Top P", 0, 1, 0.9);

		paramsSection.getChildren().addAll(tempRow, tokensRow, topPRow);

		contentArea.getChildren().addAll(modelsSection, paramsSection);
	}

	private void showKeyboardShortcuts() {
		contentArea.getChildren().clear();

		VBox section = createSection("Keyboard Shortcuts");

		String[][] shortcuts = {
				{ "New Chat", "Cmd+N" },
				{ "Search", "Cmd+K" },
				{ "Send Message", "Enter" },
				{ "New Line", "Shift+Enter" },
				{ "Toggle Theme", "Cmd+T" },
				{ "Settings", "Cmd+," },
				{ "Close", "Escape" }
		};

		for (String[] shortcut : shortcuts) {
			HBox row = createShortcutRow(shortcut[0], shortcut[1]);
			section.getChildren().add(row);
		}

		contentArea.getChildren().add(section);
	}

	private void showDataPrivacySettings() {
		contentArea.getChildren().clear();

		VBox section = createSection("Data & Privacy");

		HBox saveHistoryRow = createToggleRow("Save Chat History", true);
		HBox analyticsRow = createToggleRow("Send Anonymous Analytics", false);

		Button clearDataBtn = new Button("Clear All Chat Data");
		clearDataBtn.getStyleClass().add("settings-danger-button");

		section.getChildren().addAll(saveHistoryRow, analyticsRow, clearDataBtn);
		contentArea.getChildren().add(section);
	}

	private void showAboutSettings() {
		contentArea.getChildren().clear();

		VBox section = createSection("About");

		VBox aboutContent = new VBox(12);
		aboutContent.getStyleClass().add("about-content");

		Label appName = new Label("AI Workspace");
		appName.getStyleClass().add("about-app-name");

		Label version = new Label("Version 1.0.0");
		version.getStyleClass().add("about-version");

		Label description = new Label(
				"A modern AI chat client built with JavaFX.\nSupports multiple AI models with streaming markdown rendering.");
		description.getStyleClass().add("about-description");
		description.setWrapText(true);

		aboutContent.getChildren().addAll(appName, version, description);
		section.getChildren().add(aboutContent);

		contentArea.getChildren().add(section);
	}

	private VBox createSection(String title) {
		VBox section = new VBox(16);
		section.getStyleClass().add("settings-section");
		section.setPadding(new Insets(24));

		Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("settings-section-title");

		section.getChildren().add(titleLabel);
		return section;
	}

	private HBox createSettingRow(String label, Supplier<Node> controlSupplier) {
		HBox row = new HBox();
		row.getStyleClass().add("settings-row");
		row.setAlignment(Pos.CENTER_LEFT);

		Label labelText = new Label(label);
		labelText.getStyleClass().add("settings-label");
		labelText.setPrefWidth(200);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Node control = controlSupplier.get();

		row.getChildren().addAll(labelText, spacer, control);
		return row;
	}

	private HBox createToggleRow(String label, boolean defaultValue) {
		return createSettingRow(label, () -> {
			CheckBox checkBox = new CheckBox();
			checkBox.setSelected(defaultValue);
			checkBox.getStyleClass().add("settings-checkbox");
			return checkBox;
		});
	}

	private HBox createSliderRow(String label, double min, double max, double defaultValue) {
		return createSettingRow(label, () -> {
			HBox box = new HBox(12);
			box.setAlignment(Pos.CENTER_LEFT);

			Slider slider = new Slider(min, max, defaultValue);
			slider.setShowTickLabels(true);
			slider.setMajorTickUnit((max - min) / 4);
			slider.setPrefWidth(150);
			slider.getStyleClass().add("settings-slider");

			Label valueLabel = new Label(String.format("%.1f", defaultValue));
			valueLabel.getStyleClass().add("settings-value-label");
			slider.valueProperty()
					.addListener((obs, old, val) -> valueLabel.setText(String.format("%.1f", val.doubleValue())));

			box.getChildren().addAll(slider, valueLabel);
			return box;
		});
	}

	private VBox createModelCard(String name, String status, boolean connected, boolean isDefault) {
		VBox card = new VBox(8);
		card.getStyleClass().add("model-card");
		card.setPadding(new Insets(16));

		HBox header = new HBox(12);
		header.setAlignment(Pos.CENTER_LEFT);

		Label icon = new Label("🤖");
		icon.getStyleClass().add("model-icon");

		VBox info = new VBox(4);
		Label nameLabel = new Label(name);
		nameLabel.getStyleClass().add("model-name");

		Label statusLabel = new Label(status);
		statusLabel.getStyleClass().add(connected ? "model-status-connected" : "model-status-disconnected");

		info.getChildren().addAll(nameLabel, statusLabel);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		if (isDefault) {
			Label defaultBadge = new Label("Default");
			defaultBadge.getStyleClass().add("model-default-badge");
			header.getChildren().addAll(icon, info, spacer, defaultBadge);
		} else {
			Button configBtn = new Button(connected ? "Configure" : "Add API Key");
			configBtn.getStyleClass().add("model-config-button");
			header.getChildren().addAll(icon, info, spacer, configBtn);
		}

		card.getChildren().add(header);
		return card;
	}

	private HBox createShortcutRow(String action, String shortcut) {
		HBox row = new HBox();
		row.getStyleClass().add("settings-row");
		row.setAlignment(Pos.CENTER_LEFT);

		Label actionLabel = new Label(action);
		actionLabel.getStyleClass().add("settings-label");
		actionLabel.setPrefWidth(200);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label shortcutLabel = new Label(shortcut);
		shortcutLabel.getStyleClass().add("shortcut-key");

		row.getChildren().addAll(actionLabel, spacer, shortcutLabel);
		return row;
	}

	public void setOnThemeChange(Consumer<Boolean> callback) {
		this.onThemeChange = callback;
	}

	public void setOnClose(Runnable callback) {
		this.onClose = callback;
	}
}
