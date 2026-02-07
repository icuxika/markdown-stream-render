# Markdown Stream Render - 核心逻辑学习指南

这份文档专为初学者设计，旨在帮助你理解 `markdown-stream-render` 项目是如何运作的。我们将避开复杂的术语，用通俗易懂的方式拆解这个项目的核心逻辑。

---

## 1. 我们在做什么？(What)

想象一下，你正在使用 ChatGPT。当你问它一个问题时，它的回答不是一下子全部显示出来的，而是一个字一个字地“蹦”出来的。同时，这些字还能自动排版：标题变大、代码高亮、列表对齐。

这个项目的目标就是实现这个功能：**流式（Stream）渲染 Markdown**。

*   **Markdown**: 一种简单的标记语言（比如用 `#` 表示标题）。
*   **流式渲染**: 像打字机一样，一边接收文字，一边实时把它们变成漂亮的排版，不需要等整篇文章写完。

---

## 2. 核心架构：大脑与手脚 (Architecture)

为了让这个项目既能在电脑软件（JavaFX）上跑，以后也能在网页或其他地方跑，我们把项目分成了两部分：

### 2.1 Core 模块（大脑）
*   **职责**: 负责“读懂” Markdown 文本。
*   **特点**: 它完全不知道什么是“窗口”、“按钮”或“颜色”。它只处理纯文本数据。
*   **比喻**: 就像一个**编剧**，他只负责写剧本（分析剧情、角色、对话），不负责拍摄。

### 2.2 JavaFX / HTML 模块（手脚）
*   **职责**: 负责把 Core 分析出来的结果“画”在屏幕上。
*   **特点**: 它依赖 Core。
*   **比喻**: 就像**导演**和**摄影师**，他们拿到剧本后，负责找演员、搭布景，把剧本变成观众能看到的画面。

---

## 3. 核心流程：从文本到屏幕 (The Journey)

当一段 Markdown 文本（比如 `# Hello`）进入系统后，会经历两个主要阶段：**解析 (Parsing)** 和 **渲染 (Rendering)**。

### 阶段一：解析 (Parsing) —— 搭建骨架

解析就是把一串字符变成计算机能理解的**结构化数据**。这个结构我们称之为 **AST（抽象语法树）**。

#### 什么是 AST？
想象一篇文章的结构：
*   文章 (Document)
    *   标题 (Heading) -> "欢迎"
    *   段落 (Paragraph)
        *   加粗文字 (Strong) -> "注意"
        *   普通文字 (Text) -> "：这里有坑。"

这就构成了一棵树。解析的过程就是把平铺直叙的文字还原成这棵树。

#### 解析的两步走策略
为了高效处理，我们分两步来解析：

1.  **第一步：找大块头 (Block Parsing)**
    *   **任务**: 识别段落、标题、列表、代码块这些大的结构。
    *   **原理**: 逐行扫描。
        *   看到 `#` 开头？ -> 哦，这是标题。
        *   看到 `>` 开头？ -> 哦，这是引用块。
        *   看到 `-` 开头？ -> 哦，这是列表项。
    *   **难点**: 嵌套（俄罗斯套娃）。比如列表中套引用，引用中套代码。我们用一个**栈 (Stack)** 来记录当前在哪一层容器里。

2.  **第二步：找小细节 (Inline Parsing)**
    *   **任务**: 在大块头内部，识别粗体、斜体、链接、行内代码。
    *   **原理**: 逐字扫描。
        *   看到 `**`？ -> 可能是粗体开始。
        *   看到 `]`？ -> 可能是链接结束。

### 阶段二：渲染 (Rendering) —— 绘制画面

有了 AST 这棵树，渲染就变得很简单了。我们使用一种叫**访问者模式 (Visitor Pattern)** 的方法。

*   **逻辑**: 渲染器就像一个游客，从树根开始游览。
    *   遇到 `Heading` 节点 -> 在屏幕上画一个大号字体的标签 (Label)。
    *   遇到 `CodeBlock` 节点 -> 在屏幕上画一个灰底的方框。
    *   遇到 `Text` 节点 -> 直接把文字填进去。

---

## 4. 关键代码导读 (Code Map)

如果你想看代码，可以从这里入手：

| 角色 | 类名 (点击可跳转) | 说明 |
| :--- | :--- | :--- |
| **总指挥** | [MarkdownParser](file:///core/src/main/java/com/icuxika/markdown/stream/render/core/parser/MarkdownParser.java) | 核心入口，协调整个解析过程。 |
| **数据结构** | [Node](file:///core/src/main/java/com/icuxika/markdown/stream/render/core/ast/Node.java) | AST 节点的基类。所有标题、段落都是它的子类。 |
| **块级解析** | [BlockParserState](file:///core/src/main/java/com/icuxika/markdown/stream/render/core/parser/MarkdownParser.java) | (内部类) 负责维护“栈”，处理行级逻辑。 |
| **行内解析** | [InlineParser](file:///core/src/main/java/com/icuxika/markdown/stream/render/core/parser/InlineParser.java) | 负责扫描文本中的 `*`, `[`, `` ` `` 等符号。 |
| **JavaFX渲染** | [JavaFxStreamRenderer](file:///javafx/src/main/java/com/icuxika/markdown/stream/render/javafx/renderer/JavaFxStreamRenderer.java) | 实现流式渲染接口，把节点变成 JavaFX 控件。 |

---

## 5. 常见问题解答 (Q&A)

**Q: 为什么代码里要分 `Block` 和 `Inline`？**
A: 为了降低复杂度。先确定“这是一段话”，再确定“这段话里哪几个字是粗体”，比混在一起处理要简单得多。

**Q: 流式渲染是怎么做到的？**
A: 普通解析是“读完所有文本 -> 生成完整树 -> 渲染”。流式解析是“读一行 -> 更新树 -> 发现一个新块 -> **立即通知渲染器** -> 渲染器马上画出来”。这就实现了打字机效果。

**Q: 我想加一个新的语法（比如 `::: warning` 警告块），怎么做？**
1.  **定义节点**: 在 Core 里写一个 `WarningBlock` 类。
2.  **写解析器**: 写一个 `WarningBlockParser`，告诉系统看到 `:::` 就创建这个块。
3.  **写渲染器**: 在 JavaFX 模块里写代码，告诉系统遇到 `WarningBlock` 就画一个黄色背景的框。

---

希望这份文档能帮你建立起对项目的全局认识！动手改改代码是最好的学习方式。加油！
