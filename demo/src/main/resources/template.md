# Markdown Stream Render Feature Showcase

This document demonstrates the supported Markdown features, including CommonMark standards, GitHub Flavored Markdown (GFM) extensions, and **Custom Plugins**.

## 1. Text Formatting

You can make text **bold**, *italic*, or ***both***.
GFM adds support for ~~strikethrough~~ text.

Inline code is wrapped in backticks: `System.out.println("Hello");`.
You can also escape special characters: \*not bold\*.

## 2. Structure

### Headers (H1-H6)
#### Heading 4
##### Heading 5
###### Heading 6

### Blockquotes
> Blockquotes can hold other elements.
>
> > And they can be nested.
> >
> > - Even with lists inside!
> > - Or code blocks:
> >   ```
> >   nested code
> >   ```

### Horizontal Rules
Three or more hyphens, asterisks, or underscores:

---

## 3. Lists

### Unordered List
* Item 1
* Item 2
  * Nested Item 2.1
  * Nested Item 2.2
    + Deeply nested (using `+`)
    - Deeply nested (using `-`)

### Ordered List
1. First step
2. Second step
   1. Sub-step A
   2. Sub-step B
      1. Deep nesting
3. Third step (numbering continues)

### Task List (GFM)
- [ ] Pending task
- [x] Completed task
- [ ] Nested tasks
  - [x] Nested done
  - [ ] Nested pending

### Mixed Nesting
1. Ordered Item
   - Unordered Subitem
     > Blockquote inside list
     >
     > ```java
     > // Code inside blockquote inside list
     > ```

## 4. Links and Images

### Links

[ref]: https://google.com

*   [Standard Link](https://google.com)
*   [Reference Link][ref]
*   [Link with Title](https://example.com "Hover me!")

### Autolinks
*   Standard: <http://example.com>
*   Email: <user@example.com>
*   GFM Extended: https://www.github.com

### Images
![Markdown Logo](https://markdown-here.com/img/icon256.png "Markdown Logo")

## 5. Tables (GFM)

Supports alignment (`:---`, `:---:`, `---:`) and inline formatting.

| Feature | Support | Notes |
| :--- | :---: | ---: |
| Tables | ✅ | GFM Syntax |
| Alignment | ✅ | Left/Center/Right |
| Inline | `code` | Works inside |
| Empty | | |
| Escaped | \| | Pipe char |

## 6. Code Blocks

### Fenced Code Blocks (with Syntax Highlighting hints)

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, Markdown!");
    }
}
```

```json
{
  "name": "markdown-stream-render",
  "version": "1.0.0",
  "features": ["streaming", "javafx", "plugins"]
}
```

### Indented Code Blocks
    // This is an indented code block
    // (4 spaces indentation)
    var x = 10;

## 7. Custom Plugins (Demo Features)

### Admonition Blocks
(Requires `AdmonitionParser` and `AdmonitionJavaFxRenderer`)

!!! info "Did you know?"
    Markdown Stream Render supports **custom blocks**!
    This box is rendered by a custom plugin.

!!! warning "Caution"
    Plugins are powerful mechanisms to extend Markdown syntax.

!!! error "Critical"
    Do not forget to register the parser factories!

### Inline Math
(Requires `MathParser` and `MathJavaFxRenderer`)

We can parse inline math: $E=mc^2$.
Complex equations: $x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}$ (Visual rendering depends on JavaFX support).

## 8. HTML Integration

Raw HTML is supported (rendering depends on platform security settings):

<div style="padding: 10px; background-color: #f0f0f0; border: 1px solid #ccc;">
  <b>Bold HTML</b> and <i>Italic HTML</i> inside a div.
</div>

## 9. Edge Cases

*   Unclosed *emphasis
*   Unclosed `code block
* Long line: This is a very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very very very very very very very very very very very very very very
  very very very very very very very very very very long line.

## 10. New Regression Tests (Fix Verification)

### Table with Complex Escaped Pipes (Example 200)

| Cell 1    | Cell 2 |
|:----------|:-------|
| b `\|` az | [      | ] |

### Table Interruption (Example 201/202)

| Col 1 | Col 2 |
|-------|-------|
| Row 1 | Data  |

> This blockquote should break the table (not empty).

| Col 1 | Col 2 |
|-------|-------|
| Row 1 | Data  |

This paragraph should also break the table.

### Task List with HTML Compliance

- [ ] Unchecked
- [x] Checked <script>alert('xss')</script> (HTML should be escaped)

### Empty Table Body (Example 205)

| Header 1 | Header 2 |
|----------|----------|

(Above table should render header but no body)

### Table Row without Pipes (Example 202)

| Header 1 | Header 2 |
|----------|----------|

Row without pipes
