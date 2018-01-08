package org.zoolu.sip.header;

import org.zoolu.tools.Parser;

public class MaxForwardsHeader extends Header {
	public MaxForwardsHeader(final int n) {
		super("Max-Forwards", String.valueOf(n));
	}

	public MaxForwardsHeader(final String s) {
		super("Max-Forwards", s);
	}

	public MaxForwardsHeader(final Header header) {
		super(header);
	}

	public void decrement() {
		this.value = String.valueOf(this.getNumber() - 1);
	}

	public int getNumber() {
		return new Parser(this.value).getInt();
	}

	public void setNumber(final int n) {
		this.value = String.valueOf(n);
	}
}
