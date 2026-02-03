package com.icuxika.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icuxika.core.parser.MarkdownParser;
import com.icuxika.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FullSpecTest {

    static class SpecExample {
        public String markdown;
        public String html;
        public int example;
        public int start_line;
        public int end_line;
        public String section;
    }

    static class TestResult {
        int example;
        String section;
        boolean passed;
        String message;
    }

    @Test
    public void runFullSpecAndGenerateReport() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File specFile = new File("../commonmark-spec.json");
        if (!specFile.exists()) {
            specFile = new File("commonmark-spec.json");
        }
        if (!specFile.exists()) {
            throw new RuntimeException("Could not find commonmark-spec.json");
        }

        List<SpecExample> examples = mapper.readValue(specFile, new TypeReference<List<SpecExample>>() {});
        examples.sort(Comparator.comparingInt(e -> e.example));

        List<TestResult> results = new ArrayList<>();
        int passed = 0;
        int failed = 0;

        for (SpecExample example : examples) {
            TestResult result = new TestResult();
            result.example = example.example;
            result.section = example.section;

            try {
                MarkdownParser parser = new MarkdownParser();
                HtmlRenderer renderer = new HtmlRenderer();
                parser.parse(new java.io.StringReader(example.markdown), renderer);
                String actual = (String) renderer.getResult();

                // Normalize newlines
                String expected = example.html.replace("\r\n", "\n");
                actual = actual.replace("\r\n", "\n");

                if (expected.equals(actual)) {
                    result.passed = true;
                    passed++;
                } else {
                    result.passed = false;
                    result.message = "Expected length: " + expected.length() + ", Actual length: " + actual.length();
                    failed++;
                }
            } catch (Exception e) {
                result.passed = false;
                result.message = "Exception: " + e.getMessage();
                failed++;
            }
            results.add(result);
        }

        // Write report
        File reportFile = new File("../SPEC_REPORT.md");
        if (!reportFile.getParentFile().exists()) {
            reportFile = new File("SPEC_REPORT.md");
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
            writer.println("# CommonMark Spec Test Report");
            writer.println();
            writer.println("Total: " + examples.size() + ", Passed: " + passed + ", Failed: " + failed);
            writer.println();
            writer.println("| Example | Section | Status | Info |");
            writer.println("| :--- | :--- | :--- | :--- |");
            
            for (TestResult res : results) {
                writer.println("| " + res.example + " | " + res.section + " | " + (res.passed ? "✅ PASS" : "❌ FAIL") + " | " + (res.message != null ? res.message : "") + " |");
            }
        }
        
        System.out.println("Report generated at " + reportFile.getAbsolutePath());
    }
}
