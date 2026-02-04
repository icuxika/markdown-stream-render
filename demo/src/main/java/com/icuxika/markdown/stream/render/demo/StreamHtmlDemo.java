package com.icuxika.markdown.stream.render.demo;

import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.HtmlStreamRenderer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StreamHtmlDemo {

    public static void main(String[] args) {
        System.out.println("--- Stream HTML Demo ---");
        System.out.println("Simulating network stream (output will appear chunk by chunk)...\n");

        // 1. Create Stream Renderer targeting System.out
        HtmlStreamRenderer renderer = new HtmlStreamRenderer(System.out);

        // 2. Create Stream Parser
        StreamMarkdownParser parser = StreamMarkdownParser.builder()
                .renderer(renderer)
                .build();

        // 3. Simulate streaming input
        String[] chunks = {
                "# Hello Stream\n\n",
                "This is a **streaming** markdown parser.\n",
                "It processes text ",
                "as it arrives.\n\n",
                "## Features\n\n",
                "- Incremental Parsing\n",
                "- Real-time Rendering\n",
                "- Low Latency\n\n",
                "```java\n",
                "System.out.println(\"Code works too!\");\n",
                "```\n\n",
                "> Blockquotes are also supported.\n\n"
        };

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        final int[] index = {0};

        executor.scheduleAtFixedRate(() -> {
            if (index[0] < chunks.length) {
                String chunk = chunks[index[0]++];
                // Simulate network delay
                try {
                    // System.out.print("[DEBUG: Pushing chunk] "); // Uncomment to see chunk boundaries
                    parser.push(chunk);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                parser.close();
                executor.shutdown();
                System.out.println("\n--- Stream Finished ---");
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }
}
