package com.icuxika.markdown.stream.render.core.ast;

public class Strikethrough extends Node {
	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
