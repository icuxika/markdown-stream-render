package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.html.HtmlCssProvider;
import com.icuxika.markdown.stream.render.html.renderer.HtmlStreamRenderer;
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
            // Disable caching
            t.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            t.getResponseHeaders().set("Pragma", "no-cache");
            t.getResponseHeaders().set("Expires", "0");

            // Send 200 OK with 0 length (implies chunked in some servers, but for HttpServer we use 0)
            t.sendResponseHeaders(200, 0);

            try (OutputStream os = t.getResponseBody()) {
                // Write Header
                String header = "<html><head><meta charset='UTF-8'><style>" +
                        HtmlCssProvider.getAllCss() +
                        "</style></head><body>\n" +
                        "<div id='content' class='markdown-root'></div>\n"; // Container for stream
                os.write(header.getBytes(StandardCharsets.UTF_8));
                os.flush();

                // Streaming Content
                String content = loadTemplate();
                
                // Simulate LLM streaming (char by char or small chunks)
                // Use random chunk size for realism
                final String finalContent = content;
                java.util.Random random = new java.util.Random();
                int index = 0;

                // Create a bridge: HtmlStreamRenderer writes to StringBuilder -> we flush to OutputStream
                StringBuilder buffer = new StringBuilder();
                HtmlStreamRenderer renderer = new HtmlStreamRenderer(buffer);
                StreamMarkdownParser.Builder parserBuilder = StreamMarkdownParser.builder()
                        .renderer(renderer);
                CoreExtension.addDefaults(parserBuilder);
                StreamMarkdownParser parser = parserBuilder.build();

                while (index < finalContent.length()) {
                    int remaining = finalContent.length() - index;
                    int chunkSize = random.nextInt(10) + 1; // 1-10 chars
                    if (chunkSize > remaining) chunkSize = remaining;
                    
                    String chunk = finalContent.substring(index, index + chunkSize);
                    index += chunkSize;

                    parser.push(chunk);

                    // Flush buffer to socket
                    if (buffer.length() > 0) {
                        // Append to div using simple script injection (since we can't easily modify DOM in pure HTML stream without JS)
                        // Actually, HtmlStreamRenderer outputs HTML fragments.
                        // If we just write them, they appear at the end of body.
                        // But we want them inside <div id='content'>? 
                        // Browsers are forgiving. If we didn't close div, writing more content appends to it.
                        // So we just write content.
                        // But wait, HtmlStreamRenderer might output structural tags.
                        // Let's just output directly.
                        os.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
                        // Auto-scroll
                        os.write("<script>window.scrollTo(0, document.body.scrollHeight);</script>".getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        buffer.setLength(0);
                    }

                    // Simulate delay
                    try {
                        Thread.sleep(random.nextInt(40) + 10); // 10-50ms
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
        try (InputStream is = HtmlStreamServerDemo.class.getResourceAsStream("/comprehensive.md")) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                 try (InputStream is2 = HtmlStreamServerDemo.class.getResourceAsStream("/template.md")) {
                     if (is2 != null) return new String(is2.readAllBytes(), StandardCharsets.UTF_8);
                 }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "# Error\nCould not load template.md";
    }
}
