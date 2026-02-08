package com.icuxika.markdown.stream.render.demo.server;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.html.HtmlCssProvider;
import com.icuxika.markdown.stream.render.html.renderer.HtmlStreamRenderer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class StreamServerDemo {

    private static final int PORT = 8082;

    // ...

    /**
     * Main entry point for the HTML Stream Server Demo.
     *
     * @param args
     *            Command line arguments (not used).
     */
    public static void main(String[] args) {
        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the HTTP server.
     *
     * @throws IOException
     *             If an I/O error occurs.
     */
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
            // Set headers
            t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            t.getResponseHeaders().set("Transfer-Encoding", "chunked");
            t.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            t.getResponseHeaders().set("Pragma", "no-cache");
            t.getResponseHeaders().set("Expires", "0");

            t.sendResponseHeaders(200, 0);

            try (OutputStream os = t.getResponseBody()) {
                // Load Demo Shell CSS (layout + fonts). Theme variables are provided by html module.
                String demoCss = loadResource("/css/demo-shell.css");

                // Construct Page Shell
                String head = "<!DOCTYPE html><html lang='en' data-theme='light'><head>" + "<meta charset='UTF-8'>"
                        + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "<title>Markdown Stream Render Demo</title>" + "<style>" + demoCss + "</style>" + "<style>"
                        + HtmlCssProvider.getAllCss() + "</style>" + "</head><body>";

                String bodyStart = "<div class='app-container'>"
                        +
                        // Sidebar
                        "<aside class='sidebar'>" + "<div class='sidebar-header'>⚡ Stream Render</div>"
                        + "<a href='#' class='nav-item active'>Documentation</a>"
                        + "<a href='#' class='nav-item'>API Reference</a>" + "<a href='#' class='nav-item'>Examples</a>"
                        + "<div class='spacer'></div>"
                        + "<button class='theme-toggle' onclick='toggleTheme()'>Toggle Theme</button>" + "</aside>"
                        +
                        // Main Content
                        "<main class='main-content'>" + "<div id='content' class='markdown-root' data-theme='light'>";
                // OPEN, DO NOT CLOSE YET

                // Script (can be sent early or late, but early is fine if we don't close body)
                // However, for streaming, usually we put script at the end or use
                // DOMContentLoaded
                // But here we need the observer to run immediately as content arrives.
                // We'll output the script at the END to be safe and clean,
                // OR we can output it now but we must ensure we don't close main/app-container.

                os.write(head.getBytes(StandardCharsets.UTF_8));
                os.write(bodyStart.getBytes(StandardCharsets.UTF_8));
                os.flush();

                // Streaming Content Logic ...
                String content = loadTemplate();
                final String finalContent = content;
                java.util.Random random = new java.util.Random();
                int index = 0;

                StringBuilder buffer = new StringBuilder();
                HtmlStreamRenderer renderer = new HtmlStreamRenderer(buffer);
                StreamMarkdownParser.Builder parserBuilder = StreamMarkdownParser.builder().renderer(renderer);
                CoreExtension.addDefaults(parserBuilder);
                StreamMarkdownParser parser = parserBuilder.build();

                while (index < finalContent.length()) {
                    int remaining = finalContent.length() - index;
                    int chunkSize = random.nextInt(15) + 5; // Faster chunking for better demo
                    if (chunkSize > remaining) {
                        chunkSize = remaining;
                    }

                    String chunk = finalContent.substring(index, index + chunkSize);
                    index += chunkSize;

                    parser.push(chunk);

                    if (buffer.length() > 0) {
                        os.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        buffer.setLength(0);
                    }

                    try {
                        Thread.sleep(random.nextInt(20) + 5);
                    } catch (InterruptedException ignored) {
                        // ignore
                    }
                }

                parser.close();
                if (buffer.length() > 0) {
                    os.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
                }

                // Close tags and add script at the end
                String bodyEnd = "</div>"
                        + // close markdown-root
                        "</main>" + "</div>"
                        + // close app-container
                        "<script>" + "function applyTheme(next) {"
                        + "  const html = document.documentElement;"
                        + "  html.setAttribute('data-theme', next);"
                        + "  document.querySelectorAll('.markdown-root').forEach(el => el.setAttribute('data-theme', next));"
                        + "}"
                        + "function toggleTheme() {"
                        + "  const html = document.documentElement;"
                        + "  const current = html.getAttribute('data-theme') || 'light';"
                        + "  const next = current === 'dark' ? 'light' : 'dark';"
                        + "  applyTheme(next);"
                        + "}"
                        + "applyTheme(document.documentElement.getAttribute('data-theme') || 'light');"
                        +
                        // Auto-scroll logic
                        "const contentDiv = document.getElementById('content');"
                        + "const observer = new MutationObserver(() => {"
                        + "  window.scrollTo(0, document.body.scrollHeight);" + "});"
                        + "if (contentDiv) observer.observe(contentDiv, { childList: true, subtree: true });"
                        + "</script>" + "</body></html>";

                os.write(bodyEnd.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        }

        private String loadResource(String path) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

    }

    private static String loadTemplate() {
        try (InputStream is = StreamServerDemo.class.getResourceAsStream("/comprehensive.md")) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                try (InputStream is2 = StreamServerDemo.class.getResourceAsStream("/template.md")) {
                    if (is2 != null) {
                        return new String(is2.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "# Error\nCould not load template.md";
    }
}
