package com.icuxika.markdown.stream.render.demo.javafx.modernchat;

import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MessageComponents {

	public static class AiMessage extends HBox {
		private static final double MAX_WIDTH = 600;

		private final VBox contentBox;
		private JavaFxRenderer renderer;
		private Consumer<String> linkHandler;

		public AiMessage() {
			this.setAlignment(Pos.TOP_LEFT);
			this.setSpacing(12);
			this.setPadding(new Insets(8, 0, 8, 0));

			Circle avatar = new Circle(16);
			avatar.setFill(Color.web("#6366F1"));
			avatar.getStyleClass().add("ai-avatar");

			contentBox = new VBox();
			contentBox.getStyleClass().add("ai-message-content");
			contentBox.setMaxWidth(MAX_WIDTH);

			this.getChildren().addAll(avatar, contentBox);
		}

		public void setLinkHandler(Consumer<String> handler) {
			this.linkHandler = handler;
		}

		public void setContent(String markdown) {
			contentBox.getChildren().clear();

			if (markdown == null || markdown.isEmpty()) {
				return;
			}

			renderer = new JavaFxRenderer();
			if (linkHandler != null) {
				renderer.setOnLinkClick(linkHandler);
			}

			MarkdownParser parser = MarkdownParser.builder().build();
			Document doc = parser.parse(markdown);
			renderer.render(doc);

			contentBox.getChildren().add(renderer.getRoot());
		}
	}

	public static class UserMessage extends HBox {
		private static final double MAX_WIDTH = 500;

		public UserMessage(String text) {
			this.setAlignment(Pos.CENTER_RIGHT);
			this.setPadding(new Insets(8, 0, 8, 0));

			VBox bubble = new VBox();
			bubble.getStyleClass().add("user-message-bubble");
			bubble.setMaxWidth(MAX_WIDTH);
			bubble.setPadding(new Insets(16));
			bubble.setAlignment(Pos.CENTER_RIGHT);

			Label label = new Label(text);
			label.getStyleClass().add("user-message-text");
			label.setWrapText(true);

			bubble.getChildren().add(label);
			this.getChildren().add(bubble);
		}
	}

	public static class StepCards extends VBox {
		private static final String[][] STEP_DATA = {
				{ "STEP 01", "The Value Hook", "Capture attention with a compelling value proposition" },
				{ "STEP 02", "Guided Action", "Direct users towards the desired outcome" },
				{ "STEP 03", "Progressive Reveal", "Show information progressively to maintain engagement" },
				{ "STEP 04", "Social Proof", "Build trust with testimonials and social validation" }
		};

		public StepCards() {
			this.setSpacing(16);
			this.setPadding(new Insets(16, 0, 16, 0));
			initializeUI();
		}

		private void initializeUI() {
			GridPane grid = new GridPane();
			grid.getStyleClass().add("step-cards-grid");
			grid.setHgap(20);
			grid.setVgap(20);

			ColumnConstraints col1 = new ColumnConstraints();
			col1.setPercentWidth(50);
			ColumnConstraints col2 = new ColumnConstraints();
			col2.setPercentWidth(50);
			grid.getColumnConstraints().addAll(col1, col2);

			for (int i = 0; i < STEP_DATA.length; i++) {
				StepCard card = new StepCard(STEP_DATA[i][0], STEP_DATA[i][1], STEP_DATA[i][2]);
				grid.add(card, i % 2, i / 2);
			}

			this.getChildren().add(grid);
		}
	}

	private static class StepCard extends VBox {
		StepCard(String stepNumber, String title, String description) {
			this.getStyleClass().add("step-card");
			this.setPadding(new Insets(20));
			this.setSpacing(8);

			Label stepLabel = new Label(stepNumber);
			stepLabel.getStyleClass().add("step-number");

			Label titleLabel = new Label(title);
			titleLabel.getStyleClass().add("step-title");

			Label descLabel = new Label(description);
			descLabel.getStyleClass().add("step-description");
			descLabel.setWrapText(true);

			this.getChildren().addAll(stepLabel, titleLabel, descLabel);
		}
	}
}
