package org.zoolu.sip.header;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;

public class ReferToHeader extends NameAddressHeader {
	public ReferToHeader(final NameAddress nameAddress) {
		super("Refer-To", nameAddress);
	}

	public ReferToHeader(final SipURL sipURL) {
		super("Refer-To", sipURL);
	}

	public ReferToHeader(final Header header) {
		super(header);
	}
}
