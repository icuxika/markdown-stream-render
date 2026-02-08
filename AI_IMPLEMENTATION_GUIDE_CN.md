# Markdown Stream Render - AI 实现指南

## 1. 项目目标
创建一个高性能、可扩展的 Java Markdown 渲染引擎，专为 **流式上下文**（如大型语言模型输出）设计。

**核心需求**：引擎必须支持增量解析和渲染，允许内容在接收到文本块时立即出现在屏幕上，而无需等待整个文档完成。

### **关键要求：规范驱动开发 (Spec-Driven Development)**
本项目严格遵循 **规范驱动开发** 方法。解析器的正确性完全取决于其通过官方规范测试的能力。

*   **测试套件位置**：`html/src/test/resources/`
    *   `commonmark-spec-0.31.2.json`: CommonMark 规范测试（固定版本）。
    *   `gfm-spec-0.29.0.json`: GFM 规范 JSON（固定版本，仅用于扩展覆盖，不以完整 GFM 兼容为目标）。
*   **验收标准**：只有当解析器实现通过了这些规格文件中定义的所有测试用例时，才被视为完成。
*   **工作流**：
    1.  在 `html` 模块中创建一个测试工具，用于读取 JSON 规格文件。
    2.  对于每个测试用例（Markdown 输入 -> 预期的 HTML 输出），运行解析器并将生成的 HTML 与预期结果进行比对。
    3.  迭代开发 `core` 解析器逻辑，直到通过所有测试。
*   **可选报告**：规范报告按需生成（默认关闭），避免提交过期快照文件。

## 2. 技术架构
项目采用多模块 Maven 项目结构：

1.  **`core`**：AST + 解析逻辑 + 渲染接口。**必须保持框架无关**。
2.  **`html`**：HTML 渲染器与规范驱动的兼容性测试。
3.  **`javafx`**：JavaFX 渲染器与主题/CSS 资源。
4.  **`demo`**：可运行的应用程序，用于展示功能。

---

## 3. 实施路线图

### 第一阶段：基石 (AST)
**目标**：定义表示 Markdown 文档的数据结构。

1.  **基础节点系统**：
    *   创建一个抽象的 `Node` 类。
    *   实现双向链表结构（`parent`, `previous`, `next`, `firstChild`, `lastChild`）以便于树的遍历。
2.  **节点类型**：
    *   **块级节点 (Block Nodes)**（结构）：`Document`（根节点）, `Paragraph`, `Heading` (1-6), `BlockQuote`, `CodeBlock`, `List` (Ordered/Bullet), `ListItem`, `ThematicBreak`, `Table`。
    *   **行内节点 (Inline Nodes)**（内容）：`Text`, `Emphasis` (斜体), `StrongEmphasis` (粗体), `Code` (行内代码), `Link`, `Image`, `HardBreak`, `SoftBreak`。
3.  **访问者模式 (Visitor Pattern)**：
    *   定义一个 `Visitor` 接口，包含所有节点类型的 `visit(NodeType node)` 方法。
    *   在所有节点中实现 `accept(Visitor visitor)`。

### 第二阶段：核心解析器 (Block & Inline)
**目标**：将原始 Markdown 文本转换为第一阶段定义的 AST。遵循 **CommonMark** 规范策略。

1.  **块级解析 (Block Parsing)**：
    *   实现逐行处理器。
    *   **`BlockParser` 接口**：定义 `tryContinue(state)` 以检查打开的块是否与当前行匹配。
    *   **`BlockParserFactory`**：用于识别块起始的注册表（例如，`>` 表示引用，`#` 表示标题）。
    *   **状态管理**：维护一个“打开的块”列表。对于每一行新内容，检查哪些打开的块可以继续，关闭那些不能继续的，并在匹配时打开新的块。
