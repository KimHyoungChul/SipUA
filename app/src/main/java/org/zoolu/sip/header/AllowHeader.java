package org.zoolu.sip.header;

import java.util.Vector;

public class AllowHeader extends ListHeader {
	public AllowHeader(final String s) {
		super("Allow", s);
	}

	public AllowHeader(final Header header) {
		super(header);
	}

	public void addMethod(final String s) {
		super.addElement(s);
	}

	public Vector<String> getMethods() {
		return super.getElements();
	}

	public void setMethod(final Vector<String> elements) {
		super.setElements(elements);
	}
}
