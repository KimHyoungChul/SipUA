package org.zoolu.sip.header;

import java.util.Vector;

public class ProxyAuthenticateHeader extends WwwAuthenticateHeader {
	public ProxyAuthenticateHeader(final String s) {
		super(s);
		this.name = "Proxy-Authenticate";
	}

	public ProxyAuthenticateHeader(final String s, final Vector<String> vector) {
		super(s, vector);
		this.name = "Proxy-Authenticate";
	}

	public ProxyAuthenticateHeader(final Header header) {
		super(header);
	}
}
