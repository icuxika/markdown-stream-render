# Markdown Stream Render 开发指南

本文档旨在帮助开发者了解如何使用 `markdown-stream-render` 项目进行开发，包括引入依赖、使用核心 API 以及扩展自定义功能。

## 1. 项目简介

`markdown-stream-render` 是一个基于流式处理的 Markdown 解析和渲染库。它设计为模块化，核心解析逻辑与渲染逻辑分离，目前支持 HTML 和 JavaFX 渲染。

主要模块：

* **core**: 核心解析器、AST 定义和基础渲染接口。不依赖任何 UI 库。
* **html**: 基于 HTML 字符串构建的渲染实现，支持 CSS 样式，适用于 Web 服务端渲染或生成静态页面。
* **javafx**: 基于 JavaFX 的渲染实现，支持 CSS 样式和自定义节点渲染，适用于桌面应用。

## 2. 引入依赖

在您的 Maven 项目中，可以通过以下方式引入所需模块。

### 2.1 引入 Core 模块 (仅解析或自定义渲染)

```xml
<dependency>
    <groupId>com.icuxika</groupId>
    <artifactId>markdown-stream-render-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2.2 引入 HTML 模块 (用于 Web/服务端渲染)

```xml
<dependency>
    <groupId>com.icuxika</groupId>
    <artifactId>markdown-stream-render-html</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2.3 引入 JavaFX 模块 (用于 JavaFX 桌面应用)

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
import com.icuxika.markdown.stream.render.core.CoreExtension;

// 1. 创建解析器构建器
MarkdownParser.Builder builder = MarkdownParser.builder();

// 2. (可选) 注册核心扩展（如 Admonition, Math）
CoreExtension.addDefaults(builder);

// 3. 构建解析器
MarkdownParser parser = builder.build();

// 4. 解析 Markdown 文本
String markdown = "# Hello World\n!!! warning\nAlert\n!!!";
Document document = parser.parse(markdown);
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

## 4. HTML 渲染 API

### 4.1 HtmlRenderer (全量渲染)

```java
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import com.icuxika.markdown.stream.render.html.HtmlRendererExtension;
import com.icuxika.markdown.stream.render.html.HtmlCssProvider;

// 1. 创建渲染器构建器并注册默认扩展
HtmlRenderer.Builder htmlBuilder = HtmlRenderer.builder();
HtmlRendererExtension.addDefaults(htmlBuilder);
HtmlRenderer renderer = htmlBuilder.build();

// 2. 渲染文档
renderer.render(document);
String htmlBody = (String) renderer.getResult();

// 3. 获取完整 HTML (包含 CSS)
String fullHtml = "<html><head><style>" + 
                  HtmlCssProvider.getAllCss() + 
                  "</style></head><body>" + 
                  htmlBody + 
                  "</body></html>";
```

### 4.2 HtmlStreamRenderer (流式渲染)

```java
import com.icuxika.markdown.stream.render.html.renderer.HtmlStreamRenderer;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;

StringBuilder buffer = new StringBuilder();
HtmlStreamRenderer renderer = new HtmlStreamRenderer(buffer);

StreamMarkdownParser parser = StreamMarkdownParser.builder()
        .renderer(renderer)
        .build();

parser.push("Chunk 1...");
parser.push("Chunk 2...");
parser.close();
```

## 5. JavaFX 渲染 API

### 5.1 JavaFxRenderer (渲染器)

`JavaFxRenderer` 负责将 Markdown AST 转换为 JavaFX 节点树。

```java
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;
import com.icuxika.markdown.stream.render.core.CoreExtension;
import javafx.scene.layout.VBox;

// 1. 创建渲染器
JavaFxRenderer renderer = new JavaFxRenderer();
// (JavaFxRenderer 默认已内置了 Admonition 和 Math 的渲染支持)

// 2. 解析并渲染
// 注意：parser 需要注册 CoreExtension 以识别扩展语法
MarkdownParser.Builder builder = MarkdownParser.builder();
CoreExtension.addDefaults(builder);
MarkdownParser parser = builder.build();

parser.parse(markdown, renderer);

// 3. 获取结果 (通常是 VBox)
VBox result = (VBox) renderer.getResult();

// 4. 添加到场景中
root.getChildren().add(result);
```

### 5.2 样式定制 (CSS)

JavaFX 模块提供了默认主题管理器 `MarkdownTheme`（内置 `LIGHT/DARK` 两种主题变量）。
推荐将样式加载在 `Scene` 级别，以便主题切换与应用级覆盖更可控。

```java
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;

MarkdownTheme theme = new MarkdownTheme();
theme.apply(scene); // 加载 markdown.css + 扩展样式 + 当前主题变量
theme.setTheme(MarkdownTheme.Theme.DARK); // 切换暗色主题
```

如果你希望完全自定义主题（仅复用结构样式），可以只加载基础样式并自行提供变量/覆盖：

```java
import com.icuxika.markdown.stream.render.javafx.MarkdownStyles;

MarkdownStyles.applyBase(scene, true); // true 表示同时加载扩展样式
scene.getStylesheets().add(getClass().getResource("/your-theme.css").toExternalForm());
```

## 6. 功能特性

*   **流式解析**: 虽然目前主要接口是全量解析，但架构设计支持流式输入。
*   **AST (抽象语法树)**: 完整的节点层级结构，易于遍历和修改。
*   **可扩展性**: 支持自定义 Block 和 Inline 解析器。
* **平台无关性**: Core 模块不依赖任何 UI 框架，可轻松移植到 Swing, Android 或 Web。

## 7. 示例代码

请参考 `demo` 模块下的示例：

* `HtmlBatchServerDemo.java`: HTML 全量渲染 Web 服务示例。
* `HtmlStreamServerDemo.java`: HTML 流式渲染 Web 服务示例。
*   `JavaFxDemo.java`: 基础 JavaFX 渲染示例。
*   `VisualPluginDemo.java`: 展示自定义插件（警告块和数学公式）的 JavaFX 示例。
