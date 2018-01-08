package org.zoolu.sip.header;

public class PttExtension2Header extends Header {
	public PttExtension2Header(final String s) {
		super("Ptt-Extension2", s);
	}

	public PttExtension2Header(final Header header) {
		super(header);
	}
}
