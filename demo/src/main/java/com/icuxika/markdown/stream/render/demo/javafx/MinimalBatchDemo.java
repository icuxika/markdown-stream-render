package com.icuxika.markdown.stream.render.demo.javafx;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MinimalBatchDemo extends Application {

    @Override
    public void start(Stage stage) {
        JavaFxRenderer renderer = new JavaFxRenderer();

        MarkdownParser.Builder builder = MarkdownParser.builder();
        CoreExtension.addDefaults(builder);
        MarkdownParser parser = builder.build();

        String markdown = """
                # Hello

                This is **batch** rendering.

                !!! info "Tip"
                    Inline math: $E=mc^2$
                """;

        Document doc = parser.parse(markdown);
        renderer.render(doc);

        BorderPane root = new BorderPane(new ScrollPane(renderer.getRoot()));
        Scene scene = new Scene(root, 900, 700);

        MarkdownTheme theme = new MarkdownTheme();
        theme.apply(scene);

        stage.setTitle("Minimal Batch Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
