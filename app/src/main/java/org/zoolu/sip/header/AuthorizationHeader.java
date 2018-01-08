package org.zoolu.sip.header;

import java.util.Vector;

public class AuthorizationHeader extends AuthenticationHeader {
	public AuthorizationHeader(final String s) {
		super("Authorization", s);
	}

	public AuthorizationHeader(final String s, final Vector<String> vector) {
		super("Authorization", s, vector);
	}

	public AuthorizationHeader(final Header header) {
		super(header);
	}
}
