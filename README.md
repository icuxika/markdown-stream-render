# Markdown Stream Renderer

[English](README.md) | [中文](README_CN.md)

A high-performance, **CommonMark-compliant**, streaming Markdown parser and renderer for Java. It supports incremental parsing and multiple output targets, including HTML and JavaFX.

![Status](https://img.shields.io/badge/CommonMark%20Spec-100%25%20Passing-brightgreen)
![Java](https://img.shields.io/badge/Java-25%2B-blue)

## 🌟 Key Features

*   **100% CommonMark Compliance**: Passes all **672** tests from the CommonMark Spec (v0.29.0).
*   **Streaming Architecture**: Designed for incremental processing. Capable of rendering content as it is being typed or received over the network, without waiting for the full document.
*   **Multi-Target Rendering**:
    *   **HTML**: Generates standard, compliant HTML.
    *   **JavaFX**: Renders directly to a JavaFX Scene Graph (`VBox`, `TextFlow`, `GridPane`) for rich desktop applications.
*   **Zero Dependencies (Core)**: The core module has no external dependencies, making it lightweight and easy to embed.
*   **Advanced Features**:
    *   **GitHub Flavored Markdown (GFM)**: Supports **Tables**, **Task Lists**, Strikethrough, and Extended Autolinks.
    *   **Rich JavaFX Styling**: Includes enhanced styling for Tables (zebra striping, borders), Blockquotes, and Code Blocks.
    *   **Syntax Highlighting**: Built-in lightweight syntax highlighting for **Java** and **JSON** code blocks in JavaFX.
    *   **Extensions**: Includes support for **Admonitions** (Info/Warning/Error blocks) and **Math** (inline equations).
    *   **Image Caching**: JavaFX renderer includes smart caching to prevent flickering during streaming updates.
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
    *   `JavaFxBatchDemo`: A simple JavaFX Markdown editor with "Render" button.
    *   `JavaFxStreamDemo`: Demonstrates streaming rendering with "Typewriter" and "LLM Token" simulation modes.
    *   `HtmlBatchServerDemo`: Serves a static HTML rendering of a template file.
    *   `HtmlStreamServerDemo`: A local HTTP server demonstrating streaming HTML rendering via Server-Sent Events (SSE).

## 🚀 Quick Start

### Prerequisites
*   Java 25 (or latest JDK with Preview features)
*   Maven

### Build
```bash
mvn clean install
```

### Run Demos

**1. Streaming Fx Demo (LLM Simulation)**
Simulates an AI/LLM token stream or typewriter effect in a desktop window to visualize streaming rendering capabilities.
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.JavaFxStreamDemo"
```

**2. Batch Fx Demo (Editor)**
A basic editor where you can type Markdown and see the result instantly.
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.JavaFxBatchDemo"
```

**3. Streaming HTML Demo (Server-Sent Events)**
Starts a local web server. Open your browser to see Markdown rendered and pushed in real-time.
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.HtmlStreamServerDemo"
```

## 📐 Architecture & Design

The project follows a modular, event-driven architecture to support streaming:

1.  **Tokenizer (Lexer)**: Processes input character by character (or line by line).
2.  **Parser**: 
    *   **Block Parsing**: Determines the structure (Paragraphs, Lists, Block Quotes) using a state machine (`BlockParserState`). Handles "Lazy Continuation" where blocks continue across lines.
    *   **Inline Parsing**: Parses text styles (Bold, Italic, Links) within blocks.
3.  **AST (Abstract Syntax Tree)**: Builds a lightweight tree of `Node` objects (`Block` and `Inline`).
4.  **Renderer**: Traverses the AST (Visitor Pattern) to generate output.

## 📊 Spec Compliance Report

The core parser has been verified against the official CommonMark specification.

| Category | Status |
| :--- | :--- |
| Block Structure | ✅ 100% |
| Inline Structure | ✅ 100% |
| HTML Rendering | ✅ 100% |
| **Total (672/672)** | **✅ PASS** |
