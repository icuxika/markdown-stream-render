package com.icuxika.markdown.stream.render.core.ast;

public abstract class CustomNode extends Node {
	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
