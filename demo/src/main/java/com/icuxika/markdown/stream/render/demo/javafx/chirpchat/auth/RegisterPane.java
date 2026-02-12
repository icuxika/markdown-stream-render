package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.auth;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.User;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class RegisterPane extends VBox {

	private final ChirpChatApp app;
	private final Runnable onSwitchToLogin;

	public RegisterPane(ChirpChatApp app, Runnable onSwitchToLogin) {
		this.app = app;
		this.onSwitchToLogin = onSwitchToLogin;
		initializeUI();
	}

	private void initializeUI() {
		this.setAlignment(Pos.CENTER);
		this.setSpacing(16);
		this.setMaxWidth(400);

		Label title = new Label("Create your account");
		title.getStyleClass().add("auth-welcome-text");

		Label stepIndicator = new Label("Step 1 of 2");
		stepIndicator.getStyleClass().add("auth-text");
		stepIndicator.setStyle("-fx-text-fill: -text-muted; -fx-font-size: 13px;");

		TextField nameField = createInputField("Full name");
		TextField usernameField = createUsernameField();
		TextField emailField = createInputField("Email");
		PasswordField passwordField = createPasswordField("Password");

		Label dobLabel = new Label("Date of birth");
		dobLabel.getStyleClass().add("auth-text");
		dobLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -text-muted; -fx-padding: 8px 0 0 0;");

		HBox dobRow = createDobSelectors();

		CheckBox termsCheck = new CheckBox("I agree to the Terms of Service and Privacy Policy");
		termsCheck.getStyleClass().add("auth-checkbox");
		termsCheck.setWrapText(true);
		termsCheck.setMaxWidth(350);

		Button signUpButton = new Button("Sign Up");
		signUpButton.getStyleClass().add("auth-button");
		signUpButton.setMaxWidth(Double.MAX_VALUE);
		signUpButton.setOnAction(e -> handleRegister(
				nameField.getText(),
				usernameField.getText(),
				emailField.getText(),
				passwordField.getText()));

		HBox switchRow = new HBox();
		switchRow.setAlignment(Pos.CENTER);
		switchRow.setSpacing(4);

		Label hasAccount = new Label("Already have an account?");
		hasAccount.getStyleClass().add("auth-text");

		Label signInLink = new Label("Sign in");
		signInLink.getStyleClass().add("auth-link");
		signInLink.setOnMouseClicked(e -> {
			if (onSwitchToLogin != null) {
				onSwitchToLogin.run();
			}
		});

		switchRow.getChildren().addAll(hasAccount, signInLink);

		this.getChildren().addAll(
				title,
				stepIndicator,
				nameField,
				usernameField,
				emailField,
				passwordField,
				dobLabel,
				dobRow,
				termsCheck,
				signUpButton,
				switchRow);
	}

	private TextField createInputField(String placeholder) {
		TextField field = new TextField();
		field.setPromptText(placeholder);
		field.getStyleClass().add("auth-input");
		field.setMaxWidth(Double.MAX_VALUE);
		return field;
	}

	private TextField createUsernameField() {
		HBox container = new HBox();
		container.getStyleClass().add("auth-input");
		container.setAlignment(Pos.CENTER_LEFT);
		container.setSpacing(4);

		Label atSign = new Label("@");
		atSign.setStyle("-fx-text-fill: -text-muted;");

		TextField field = new TextField();
		field.setPromptText("username");
		field.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
		HBox.setHgrow(field, Priority.ALWAYS);

		return field;
	}

	private PasswordField createPasswordField(String placeholder) {
		PasswordField field = new PasswordField();
		field.setPromptText(placeholder);
		field.getStyleClass().add("auth-input");
		field.setMaxWidth(Double.MAX_VALUE);
		return field;
	}

	private HBox createDobSelectors() {
		HBox row = new HBox(12);
		row.setAlignment(Pos.CENTER_LEFT);

		ComboBox<String> monthBox = new ComboBox<>();
		monthBox.getItems().addAll("January", "February", "March", "April", "May", "June",
				"July", "August", "September", "October", "November", "December");
		monthBox.setPromptText("Month");
		monthBox.getStyleClass().add("auth-input");
		monthBox.setPrefWidth(140);

		ComboBox<String> dayBox = new ComboBox<>();
		for (int i = 1; i <= 31; i++) {
			dayBox.getItems().add(String.valueOf(i));
		}
		dayBox.setPromptText("Day");
		dayBox.getStyleClass().add("auth-input");
		dayBox.setPrefWidth(80);

		ComboBox<String> yearBox = new ComboBox<>();
		for (int i = 2010; i >= 1950; i--) {
			yearBox.getItems().add(String.valueOf(i));
		}
		yearBox.setPromptText("Year");
		yearBox.getStyleClass().add("auth-input");
		yearBox.setPrefWidth(100);

		row.getChildren().addAll(monthBox, dayBox, yearBox);
		return row;
	}

	private void handleRegister(String name, String username, String email, String password) {
		if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
			return;
		}

		String cleanUsername = username.replace("@", "").trim();

		Optional<User> user = app.getAuthService().register(cleanUsername, email, name, password);
		if (user.isPresent()) {
			app.showMainWindow(user.get());
		}
	}
}
