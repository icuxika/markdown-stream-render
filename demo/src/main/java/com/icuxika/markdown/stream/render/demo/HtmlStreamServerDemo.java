package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlStreamRenderer;
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
import java.util.concurrent.Executors;

public class HtmlStreamServerDemo {

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
        server.createContext("/", new StreamHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Stream Server started on port " + PORT);
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

    private static class StreamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Set headers for chunked transfer encoding (streaming)
            t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            t.getResponseHeaders().set("Transfer-Encoding", "chunked");

            // Send 200 OK with 0 length (implies chunked in some servers, but for HttpServer we use 0)
            t.sendResponseHeaders(200, 0);

            try (OutputStream os = t.getResponseBody()) {
                // Write Header
                String header = "<html><head><meta charset='UTF-8'><style>" +
                        "body { font-family: sans-serif; padding: 20px; max-width: 800px; margin: 0 auto; }" +
                        "code { background: #f0f0f0; padding: 2px 4px; border-radius: 3px; }" +
                        "pre { background: #f6f8fa; padding: 10px; border-radius: 5px; overflow-x: auto; }" +
                        "blockquote { border-left: 4px solid #ddd; padding-left: 10px; color: #666; }" +
                        "table { border-collapse: collapse; width: 100%; margin: 15px 0; }" +
                        "th, td { border: 1px solid #ddd; padding: 8px; }" +
                        "th { background-color: #f2f2f2; text-align: left; }" +
                        "</style></head><body>\n";
                os.write(header.getBytes(StandardCharsets.UTF_8));
                os.flush();

                // Streaming Content
                String content = loadTemplate();
                String[] chunks = content.split("(?<=\\n)");

                // Create a bridge: HtmlStreamRenderer writes to StringBuilder -> we flush to OutputStream
                StringBuilder buffer = new StringBuilder();
                HtmlStreamRenderer renderer = new HtmlStreamRenderer(buffer);
                StreamMarkdownParser parser = StreamMarkdownParser.builder()
                        .renderer(renderer)
                        .build();

                for (String chunk : chunks) {
                    parser.push(chunk);

                    // Flush buffer to socket
                    if (buffer.length() > 0) {
                        os.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
                        os.write("<script>window.scrollTo(0, document.body.scrollHeight);</script>".getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        buffer.setLength(0);
                    }

                    // Simulate delay
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                }

                parser.close();
                // Final flush
                if (buffer.length() > 0) {
                    os.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
                }

                os.write("</body></html>".getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        }
    }

    private static String loadTemplate() {
        try (InputStream is = HtmlStreamServerDemo.class.getResourceAsStream("/template.md")) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "# Error\nCould not load template.md";
    }
}
