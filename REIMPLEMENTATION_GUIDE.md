# 项目重构指南：Markdown Stream Render

## 1. 项目愿景与目标

本项目 `markdown-stream-render` 旨在构建一个高性能、模块化、支持流式处理的 Markdown 渲染引擎。其核心差异化特性在于支持类似 ChatGPT 的“打字机”效果，能够随着网络数据包的到达实时渲染 Markdown 内容，而不是等待完整文档下载完毕。

**关键约束：**
*   **流式优先**：必须支持 `push(String chunk)` 模式的增量输入。
*   **跨平台输出**：核心逻辑与渲染实现分离，目前支持 JavaFX (Desktop) 和 HTML (Web/Server)。
*   **高性能**：特别是在 JavaFX 桌面端，需要处理长文档的渲染性能（虚拟化）。
*   **零依赖核心**：`core` 模块不应依赖任何特定的 UI 库或重型第三方 Markdown 库（如 flexmark/commonmark），以保持轻量和可控。

## 2. 核心架构设计

项目采用标准的多模块 Maven 结构：

```
markdown-stream-render
├── core          # 核心解析器、AST 定义、渲染接口 (无 UI 依赖)
├── html          # HTML 渲染实现
├── javafx        # JavaFX 渲染实现 (UI 组件)
├── benchmark     # JMH 性能测试
└── demo          # 演示程序 (包含 Client/Server/UI Demo)
```

### 2.1 核心解析器 (Core Module)

解析器采用两阶段解析策略，参考 CommonMark 规范设计：

1.  **Block Parsing (阶段一)**：
    *   输入：字符流/行流。
    *   输出：Block AST (Document -> Section -> Paragraph/List/BlockQuote)。
    *   关键类：`MarkdownParser` (非流式入口), `StreamMarkdownParser` (流式入口), `BlockParserState`。
    *   **流式处理难点**：Markdown 的某些块结构（如列表、引用）是上下文相关的。`StreamMarkdownParser` 通过维护一个 `buffer` 处理未完成的行，并在换行符处触发 `processLine`。

2.  **Inline Parsing (阶段二)**：
    *   输入：Block 节点的文本内容。
    *   输出：Inline AST (Text, Strong, Emphasis, Link, Code)。
    *   时机：在 Block 闭合（Finalized）时触发。这意味着流式渲染的最小粒度通常是“行”或“块”，而不是字符。

### 2.2 AST (抽象语法树)

AST 节点设计应遵循 Visitor 模式，以便于不同的渲染器实现。

*   `Node` (基类): `parent`, `firstChild`, `next`, `sourceSpan`.
*   `Visitor` (接口): 定义 `visit(Paragraph)`, `visit(Heading)` 等方法。
*   `MarkdownRenderer` (接口): 继承自 `Visitor`，用于非流式渲染。
*   `StreamMarkdownRenderer` (接口): 定义流式事件回调 (`openBlock`, `renderNode`, `closeBlock`)。

## 3. 关键实现细节

### 3.1 流式解析器 (StreamMarkdownParser)

这是本项目的核心。如果要重写，必须理解其工作流：

1.  **Buffer Management**: 接收 `push(String)`，拼接到内部 `StringBuilder`。
2.  **Line Splitting**: 扫描 `\n`，提取完整行进行处理。剩余未闭合的字符留在 Buffer 中。
3.  **State Machine**: `BlockParserState` 维护当前打开的块栈（Block Stack）。每行输入都会尝试匹配当前打开的块，或者开启新块，或者关闭旧块。
4.  **Event Firing**:
    *   `onBlockStarted`: 当新块创建时触发（如 `<ul>` 开始）。
    *   `onBlockFinalized`: 当块确认结束时触发（如段落结束）。此时触发 Inline 解析，并调用 `renderer.renderNode(node)`。
    *   `onBlockClosed`: 块完全脱离上下文。

**注意**：流式渲染的一个妥协是，Inline 元素（如加粗、链接）通常只有在当前块结束（换行）后才能解析。

### 3.2 JavaFX 渲染器

*   **传统渲染 (`JavaFxStreamRenderer`)**: 直接将 Node 转换为 JavaFX 组件（`TextFlow`, `VBox`）添加到界面。适合短对话。
*   **虚拟化渲染 (`VirtualJavaFxStreamRenderer`)**:
    *   **痛点**: 长对话会导致场景图节点过多，内存爆炸，渲染卡顿。
    *   **方案**: 使用 `ListView<Node>`。Renderer 不直接操作 UI 树，而是维护一个 `ObservableList<Node>`。
    *   **核心**: `MarkdownListCell` 负责渲染单个 AST Block。利用 JavaFX 的 `VirtualFlow` 机制只渲染可见区域。

### 3.3 HTML 渲染器

相对简单，基于 `Appendable`（如 `StringBuilder` 或 `Writer`）。
*   `HtmlStreamRenderer`: 在 `openBlock` 时写入 `<tag>`，在 `renderNode` 时写入内容和闭合标签（或在 `closeBlock` 时闭合，取决于实现策略）。目前实现主要在 `renderNode` 处理内容。

## 4. 扩展机制

如果要让 AI 实现扩展功能（如数学公式、Admonition），需要告诉它：

1.  **Block Extension**: 实现 `BlockParserFactory` 和 `BlockParser`。
2.  **Inline Extension**: 实现 `InlineContentParserFactory` 和 `InlineContentParser`。
3.  **注册**: 在 `MarkdownParser.Builder` 中注册 Factory。
4.  **渲染支持**: 在 `JavaFxRenderer` / `HtmlRenderer` 中注册对应的 `NodeRenderer`。

## 5. 工程规范与陷阱

### 5.1 编码规范
*   **Checkstyle**: 项目配置了严格的 Google Checks。
    *   **常见坑**: Missing Javadoc, WhitespaceAround (if/else 必须有大括号), Import Order (避免 `.*` 导入)。
    *   **解决方法**: 使用 `@SuppressWarnings` 处理遗留代码，新代码必须合规。

### 5.2 常见陷阱
1.  **Unicode 处理**: Markdown 解析涉及很多字符判断，不要简单地用 `char` 遍历，要注意 Surrogate Pairs（虽然本项目目前主要处理 BMP 内字符，但应有此意识）。
2.  **线程安全**: JavaFX UI 更新必须在 JavaFX Application Thread (`Platform.runLater`)。`StreamMarkdownParser` 可以在后台线程运行，但回调 Renderer 时需注意线程切换。
3.  **HTML 安全**: 如果实现 HTML 渲染，必须考虑 XSS 攻击（本项目作为 Demo 可能未严格处理，但生产环境必须处理）。
4.  **Tab 展开**: Markdown 规范要求 Tab 展开为 4 个空格，这影响缩进计算。

## 6. 任务清单 (对于新 AI)

如果你是接手的 AI，请按以下顺序执行：

1.  **阅读 `StreamMarkdownParser.java`**: 理解 buffer 和 line processing 逻辑。
2.  **运行 `StreamingOutputTest`**: 理解事件触发顺序。
3.  **运行 `AiChatDemo`**: 体验最终效果。
4.  **遵循 Checkstyle**: 在编写代码前检查 `config/checkstyle/checkstyle.xml`。
5.  **优先实现 Core**: 确保解析逻辑独立于 UI。
