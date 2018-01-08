package org.zoolu.sdp;

import org.zoolu.tools.Parser;

import java.util.Vector;

public class MediaField extends SdpField {
	public MediaField(final String s) {
		super('m', s);
	}

	public MediaField(final String s, final int n, final int n2, final String s2, final String s3) {
		super('m', null);
		this.value = String.valueOf(s) + " " + n;
		if (n2 > 0) {
			this.value = String.valueOf(this.value) + "/" + n2;
		}
		this.value = String.valueOf(this.value) + " " + s2 + " " + s3;
	}

	public MediaField(final String s, int i, final int n, final String s2, final Vector<String> vector) {
		super('m', null);
		this.value = String.valueOf(s) + " " + i;
		if (n > 0) {
			this.value = String.valueOf(this.value) + "/" + n;
		}
		this.value = String.valueOf(this.value) + " " + s2;
		for (i = 0; i < vector.size(); ++i) {
			this.value = String.valueOf(this.value) + " " + vector.elementAt(i);
		}
	}

	public MediaField(final SdpField sdpField) {
		super(sdpField);
	}

	public Vector<String> getFormatList() {
		final Vector<String> vector = new Vector<String>();
		final Parser parser = new Parser(this.value);
		parser.skipString().skipString().skipString();
		while (parser.hasMore()) {
			final String string = parser.getString();
			if (string != null && string.length() > 0) {
				vector.addElement(string);
			}
		}
		return vector;
	}

	public String getFormats() {
		return new Parser(this.value).skipString().skipString().skipString().skipWSP().getRemainingString();
	}

	public String getMedia() {
		return new Parser(this.value).getString();
	}

	public int getPort() {
		final String string = new Parser(this.value).skipString().getString();
		final int index = string.indexOf(47);
		if (index < 0) {
			return Integer.parseInt(string);
		}
		return Integer.parseInt(string.substring(0, index));
	}

	public String getTransport() {
		return new Parser(this.value).skipString().skipString().getString();
	}
}
