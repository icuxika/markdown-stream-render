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
    *   **Robust Parsing**: Correctly handles edge cases like tab expansion, nested lists, and complex block interactions.
    *   **Image Caching**: JavaFX renderer includes smart caching to prevent flickering during streaming updates.
    *   **Secure**: Includes a "Safe Mode" to sanitize output by filtering disallowed raw HTML (XSS prevention).
    *   **High Performance**: Optimized for speed with zero-allocation line processing and true streaming architecture.

## 📂 Project Structure

*   **`core`**: The heart of the project. Contains the `MarkdownParser`, AST nodes, and `HtmlRenderer`.
*   **`javafx`**: Contains the `JavaFxRenderer` for rendering Markdown to JavaFX nodes.
*   **`benchmark`**: JMH benchmarks for performance testing.
*   **`demo`**: Example applications demonstrating usage.
    *   `GuiApp`: A simple JavaFX Markdown editor.
    *   `StreamingGuiApp`: Demonstrates streaming rendering in JavaFX (simulates typing).
    *   `StreamingHtmlDemo`: A local HTTP server demonstrating streaming HTML rendering via Server-Sent Events (SSE).

## 🚀 Quick Start

### Prerequisites
*   Java 25 (or latest Preview capable JDK)
*   Maven

### Build the Project
```bash
mvn clean install
```

### Run the Demos

**1. JavaFX Streaming Demo**
Visualizes the streaming capability by simulating a typewriter effect in a desktop window.
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.StreamingGuiApp"
```

**2. HTML Streaming Demo**
Starts a local web server. Open your browser to watch the Markdown being rendered and streamed in real-time.
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.StreamingHtmlDemo"
```

**3. JavaFX Editor**
A basic editor where you can type Markdown and see the result.
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.GuiApp"
```

**4. DeepSeek Chat Demo (AI Streaming)**
A chat interface interacting with DeepSeek API. Requires API Key.
```bash
# Set environment variable first
# Windows (PowerShell): $env:DEEPSEEK_API_KEY="your-key"
# Linux/Mac: export DEEPSEEK_API_KEY="your-key"
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.DeepSeekChatDemo"
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

*   **Enhanced JavaFX Styling**: Add support for complex table borders and syntax highlighting for code blocks.
*   **Source Mapping**: Track source positions for AST nodes to enable synchronized scrolling.
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
