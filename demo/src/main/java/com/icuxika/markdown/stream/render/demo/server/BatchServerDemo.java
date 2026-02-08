package com.icuxika.markdown.stream.render.demo.server;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.html.HtmlCssProvider;
import com.icuxika.markdown.stream.render.html.HtmlRendererExtension;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
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

public class BatchServerDemo {

	private static final int PORT = 8082;

	// ...

	/**
	 * Main entry point for the HTML Batch Server Demo.
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

			com.icuxika.markdown.stream.render.core.parser.MarkdownParserOptions options = new com.icuxika.markdown.stream.render.core.parser.MarkdownParserOptions();
			options.setGenerateHeadingIds(true);

			HtmlRenderer.Builder builder = HtmlRenderer.builder().options(options);
			HtmlRendererExtension.addDefaults(builder);
			HtmlRenderer renderer = builder.build();
			renderer.render(doc);
			String htmlContent = (String) renderer.getResult();

			// Load Demo Shell CSS (layout + fonts). Theme variables are provided by html
			// module.
			String demoCss = loadResource("/css/demo-shell.css");

			// Wrap in full HTML with Modern Sidebar Layout
			String response = "<!DOCTYPE html><html lang='en' data-theme='light'><head>" + "<meta charset='UTF-8'>"
					+ "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
					+ "<title>Markdown Batch Render Demo</title>" + "<style>" + demoCss + "</style>" + "<style>"
					+ HtmlCssProvider.getAllCss() + "</style>" + "</head><body>" + "<div class='app-container'>"
					+
					// Sidebar
					"<aside class='sidebar'>" + "<div class='sidebar-header'>⚡ Batch Render</div>"
					+ "<a href='#' class='nav-item active'>Documentation</a>"
					+ "<a href='#' class='nav-item'>API Reference</a>" + "<a href='#' class='nav-item'>Examples</a>"
					+ "<div class='spacer'></div>"
					+ "<button class='theme-toggle' onclick='toggleTheme()'>Toggle Theme</button>" + "</aside>"
					+
					// Main Content
					"<main class='main-content'>" + "<div class='markdown-root' data-theme='light'>" + htmlContent
					+ "</div>" + "</main>"
					+ "</div>"
					+
					// Script
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
					+ "</script>" + "</body></html>";

			byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
			t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
			t.sendResponseHeaders(200, bytes.length);
			try (OutputStream os = t.getResponseBody()) {
				os.write(bytes);
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
		try (InputStream is = BatchServerDemo.class.getResourceAsStream("/comprehensive.md")) {
			if (is != null) {
				return new String(is.readAllBytes(), StandardCharsets.UTF_8);
			} else {
				try (InputStream is2 = BatchServerDemo.class.getResourceAsStream("/template.md")) {
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
