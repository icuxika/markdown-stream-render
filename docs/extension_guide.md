# 插件开发指南 (Extension Guide)

Markdown Stream Render 提供了强大的扩展机制，允许开发者自定义新的 Markdown 语法、AST 节点以及在 HTML 和 JavaFX 中的渲染表现。

本文将以一个完整的 **Greeting (问候卡片)** 插件为例，演示如何从零开发一个跨平台的 Markdown 扩展。

## 1. 扩展架构概览

一个完整的插件通常包含以下三个层面的实现：

| 层面 | 接口 (Core/HTML/JavaFX) | 职责 |
| :--- | :--- | :--- |
| **Parser (解析层)** | `ParserExtension` | 定义新的 AST 节点，实现解析逻辑（Block 或 Inline）。 |
| **HTML Renderer (渲染层)** | `HtmlRendererExtension` | 将自定义 AST 节点转换为 HTML 标签字符串。 |
| **JavaFX Renderer (渲染层)** | `JavaFxRendererExtension` | 将自定义 AST 节点转换为 JavaFX UI 组件。 |

为了方便使用，我们通常会创建一个统一的 `Extension` 类同时实现这三个接口，实现“一次注册，全栈支持”。

---

## 2. 实战示例：开发 Greeting 插件

我们将实现一个自定义块级语法 `::: greeting <Name>`，它会被渲染为一个带有问候语的卡片。

### 2.1 定义 AST 节点

首先，我们需要定义一个新的 AST 节点来存储解析出的数据（这里是 `name`）。

> **注意**：自定义块级节点必须继承自 `Block`（实现了 `CustomNode` 接口或手动处理 Visitor）。

```java
import com.icuxika.markdown.stream.render.core.ast.Block;
import com.icuxika.markdown.stream.render.core.ast.Visitor;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRenderer;

public class GreetingBlock extends Block {
    private final String name;

    public GreetingBlock(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(Visitor visitor) {
        // 关键：将 visit 调用分发给对应的渲染器
        if (visitor instanceof HtmlRenderer) {
            ((HtmlRenderer) visitor).visit(this);
        } else if (visitor instanceof JavaFxRenderer) {
            ((JavaFxRenderer) visitor).visit(this);
        } else {
            // 默认行为：继续遍历子节点
            visitor.visitChildren(this);
        }
    }
}
```

### 2.2 实现解析逻辑 (Parser)

解析逻辑分为两部分：
1.  **`BlockParserFactory`**: 识别块的开始（例如检测到 `::: greeting`）。
2.  **`BlockParser`**: 处理块的具体内容（是否继续、何时结束）。

```java
import com.icuxika.markdown.stream.render.core.parser.block.*;

// 1. 工厂：负责识别语法
public class GreetingBlockParserFactory implements BlockParserFactory {
    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
        // 获取当前行内容
        String line = state.getLine().toString().substring(state.getNextNonSpaceIndex());
        
        // 正则匹配语法: ::: greeting <Name>
        Pattern pattern = Pattern.compile("^:::\\s+greeting\\s+(.*)$");
        Matcher matcher = pattern.matcher(line);
        
        if (matcher.matches()) {
            String name = matcher.group(1);
            // 匹配成功，启动 Parser
            return BlockStart.of(new GreetingParser(name))
                    .atIndex(state.getIndex() + line.length());
        }
        return BlockStart.none();
    }
}

// 2. 解析器：负责构建 AST 节点
public class GreetingParser implements BlockParser {
    private final GreetingBlock block;

    public GreetingParser(String name) {
        this.block = new GreetingBlock(name);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        // 这是一个单行块，不接受后续内容
        return BlockContinue.none();
    }
    
    // ... 其他方法可为空实现
}
```

### 2.3 实现 HTML 渲染器

将 `GreetingBlock` 渲染为 `<div class="greeting-card">...</div>`。

