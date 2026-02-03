# 项目总结报告 (Project Summary Report)

## 1. 项目概述 (Project Overview)
本项目旨在从零开始构建一个符合 **CommonMark** 规范的 Markdown 解析器，并提供 HTML 渲染和 JavaFX 流式渲染功能。项目包含核心解析库 (`core`) 和演示应用 (`demo`) 两个模块。

目前，核心解析器已通过 CommonMark 规范测试集 (spec 0.31.2) 中 **652 个测试用例中的 651 个**，通过率高达 **99.8%**。唯一未通过的用例涉及特殊 Unicode 字符的大小写折叠 (Case Folding) 边缘情况。

## 2. 任务完成过程 (Task Completion Process)

任务执行主要分为以下几个阶段：

### 第一阶段：项目初始化与基础架构
- 创建 Maven 多模块项目结构 (`core` 和 `demo`)。
- 定义 AST (抽象语法树) 节点类层次结构 (`Node`, `Block`, `Inline` 等)。
- 建立基于状态机的块级解析器 (`BlockParserState`) 和内联解析器 (`InlineParser`) 框架。

### 第二阶段：核心语法解析实现
按照 CommonMark 规范逐步实现了以下语法特性的解析：
- **块级元素**: 段落、ATX 标题、Setext 标题、缩进代码块、Fenced 代码块、引用块 (Block Quote)、列表 (无序/有序、紧凑/松散)、HTML 区块、分割线 (Thematic Break)。
- **内联元素**: 文本、反斜杠转义、实体引用、代码片段 (Code Spans)、强调 (Emphasis/Strong)、链接 (Links)、图片 (Images)、自动链接、原生 HTML 标签、硬/软换行。
- **复杂逻辑**:
  - **制表符展开 (Tabs)**: 实现了虚拟列 (Virtual Column) 计算，正确处理制表符缩进。
  - **列表嵌套与松散判断**: 实现了复杂的列表紧凑性 (Tight/Loose) 判断逻辑。
  - **HTML 区块识别**: 完整实现了 7 种 HTML 区块类型的识别条件。
  - **链接引用定义**: 支持文档末尾或中间的链接引用定义提取与正规化。

### 第三阶段：渲染器实现
- **HTML 渲染器**: 实现了 `HtmlRenderer`，将 AST 转换为符合规范的 HTML 字符串。
- **JavaFX 渲染器**: 实现了 `JavaFxRenderer`，将 AST 直接渲染为 JavaFX UI 组件树 (`VBox`, `TextFlow` 等)，支持富文本样式和图片加载。

### 第四阶段：规范测试与修复
- 引入 `commonmark-spec.json` 测试集。
- 编写测试类 `CommonMarkSpecTest` 和 `FullSpecTest`。
- 针对测试失败的用例进行逐一修复，特别是 HTML 区块结束条件、列表项缩进计算、制表符保留等细节。

## 3. 技术难点与注意事项 (Technical Challenges & Precautions)

在开发过程中，以下几点是需要特别注意的技术细节，供其他大模型或开发者参考：

1.  **块级解析的状态机**: Markdown 的块级解析是行导向的。每一行都需要检查是否延续当前容器 (Container) 或开启新容器。必须维护一个 `openContainers` 栈，并正确处理 Lazy Continuation (懒惰延续) —— 即段落内容可以在不缩进的情况下延续到引用块或列表项中。
2.  **制表符 (Tabs) 处理**: CommonMark 要求将制表符视作对齐到下一个 4 的倍数列。在解析缩进时，不能简单地将 `\t` 替换为 4 个空格，而必须计算当前行位置的虚拟列偏移。
3.  **内联解析优先级**: 链接 (`[...]`)、图片 (`![...]`) 和强调 (`*`, `_`) 的解析存在优先级和嵌套限制。特别是链接的文本中不能包含其他链接，但可以包含图片。
4.  **Unicode 大小写折叠**: 链接引用定义 (Link Reference Definitions) 的标签匹配要求进行 Unicode Case Fold。Java 的 `toLowerCase()` 在某些特殊字符 (如 `ẞ`) 上可能与规范要求的折叠行为不完全一致 (这是目前唯一的遗留 Fail)。
5.  **JavaFX 文本流**: 在 JavaFX 中渲染 Markdown 时，`TextFlow` 是处理混合样式 (粗体、斜体、链接混排) 的最佳选择，而块级元素则适合用 `VBox` 布局。

## 4. 接下来的计划 (Next Steps)

1.  **修复剩余的一个测试用例**:
    - 针对 Example 540 (Links)，引入更严格的 Unicode Case Folding 库或映射表，以正确处理 `ẞ` 等特殊字符的归一化。
2.  **性能优化**:
    - 目前解析器在处理深层嵌套或大量回溯时可能存在性能瓶颈，可以引入 memoization 或优化正则匹配。
3.  **JavaFX 渲染器增强**:
    - 支持表格 (Table) 渲染 (GridPane)。
    - 支持代码块语法高亮。
    - 优化图片异步加载体验。
4.  **扩展语法支持**:
    - 支持 GFM (GitHub Flavored Markdown) 扩展，如表格、任务列表、删除线等。

## 5. CommonMark 规范测试完整报告 (Spec Test Report)

以下是基于 CommonMark Spec 0.31.2 的完整测试结果：

