# Markdown Stream Render 项目交接指南

## 1. 项目概述

**Markdown Stream Render** 是一个基于 Java 的 Markdown 解析与渲染引擎。与常见的全量解析器（如 CommonMark-Java）不同，本项目的核心特性是**流式（增量）解析与渲染**。

它允许在 Markdown 文本还在生成（例如 LLM 逐字输出）的过程中，实时解析并渲染 UI，而无需等待完整文档生成完毕。

### 核心能力
*   **流式解析**：通过 `push(String)` 接收片段，实时触发 AST 事件。
*   **多端渲染**：
    *   **JavaFX**: 原生节点渲染，支持复杂的 CSS 样式和自定义组件。
    * **HTML**: 支持 HTTP Chunked Transfer Encoding 的流式 HTML 输出，也支持全量 HTML 生成。
*   **插件系统**：支持 Admonition（提示块）、Math（数学公式）等扩展。

## 2. 模块结构

项目采用 Maven 多模块结构：

| 模块 | 路径 | 职责 |
| :--- | :--- | :--- |
| **core** | [core](core) | **核心逻辑**。包含 AST 定义、解析器状态机 (`MarkdownParser`)、流式解析器 (`StreamMarkdownParser`) 以及流式渲染接口（`StreamMarkdownRenderer`/`StreamMarkdownTypingRenderer`）。无 UI 依赖。 |
| **html** | [html](html) | **HTML 实现**。包含 HTML 全量/流式渲染器、CSS 样式资源 (`markdown.css` 及扩展样式)。 |
| **javafx** | [javafx](javafx) | **JavaFX 实现**。包含 JavaFX 渲染器、CSS 样式表、自定义控件（如数学公式视图）。 |
| **demo** | [demo](demo) | **演示程序**。包含启动器 (`Launcher`) 和各类流式/非流式 Demo。 |

## 3. 关键架构说明

### 3.1 流式解析机制 (The Streaming Contract)

这是本项目最复杂也是最重要的部分。

*   **解析器**: [StreamMarkdownParser](core/src/main/java/com/icuxika/markdown/stream/render/core/parser/StreamMarkdownParser.java)
    *   维护一个内部 buffer 和状态机。
    *   当通过 `push()` 输入文本时，它会判断块（Block）的状态。
    *   **关键事件**:
        1.  `openBlock(Node)`: 一个容器块（如引用、列表、Admonition）开始了。
        2.  `renderNode(Node)`: 一个叶子节点（如段落、标题、代码块）内容解析完毕，可以渲染。
        3.  `closeBlock(Node)`: 容器块结束。

*   **渲染器接口**: [StreamMarkdownRenderer](core/src/main/java/com/icuxika/markdown/stream/render/core/renderer/StreamMarkdownRenderer.java)
    *   实现此接口以适配不同的输出端。
*   **打字机预览接口（可选）**: [StreamMarkdownTypingRenderer](core/src/main/java/com/icuxika/markdown/stream/render/core/renderer/StreamMarkdownTypingRenderer.java)
    *   在流式输入尚未形成完整块时，允许渲染“预览节点”。

### 3.2 JavaFX 渲染实现

*   **实现类**: [JavaFxStreamRenderer](javafx/src/main/java/com/icuxika/markdown/stream/render/javafx/renderer/JavaFxStreamRenderer.java)
*   **容器栈 (`Stack<Pane>`)**:
    *   为了正确处理嵌套结构（如 Admonition 包含 列表 包含 段落），渲染器维护了一个 UI 容器栈。
    *   `openBlock`: 创建对应的 JavaFX 容器（设置样式类），压入栈顶，并添加到父容器。
    *   `renderNode`: 将内容渲染并添加到**栈顶**容器中。
    *   `closeBlock`: 弹出栈顶容器。
* **样式与主题**:
  * JavaFX 的 Markdown 样式与主题变量位于 `javafx` 模块资源目录下。
  * 推荐在 `Scene` 级别加载样式（通过 `MarkdownTheme.apply(scene)` 或 `MarkdownStyles.applyBase(scene, ...)`），渲染器本身不会自动注入 `Scene` 样式表，以避免主题切换/覆盖顺序不受控。

### 3.3 HTML 渲染实现 (最新重构)

* **模块位置**: [html](html)
* **渲染器**:
    * `HtmlRenderer`: 全量渲染，将 Document 转换为完整 HTML 字符串。
    * `HtmlStreamRenderer`: 流式渲染，实现 `IStreamMarkdownRenderer` 接口，配合流式解析器输出 HTML 片段。
* **样式**:
    * CSS 资源已内置于 `html` 模块的 `resources` 目录。
    * 使用 [HtmlCssProvider](html/src/main/java/com/icuxika/markdown/stream/render/html/HtmlCssProvider.java) 获取样式内容。

## 4. 最新变更与注意事项 (Handover Notes)

接手者请特别注意以下最近完成的改动：

1. **HTML 模块重构与解耦**:
    * HTML 渲染逻辑已从 `core` 彻底移至 `html` 模块。`core` 现在保持纯净，不含任何 HTML 生成代码。
    * `html` 模块现在包含自己的 CSS 资源（`markdown.css`, `admonition.css`, `math.css`），不再依赖外部提供样式。

2. **扩展解析器注册机制**:
    * **默认加载**: `MarkdownParser` 和 `StreamMarkdownParser` 现在默认会自动加载 Admonition 和 Math 扩展，无需手动配置。
    * **自定义扩展**: 如需加载其他扩展，请使用 Builder 的 `.extensions()` 方法。
    * Demo 代码已更新以遵循此规范。

3. **文档更新**:
    * 新增 [ARCHITECTURE.md](docs/ARCHITECTURE.md): 项目分层架构图。
    * 新增 [MODULE_EXPORTS.md](docs/MODULE_EXPORTS.md): 模块化 API 导出规范。
    * 更新 [developer_guide.md](docs/developer_guide.md): 包含了 HTML 模块的使用指南。

## 5. 待办事项与未来规划 (Roadmap)

给 AI 或人类接手者的建议任务：

*   **性能优化 (High)**:
    * 当前 JavaFX `VBox` 在节点数量极多（如数千行对话）时性能会下降。建议引入虚拟化列表机制。
* **测试覆盖 (Medium)**:
    * HTML 模块目前主要依赖 Demo 验证。建议增加对 `HtmlRenderer` 输出 HTML 结构的单元测试断言。
* **新插件支持 (Low)**:
    * Mermaid 图表支持（需集成 WebView 或 JS 引擎）。
    * 脚注支持。

## 6. 如何运行

1. **构建项目**:
   ```bash
   mvn clean install
   ```
   *(注意：首次运行必须执行 install，因为各模块间存在依赖)*

2. **运行 Demo**:
    * 定位到 `demo` 模块。
    * 运行 `com.icuxika.markdown.stream.render.demo.Launcher` 类。
    * **JavaFX Streaming Demo**: 验证桌面端流式渲染。
    * **HTML Streaming Server**: 验证 Web 端流式渲染（浏览器自动打开）。
    * **HTML Batch Server**: 验证 Web 端全量渲染。

***

**致 AI 接手者**:
当你处理 HTML 渲染相关问题时，请记住样式文件位于 `html` 模块的资源目录中，通过 `HtmlCssProvider`
加载。当你处理解析问题（如某种语法不生效）时，首先检查是否在 Parser Builder 中注册了相应的 `ParserFactory`。
