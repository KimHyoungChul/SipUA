package org.zoolu.sip.address;

import org.zoolu.sip.provider.SipParser;

public class NameAddress {
	String name;
	SipURL url;

	public NameAddress(final String s) {
		final NameAddress nameAddress = new SipParser(s).getNameAddress();
		this.name = nameAddress.name;
		this.url = nameAddress.url;
	}

	public NameAddress(final String name, final SipURL url) {
		this.name = name;
		this.url = url;
	}

	public NameAddress(final NameAddress nameAddress) {
		this.name = nameAddress.getDisplayName();
		this.url = nameAddress.getAddress();
	}

	public NameAddress(final SipURL url) {
		this.name = null;
		this.url = url;
	}

	public Object clone() {
		return new NameAddress(this);
	}

	@Override
	public boolean equals(final Object o) {
		return this.url.equals(((NameAddress) o).getAddress());
	}

	public boolean equals(final NameAddress nameAddress) {
		return this.name == nameAddress.name && this.url == nameAddress.url;
	}

	public SipURL getAddress() {
		return this.url;
	}

	public String getDisplayName() {
		return this.name;
	}

	public boolean hasDisplayName() {
		return this.name != null;
	}

	public void removeDisplayName() {
		this.name = null;
	}

	public void setAddress(final SipURL url) {
		this.url = url;
	}

	public void setDisplayName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		if (this.hasDisplayName()) {
			return "\"" + this.name + "\" <" + this.url + ">";
		}
		return "<" + this.url + ">";
	}
}
