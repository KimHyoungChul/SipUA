package org.zoolu.sdp;

import org.zoolu.tools.Parser;

public class OriginField extends SdpField {
	public OriginField(final String s) {
		super('o', s);
	}

	public OriginField(final String s, final String s2, final String s3, final String s4) {
		super('o', String.valueOf(s) + " " + s2 + " " + s3 + " IN IP4 " + s4);
	}

	public OriginField(final String s, final String s2, final String s3, final String s4, final String s5) {
		super('o', String.valueOf(s) + " " + s2 + " " + s3 + " IN " + s4 + " " + s5);
	}

	public OriginField(final SdpField sdpField) {
		super(sdpField);
	}

	public String getAddress() {
		return new Parser(this.value).skipString().skipString().skipString().skipString().skipString().getString();
	}

	public String getAddressType() {
		return new Parser(this.value).skipString().skipString().skipString().skipString().getString();
	}

	public String getSessionId() {
		return new Parser(this.value).skipString().getString();
	}

	public String getSessionVersion() {
		return new Parser(this.value).skipString().skipString().getString();
	}

	public String getUserName() {
		return new Parser(this.value).getString();
	}
}
