# Markdown Stream Render Demos

This module contains example applications demonstrating how to use the `markdown-stream-render` library.

## Available Demos

### 1. ModernAiChatDemo (Modern UI)
**Class:** `com.icuxika.markdown.stream.render.demo.javafx.modernchat.ModernAiChatDemo`

A modern SaaS-style AI chat interface demonstrating the library's capabilities.
- **Features**:
    - Modern UI with sidebar, top navigation, and chat area.
    - Real-time streaming Markdown rendering.
    - Step cards and message components.
    - CSS-based styling with Light/Dark theme support.

### 2. VirtualStreamRenderDemo (Flagship)
**Class:** `com.icuxika.markdown.stream.render.demo.javafx.VirtualStreamRenderDemo`

Demonstrates the **Hybrid Virtualization Architecture** for AI chat scenarios.
- **Features**:
    - Simulates an LLM stream (Token-by-token or Chunk-by-chunk).
    - Uses `VirtualJavaFxStreamRenderer` for high performance.
    - Shows "Active Stream" (typing effect) vs "History" (virtualized list) transition.
    - Supports Fast/Slow stream simulation.

### 3. VirtualListDemo (Stress Test)
**Class:** `com.icuxika.markdown.stream.render.demo.javafx.VirtualListDemo`

A stress test application for the renderer.
- **Content**: Loads a massive Markdown document (`huge_stress_test.md`, ~4000 lines).
- **Purpose**: Verifies that the renderer does not leak memory or lag when handling very large documents.
- **Key Metric**: Scroll smoothness (FPS) and memory stability after rendering thousands of blocks.

### 4. TypewriterPreviewDemo
**Class:** `com.icuxika.markdown.stream.render.demo.javafx.TypewriterPreviewDemo`

A simple dual-pane previewer.
- Left pane: Raw Markdown text input.
- Right pane: Real-time rendered preview.
- Demonstrates basic incremental parsing.

## Running Demos via Maven

You can run any demo using the `exec:java` goal from the project root:

```bash
# Run ModernAiChatDemo
mvn -pl demo exec:java "-DmainClass=com.icuxika.markdown.stream.render.demo.javafx.modernchat.ModernAiChatDemo"

# Run VirtualStreamRenderDemo
mvn -pl demo exec:java "-DmainClass=com.icuxika.markdown.stream.render.demo.javafx.VirtualStreamRenderDemo"

# Run VirtualListDemo
mvn -pl demo exec:java "-DmainClass=com.icuxika.markdown.stream.render.demo.javafx.VirtualListDemo"

# Run TypewriterPreviewDemo
mvn -pl demo exec:java "-DmainClass=com.icuxika.markdown.stream.render.demo.javafx.TypewriterPreviewDemo"
```
