package com.icuxika.markdown.stream.render.demo.javafx.modernchat;

import com.icuxika.markdown.stream.render.javafx.MarkdownStyles;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ModernAiChatDemo extends Application {

	private static final double DEFAULT_WIDTH = 1200;
	private static final double DEFAULT_HEIGHT = 800;
	private static final double MIN_WIDTH = 1000;
	private static final double MIN_HEIGHT = 700;

	private SidebarPane sidebarPane;
	private MainContentPane mainContentPane;
	private StackPane overlayContainer;
	private SettingsPane settingsPane;
	private PromptLibraryPane promptLibraryPane;
	private Scene scene;
	private boolean isDarkMode = false;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.getStyleClass().add("root-pane");

		sidebarPane = new SidebarPane();
		mainContentPane = new MainContentPane(this::toggleTheme);

		root.setLeft(sidebarPane);
		root.setCenter(mainContentPane);

		overlayContainer = new StackPane();
		overlayContainer.getStyleClass().add("overlay-container");
		overlayContainer.getChildren().add(root);

		scene = new Scene(overlayContainer, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		MarkdownStyles.applyBase(scene, true);
		scene.getStylesheets().add(getClass().getResource("/css/modern-chat.css").toExternalForm());

		primaryStage.setTitle("AI Workspace - Modern Chat");
		primaryStage.setScene(scene);
		primaryStage.setMinWidth(MIN_WIDTH);
		primaryStage.setMinHeight(MIN_HEIGHT);
		primaryStage.show();

		sidebarPane.setOnNewChat(() -> mainContentPane.clearChat());
		sidebarPane.setOnChatSelected(index -> mainContentPane.loadChatHistory(index));
		sidebarPane.setOnSettingsClick(this::showSettings);
		mainContentPane.setOnPromptLibraryClick(this::showPromptLibrary);
	}

	private void toggleTheme() {
		isDarkMode = !isDarkMode;
		if (isDarkMode) {
			scene.getRoot().getStyleClass().add("dark-theme");
		} else {
			scene.getRoot().getStyleClass().remove("dark-theme");
		}
		mainContentPane.updateThemeIcon(isDarkMode);
	}

	private void showSettings() {
		if (settingsPane == null) {
			settingsPane = new SettingsPane();
			settingsPane.setOnThemeChange(this::setTheme);
			settingsPane.setOnClose(this::hideOverlay);
		}

		showOverlay(settingsPane);
	}

	private void showPromptLibrary() {
		if (promptLibraryPane == null) {
			promptLibraryPane = new PromptLibraryPane();
			promptLibraryPane.setOnClose(this::hideOverlay);
			promptLibraryPane.setOnPromptSelected(prompt -> {
				mainContentPane.setPromptText(prompt.title + "\n\n" + prompt.description);
				hideOverlay();
			});
		}

		showOverlay(promptLibraryPane);
	}

	private void showOverlay(javafx.scene.Node overlay) {
		if (overlayContainer.getChildren().size() > 1) {
			overlayContainer.getChildren().remove(1);
		}

		StackPane overlayWrapper = new StackPane();
		overlayWrapper.getStyleClass().add("overlay-wrapper");
		overlayWrapper.setOnMouseClicked(e -> {
			if (e.getTarget() == overlayWrapper) {
				hideOverlay();
			}
		});

		overlayWrapper.getChildren().add(overlay);
		overlayContainer.getChildren().add(overlayWrapper);
	}

	private void hideOverlay() {
		if (overlayContainer.getChildren().size() > 1) {
			overlayContainer.getChildren().remove(overlayContainer.getChildren().size() - 1);
		}
	}

	private void setTheme(boolean dark) {
		if (dark != isDarkMode) {
			toggleTheme();
		}
	}
}
