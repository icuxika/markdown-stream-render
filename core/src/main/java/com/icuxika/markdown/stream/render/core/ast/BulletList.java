package com.icuxika.markdown.stream.render.core.ast;

public class BulletList extends Block {
	private char bulletChar; // '-', '+', or '*'
	private boolean tight = true;

	public char getBulletChar() {
		return bulletChar;
	}

	public void setBulletChar(char bulletChar) {
		this.bulletChar = bulletChar;
	}

	public boolean isTight() {
		return tight;
	}

	public void setTight(boolean tight) {
		this.tight = tight;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
