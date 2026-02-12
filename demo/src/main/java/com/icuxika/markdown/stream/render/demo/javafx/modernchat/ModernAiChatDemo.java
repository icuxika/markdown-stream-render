package com.icuxika.markdown.stream.render.demo.javafx.modernchat;

import com.icuxika.markdown.stream.render.javafx.MarkdownStyles;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ModernAiChatDemo extends Application {

	private static final double DEFAULT_WIDTH = 1200;
	private static final double DEFAULT_HEIGHT = 800;
	private static final double MIN_WIDTH = 1000;
	private static final double MIN_HEIGHT = 700;

	private SidebarPane sidebarPane;
	private MainContentPane mainContentPane;
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

		scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		MarkdownStyles.applyBase(scene, true);
		scene.getStylesheets().add(getClass().getResource("/css/modern-chat.css").toExternalForm());

		primaryStage.setTitle("AI Workspace - Modern Chat");
		primaryStage.setScene(scene);
		primaryStage.setMinWidth(MIN_WIDTH);
		primaryStage.setMinHeight(MIN_HEIGHT);
		primaryStage.show();

		sidebarPane.setOnNewChat(() -> mainContentPane.clearChat());
		sidebarPane.setOnChatSelected(index -> mainContentPane.loadChatHistory(index));
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
}
