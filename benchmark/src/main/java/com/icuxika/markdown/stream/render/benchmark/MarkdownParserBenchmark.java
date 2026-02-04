package com.icuxika.markdown.stream.render.benchmark;

import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import org.openjdk.jmh.annotations.*;

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
    private HtmlRenderer htmlRenderer;
    private Document preParsedDoc;

    @Setup
    public void setup() {
        parser = new MarkdownParser();
        // Create a fresh renderer for each iteration? No, renderer is usually stateless or reset.
        // HtmlRenderer accumulates result in StringBuilder. It is NOT stateless.
        // So we need to create it inside the benchmark or reset it.
        // HtmlRenderer implementation shows: private final StringBuilder sb = new StringBuilder();
        // It does NOT have a reset method.
        // So we should create it inside the benchmark method or use a fresh one.
        // Creating it might add overhead.
        // But for "render", we want to measure rendering.

        // Let's check HtmlRenderer again. It seems designed for single use or we need to clear sb.
        // It has no clear method.
        // So we will instantiate it in the benchmark method for correctness, 
        // OR we assume the overhead of instantiation is negligible compared to rendering.

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

        preParsedDoc = parser.parse(markdownInput);
    }

    @Benchmark
    public Document parseOnly() {
        return parser.parse(markdownInput);
    }

    @Benchmark
    public String renderHtmlOnly() {
        // HtmlRenderer is stateful (StringBuilder), so we must create a new one each time
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        preParsedDoc.accept(renderer);
        return (String) renderer.getResult();
    }

    @Benchmark
    public String parseAndRenderHtml() {
        Document doc = parser.parse(markdownInput);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        doc.accept(renderer);
        return (String) renderer.getResult();
    }
}
