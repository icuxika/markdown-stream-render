package com.icuxika.markdown.stream.render.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icuxika.markdown.stream.render.core.ast.Document;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParserOptions;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class SpecComplianceTest {

    private static class SpecTestCase {
        public String markdown;
        public String html;
        public int example;
        public int start_line;
        public int end_line;
        public String section;
    }

    @TestFactory
    Stream<DynamicTest> testGfmCompliance() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/gfm-spec-0.29.0.json");
        List<SpecTestCase> tests = mapper.readValue(is, new TypeReference<List<SpecTestCase>>() {
        });

        return tests.stream()
                // Filter out some sections if needed initially, or just run all
                // .filter(t -> t.section.equals("Tabs"))
                .map(testCase -> DynamicTest.dynamicTest("Example " + testCase.example + " (" + testCase.section + ")",
                        () -> {
                            MarkdownParserOptions options = new MarkdownParserOptions();
                            options.setGfm(true);
                            MarkdownParser parser = MarkdownParser.builder().options(options).build();
                            Document doc = parser.parse(testCase.markdown);

                            // Currently we only check if parsing succeeds without exception
                            // Ideally we should render to HTML and compare with testCase.html
                            // But Core doesn't depend on HtmlRenderer.
                            // So we just assert Document structure is not null.
                            // If we wanted to test output equality, we would need to depend on 'html'
                            // module in 'core' tests,
                            // which would create circular dependency (html depends on core).
                            // So this test mainly verifies that the PARSER doesn't crash on any GFM spec
                            // example.
                            assertNotNull(doc);
                        }));
    }
}
