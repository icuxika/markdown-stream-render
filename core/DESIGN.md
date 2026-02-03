# Markdown Stream Renderer Design

## 1. Project Overview
Goal: Implement a CommonMark-compliant Markdown renderer that supports streaming input and multiple output targets (HTML, JavaFX).
Core Logic: Located in the `core` module.

## 2. Architecture

### 2.1 Core Concepts
To support "streaming" (incremental rendering) and "multiple targets", we will use an **Event-Driven Architecture** (similar to SAX for XML) combined with a **Visitor Pattern** for AST traversal when needed.

#### Data Flow:
`Input Source` -> `Tokenizer (Lexer)` -> `Parser` -> `AST / Event Stream` -> `Renderer` -> `Output`

### 2.2 Modules
- **`core`**: Contains the Parser, AST definitions, and Renderer interfaces.
- **`html-renderer`** (or inside core): HTML implementation.
- **`javafx-renderer`**: JavaFX implementation (requires JavaFX dependencies).

### 2.3 Key Components in `core`

#### A. AST (Abstract Syntax Tree)
Although "streaming" implies processing on the fly, CommonMark structure often requires block completion (e.g., distinguishing a paragraph from a Setext heading). We will build a lightweight AST but expose a streaming API where possible.

*   `Node` (Abstract Base)
    *   `Block` (Paragraph, Heading, List, etc.)
    *   `Inline` (Text, Emphasis, Link, etc.)

#### B. Parser (`MarkdownParser`)
*   **Phase 1: Block Parsing**: Reads lines and determines block structure.
*   **Phase 2: Inline Parsing**: Parses text within blocks.
*   **Streaming aspect**: The parser will emit events or finalized blocks as soon as they are closed.

#### C. Renderer Interface (`IMarkdownRenderer`)
We will define an interface that renderers must implement.

```java
public interface IMarkdownRenderer<T> {
    void render(Node node, T output);
    // Or for streaming:
    void consume(RenderEvent event);
}
```

## 3. Implementation Plan

### Step 1: Core AST & Interfaces
- Define `Node`, `Block`, `Inline` classes.
- Define `Visitor` interface for traversing the AST.
- Define `Parser` interface.

### Step 2: Basic Block Parser
- Implement a parser that can recognize simple blocks (Headings, Paragraphs).
- Implement a simple "Stream" mechanism (e.g., accepting a `String` or `Reader`).

### Step 3: HTML Renderer
- Implement `HtmlRenderer` that produces HTML strings.

### Step 4: JavaFX Renderer
- Implement `JavaFxRenderer` that builds a `VBox` or `TextFlow` scene graph.

## 4. Technology Stack
- **Language**: Java 25 (Preview/Latest)
- **Build Tool**: Maven
- **Dependencies**: None for `core` (keep it lightweight).

