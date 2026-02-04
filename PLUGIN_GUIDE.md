# Markdown Stream Render 插件系统指南

本文档详细介绍了 Markdown Stream Render 的插件系统架构、工作原理以及如何开发自定义扩展。

## 1. 系统概述

本项目的核心设计目标之一是**高扩展性**。插件系统允许开发者介入 Markdown 处理的两个主要阶段：
1.  **解析阶段 (Parsing)**: 将 Markdown 文本转换为抽象语法树 (AST)。
2.  **渲染阶段 (Rendering)**: 将 AST 转换为目标格式（HTML 字符串或 JavaFX 组件树）。

通过插件系统，你可以：
*   添加新的 Markdown 语法（例如：数学公式、警告块、Mermaid 图表）。
*   修改现有语法的渲染方式（例如：自定义链接的点击行为、图片懒加载）。
*   支持全新的输出格式。

---

## 2. 解析扩展 (Parser Extensibility)

解析器的任务是读取文本流并构建 AST。解析过程分为**块级解析**和**行内解析**。

### 2.1 自定义块级语法 (Block Parser)

块级元素占据整行或多行（如段落、列表、代码块）。要扩展块级语法，需要实现以下接口：

*   **`BlockParserFactory`**: 负责识别块的开始。
*   **`BlockParser`**: 负责处理块的内容（继续、结束、嵌套）。
*   **`Block` (AST Node)**: 自定义的 AST 节点，用于存储解析结果。

#### 开发步骤：
1.  定义一个新的 AST 节点类（继承自 `Block`）。
2.  实现 `BlockParser`，处理行的匹配逻辑。
3.  实现 `BlockParserFactory`，定义触发条件。
4.  在 `MarkdownParser.builder()` 中注册工厂。

#### 示例：Admonition (警告块)
语法：
```markdown
!!! info "Title"
    Content...
```

**代码片段** (完整代码见 `demo/src/main/java/com/icuxika/markdown/stream/render/demo/BlockPluginDemo.java`):
```java
public class AdmonitionParser extends AbstractBlockParser {
    // ... 实现 tryContinue 等逻辑
    
    public static class Factory implements BlockParserFactory {
        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            // 检测 "!!!" 开头
            // 返回 BlockStart.of(new AdmonitionParser(...))
        }
    }
}

// 注册
MarkdownParser parser = MarkdownParser.builder()
        .blockParserFactory(new AdmonitionParser.Factory())
        .build();
```

### 2.2 自定义行内语法 (Inline Parser)

行内元素位于块级元素内部（如粗体、斜体、行内公式）。扩展行内语法需要实现：

*   **`InlineContentParserFactory`**: 定义触发字符（Trigger Character）。
*   **`InlineContentParser`**: 解析特定字符后的内容。

#### 开发步骤：
1.  定义一个新的 AST 节点类（继承自 `Node`）。
2.  实现 `InlineContentParser`，从触发字符开始解析。
3.  实现 `InlineContentParserFactory`，返回触发字符集合。
4.  在 `MarkdownParser.builder()` 中注册工厂。

#### 示例：行内数学公式
语法：`$E=mc^2$`

**代码片段** (完整代码见 `demo/src/main/java/com/icuxika/markdown/stream/render/demo/InlinePluginDemo.java`):
```java
public class MathParser implements InlineContentParser {
    @Override
    public ParsedInline tryParse(String input, int index, InlineParserState state) {
        // 查找闭合的 '$'
        // 返回 ParsedInline.of(new MathNode(content), newIndex)
    }

    public static class Factory implements InlineContentParserFactory {
        @Override
        public Set<Character> getTriggerCharacters() {
            return Collections.singleton('$');
        }
        // ...
    }
}

// 注册
MarkdownParser parser = MarkdownParser.builder()
        .inlineParserFactory(new MathParser.Factory())
        .build();
```

---

## 3. 渲染扩展 (Renderer Extensibility)

渲染器遍历 AST 并生成输出。你可以为自定义节点提供渲染逻辑，或覆盖现有节点的渲染逻辑。

### 3.1 HTML 渲染扩展

使用 `HtmlRenderer` 生成 HTML 字符串。

*   **`NodeRenderer`**: 处理特定类型 AST 节点的渲染。
*   **`NodeRendererFactory`**: 创建渲染器实例。

#### 开发步骤：
1.  实现 `NodeRenderer` 接口。
2.  在 `getNodeTypes()` 中返回你要处理的节点类型（如 `AdmonitionBlock.class`）。
3.  在 `render(Node node)` 中调用 `HtmlWriter` 输出标签。
4.  在 `HtmlRenderer.builder()` 中注册。

**示例**：
```java
public class AdmonitionRenderer implements NodeRenderer {
    // ...
    @Override
    public void render(Node node) {
        html.tag("div", attrs);
        context.renderChildren(node); // 渲染子节点
        html.closeTag("div");
    }
}
```

### 3.2 JavaFX 渲染扩展

使用 `JavaFxRenderer` 生成 JavaFX 组件树。

*   **`JavaFxNodeRenderer`**: 类似 `NodeRenderer`，但用于 JavaFX。
*   **`JavaFxNodeRendererFactory`**: 工厂接口。

#### 开发步骤：
1.  实现 `JavaFxNodeRenderer` 接口。
2.  在 `getNodeTypes()` 中返回节点类型。
3.  在 `render(Node node, JavaFxNodeRendererContext context)` 中创建并配置 JavaFX 节点（如 `VBox`, `TextFlow`）。
4.  在 `JavaFxRenderer.builder()` 中注册。

**示例**：
```java
public class AdmonitionJavaFxRenderer implements JavaFxNodeRenderer {
    @Override
    public void render(Node node, JavaFxNodeRendererContext context) {
        VBox box = new VBox();
        box.getStyleClass().add("admonition");
        // ... 配置样式
        context.getContainer().getChildren().add(box);
        
        // 递归渲染子节点到 box 中
        context.renderChildren(node, box);
    }
}
```

---

## 4. 核心类图与接口

### 解析器相关
*   `MarkdownParser` (入口)
    *   `BlockParserFactory` (列表)
    *   `InlineContentParserFactory` (列表)
*   `BlockParser` (状态机，用于多行解析)
*   `InlineContentParser` (单次解析)

### 渲染器相关
*   `HtmlRenderer` / `JavaFxRenderer` (入口)
    *   `NodeRendererFactory` / `JavaFxNodeRendererFactory` (列表)
*   `NodeRenderer` / `JavaFxNodeRenderer` (具体策略)
    *   `Set<Class<? extends Node>> getNodeTypes()` (声明处理的节点)
    *   `render(...)` (执行渲染)

## 5. 最佳实践

1.  **节点定义**：自定义 AST 节点应尽量保持纯数据结构，不包含业务逻辑。
2.  **访问者模式**：虽然渲染器使用了映射表（Map）而非标准的 Visitor 模式，但 AST 节点仍应保留 `accept(Visitor)` 方法以兼容其他工具（尽管自定义节点可能需要扩展 Visitor 接口或被忽略）。
3.  **无状态解析器**：`BlockParserFactory` 和 `InlineContentParserFactory` 通常是无状态的单例。`BlockParser` 是有状态的（每遇到一个新块创建一个实例）。
4.  **组合优先**：通过注册多个小的、功能单一的解析器/渲染器来构建复杂功能，而不是修改核心代码。
