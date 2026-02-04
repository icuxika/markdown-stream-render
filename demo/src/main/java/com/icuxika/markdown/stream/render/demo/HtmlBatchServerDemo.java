package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HtmlBatchServerDemo extends Application {

    private HttpServer server;
    private static final int PORT = 8081;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        Label statusLabel = new Label("Click Start to launch HTTP Server");
        Button startButton = new Button("Start Batch Server & Open Browser");

        startButton.setOnAction(e -> {
            try {
                startServer();
                statusLabel.setText("Server running at http://localhost:" + PORT + "/");
                getHostServices().showDocument("http://localhost:" + PORT + "/");
                startButton.setDisable(true);
            } catch (IOException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(statusLabel, startButton);

        Scene scene = new Scene(root, 400, 200);
        primaryStage.setTitle("HTML Batch Server Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new BatchHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Batch Server started on port " + PORT);
    }

    private class BatchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String markdown = loadTemplate();

            // Render
            MarkdownParser parser = new MarkdownParser();
            com.icuxika.markdown.stream.render.core.ast.Document doc = parser.parse(markdown);
            HtmlRenderer renderer = new HtmlRenderer();
            renderer.render(doc);
            String htmlContent = (String) renderer.getResult();

            // Wrap in full HTML
            String response = "<html><head><meta charset='UTF-8'><style>" +
                    "body { font-family: sans-serif; padding: 20px; max-width: 800px; margin: 0 auto; }" +
                    "code { background: #f0f0f0; padding: 2px 4px; border-radius: 3px; }" +
                    "pre { background: #f6f8fa; padding: 10px; border-radius: 5px; overflow-x: auto; }" +
                    "blockquote { border-left: 4px solid #ddd; padding-left: 10px; color: #666; }" +
                    "table { border-collapse: collapse; width: 100%; margin: 15px 0; }" +
                    "th, td { border: 1px solid #ddd; padding: 8px; }" +
                    "th { background-color: #f2f2f2; text-align: left; }" +
                    "img { max-width: 100%; }" +
                    "</style></head><body>" +
                    htmlContent +
                    "</body></html>";

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private String loadTemplate() {
        try (InputStream is = getClass().getResourceAsStream("/template.md")) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "# Error\nCould not load template.md";
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (server != null) {
            server.stop(0);
        }
    }
}
