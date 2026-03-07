package com.icuxika.markdown.stream.render.core.extension.math;

import java.util.Collections;
import java.util.Set;

import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParser;
import com.icuxika.markdown.stream.render.core.parser.inline.InlineContentParserFactory;

public class MathParserFactory implements InlineContentParserFactory {
  @Override
  public Set<Character> getTriggerCharacters() {
    return Collections.singleton('$');
  }

  @Override
  public InlineContentParser create() {
    return new MathParser();
  }
}
