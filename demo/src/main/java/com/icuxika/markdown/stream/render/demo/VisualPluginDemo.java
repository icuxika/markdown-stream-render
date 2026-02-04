package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Visual demonstration of Plugins in JavaFX.
 * Renders both Admonition blocks and Inline Math.
 */
public class VisualPluginDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        String markdown =
                "# Plugin Demo\n" +
                        "\n" +
                        "This demo shows **custom plugins** in action.\n" +
                        "\n" +
                        "## 1. Admonition Block\n" +
                        "!!! info \"Did you know?\"\n" +
                        "    Markdown Stream Render supports **custom blocks**!\n" +
                        "    This box is rendered by `AdmonitionJavaFxRenderer`.\n" +
                        "\n" +
                        "!!! warning \"Caution\"\n" +
                        "    It is very flexible.\n" +
                        "\n" +
                        "## 2. Inline Math\n" +
                        "We can also parse inline math like $E=mc^2$ or $a^2+b^2=c^2$.\n" +
                        "This is rendered by `MathJavaFxRenderer`.\n" +
                        "\n" +
                        "## 3. Standard Features\n" +
                        "*   Lists\n" +
                        "*   Links\n" +
                        "*   **Bold** and *Italic*\n";

        // 1. Configure Parser with both factories
        MarkdownParser parser = MarkdownParser.builder()
                .blockParserFactory(new BlockPluginDemo.AdmonitionBlockParserFactory())
                .inlineParserFactory(new InlinePluginDemo.MathParserFactory())
                .build();

        // 2. Configure Renderer with both custom renderers
        JavaFxRenderer renderer = JavaFxRenderer.builder()
                .nodeRendererFactory(context -> new BlockPluginDemo.AdmonitionJavaFxRenderer(context))
                .nodeRendererFactory(context -> new InlinePluginDemo.MathJavaFxRenderer(context))
                .build();

        // 3. Render
        VBox root = new VBox();
        root.setPadding(new javafx.geometry.Insets(20));
        root.setSpacing(10);

        com.icuxika.markdown.stream.render.core.ast.Document doc = parser.parse(markdown);
        doc.accept(renderer);

        VBox renderedContent = (VBox) renderer.getResult();
        root.getChildren().add(renderedContent);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 800, 600);
        primaryStage.setTitle("Markdown Plugin System Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
