package org.zoolu.sdp;

public class AttributeField extends SdpField {
	public AttributeField(final String s) {
		super('a', s);
	}

	public AttributeField(final String s, final String s2) {
		super('a', String.valueOf(s) + ":" + s2);
	}

	public AttributeField(final SdpField sdpField) {
		super(sdpField);
	}

	public String getAttributeName() {
		final int index = this.value.indexOf(":");
		if (index < 0) {
			return this.value;
		}
		return this.value.substring(0, index);
	}

	public String getAttributeValue() {
		final int index = this.value.indexOf(":");
		if (index < 0) {
			return null;
		}
		return this.value.substring(index + 1);
	}
}
