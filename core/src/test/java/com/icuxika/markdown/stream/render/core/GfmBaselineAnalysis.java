package com.icuxika.markdown.stream.render.core;

import com.icuxika.markdown.stream.render.core.parser.MarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlRenderer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行 GFM Spec 中的所有测试用例，并生成对比分析报告。
 * 不作为自动化回归测试的一部分（因为预期会失败），而是作为开发进度的评估工具。
 */
public class GfmBaselineAnalysis {

    @Test
    public void runAnalysis() throws IOException {
        InputStream specStream = getClass().getResourceAsStream("/spec-0.29.0.gfm.13.txt");
        if (specStream == null) {
            throw new RuntimeException("Could not find spec-0.29.0.gfm.13.txt");
        }

        List<SpecReader.SpecExample> examples = SpecReader.readExamples(specStream);
        
        Map<String, int[]> stats = new HashMap<>(); // Section -> [total, passed]
        int totalPassed = 0;
        int totalFailed = 0;

        StringBuilder detailReport = new StringBuilder();
        detailReport.append("# GFM Baseline Analysis Report\n\n");
        detailReport.append("| Example | Section | Status | Expected vs Actual |\n");
        detailReport.append("| :--- | :--- | :--- | :--- |\n");

        for (SpecReader.SpecExample example : examples) {
            MarkdownParser parser = new MarkdownParser();
            HtmlRenderer renderer = new HtmlRenderer();
            
            String actual = "";
            try {
                parser.parse(new java.io.StringReader(example.markdown), renderer);
                actual = (String) renderer.getResult();
            } catch (Exception e) {
                actual = "Exception: " + e.getMessage();
            }

            // Normalize newlines
            String expected = example.html.replace("\r\n", "\n");
            actual = actual.replace("\r\n", "\n");

            boolean passed = expected.equals(actual);
            
            // Update stats
            stats.putIfAbsent(example.section, new int[]{0, 0});
            stats.get(example.section)[0]++;
            if (passed) {
                stats.get(example.section)[1]++;
                totalPassed++;
            } else {
                totalFailed++;
                // Only log failures for key sections to keep report readable
                if (isKeySection(example.section)) {
                   if (!passed) {
                       System.out.println("Failed: " + example.section + " Example " + example.exampleNumber);
                       System.out.println("Markdown: [" + example.markdown.replace("\n", "\\n") + "]");
                       System.out.println("Expected: [" + expected.replace("\n", "\\n") + "]");
                       System.out.println("Actual:   [" + actual.replace("\n", "\\n") + "]");
                   }
                }
            }
        }

        // Generate Summary
        System.out.println("==================================================");
        System.out.println("GFM Baseline Analysis");
        System.out.println("==================================================");
        System.out.println("Total Examples: " + examples.size());
        System.out.println("Passed: " + totalPassed);
        System.out.println("Failed: " + totalFailed);
        System.out.println("Pass Rate: " + String.format("%.2f%%", (double)totalPassed / examples.size() * 100));
        System.out.println("--------------------------------------------------");
        System.out.println(String.format("%-30s | %-10s | %-10s | %-10s", "Section", "Total", "Passed", "Rate"));
        System.out.println("--------------------------------------------------");
        
        stats.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEach(entry -> {
                    String section = entry.getKey();
                    int total = entry.getValue()[0];
                    int pass = entry.getValue()[1];
                    double rate = (double) pass / total * 100;
                    System.out.println(String.format("%-30s | %-10d | %-10d | %6.2f%%", section, total, pass, rate));
                });
        System.out.println("==================================================");
        
        // Write detailed report to file
        try (PrintWriter writer = new PrintWriter(new FileWriter("GFM_ANALYSIS.md"))) {
            writer.println("# GFM Baseline Analysis");
            writer.println();
            writer.println("**Total**: " + examples.size() + " | **Passed**: " + totalPassed + " | **Failed**: " + totalFailed);
            writer.println();
            writer.println("## Section Breakdown");
            writer.println("| Section | Total | Passed | Pass Rate |");
            writer.println("| :--- | :--- | :--- | :--- |");
            
            stats.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEach(entry -> {
                    String section = entry.getKey();
                    int total = entry.getValue()[0];
                    int pass = entry.getValue()[1];
                    double rate = (double) pass / total * 100;
                    writer.println(String.format("| %s | %d | %d | %.2f%% |", section, total, pass, rate));
                });
        }
    }
    
    private boolean isKeySection(String section) {
        return section.equalsIgnoreCase("Tables (extension)") || 
               section.equalsIgnoreCase("Task list items (extension)") || 
               section.equalsIgnoreCase("Strikethrough (extension)") ||
               section.equalsIgnoreCase("Autolinks (extension)");
    }
}
