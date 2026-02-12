package com.icuxika.markdown.stream.render.demo.javafx.chirpchat;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.auth.AuthWindow;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.main.MainWindow;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.User;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.service.AuthService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ChirpChatApp extends Application {

	private static final double DEFAULT_WIDTH = 1280;
	private static final double DEFAULT_HEIGHT = 800;
	private static final double MIN_WIDTH = 1024;
	private static final double MIN_HEIGHT = 700;

	private StackPane rootContainer;
	private Scene scene;
	private Stage primaryStage;
	private boolean isDarkMode = false;

	private AuthService authService;
	private User currentUser;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		this.primaryStage = stage;
		this.authService = new AuthService();

		rootContainer = new StackPane();
		rootContainer.getStyleClass().add("root-container");

		scene = new Scene(rootContainer, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		scene.getStylesheets().add(getClass().getResource("/css/chirpchat.css").toExternalForm());

		stage.setTitle("ChirpChat");
		stage.setScene(scene);
		stage.setMinWidth(MIN_WIDTH);
		stage.setMinHeight(MIN_HEIGHT);
		stage.show();

		showAuthWindow();
	}

	public void showAuthWindow() {
		rootContainer.getChildren().clear();
		AuthWindow authWindow = new AuthWindow(this);
		rootContainer.getChildren().add(authWindow);
	}

	public void showMainWindow(User user) {
		this.currentUser = user;
		rootContainer.getChildren().clear();
		MainWindow mainWindow = new MainWindow(this, user);
		rootContainer.getChildren().add(mainWindow);
	}

	public void toggleTheme() {
		isDarkMode = !isDarkMode;
		if (isDarkMode) {
			rootContainer.getStyleClass().add("dark-theme");
		} else {
			rootContainer.getStyleClass().remove("dark-theme");
		}
	}

	public boolean isDarkMode() {
		return isDarkMode;
	}

	public AuthService getAuthService() {
		return authService;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void logout() {
		currentUser = null;
		showAuthWindow();
	}
}
