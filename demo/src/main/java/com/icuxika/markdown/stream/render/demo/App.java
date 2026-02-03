package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;

public class App {

    public static void main(String[] args) throws java.io.IOException {
        System.out.println("Markdown Stream Renderer Demo");
        System.out.println("=============================");

        String markdown = """
                # Markdown Stream Renderer Demo
                
                This demo showcases the **MarkdownParser** and **HtmlRenderer**.
                
                ## Supported Features
                
                1. **Block Elements**:
                   - Headers (ATX)
                   - Paragraphs
                   - Lists (Ordered and Bullet)
                   - Block Quotes
                   - Fenced Code Blocks
                   - HTML Blocks
                
                2. **Inline Elements**:
                   - *Emphasis* and **Strong Emphasis**
                   - `Code Spans`
                   - [Links](https://commonmark.org)
                   - Images: ![Alt Text](https://via.placeholder.com/150)
                   - Autolinks: <https://github.com>
                   - Raw HTML: <span>Inline HTML</span>
                   - Hard line breaks (end with spaces)  
                     and Soft line breaks
                
                ## Code Example
                
                ```java
                public static void main(String[] args) {
                    System.out.println("Hello World!");
                }
                ```
                
                ## Quote
                
                > "Simplicity is the ultimate sophistication."
                > - Leonardo da Vinci
                """;

        System.out.println("Input Markdown:");
        System.out.println(markdown);
        System.out.println("-----------------------------");

        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer();

        // Parse and Render
        parser.parse(new java.io.StringReader(markdown), renderer);
        String html = (String) renderer.getResult();

        System.out.println("Output HTML:");
        System.out.println(html);
    }
}
