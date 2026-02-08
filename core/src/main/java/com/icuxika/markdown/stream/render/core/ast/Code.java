package com.icuxika.markdown.stream.render.core.ast;

public class Code extends Inline {
	private String literal;

	public Code(String literal) {
		this.literal = literal;
	}

	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
