package org.zoolu.sdp;

import org.zoolu.tools.Parser;

public class TimeField extends SdpField {
	public TimeField() {
		super('t', "0 0");
	}

	public TimeField(final String s) {
		super('t', s);
	}

	public TimeField(final String s, final String s2) {
		super('t', String.valueOf(s) + " " + s2);
	}

	public TimeField(final SdpField sdpField) {
		super(sdpField);
	}

	public String getStartTime() {
		return new Parser(this.value).getString();
	}

	public String getStopTime() {
		return new Parser(this.value).skipString().getString();
	}
}
