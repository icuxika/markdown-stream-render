# Markdown Stream Renderer

[English](README.md) | [中文](README_CN.md)

A high-performance, **CommonMark-compliant**, streaming Markdown parser and renderer for Java. It supports incremental parsing and multiple output targets, including HTML and JavaFX.

![Status](https://img.shields.io/badge/CommonMark%20Spec-100%25%20Passing-brightgreen)
![Java](https://img.shields.io/badge/Java-25%2B-blue)

## 🌟 Key Features

*   **100% CommonMark Compliance**: Passes all **652** tests from the CommonMark Spec (v0.31.2).
*   **Streaming Architecture**: Designed for incremental processing. Capable of rendering content as it is being typed or received over the network, without waiting for the full document.
*   **Multi-Target Rendering**:
    *   **HTML**: Generates standard, compliant HTML.
    *   **JavaFX**: Renders directly to a JavaFX Scene Graph (`VBox`, `TextFlow`, `GridPane`) for rich desktop applications.
*   **Zero Dependencies (Core)**: The core module has no external dependencies, making it lightweight and easy to embed.
*   **Advanced Features**:
    *   **GitHub Flavored Markdown (GFM)**: Supports Tables, Task Lists, Strikethrough, and Extended Autolinks.
    *   **Rich JavaFX Styling**: Includes enhanced styling for Tables (zebra striping, borders), Blockquotes, and Code Blocks.
    *   **Syntax Highlighting (Demo)**: Demonstrates real-time Markdown syntax highlighting in the JavaFX editor demo.
    *   **Robust Parsing**: Correctly handles edge cases like tab expansion, nested lists, and complex block interactions.
    *   **Image Caching**: JavaFX renderer includes smart caching to prevent flickering during streaming updates.
    *   **Secure**: Includes a "Safe Mode" to sanitize output by filtering disallowed raw HTML (XSS prevention).
    *   **High Performance**: Optimized for speed with zero-allocation line processing and true streaming architecture.

## 🎨 JavaFX Styling & Theming

The JavaFX renderer uses a modern, CSS-based styling system.

### Built-in Themes
The library comes with `Light` and `Dark` themes. You can switch themes using the `MarkdownTheme` helper:

```java
MarkdownTheme theme = new MarkdownTheme();
theme.apply(scene); // Applies default Light theme
theme.setTheme(MarkdownTheme.Theme.DARK); // Switch to Dark theme
```

### Customizing Styles
You can override the default styles by providing your own CSS file. The renderer uses **Looked-up Colors** for easy customization without redefining all rules.

**Example: Creating a custom "Sepia" theme**

1. Create a `sepia.css` file:
   ```css
   .root {
       -md-fg-color: #5f4b32;
       -md-bg-color: #f4ecd8;
       -md-link-color: #d2691e;
       -md-code-bg-color: #eae0c9;
       /* ... override other variables as needed */
   }
   ```
2. Load it in your application:
   ```java
   scene.getStylesheets().add("path/to/sepia.css");
   ```

### CSS Variables Reference
| Variable | Description | Default (Light) |
| :--- | :--- | :--- |
| `-md-fg-color` | Main text color | `#24292f` |
| `-md-bg-color` | Background color | `#ffffff` |
| `-md-link-color` | Link color | `#0969da` |
| `-md-code-bg-color` | Background for inline code/blocks | `#f6f8fa` |
| `-md-border-color` | Border color for tables/blocks | `#d0d7de` |

## 📂 Project Structure

*   **`core`**: The heart of the project. Contains the `MarkdownParser`, AST nodes, and `HtmlRenderer`.
*   **`javafx`**: Contains the `JavaFxRenderer` for rendering Markdown to JavaFX nodes.
*   **`benchmark`**: JMH benchmarks for performance testing.
*   **`demo`**: Example applications demonstrating usage.
    *   `BatchFxDemo`: A simple JavaFX Markdown editor.
    *   `BatchHtmlDemo`: Serves a static HTML rendering of a template file.
    *   `StreamingFxDemo`: Demonstrates streaming rendering in JavaFX (simulates typing).
    *   `StreamingHtmlDemo`: A local HTTP server demonstrating streaming HTML rendering via Server-Sent Events (SSE).

## 🚀 Quick Start

### Prerequisites
*   Java 25 (or latest JDK with Preview features)
*   Maven

### Build
```bash
mvn clean install
```

### Run Demos

**1. Streaming Fx Demo (Typewriter Effect)**
Simulates a typewriter effect in a desktop window to visualize streaming rendering.
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.JavaFxStreamDemo"
```

**2. Streaming HTML Demo (Server-Sent Events)**
Starts a local web server. Open your browser to see Markdown rendered and pushed in real-time.
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.HtmlStreamServerDemo"
```

**3. Batch HTML Demo (Static Preview)**
Renders `template.md` to a static HTML page and serves it.
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.HtmlBatchServerDemo"
```

**4. Batch Fx Demo (Editor)**
A basic editor where you can type Markdown and see the result instantly.
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.JavaFxBatchDemo"
```

**5. Streaming AI Chat Demo**
A chat interface interacting with DeepSeek API. Requires API Key.
```bash
# Set environment variable first
# Windows (PowerShell): $env:DEEPSEEK_API_KEY="your-key"
# Linux/Mac: export DEEPSEEK_API_KEY="your-key"
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.JavaFxAiChatDemo"
```

## 📐 Architecture & Design

The project follows a modular, event-driven architecture to support streaming:

1.  **Tokenizer (Lexer)**: Processes input character by character (or line by line).
2.  **Parser**: 
    *   **Block Parsing**: Determines the structure (Paragraphs, Lists, Block Quotes) using a state machine (`BlockParserState`). Handles "Lazy Continuation" where blocks continue across lines.
    *   **Inline Parsing**: Parses text styles (Bold, Italic, Links) within blocks.
3.  **AST (Abstract Syntax Tree)**: Builds a lightweight tree of `Node` objects (`Block` and `Inline`).
4.  **Renderer**: Traverses the AST (Visitor Pattern) to generate output.

### Technical Highlights
*   **Tabs Handling**: Implements precise virtual column calculation to handle mixed tabs and spaces correctly according to spec.
*   **Ambiguity Resolution**: Solved complex conflicts, such as distinguishing between Setext Headings (`---`) and Table Delimiter Rows (`---|---`).
*   **Unicode Support**: Implements correct case folding for Link Reference Definitions (e.g., handling `ẞ`).

## 🔮 Future Roadmap

*   **Source Mapping**: Track source positions for AST nodes to enable synchronized scrolling (partially implemented).
*   **Incremental Parsing**: True incremental AST updates for editing large documents.
*   **Plugin System**: Allow custom extensions to the parser and renderer.

## 📊 Spec Compliance Report

The core parser has been verified against the official CommonMark specification.
See [SPEC_REPORT.md](SPEC_REPORT.md) for the detailed test execution report.

| Category | Status |
| :--- | :--- |
| Block Structure | ✅ 100% |
| Inline Structure | ✅ 100% |
| HTML Rendering | ✅ 100% |
| **Total (652/652)** | **✅ PASS** |
