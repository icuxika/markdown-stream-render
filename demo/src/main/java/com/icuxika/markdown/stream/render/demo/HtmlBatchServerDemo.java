package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.html.HtmlCssProvider;
import com.icuxika.markdown.stream.render.html.HtmlRendererExtension;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class HtmlBatchServerDemo {

    private static final int PORT = 8082;

    public static void main(String[] args) {
        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new BatchHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Batch Server started on port " + PORT);
        String url = "http://localhost:" + PORT + "/";
        System.out.println("Please visit: " + url);

        // Try to open browser
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            System.err.println("Failed to open browser automatically: " + e.getMessage());
        }
    }

    private static class BatchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String markdown = loadTemplate();

            // Render
            MarkdownParser.Builder parserBuilder = MarkdownParser.builder();
            CoreExtension.addDefaults(parserBuilder);
            MarkdownParser parser = parserBuilder.build();
            com.icuxika.markdown.stream.render.core.ast.Document doc = parser.parse(markdown);
            HtmlRenderer.Builder builder = HtmlRenderer.builder();
            HtmlRendererExtension.addDefaults(builder);
            HtmlRenderer renderer = builder.build();
            renderer.render(doc);
            String htmlContent = (String) renderer.getResult();

            // Wrap in full HTML
            String response = "<html><head><meta charset='UTF-8'><style>" +
                    HtmlCssProvider.getAllCss() +
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

    private static String loadTemplate() {
        try (InputStream is = HtmlBatchServerDemo.class.getResourceAsStream("/template.md")) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "# Error\nCould not load template.md";
    }
}
