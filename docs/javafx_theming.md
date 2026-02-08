# JavaFX 主题与样式（推荐实践）

## 加载原则

- 在 `Scene` 级别加载 Markdown 样式与主题变量（便于主题切换与应用级覆盖）
- 渲染器（`JavaFxRenderer` / `JavaFxStreamRenderer`）不自动注入样式表

## 内置主题：Light / Dark

```java
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;

MarkdownTheme theme = new MarkdownTheme();
theme.apply(scene);
theme.setTheme(MarkdownTheme.Theme.DARK);
```

## 自定义主题：注册并切换

```java
import com.icuxika.markdown.stream.render.javafx.MarkdownTheme;

MarkdownTheme theme = new MarkdownTheme();
theme.registerTheme("sepia", getClass().getResource("/sepia.css").toExternalForm());
theme.apply(scene);
theme.setTheme("sepia");
```

## 完全自定义（仅复用结构样式）

```java
import com.icuxika.markdown.stream.render.javafx.MarkdownStyles;

MarkdownStyles.applyBase(scene, true);
scene.getStylesheets().add(getClass().getResource("/your-theme.css").toExternalForm());
```

## 变量语义集合（跨端对齐）

JavaFX 使用 `-md-*` Looked-up Colors；HTML 使用 `--md-*` CSS Variables。建议保持变量“语义”一致（名称仅差一个前缀符号），以便同一份主题能同时提供 web 与桌面两份实现。

