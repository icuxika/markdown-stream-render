package com.icuxika.core.parser;

import com.icuxika.core.ast.Document;
import com.icuxika.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DebugReferenceLinkTest {

    @Test
    public void testReferenceLink() {
        String input = "[foo]: /url \"title\"\n\n[foo]";
        MarkdownParser parser = new MarkdownParser();
        Document doc = parser.parse(input);
        HtmlRenderer renderer = new HtmlRenderer();
        doc.accept(renderer);
        String output = (String) renderer.getResult();
        
        System.out.println("Output: " + output);
        assertEquals("<p><a href=\"/url\" title=\"title\">foo</a></p>\n", output);
    }

    @Test
    public void testReferenceLinkNoBlankLine() {
        String input = "[foo]: /url \"title\"\n[foo]";
        MarkdownParser parser = new MarkdownParser();
        Document doc = parser.parse(input);
        HtmlRenderer renderer = new HtmlRenderer();
        doc.accept(renderer);
        String output = (String) renderer.getResult();
        
        System.out.println("Output NoBlank: " + output);
        assertEquals("<p><a href=\"/url\" title=\"title\">foo</a></p>\n", output);
    }
}
