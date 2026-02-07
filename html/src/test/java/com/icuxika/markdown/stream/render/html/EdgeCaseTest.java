package com.icuxika.markdown.stream.render.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Edge Case Tests to increase coverage and verify robustness.
 */
public class EdgeCaseTest {

    // --- 1. Table Edge Cases ---

    @Test
    public void testTableWithUnevenRows() {
        String input = "| Header 1 | Header 2 |\n| --- | --- |\n| Cell 1 |\n| Cell 3 | Cell 4 | Cell 5 |";
        String html = render(input);
        // Ensure parsing is robust and renders what it can
        assertTrue(html.contains("<table>"), "Should parse as table");
        assertTrue(html.contains("<td>Cell 1</td>"), "Should contain Cell 1");
        // Extra cells might be ignored or handled depending on implementation
        assertTrue(html.contains("<td>Cell 3</td>"), "Should contain Cell 3");
    }

    @Test
    public void testTableWithEscapedPipes() {
        String input = "| Header | |\n| --- | --- |\n| Cell with \\| pipe | Next |";
        String html = render(input);
        assertTrue(html.contains("Cell with | pipe"), "Should unescape pipe");
    }

    @Test
    public void testTableWithEmptyHeaderCells() {
        String input = "| | Header 2 |\n| --- | --- |\n| C1 | C2 |";
        String html = render(input);
        assertTrue(html.contains("<th></th>"), "Should handle empty header cell");
        assertTrue(html.contains("<th>Header 2</th>"), "Should handle non-empty header cell");
    }

    // --- 2. Unclosed Blocks ---

    @Test
    public void testUnclosedFencedCodeBlock() {
        String input = "```java\nSystem.out.println(\"Hello\");"; // EOF without closing fence
        String html = render(input);
        assertTrue(html.contains("<pre><code class=\"language-java\">"), "Should start code block");
        assertTrue(html.contains("System.out.println"), "Should contain content");
        // Should implicitly close at EOF
    }

    @Test
    public void testUnclosedBlockQuote() {
        String input = "> Quote text\n> More quote"; // EOF
        String html = render(input);
        assertTrue(html.contains("<blockquote>"), "Should render blockquote");
        assertTrue(html.contains("<p>Quote text\nMore quote</p>"), "Should contain merged content");
    }

    // --- 3. Nesting Edge Cases ---

    @Test
    public void testDeeplyNestedLists() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("  ".repeat(i)).append("- Level ").append(i).append("\n");
        }
        String html = render(sb.toString());
        // Just verify it doesn't crash and contains deep level
        assertTrue(html.contains("Level 19"), "Should parse deep nesting");
    }

    @Test
    public void testCodeBlockInsideBlockQuoteInsideList() {
        String input = "- Item\n  > Quote\n  > ```\n  > Code\n  > ```";
        String html = render(input);
        assertTrue(html.contains("<li>"), "Should have list item");
        assertTrue(html.contains("<blockquote>"), "Should have blockquote");
        assertTrue(html.contains("<pre><code>Code"), "Should have code block");
    }

    // --- 4. Input Boundary Conditions ---

    @Test
    public void testEmptyInput() {
        String html = render("");
        assertEquals("", html.trim(), "Empty input should produce empty output");
    }

    @Test
    public void testOnlyWhitespace() {
        String html = render("   \n   ");
        assertEquals("", html.trim(), "Whitespace input should produce empty output");
    }

    @Test
    public void testNullInput() {
        String html = render(null);
        assertEquals("", html.trim(), "Null input should produce empty output");
    }

    // --- 5. Inline Edge Cases ---

    @Test
    public void testUnmatchedEmphasis() {
        String input = "*foo *bar";
        String html = render(input);
        // *foo is NOT a list item because no space after *.
        // *foo *bar -> neither * matches a closing *, so literal text.
        assertTrue(html.contains("<p>*foo *bar</p>"), "Should parse as literal text in paragraph");

        input = "Text *foo *bar";
        html = render(input);
        assertTrue(html.contains("Text *foo *bar"), "Should not parse unmatched emphasis");
    }

    @Test
    public void testComplexBackticks() {
        String input = "`` ` ``";
        String html = render(input);
        assertTrue(html.contains("<code>`</code>"), "Should handle escaped backtick logic");
    }

    // --- 6. Task List Edge Cases ---

    @Test
    public void testMixedTaskAndBulletList() {
        String input = "- [ ] Task 1\n- Normal Item\n- [x] Task 2";
        String html = render(input);
        assertTrue(html.contains("type=\"checkbox\""), "Should have checkbox");
        assertTrue(html.contains("Task 1"), "Should have Task 1");
        assertTrue(html.contains("Normal Item"), "Should have Normal Item");
    }

    @Test
    public void testMalformedTaskBox() {
        String input = "- [v] Invalid\n- [ ]Valid"; // Missing space in second one?
        // "- [ ]Valid" -> is valid task list item? CommonMark spec says space required
        // after ]?
        // GFM says space required.
        String html = render(input);
        assertTrue(html.contains("[v] Invalid"), "Should treat invalid box as text");
    }

    // --- Helper ---
    private String render(String input) {
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        Node document = parser.parse(input);
        document.accept(renderer);
        return (String) renderer.getResult();
    }
}
