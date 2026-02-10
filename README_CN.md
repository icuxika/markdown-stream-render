# Markdown Stream Renderer (流式 Markdown 渲染器)

[English](README.md) | [中文](README_CN.md)

这是一个高性能、**完全兼容 CommonMark 规范**的 Java 流式 Markdown 解析器和渲染器。它支持增量解析，并提供多种输出目标，包括 HTML 和 JavaFX。

![Status](https://img.shields.io/badge/CommonMark%20Spec-100%25%20Passing-brightgreen)
![Java](https://img.shields.io/badge/Java-25%2B-blue)

## 核心特性

*   **100% 兼容 CommonMark**: 通过了 CommonMark 规范 (v0.31.2) 中的所有 **652** 个测试用例。
*   **流式架构**: 专为增量处理设计。能够在打字或网络传输过程中实时渲染内容，无需等待文档完全加载。
*   **多端渲染**:
    *   **HTML**: 生成符合标准、规范的 HTML。
    *   **JavaFX**: 直接渲染为 JavaFX 场景图。内置 **高性能混合渲染器** (`VirtualJavaFxStreamRenderer`)，结合了虚拟化列表与实时流式渲染，完美支持无限长文档。
*   **零依赖 (Core)**: 核心模块没有任何外部依赖，轻量且易于嵌入。
*   **高级功能**:
    *   **内置 GFM 支持**: GitHub Flavored Markdown 特性（表格、任务列表、删除线、自动链接）直接集成在核心解析器中，以获得最佳性能（可通过选项开关）。
    *   **丰富的 JavaFX 样式**: 为表格（斑马纹、边框）、引用块和代码块提供了增强的样式支持。
    *   **扩展模块**: 提供 **提示块 (Admonitions)** 和 **数学公式 (Math)** 的独立扩展支持。
    *   **健壮的解析**: 正确处理边缘情况，如制表符展开、列表嵌套和复杂的块级交互。
    *   **图片缓存**: JavaFX 渲染器内置智能缓存，防止流式更新时的图片闪烁。
    *   **安全性**: 提供“安全模式” (Safe Mode)，通过过滤禁止的原始 HTML 来防止 XSS 攻击。

## JavaFX 样式与主题

JavaFX 渲染器采用了现代化的 CSS 样式系统。
推荐实践与自定义主题方式见 [javafx_theming.md](docs/javafx_theming.md)。

### 内置主题
本库自带 `Light` (明亮) 和 `Dark` (暗黑) 两种主题。你可以使用 `MarkdownTheme` 辅助类轻松切换：

```java
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;

MarkdownTheme theme = new MarkdownTheme();
theme.apply(scene); // 加载 markdown.css + 扩展样式 + 默认明亮主题变量
theme.setTheme(MarkdownTheme.Theme.DARK); // 切换到暗黑主题
```

### 自定义样式
你可以通过提供自己的 CSS 文件来覆盖默认样式。渲染器使用 **查找颜色 (Looked-up Colors)** 机制，无需重写所有规则即可轻松定制。

**示例：创建自定义的 "复古 (Sepia)" 主题**

1. 创建 `sepia.css` 文件：
   ```css
   .root {
       -md-fg-color: #5f4b32;
       -md-bg-color: #f4ecd8;
       -md-link-color: #d2691e;
       -md-code-bg-color: #eae0c9;
       /* ... 根据需要覆盖其他变量 */
   }
   ```
2. 在你的应用中加载它：
   ```java
   import com.icuxika.markdown.stream.render.javafx.MarkdownStyles;

   MarkdownStyles.applyBase(scene, true);
   scene.getStylesheets().add(getClass().getResource("/sepia.css").toExternalForm());
   ```

### CSS 变量参考
| 变量名 | 描述 | 默认值 (Light) |
| :--- | :--- | :--- |
| `-md-fg-color` | 主要文本颜色 | `#24292f` |
| `-md-bg-color` | 背景颜色 | `#ffffff` |
| `-md-link-color` | 链接颜色 | `#0969da` |
| `-md-code-bg-color` | 行内代码/代码块背景 | `#f6f8fa` |
| `-md-border-color` | 表格/区块边框颜色 | `#d0d7de` |

## 项目结构

*   **`core`**: 解析器 + AST + 渲染接口（与具体平台无关）。
*   **`html`**: HTML 渲染器 + 规范驱动的兼容性测试。
*   **`javafx`**: JavaFX 渲染器 + 主题/CSS 资源。
*   **`benchmark`**: JMH 性能基准测试模块。
*   **`demo`**: 演示应用模块。
    *   `Launcher`: Demo 启动器（聚合 JavaFX 与服务端 demo）。
    *   `TypewriterPreviewDemo`: 逐字符流式预览演示。

## 🚀 快速开始

### 前置要求
*   Java 25 (或支持 Preview 特性的最新 JDK)
*   Maven

### 构建项目
```bash
mvn clean install
```

### 运行演示 (Demos)

**1. 虚拟化流式演示 (推荐)**
核心演示，展示了用于 AI 对话场景的高性能混合渲染器（模拟 LLM 流式输出）。
```bash
mvn -pl demo -am exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.javafx.VirtualStreamRenderDemo"
```

**2. 虚拟化列表压力测试**
针对超大文档（4000+ 行）的压力测试，验证内存效率和滚动流畅度。
```bash
mvn -pl demo -am exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.javafx.VirtualListDemo"
```

**3. 打字机预览演示 (Typewriter Preview Demo)**
简单的逐字符流式预览。
```bash
mvn -pl demo -am exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.javafx.TypewriterPreviewDemo"
```

**4. AI 流式对话演示 (Streaming AI Chat Demo)**
与 DeepSeek API 交互的聊天界面。需要设置 API Key。
```bash
# 请先设置环境变量
# Windows (PowerShell): $env:DEEPSEEK_API_KEY="your-key"
# Linux/Mac: export DEEPSEEK_API_KEY="your-key"
mvn -pl demo -am exec:java "-Dexec.mainClass=com.icuxika.markdown.stream.render.demo.javafx.AiChatDemo"
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

## 📊 规范兼容性报告

核心解析器已通过官方 CommonMark 规范验证。
CommonMark 用例在 `html` 模块中通过 JSON 规范用例执行。
GFM 当前仅覆盖扩展章节（基线资源固定为 v0.29.0），范围说明见 [spec_coverage.md](docs/spec_coverage.md)。

| 类别 | 状态 |
| :--- | :--- |
| 块级结构 (Block Structure) | ✅ 100% |
| 内联结构 (Inline Structure) | ✅ 100% |
| HTML 渲染 (HTML Rendering) | ✅ 100% |
| **总计 (652/652)** | **✅ 通过** |

可选：生成本地 CommonMark 报告文件（不作为默认测试的一部分）：
```bash
mvn -pl html test -DgenerateSpecReport=true
```

## 📅 未来路线图 (Roadmap)

`markdown-stream-render` 的核心功能现已稳定，能够满足大多数流式场景的需求。未来的开发重点将集中在：

*   **插件系统**: 正式化自定义块和内联解析器的 API，允许用户无需 Fork 项目即可添加如 Mermaid 图表、脚注等新语法支持。
*   **性能优化**: 针对移动设备或资源受限环境，进一步微调 `VirtualJavaFxStreamRenderer` 的性能表现。
*   **更多输出目标**: 调研对 Swing 或 终端 (ANSI) 输出的支持。
*   **GFM 完整性**: 逐步补充缺失的 GFM 特性（如核心层的原始 HTML 标签过滤）。

**注意**: 1.x 系列目前没有重大的 API 破坏性变更计划。

