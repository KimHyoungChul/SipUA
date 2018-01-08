package org.zoolu.sip.header;

import org.zoolu.sip.provider.SipParser;

public class CSeqHeader extends Header {
	public CSeqHeader(final long n, final String s) {
		super("CSeq", String.valueOf(String.valueOf(n)) + " " + s);
	}

	public CSeqHeader(final String s) {
		super("CSeq", s);
	}

	public CSeqHeader(final Header header) {
		super(header);
	}

	public String getMethod() {
		final SipParser sipParser = new SipParser(this.value);
		sipParser.skipString();
		return sipParser.getString();
	}

	public long getSequenceNumber() {
		return new SipParser(this.value).getInt();
	}

	public CSeqHeader incSequenceNumber() {
		this.value = String.valueOf(String.valueOf(this.getSequenceNumber() + 1L)) + " " + this.getMethod();
		return this;
	}

	public void setMethod(final String s) {
		this.value = String.valueOf(this.getSequenceNumber()) + " " + s;
	}

	public void setSequenceNumber(final long n) {
		this.value = String.valueOf(String.valueOf(n)) + " " + this.getMethod();
	}
}
