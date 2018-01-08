package org.zoolu.sip.header;

import org.zoolu.tools.Parser;

public class CallIdHeader extends Header {
	public CallIdHeader(final String s) {
		super("Call-ID", s);
	}

	public CallIdHeader(final Header header) {
		super(header);
	}

	public String getCallId() {
		return new Parser(this.value).getString();
	}

	public void setCallId(final String value) {
		this.value = value;
	}
}
