package com.icuxika.markdown.stream.render.core.extension.admonition;

import com.icuxika.markdown.stream.render.core.parser.block.BlockParserFactory;
import com.icuxika.markdown.stream.render.core.parser.block.BlockStart;
import com.icuxika.markdown.stream.render.core.parser.block.MatchedBlockParser;
import com.icuxika.markdown.stream.render.core.parser.block.ParserState;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdmonitionBlockParserFactory implements BlockParserFactory {
    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
        CharSequence line = state.getLine();
        int nextNonSpace = state.getNextNonSpaceIndex();

        // Regex check
        String currentLine = line.toString().substring(nextNonSpace);
        Pattern pattern = Pattern.compile("^!!!\\s+(\\w+)(?:\\s+\"(.*)\")?$");
        Matcher matcher = pattern.matcher(currentLine);

        if (matcher.matches()) {
            String type = matcher.group(1);
            String title = matcher.group(2);
            return BlockStart.of(new AdmonitionParser(type, title)).atIndex(state.getIndex() + currentLine.length()); // Consume
                                                                                                                      // whole
                                                                                                                      // line
        }

        return BlockStart.none();
    }
}
