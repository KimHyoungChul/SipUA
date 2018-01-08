package org.zoolu.sip.header;

public class SupportedHeader extends OptionHeader {
	public SupportedHeader(final String s) {
		super("Supported", s);
	}

	public SupportedHeader(final Header header) {
		super(header);
	}
}
