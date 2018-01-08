package org.zoolu.sip.header;

import java.util.Vector;

public class WwwAuthenticateHeader extends AuthenticationHeader {
	public WwwAuthenticateHeader(final String s) {
		super("WWW-Authenticate", s);
	}

	public WwwAuthenticateHeader(final String s, final Vector<String> vector) {
		super("WWW-Authenticate", s, vector);
	}

	public WwwAuthenticateHeader(final Header header) {
		super(header);
	}
}
