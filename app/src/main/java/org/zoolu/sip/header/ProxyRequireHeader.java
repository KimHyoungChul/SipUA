package org.zoolu.sip.header;

public class ProxyRequireHeader extends OptionHeader {
	public ProxyRequireHeader(final String s) {
		super("Proxy-Require", s);
	}

	public ProxyRequireHeader(final Header header) {
		super(header);
	}
}
