package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class StreamingAiChatDemo extends Application {

    private final VBox chatContainer = new VBox();
    private final ScrollPane scrollPane = new ScrollPane(chatContainer);
    private final TextArea inputArea = new TextArea();
    private final Button sendButton = new Button("Send");
    private DeepSeekClient client;
    private final MarkdownTheme theme = new MarkdownTheme();
    private final java.util.List<DeepSeekClient.ChatMessage> history = new java.util.ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        String apiKey = System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = "YOUR_API_KEY_HERE"; // Fallback or prompt
            System.out.println("Warning: DEEPSEEK_API_KEY environment variable not set.");

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Configuration Missing");
            alert.setHeaderText("API Key Not Found");
            alert.setContentText("Please set DEEPSEEK_API_KEY environment variable.\nUsing placeholder key which will fail.");
            alert.showAndWait();
        }
        client = new DeepSeekClient(apiKey);

        // Initialize history with system prompt
        history.add(new DeepSeekClient.ChatMessage("system", "You are a helpful assistant."));

        // Layout
        chatContainer.setSpacing(20);
        chatContainer.setPadding(new Insets(20));
        chatContainer.setFillWidth(true);

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Input Area
        inputArea.setPromptText("Type a message... (Enter to send, Shift+Enter for new line)");
        inputArea.setPrefRowCount(3);
        inputArea.setWrapText(true);
        inputArea.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                sendMessage();
            }
        });
        HBox.setHgrow(inputArea, Priority.ALWAYS);

        sendButton.setMinWidth(60);
        sendButton.setPrefHeight(inputArea.getPrefHeight());
        sendButton.setOnAction(e -> sendMessage());

        HBox inputBox = new HBox(10, inputArea, sendButton);
        inputBox.setPadding(new Insets(10));
        inputBox.setAlignment(Pos.BOTTOM_CENTER);
        inputBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1 0 0 0; -fx-background-color: -md-bg-color;");

        // Toolbar
        Button themeBtn = new Button("Switch Theme");
        themeBtn.setOnAction(e -> {
            if (theme.getTheme() == MarkdownTheme.Theme.LIGHT) {
                theme.setTheme(MarkdownTheme.Theme.DARK);
            } else {
                theme.setTheme(MarkdownTheme.Theme.LIGHT);
            }
        });
        ToolBar toolBar = new ToolBar(themeBtn);

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(scrollPane);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 900, 700);
        theme.apply(scene); // Apply theme

        // Also apply theme to input box area manually if needed, or rely on root inheritance
        // We added a style to inputBox that uses -md-bg-color, so it should work.

        primaryStage.setTitle("DeepSeek Chat - Markdown Stream Render");
        primaryStage.setScene(scene);
        primaryStage.show();

        inputArea.requestFocus();
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) return;

        inputArea.clear();
        addMessage(text, true);

        // Add user message to history
        history.add(new DeepSeekClient.ChatMessage("user", text));

        // Prepare AI Message placeholder
        AIMessageBubble aiBubble = new AIMessageBubble();
        chatContainer.getChildren().add(aiBubble);
        scrollToBottom();

        StringBuilder assistantResponse = new StringBuilder();

        client.streamChat(history,
                token -> Platform.runLater(() -> {
                    assistantResponse.append(token);
                    aiBubble.append(token);
                    scrollToBottom();
                }),
                () -> Platform.runLater(() -> {
                    // Done, add assistant message to history
                    history.add(new DeepSeekClient.ChatMessage("assistant", assistantResponse.toString()));
                }),
                error -> Platform.runLater(() -> {
                    aiBubble.append("\n\n**Error**: " + error.getMessage());
                    scrollToBottom();
                })
        );
    }

    private void addMessage(String text, boolean isUser) {
        HBox bubbleWrapper = new HBox();
        bubbleWrapper.setPadding(new Insets(5));

        if (isUser) {
            Label label = new Label(text);
            label.setWrapText(true);
            label.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 10; -fx-font-size: 14px;");
            label.setMaxWidth(600);

            bubbleWrapper.setAlignment(Pos.CENTER_RIGHT);
            bubbleWrapper.getChildren().add(label);
        } else {
            // Should not happen here for AI, using AIMessageBubble class
        }

        chatContainer.getChildren().add(bubbleWrapper);
        scrollToBottom();
    }

    private void scrollToBottom() {
        // Run later to allow layout to update first
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private static class AIMessageBubble extends HBox {
        private final VBox contentBox = new VBox();
        private final MarkdownParser parser = new MarkdownParser();
        private final StringBuilder fullText = new StringBuilder();

        public AIMessageBubble() {
            this.setAlignment(Pos.CENTER_LEFT);
            this.setPadding(new Insets(5));

            contentBox.setStyle("-fx-background-color: transparent;"); // Markdown renderer handles text color via CSS
            contentBox.setMaxWidth(700);

            // Add initial empty content or loading indicator?
            // contentBox.getChildren().add(new Label("..."));

            this.getChildren().add(contentBox);
        }

        public void append(String token) {
            fullText.append(token);
            render();
        }

        private void render() {
            JavaFxRenderer renderer = new JavaFxRenderer();
            try {
                // Re-parse the whole text. 
                // Optimization: For very long text, we might want to only re-parse the last block, 
                // but that requires parser support for incremental state.
                // Current parser is fast enough for chat length.
                parser.parse(new java.io.StringReader(fullText.toString()), renderer);
                Pane result = (Pane) renderer.getResult();
                contentBox.getChildren().setAll(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
