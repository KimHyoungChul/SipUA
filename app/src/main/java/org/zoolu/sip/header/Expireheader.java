package org.zoolu.sip.header;

public class Expireheader extends Header {
	public Expireheader(final String s) {
		super("Session-Expires", s);
	}

	public Expireheader(final Header header) {
		super(header);
	}

	public String getInfo() {
		return this.value;
	}

	public void setInfo(final String value) {
		this.value = value;
	}
}
