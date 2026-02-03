package com.icuxika.markdown.stream.render.core;

import com.icuxika.markdown.stream.render.core.ast.*;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.IMarkdownRenderer;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 流式输出测试类 (Streaming Output Tests)
 * 验证解析器是否按照正确的 AST 遍历顺序触发 Visitor 方法。
 * 这对于确保流式渲染（边解析边输出）的正确性至关重要。
 */
public class StreamingOutputTest {

    /**
     * 测试 AST 遍历事件的顺序
     */
    @Test
    public void testEventOrder() throws Exception {
        String markdown = "# Heading\n\nParagraph with **bold** text.";
        
        List<String> events = new ArrayList<>();
        // 创建一个追踪用的 Renderer，记录访问顺序
        IMarkdownRenderer trackingRenderer = new IMarkdownRenderer() {
            @Override
            public Object getResult() {
                return events;
            }

            @Override
            public void visit(Document document) {
                events.add("Document Start");
                visitChildren(document);
                events.add("Document End");
            }

            @Override
            public void visit(Heading heading) {
                events.add("Heading Start (Level " + heading.getLevel() + ")");
                visitChildren(heading);
                events.add("Heading End");
            }

            @Override
            public void visit(Paragraph paragraph) {
                events.add("Paragraph Start");
                visitChildren(paragraph);
                events.add("Paragraph End");
            }

            @Override
            public void visit(Text text) {
                events.add("Text: " + text.getLiteral());
            }

            @Override
            public void visit(StrongEmphasis strongEmphasis) {
                events.add("Strong Start");
                visitChildren(strongEmphasis);
                events.add("Strong End");
            }

            // Implement other methods as no-ops or default
            @Override public void visit(Emphasis emphasis) { visitChildren(emphasis); }
            @Override public void visit(BlockQuote blockQuote) { visitChildren(blockQuote); }
            @Override public void visit(BulletList bulletList) { visitChildren(bulletList); }
            @Override public void visit(OrderedList orderedList) { visitChildren(orderedList); }
            @Override public void visit(ListItem listItem) { visitChildren(listItem); }
            @Override public void visit(CodeBlock codeBlock) { }
            @Override public void visit(HtmlBlock htmlBlock) { }
            @Override public void visit(Table table) { visitChildren(table); }
            @Override public void visit(TableHead tableHead) { visitChildren(tableHead); }
            @Override public void visit(TableBody tableBody) { visitChildren(tableBody); }
            @Override public void visit(TableRow tableRow) { visitChildren(tableRow); }
            @Override public void visit(TableCell tableCell) { visitChildren(tableCell); }
            @Override public void visit(ThematicBreak thematicBreak) { }
            @Override public void visit(SoftBreak softBreak) { }
            @Override public void visit(HardBreak hardBreak) { }
            @Override public void visit(Code code) { }
            @Override public void visit(HtmlInline htmlInline) { }
            @Override public void visit(Link link) { visitChildren(link); }
            @Override public void visit(Image image) { visitChildren(image); }

            private void visitChildren(Node parent) {
                Node child = parent.getFirstChild();
                while (child != null) {
                    child.accept(this);
                    child = child.getNext();
                }
            }
        };

        MarkdownParser parser = new MarkdownParser();
        parser.parse(new StringReader(markdown), trackingRenderer);

        List<String> expected = new ArrayList<>();
        expected.add("Document Start");
        expected.add("Heading Start (Level 1)");
        expected.add("Text: Heading");
        expected.add("Heading End");
        expected.add("Paragraph Start");
        expected.add("Text: Paragraph with ");
        expected.add("Strong Start");
        expected.add("Text: bold");
        expected.add("Strong End");
        expected.add("Text:  text.");
        expected.add("Paragraph End");
        expected.add("Document End");

        assertEquals(expected, events);
    }
}
