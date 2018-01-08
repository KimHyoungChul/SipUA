package org.zoolu.sip.header;

public class UserAgentHeader extends Header {
	public UserAgentHeader(final String s) {
		super("User-Agent", s);
	}

	public UserAgentHeader(final Header header) {
		super(header);
	}

	public String getInfo() {
		return this.value;
	}

	public void setInfo(final String value) {
		this.value = value;
	}
}
