package org.zoolu.sdp;

public class SdpField {
	char type;
	String value;

	public SdpField(final char type, final String value) {
		this.type = type;
		this.value = value;
	}

	public SdpField(final String s) {
		final SdpField sdpField = new SdpParser(s).parseSdpField();
		this.type = sdpField.type;
		this.value = sdpField.value;
	}

	public SdpField(final SdpField sdpField) {
		this.type = sdpField.type;
		this.value = sdpField.value;
	}

	public Object clone() {
		return new SdpField(this);
	}

	@Override
	public boolean equals(final Object o) {
		try {
			final SdpField sdpField = (SdpField) o;
			if (this.type != sdpField.type) {
				return false;
			}
			if (this.value == sdpField.value) {
				return true;
			}
		} catch (Exception ex) {
		}
		return false;
	}

	public char getType() {
		return this.type;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return String.valueOf(this.type) + "=" + this.value + "\r\n";
	}
}
