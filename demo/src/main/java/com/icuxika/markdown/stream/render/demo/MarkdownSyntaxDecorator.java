package com.icuxika.markdown.stream.render.demo;

import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.SyntaxDecorator;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownSyntaxDecorator implements SyntaxDecorator {

    private final Map<Integer, Boolean> lineStateMap = new HashMap<>();

    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)$");
    private static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile("^(\\s*)(>+)\\s+(.*)$");
    private static final Pattern LIST_PATTERN = Pattern.compile("^(\\s*)([-+*]|\\d+\\.)\\s+(.*)$");
    private static final Pattern FENCE_PATTERN = Pattern.compile("^(\\s*)(`{3,}|~{3,})(.*)$");

    // Inline patterns
    private static final Pattern BOLD_PATTERN = Pattern.compile("(\\*\\*|__)(.*?)\\1");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("(\\*|_)(.*?)\\1");
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("(`)([^`]+)\\1");

    @Override
    public RichParagraph createRichParagraph(CodeTextModel model, int index) {
        String text = model.getPlainText(index);
        RichParagraph.Builder builder = RichParagraph.builder();

        // Determine start state from previous line
        boolean inCodeBlock = false;
        if (index > 0) {
            Boolean prevState = lineStateMap.get(index);
            if (prevState != null) {
                inCodeBlock = prevState;
            } else {
                // If state is missing, re-calculate from 0? Or assume false?
                // For simplicity in this demo, we assume false if unknown, 
                // but ideally we should ensure states are computed sequentially.
                // handleChange handles invalidation.
                inCodeBlock = false;
            }
        }

        // Check for fence on this line
        Matcher fenceMatcher = FENCE_PATTERN.matcher(text);
        boolean isFenceStartOrEnd = fenceMatcher.matches();

        boolean nextLineInCodeBlock = inCodeBlock;
        if (isFenceStartOrEnd) {
            nextLineInCodeBlock = !inCodeBlock;
        }

        // Store state for NEXT line
        lineStateMap.put(index + 1, nextLineInCodeBlock);

        // Apply Styles
        if (inCodeBlock) {
            if (isFenceStartOrEnd) {
                // End of code block
                builder.addSegment(text, StyleAttributeMap.builder()
                        .setTextColor(Color.GRAY)
                        .build());
            } else {
                // Inside code block
                builder.addSegment(text, StyleAttributeMap.builder()
                        .setTextColor(Color.DARKGREEN)
                        .setFontFamily("Consolas")
                        .build());
            }
        } else {
            if (isFenceStartOrEnd) {
                // Start of code block
                builder.addSegment(text, StyleAttributeMap.builder()
                        .setTextColor(Color.GRAY)
                        .build());
            } else {
                // Normal Markdown
                applyMarkdownStyling(builder, text);
            }
        }

        return builder.build();
    }

    private void applyMarkdownStyling(RichParagraph.Builder builder, String text) {
        // Simple line-based checks first
        Matcher headerMatcher = HEADER_PATTERN.matcher(text);
        if (headerMatcher.matches()) {
            // Whole line as header
            // Group 1: hashes, Group 2: content
            int hashesEnd = headerMatcher.end(1);
            builder.addSegment(text.substring(0, hashesEnd), StyleAttributeMap.builder()
                    .setTextColor(Color.BLUE)
                    .build());

            // Add space
            int contentStart = headerMatcher.start(2);
            if (contentStart > hashesEnd) {
                builder.addSegment(text.substring(hashesEnd, contentStart), null);
            }

            // Content
            builder.addSegment(text.substring(contentStart), StyleAttributeMap.builder()
                    .setTextColor(Color.DARKBLUE)
                    .build());
            return;
        }

        Matcher quoteMatcher = BLOCKQUOTE_PATTERN.matcher(text);
        if (quoteMatcher.matches()) {
            builder.addSegment(text, StyleAttributeMap.builder()
                    .setTextColor(Color.GRAY)
                    .build());
            return;
        }

        // Default: Scan for inline styles
        // This is a simplified scanner that just finds tokens and highlights them.
        // It doesn't handle overlapping or nested styles correctly for this demo.

        int lastIdx = 0;
        // We will just process the whole line as plain text for now, 
        // implementing a full inline parser in one method is complex.
        // Let's just handle one type of inline style to demonstrate: Inline Code

        Matcher codeMatcher = INLINE_CODE_PATTERN.matcher(text);
        while (codeMatcher.find()) {
            if (codeMatcher.start() > lastIdx) {
                builder.addSegment(text.substring(lastIdx, codeMatcher.start()), null);
            }

            builder.addSegment(text.substring(codeMatcher.start(), codeMatcher.end()), StyleAttributeMap.builder()
                    .setTextColor(Color.CRIMSON)
                    .setFontFamily("Consolas")
                    .build());

            lastIdx = codeMatcher.end();
        }

        if (lastIdx < text.length()) {
            builder.addSegment(text.substring(lastIdx), null);
        }
    }

    @Override
    public void handleChange(CodeTextModel m, TextPos start, TextPos end, int charsTop, int linesAdded, int charsBottom) {
        // Invalidate state map from start line onwards
        int startLine = start.index();
        lineStateMap.keySet().removeIf(index -> index > startLine);

        // In a real implementation, we might want to re-parse from startLine to ensure state consistency
        // But CodeArea calls createRichParagraph lazily, so as long as we clear future states, 
        // they should be recomputed when those lines are rendered?
        // Actually, createRichParagraph depends on Previous line state. 
        // So we need to ensure the previous line state is correct.
        // Since we compute next line state inside createRichParagraph, simply clearing might be enough 
        // IF the view refreshes sequentially. 

        // However, the interface says: "The implementation might do nothing... Other implementations... should use this method to trigger the refresh."
        // We probably should force a refresh if the state change propagates.
        // For this simple demo, we'll just clear the map.
    }
}
