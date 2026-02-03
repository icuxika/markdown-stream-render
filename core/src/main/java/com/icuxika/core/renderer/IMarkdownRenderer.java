package com.icuxika.core.renderer;

import com.icuxika.core.ast.Visitor;

public interface IMarkdownRenderer extends Visitor {
    Object getResult();
}
