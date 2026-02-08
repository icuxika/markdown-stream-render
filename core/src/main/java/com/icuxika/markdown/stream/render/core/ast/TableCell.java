package com.icuxika.markdown.stream.render.core.ast;

public class TableCell extends Block {
	private boolean header;
	private Alignment alignment;

	public enum Alignment {
		LEFT, CENTER, RIGHT, NONE
	}

	public boolean isHeader() {
		return header;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