```java
import com.icuxika.markdown.stream.render.html.renderer.*;

public class GreetingHtmlRenderer implements HtmlNodeRenderer {
    private final HtmlNodeRendererContext context;

    public GreetingHtmlRenderer(HtmlNodeRendererContext context) {
        this.context = context;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.singleton(GreetingBlock.class);
    }

    @Override
    public void render(Node node) {
        GreetingBlock greeting = (GreetingBlock) node;
        HtmlWriter html = context.getWriter();
        
        // 输出 HTML
        html.tag("div", Collections.singletonMap("class", "greeting-card"));
        html.text("👋 Hello, " + greeting.getName() + "!");
        html.closeTag("div");
    }
}
```

### 2.4 实现 JavaFX 渲染器

将 `GreetingBlock` 渲染为一个带有样式的 JavaFX `Label`。

```java
import com.icuxika.markdown.stream.render.javafx.renderer.*;
import javafx.scene.control.Label;

public class GreetingJavaFxRenderer implements JavaFxNodeRenderer {
    private final JavaFxNodeRendererContext context;

    public GreetingJavaFxRenderer(JavaFxNodeRendererContext context) {
        this.context = context;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.singleton(GreetingBlock.class);
    }

    @Override
    public void render(Node node) {
        GreetingBlock greeting = (GreetingBlock) node;
        
        // 创建 JavaFX 组件
        Label label = new Label("👋 Hello, " + greeting.getName() + "!");
        label.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-width: 0 0 0 4px;");
        
        // 添加到当前容器
        context.getCurrentContainer().getChildren().add(label);
    }
}
```

### 2.5 封装为 Extension

最后，我们将上述所有组件打包成一个 `Extension`，方便用户使用。

```java
import com.icuxika.markdown.stream.render.core.parser.ParserExtension;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRendererExtension;
import com.icuxika.markdown.stream.render.javafx.renderer.JavaFxRendererExtension;

public class GreetingExtension implements ParserExtension, HtmlRendererExtension, JavaFxRendererExtension {
    
    // Parser 扩展：注册 BlockParserFactory
    @Override
    public void extend(MarkdownParser.Builder builder) {
        builder.blockParserFactory(new GreetingBlockParserFactory());
    }

    // HTML 渲染扩展：注册 HtmlNodeRendererFactory
    @Override
    public void extend(HtmlRenderer.Builder builder) {
        builder.nodeRendererFactory(GreetingHtmlRenderer::new);
    }

    // JavaFX 渲染扩展：注册 JavaFxNodeRendererFactory
    @Override
    public void extend(JavaFxRenderer.Builder builder) {
        builder.nodeRendererFactory(GreetingJavaFxRenderer::new);
    }
    
    public static GreetingExtension create() {
        return new GreetingExtension();
    }
}
```

---

## 3. 使用插件

现在，用户只需在构建 `MarkdownParser` 和 `Renderer` 时注册这个扩展即可。

```java
public void render(String markdown) {
    // 1. 准备扩展列表
    List<Extension> extensions = Arrays.asList(
        GreetingExtension.create() // 我们的自定义插件
        // AdmonitionExtension.create(), // 其他内置插件
        // MathExtension.create()
    );

    // 2. 配置 Parser
    MarkdownParser parser = MarkdownParser.builder()
            .extensions(extensions)
            .build();

    // 3. 配置 JavaFX Renderer
    JavaFxRenderer renderer = JavaFxRenderer.builder()
            .extensions(extensions)
            .build();
            
    // 4. 执行解析与渲染
    parser.parse(new StringReader(markdown), renderer);
    VBox result = (VBox) renderer.getResult();
    
    // ... 显示 result
}
```

## 4. 总结

通过实现 `ParserExtension`, `HtmlRendererExtension`, 和 `JavaFxRendererExtension` 接口，你可以轻松地扩展 Markdown Stream Render 的功能。自定义的节点通过 `CustomNode` (或 `Block` + `accept` 方法) 机制，能够无缝融入核心的 Visitor 遍历流程中，确保了极高的灵活性和可维护性。
