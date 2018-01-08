package org.zoolu.sip.header;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;

public class ToHeader extends EndPointHeader {
	public ToHeader(final NameAddress nameAddress) {
		super("To", nameAddress);
	}

	public ToHeader(final NameAddress nameAddress, final String s) {
		super("To", nameAddress, s);
	}

	public ToHeader(final SipURL sipURL) {
		super("To", sipURL);
	}

	public ToHeader(final SipURL sipURL, final String s) {
		super("To", sipURL, s);
	}

	public ToHeader(final Header header) {
		super(header);
	}
}
