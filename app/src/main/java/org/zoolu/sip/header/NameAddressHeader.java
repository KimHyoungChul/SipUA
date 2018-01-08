package org.zoolu.sip.header;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.provider.SipParser;
import org.zoolu.tools.Parser;

public abstract class NameAddressHeader extends ParametricHeader {
	public NameAddressHeader(final String s, final NameAddress nameAddress) {
		super(s, nameAddress.toString());
	}

	public NameAddressHeader(final String s, final SipURL sipURL) {
		super(s, sipURL.toString());
	}

	public NameAddressHeader(final Header header) {
		super(header);
	}

	public NameAddress getNameAddress() {
		return new SipParser(this.value).getNameAddress();
	}

	@Override
	protected int indexOfFirstSemi() {
		final Parser parser = new Parser(this.value);
		parser.goToSkippingQuoted('>');
		if (parser.getPos() == this.value.length()) {
			parser.setPos(0);
		}
		parser.goToSkippingQuoted(';');
		if (parser.getPos() < this.value.length()) {
			return parser.getPos();
		}
		return -1;
	}

	public void setNameAddress(final NameAddress nameAddress) {
		this.value = nameAddress.toString();
	}
}
