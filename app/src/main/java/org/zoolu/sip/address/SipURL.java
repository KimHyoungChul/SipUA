package org.zoolu.sip.address;

import org.zoolu.sip.provider.SipParser;
import org.zoolu.tools.Parser;

import java.util.Vector;

public class SipURL {
	protected static final String lr_param = "lr";
	protected static final String maddr_param = "maddr";
	protected static final String transport_param = "transport";
	protected static final String ttl_param = "ttl";
	protected String url;

	public SipURL(final String s) {
		if (s.startsWith("sip:")) {
			this.url = new String(s);
			return;
		}
		this.url = "sip:" + s;
	}

	public SipURL(final String s, final int n) {
		this.init(null, s, n);
	}

	public SipURL(final String s, final String s2) {
		this.init(s, s2, -1);
	}

	public SipURL(final String s, final String s2, final int n) {
		this.init(s, s2, n);
	}

	private void init(final String s, final String s2, final int n) {
		final StringBuffer sb = new StringBuffer("sip:");
		if (s != null) {
			sb.append(s);
			if (s.indexOf(64) < 0) {
				sb.append('@');
				sb.append(s2);
			}
		} else {
			sb.append(s2);
		}
		if (n > 0 && (s == null || s.indexOf(58) < 0)) {
			sb.append(":" + n);
		}
		this.url = sb.toString();
	}

	public void addLr() {
		this.addParameter("lr");
	}

	public void addMaddr(final String s) {
		this.addParameter("maddr", s);
	}

	public void addParameter(final String s) {
		this.url = String.valueOf(this.url) + ";" + s;
	}

	public void addParameter(final String s, final String s2) {
		if (s2 != null) {
			this.url = String.valueOf(this.url) + ";" + s + "=" + s2;
			return;
		}
		this.url = String.valueOf(this.url) + ";" + s;
	}

	public void addTransport(final String s) {
		this.addParameter("transport", s.toLowerCase());
	}

	public void addTtl(final int n) {
		this.addParameter("ttl", Integer.toString(n));
	}

	public Object clone() {
		return new SipURL(this.url);
	}

	@Override
	public boolean equals(final Object o) {
		return this.url.toString().equals(((SipURL) o).toString());
	}

	public boolean equals(final SipURL sipURL) {
		return this.url == sipURL.url;
	}

	public String getHost() {
		final Parser parser = new Parser(this.url);
		final int index = parser.indexOf('@');
		int pos;
		if (index < 0) {
			pos = 4;
		} else {
			pos = index + 1;
		}
		parser.setPos(pos);
		final int index2 = parser.indexOf(new char[]{':', ';', '?'});
		if (index2 < 0) {
			return this.url.substring(pos);
		}
		return this.url.substring(pos, index2);
	}

	public String getMaddr() {
		return this.getParameter("maddr");
	}

	public String getParameter(final String s) {
		return ((SipParser) new SipParser(this.url).goTo(';').skipChar()).getParameter(s);
	}

	public Vector<String> getParameters() {
		return ((SipParser) new SipParser(this.url).goTo(';').skipChar()).getParameters();
	}

	public int getPort() {
		final Parser parser = new Parser(this.url, 4);
		final int index = parser.indexOf(':');
		if (index < 0) {
			return -1;
		}
		final int pos = index + 1;
		parser.setPos(pos);
		final int index2 = parser.indexOf(new char[]{';', '?'});
		if (index2 < 0) {
			return Integer.parseInt(this.url.substring(pos));
		}
		return Integer.parseInt(this.url.substring(pos, index2));
	}

	public String getTransport() {
		return this.getParameter("transport");
	}

	public int getTtl() {
		try {
			return Integer.parseInt(this.getParameter("ttl"));
		} catch (Exception ex) {
			return 1;
		}
	}

	public String getUserName() {
		final int index = this.url.indexOf(64, 4);
		if (index < 0) {
			return null;
		}
		return this.url.substring(4, index);
	}

	public boolean hasLr() {
		return this.hasParameter("lr");
	}

	public boolean hasMaddr() {
		return this.hasParameter("maddr");
	}

	public boolean hasParameter(final String s) {
		return ((SipParser) new SipParser(this.url).goTo(';').skipChar()).hasParameter(s);
	}

	public boolean hasParameters() {
		return this.url != null && this.url.indexOf(59) >= 0;
	}

	public boolean hasPort() {
		return this.getPort() >= 0;
	}

	public boolean hasTransport() {
		return this.hasParameter("transport");
	}

	public boolean hasTtl() {
		return this.hasParameter("ttl");
	}

	public boolean hasUserName() {
		return this.getUserName() != null;
	}

	public void removeParameter(String substring) {
		final int index = this.url.indexOf(59);
		if (index >= 0) {
			final Parser parser = new Parser(this.url, index);
			while (parser.hasMore()) {
				final int pos = parser.getPos();
				parser.skipChar();
				if (parser.getWord(SipParser.param_separators).equals(substring)) {
					final String substring2 = this.url.substring(0, pos);
					parser.goToSkippingQuoted(';');
					substring = "";
					if (parser.hasMore()) {
						substring = this.url.substring(parser.getPos());
					}
					this.url = substring2.concat(substring);
					return;
				}
				parser.goTo(';');
			}
		}
	}

	public void removeParameters() {
		final int index = this.url.indexOf(59);
		if (index >= 0) {
			this.url = this.url.substring(0, index);
		}
	}

	@Override
	public String toString() {
		return this.url;
	}
}
