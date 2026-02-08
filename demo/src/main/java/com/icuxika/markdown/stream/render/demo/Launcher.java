package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.demo.javafx.AiChatDemo;
import com.icuxika.markdown.stream.render.demo.javafx.BatchRenderDemo;
import com.icuxika.markdown.stream.render.demo.javafx.MinimalBatchDemo;
import com.icuxika.markdown.stream.render.demo.javafx.MinimalStreamDemo;
import com.icuxika.markdown.stream.render.demo.javafx.StreamRenderDemo;
import com.icuxika.markdown.stream.render.demo.javafx.TypewriterPreviewDemo;
import com.icuxika.markdown.stream.render.demo.javafx.VirtualListDemo;
import com.icuxika.markdown.stream.render.demo.server.BatchServerDemo;
import com.icuxika.markdown.stream.render.demo.server.StreamServerDemo;
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

		Button minimalBatchBtn = new Button("Minimal Batch Demo");
		minimalBatchBtn.setPrefWidth(250);
		minimalBatchBtn.setOnAction(e -> launchDemo(new MinimalBatchDemo()));

		Button minimalStreamBtn = new Button("Minimal Stream Demo");
		minimalStreamBtn.setPrefWidth(250);
		minimalStreamBtn.setOnAction(e -> launchDemo(new MinimalStreamDemo()));

		Button streamDemoBtn = new Button("Streaming Demo (Incremental)");
		streamDemoBtn.setPrefWidth(250);
		streamDemoBtn.setOnAction(e -> launchDemo(new StreamRenderDemo()));

		Button typewriterPreviewBtn = new Button("Typewriter Preview Demo (Char-level)");
		typewriterPreviewBtn.setPrefWidth(250);
		typewriterPreviewBtn.setOnAction(e -> launchDemo(new TypewriterPreviewDemo()));

		Button batchDemoBtn = new Button("Batch Demo (Full Parse)");
		batchDemoBtn.setPrefWidth(250);
		batchDemoBtn.setOnAction(e -> launchDemo(new BatchRenderDemo()));

		Button chatDemoBtn = new Button("AI Chat Demo (Streaming)");
		chatDemoBtn.setPrefWidth(250);
		chatDemoBtn.setOnAction(e -> launchDemo(new AiChatDemo()));

		Button virtualDemoBtn = new Button("Virtual List Demo (High Perf)");
		virtualDemoBtn.setPrefWidth(250);
		virtualDemoBtn.setOnAction(e -> launchDemo(new VirtualListDemo()));

		// HTML Demos
		Label htmlLabel = new Label("HTML Renderer (Browser)");
		htmlLabel.setStyle("-fx-font-weight: bold;");

		Button htmlStreamBtn = new Button("HTML Streaming Server");
		htmlStreamBtn.setPrefWidth(250);
		htmlStreamBtn.setOnAction(e -> launchServer(() -> {
			try {
				StreamServerDemo.startServer();
				showInfo("Streaming Server Started", "Server running at http://localhost:8082/");
			} catch (Exception ex) {
				showError("Error", ex.getMessage());
			}
		}));

		Button htmlBatchBtn = new Button("HTML Batch Server");
		htmlBatchBtn.setPrefWidth(250);
		htmlBatchBtn.setOnAction(e -> launchServer(() -> {
			try {
				BatchServerDemo.startServer();
				showInfo("Batch Server Started", "Server running at http://localhost:8081/");
			} catch (Exception ex) {
				showError("Error", ex.getMessage());
			}
		}));

		root.getChildren().addAll(title, new Separator(), fxLabel, minimalBatchBtn, minimalStreamBtn, streamDemoBtn,
				typewriterPreviewBtn, batchDemoBtn, chatDemoBtn, virtualDemoBtn,
				new Separator(), htmlLabel, htmlStreamBtn, htmlBatchBtn);

		Scene scene = new Scene(root, 400, 560);
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
