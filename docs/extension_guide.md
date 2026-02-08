# 扩展开发指南（Core / HTML / JavaFX）

## 目标

扩展应当在三个层面保持一致：

- **Core**：解析语法，产出 AST 节点（Node）
- **Renderer**：将节点渲染到目标端（HTML / JavaFX）
- **Style**：提供结构样式与主题变量（可被宿主覆盖）

## Core 侧（解析）

一个扩展通常包含：

- `Node` 子类（例如 `AdmonitionBlock`）
- `BlockParserFactory` 或 `InlineContentParserFactory`
- 可选：提供一个 `XxxExtension.addTo(builder)` 形式的注册工具，便于用户一行启用

本项目的默认扩展注册入口位于 [CoreExtension](file:///c:/Users/icuxika/IdeaProjects/markdown-stream-render/core/src/main/java/com/icuxika/markdown/stream/render/core/CoreExtension.java)。

## Renderer 侧（渲染）

- **HTML（全量）**：实现 `HtmlNodeRenderer`，将扩展节点映射为 HTML 结构
- **HTML（流式）**：如需支持流式输出，确保 `HtmlStreamRenderer` 对容器块与叶子块的事件处理一致
- **JavaFX（全量）**：实现 `JavaFxNodeRenderer`
- **JavaFX（流式）**：确保 `JavaFxStreamRenderer` 在 `openBlock/closeBlock/renderNode` 的行为与全量渲染一致

## 样式与命名约定

### CSS 类名

- 推荐使用 `markdown-<feature>` 前缀，避免与宿主应用的 CSS 冲突
- 扩展类名建议同时保留旧别名（若存在历史包袱），以降低升级成本

示例（Admonition）：

- `markdown-admonition`
- `markdown-admonition-title`
- `markdown-admonition-info` / `markdown-admonition-warning` / `markdown-admonition-error`

### 主题变量

- JavaFX：`-md-<feature>-...`
- HTML：`--md-<feature>-...`

尽量保持“语义集合”一致，例如：

- `-md-admonition-bg-color` ↔ `--md-admonition-bg-color`
- `-md-inline-math-bg-color` ↔ `--md-inline-math-bg-color`

## 可参考的内置扩展

- Admonition（Core / JavaFX / HTML + CSS）
- Math（Core / JavaFX / HTML + CSS）

