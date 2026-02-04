# Markdown Stream Render 开发指南

本文档旨在帮助开发者了解如何使用 `markdown-stream-render` 项目进行开发，包括引入依赖、使用核心 API 以及扩展自定义功能。

## 1. 项目简介

`markdown-stream-render` 是一个基于流式处理的 Markdown 解析和渲染库。它设计为模块化，核心解析逻辑与渲染逻辑分离，目前支持 HTML 和 JavaFX 渲染。

主要模块：
*   **core**: 核心解析器、AST 定义和基础渲染接口。
*   **javafx**: 基于 JavaFX 的渲染实现，支持 CSS 样式和自定义节点渲染。

## 2. 引入依赖

在您的 Maven 项目中，可以通过以下方式引入 `core` 和 `javafx` 模块。

### 引入 Core 模块 (仅解析或自定义渲染)

```xml
<dependency>
    <groupId>com.icuxika</groupId>
    <artifactId>markdown-stream-render-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 引入 JavaFX 模块 (用于 JavaFX 应用)

```xml
<dependency>
    <groupId>com.icuxika</groupId>
    <artifactId>markdown-stream-render-javafx</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

> 注意：由于项目尚未发布到中央仓库，您需要先在本地运行 `mvn install` 将项目安装到本地 Maven 仓库。

## 3. 核心 API 使用

### 3.1 MarkdownParser (解析器)

`MarkdownParser` 是解析 Markdown 文本的入口。

```java
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.ast.Document;

// 1. 创建解析器实例
MarkdownParser parser = MarkdownParser.builder().build();

// 2. 解析 Markdown 文本
String markdown = "# Hello World";
Document document = parser.parse(markdown);

// 3. (可选) 遍历 AST
// document.accept(new MyVisitor());
```

### 3.2 自定义插件扩展

您可以通过实现工厂接口来扩展块级元素或行内元素。

*   **BlockParserFactory**: 用于注册自定义块解析器（如 Admonition 警告块）。
*   **InlineContentParserFactory**: 用于注册自定义行内解析器（如 LaTeX 数学公式）。

```java
MarkdownParser parser = MarkdownParser.builder()
        .blockParserFactory(new MyBlockParserFactory())
        .inlineParserFactory(new MyInlineParserFactory())
        .build();
```

## 4. JavaFX 渲染 API

### 4.1 JavaFxRenderer (渲染器)

`JavaFxRenderer` 负责将 Markdown AST 转换为 JavaFX 节点树。

```java
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import javafx.scene.layout.VBox;

// 1. 创建渲染器
JavaFxRenderer renderer = new JavaFxRenderer();

// 2. 解析并渲染
parser.parse(markdown, renderer);

// 3. 获取结果 (通常是 VBox)
VBox result = (VBox) renderer.getResult();

// 4. 添加到场景中
root.getChildren().add(result);
```

### 4.2 自定义节点渲染

如果您扩展了 AST 节点，可以通过 `JavaFxNodeRenderer` 定义其在 JavaFX 中的显示方式。

```java
JavaFxRenderer renderer = JavaFxRenderer.builder()
        .nodeRendererFactory(context -> new MyCustomNodeRenderer(context))
        .build();
```

### 4.3 样式定制 (CSS)

默认样式定义在 `markdown.css` 中。您可以通过 CSS 类名来定制外观：

*   `.markdown-root`: 根容器
*   `.markdown-h1-text` 到 `.markdown-h6-text`: 标题
*   `.markdown-paragraph`: 段落
*   `.markdown-code-block`: 代码块
*   `.markdown-link`: 链接

## 5. 功能特性

*   **流式解析**: 虽然目前主要接口是全量解析，但架构设计支持流式输入。
*   **AST (抽象语法树)**: 完整的节点层级结构，易于遍历和修改。
*   **可扩展性**: 支持自定义 Block 和 Inline 解析器。
*   **平台无关性**: Core 模块不依赖任何 UI 框架，可轻松移植到 Swing, Android 或 Web (生成 HTML)。

## 6. 示例代码

请参考 `demo` 模块下的示例：

*   `SimpleDemo.java`: 基础控制台输出示例。
*   `JavaFxDemo.java`: 基础 JavaFX 渲染示例。
*   `VisualPluginDemo.java`: 展示自定义插件（警告块和数学公式）的 JavaFX 示例。
