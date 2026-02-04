package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.ast.Heading;
import com.icuxika.markdown.stream.render.core.ast.ListItem;
import com.icuxika.markdown.stream.render.core.ast.BulletList;
import com.icuxika.markdown.stream.render.core.ast.OrderedList;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.IStreamMarkdownRenderer;

public class DebugHeadingListStream {
    public static void main(String[] args) {
        System.out.println("--- Starting Debug ---");
        StreamMarkdownParser parser = StreamMarkdownParser.builder()
                .renderer(new IStreamMarkdownRenderer() {
                    @Override
                    public void renderNode(Node node) {
                        System.out.println("RENDER EVENT: " + node.getClass().getSimpleName());
                        if (node instanceof Heading) {
                            System.out.println("  -> Heading Level: " + ((Heading) node).getLevel());
                        }
                        
                        Node parent = node.getParent();
                        if (parent instanceof ListItem) {
                            System.out.println("  -> Inside ListItem");
                            if (node.getPrevious() == null) {
                                System.out.println("  -> IS FIRST CHILD (Should render marker)");
                                Node list = parent.getParent();
                                if (list instanceof BulletList) {
                                    System.out.println("    -> Parent is BulletList");
                                } else if (list instanceof OrderedList) {
                                    System.out.println("    -> Parent is OrderedList");
                                } else {
                                    System.out.println("    -> Parent is " + (list == null ? "NULL" : list.getClass().getSimpleName()));
                                }
                            } else {
                                System.out.println("  -> NOT FIRST CHILD");
                            }
                        }
                    }
                })
                .build();

        String input = "# Heading 1\n\n## Heading 2\n\n- List Item 1\n- List Item 2\n\nText paragraph.\n";
        System.out.println("Pushing input:\n" + input);
        parser.push(input);
        parser.close();
    }
}
