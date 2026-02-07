package com.icuxika.markdown.stream.render.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Markdown Stream Renderer Demos");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // JavaFX Demos
        Label fxLabel = new Label("JavaFX Renderer");
        fxLabel.setStyle("-fx-font-weight: bold;");

        Button streamDemoBtn = new Button("Streaming Demo (Incremental)");
        streamDemoBtn.setPrefWidth(250);
        streamDemoBtn.setOnAction(e -> launchDemo(new JavaFxStreamDemo()));

        Button batchDemoBtn = new Button("Batch Demo (Full Parse)");
        batchDemoBtn.setPrefWidth(250);
        batchDemoBtn.setOnAction(e -> launchDemo(new JavaFxBatchDemo()));

        Button chatDemoBtn = new Button("AI Chat Demo (Streaming)");
        chatDemoBtn.setPrefWidth(250);
        chatDemoBtn.setOnAction(e -> launchDemo(new JavaFxAiChatDemo()));

        // HTML Demos
        Label htmlLabel = new Label("HTML Renderer (Browser)");
        htmlLabel.setStyle("-fx-font-weight: bold;");

        Button htmlStreamBtn = new Button("HTML Streaming Server");
        htmlStreamBtn.setPrefWidth(250);
        htmlStreamBtn.setOnAction(e -> launchServer(() -> {
            try {
                HtmlStreamServerDemo.startServer();
                showInfo("Streaming Server Started", "Server running at http://localhost:8082/");
            } catch (Exception ex) {
                showError("Error", ex.getMessage());
            }
        }));

        Button htmlBatchBtn = new Button("HTML Batch Server");
        htmlBatchBtn.setPrefWidth(250);
        htmlBatchBtn.setOnAction(e -> launchServer(() -> {
            try {
                HtmlBatchServerDemo.startServer();
                showInfo("Batch Server Started", "Server running at http://localhost:8081/");
            } catch (Exception ex) {
                showError("Error", ex.getMessage());
            }
        }));

        root.getChildren().addAll(title, new Separator(), fxLabel, streamDemoBtn, batchDemoBtn, chatDemoBtn,
                new Separator(), htmlLabel, htmlStreamBtn, htmlBatchBtn);

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setTitle("Demo Launcher");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void launchDemo(Application app) {
        try {
            Stage stage = new Stage();
            app.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchServer(Runnable serverTask) {
        new Thread(serverTask).start();
    }

    private void showInfo(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
