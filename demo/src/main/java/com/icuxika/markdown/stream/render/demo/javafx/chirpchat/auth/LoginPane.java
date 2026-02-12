package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.auth;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.User;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class LoginPane extends VBox {

	private final ChirpChatApp app;
	private final Runnable onSwitchToRegister;

	public LoginPane(ChirpChatApp app, Runnable onSwitchToRegister) {
		this.app = app;
		this.onSwitchToRegister = onSwitchToRegister;
		initializeUI();
	}

	private void initializeUI() {
		this.setAlignment(Pos.CENTER);
		this.setSpacing(20);
		this.setMaxWidth(400);

		Label welcomeText = new Label("Sign in to continue");
		welcomeText.getStyleClass().add("auth-welcome-text");

		TextField usernameField = createInputField("Email or username");
		PasswordField passwordField = createPasswordField("Password");

		HBox rememberRow = new HBox();
		rememberRow.setAlignment(Pos.CENTER_LEFT);
		rememberRow.setSpacing(8);

		CheckBox rememberCheck = new CheckBox("Remember me");
		rememberCheck.getStyleClass().add("auth-checkbox");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label forgotLink = new Label("Forgot password?");
		forgotLink.getStyleClass().add("auth-link");

		rememberRow.getChildren().addAll(rememberCheck, spacer, forgotLink);

		Button signInButton = new Button("Sign In");
		signInButton.getStyleClass().add("auth-button");
		signInButton.setMaxWidth(Double.MAX_VALUE);
		signInButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

		VBox dividerBox = createDivider("or");

		HBox socialButtons = new HBox(12);
		socialButtons.setAlignment(Pos.CENTER);

		Button googleBtn = createSocialButton("G");
		Button appleBtn = createSocialButton("");
		Button twitterBtn = createSocialButton("🐦");

		socialButtons.getChildren().addAll(googleBtn, appleBtn, twitterBtn);

		HBox switchRow = new HBox();
		switchRow.setAlignment(Pos.CENTER);
		switchRow.setSpacing(4);

		Label noAccount = new Label("Don't have an account?");
		noAccount.getStyleClass().add("auth-text");

		Label signUpLink = new Label("Sign up");
		signUpLink.getStyleClass().add("auth-link");
		signUpLink.setOnMouseClicked(e -> {
			if (onSwitchToRegister != null) {
				onSwitchToRegister.run();
			}
		});

		switchRow.getChildren().addAll(noAccount, signUpLink);

		this.getChildren().addAll(
				welcomeText,
				usernameField,
				passwordField,
				rememberRow,
				signInButton,
				dividerBox,
				socialButtons,
				switchRow);
	}

	private TextField createInputField(String placeholder) {
		TextField field = new TextField();
		field.setPromptText(placeholder);
		field.getStyleClass().add("auth-input");
		field.setMaxWidth(Double.MAX_VALUE);
		return field;
	}

	private PasswordField createPasswordField(String placeholder) {
		PasswordField field = new PasswordField();
		field.setPromptText(placeholder);
		field.getStyleClass().add("auth-input");
		field.setMaxWidth(Double.MAX_VALUE);
		return field;
	}

	private VBox createDivider(String text) {
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(16, 0, 8, 0));

		HBox line = new HBox();
		line.setAlignment(Pos.CENTER);

		Region leftLine = new Region();
		leftLine.getStyleClass().add("auth-divider");
		leftLine.setPrefHeight(1);
		HBox.setHgrow(leftLine, Priority.ALWAYS);

		Label dividerText = new Label(text);
		dividerText.getStyleClass().add("auth-divider-text");

		Region rightLine = new Region();
		rightLine.getStyleClass().add("auth-divider");
		rightLine.setPrefHeight(1);
		HBox.setHgrow(rightLine, Priority.ALWAYS);

		line.getChildren().addAll(leftLine, dividerText, rightLine);
		box.getChildren().add(line);
		return box;
	}

	private Button createSocialButton(String icon) {
		Button button = new Button(icon);
		button.getStyleClass().add("auth-outline-button");
		button.setPrefSize(48, 48);
		return button;
	}

	private void handleLogin(String username, String password) {
		if (username.isEmpty() || password.isEmpty()) {
			return;
		}

		Optional<User> user = app.getAuthService().login(username, password);
		if (user.isPresent()) {
			app.showMainWindow(user.get());
		}
	}
}
