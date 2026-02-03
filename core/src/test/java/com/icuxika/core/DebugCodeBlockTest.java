package com.icuxika.core;

import com.icuxika.core.parser.MarkdownParser;
import com.icuxika.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

public class DebugCodeBlockTest {
    @Test
    public void testExample117() {
        String input = "\n\n    foo\n\n";
        MarkdownParser parser = new MarkdownParser();
        HtmlRenderer renderer = new HtmlRenderer();
        parser.parse(input).accept(renderer);
        System.out.println("Result: [" + renderer.getResult() + "]");
    }
}
