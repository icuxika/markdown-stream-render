# JavaFX Module

This module provides a high-performance JavaFX renderer for the `markdown-stream-render` library. It translates Markdown AST nodes directly into JavaFX Scene Graph nodes (`VBox`, `TextFlow`, `Label`, etc.), supporting rich styling and interactivity.

## Key Components

### 1. `JavaFxRenderer` (Static)
The standard renderer for converting a full Markdown document (AST) into a JavaFX Node hierarchy.

**Usage:**
```java
MarkdownParser parser = MarkdownParser.builder().build();
Node document = parser.parse(markdownText);

JavaFxRenderer renderer = new JavaFxRenderer();
javafx.scene.Node root = renderer.render(document);
```

### 2. `VirtualJavaFxStreamRenderer` (Streaming & Virtualized)
A specialized renderer designed for **high-performance streaming** scenarios (e.g., AI chat interfaces, large log viewing).

**Features:**
- **Hybrid Architecture**: Combines a virtualized `ListView` for history with a direct `VBox` for the active streaming block.
- **Memory Efficient**: Only renders visible nodes in the history list; destroys UI nodes for blocks that scroll out of view.
- **Smooth Typing Effect**: Renders the currently generating block in real-time without the jitter or performance cost of full list updates.
- **Thread Safety**: Built-in `uiTaskQueue` ensures all parser events are processed sequentially on the JavaFX Application Thread.
- **Time Slicing**: Limits UI processing time per frame (e.g., 8ms) to prevent UI freeze during high-speed data streaming.

**Usage:**
```java
// 1. Data Model for History
ObservableList<Node> markdownNodes = FXCollections.observableArrayList();

// 2. Active Container for Streaming Content
VBox activeStreamBox = new VBox();

// 3. Initialize Renderer
VirtualJavaFxStreamRenderer renderer = new VirtualJavaFxStreamRenderer(
    markdownNodes,      // History list
    activeStreamBox,    // Active container
    () -> {             // Auto-scroll callback
        listView.scrollTo(markdownNodes.size() - 1);
    }
);

// 4. Connect to Parser
StreamMarkdownParser parser = StreamMarkdownParser.builder()
    .renderer(renderer)
    .build();

// 5. Push Data
parser.push("Some markdown content...");
```

### 3. Styling & Themes
The renderer uses standard JavaFX CSS.
- **Base Styles**: `markdown.css`
- **Themes**: `markdown-light.css` / `markdown-dark.css`

See [JavaFX Theming](../docs/javafx_theming.md) for customization details.
