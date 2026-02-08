package com.icuxika.markdown.stream.render.core.ast;

public class Emphasis extends Inline {
	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
