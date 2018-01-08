package org.zoolu.sip.header;

import java.util.Date;

public class DateHeader extends SipDateHeader {
	public DateHeader(final String s) {
		super("Date", s);
	}

	public DateHeader(final Date date) {
		super("Date", date);
	}

	public DateHeader(final Header header) {
		super(header);
	}
}
