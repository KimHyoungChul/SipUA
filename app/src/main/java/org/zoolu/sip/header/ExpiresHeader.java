package org.zoolu.sip.header;

import org.zoolu.sip.provider.SipParser;
import org.zoolu.tools.Parser;

import java.util.Date;

public class ExpiresHeader extends SipDateHeader {
	public ExpiresHeader(final int n) {
		super("Expires", (String) null);
		this.value = String.valueOf(n);
	}

	public ExpiresHeader(final String s) {
		super("Expires", s);
	}

	public ExpiresHeader(final Date date) {
		super("Expires", date);
	}

	public ExpiresHeader(final Header header) {
		super(header);
	}

	@Override
	public Date getDate() {
		Date date = null;
		if (this.isDate()) {
			date = new SipParser(new Parser(this.value).getStringUnquoted()).getDate();
		} else {
			final long n = this.getDeltaSeconds();
			if (n >= 0L) {
				return new Date(System.currentTimeMillis() + 1000L * n);
			}
		}
		return date;
	}

	public int getDeltaSeconds() {
		if (this.isDate()) {
			int n;
			if ((n = (int) ((new SipParser(new Parser(this.value).getStringUnquoted()).getDate().getTime() - System.currentTimeMillis()) / 1000L)) < 0) {
				n = 0;
			}
			return n;
		}
		return new SipParser(this.value).getInt();
	}

	public boolean isDate() {
		return this.value.indexOf("GMT") >= 0;
	}
}
