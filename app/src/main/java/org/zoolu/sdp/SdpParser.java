package org.zoolu.sdp;

import org.zoolu.tools.Parser;

import java.util.Vector;

class SdpParser extends Parser {
	public SdpParser(final String s) {
		super(s);
	}

	public SdpParser(final String s, final int n) {
		super(s, n);
	}

	public AttributeField parseAttributeField() {
		final SdpField sdpField = this.parseSdpField('a');
		if (sdpField != null) {
			return new AttributeField(sdpField);
		}
		return null;
	}

	public ConnectionField parseConnectionField() {
		final SdpField sdpField = this.parseSdpField('c');
		if (sdpField != null) {
			return new ConnectionField(sdpField);
		}
		return null;
	}

	public MediaDescriptor parseMediaDescriptor() {
		final MediaField mediaField = this.parseMediaField();
		if (mediaField == null) {
			return null;
		}
		final int index = this.index;
		final int index2 = this.str.indexOf("\nm", index);
		int length;
		if (index2 < 0) {
			length = this.str.length();
		} else {
			length = index2 + 1;
		}
		this.index = length;
		final SdpParser sdpParser = new SdpParser(this.str.substring(index, length));
		final ConnectionField connectionField = sdpParser.parseConnectionField();
		final Vector<AttributeField> vector = new Vector<AttributeField>();
		for (AttributeField attributeField = sdpParser.parseAttributeField(); attributeField != null; attributeField = sdpParser.parseAttributeField()) {
			vector.addElement(attributeField);
		}
		return new MediaDescriptor(mediaField, connectionField, vector);
	}

	public MediaField parseMediaField() {
		final SdpField sdpField = this.parseSdpField('m');
		if (sdpField != null) {
			return new MediaField(sdpField);
		}
		return null;
	}

	public OriginField parseOriginField() {
		final SdpField sdpField = this.parseSdpField('o');
		if (sdpField != null) {
			return new OriginField(sdpField);
		}
		return null;
	}

	public SdpField parseSdpField() {
		int n;
		for (n = this.index; n >= 0 && n < this.str.length() - 1 && this.str.charAt(n + 1) != '='; n = this.str.indexOf("\n", n)) {
		}
		if (n >= 0) {
			final char char1 = this.str.charAt(n);
			final int n2 = n + 2;
			final int length = this.str.length();
			final int index = this.str.indexOf(13, n2);
			int n3 = length;
			if (index > 0 && index < (n3 = length)) {
				n3 = index;
			}
			final int index2 = this.str.indexOf(10, n2);
			int pos = n3;
			if (index2 > 0 && index2 < (pos = n3)) {
				pos = index2;
			}
			final String trim = this.str.substring(n2, pos).trim();
			if (trim != null) {
				this.setPos(pos);
				this.goToNextLine();
				return new SdpField(char1, trim);
			}
		}
		return null;
	}

	public SdpField parseSdpField(final char c) {
		if (!this.str.startsWith(String.valueOf(c) + "=", this.index)) {
			final int index = this.str.indexOf("\n" + c + "=", this.index);
			if (index < 0) {
				return null;
			}
			this.index = index + 1;
		}
		return this.parseSdpField();
	}

	public SessionNameField parseSessionNameField() {
		final SdpField sdpField = this.parseSdpField('s');
		if (sdpField != null) {
			return new SessionNameField(sdpField);
		}
		return null;
	}

	public TimeField parseTimeField() {
		final SdpField sdpField = this.parseSdpField('t');
		if (sdpField != null) {
			return new TimeField(sdpField);
		}
		return null;
	}
}
