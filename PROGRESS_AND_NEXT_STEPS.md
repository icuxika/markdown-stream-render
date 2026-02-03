# Progress and Next Steps

## Completed
- **Project Structure**: Core module and Demo module.
- **Block Parsing**:
  - Paragraphs
  - ATX Headings
  - Thematic Breaks
  - List Items
  - Fenced Code Blocks
  - Indented Code Blocks
  - Block Quotes
  - Lists (Loose/Tight, Nesting)
  - HTML Blocks
  - Setext Headings
- **Inline Parsing**:
  - Textual Content
  - Emphasis and Strong Emphasis
  - Code Spans
  - Autolinks
  - Raw HTML
  - Hard and Soft Line Breaks
  - Backslash Escapes
  - Entity References
  - Link Reference Definitions
  - Links & Images (including Alt Text parsing)
  - Tabs (Virtual expansion)
- **Rendering**:
  - HtmlRenderer (Full support for implemented nodes)
- **Demo**:
  - Console application demonstrating features.

## Pending / In Progress
- **JavaFX Renderer**: Not started.

## Next Steps Plan
1.  **Add JavaFX Dependencies**: Add OpenJFX to `core` module.
2.  **Implement JavaFX Renderer**: Create `JavaFxRenderer` extending `IMarkdownRenderer` (or similar interface) to produce a `VBox` or `FlowPane` of nodes.
3.  **Update Demo**: Create a JavaFX application in `demo` module to visualize the rendering.
