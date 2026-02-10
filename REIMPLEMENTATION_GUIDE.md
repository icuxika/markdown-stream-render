# Reimplementation Guide (复现指南)

这份文档旨在指导 AI 助手或开发者从零开始复现 `markdown-stream-render` 项目。它浓缩了本项目的核心架构决策、关键算法难点以及性能优化方案。

---

## 1. 核心设计哲学 (Core Philosophy)

在开始写第一行代码前，必须明确以下原则：

1.  **流式优先 (Streaming First)**: 
    *   **不要**设计成 `String -> AST` 的全量转换。
    *   **必须**设计成 `char/chunk -> Event -> Partial AST` 的增量处理。解析器必须是状态机的，能够随时暂停和恢复。
2.  **规范驱动 (Spec Driven)**:
    *   Markdown 不是随意的文本格式，必须严格遵循 **CommonMark Spec**。
    *   **起手式**：先导入 `spec.json` 测试集，而不是先写代码。
3.  **零依赖 (Zero Dependency)**:
    *   核心模块 (`core`) 严禁引入任何第三方库（如 guava, apache-commons），确保其极致轻量和可移植性。

---

## 2. 核心解析器架构 (The Parser)

### 2.1 分层解析 (Two-Phase Parsing)
CommonMark 要求解析分为两个阶段，复现时必须严格遵守：

1.  **块级解析 (Block Parsing)**:
    *   逐行扫描。
    *   维护一个 `BlockParserState` 栈（当前打开的块）。
    *   **难点**: 处理 "Lazy Continuation"（懒惰延续）。即：一个段落可以在引用块 (`>`) 中跨行延续，而无需每行都加 `>`。
2.  **内联解析 (Inline Parsing)**:
    *   只有当一个块彻底关闭（Closed）后，才对其文本内容进行内联解析。
    *   **难点**: 强调符号 (`*`, `_`) 的解析是极其复杂的，不要试图用正则解决，必须实现 **"Delimiter Stack" (分隔符栈)** 算法。

### 2.2 流式事件机制 (Streaming Events)
解析器不应直接返回 AST，而应通过回调接口发射事件：
*   `openBlock(Node)`
*   `renderNode(Node)` (用于增量更新文本)
*   `closeBlock(Node)`

---

## 3. JavaFX 渲染器架构 (The Renderer)

这是本项目最精华的部分，解决了“无限长流式文档”的性能问题。

### 3.1 错误的路径 (The Wrong Way)
*   ❌ **全量刷新**: 每次有新字符，清空 VBox 重绘。 -> **O(N^2) 性能灾难**。
*   ❌ **单纯追加**: 把所有节点塞进一个 VBox。 -> **内存泄漏，布局计算卡顿**。

### 3.2 正确的路径：混合渲染 (Hybrid Rendering)
必须实现 **"Virtualization + Active Stream"** 混合模式：

1.  **历史区 (History)**:
    *   使用 `ListView<Node>`。
    *   利用 JavaFX 的 `VirtualFlow` 机制，只渲染屏幕可见的 Item。
    *   **关键**: Item 存储的是轻量级 AST 数据，而非 UI 组件。
2.  **活跃区 (Active Stream)**:
    *   使用一个独立的 `VBox` (`activeContainer`)。
    *   当前正在生成的块（Open 状态）渲染在这里。
    *   **平滑性**: 字符追加直接操作这个 VBox 内的 TextFlow，无需触发 ListView 的刷新。
3.  **生命周期流转**:
    *   `Open`: 在 `activeContainer` 创建 UI。
    *   `Append`: 更新 `activeContainer` 中的 UI。
    *   `Close`: 将 AST 移入 `ListView` 数据源，**同时从 `activeContainer` 移除 UI**。

### 3.3 并发与防抖 (Concurrency)
*   **单线程模型**: 解析器在后台线程，UI 在 FX 线程。
*   **任务队列**: 必须实现一个 `uiTaskQueue`，将后台事件串行化后提交给 `Platform.runLater`。
*   **时间分片 (Time Slicing)**: 在 `runLater` 中处理队列时，必须加时间锁（如 8ms），超时则让出 CPU 到下一帧。**这是防止 UI 卡死的关键。**

---

## 4. 关键算法避坑 (Gotchas)

1.  **制表符 (Tabs)**:
    *   Markdown 规范要求 Tab 展开为 4 个空格，但不是简单的替换，而是基于 **列位置 (Column Index)** 的对齐。必须实现 `Virtual Column` 计算。
2.  **HTML 实体**:
    *   必须支持 `&copy;`, `&#123;`, `&#x1F600;` 的解码。
3.  **链接引用定义 (Link Reference Definitions)**:
    *   必须在文档末尾或任意位置扫描 `[id]: url`，并支持不区分大小写的 Unicode 匹配（Case Folding）。

---

## 5. 推荐开发顺序 (Roadmap)

1.  **Day 1**: 搭建 AST 基础类结构 (`Node`, `Block`, `Inline`)。
2.  **Day 2**: 实现 `BlockParser`，跑通简单的段落和标题测试。
3.  **Day 3**: 实现 `InlineParser`，重点攻克强调符号 (`Emphasis`) 算法。
4.  **Day 4**: 实现 `HtmlRenderer`，并通过 `spec.json` 验证核心正确性。
5.  **Day 5**: 实现 `JavaFxStreamRenderer` (基础版)。
6.  **Day 6**: 重构为 `VirtualJavaFxStreamRenderer` (混合高性能版)。

---

## 6. 核心代码片段参考

**混合渲染器的任务调度：**
```java
private void processUiTasks() {
    long startTime = System.nanoTime();
    long maxDuration = 8_000_000; // 8ms budget

    while ((task = uiTaskQueue.poll()) != null) {
        task.run();
        if (System.nanoTime() - startTime > maxDuration) {
            // Yield to next frame
            Platform.runLater(this::processUiTasks);
            break;
        }
    }
}
```
