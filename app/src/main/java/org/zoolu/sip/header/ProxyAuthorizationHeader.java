package org.zoolu.sip.header;

import java.util.Vector;

public class ProxyAuthorizationHeader extends AuthorizationHeader {
	public ProxyAuthorizationHeader(final String s) {
		super(s);
		this.name = "Proxy-Authorization";
	}

	public ProxyAuthorizationHeader(final String s, final Vector<String> vector) {
		super(s, vector);
		this.name = "Proxy-Authorization";
	}

	public ProxyAuthorizationHeader(final Header header) {
		super(header);
	}
}