| Example | Section | Status | Info |
| :--- | :--- | :--- | :--- |
| 1 | Tabs | ✅ PASS |  |
| 2 | Tabs | ✅ PASS |  |
| 3 | Tabs | ✅ PASS |  |
| 4 | Tabs | ✅ PASS |  |
| 5 | Tabs | ✅ PASS |  |
| 6 | Tabs | ✅ PASS |  |
| 7 | Tabs | ✅ PASS |  |
| 8 | Tabs | ✅ PASS |  |
| 9 | Tabs | ✅ PASS |  |
| 10 | Tabs | ✅ PASS |  |
| 11 | Tabs | ✅ PASS |  |
| 12 | Backslash escapes | ✅ PASS |  |
| 13 | Backslash escapes | ✅ PASS |  |
| 14 | Backslash escapes | ✅ PASS |  |
| 15 | Backslash escapes | ✅ PASS |  |
| 16 | Backslash escapes | ✅ PASS |  |
| 17 | Backslash escapes | ✅ PASS |  |
| 18 | Backslash escapes | ✅ PASS |  |
| 19 | Backslash escapes | ✅ PASS |  |
| 20 | Backslash escapes | ✅ PASS |  |
| 21 | Backslash escapes | ✅ PASS |  |
| 22 | Backslash escapes | ✅ PASS |  |
| 23 | Backslash escapes | ✅ PASS |  |
| 24 | Backslash escapes | ✅ PASS |  |
| 25 | Entity and numeric character references | ✅ PASS |  |
| 26 | Entity and numeric character references | ✅ PASS |  |
| 27 | Entity and numeric character references | ✅ PASS |  |
| 28 | Entity and numeric character references | ✅ PASS |  |
| 29 | Entity and numeric character references | ✅ PASS |  |
| 30 | Entity and numeric character references | ✅ PASS |  |
| 31 | Entity and numeric character references | ✅ PASS |  |
| 32 | Entity and numeric character references | ✅ PASS |  |
| 33 | Entity and numeric character references | ✅ PASS |  |
| 34 | Entity and numeric character references | ✅ PASS |  |
| 35 | Entity and numeric character references | ✅ PASS |  |
| 36 | Entity and numeric character references | ✅ PASS |  |
| 37 | Entity and numeric character references | ✅ PASS |  |
| 38 | Entity and numeric character references | ✅ PASS |  |
| 39 | Entity and numeric character references | ✅ PASS |  |
| 40 | Entity and numeric character references | ✅ PASS |  |
| 41 | Entity and numeric character references | ✅ PASS |  |
| 42 | Precedence | ✅ PASS |  |
| 43 | Thematic breaks | ✅ PASS |  |
| 44 | Thematic breaks | ✅ PASS |  |
| 45 | Thematic breaks | ✅ PASS |  |
| 46 | Thematic breaks | ✅ PASS |  |
| 47 | Thematic breaks | ✅ PASS |  |
| 48 | Thematic breaks | ✅ PASS |  |
| 49 | Thematic breaks | ✅ PASS |  |
| 50 | Thematic breaks | ✅ PASS |  |
| 51 | Thematic breaks | ✅ PASS |  |
| 52 | Thematic breaks | ✅ PASS |  |
| 53 | Thematic breaks | ✅ PASS |  |
| 54 | Thematic breaks | ✅ PASS |  |
| 55 | Thematic breaks | ✅ PASS |  |
| 56 | Thematic breaks | ✅ PASS |  |
| 57 | Thematic breaks | ✅ PASS |  |
| 58 | Thematic breaks | ✅ PASS |  |
| 59 | Thematic breaks | ✅ PASS |  |
| 60 | Thematic breaks | ✅ PASS |  |
| 61 | Thematic breaks | ✅ PASS |  |
| 62 | ATX headings | ✅ PASS |  |
| 63 | ATX headings | ✅ PASS |  |
| 64 | ATX headings | ✅ PASS |  |
| 65 | ATX headings | ✅ PASS |  |
| 66 | ATX headings | ✅ PASS |  |
| 67 | ATX headings | ✅ PASS |  |
| 68 | ATX headings | ✅ PASS |  |
| 69 | ATX headings | ✅ PASS |  |
| 70 | ATX headings | ✅ PASS |  |
| 71 | ATX headings | ✅ PASS |  |
| 72 | ATX headings | ✅ PASS |  |
| 73 | ATX headings | ✅ PASS |  |
| 74 | ATX headings | ✅ PASS |  |
| 75 | ATX headings | ✅ PASS |  |
| 76 | ATX headings | ✅ PASS |  |
| 77 | ATX headings | ✅ PASS |  |
| 78 | ATX headings | ✅ PASS |  |
| 79 | ATX headings | ✅ PASS |  |
| 80 | Setext headings | ✅ PASS |  |
| 81 | Setext headings | ✅ PASS |  |
| 82 | Setext headings | ✅ PASS |  |
| 83 | Setext headings | ✅ PASS |  |
| 84 | Setext headings | ✅ PASS |  |
| 85 | Setext headings | ✅ PASS |  |
| 86 | Setext headings | ✅ PASS |  |
| 87 | Setext headings | ✅ PASS |  |
| 88 | Setext headings | ✅ PASS |  |
| 89 | Setext headings | ✅ PASS |  |
| 90 | Setext headings | ✅ PASS |  |
| 91 | Setext headings | ✅ PASS |  |
| 92 | Setext headings | ✅ PASS |  |
| 93 | Setext headings | ✅ PASS |  |
| 94 | Setext headings | ✅ PASS |  |
| 95 | Setext headings | ✅ PASS |  |
| 96 | Setext headings | ✅ PASS |  |
| 97 | Setext headings | ✅ PASS |  |
| 98 | Setext headings | ✅ PASS |  |
| 99 | Setext headings | ✅ PASS |  |
| 100 | Setext headings | ✅ PASS |  |
| 101 | Setext headings | ✅ PASS |  |
| 102 | Setext headings | ✅ PASS |  |
| 103 | Setext headings | ✅ PASS |  |
| 104 | Setext headings | ✅ PASS |  |
| 105 | Setext headings | ✅ PASS |  |
| 106 | Setext headings | ✅ PASS |  |
| 107 | Indented code blocks | ✅ PASS |  |
| 108 | Indented code blocks | ✅ PASS |  |
| 109 | Indented code blocks | ✅ PASS |  |
| 110 | Indented code blocks | ✅ PASS |  |
| 111 | Indented code blocks | ✅ PASS |  |
| 112 | Indented code blocks | ✅ PASS |  |
| 113 | Indented code blocks | ✅ PASS |  |
| 114 | Indented code blocks | ✅ PASS |  |
| 115 | Indented code blocks | ✅ PASS |  |
| 116 | Indented code blocks | ✅ PASS |  |
| 117 | Indented code blocks | ✅ PASS |  |
| 118 | Indented code blocks | ✅ PASS |  |
| 119 | Fenced code blocks | ✅ PASS |  |
| 120 | Fenced code blocks | ✅ PASS |  |
| 121 | Fenced code blocks | ✅ PASS |  |
| 122 | Fenced code blocks | ✅ PASS |  |
| 123 | Fenced code blocks | ✅ PASS |  |
| 124 | Fenced code blocks | ✅ PASS |  |
| 125 | Fenced code blocks | ✅ PASS |  |
| 126 | Fenced code blocks | ✅ PASS |  |
| 127 | Fenced code blocks | ✅ PASS |  |
| 128 | Fenced code blocks | ✅ PASS |  |
| 129 | Fenced code blocks | ✅ PASS |  |
| 130 | Fenced code blocks | ✅ PASS |  |
| 131 | Fenced code blocks | ✅ PASS |  |
| 132 | Fenced code blocks | ✅ PASS |  |
| 133 | Fenced code blocks | ✅ PASS |  |
| 134 | Fenced code blocks | ✅ PASS |  |
| 135 | Fenced code blocks | ✅ PASS |  |
| 136 | Fenced code blocks | ✅ PASS |  |
| 137 | Fenced code blocks | ✅ PASS |  |
| 138 | Fenced code blocks | ✅ PASS |  |
| 139 | Fenced code blocks | ✅ PASS |  |
| 140 | Fenced code blocks | ✅ PASS |  |
| 141 | Fenced code blocks | ✅ PASS |  |
| 142 | Fenced code blocks | ✅ PASS |  |
| 143 | Fenced code blocks | ✅ PASS |  |
| 144 | Fenced code blocks | ✅ PASS |  |
| 145 | Fenced code blocks | ✅ PASS |  |
| 146 | Fenced code blocks | ✅ PASS |  |
| 147 | Fenced code blocks | ✅ PASS |  |
| 148 | HTML blocks | ✅ PASS |  |
| 149 | HTML blocks | ✅ PASS |  |
| 150 | HTML blocks | ✅ PASS |  |
| 151 | HTML blocks | ✅ PASS |  |
| 152 | HTML blocks | ✅ PASS |  |
| 153 | HTML blocks | ✅ PASS |  |
| 154 | HTML blocks | ✅ PASS |  |
| 155 | HTML blocks | ✅ PASS |  |
| 156 | HTML blocks | ✅ PASS |  |
| 157 | HTML blocks | ✅ PASS |  |
| 158 | HTML blocks | ✅ PASS |  |
| 159 | HTML blocks | ✅ PASS |  |
| 160 | HTML blocks | ✅ PASS |  |
| 161 | HTML blocks | ✅ PASS |  |
| 162 | HTML blocks | ✅ PASS |  |
| 163 | HTML blocks | ✅ PASS |  |
| 164 | HTML blocks | ✅ PASS |  |
| 165 | HTML blocks | ✅ PASS |  |
| 166 | HTML blocks | ✅ PASS |  |
| 167 | HTML blocks | ✅ PASS |  |
| 168 | HTML blocks | ✅ PASS |  |
| 169 | HTML blocks | ✅ PASS |  |
| 170 | HTML blocks | ✅ PASS |  |
| 171 | HTML blocks | ✅ PASS |  |
| 172 | HTML blocks | ✅ PASS |  |
| 173 | HTML blocks | ✅ PASS |  |
| 174 | HTML blocks | ✅ PASS |  |
| 175 | HTML blocks | ✅ PASS |  |
| 176 | HTML blocks | ✅ PASS |  |
| 177 | HTML blocks | ✅ PASS |  |
| 178 | HTML blocks | ✅ PASS |  |
| 179 | HTML blocks | ✅ PASS |  |
| 180 | HTML blocks | ✅ PASS |  |
| 181 | HTML blocks | ✅ PASS |  |
| 182 | HTML blocks | ✅ PASS |  |
| 183 | HTML blocks | ✅ PASS |  |
| 184 | HTML blocks | ✅ PASS |  |
| 185 | HTML blocks | ✅ PASS |  |
| 186 | HTML blocks | ✅ PASS |  |
| 187 | HTML blocks | ✅ PASS |  |
| 188 | HTML blocks | ✅ PASS |  |
| 189 | HTML blocks | ✅ PASS |  |
| 190 | HTML blocks | ✅ PASS |  |
| 191 | HTML blocks | ✅ PASS |  |
| 192 | Link reference definitions | ✅ PASS |  |
| 193 | Link reference definitions | ✅ PASS |  |
| 194 | Link reference definitions | ✅ PASS |  |
| 195 | Link reference definitions | ✅ PASS |  |
| 196 | Link reference definitions | ✅ PASS |  |
| 197 | Link reference definitions | ✅ PASS |  |
| 198 | Link reference definitions | ✅ PASS |  |
| 199 | Link reference definitions | ✅ PASS |  |
| 200 | Link reference definitions | ✅ PASS |  |
| 201 | Link reference definitions | ✅ PASS |  |
| 202 | Link reference definitions | ✅ PASS |  |
| 203 | Link reference definitions | ✅ PASS |  |
| 204 | Link reference definitions | ✅ PASS |  |
| 205 | Link reference definitions | ✅ PASS |  |
| 206 | Link reference definitions | ✅ PASS |  |
| 207 | Link reference definitions | ✅ PASS |  |
| 208 | Link reference definitions | ✅ PASS |  |
| 209 | Link reference definitions | ✅ PASS |  |
| 210 | Link reference definitions | ✅ PASS |  |
| 211 | Link reference definitions | ✅ PASS |  |
| 212 | Link reference definitions | ✅ PASS |  |
| 213 | Link reference definitions | ✅ PASS |  |
| 214 | Link reference definitions | ✅ PASS |  |
| 215 | Link reference definitions | ✅ PASS |  |
| 216 | Link reference definitions | ✅ PASS |  |
| 217 | Link reference definitions | ✅ PASS |  |
| 218 | Link reference definitions | ✅ PASS |  |
| 219 | Paragraphs | ✅ PASS |  |
| 220 | Paragraphs | ✅ PASS |  |
| 221 | Paragraphs | ✅ PASS |  |
| 222 | Paragraphs | ✅ PASS |  |
| 223 | Paragraphs | ✅ PASS |  |
| 224 | Paragraphs | ✅ PASS |  |
| 225 | Paragraphs | ✅ PASS |  |
| 226 | Paragraphs | ✅ PASS |  |
| 227 | Blank lines | ✅ PASS |  |
| 228 | Block quotes | ✅ PASS |  |
| 229 | Block quotes | ✅ PASS |  |
| 230 | Block quotes | ✅ PASS |  |
| 231 | Block quotes | ✅ PASS |  |
| 232 | Block quotes | ✅ PASS |  |
| 233 | Block quotes | ✅ PASS |  |
| 234 | Block quotes | ✅ PASS |  |
| 235 | Block quotes | ✅ PASS |  |
| 236 | Block quotes | ✅ PASS |  |
| 237 | Block quotes | ✅ PASS |  |
| 238 | Block quotes | ✅ PASS |  |
| 239 | Block quotes | ✅ PASS |  |
| 240 | Block quotes | ✅ PASS |  |
| 241 | Block quotes | ✅ PASS |  |
| 242 | Block quotes | ✅ PASS |  |
| 243 | Block quotes | ✅ PASS |  |
| 244 | Block quotes | ✅ PASS |  |
| 245 | Block quotes | ✅ PASS |  |
| 246 | Block quotes | ✅ PASS |  |
| 247 | Block quotes | ✅ PASS |  |
| 248 | Block quotes | ✅ PASS |  |
| 249 | Block quotes | ✅ PASS |  |
| 250 | Block quotes | ✅ PASS |  |
| 251 | Block quotes | ✅ PASS |  |
| 252 | Block quotes | ✅ PASS |  |
| 253 | List items | ✅ PASS |  |
| 254 | List items | ✅ PASS |  |
| 255 | List items | ✅ PASS |  |
| 256 | List items | ✅ PASS |  |
| 257 | List items | ✅ PASS |  |
| 258 | List items | ✅ PASS |  |
| 259 | List items | ✅ PASS |  |
| 260 | List items | ✅ PASS |  |
| 261 | List items | ✅ PASS |  |
| 262 | List items | ✅ PASS |  |
| 263 | List items | ✅ PASS |  |
| 264 | List items | ✅ PASS |  |
| 265 | List items | ✅ PASS |  |
| 266 | List items | ✅ PASS |  |
| 267 | List items | ✅ PASS |  |
| 268 | List items | ✅ PASS |  |
| 269 | List items | ✅ PASS |  |
| 270 | List items | ✅ PASS |  |
| 271 | List items | ✅ PASS |  |
| 272 | List items | ✅ PASS |  |
| 273 | List items | ✅ PASS |  |
| 274 | List items | ✅ PASS |  |
| 275 | List items | ✅ PASS |  |
| 276 | List items | ✅ PASS |  |
| 277 | List items | ✅ PASS |  |
| 278 | List items | ✅ PASS |  |
| 279 | List items | ✅ PASS |  |
| 280 | List items | ✅ PASS |  |
| 281 | List items | ✅ PASS |  |
| 282 | List items | ✅ PASS |  |
| 283 | List items | ✅ PASS |  |
| 284 | List items | ✅ PASS |  |
| 285 | List items | ✅ PASS |  |
| 286 | List items | ✅ PASS |  |
| 287 | List items | ✅ PASS |  |
| 288 | List items | ✅ PASS |  |
| 289 | List items | ✅ PASS |  |
| 290 | List items | ✅ PASS |  |
| 291 | List items | ✅ PASS |  |
| 292 | List items | ✅ PASS |  |
| 293 | List items | ✅ PASS |  |
| 294 | List items | ✅ PASS |  |
| 295 | List items | ✅ PASS |  |
| 296 | List items | ✅ PASS |  |
| 297 | List items | ✅ PASS |  |
| 298 | List items | ✅ PASS |  |
| 299 | List items | ✅ PASS |  |
| 300 | List items | ✅ PASS |  |
| 301 | Lists | ✅ PASS |  |
| 302 | Lists | ✅ PASS |  |
| 303 | Lists | ✅ PASS |  |
| 304 | Lists | ✅ PASS |  |
| 305 | Lists | ✅ PASS |  |
| 306 | Lists | ✅ PASS |  |
| 307 | Lists | ✅ PASS |  |
| 308 | Lists | ✅ PASS |  |
| 309 | Lists | ✅ PASS |  |
| 310 | Lists | ✅ PASS |  |
| 311 | Lists | ✅ PASS |  |
| 312 | Lists | ✅ PASS |  |
| 313 | Lists | ✅ PASS |  |
| 314 | Lists | ✅ PASS |  |
| 315 | Lists | ✅ PASS |  |
| 316 | Lists | ✅ PASS |  |
| 317 | Lists | ✅ PASS |  |
| 318 | Lists | ✅ PASS |  |
| 319 | Lists | ✅ PASS |  |
| 320 | Lists | ✅ PASS |  |
| 321 | Lists | ✅ PASS |  |
| 322 | Lists | ✅ PASS |  |
| 323 | Lists | ✅ PASS |  |
| 324 | Lists | ✅ PASS |  |
| 325 | Lists | ✅ PASS |  |
| 326 | Lists | ✅ PASS |  |
| 327 | Inlines | ✅ PASS |  |
| 328 | Code spans | ✅ PASS |  |
| 329 | Code spans | ✅ PASS |  |
| 330 | Code spans | ✅ PASS |  |
| 331 | Code spans | ✅ PASS |  |
| 332 | Code spans | ✅ PASS |  |
| 333 | Code spans | ✅ PASS |  |
| 334 | Code spans | ✅ PASS |  |
| 335 | Code spans | ✅ PASS |  |
| 336 | Code spans | ✅ PASS |  |
| 337 | Code spans | ✅ PASS |  |
| 338 | Code spans | ✅ PASS |  |
| 339 | Code spans | ✅ PASS |  |
| 340 | Code spans | ✅ PASS |  |
| 341 | Code spans | ✅ PASS |  |
| 342 | Code spans | ✅ PASS |  |
| 343 | Code spans | ✅ PASS |  |
| 344 | Code spans | ✅ PASS |  |
| 345 | Code spans | ✅ PASS |  |
| 346 | Code spans | ✅ PASS |  |
| 347 | Code spans | ✅ PASS |  |
| 348 | Code spans | ✅ PASS |  |
| 349 | Code spans | ✅ PASS |  |
| 350 | Emphasis and strong emphasis | ✅ PASS |  |
| 351 | Emphasis and strong emphasis | ✅ PASS |  |
| 352 | Emphasis and strong emphasis | ✅ PASS |  |
| 353 | Emphasis and strong emphasis | ✅ PASS |  |
| 354 | Emphasis and strong emphasis | ✅ PASS |  |
| 355 | Emphasis and strong emphasis | ✅ PASS |  |
| 356 | Emphasis and strong emphasis | ✅ PASS |  |
| 357 | Emphasis and strong emphasis | ✅ PASS |  |
| 358 | Emphasis and strong emphasis | ✅ PASS |  |
| 359 | Emphasis and strong emphasis | ✅ PASS |  |
| 360 | Emphasis and strong emphasis | ✅ PASS |  |
| 361 | Emphasis and strong emphasis | ✅ PASS |  |
| 362 | Emphasis and strong emphasis | ✅ PASS |  |
| 363 | Emphasis and strong emphasis | ✅ PASS |  |
| 364 | Emphasis and strong emphasis | ✅ PASS |  |
| 365 | Emphasis and strong emphasis | ✅ PASS |  |
| 366 | Emphasis and strong emphasis | ✅ PASS |  |
| 367 | Emphasis and strong emphasis | ✅ PASS |  |
| 368 | Emphasis and strong emphasis | ✅ PASS |  |
| 369 | Emphasis and strong emphasis | ✅ PASS |  |
| 370 | Emphasis and strong emphasis | ✅ PASS |  |
| 371 | Emphasis and strong emphasis | ✅ PASS |  |
| 372 | Emphasis and strong emphasis | ✅ PASS |  |
| 373 | Emphasis and strong emphasis | ✅ PASS |  |
| 374 | Emphasis and strong emphasis | ✅ PASS |  |
| 375 | Emphasis and strong emphasis | ✅ PASS |  |
| 376 | Emphasis and strong emphasis | ✅ PASS |  |
| 377 | Emphasis and strong emphasis | ✅ PASS |  |
| 378 | Emphasis and strong emphasis | ✅ PASS |  |
| 379 | Emphasis and strong emphasis | ✅ PASS |  |
| 380 | Emphasis and strong emphasis | ✅ PASS |  |
| 381 | Emphasis and strong emphasis | ✅ PASS |  |
| 382 | Emphasis and strong emphasis | ✅ PASS |  |
| 383 | Emphasis and strong emphasis | ✅ PASS |  |
| 384 | Emphasis and strong emphasis | ✅ PASS |  |
| 385 | Emphasis and strong emphasis | ✅ PASS |  |
| 386 | Emphasis and strong emphasis | ✅ PASS |  |
| 387 | Emphasis and strong emphasis | ✅ PASS |  |
| 388 | Emphasis and strong emphasis | ✅ PASS |  |
| 389 | Emphasis and strong emphasis | ✅ PASS |  |
| 390 | Emphasis and strong emphasis | ✅ PASS |  |
| 391 | Emphasis and strong emphasis | ✅ PASS |  |
| 392 | Emphasis and strong emphasis | ✅ PASS |  |
| 393 | Emphasis and strong emphasis | ✅ PASS |  |
| 394 | Emphasis and strong emphasis | ✅ PASS |  |
| 395 | Emphasis and strong emphasis | ✅ PASS |  |
| 396 | Emphasis and strong emphasis | ✅ PASS |  |
| 397 | Emphasis and strong emphasis | ✅ PASS |  |
| 398 | Emphasis and strong emphasis | ✅ PASS |  |
| 399 | Emphasis and strong emphasis | ✅ PASS |  |
| 400 | Emphasis and strong emphasis | ✅ PASS |  |
| 401 | Emphasis and strong emphasis | ✅ PASS |  |
| 402 | Emphasis and strong emphasis | ✅ PASS |  |
| 403 | Emphasis and strong emphasis | ✅ PASS |  |
| 404 | Emphasis and strong emphasis | ✅ PASS |  |
| 405 | Emphasis and strong emphasis | ✅ PASS |  |
| 406 | Emphasis and strong emphasis | ✅ PASS |  |
| 407 | Emphasis and strong emphasis | ✅ PASS |  |
| 408 | Emphasis and strong emphasis | ✅ PASS |  |
| 409 | Emphasis and strong emphasis | ✅ PASS |  |
| 410 | Emphasis and strong emphasis | ✅ PASS |  |
| 411 | Emphasis and strong emphasis | ✅ PASS |  |
| 412 | Emphasis and strong emphasis | ✅ PASS |  |
| 413 | Emphasis and strong emphasis | ✅ PASS |  |
| 414 | Emphasis and strong emphasis | ✅ PASS |  |
| 415 | Emphasis and strong emphasis | ✅ PASS |  |
| 416 | Emphasis and strong emphasis | ✅ PASS |  |
| 417 | Emphasis and strong emphasis | ✅ PASS |  |
| 418 | Emphasis and strong emphasis | ✅ PASS |  |
| 419 | Emphasis and strong emphasis | ✅ PASS |  |
| 420 | Emphasis and strong emphasis | ✅ PASS |  |
| 421 | Emphasis and strong emphasis | ✅ PASS |  |
| 422 | Emphasis and strong emphasis | ✅ PASS |  |
| 423 | Emphasis and strong emphasis | ✅ PASS |  |
| 424 | Emphasis and strong emphasis | ✅ PASS |  |
| 425 | Emphasis and strong emphasis | ✅ PASS |  |
| 426 | Emphasis and strong emphasis | ✅ PASS |  |
| 427 | Emphasis and strong emphasis | ✅ PASS |  |
| 428 | Emphasis and strong emphasis | ✅ PASS |  |
| 429 | Emphasis and strong emphasis | ✅ PASS |  |
| 430 | Emphasis and strong emphasis | ✅ PASS |  |
| 431 | Emphasis and strong emphasis | ✅ PASS |  |
| 432 | Emphasis and strong emphasis | ✅ PASS |  |
| 433 | Emphasis and strong emphasis | ✅ PASS |  |
| 434 | Emphasis and strong emphasis | ✅ PASS |  |
| 435 | Emphasis and strong emphasis | ✅ PASS |  |
| 436 | Emphasis and strong emphasis | ✅ PASS |  |
| 437 | Emphasis and strong emphasis | ✅ PASS |  |
| 438 | Emphasis and strong emphasis | ✅ PASS |  |
| 439 | Emphasis and strong emphasis | ✅ PASS |  |
| 440 | Emphasis and strong emphasis | ✅ PASS |  |
| 441 | Emphasis and strong emphasis | ✅ PASS |  |
| 442 | Emphasis and strong emphasis | ✅ PASS |  |
| 443 | Emphasis and strong emphasis | ✅ PASS |  |
| 444 | Emphasis and strong emphasis | ✅ PASS |  |
| 445 | Emphasis and strong emphasis | ✅ PASS |  |
| 446 | Emphasis and strong emphasis | ✅ PASS |  |
| 447 | Emphasis and strong emphasis | ✅ PASS |  |
| 448 | Emphasis and strong emphasis | ✅ PASS |  |
| 449 | Emphasis and strong emphasis | ✅ PASS |  |
| 450 | Emphasis and strong emphasis | ✅ PASS |  |
| 451 | Emphasis and strong emphasis | ✅ PASS |  |
| 452 | Emphasis and strong emphasis | ✅ PASS |  |
| 453 | Emphasis and strong emphasis | ✅ PASS |  |
| 454 | Emphasis and strong emphasis | ✅ PASS |  |
| 455 | Emphasis and strong emphasis | ✅ PASS |  |
| 456 | Emphasis and strong emphasis | ✅ PASS |  |
| 457 | Emphasis and strong emphasis | ✅ PASS |  |
| 458 | Emphasis and strong emphasis | ✅ PASS |  |
| 459 | Emphasis and strong emphasis | ✅ PASS |  |
| 460 | Emphasis and strong emphasis | ✅ PASS |  |
| 461 | Emphasis and strong emphasis | ✅ PASS |  |
| 462 | Emphasis and strong emphasis | ✅ PASS |  |
| 463 | Emphasis and strong emphasis | ✅ PASS |  |
| 464 | Emphasis and strong emphasis | ✅ PASS |  |
| 465 | Emphasis and strong emphasis | ✅ PASS |  |
| 466 | Emphasis and strong emphasis | ✅ PASS |  |
| 467 | Emphasis and strong emphasis | ✅ PASS |  |
| 468 | Emphasis and strong emphasis | ✅ PASS |  |
| 469 | Emphasis and strong emphasis | ✅ PASS |  |
| 470 | Emphasis and strong emphasis | ✅ PASS |  |
| 471 | Emphasis and strong emphasis | ✅ PASS |  |
| 472 | Emphasis and strong emphasis | ✅ PASS |  |
| 473 | Emphasis and strong emphasis | ✅ PASS |  |
| 474 | Emphasis and strong emphasis | ✅ PASS |  |
| 475 | Emphasis and strong emphasis | ✅ PASS |  |
| 476 | Emphasis and strong emphasis | ✅ PASS |  |
| 477 | Emphasis and strong emphasis | ✅ PASS |  |
| 478 | Emphasis and strong emphasis | ✅ PASS |  |
| 479 | Emphasis and strong emphasis | ✅ PASS |  |
| 480 | Emphasis and strong emphasis | ✅ PASS |  |
| 481 | Emphasis and strong emphasis | ✅ PASS |  |
| 482 | Links | ✅ PASS |  |
| 483 | Links | ✅ PASS |  |
| 484 | Links | ✅ PASS |  |
| 485 | Links | ✅ PASS |  |
| 486 | Links | ✅ PASS |  |
| 487 | Links | ✅ PASS |  |
| 488 | Links | ✅ PASS |  |
| 489 | Links | ✅ PASS |  |
| 490 | Links | ✅ PASS |  |
| 491 | Links | ✅ PASS |  |
| 492 | Links | ✅ PASS |  |
| 493 | Links | ✅ PASS |  |
| 494 | Links | ✅ PASS |  |
| 495 | Links | ✅ PASS |  |
| 496 | Links | ✅ PASS |  |
| 497 | Links | ✅ PASS |  |
| 498 | Links | ✅ PASS |  |
| 499 | Links | ✅ PASS |  |
| 500 | Links | ✅ PASS |  |
| 501 | Links | ✅ PASS |  |
| 502 | Links | ✅ PASS |  |
| 503 | Links | ✅ PASS |  |
| 504 | Links | ✅ PASS |  |
| 505 | Links | ✅ PASS |  |
| 506 | Links | ✅ PASS |  |
| 507 | Links | ✅ PASS |  |
| 508 | Links | ✅ PASS |  |
| 509 | Links | ✅ PASS |  |
| 510 | Links | ✅ PASS |  |
| 511 | Links | ✅ PASS |  |
| 512 | Links | ✅ PASS |  |
| 513 | Links | ✅ PASS |  |
| 514 | Links | ✅ PASS |  |
| 515 | Links | ✅ PASS |  |
| 516 | Links | ✅ PASS |  |
| 517 | Links | ✅ PASS |  |
| 518 | Links | ✅ PASS |  |
| 519 | Links | ✅ PASS |  |
| 520 | Links | ✅ PASS |  |
| 521 | Links | ✅ PASS |  |
| 522 | Links | ✅ PASS |  |
| 523 | Links | ✅ PASS |  |
| 524 | Links | ✅ PASS |  |
| 525 | Links | ✅ PASS |  |
| 526 | Links | ✅ PASS |  |
| 527 | Links | ✅ PASS |  |
| 528 | Links | ✅ PASS |  |
| 529 | Links | ✅ PASS |  |
| 530 | Links | ✅ PASS |  |
| 531 | Links | ✅ PASS |  |
| 532 | Links | ✅ PASS |  |
| 533 | Links | ✅ PASS |  |
| 534 | Links | ✅ PASS |  |
| 535 | Links | ✅ PASS |  |
| 536 | Links | ✅ PASS |  |
| 537 | Links | ✅ PASS |  |
| 538 | Links | ✅ PASS |  |
| 539 | Links | ✅ PASS |  |
| 540 | Links | ❌ FAIL | Expected length: 28, Actual length: 11 |
| 541 | Links | ✅ PASS |  |
| 542 | Links | ✅ PASS |  |
| 543 | Links | ✅ PASS |  |
| 544 | Links | ✅ PASS |  |
| 545 | Links | ✅ PASS |  |
| 546 | Links | ✅ PASS |  |
| 547 | Links | ✅ PASS |  |
| 548 | Links | ✅ PASS |  |
| 549 | Links | ✅ PASS |  |
| 550 | Links | ✅ PASS |  |
| 551 | Links | ✅ PASS |  |
| 552 | Links | ✅ PASS |  |
| 553 | Links | ✅ PASS |  |
| 554 | Links | ✅ PASS |  |
| 555 | Links | ✅ PASS |  |
| 556 | Links | ✅ PASS |  |
| 557 | Links | ✅ PASS |  |
| 558 | Links | ✅ PASS |  |
| 559 | Links | ✅ PASS |  |
| 560 | Links | ✅ PASS |  |
| 561 | Links | ✅ PASS |  |
| 562 | Links | ✅ PASS |  |
| 563 | Links | ✅ PASS |  |
| 564 | Links | ✅ PASS |  |
| 565 | Links | ✅ PASS |  |
| 566 | Links | ✅ PASS |  |
| 567 | Links | ✅ PASS |  |
| 568 | Links | ✅ PASS |  |
| 569 | Links | ✅ PASS |  |
| 570 | Links | ✅ PASS |  |
| 571 | Links | ✅ PASS |  |
| 572 | Images | ✅ PASS |  |
| 573 | Images | ✅ PASS |  |
| 574 | Images | ✅ PASS |  |
| 575 | Images | ✅ PASS |  |
| 576 | Images | ✅ PASS |  |
| 577 | Images | ✅ PASS |  |
| 578 | Images | ✅ PASS |  |
| 579 | Images | ✅ PASS |  |
| 580 | Images | ✅ PASS |  |
| 581 | Images | ✅ PASS |  |
| 582 | Images | ✅ PASS |  |
| 583 | Images | ✅ PASS |  |
| 584 | Images | ✅ PASS |  |
| 585 | Images | ✅ PASS |  |
| 586 | Images | ✅ PASS |  |
| 587 | Images | ✅ PASS |  |
| 588 | Images | ✅ PASS |  |
| 589 | Images | ✅ PASS |  |
| 590 | Images | ✅ PASS |  |
| 591 | Images | ✅ PASS |  |
| 592 | Images | ✅ PASS |  |
| 593 | Images | ✅ PASS |  |
| 594 | Autolinks | ✅ PASS |  |
| 595 | Autolinks | ✅ PASS |  |
| 596 | Autolinks | ✅ PASS |  |
| 597 | Autolinks | ✅ PASS |  |
| 598 | Autolinks | ✅ PASS |  |
| 599 | Autolinks | ✅ PASS |  |
| 600 | Autolinks | ✅ PASS |  |
| 601 | Autolinks | ✅ PASS |  |
| 602 | Autolinks | ✅ PASS |  |
| 603 | Autolinks | ✅ PASS |  |
| 604 | Autolinks | ✅ PASS |  |
| 605 | Autolinks | ✅ PASS |  |
| 606 | Autolinks | ✅ PASS |  |
| 607 | Autolinks | ✅ PASS |  |
| 608 | Autolinks | ✅ PASS |  |
| 609 | Autolinks | ✅ PASS |  |
| 610 | Autolinks | ✅ PASS |  |
| 611 | Autolinks | ✅ PASS |  |
| 612 | Autolinks | ✅ PASS |  |
| 613 | Raw HTML | ✅ PASS |  |
| 614 | Raw HTML | ✅ PASS |  |
| 615 | Raw HTML | ✅ PASS |  |
| 616 | Raw HTML | ✅ PASS |  |
| 617 | Raw HTML | ✅ PASS |  |
| 618 | Raw HTML | ✅ PASS |  |
| 619 | Raw HTML | ✅ PASS |  |
| 620 | Raw HTML | ✅ PASS |  |
| 621 | Raw HTML | ✅ PASS |  |
| 622 | Raw HTML | ✅ PASS |  |
| 623 | Raw HTML | ✅ PASS |  |
| 624 | Raw HTML | ✅ PASS |  |
| 625 | Raw HTML | ✅ PASS |  |
| 626 | Raw HTML | ✅ PASS |  |
| 627 | Raw HTML | ✅ PASS |  |
| 628 | Raw HTML | ✅ PASS |  |
| 629 | Raw HTML | ✅ PASS |  |
| 630 | Raw HTML | ✅ PASS |  |
| 631 | Raw HTML | ✅ PASS |  |
| 632 | Raw HTML | ✅ PASS |  |
| 633 | Hard line breaks | ✅ PASS |  |
| 634 | Hard line breaks | ✅ PASS |  |
| 635 | Hard line breaks | ✅ PASS |  |
| 636 | Hard line breaks | ✅ PASS |  |
| 637 | Hard line breaks | ✅ PASS |  |
| 638 | Hard line breaks | ✅ PASS |  |
| 639 | Hard line breaks | ✅ PASS |  |
| 640 | Hard line breaks | ✅ PASS |  |
| 641 | Hard line breaks | ✅ PASS |  |
| 642 | Hard line breaks | ✅ PASS |  |
| 643 | Hard line breaks | ✅ PASS |  |
| 644 | Hard line breaks | ✅ PASS |  |
| 645 | Hard line breaks | ✅ PASS |  |
| 646 | Hard line breaks | ✅ PASS |  |
| 647 | Hard line breaks | ✅ PASS |  |
| 648 | Soft line breaks | ✅ PASS |  |
| 649 | Soft line breaks | ✅ PASS |  |
| 650 | Textual content | ✅ PASS |  |
| 651 | Textual content | ✅ PASS |  |
| 652 | Textual content | ✅ PASS |  |
