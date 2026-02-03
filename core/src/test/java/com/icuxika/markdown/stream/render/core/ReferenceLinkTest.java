package com.icuxika.markdown.stream.render.core;

import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.ast.LinkReference;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReferenceLinkTest {

    @Test
    public void testReferenceParsing() throws java.io.IOException {
        String input = "[foo]: /url \"title\"\n\n[foo]";
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer();
        parser.parse(new java.io.StringReader(input), renderer);
        
        String result = (String) renderer.getResult();
        System.out.println("Result: " + result);
        
        assertEquals("<p><a href=\"/url\" title=\"title\">foo</a></p>\n", result);
    }

    @Test
    public void testReferenceMap() {
        String input = "[foo]: /url \"title\"";
        MarkdownParser parser = new MarkdownParser();
        Document doc = parser.parse(input);
        
        LinkReference ref = doc.getLinkReference("foo");
        assertNotNull(ref, "Reference 'foo' should be parsed");
        assertEquals("/url", ref.getDestination());
        assertEquals("title", ref.getTitle());
    }
    
    @Test
    public void testImageReference() throws java.io.IOException {
        String input = "![foo][bar]\n\n[bar]: /image.jpg";
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer();
        parser.parse(new java.io.StringReader(input), renderer);
        
        String result = (String) renderer.getResult();
        System.out.println("Result: " + result);
        
        assertEquals("<p><img src=\"/image.jpg\" alt=\"foo\" /></p>\n", result);
    }
}
