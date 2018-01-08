package org.zoolu.sip.header;

public class AlertInfoHeader extends ParametricHeader {
	public AlertInfoHeader(final String absoluteURI) {
		super("Alert-Info", null);
		this.setAbsoluteURI(absoluteURI);
	}

	public AlertInfoHeader(final Header header) {
		super(header);
	}

	public String getAbsoluteURI() {
		final int index = this.value.indexOf("<");
		final int index2 = this.value.indexOf(">");
		int n;
		if (index < 0) {
			n = 0;
		} else {
			n = index + 1;
		}
		int length = index2;
		if (index2 < 0) {
			length = this.value.length();
		}
		return this.value.substring(n, length);
	}

	public void setAbsoluteURI(String s) {
		final String s2 = s = s.trim();
		if (s2.indexOf("<") < 0) {
			s = "<" + s2;
		}
		String string = s;
		if (s.indexOf(">") < 0) {
			string = String.valueOf(s) + ">";
		}
		this.value = string;
	}
}
