package org.zoolu.sip.header;

public abstract class OptionHeader extends Header {
	public OptionHeader(final String s, final String s2) {
		super(s, s2);
	}

	public OptionHeader(final Header header) {
		super(header);
	}

	public String getOption() {
		return this.value;
	}

	public void setOption(final String value) {
		this.value = value;
	}
}
