package com.icuxika.core;

import com.icuxika.core.ast.Node;
import com.icuxika.core.parser.MarkdownParser;
import com.icuxika.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

public class DebugListTest {
    @Test
    public void testListWithIndentedCode6Spaces() {
        String input = "- foo\n\n      bar";
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer();
        Node document = parser.parse(input);
        document.accept(renderer);
        System.out.println("Result 6spaces: [" + renderer.getResult() + "]");
    }
}
