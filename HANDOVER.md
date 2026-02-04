# Markdown Stream Render 项目交接指南

## 1. 项目概述

**Markdown Stream Render** 是一个基于 Java 的 Markdown 解析与渲染引擎。与常见的全量解析器（如 CommonMark-Java）不同，本项目的核心特性是**流式（增量）解析与渲染**。

它允许在 Markdown 文本还在生成（例如 LLM 逐字输出）的过程中，实时解析并渲染 UI，而无需等待完整文档生成完毕。

### 核心能力
*   **流式解析**：通过 `push(String)` 接收片段，实时触发 AST 事件。
*   **多端渲染**：
    *   **JavaFX**: 原生节点渲染，支持复杂的 CSS 样式和自定义组件。
    *   **HTML**: 支持 HTTP Chunked Transfer Encoding 的流式 HTML 输出。
*   **插件系统**：支持 Admonition（提示块）、Math（数学公式）等扩展。

## 2. 模块结构

项目采用 Maven 多模块结构：

| 模块 | 路径 | 职责 |
| :--- | :--- | :--- |
| **core** | [core](core) | **核心逻辑**。包含 AST 定义、解析器状态机 (`MarkdownParser`)、流式解析器封装 (`StreamMarkdownParser`) 以及渲染接口 (`IStreamMarkdownRenderer`)。无 UI 依赖。 |
| **javafx** | [javafx](javafx) | **JavaFX 实现**。包含 JavaFX 渲染器、CSS 样式表、自定义控件（如数学公式视图）。 |
| **demo** | [demo](demo) | **演示程序**。包含启动器 (`Launcher`) 和各类流式/非流式 Demo。 |

## 3. 关键架构说明

### 3.1 流式渲染机制 (The Streaming Contract)

这是本项目最复杂也是最重要的部分。

*   **解析器**: [StreamMarkdownParser](core/src/main/java/com/icuxika/markdown/stream/render/core/parser/StreamMarkdownParser.java)
    *   维护一个内部 buffer 和状态机。
    *   当通过 `push()` 输入文本时，它会判断块（Block）的状态。
    *   **关键事件**:
        1.  `openBlock(Node)`: 一个容器块（如引用、列表、Admonition）开始了。
        2.  `renderNode(Node)`: 一个叶子节点（如段落、标题、代码块）内容解析完毕，可以渲染。
        3.  `closeBlock(Node)`: 容器块结束。

*   **渲染器接口**: [IStreamMarkdownRenderer](core/src/main/java/com/icuxika/markdown/stream/render/core/renderer/IStreamMarkdownRenderer.java)
    *   实现此接口以适配不同的输出端。

### 3.2 JavaFX 渲染实现

*   **实现类**: [JavaFxStreamRenderer](javafx/src/main/java/com/icuxika/markdown/stream/render/javafx/renderer/JavaFxStreamRenderer.java)
*   **容器栈 (`Stack<Pane>`)**:
    *   为了正确处理嵌套结构（如 Admonition 包含 列表 包含 段落），渲染器维护了一个 UI 容器栈。
    *   `openBlock`: 创建对应的 JavaFX 容器（设置样式类），压入栈顶，并添加到父容器。
    *   `renderNode`: 将内容渲染并添加到**栈顶**容器中。
    *   `closeBlock`: 弹出栈顶容器。
*   **样式**: 样式定义在 [markdown.css](javafx/src/main/resources/com/icuxika/markdown/stream/render/javafx/css/markdown.css) 中。渲染器会自动挂载样式表，无需 Demo 层干预。

### 3.3 HTML 流式服务器

*   **实现类**: [HtmlStreamServerDemo](demo/src/main/java/com/icuxika/markdown/stream/render/demo/HtmlStreamServerDemo.java)
*   **原理**: 使用 HTTP `Transfer-Encoding: chunked`。
*   后端解析到一个 Node，就立即转换为 HTML 字符串写入 Socket 输出流，并附带一段 `<script>scrollTo...</script>` 以实现浏览器端的自动滚动。

## 4. 最新变更与注意事项 (Handover Notes)

接手者请特别注意以下最近完成的改动：

1.  **修复了容器块的渲染逻辑**:
    *   之前 Admonition 和 List 的样式丢失，是因为流式解析器没有正确触发 `openBlock`/`closeBlock`。现在已修复。
    *   **注意**: 如果新增自定义 Block，必须确保在 Parser 中正确处理其 Start/Close 状态。

2.  **修复了表格与公式的解析**:
    *   Table 和 Math 属于复杂节点。在流式模式下，现在在 `onBlockFinalized` 阶段会触发递归的 Inline 解析，确保表格内容和公式能被正确渲染。

3.  **Demo 结构重构**:
    *   统一入口为: [Launcher](demo/src/main/java/com/icuxika/markdown/stream/render/demo/Launcher.java)。
    *   删除了所有过时的 Demo。
    *   Demo 分为 **Stream** (增量验证) 和 **Batch** (全量对比) 两类。

4.  **CSS 加载解耦**:
    *   Demo 代码中不再包含 CSS 路径硬编码。
    *   JavaFX Renderer 会自动加载 `markdown.css` 以及插件的 css（`admonition.css`, `math.css`）。

## 5. 待办事项与未来规划 (Roadmap)

给 AI 或人类接手者的建议任务：

*   **性能优化 (High)**:
    *   当前 JavaFX `VBox` 在节点数量极多（如数千行对话）时性能会下降。
    *   **建议**: 引入 `ListView` 或 `VirtualFlow` 机制来实现虚拟化渲染。
*   **HTML 渲染器增强 (Medium)**:
    *   目前的 [HtmlStreamRenderer](core/src/main/java/com/icuxika/markdown/stream/render/core/renderer/HtmlStreamRenderer.java) 是基于字符串拼接的，比较简陋。
    *   **建议**: 考虑更健壮的转义处理，或者支持 CSS 类的自定义注入。
*   **插件扩展 (Low)**:
    *   目前支持 Math 和 Admonition。
    *   **建议**: 增加 Mermaid 图表支持（较难，需集成 WebView 或 JS 引擎）、脚注支持。
*   **测试覆盖**:
    *   目前主要依赖 Demo 进行视觉回归测试。
    *   **建议**: 增加针对 `StreamMarkdownParser` 的单元测试，断言事件触发的顺序是否符合预期。

## 6. 如何运行

1.  打开项目。
2.  定位到 `demo` 模块。
3.  运行 `com.icuxika.markdown.stream.render.demo.Launcher` 类。
4.  在弹出的菜单中选择：
    *   **JavaFX Streaming Demo**: 观察日志窗口，确认 `PUSH` 和 `RENDER` 日志交替出现，验证增量性。
    *   **HTML Streaming Server**: 浏览器会自动打开，观察网页内容的打字机效果。

***

**致 AI 接手者**:
当你读取此文档时，请优先检查 `core` 模块中的 `StreamMarkdownParser` 逻辑。任何关于渲染样式错乱的问题，通常是因为 Parser 没有正确地成对触发 Open/Close 事件，导致 JavaFX 的容器栈 (`containerStack`) 状态不一致。调试时，请利用 `JavaFxStreamDemo` 中的代理 Logger 进行事件追踪。
