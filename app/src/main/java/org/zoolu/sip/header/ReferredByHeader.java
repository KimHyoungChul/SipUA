package org.zoolu.sip.header;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;

public class ReferredByHeader extends NameAddressHeader {
	public ReferredByHeader(final NameAddress nameAddress) {
		super("Referred-By", nameAddress);
	}

	public ReferredByHeader(final SipURL sipURL) {
		super("Referred-By", sipURL);
	}

	public ReferredByHeader(final Header header) {
		super(header);
	}
}
