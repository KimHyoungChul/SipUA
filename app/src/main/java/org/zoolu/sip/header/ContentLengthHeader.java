package org.zoolu.sip.header;

import org.zoolu.sip.provider.SipParser;

public class ContentLengthHeader extends Header {
	public ContentLengthHeader(final int n) {
		super("Content-Length", String.valueOf(n));
	}

	public ContentLengthHeader(final Header header) {
		super(header);
	}

	public int getContentLength() {
		return new SipParser(this.value).getInt();
	}

	public void setContentLength(final int n) {
		this.value = String.valueOf(n);
	}
}
