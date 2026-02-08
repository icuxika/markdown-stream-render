# Markdown Stream Render - AI Implementation Guide

## 1. Project Objective
Create a high-performance, extensible Markdown rendering engine in Java designed specifically for **streaming contexts** (e.g., Large Language Model outputs). 

**Key Requirement**: The engine must support incremental parsing and rendering, allowing content to appear on screen immediately as text chunks are received, without waiting for the full document to complete.

### **Critical Requirement: Spec-Driven Development**
This project strictly follows a **Spec-Driven Development** approach. The correctness of the parser is defined solely by its ability to pass the official specification tests.

*   **Test Suites Location**: `html/src/test/resources/`
    *   `commonmark-spec-0.31.2.json`: The CommonMark specification test suite (pinned).
    *   `gfm-spec-0.29.0.json`: A pinned GFM spec JSON used for extension coverage (not full GFM conformance).
*   **Success Criteria**: The parser implementation is considered complete ONLY when it passes the test cases defined in these spec files.
*   **Workflow**: 
    1.  Create a test harness in the `html` module that reads the JSON spec files.
    2.  For each test case (markdown input -> expected html), run the parser and compare the output.
    3.  Iterate on the `core` parser logic until all tests pass.
*   **Optional Reports**: Spec reports are generated on-demand (disabled by default) to avoid committing stale snapshots.

## 2. Technical Architecture
The project is organized into a multi-module Maven project:

1.  **`core`**: AST + parsing logic + renderer interfaces. **Must remain framework-agnostic**.
2.  **`html`**: HTML renderer and spec-driven conformance tests.
3.  **`javafx`**: JavaFX renderer and themes/CSS resources.
4.  **`demo`**: Runnable applications showcasing capabilities.

---

## 3. Implementation Roadmap

### Phase 1: The Foundation (AST)
**Goal**: Define the data structures representing a Markdown document.

1.  **Base Node System**:
    *   Create an abstract `Node` class.
    *   Implement a doubly-linked list structure (`parent`, `previous`, `next`, `firstChild`, `lastChild`) for easy tree traversal.
2.  **Node Types**:
    *   **Block Nodes** (Structural): `Document` (root), `Paragraph`, `Heading` (1-6), `BlockQuote`, `CodeBlock`, `List` (Ordered/Bullet), `ListItem`, `ThematicBreak`, `Table`.
    *   **Inline Nodes** (Content): `Text`, `Emphasis` (italic), `StrongEmphasis` (bold), `Code` (inline), `Link`, `Image`, `HardBreak`, `SoftBreak`.
3.  **Visitor Pattern**:
    *   Define a `Visitor` interface with `visit(NodeType node)` methods for all node types.
    *   Implement `accept(Visitor visitor)` in all nodes.

### Phase 2: The Core Parser (Block & Inline)
**Goal**: Convert raw Markdown text into the AST defined in Phase 1. Follow the **CommonMark** spec strategy.

1.  **Block Parsing**:
    *   Implement a line-by-line processor.
    *   **`BlockParser` Interface**: Defines `tryContinue(state)` to check if an open block matches the current line.
    *   **`BlockParserFactory`**: Registry for identifying block starts (e.g., `>` for BlockQuote, `#` for Heading).
    *   **State Management**: Maintain a list of "Open Blocks". For each new line, check which open blocks continue, close those that don't, and open new ones if matched.
2.  **Inline Parsing**:
    *   After a block (like Paragraph) is closed, parse its text content.
    *   Implement scanners for delimiters (`*`, `_`, `` ` ``, `[`, `!`).
    *   Construct the inline node tree and attach it to the parent block.

### Phase 3: The Stream Engine
**Goal**: The "Secret Sauce". Enable incremental processing.

1.  **`StreamMarkdownParser`**:
    *   **Input**: A `push(String text)` method that accepts partial strings.
    *   **Buffering**: Internally buffer characters until a newline (`\n`) is detected, as Markdown blocks are fundamentally line-based.
    *   **Event Dispatch**:
        *   `onBlockStarted(Node)`: When a new block (e.g., BlockQuote) is detected.
        *   `onBlockClosed(Node)`: When a block ends.
        *   `onBlockFinalized(Node)`: When a block is fully parsed (including inlines) and ready for final rendering.
    *   **Design Note**: This parser wraps the Core Parser but manages the input buffer and lifecycle events.

### Phase 4: The Renderer Interfaces
**Goal**: Define "Contract" for rendering, decoupling parsing from UI.

1.  **`MarkdownRenderer`** (Static):
    *   Method: `render(Document doc)`.
    *   For rendering a complete, static document.
2.  **`StreamMarkdownRenderer`** (Dynamic):
    *   Methods: `openBlock(Node)`, `closeBlock(Node)`, `renderNode(Node)`.
    *   Designed to react to `StreamMarkdownParser` events.

### Phase 5: JavaFX Implementation
**Goal**: Bring the AST to life on screen.

1.  **Component Mapping**:
    *   `Paragraph` -> `TextFlow`
    *   `CodeBlock` -> `VBox` + `Label` (with styling)
    *   `List` -> `VBox`
    *   `Text` -> `Text` (JavaFX Node)
2.  **`JavaFxRenderer` (Static)**:
    *   Traverse the full AST and build a JavaFX Node tree.
3.  **`JavaFxStreamRenderer` (Dynamic)**:
    *   **Container Stack**: Maintain a `Stack<Pane>` to track nesting (e.g., inside a BlockQuote -> inside a List).
    *   **Incremental Updates**:
        *   `openBlock`: Create a container (e.g., `VBox` for BlockQuote), add it to the current parent, and push it to the stack.
        *   `renderNode`: Create the leaf node (e.g., `TextFlow`) and add it to the current container.
        *   `closeBlock`: Pop the stack.
    *   **Thread Safety**: Ensure all UI manipulations happen on `Platform.runLater`.

## 4. Key Challenges & Solutions

### 1. The "Typewriter" Effect
*   **Challenge**: Standard parsers wait for a block to close before rendering. This feels laggy for long paragraphs.
*   **Solution**:
    *   For **Code Blocks**: Render the block container immediately on `openBlock`. As lines arrive, append them to the content area dynamically without waiting for the closing ```.
    *   For **Text**: `StreamMarkdownParser` should ideally support an "intermediate" text event if possible, or at least finalize paragraphs line-by-line.

### 2. Styling
*   Use CSS for all styling. Assign CSS classes (e.g., `.markdown-h1`, `.markdown-code`) to JavaFX nodes.
*   Avoid hardcoding fonts or colors in Java code.

### 3. Scroll & Layout
*   Use `ScrollPane` + `VBox` for the main chat interface.
*   Implement "Stick to Bottom" logic: When new content is added, auto-scroll to the bottom if the user was already at the bottom.

## 5. Development Checklist
- [ ] **Core**: AST Node definitions.
- [ ] **Core**: Block Parser (Paragraph, Heading, List, BlockQuote, CodeBlock).
- [ ] **Core**: Inline Parser (Emphasis, Strong, Code, Link).
- [ ] **Core**: StreamMarkdownParser with event callbacks.
- [ ] **JavaFX**: Base CSS styles.
- [ ] **JavaFX**: Node renderers (CoreJavaFxNodeRenderer).
- [ ] **JavaFX**: JavaFxStreamRenderer implementation.
- [ ] **Demo**: Chat UI with a mock LLM stream source.
