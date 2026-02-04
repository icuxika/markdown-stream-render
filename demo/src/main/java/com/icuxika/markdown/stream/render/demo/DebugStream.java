package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.IStreamMarkdownRenderer;

public class DebugStream {
    public static void main(String[] args) {
        StreamMarkdownParser parser = StreamMarkdownParser.builder()
                .renderer(new IStreamMarkdownRenderer() {
                    @Override
                    public void renderNode(Node node) {
                        System.out.println("Finalized Node: " + node.getClass().getSimpleName());
                        Node parent = node.getParent();
                        if (parent != null) {
                            System.out.println("  Parent: " + parent.getClass().getSimpleName());
                            Node grandParent = parent.getParent();
                            if (grandParent != null) {
                                System.out.println("    GrandParent: " + grandParent.getClass().getSimpleName());
                            }
                        } else {
                            System.out.println("  Parent: NULL");
                        }
                    }
                })
                .build();

        String input = "- Item 1\n- Item 2\n";
        System.out.println("Pushing input: \n" + input);
        parser.push(input);
        parser.close();
    }
}
