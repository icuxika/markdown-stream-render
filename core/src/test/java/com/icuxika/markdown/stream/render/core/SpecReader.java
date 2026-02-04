package com.icuxika.markdown.stream.render.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SpecReader {

    public static class SpecExample {
        public String markdown;
        public String html;
        public String section;
        public int exampleNumber;

        public SpecExample(String markdown, String html, String section, int exampleNumber) {
            this.markdown = markdown;
            this.html = html;
            this.section = section;
            this.exampleNumber = exampleNumber;
        }
    }

    private static final Pattern EXAMPLE_START = Pattern.compile("^`{32} example.*");
    private static final Pattern EXAMPLE_END = Pattern.compile("^`{32}$");

    public static List<SpecExample> readExamples(InputStream inputStream) throws IOException {
        List<SpecExample> examples = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            String currentSection = "";
            int exampleCount = 0;

            StringBuilder markdownBuffer = new StringBuilder();
            StringBuilder htmlBuffer = new StringBuilder();

            boolean inExample = false;
            boolean readingHtml = false;

            while ((line = reader.readLine()) != null) {
                if (!inExample) {
                    if (EXAMPLE_START.matcher(line).matches()) {
                        inExample = true;
                        readingHtml = false;
                        markdownBuffer.setLength(0);
                        htmlBuffer.setLength(0);
                        exampleCount++;
                    } else if (line.startsWith("#")) {
                        // Simple header parsing to get section
                        // e.g., "# Task list items"
                        currentSection = line.replaceAll("^#+\\s*", "").trim();
                    }
                } else {
                    if (EXAMPLE_END.matcher(line).matches()) {
                        inExample = false;
                        // Add example
                        examples.add(new SpecExample(
                                markdownBuffer.toString().replace("→", "\t"), // Spec uses → for tab visualization sometimes, but usually raw tabs
                                htmlBuffer.toString().replace("→", "\t"),
                                currentSection,
                                exampleCount
                        ));
                    } else if (line.equals(".")) {
                        readingHtml = true;
                    } else {
                        if (readingHtml) {
                            htmlBuffer.append(line).append("\n");
                        } else {
                            markdownBuffer.append(line).append("\n");
                        }
                    }
                }
            }
        }
        return examples;
    }
}
