package org.zoolu.sip.header;

import org.zoolu.sip.provider.SipParser;
import org.zoolu.tools.DateFormat;

import java.util.Date;

public abstract class SipDateHeader extends Header {
	public SipDateHeader(final String s, final String s2) {
		super(s, s2);
	}

	public SipDateHeader(final String s, final Date date) {
		super(s, null);
		this.value = DateFormat.formatEEEddMMM(date);
	}

	public SipDateHeader(final Header header) {
		super(header);
	}

	public Date getDate() {
		return new SipParser(this.value).getDate();
	}
}
