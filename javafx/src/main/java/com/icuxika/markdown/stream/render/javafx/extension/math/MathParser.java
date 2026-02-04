package com.icuxika.markdown.stream.render.javafx.extension.math;

import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParser;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineParserState;
import com.icuxika.markdown.stream.render.core.parser.inline.ParsedInline;

public class MathParser implements InlineContentParser {

    @Override
    public ParsedInline tryParse(String input, int index, InlineParserState state) {
        // Check for opening '$'
        // The factory triggers on '$', so we know input[index] is '$'

        // Look for closing '$'
        int start = index + 1;
        int i = start;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '$') {
                // Found closer
                String content = input.substring(start, i);
                if (content.isEmpty()) return ParsedInline.none(); // $$ is empty or handled as Text?

                return ParsedInline.of(new MathNode(content), i + 1);
            }
            if (c == '\n') {
                break;
            }
            i++;
        }

        return ParsedInline.none();
    }

}
