package org.zoolu.sip.header;

import org.zoolu.sip.address.NameAddress;

public class RecordRouteHeader extends NameAddressHeader {
	public RecordRouteHeader(final NameAddress nameAddress) {
		super("Record-Route", nameAddress);
	}

	public RecordRouteHeader(final Header header) {
		super(header);
	}
}
