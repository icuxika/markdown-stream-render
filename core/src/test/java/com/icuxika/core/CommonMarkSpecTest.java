package com.icuxika.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icuxika.core.parser.MarkdownParser;
import com.icuxika.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonMarkSpecTest {

    static class SpecExample {
        public String markdown;
        public String html;
        public int example;
        public int start_line;
        public int end_line;
        public String section;
        }

    private void printAst(com.icuxika.core.ast.Node node, String indent) {
        System.out.println(indent + node.getClass().getSimpleName());
        com.icuxika.core.ast.Node child = node.getFirstChild();
        while (child != null) {
            printAst(child, indent + "  ");
            child = child.getNext();
       }
}

    @TestFactory
    Stream<DynamicTest> commonMarkSpecTests() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File specFile = new File("../commonmark-spec.json");
        if (!specFile.exists()) {
             // Fallback for different CWD
             specFile = new File("commonmark-spec.json");
        }
        if (!specFile.exists()) {
            throw new RuntimeException("Could not find commonmark-spec.json at " + specFile.getAbsolutePath());
        }
        
        List<SpecExample> examples = mapper.readValue(specFile, new TypeReference<List<SpecExample>>() {});

        return examples.stream()
                .filter(example -> "Images".equals(example.section))
                .map(example -> DynamicTest.dynamicTest(
                        "Example " + example.example + " (" + example.section + ")",
                        () -> {
                            MarkdownParser parser = new MarkdownParser();
                            HtmlRenderer renderer = new HtmlRenderer();
                            parser.parse(new java.io.StringReader(example.markdown), renderer);
                            String actual = (String) renderer.getResult();
                            
                            // Normalize newlines for comparison
                            String expected = example.html.replace("\r\n", "\n");
                            actual = actual.replace("\r\n", "\n");
                            
                            if (!expected.equals(actual)) {
                                System.out.println("Failed Example " + example.example);
                                System.out.println("Markdown: [" + example.markdown + "]");
                                System.out.println("Expected: [" + expected + "]");
                                System.out.println("Actual:   [" + actual + "]");
                                printAst(parser.parse(example.markdown), "");
                            }
                            
                            // Basic trimming for this naive implementation
                            // We don't want to be too strict on whitespace for this initial pass
                            // but commonmark is strict. 
                            // Let's assert equality and see.
                            assertEquals(expected, actual);
                        }
                ));
    }
}
