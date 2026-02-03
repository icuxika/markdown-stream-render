package com.icuxika.markdown.stream.render.core;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 回归测试类 (Regression Tests)
 * 用于记录和验证曾经出现的 bug 或特定的边缘情况，确保后续修改不会破坏这些行为。
 * 包含之前 ReproTest, DebugCodeBlockTest, DebugListTest 的内容。
 */
public class RegressionTest {

    /**
     * 测试用例 1: 简单的列表项
     */
    @Test
    public void testSimpleListItem() {
        String input = "- \n  foo";
        // 预期行为可能取决于规范细节，这里主要确保不崩溃且结构合理
        String actual = render(input);
        System.out.println("Case 1 Actual:\n" + actual);
    }

    /**
     * 测试用例 2: 紧凑列表与段落
     */
    @Test
    public void testTightListAndParagraph() {
        String input = "-\nfoo";
        String actual = render(input);
        System.out.println("Case 2 Actual:\n" + actual);
    }

    /**
     * 测试用例 3: 嵌套列表与 Setext 标题 (Example 300)
     */
    @Test
    public void testNestedListAndSetextHeading() {
        // Example 300
        String input = "- # Foo\n- Bar\n  ---\n  baz";
        String expected = "<ul>\n<li>\n<h1>Foo</h1>\n</li>\n<li>\n<h2>Bar</h2>\nbaz</li>\n</ul>\n";
        String actual = render(input);
        assertEquals(expected.trim(), actual.trim());
    }

    /**
     * 测试用例 4: 空列表项 (Example 278)
     */
    @Test
    public void testEmptyListItem() {
        // Example 278
        String input = "-\n  foo";
        String expected = "<ul>\n<li>foo</li>\n</ul>\n";
        String actual = render(input);
        assertEquals(expected.trim(), actual.trim());
    }

    /**
     * 测试用例 6: 列表项内的 Setext 标题
     */
    @Test
    public void testListItemSetextHeading() {
        String input = "- Bar\n  ---";
        String expected = "<ul>\n<li>\n<h1>Bar</h1>\n</li>\n</ul>\n";
        String actual = render(input);
        // assertEquals(expected.trim(), actual.trim());
        System.out.println("Case 6 Actual:\n" + actual);
    }

    /**
     * 来自 DebugCodeBlockTest: 缩进代码块的基本测试
     */
    @Test
    public void testIndentedCodeBlock() {
        String input = "\n\n    foo\n\n";
        String actual = render(input);
        System.out.println("CodeBlock Result: [" + actual + "]");
        // 验证结果包含 pre code
        assert actual.contains("<pre><code>foo");
    }

    /**
     * 来自 DebugListTest: 深度缩进的代码块在列表中
     */
    @Test
    public void testListWithDeeplyIndentedCode() {
        String input = "- foo\n\n      bar";
        String actual = render(input);
        System.out.println("DeepList Result: [" + actual + "]");
        // 验证 bar 被解析为代码块 (4空格缩进相对于列表内容)
        // 列表项内容缩进通常是 2 空格，所以 6 空格 = 2 (marker) + 4 (code indent)
        assert actual.contains("<pre><code>bar");
    }

    private String render(String input) {
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer();
        Node document = parser.parse(input);
        document.accept(renderer);
        return (String) renderer.getResult();
    }
}
