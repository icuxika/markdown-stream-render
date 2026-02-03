package com.icuxika.markdown.stream.render.core;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReproTest {

    @Test
    public void testCase1() {
        String input = "- \n  foo";
        String expected = "<ul>\n<li>foo</li>\n</ul>\n";
        String actual = render(input);
        System.out.println("Case 1 Actual:\n" + actual);
        // assertEquals(expected.trim(), actual.trim());
    }

    @Test
    public void testCase2() {
        String input = "-\nfoo";
        String expected = "<ul>\n<li></li>\n</ul>\n<p>foo</p>\n";
        String actual = render(input);
        System.out.println("Case 2 Actual:\n" + actual);
        // assertEquals(expected.trim(), actual.trim());
    }

    @Test
    public void testCase3() {
        // Example 300
        String input = "- # Foo\n- Bar\n  ---\n  baz";
        String expected = "<ul>\n<li>\n<h1>Foo</h1>\n</li>\n<li>\n<h2>Bar</h2>\nbaz</li>\n</ul>\n";
        String actual = render(input);
        System.out.println("Case 3 Actual:\n" + actual);
        assertEquals(expected.trim(), actual.trim());
    }

    @Test
    public void testCase4() {
        // Example 278
        String input = "-\n  foo";
        String expected = "<ul>\n<li>foo</li>\n</ul>\n";
        String actual = render(input);
        System.out.println("Case 4 Actual:\n" + actual);
        assertEquals(expected.trim(), actual.trim());
    }

    @Test
    public void testCase6_Example300() {
        String input = "- Bar\n  ---";
        String expected = "<ul>\n<li>\n<h1>Bar</h1>\n</li>\n</ul>\n";
        String actual = render(input);
        System.out.println("Case 6 Actual:\n" + actual);
        // assertEquals(expected.trim(), actual.trim());
    }

    private String render(String input) {
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer();
        renderer.visit(parser.parse(input));
        return (String) renderer.getResult();
    }
}
