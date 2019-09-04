package com.example.language.attrnode;

import com.example.language.JavaAttribute;
import com.example.language.JavaDescriber.CharPosition;

public abstract class JavaNode extends Node {
	
	private String id;
	private String attribute;
	private JavaNode parent;
	private CharPosition boundsPos;
	private String[] decorateWords;
	
	//private List<String, JavaNode> varList
	public final String getId() {
		return id;
	}
	
	public final void setId(String id) {
		if (getParent() != null) {
			StringBuffer sb = new StringBuffer();
			sb.append(getParent().getId())
			.append(".").append(id);
			this.id = sb.toString();
		}
		this.id = id;
	}
	
	public final String getAttribute() {
		return attribute;
	}
	
	public final void setAttribute(String attr) {
		this.attribute = attr;
	}
	
	public final JavaNode getParent() {
		return parent;
	}
	
	public final void setParent(JavaNode parent) {
		String parentAttr = parent.getAttribute();
		if (parentAttr.equals(JavaAttribute.FIELD)) {
			throw new IllegalArgumentException();
		}
		this.parent = parent;
	}
	
	public final void setBoundsPos(CharPosition boundsPos) {
		if (boundsPos.getMarkPosition() == null) {
			throw new IllegalArgumentException();
		}
		this.boundsPos = boundsPos;
	}
	
	public final int getNodeStartLine() {
		return boundsPos.getRow();
	}
	
	public final int getNodeEndLine() {
		return boundsPos.getMarkPosition().getRow();
	}

	public void setDecorateWord(String[] decor) {
		decorateWords = decor;
	}
	
	public String[] getDecorateWord() {
		return decorateWords;
	}
}
