package org.zoolu.sip.header;

import org.zoolu.sip.provider.SipParser;

import java.util.Vector;

public class MultipleHeader {
	protected boolean compact;
	protected String name;
	protected Vector<String> values;

	protected MultipleHeader() {
		this.name = null;
		this.values = new Vector<String>();
		this.compact = true;
	}

	public MultipleHeader(final String name) {
		this.name = name;
		this.values = new Vector<String>();
		this.compact = true;
	}

	public MultipleHeader(final String name, final Vector<String> values) {
		this.name = name;
		this.values = values;
		this.compact = true;
	}

	public MultipleHeader(final Vector<Header> vector) {
		this.name = vector.elementAt(0).getName();
		this.values = new Vector<String>(vector.size());
		for (int i = 0; i < vector.size(); ++i) {
			this.addBottom(vector.elementAt(i));
		}
		this.compact = false;
	}

	public MultipleHeader(final Header header) {
		this.name = header.getName();
		this.values = new Vector<String>();
		final SipParser sipParser = new SipParser(header.getValue());
		for (int i = sipParser.indexOfCommaHeaderSeparator(); i >= 0; i = sipParser.indexOfCommaHeaderSeparator()) {
			this.values.addElement(sipParser.getString(i - sipParser.getPos()).trim());
			sipParser.skipChar();
		}
		this.values.addElement(sipParser.getRemainingString().trim());
		this.compact = true;
	}

	public MultipleHeader(final MultipleHeader multipleHeader) {
		this.name = multipleHeader.getName();
		this.values = multipleHeader.getValues();
		this.compact = multipleHeader.isCommaSeparated();
	}

	public static boolean isCommaSeparated(final Header header) {
		return new SipParser(header.getValue()).indexOfCommaHeaderSeparator() >= 0;
	}

	public void addBottom(final Header header) {
		if (!isCommaSeparated(header)) {
			this.values.addElement(header.getValue());
			return;
		}
		this.addBottom(new MultipleHeader(header));
	}

	public void addBottom(final MultipleHeader multipleHeader) {
		for (int i = 0; i < multipleHeader.size(); ++i) {
			this.values.addElement(multipleHeader.getValue(i));
		}
	}

	public void addTop(final Header header) {
		this.values.insertElementAt(header.getValue(), 0);
	}

	public Object clone() {
		return new MultipleHeader(this.getName(), this.getValues());
	}

	@Override
	public boolean equals(final Object o) {
		final MultipleHeader multipleHeader = (MultipleHeader) o;
		return multipleHeader.getName().equals(this.getName()) && multipleHeader.getValues().equals(this.getValues());
	}

	public Header getBottom() {
		return new Header(this.name, this.values.lastElement());
	}

	public Vector<Header> getHeaders() {
		final Vector<Header> vector = new Vector<Header>(this.values.size());
		for (int i = 0; i < this.values.size(); ++i) {
			vector.addElement(new Header(this.name, this.values.elementAt(i)));
		}
		return vector;
	}

	public String getName() {
		return this.name;
	}

	public Header getTop() {
		return new Header(this.name, this.values.firstElement());
	}

	public String getValue(final int n) {
		return this.values.elementAt(n);
	}

	public Vector<String> getValues() {
		return this.values;
	}

	public boolean isCommaSeparated() {
		return this.compact;
	}

	public boolean isEmpty() {
		return this.values.isEmpty();
	}

	public void removeBottom() {
		this.values.removeElementAt(this.values.size() - 1);
	}

	public void removeTop() {
		this.values.removeElementAt(0);
	}

	public void setCommaSeparated(final boolean compact) {
		this.compact = compact;
	}

	public void setHeaders(final Vector<Header> vector) {
		this.values = new Vector<String>(vector.size());
		for (int i = 0; i < vector.size(); ++i) {
			this.values.addElement(vector.elementAt(i).getValue());
		}
	}

	public void setValues(final Vector<String> values) {
		this.values = values;
	}

	public int size() {
		return this.values.size();
	}

	public Header toHeader() {
		String string = "";
		for (int i = 0; i < this.values.size() - 1; ++i) {
			string = String.valueOf(string) + this.values.elementAt(i) + ", ";
		}
		String string2 = string;
		if (this.values.size() > 0) {
			string2 = String.valueOf(string) + this.values.elementAt(this.values.size() - 1);
		}
		return new Header(this.name, string2);
	}

	@Override
	public String toString() {
		if (this.compact) {
			String s = String.valueOf(this.name) + ": ";
			for (int i = 0; i < this.values.size() - 1; ++i) {
				s = String.valueOf(s) + this.values.elementAt(i) + ", ";
			}
			String string = s;
			if (this.values.size() > 0) {
				string = String.valueOf(s) + this.values.elementAt(this.values.size() - 1);
			}
			return String.valueOf(string) + "\r\n";
		}
		String string2 = "";
		for (int j = 0; j < this.values.size(); ++j) {
			string2 = String.valueOf(string2) + this.name + ": " + this.values.elementAt(j) + "\r\n";
		}
		return string2;
	}
}
