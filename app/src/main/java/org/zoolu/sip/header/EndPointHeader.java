package org.zoolu.sip.header;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.provider.SipParser;

public abstract class EndPointHeader extends NameAddressHeader {
	static final String[] ENDPOINT_PARAMS;

	static {
		ENDPOINT_PARAMS = new String[]{"tag", "expires"};
	}

	public EndPointHeader(final String s, final NameAddress nameAddress) {
		super(s, nameAddress);
	}

	public EndPointHeader(final String s, final NameAddress nameAddress, final String s2) {
		super(s, nameAddress);
		if (s2 != null) {
			this.setParameter("tag", s2);
		}
	}

	public EndPointHeader(final String s, final SipURL sipURL) {
		super(s, sipURL);
	}

	public EndPointHeader(final String s, final SipURL sipURL, final String s2) {
		super(s, sipURL);
		if (s2 != null) {
			this.setParameter("tag", s2);
		}
	}

	public EndPointHeader(final Header header) {
		super(header);
	}

	@Override
	public NameAddress getNameAddress() {
		NameAddress nameAddress = new SipParser(this.value).getNameAddress();
		final SipURL address = nameAddress.getAddress();
		NameAddress nameAddress2;
		for (int i = 0; i < EndPointHeader.ENDPOINT_PARAMS.length; ++i, nameAddress = nameAddress2) {
			nameAddress2 = nameAddress;
			if (address.hasParameter(EndPointHeader.ENDPOINT_PARAMS[i])) {
				address.removeParameter(EndPointHeader.ENDPOINT_PARAMS[i]);
				nameAddress2 = new NameAddress(nameAddress.getDisplayName(), address);
			}
		}
		return nameAddress;
	}

	public String getTag() {
		return this.getParameter("tag");
	}

	public boolean hasTag() {
		return this.hasParameter("tag");
	}
}
