package org.zoolu.sip.header;

import org.zoolu.sip.address.NameAddress;

public class RouteHeader extends NameAddressHeader {
	public RouteHeader(final NameAddress nameAddress) {
		super("Route", nameAddress);
	}

	public RouteHeader(final Header header) {
		super(header);
	}
}
