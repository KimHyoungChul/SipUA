package org.zoolu.sip.header;

public class AcceptHeader extends ParametricHeader {
	public AcceptHeader() {
		super("Accept", "application/sdp");
	}

	public AcceptHeader(final String s) {
		super("Accept", s);
	}

	public AcceptHeader(final Header header) {
		super(header);
	}

	public String getAcceptRange() {
		return this.value;
	}

	public void setAcceptRange(final String value) {
		this.value = value;
	}
}
