package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.CoreExtension;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.IStreamMarkdownRenderer;

public class StreamParserDebug {

    public static void main(String[] args) {
        String markdown = "- Item 1\n" +
                "- Item 2\n" +
                "- Item 3\n";

        IStreamMarkdownRenderer renderer = new IStreamMarkdownRenderer() {
            @Override
            public void renderNode(Node node) {
                // System.out.println("RENDER: " + node.getClass().getSimpleName());
            }

            @Override
            public void openBlock(Node node) {
                System.out.println("OPEN: " + node.getClass().getSimpleName());
            }

            @Override
            public void closeBlock(Node node) {
                System.out.println("CLOSE: " + node.getClass().getSimpleName());
            }
        };

        StreamMarkdownParser parser = StreamMarkdownParser.builder().renderer(renderer).build();
        CoreExtension.addDefaults(StreamMarkdownParser.builder().renderer(renderer)); // This line does nothing to
                                                                                      // parser instance!

        // Correct way to add defaults
        StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(renderer);
        CoreExtension.addDefaults(builder);
        parser = builder.build();

        System.out.println("--- Start Parsing ---");
        parser.push(markdown);
        parser.close();
        System.out.println("--- End Parsing ---");
    }
}
