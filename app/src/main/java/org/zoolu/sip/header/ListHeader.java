package org.zoolu.sip.header;

import org.zoolu.tools.Parser;

import java.util.Vector;

public abstract class ListHeader extends Header {
	public ListHeader(final String s, final String s2) {
		super(s, s2);
	}

	public ListHeader(final Header header) {
		super(header);
	}

	public void addElement(final String value) {
		if (this.value == null || this.value.length() == 0) {
			this.value = value;
			return;
		}
		this.value = String.valueOf(this.value) + ", " + value;
	}

	public Vector<String> getElements() {
		final Vector<String> vector = new Vector<String>();
		final Parser parser = new Parser(this.value);
		while (parser.hasMore()) {
			final String trim = parser.getWord(new char[]{','}).trim();
			if (trim != null && trim.length() > 0) {
				vector.addElement(trim);
			}
			parser.skipChar();
		}
		return vector;
	}

	public void setElements(final Vector<String> vector) {
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < vector.size(); ++i) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(vector.elementAt(i));
		}
		this.value = sb.toString();
	}
}
