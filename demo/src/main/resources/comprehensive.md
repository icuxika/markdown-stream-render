# Markdown Stream Render 综合功能演示

本文档旨在全面展示 `markdown-stream-render` 库的渲染能力，包括 **CommonMark 标准**、**GFM 扩展**、**自定义扩展**以及**交互特性**。

---

## 1. 基础排版 (Typography)

### 1.1 标题 (Headings)
# Heading 1
## Heading 2
### Heading 3
#### Heading 4
##### Heading 5
###### Heading 6

### 1.2 文本样式 (Styles)
- **粗体 (Bold)**: 使用 `**` 或 `__`
- *斜体 (Italic)*: 使用 `*` 或 `_`
- ***粗斜体 (Bold & Italic)***: 组合使用
- ~~删除线 (Strikethrough)~~: GFM 特性
- `行内代码 (Inline Code)`: 使用反引号
- [超链接 (Link)](https://github.com/icuxika/markdown-stream-render): 支持点击跳转（需配置 `HostServices`）
- 自动链接: https://www.google.com

---

## 2. 列表与结构 (Structure)

### 2.1 无序列表 (Unordered List)
* 项目 A
* 项目 B
  * 子项目 B.1
  * 子项目 B.2
    * 深层嵌套

### 2.2 有序列表 (Ordered List)
1. 第一步
2. 第二步
   1. 详细步骤 A
   2. 详细步骤 B
3. 第三步

### 2.3 任务列表 (Task List)
- [ ] 待办任务 (Pending)
- [x] 已完成任务 (Completed)
- [ ] **重要**任务 (支持富文本)
- [ ] 嵌套任务
  - [x] 子任务已完成

### 2.4 引用块 (Blockquotes)
> Markdown 是一种轻量级标记语言。
>
> > 它允许人们使用易读易写的纯文本格式编写文档。
> > - 支持嵌套
> > - 支持包含其他块元素

---

## 3. 表格 (Tables)

支持 GFM 表格语法，包括对齐方式和单元格内的内联样式。

| 姓名 | 年龄 | 角色 | 状态 |
| :--- | :--: | :--- | ---: |
| **Alice** | 24 | `Admin` | 🟢 在线 |
| Bob | 30 | Developer | 🔴 离线 |
| Charlie | 28 | Designer | 🟡 忙碌 |
| Dave | 35 | Manager | 🔵 会议中 |

---

## 4. 代码高亮 (Syntax Highlighting)

支持多种语言的语法高亮，并提供**复制按钮**交互。

### 4.1 Java
```java
package com.example;

import java.util.List;

public class StreamDemo {
    public static void main(String[] args) {
        // 这是一个注释
        String message = "Hello, Markdown!";
        System.out.println(message);
        
        List<String> items = List.of("A", "B", "C");
        items.forEach(System.out::println);
    }
}
```

### 4.2 JSON
```json
{
  "project": "markdown-stream-render",
  "version": "1.0.0-SNAPSHOT",
  "features": {
    "streaming": true,
    "javafx": true,
    "syntax_highlight": ["java", "json", "xml", "sql"]
  },
  "score": 99.9
}
```

### 4.3 XML / HTML
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.icuxika</groupId>
    <artifactId>demo</artifactId>
    <!-- Dependencies -->
    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### 4.4 SQL
```sql
SELECT id, username, email 
FROM users 
WHERE status = 'ACTIVE' 
  AND created_at > '2023-01-01'
ORDER BY created_at DESC 
LIMIT 10;
```

### 4.5 Bash / Shell
```bash
# Clone the repository
git clone https://github.com/icuxika/markdown-stream-render.git

# Build the project
cd markdown-stream-render
mvn clean install -DskipTests

# Run the demo
mvn -pl demo exec:java
```

---

## 5. 图像 (Images)

支持远程图片加载，并提供加载失败时的占位符处理。

### 5.1 正常图片
![Markdown Logo](https://markdown-here.com/img/icon256.png)

### 5.2 加载失败演示 (Broken Image)
![不存在的图片](https://example.com/non-existent-image.png)

---

## 6. 扩展功能 (Extensions)

### 6.1 提示块 (Admonitions)
支持 `info`, `warning`, `error` 等类型的提示块。

!!! info "信息提示"
    这是一个 **Info** 块。用于提供额外的上下文信息或提示。
    支持多行内容。

!!! warning "注意事项"
    这是一个 **Warning** 块。请注意潜在的副作用或风险。

!!! error "严重错误"
    这是一个 **Error** 块。表示操作失败或禁止的操作。

### 6.2 数学公式 (Math)
支持行内数学公式渲染（需配置 Math 扩展）。

- 质能方程: $E = mc^2$
- 欧拉公式: $e^{i\pi} + 1 = 0$
- 勾股定理: $a^2 + b^2 = c^2$

---

## 7. 分割线 (Horizontal Rules)

---
***
___

## 8. HTML 嵌入 (HTML Integration)

在允许的情况下，支持直接嵌入 HTML 标签。

<div style="padding: 15px; background-color: #f0f4f8; border-left: 5px solid #005cc5; border-radius: 4px;">
    <strong>这是一个原生 HTML div 元素。</strong><br>
    它包含 <em>HTML 标签</em> 并且可以自定义样式。
</div>

---

## 9. 极限情况测试 (Edge Cases)

- **超长行**: 
  This is a very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very long line to test wrapping.

- **混合嵌套**:
  > 引用块中包含列表
  > 1. 有序列表项
  >    - 无序子项
  >      ```java
  >      // 代码块在引用块的列表项中
  >      ```

---

**End of Document**
