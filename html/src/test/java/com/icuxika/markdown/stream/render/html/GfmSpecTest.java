package com.icuxika.markdown.stream.render.html;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.html.renderer.HtmlRenderer;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * GFM 规范兼容性测试 (GFM Spec Conformance Tests)
 * <p>
 * 该类负责运行 GFM Spec (v0.29.0) 中标记为扩展章节（section 以 "(extension)" 结尾）的测试用例。
 * </p>
 */
public class GfmSpecTest {

    static class SpecExample {
        public String markdown;
        public String html;
        public int example;
        public int start_line;
        public int end_line;
        public String section;
    }

    private List<SpecExample> loadExamples() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.io.InputStream specStream = getClass().getResourceAsStream("/gfm-spec-0.29.0.json");

        if (specStream == null) {
            throw new RuntimeException("Could not find gfm-spec-0.29.0.json in classpath");
        }

        return mapper.readValue(specStream, new TypeReference<List<SpecExample>>() {
        });
    }

    @TestFactory
    Stream<DynamicTest> gfmSpecTests() throws IOException {
        List<SpecExample> examples = loadExamples();

        return examples.stream().filter(example -> example.section.endsWith("(extension)")).map(
                example -> DynamicTest.dynamicTest("Example " + example.example + " (" + example.section + ")", () -> {
                    MarkdownParser parser = new MarkdownParser();
                    parser.getOptions().setGfm(true); // Enable GFM extensions
                    HtmlRenderer renderer = new HtmlRenderer();
                    parser.parse(new java.io.StringReader(example.markdown), renderer);
                    String actual = (String) renderer.getResult();

                    // 规范化换行符以进行比较
                    String expected = example.html.replace("\r\n", "\n");
                    actual = actual.replace("\r\n", "\n");

                    if (!expected.equals(actual)) {
                        System.out.println("Failed Example " + example.example);
                        System.out.println("Markdown: [" + example.markdown.replace("\n", "\\n") + "]");
                        System.out.println("Expected: [" + expected.replace("\n", "\\n") + "]");
                        System.out.println("Actual:   [" + actual.replace("\n", "\\n") + "]");
                    }

                    assertEquals(expected, actual);
                }));
    }
}
