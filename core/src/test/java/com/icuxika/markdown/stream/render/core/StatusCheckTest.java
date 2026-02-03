package com.icuxika.markdown.stream.render.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatusCheckTest {

    static class SpecExample {
        public String markdown;
        public String html;
        public int example;
        public int start_line;
        public int end_line;
        public String section;
    }

    @Test
    public void checkStatus() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File specFile = new File("../commonmark-spec.json");
        if (!specFile.exists()) {
            specFile = new File("commonmark-spec.json");
        }
        
        List<SpecExample> examples = mapper.readValue(specFile, new TypeReference<List<SpecExample>>() {});

        Map<String, int[]> stats = new TreeMap<>(); // Section -> [total, passed]

        for (SpecExample example : examples) {
            stats.putIfAbsent(example.section, new int[]{0, 0});
            int[] counts = stats.get(example.section);
            counts[0]++;

            try {
                MarkdownParser parser = new MarkdownParser();
                HtmlRenderer renderer = new HtmlRenderer();
                parser.parse(new java.io.StringReader(example.markdown), renderer);
                String actual = (String) renderer.getResult();
                
                String expected = example.html.replace("\r\n", "\n");
                actual = actual.replace("\r\n", "\n");

                if (expected.equals(actual)) {
                    counts[1]++;
                }
            } catch (Exception e) {
                // Ignore exceptions, count as fail
            }
        }

        System.out.println("\n| Section | Status | Passing | Total |");
        System.out.println("| :--- | :--- | :--- | :--- |");
        
        for (Map.Entry<String, int[]> entry : stats.entrySet()) {
            String section = entry.getKey();
            int total = entry.getValue()[0];
            int passed = entry.getValue()[1];
            
            String status = "未实现";
            if (passed == total) {
                status = "已实现";
            } else if (passed > 0) {
                status = "实现中";
            }
            
            System.out.printf("| **%s** | %s | %d | %d |\n", section, status, passed, total);
        }
        System.out.println();
    }
}
