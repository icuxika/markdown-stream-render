package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxStreamRenderer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JavaFxAiChatDemo extends Application {

    private final VBox chatContainer = new VBox();
    private final ScrollPane scrollPane = new ScrollPane(chatContainer);
    private final TextArea inputArea = new TextArea();
    private final Button sendButton = new Button("Send");
    private DeepSeekClient client;
    private final MarkdownTheme theme = new MarkdownTheme();
    private final java.util.List<DeepSeekClient.ChatMessage> history = new java.util.ArrayList<>();

    private final CheckBox mockModeCbx = new CheckBox("Mock Mode");
    private String apiKey;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        apiKey = System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = "YOUR_API_KEY_HERE";
            System.out.println("Warning: DEEPSEEK_API_KEY environment variable not set. Defaulting to Mock Mode.");
            mockModeCbx.setSelected(true); // Default to mock if no key
        }

        // Initialize client
        recreateClient();
        mockModeCbx.setOnAction(e -> recreateClient());

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
        ToolBar toolBar = new ToolBar(themeBtn, new Separator(), mockModeCbx);

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(scrollPane);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 900, 700);
        theme.apply(scene); // Apply theme

        // Also apply theme to input box area manually if needed, or rely on root
        // inheritance
        // We added a style to inputBox that uses -md-bg-color, so it should work.

        primaryStage.setTitle("DeepSeek Chat - Markdown Stream Render");
        primaryStage.setScene(scene);
        primaryStage.show();

        inputArea.requestFocus();
    }

    private void recreateClient() {
        client = new DeepSeekClient(apiKey, mockModeCbx.isSelected());
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        inputArea.clear();
        addMessage(text, true);

        // Add user message to history
        history.add(new DeepSeekClient.ChatMessage("user", text));

        // Thinking Indicator
        Label thinkingLabel = new Label("Thinking...");
        thinkingLabel.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-padding: 10;");
        chatContainer.getChildren().add(thinkingLabel);
        scrollToBottom();

        // Prepare AI Message placeholder
        MarkdownBubble aiBubble = new MarkdownBubble(false);
        // Don't add to children yet? Or add it but empty?
        // If we add it empty, it might show a small empty box.
        // Let's add it when first token arrives, replacing thinking label.

        StringBuilder assistantResponse = new StringBuilder();

        client.streamChat(history, token -> {
            // Parser runs in background thread
            aiBubble.append(token);
            // UI updates in FX thread
            Platform.runLater(() -> {
                if (chatContainer.getChildren().contains(thinkingLabel)) {
                    chatContainer.getChildren().remove(thinkingLabel);
                    chatContainer.getChildren().add(aiBubble);
                }
                scrollToBottom();
            });
        }, () -> {
            aiBubble.finish();
            Platform.runLater(() -> {
                // Done, add assistant message to history
                history.add(new DeepSeekClient.ChatMessage("assistant", assistantResponse.toString()));
                if (chatContainer.getChildren().contains(thinkingLabel)) {
                    chatContainer.getChildren().remove(thinkingLabel); // In case of empty response?
                }
            });
        }, error -> Platform.runLater(() -> {
            if (chatContainer.getChildren().contains(thinkingLabel)) {
                chatContainer.getChildren().remove(thinkingLabel);
                chatContainer.getChildren().add(aiBubble);
            }
            aiBubble.append("\n\n**Error**: " + error.getMessage());
            aiBubble.finish();
            scrollToBottom();
        }));
    }

    private void addMessage(String text, boolean isUser) {
        MarkdownBubble bubble = new MarkdownBubble(isUser);
        bubble.setText(text);
        chatContainer.getChildren().add(bubble);
        scrollToBottom();
    }

    private void scrollToBottom() {
        // Run later to allow layout to update first
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private static class MarkdownBubble extends HBox {
        private final VBox contentBox = new VBox();
        private final StreamMarkdownParser parser;
        private final JavaFxStreamRenderer renderer;
        private final boolean isUser;

        public MarkdownBubble(boolean isUser) {
            this.isUser = isUser;
            this.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            this.setPadding(new Insets(5));
            this.setFillHeight(false); // Don't stretch vertically

            contentBox.setStyle("-fx-background-color: transparent;");
            contentBox.setMaxWidth(700);

            // Initialize Renderer and Parser
            this.renderer = new JavaFxStreamRenderer(contentBox);
            StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(renderer);
            CoreExtension.addDefaults(builder);
            this.parser = builder.build();

            VBox bubble = new VBox(contentBox);
            bubble.setPadding(new Insets(10));

            // Use CSS variables for theming
            if (isUser) {
                bubble.setStyle(
                        "-fx-background-color: -chat-user-bg; -fx-background-radius: 10; -fx-border-color: -chat-user-border; -fx-border-radius: 10;");
            } else {
                bubble.setStyle(
                        "-fx-background-color: -chat-assistant-bg; -fx-background-radius: 10; -fx-border-color: -chat-assistant-border; -fx-border-radius: 10;");
            }

            this.getChildren().add(bubble);
        }

        public void setText(String text) {
            parser.push(text);
            parser.close();
        }

        public void append(String token) {
            parser.push(token);
        }

        public void finish() {
            parser.close();
        }
    }
}
