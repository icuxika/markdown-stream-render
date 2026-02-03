package com.icuxika.markdown.stream.render.core.renderer;

import com.icuxika.markdown.stream.render.core.ast.Visitor;

public interface IMarkdownRenderer extends Visitor {
    Object getResult();
}
