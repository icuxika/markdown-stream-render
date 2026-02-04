# Markdown Stream Render Feature Showcase

This document demonstrates the supported Markdown features, including CommonMark standards and GitHub Flavored Markdown (GFM) extensions.

## 1. Text Formatting

You can make text **bold**, *italic*, or ***both***.
GFM adds support for ~~strikethrough~~ text.

Inline code is wrapped in backticks: `System.out.println("Hello");`.

You can also escape special characters: \*not bold\*.

## 2. Structure

### Headers
(H1-H6 are supported)
#### Heading 4
##### Heading 5
###### Heading 6

### Blockquotes
> Blockquotes can hold other elements.
>
> > And they can be nested.

### Horizontal Rules
Three or more hyphens, asterisks, or underscores:

---

## 3. Lists

### Unordered List
* Item 1
* Item 2
  * Nested Item 2.1
  * Nested Item 2.2

### Ordered List
1. First step
2. Second step
   1. Sub-step A
   2. Sub-step B

### Task List (GFM)
- [ ] Pending task
- [x] Completed task
- [ ] Nested tasks
  - [x] Nested done
  - [ ] Nested pending

## 4. Links and Images

### Standard Links
[Google](https://google.com) or [Reference Link][ref]

[ref]: https://google.com

### Autolinks (Standard)
<http://example.com>
<user@example.com>

### Extended Autolinks (GFM)
Directly recognized URLs: https://www.github.com
Directly recognized www: www.stackoverflow.com
Directly recognized email: support@github.com

### Images
![Markdown Logo](https://markdown-here.com/img/icon256.png "Markdown Logo")

## 5. Tables (GFM)

Supports alignment and inline formatting.

| Feature | Support | Notes |
| :--- | :---: | ---: |
| Tables | ✅ | GFM Syntax |
| Alignment | ✅ | Left/Center/Right |
| Inline | `code` | Works inside |

## 6. Code Blocks

### Fenced Code Blocks
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
  "version": "1.0.0"
}
```

## 7. HTML

Raw HTML is supported (rendering depends on platform):

<b>Bold HTML</b> and <i>Italic HTML</i>