2.  **行内解析 (Inline Parsing)**：
    *   在一个块（如 Paragraph）关闭后，解析其文本内容。
    *   实现分隔符扫描器（`*`, `_`, `` ` ``, `[`, `!`).
    *   构建行内节点树并将其附加到父块。

### 第三阶段：流式引擎
**目标**：项目的“秘制酱料”。启用增量处理。

1.  **`StreamMarkdownParser`**：
    *   **输入**：一个接受部分字符串的 `push(String text)` 方法。
    *   **缓冲**：内部缓冲字符，直到检测到换行符 (`\n`)，因为 Markdown 块本质上是基于行的。
    *   **事件分发**：
        *   `onBlockStarted(Node)`：当检测到新块（例如 BlockQuote）时。
        *   `onBlockClosed(Node)`：当块结束时。
        *   `onBlockFinalized(Node)`：当块被完全解析（包括行内元素）并准备好最终渲染时。
    *   **设计说明**：此解析器包装核心解析器，但管理输入缓冲区和生命周期事件。

### 第四阶段：渲染器接口
**目标**：定义渲染的“契约”，将解析与 UI 解耦。

1.  **`MarkdownRenderer`** (静态)：
    *   方法：`render(Document doc)`。
    *   用于渲染完整的静态文档。
2.  **`StreamMarkdownRenderer`** (动态)：
    *   方法：`openBlock(Node)`, `closeBlock(Node)`, `renderNode(Node)`。
    *   设计用于响应 `StreamMarkdownParser` 事件。

### 第五阶段：JavaFX 实现
**目标**：将 AST 在屏幕上生动呈现。

1.  **组件映射**：
    *   `Paragraph` -> `TextFlow`
    *   `CodeBlock` -> `VBox` + `Label` (带样式)
    *   `List` -> `VBox`
    *   `Text` -> `Text` (JavaFX Node)
2.  **`JavaFxRenderer` (静态)**：
    *   遍历完整的 AST 并构建 JavaFX 节点树。
3.  **`JavaFxStreamRenderer` (动态)**：
    *   **容器栈**：维护一个 `Stack<Pane>` 来跟踪嵌套（例如，在 BlockQuote 内部 -> 在 List 内部）。
    *   **增量更新**：
        *   `openBlock`：创建一个容器（例如，BlockQuote 对应的 `VBox`），将其添加到当前父容器，并推入栈。
        *   `renderNode`：创建叶子节点（例如，`TextFlow`）并将其添加到当前容器。
        *   `closeBlock`：弹出栈。
    *   **线程安全**：确保所有 UI 操作都在 `Platform.runLater` 上发生。

## 4. 关键挑战与解决方案

### 1. “打字机”效果
*   **挑战**：标准解析器等待块关闭后才进行渲染。对于长段落，这会感觉有延迟。
*   **解决方案**：
    *   对于 **代码块**：在 `openBlock` 时立即渲染块容器。随着行数据的到达，动态地将它们追加到内容区域，而无需等待结束的 ```。
    *   对于 **文本**：`StreamMarkdownParser` 理想情况下应支持“中间”文本事件，或者至少逐行最终化段落。

### 2. 样式
*   使用 CSS 进行所有样式设置。为 JavaFX 节点分配 CSS 类（例如，`.markdown-h1`, `.markdown-code`）。
*   避免在 Java 代码中硬编码字体或颜色。

### 3. 滚动与布局
*   主聊天界面使用 `ScrollPane` + `VBox`。
*   实现“粘滞底部”逻辑：当添加新内容时，如果用户已经在底部，则自动滚动到底部。

## 5. 开发检查清单
- [ ] **Core**: AST 节点定义。
- [ ] **Core**: 块级解析器 (Paragraph, Heading, List, BlockQuote, CodeBlock)。
- [ ] **Core**: 行内解析器 (Emphasis, Strong, Code, Link)。
- [ ] **Core**: 带事件回调的 StreamMarkdownParser。
- [ ] **JavaFX**: 基础 CSS 样式。
- [ ] **JavaFX**: 节点渲染器 (CoreJavaFxNodeRenderer)。
- [ ] **JavaFX**: JavaFxStreamRenderer 实现。
- [ ] **Demo**: 带有模拟 LLM 流源的聊天 UI。
