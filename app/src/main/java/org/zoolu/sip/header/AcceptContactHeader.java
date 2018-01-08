package org.zoolu.sip.header;

public class AcceptContactHeader extends ParametricHeader {
	public AcceptContactHeader() {
		super("Accept-Contact", "*");
	}

	public AcceptContactHeader(final String s) {
		super("Accept-Contact", "*");
		if (s != null) {
			this.setParameter("+g.3gpp.icsi-ref", s);
		}
	}

	public AcceptContactHeader(final Header header) {
		super(header);
	}
}
