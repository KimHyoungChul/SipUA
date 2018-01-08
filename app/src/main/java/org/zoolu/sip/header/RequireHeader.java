package org.zoolu.sip.header;

public class RequireHeader extends OptionHeader {
	public RequireHeader(final String s) {
		super("Require", s);
	}

	public RequireHeader(final Header header) {
		super(header);
	}
}
