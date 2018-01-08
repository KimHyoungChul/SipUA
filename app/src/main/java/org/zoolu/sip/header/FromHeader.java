package org.zoolu.sip.header;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;

public class FromHeader extends EndPointHeader {
	public FromHeader(final NameAddress nameAddress) {
		super("From", nameAddress);
	}

	public FromHeader(final NameAddress nameAddress, final String s) {
		super("From", nameAddress, s);
	}

	public FromHeader(final SipURL sipURL) {
		super("From", sipURL);
	}

	public FromHeader(final SipURL sipURL, final String s) {
		super("From", sipURL, s);
	}

	public FromHeader(final Header header) {
		super(header);
	}
}
