package org.zoolu.sip.header;

public class ServerHeader extends Header {
	public ServerHeader(final String s) {
		super("Server", s);
	}

	public ServerHeader(final Header header) {
		super(header);
	}

	public String getInfo() {
		return this.value;
	}

	public void setInfo(final String value) {
		this.value = value;
	}
}
