# 模块化导出 API 规范 (Java 9+ Modules)

如果本项目采用 Java 模块系统 (JPMS) 进行模块化改造，建议采用以下导出策略，以明确区分公共 API 和内部实现。

## 1. 模块定义

### 1.1 `com.icuxika.markdown.core` (对应 `core` 模块)

该模块是基础，被其他所有模块依赖。

**exports (导出):**

* `com.icuxika.markdown.stream.render.core`: 包含 `CoreExtension` 等工具类。
* `com.icuxika.markdown.stream.render.core.ast`: **核心 API**。所有 AST 节点类必须导出，供使用者操作文档结构。
* `com.icuxika.markdown.stream.render.core.parser`: **核心 API**。包含 `MarkdownParser`, `StreamMarkdownParser` 等入口类。
* `com.icuxika.markdown.stream.render.core.parser.block`: 供扩展开发者实现自定义块解析器。
* `com.icuxika.markdown.stream.render.core.parser.inline`: 供扩展开发者实现自定义行内解析器。
* `com.icuxika.markdown.stream.render.core.renderer`: **核心 API**。包含渲染器接口。
* `com.icuxika.markdown.stream.render.core.extension.admonition`: 导出扩展的 AST 节点。
* `com.icuxika.markdown.stream.render.core.extension.math`: 导出扩展的 AST 节点。

**opens (开放反射):**

* 通常不需要，除非有依赖反射的序列化库。

### 1.2 `com.icuxika.markdown.html` (对应 `html` 模块)

**requires (依赖):**

* `com.icuxika.markdown.core`

**exports (导出):**

* `com.icuxika.markdown.stream.render.html`: 包含 `HtmlCssProvider`, `HtmlRendererExtension`。
* `com.icuxika.markdown.stream.render.html.renderer`: **核心 API**。包含 `HtmlRenderer`, `HtmlStreamRenderer`。
* `com.icuxika.markdown.stream.render.html.extension.admonition`: (可选) 如果允许用户继承/修改默认渲染逻辑。
* `com.icuxika.markdown.stream.render.html.extension.math`: (可选) 同上。

### 1.3 `com.icuxika.markdown.javafx` (对应 `javafx` 模块)

**requires (依赖):**

* `com.icuxika.markdown.core`
* `javafx.controls`
* `javafx.web` (如果用于 Math 渲染)

**exports (导出):**

* `com.icuxika.markdown.stream.render.javafx`: 包含 `MarkdownTheme`。
* `com.icuxika.markdown.stream.render.javafx.renderer`: **核心 API**。包含 `JavaFxRenderer`。

**opens (开放反射):**

* 如果使用了 FXML 或者 CSS 样式加载需要资源访问权限，可能需要 `opens` 资源目录。

## 2. `module-info.java` 示例草稿

### Core

```java
module com.icuxika.markdown.core {
    exports com.icuxika.markdown.stream.render.core;
    exports com.icuxika.markdown.stream.render.core.ast;
    exports com.icuxika.markdown.stream.render.core.parser;
    exports com.icuxika.markdown.stream.render.core.parser.block;
    exports com.icuxika.markdown.stream.render.core.parser.inline;
    exports com.icuxika.markdown.stream.render.core.renderer;
    exports com.icuxika.markdown.stream.render.core.extension.admonition;
    exports com.icuxika.markdown.stream.render.core.extension.math;
}
```

### HTML

```java
module com.icuxika.markdown.html {
    requires com.icuxika.markdown.core;

    exports com.icuxika.markdown.stream.render.html;
    exports com.icuxika.markdown.stream.render.html.renderer;
    // 资源文件通常自动对模块内可见，若需对其他模块开放资源流读取，可能需要 opens
}
```

### JavaFX

```java
module com.icuxika.markdown.javafx {
    requires com.icuxika.markdown.core;
    requires javafx.controls;
    requires javafx.web;

    exports com.icuxika.markdown.stream.render.javafx;
    exports com.icuxika.markdown.stream.render.javafx.renderer;

    // 允许 JavaFX CSS 引擎访问样式文件
    opens com.icuxika.markdown.stream.render.javafx.css to javafx.graphics;
    opens com.icuxika.markdown.stream.render.javafx.css.extensions to javafx.graphics;
}
```
