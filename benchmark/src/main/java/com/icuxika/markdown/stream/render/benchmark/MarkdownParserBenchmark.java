package com.icuxika.markdown.stream.render.benchmark;

import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.IMarkdownRenderer;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class MarkdownParserBenchmark {

    @Param({"SMALL", "MEDIUM", "LARGE"})
    private String size;

    private String markdownInput;
    private MarkdownParser parser;
    private NoOpRenderer renderer;

    @Setup
    public void setup() {
        parser = new MarkdownParser();
        renderer = new NoOpRenderer();

        switch (size) {
            case "SMALL":
                markdownInput = "# Hello\nThis is a small test.";
                break;
            case "MEDIUM":
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 100; i++) {
                    sb.append("## Section ").append(i).append("\n");
                    sb.append("This is paragraph ").append(i).append(" with **bold** and *italic* text.\n");
                    sb.append("- List item 1\n- List item 2\n\n");
                }
                markdownInput = sb.toString();
                break;
            case "LARGE":
                StringBuilder sb2 = new StringBuilder();
                for (int i = 0; i < 1000; i++) {
                    sb2.append("## Section ").append(i).append("\n");
                    sb2.append("This is paragraph ").append(i).append(" with **bold** and *italic* text.\n");
                    sb2.append("- List item 1\n- List item 2\n\n");
                    sb2.append("```java\nSystem.out.println(\"Code block\");\n```\n\n");
                }
                markdownInput = sb2.toString();
                break;
        }
    }

    @Benchmark
    public Document parseOnly() {
        return parser.parse(markdownInput);
    }

    @Benchmark
    public void parseAndRender() throws IOException {
        parser.parse(new StringReader(markdownInput), renderer);
    }

    // Dummy renderer that does nothing
    private static class NoOpRenderer implements IMarkdownRenderer {
        @Override
        public Object getResult() {
            return null;
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.Document document) {
            visitChildren(document);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.HtmlBlock htmlBlock) {
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.HtmlInline htmlInline) {
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.Paragraph paragraph) {
            visitChildren(paragraph);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.Heading heading) {
            visitChildren(heading);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.Text text) {
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.SoftBreak softBreak) {
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.HardBreak hardBreak) {
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.Emphasis emphasis) {
            visitChildren(emphasis);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.StrongEmphasis strongEmphasis) {
            visitChildren(strongEmphasis);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.BlockQuote blockQuote) {
            visitChildren(blockQuote);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.BulletList bulletList) {
            visitChildren(bulletList);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.OrderedList orderedList) {
            visitChildren(orderedList);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.ListItem listItem) {
            visitChildren(listItem);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.Strikethrough strikethrough) {
            visitChildren(strikethrough);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.Code code) {
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.ThematicBreak thematicBreak) {
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.CodeBlock codeBlock) {
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.Link link) {
            visitChildren(link);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.Image image) {
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.Table table) {
            visitChildren(table);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.TableHead tableHead) {
            visitChildren(tableHead);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.TableBody tableBody) {
            visitChildren(tableBody);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.TableRow tableRow) {
            visitChildren(tableRow);
        }

        @Override
        public void visit(com.icuxika.markdown.stream.render.core.ast.TableCell tableCell) {
            visitChildren(tableCell);
        }

        private void visitChildren(Node parent) {
            Node child = parent.getFirstChild();
            while (child != null) {
                child.accept(this);
                child = child.getNext();
            }
        }
    }
}
