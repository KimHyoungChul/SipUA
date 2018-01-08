package org.zoolu.sip.header;

public class UnsupportedHeader extends OptionHeader {
	public UnsupportedHeader(final String s) {
		super("Unsupported", s);
	}

	public UnsupportedHeader(final Header header) {
		super(header);
	}
}
