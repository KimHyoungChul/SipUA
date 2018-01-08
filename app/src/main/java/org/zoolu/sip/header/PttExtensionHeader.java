package org.zoolu.sip.header;

public class PttExtensionHeader extends Header {
	public PttExtensionHeader(final String s) {
		super("Ptt-Extension", s);
	}

	public PttExtensionHeader(final Header header) {
		super(header);
	}
}
