package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.awt.Desktop;
import java.net.URI;

public class App {

    public static void main(String[] args) throws IOException {
        System.out.println("Markdown Stream Renderer HTML Demo");
        System.out.println("==================================");

        // Load template.md
        String markdown = "";
        try (InputStream is = App.class.getResourceAsStream("/template.md")) {
            if (is != null) {
                markdown = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                markdown = "# Template not found\n\nPlease ensure template.md is in resources.";
            }
        }

        // Render to HTML
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer();
        parser.parse(new java.io.StringReader(markdown), renderer);
        String bodyContent = (String) renderer.getResult();

        String fullHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Markdown Preview</title>
                    <style>
                        body { font-family: sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; line-height: 1.6; }
                        code { background-color: #f0f0f0; padding: 2px 4px; border-radius: 4px; }
                        pre { background-color: #f0f0f0; padding: 10px; border-radius: 4px; overflow-x: auto; }
                        blockquote { border-left: 4px solid #ccc; margin: 0; padding-left: 10px; color: #666; }
                        table { border-collapse: collapse; width: 100%; }
                        th, td { border: 1px solid #ddd; padding: 8px; }
                        th { background-color: #f2f2f2; text-align: left; }
                        img { max-width: 100%; }
                    </style>
                </head>
                <body>
                """ + bodyContent + """
                </body>
                </html>
                """;

        int port = 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on http://localhost:" + port);

            // Open browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + port));
                } catch (Exception e) {
                    System.err.println("Failed to open browser: " + e.getMessage());
                }
            } else {
                System.out.println("Please open http://localhost:" + port + " in your browser.");
            }

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    // Read request (we don't strictly need to parse it for this simple demo, but good to consume it)
                    InputStream clientInput = clientSocket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int read = clientInput.read(buffer); // Read part of the request
                    
                    // Send response
                    OutputStream clientOutput = clientSocket.getOutputStream();
                    byte[] responseBytes = fullHtml.getBytes(StandardCharsets.UTF_8);
                    
                    String header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=utf-8\r\n" +
                            "Content-Length: " + responseBytes.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n";
                    
                    clientOutput.write(header.getBytes(StandardCharsets.UTF_8));
                    clientOutput.write(responseBytes);
                    clientOutput.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
