package com.icuxika.markdown.stream.render.demo.javafx;

import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxStreamRenderer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MinimalStreamDemo extends Application {

	@Override
	public void start(Stage stage) {
		VBox output = new VBox();
		JavaFxStreamRenderer renderer = new JavaFxStreamRenderer(output);
		StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(renderer);

		BorderPane root = new BorderPane(new ScrollPane(output));
		Scene scene = new Scene(root, 900, 700);

		MarkdownTheme theme = new MarkdownTheme();
		theme.apply(scene);

		stage.setTitle("Minimal Stream Demo");
		stage.setScene(scene);
		stage.show();

		String markdown = """
				# Streaming

				This is **streaming** rendering.

				!!! warning "Note"
				    Tokens arrive incrementally.
				""";

		final StreamMarkdownParser parser = builder.build();
		Timeline timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);

		final int chunkSize = 6;
		final int[] index = { 0 };
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(25), e -> {
			if (index[0] >= markdown.length()) {
				parser.close();
				timeline.stop();
				return;
			}
			int end = Math.min(markdown.length(), index[0] + chunkSize);
			parser.push(markdown.substring(index[0], end));
			index[0] = end;
		}));
		timeline.play();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
