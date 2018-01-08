package org.zoolu.sip.header;

import org.zoolu.sip.provider.SipParser;

public class ContentTypeHeader extends ParametricHeader {
	public ContentTypeHeader(final String s) {
		super("Content-Type", s);
	}

	public ContentTypeHeader(final Header header) {
		super(header);
	}

	public String getContentType() {
		final int index = new SipParser(this.value).indexOf(';');
		String s;
		if (index < 0) {
			s = this.value;
		} else {
			s = this.value.substring(0, index);
		}
		return new SipParser(s).getString();
	}

	public void setContentType(final String value) {
		this.value = value;
	}
}
