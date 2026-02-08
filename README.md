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
    *   **JavaFX**: Renders directly to a JavaFX Scene Graph (`VBox`, `TextFlow`, `GridPane`) for rich desktop applications.
*   **Zero Dependencies (Core)**: The core module has no external dependencies, making it lightweight and easy to embed.
*   **Advanced Features**:
    *   **Partial GitHub Flavored Markdown (GFM)**: Supports a subset of extensions (e.g., tables/task lists/strikethrough/extended autolinks) without targeting full GFM conformance.
    *   **Rich JavaFX Styling**: Includes enhanced styling for Tables (zebra striping, borders), Blockquotes, and Code Blocks.
    *   **Extensions**: Includes support for **Admonitions** and **Math** (inline equations).

## JavaFX Styling & Theming

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

**1. Demo Launcher**
```bash
mvn -pl demo -am exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.Launcher"
```

**2. Typewriter Preview Demo**
```bash
mvn -pl demo -am exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.javafx.TypewriterPreviewDemo"
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
*   **GFM**: A subset of extension examples are covered (resource pinned to v0.29.0).

To generate a local CommonMark report file (optional):
```bash
mvn -pl html test -DgenerateSpecReport=true
```
