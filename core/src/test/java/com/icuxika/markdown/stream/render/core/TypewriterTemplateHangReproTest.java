package com.icuxika.markdown.stream.render.core;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.icuxika.markdown.stream.render.core.ast.Node;
import com.icuxika.markdown.stream.render.core.parser.StreamMarkdownParser;
import com.icuxika.markdown.stream.render.core.renderer.StreamMarkdownTypingRenderer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class TypewriterTemplateHangReproTest {

    @Test
    public void streamingAroundInlineMathDoesNotHang() throws Exception {
        String markdown = Files.readString(Path.of("..", "demo", "src", "main", "resources", "template.md"),
                StandardCharsets.UTF_8);
        int start = markdown.indexOf("### Inline Math");
        if (start < 0) {
            start = 0;
        }
        int end = Math.min(markdown.length(), start + 3500);
        String sample = markdown.substring(0, end);

        StreamMarkdownTypingRenderer renderer = new StreamMarkdownTypingRenderer() {
            @Override
            public void renderPreviewNode(Node node) {
            }

            @Override
            public void clearPreview() {
            }

            @Override
            public void renderNode(Node node) {
            }

            @Override
            public void openBlock(Node node) {
            }

            @Override
            public void closeBlock(Node node) {
            }
        };

        StreamMarkdownParser.Builder builder = StreamMarkdownParser.builder().renderer(renderer);
        CoreExtension.addDefaults(builder);
        StreamMarkdownParser parser = builder.build();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            int chunk = 1;
            for (int i = 0; i < sample.length(); i += chunk) {
                int e = Math.min(sample.length(), i + chunk);
                parser.push(sample.substring(i, e));
            }
            parser.close();
        });
    }
}
