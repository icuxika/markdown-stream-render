package com.icuxika.markdown.stream.render.core;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

/**
 * 表格功能测试类 (Table Tests)
 * 测试 GFM 风格的表格解析与渲染，包括表头、对齐方式等。
 */
public class TableTest {

    /**
     * 测试基本表格结构
     */
    @Test
    public void testBasicTable() throws IOException {
        String markdown = """
                | Header 1 | Header 2 |
                | --- | --- |
                | Cell 1 | Cell 2 |
                """;

        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer();
        parser.parse(new StringReader(markdown), renderer);

        String html = (String) renderer.getResult();
        System.out.println(html);

        // Expected output should contain table tags
        assert html.contains("<table>");
        assert html.contains("<thead>");
        assert html.contains("<tbody>");
        assert html.contains("<th>Header 1</th>");
        assert html.contains("<td>Cell 1</td>");
    }

    /**
     * 测试表格对齐方式 (左对齐、居中、右对齐)
     */
    @Test
    public void testTableWithAlignment() throws IOException {
        String markdown = """
                | Left | Center | Right |
                | :--- | :---: | ---: |
                | L    | C     | R    |
                """;

        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer();
        parser.parse(new StringReader(markdown), renderer);

        String html = (String) renderer.getResult();
        System.out.println(html);

        assert html.contains("<th align=\"left\">Left</th>");
        assert html.contains("<th align=\"center\">Center</th>");
        assert html.contains("<th align=\"right\">Right</th>");
    }
}
