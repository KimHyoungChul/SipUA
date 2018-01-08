package org.zoolu.sip.header;

public class SubjectHeader extends Header {
	public SubjectHeader(final String s) {
		super("Subject", s);
	}

	public SubjectHeader(final Header header) {
		super(header);
	}

	public String getSubject() {
		return this.value;
	}
}
