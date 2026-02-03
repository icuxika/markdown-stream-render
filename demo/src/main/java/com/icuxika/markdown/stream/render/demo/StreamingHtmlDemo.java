package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;

import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class StreamingHtmlDemo {

    private static String fullMarkdown = "";

    public static void main(String[] args) throws Exception {
        // Load template
        try (InputStream is = StreamingHtmlDemo.class.getResourceAsStream("/template.md")) {
            if (is != null) {
                fullMarkdown = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                fullMarkdown = "# Error\nTemplate not found.";
            }
        }

        int port = 8081;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Streaming Server started on http://localhost:" + port);

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:" + port));
            }

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void handleClient(Socket socket) throws Exception {
        InputStream input = socket.getInputStream();
        byte[] buffer = new byte[4096];
        int n = input.read(buffer);
        if (n == -1) return;
        String request = new String(buffer, 0, n);

        OutputStream output = socket.getOutputStream();

        if (request.startsWith("GET /stream ")) {
            // SSE Stream
            String header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/event-stream\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "Connection: keep-alive\r\n" +
                    "\r\n";
            output.write(header.getBytes(StandardCharsets.UTF_8));
            output.flush();

            MarkdownParser parser = new MarkdownParser();
            int index = 0;
            while (index < fullMarkdown.length()) {
                int chunkSize = 2 + (int) (Math.random() * 5);
                int endIndex = Math.min(index + chunkSize, fullMarkdown.length());
                String currentText = fullMarkdown.substring(0, endIndex);
                index = endIndex;

                HtmlRenderer renderer = new HtmlRenderer();
                try {
                    parser.parse(new java.io.StringReader(currentText), renderer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String html = (String) renderer.getResult();

                // SSE format: data: <payload>\n\n
                // JSON escape? Simple string replace for demo
                String jsonHtml = html.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
                String payload = "data: {\"html\": \"" + jsonHtml + "\"}\n\n";

                output.write(payload.getBytes(StandardCharsets.UTF_8));
                output.flush();

                Thread.sleep(20); // Simulate typing speed
            }
            // End stream? keep open or close.
        } else {
            // Static Page
            String page = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Markdown Streaming Demo</title>
                        <style>
                            body { font-family: sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; line-height: 1.6; }
                            code { background-color: #f0f0f0; padding: 2px 4px; border-radius: 4px; }
                            pre { background-color: #f0f0f0; padding: 10px; border-radius: 4px; overflow-x: auto; }
                            blockquote { border-left: 4px solid #ccc; margin: 0; padding-left: 10px; color: #666; }
                            table { border-collapse: collapse; width: 100%; margin: 10px 0; }
                            th, td { border: 1px solid #ddd; padding: 8px; }
                            th { background-color: #f2f2f2; text-align: left; }
                            img { max-width: 100%; }
                        </style>
                    </head>
                    <body>
                        <h1>Streaming Preview</h1>
                        <div id="content"></div>
                        <script>
                            const evtSource = new EventSource("/stream");
                            evtSource.onmessage = (e) => {
                                const data = JSON.parse(e.data);
                                document.getElementById('content').innerHTML = data.html;
                                window.scrollTo(0, document.body.scrollHeight);
                            };
                            evtSource.onerror = (e) => {
                                console.log("Stream ended or error");
                                evtSource.close();
                            };
                        </script>
                    </body>
                    </html>
                    """;

            String header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=utf-8\r\n" +
                    "Content-Length: " + page.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            output.write(header.getBytes(StandardCharsets.UTF_8));
            output.write(page.getBytes(StandardCharsets.UTF_8));
            output.flush();
        }
    }
}
