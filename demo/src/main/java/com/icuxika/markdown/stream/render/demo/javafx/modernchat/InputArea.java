package com.icuxika.markdown.stream.render.demo.javafx.modernchat;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class InputArea extends VBox {

	private static final double TEXT_AREA_HEIGHT = 80;

	private final TextArea textArea;
	private Button sendButton;
	private Consumer<String> onSendMessage;

	public InputArea() {
		this.getStyleClass().add("input-area");
		this.setPadding(new Insets(16, 24, 12, 24));
		this.setSpacing(12);

		textArea = createTextArea();
		HBox toolbar = createToolbar();
		Label hint = createHintLabel();

		this.getChildren().addAll(textArea, toolbar, hint);
	}

	private TextArea createTextArea() {
		TextArea area = new TextArea();
		area.getStyleClass().add("message-text-area");
		area.setPromptText("Ask anything… (type / for commands)");
		area.setPrefHeight(TEXT_AREA_HEIGHT);
		area.setMinHeight(TEXT_AREA_HEIGHT);
		area.setMaxHeight(TEXT_AREA_HEIGHT);
		area.setWrapText(true);

		area.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
				e.consume();
				sendMessage();
			}
		});

		return area;
	}

	private HBox createToolbar() {
		HBox toolbar = new HBox();
		toolbar.setAlignment(Pos.CENTER_LEFT);
		toolbar.setSpacing(8);

		HBox leftTools = createLeftTools();
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		HBox rightTools = createRightTools();

		toolbar.getChildren().addAll(leftTools, spacer, rightTools);
		return toolbar;
	}

	private HBox createLeftTools() {
		HBox tools = new HBox(8);
		tools.setAlignment(Pos.CENTER_LEFT);

		Button attachButton = createToolButton("📎");
		Button emojiButton = createToolButton("😊");
		Button micButton = createToolButton("🎤");
		Button webSearchButton = createWebSearchButton();

		tools.getChildren().addAll(attachButton, emojiButton, micButton, webSearchButton);
		return tools;
	}

	private Button createToolButton(String icon) {
		Button button = new Button(icon);
		button.getStyleClass().add("tool-button");
		return button;
	}

	private Button createWebSearchButton() {
		Button button = new Button("🔍 Web Search");
		button.getStyleClass().add("web-search-button");
		return button;
	}

	private HBox createRightTools() {
		HBox tools = new HBox();
		tools.setAlignment(Pos.CENTER_RIGHT);

		sendButton = new Button("➤");
		sendButton.getStyleClass().add("send-button");
		sendButton.setOnAction(e -> sendMessage());

		tools.getChildren().add(sendButton);
		return tools;
	}

	private Label createHintLabel() {
		Label hint = new Label("AI can make mistakes. Verify important info.");
		hint.getStyleClass().add("input-hint");
		return hint;
	}

	private void sendMessage() {
		String text = textArea.getText().trim();
		if (text.isEmpty()) {
			return;
		}

		textArea.clear();

		if (onSendMessage != null) {
			onSendMessage.accept(text);
		}
	}

	public void setOnSendMessage(Consumer<String> callback) {
		this.onSendMessage = callback;
	}

	public void focusInput() {
		textArea.requestFocus();
	}

	public void setText(String text) {
		textArea.setText(text);
		textArea.requestFocus();
		textArea.positionCaret(text.length());
	}
}
