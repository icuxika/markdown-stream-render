package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.auth;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AuthWindow extends StackPane {

	private final ChirpChatApp app;
	private LoginPane loginPane;
	private RegisterPane registerPane;

	public AuthWindow(ChirpChatApp app) {
		this.app = app;
		initializeUI();
	}

	private void initializeUI() {
		this.getStyleClass().add("auth-container");

		HBox container = new HBox();
		container.getStyleClass().add("auth-container");

		VBox brandSection = createBrandSection();
		VBox formSection = createFormSection();

		HBox.setHgrow(formSection, Priority.ALWAYS);

		container.getChildren().addAll(brandSection, formSection);
		this.getChildren().add(container);
	}

	private VBox createBrandSection() {
		VBox section = new VBox();
		section.getStyleClass().add("auth-brand-section");
		section.setAlignment(Pos.CENTER);
		section.setPrefWidth(500);
		section.setMinWidth(400);
		section.setSpacing(24);
		section.setPadding(new Insets(60));

		Label logo = new Label("🐦");
		logo.getStyleClass().add("auth-brand-logo");

		Label title = new Label("Connect with friends");
		title.getStyleClass().add("auth-brand-title");

		Label subtitle = new Label("Share moments, build communities");
		subtitle.getStyleClass().add("auth-brand-subtitle");

		VBox features = new VBox(16);
		features.getStyleClass().add("auth-features");
		features.setPadding(new Insets(32, 0, 0, 0));

		HBox feature1 = createFeatureItem("💬", "Chat with friends in real-time");
		HBox feature2 = createFeatureItem("📸", "Share moments with photos and videos");
		HBox feature3 = createFeatureItem("🌐", "Connect with communities worldwide");

		features.getChildren().addAll(feature1, feature2, feature3);

		section.getChildren().addAll(logo, title, subtitle, features);
		return section;
	}

	private HBox createFeatureItem(String icon, String text) {
		HBox item = new HBox(12);
		item.getStyleClass().add("auth-feature-item");
		item.setAlignment(Pos.CENTER_LEFT);

		Label iconLabel = new Label(icon);
		iconLabel.getStyleClass().add("auth-feature-icon");

		Label textLabel = new Label(text);
		textLabel.getStyleClass().add("auth-feature-text");

		item.getChildren().addAll(iconLabel, textLabel);
		return item;
	}

	private VBox createFormSection() {
		VBox section = new VBox();
		section.getStyleClass().add("auth-form-section");
		section.setAlignment(Pos.CENTER);
		section.setSpacing(16);

		Label appName = new Label("ChirpChat");
		appName.getStyleClass().add("auth-app-name");

		loginPane = new LoginPane(app, this::showRegister);
		registerPane = new RegisterPane(app, this::showLogin);

		section.getChildren().addAll(appName, loginPane);

		return section;
	}

	public void showLogin() {
		VBox formSection = (VBox) ((HBox) this.getChildren().get(0)).getChildren().get(1);
		formSection.getChildren().set(1, loginPane);
	}

	public void showRegister() {
		VBox formSection = (VBox) ((HBox) this.getChildren().get(0)).getChildren().get(1);
		formSection.getChildren().set(1, registerPane);
	}
}
