package org.zoolu.sip.header;

public class PttAttributeHeader extends Header {
	public PttAttributeHeader(final String s) {
		super("Ptt-Attribute", s);
	}

	public PttAttributeHeader(final Header header) {
		super(header);
	}

	public static String getString(final String s, final String s2) {
		final StringBuilder sb = new StringBuilder();
		sb.append("version=");
		sb.append('\"');
		sb.append(s);
		sb.append('\"');
		sb.append(",");
		sb.append("thid=");
		sb.append('\"');
		sb.append(s2);
		sb.append('\"');
		return sb.toString();
	}
}
