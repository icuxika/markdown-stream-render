# Markdown Stream Renderer

[English](README.md) | [中文](README_CN.md)

A high-performance, **CommonMark-compliant**, streaming Markdown parser and renderer for Java. It supports incremental parsing and multiple output targets, including HTML and JavaFX.

![Status](https://img.shields.io/badge/CommonMark%20Spec-100%25%20Passing-brightgreen)
![Java](https://img.shields.io/badge/Java-25%2B-blue)

## Key Features

*   **100% CommonMark Compliance**: Passes all **652** tests from the CommonMark Spec (v0.31.2).
*   **Streaming Architecture**: Designed for incremental processing. Capable of rendering content as it is being typed or received over the network, without waiting for the full document.
*   **Multi-Target Rendering**:
    *   **HTML**: Generates standard, compliant HTML.
    *   **JavaFX**: Renders directly to a JavaFX Scene Graph. Includes a **High-Performance Hybrid Renderer** (`VirtualJavaFxStreamRenderer`) that combines virtualization with real-time streaming for infinite-length documents.
*   **Zero Dependencies (Core)**: The core module has no external dependencies, making it lightweight and easy to embed.
*   **Advanced Features**:
    *   **Built-in GFM Support**: GitHub Flavored Markdown features (Tables, Task Lists, Strikethrough, Autolinks) are integrated directly into the core parser for maximum performance (toggleable via options).
    *   **Rich JavaFX Styling**: Includes enhanced styling for Tables (zebra striping, borders), Blockquotes, and Code Blocks.
    *   **Extensions**: Includes separate extension modules for **Admonitions** and **Math** (inline equations).

## JavaFX Styling & Theming

The JavaFX renderer uses a modern, CSS-based styling system.
See [javafx_theming.md](docs/javafx_theming.md) for recommended style loading and custom theme patterns.

### Built-in Themes
The library comes with `Light` and `Dark` themes. You can switch themes using the `MarkdownTheme` helper:

```java
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;

MarkdownTheme theme = new MarkdownTheme();
theme.apply(scene); // Loads markdown.css + extension styles + default Light theme variables
theme.setTheme(MarkdownTheme.Theme.DARK); // Switch to Dark theme
```

If you want to fully customize the theme, load the base markdown styles and then add your own stylesheet:

```java
import com.icuxika.markdown.stream.render.javafx.MarkdownStyles;

MarkdownStyles.applyBase(scene, true);
scene.getStylesheets().add(getClass().getResource("/your-theme.css").toExternalForm());
```

### CSS Variables Reference
| Variable | Description | Default (Light) |
| :--- | :--- | :--- |
| `-md-fg-color` | Main text color | `#24292f` |
| `-md-bg-color` | Background color | `#ffffff` |
| `-md-link-color` | Link color | `#0969da` |
| `-md-code-bg-color` | Background for inline code/blocks | `#f6f8fa` |
| `-md-border-color` | Border color for tables/blocks | `#d0d7de` |

## Project Structure

*   **`core`**: Parser + AST + renderer interfaces (framework-agnostic).
*   **`html`**: HTML renderer + spec-driven conformance tests.
*   **`javafx`**: JavaFX renderer + themes/CSS resources.
*   **`benchmark`**: JMH benchmarks for performance testing.
*   **`demo`**: Example applications demonstrating usage.
    *   `Launcher`: Demo launcher for JavaFX and server demos.
    *   `TypewriterPreviewDemo`: Char-level streaming preview demo.

## Quick Start

### Prerequisites
*   Java 25 (or latest JDK with Preview features)
*   Maven

### Build
```bash
mvn clean install
```

### Run Demos

**1. Modern AI Chat Demo**
A modern SaaS-style AI chat interface with streaming Markdown rendering.
```bash
mvn -pl demo exec:java "-DmainClass=com.icuxika.markdown.stream.render.demo.javafx.modernchat.ModernAiChatDemo"
```

**2. ChirpChat - Twitter Style Chat App**
A Twitter-style social chat application with login, feed, messages, and notifications.
```bash
mvn -pl demo exec:java "-DmainClass=com.icuxika.markdown.stream.render.demo.javafx.chirpchat.ChirpChatApp"
```

**3. Virtualized Streaming Demo (Recommended)**
The flagship demo showcasing the high-performance hybrid renderer with simulated LLM streaming.
```bash
mvn -pl demo exec:java "-DmainClass=com.icuxika.markdown.stream.render.demo.javafx.VirtualStreamRenderDemo"
```

**4. Virtualized List Stress Test**
Tests the renderer against massive documents (4000+ lines) to verify memory efficiency and scroll performance.
```bash
mvn -pl demo exec:java "-DmainClass=com.icuxika.markdown.stream.render.demo.javafx.VirtualListDemo"
```

**5. Typewriter Preview Demo**
Simple char-level streaming preview.
```bash
mvn -pl demo exec:java "-DmainClass=com.icuxika.markdown.stream.render.demo.javafx.TypewriterPreviewDemo"
```

## Architecture & Design

The project follows a modular, event-driven architecture to support streaming:

1.  **Tokenizer (Lexer)**: Processes input character by character (or line by line).
2.  **Parser**: 
    *   **Block Parsing**: Determines the structure (Paragraphs, Lists, Block Quotes) using a state machine (`BlockParserState`). Handles "Lazy Continuation" where blocks continue across lines.
    *   **Inline Parsing**: Parses text styles (Bold, Italic, Links) within blocks.
3.  **AST (Abstract Syntax Tree)**: Builds a lightweight tree of `Node` objects (`Block` and `Inline`).
4.  **Renderer**: Traverses the AST (Visitor Pattern) to generate output.

## Spec Compliance

The core parser has been verified against the official CommonMark specification.

*   **CommonMark**: 652/652 (v0.31.2) via `html` module tests.
*   **GFM**: Extension sections only (resource pinned to v0.29.0).
See [spec_coverage.md](docs/spec_coverage.md) for scope details.

To generate a local CommonMark report file (optional):
```bash
mvn -pl html test -DgenerateSpecReport=true
```

## Future Roadmap

The core functionality of `markdown-stream-render` is now stable and feature-complete for most streaming scenarios. Future development will focus on:

*   **Plugin System**: Formalizing the API for custom block and inline parsers to allow users to add syntax like Mermaid diagrams or Footnotes without forking.
*   **Performance Optimization**: Further tuning of the `VirtualJavaFxStreamRenderer` for mobile devices or constrained environments.
*   **More Output Targets**: Investigating support for Swing or Terminal (ANSI) output.
*   **GFM Completeness**: Gradually adding support for missing GFM features (like raw HTML tag filtering in core).

**Note**: No major API-breaking changes are planned for the 1.x series.

