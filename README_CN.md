# Markdown Stream Renderer (流式 Markdown 渲染器)

[English](README.md) | [中文](README_CN.md)

这是一个高性能、**完全兼容 CommonMark 规范**的 Java 流式 Markdown 解析器和渲染器。它支持增量解析，并提供多种输出目标，包括 HTML 和 JavaFX。

![Status](https://img.shields.io/badge/CommonMark%20Spec-100%25%20Passing-brightgreen)
![Java](https://img.shields.io/badge/Java-25%2B-blue)

## 🌟 核心特性

*   **100% 兼容 CommonMark**: 通过了 CommonMark 规范 (v0.31.2) 中的所有 **652** 个测试用例。
*   **流式架构**: 专为增量处理设计。能够在打字或网络传输过程中实时渲染内容，无需等待文档完全加载。
*   **多端渲染**:
    *   **HTML**: 生成符合标准、规范的 HTML。
    *   **JavaFX**: 直接渲染为 JavaFX 场景图 (`VBox`, `TextFlow`, `GridPane`)，适用于富桌面应用。
*   **零依赖 (Core)**: 核心模块没有任何外部依赖，轻量且易于嵌入。
*   **高级功能**:
    *   **表格**: 支持 GFM 风格的表格及对齐方式。
    *   **健壮的解析**: 正确处理边缘情况，如制表符展开、列表嵌套和复杂的块级交互。
    *   **图片缓存**: JavaFX 渲染器内置智能缓存，防止流式更新时的图片闪烁。

## 📂 项目结构

*   **`core`**: 项目核心。包含 `MarkdownParser`（解析器）、AST 节点和 `HtmlRenderer`（HTML 渲染器）。
*   **`javafx`**: 包含 `JavaFxRenderer`，用于将 Markdown 渲染为 JavaFX 节点。
*   **`demo`**: 演示应用模块。
    *   `GuiApp`: 一个简单的 JavaFX Markdown 编辑器。
    *   `StreamingGuiApp`: 演示 JavaFX 流式渲染（模拟打字机效果）。
    *   `StreamingHtmlDemo`: 本地 HTTP 服务器，通过 Server-Sent Events (SSE) 演示流式 HTML 渲染。

## 🚀 快速开始

### 前置要求
*   Java 25 (或支持 Preview 特性的最新 JDK)
*   Maven

### 构建项目
```bash
mvn clean install
```

### 运行演示 (Demos)

**1. JavaFX 流式渲染演示**
在一个桌面窗口中模拟打字机效果，直观展示流式渲染能力。
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.StreamingGuiApp"
```

**2. HTML 流式渲染演示**
启动本地 Web 服务器。打开浏览器即可看到 Markdown 被实时渲染并推送到页面上。
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.StreamingHtmlDemo"
```

**3. JavaFX 编辑器**
一个基础的编辑器，你可以输入 Markdown 并实时查看结果。
```bash
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.GuiApp"
```

**4. DeepSeek 聊天演示 (AI 流式对话)**
与 DeepSeek API 交互的聊天界面。需要设置 API Key。
```bash
# 请先设置环境变量
# Windows (PowerShell): $env:DEEPSEEK_API_KEY="your-key"
# Linux/Mac: export DEEPSEEK_API_KEY="your-key"
mvn -pl demo exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.DeepSeekChatDemo"
```

## 📐 架构与设计

本项目采用模块化、事件驱动的架构以支持流式处理：

1.  **分词器 (Tokenizer/Lexer)**: 逐字符（或逐行）处理输入。
2.  **解析器 (Parser)**:
    *   **块级解析**: 使用状态机 (`BlockParserState`) 确定结构（段落、列表、引用块）。处理“懒惰延续 (Lazy Continuation)”，即块结构跨行的情况。
    *   **内联解析**: 解析块内的文本样式（加粗、斜体、链接）。
3.  **AST (抽象语法树)**: 构建轻量级的 `Node` 对象树 (`Block` 和 `Inline`)。
4.  **渲染器 (Renderer)**: 遍历 AST (访问者模式) 以生成输出。

### 技术亮点
*   **制表符 (Tabs) 处理**: 实现了精确的虚拟列计算，严格按照规范处理混合使用的制表符和空格。
*   **歧义消除**: 解决了复杂的语法冲突，例如区分 Setext 标题 (`---`) 和表格分隔行 (`---|---`)。
*   **Unicode 支持**: 实现了链接引用定义 (Link Reference Definitions) 的正确大小写折叠 (Case Folding)，例如处理 `ẞ` 等特殊字符。

## 🔮 未来路线图

*   **性能优化**: 引入记忆化 (Memoization) 并优化正则表达式以处理深层嵌套。
*   **增强 JavaFX 样式**: 支持复杂的表格边框、背景色以及代码块的语法高亮。
*   **GFM 扩展**: 支持任务列表 (`[ ]`)、删除线 (`~~`) 以及其他 GitHub Flavored Markdown 特性。

## 📊 规范兼容性报告

核心解析器已通过官方 CommonMark 规范验证。
详细的测试执行报告请参阅 [SPEC_REPORT.md](SPEC_REPORT.md)。

| 类别 | 状态 |
| :--- | :--- |
| 块级结构 (Block Structure) | ✅ 100% |
| 内联结构 (Inline Structure) | ✅ 100% |
| HTML 渲染 (HTML Rendering) | ✅ 100% |
| **总计 (652/652)** | **✅ 通过** |
