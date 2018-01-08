package org.zoolu.sip.header;

public class Header {
	protected String name;
	protected String value;

	protected Header() {
		this.name = null;
		this.value = null;
	}

	public Header(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	public Header(final Header header) {
		this.name = header.getName();
		this.value = header.getValue();
	}

	public Object clone() {
		return new Header(this.getName(), this.getValue());
	}

	@Override
	public boolean equals(final Object o) {
		final boolean b = false;
		try {
			final Header header = (Header) o;
			boolean b2 = b;
			if (header.getName().equals(this.getName())) {
				final boolean equals = header.getValue().equals(this.getValue());
				b2 = b;
				if (equals) {
					b2 = true;
				}
			}
			return b2;
		} catch (Exception ex) {
			return false;
		}
	}

	public String getName() {
		return this.name;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(this.name) + ": " + this.value + "\r\n";
	}
}
