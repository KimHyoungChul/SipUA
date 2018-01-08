package org.zoolu.sdp;

import org.zoolu.tools.Parser;

public class ConnectionField extends SdpField {
	public ConnectionField(final String s) {
		super('c', s);
	}

	public ConnectionField(final String s, final String s2) {
		super('c', "IN " + s + " " + s2);
	}

	public ConnectionField(final String s, final String s2, final int n, final int n2) {
		super('c', null);
		this.value = "IN " + s + " " + s2;
		if (n > 0) {
			this.value = String.valueOf(this.value) + "/" + n;
		}
		if (n2 > 0) {
			this.value = String.valueOf(this.value) + "/" + n2;
		}
	}

	public ConnectionField(final SdpField sdpField) {
		super(sdpField);
	}

	public String getAddress() {
		final String string = new Parser(this.value).skipString().skipString().getString();
		final int index = string.indexOf("/");
		if (index < 0) {
			return string;
		}
		return string.substring(0, index);
	}

	public String getAddressType() {
		return new Parser(this.value).skipString().getString();
	}

	public int getNum() {
		final String string = new Parser(this.value).skipString().skipString().getString();
		final int index = string.indexOf("/");
		if (index >= 0) {
			final int index2 = string.indexOf("/", index);
			if (index2 >= 0) {
				return Integer.parseInt(string.substring(index2));
			}
		}
		return 0;
	}

	public int getTTL() {
		final String string = new Parser(this.value).skipString().skipString().getString();
		final int index = string.indexOf("/");
		if (index < 0) {
			return 0;
		}
		final int index2 = string.indexOf("/", index);
		if (index2 < 0) {
			return Integer.parseInt(string.substring(index));
		}
		return Integer.parseInt(string.substring(index, index2));
	}
}
