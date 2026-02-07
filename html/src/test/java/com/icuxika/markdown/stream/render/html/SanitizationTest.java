package com.icuxika.markdown.stream.render.html;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParserOptions;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

public class SanitizationTest {

    @Test
    public void testSafeModeHtmlBlock() throws java.io.IOException {
        String markdown = "<div>\nfoo\n</div>";
        String expected = "<!-- Raw HTML Omitted -->\n";

        MarkdownParserOptions options = new MarkdownParserOptions();
        options.setSafeMode(true);
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer(options);
        parser.parse(new java.io.StringReader(markdown), renderer);
        String actual = (String) renderer.getResult();

        assertEquals(expected, actual.replace("\r\n", "\n"));
    }

    @Test
    public void testSafeModeHtmlInline() throws java.io.IOException {
        String markdown = "foo <br> bar";
        String expected = "<p>foo <!-- Raw HTML Omitted --> bar</p>\n";

        MarkdownParserOptions options = new MarkdownParserOptions();
        options.setSafeMode(true);
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer(options);
        parser.parse(new java.io.StringReader(markdown), renderer);
        String actual = (String) renderer.getResult();

        assertEquals(expected, actual.replace("\r\n", "\n"));
    }

    @Test
    public void testUnsafeModeHtmlBlock() throws java.io.IOException {
        String markdown = "<div>\nfoo\n</div>";
        String expected = "<div>\nfoo\n</div>\n"; // Block parser includes newline in literal? Yes.

        MarkdownParserOptions options = new MarkdownParserOptions();
        options.setSafeMode(false);
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer(options);
        parser.parse(new java.io.StringReader(markdown), renderer);
        String actual = (String) renderer.getResult();

        assertEquals(expected, actual.replace("\r\n", "\n"));
    }
}
