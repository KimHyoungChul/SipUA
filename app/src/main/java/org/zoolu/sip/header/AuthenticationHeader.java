package org.zoolu.sip.header;

import org.zoolu.sip.provider.SipParser;

import java.util.Vector;

public abstract class AuthenticationHeader extends Header {
	public static String LWS_SEPARATOR;
	public static String[] QUOTED_PARAMETERS;

	static {
		AuthenticationHeader.LWS_SEPARATOR = " ";
		AuthenticationHeader.QUOTED_PARAMETERS = new String[]{"auts", "cnonce", "nextnonce", "nonce", "opaque", "realm", "response", "rspauth", "uri", "username"};
	}

	public AuthenticationHeader(final String s, final String s2) {
		super(s, s2);
	}

	public AuthenticationHeader(final String s, final String s2, final Vector<String> vector) {
		super(s, s2);
		if (vector.size() > 0) {
			this.value = String.valueOf(this.value) + " " + vector.elementAt(0);
		}
		for (int i = 1; i < vector.size(); ++i) {
			this.value = String.valueOf(this.value) + "," + AuthenticationHeader.LWS_SEPARATOR + vector.elementAt(i);
		}
	}

	public AuthenticationHeader(final Header header) {
		super(header);
	}

	private static boolean isQuotedParameter(final String s) {
		for (int i = 0; i < AuthenticationHeader.QUOTED_PARAMETERS.length; ++i) {
			if (s.equalsIgnoreCase(AuthenticationHeader.QUOTED_PARAMETERS[i])) {
				return true;
			}
		}
		return false;
	}

	public void addAlgorithParam(final String s) {
		this.addUnquotedParameter("algorithm", s);
	}

	public void addAutsParam(final String s) {
		this.addQuotedParameter("auts", s);
	}

	public void addCnonceParam(final String s) {
		this.addQuotedParameter("cnonce", s);
	}

	public void addNcParam(final String s) {
		this.addUnquotedParameter("nc", s);
	}

	public void addNextnonceParam(final String s) {
		this.addQuotedParameter("nextnonce", s);
	}

	public void addNonceParam(final String s) {
		this.addQuotedParameter("nonce", s);
	}

	public void addOpaqueParam(final String s) {
		this.addQuotedParameter("opaque", s);
	}

	public void addParameter(final String s, final String s2) {
		if (s2.indexOf(34) < 0 && isQuotedParameter(s)) {
			this.addQuotedParameter(s, s2);
			return;
		}
		this.addUnquotedParameter(s, s2);
	}

	public void addQopOptionsParam(final String s) {
		this.addQuotedParameter("qop", s);
	}

	public void addQopParam(final String s) {
		this.addUnquotedParameter("qop", s);
	}

	public void addQuotedParameter(final String s, final String s2) {
		if (this.value.indexOf(61) < 0) {
			this.value = String.valueOf(this.value) + " ";
		} else {
			this.value = String.valueOf(this.value) + "," + AuthenticationHeader.LWS_SEPARATOR;
		}
		if (s2.indexOf(34) >= 0) {
			this.value = String.valueOf(this.value) + s + "=" + s2;
			return;
		}
		this.value = String.valueOf(this.value) + s + "=\"" + s2 + "\"";
	}

	public void addRealmParam(final String s) {
		this.addQuotedParameter("realm", s);
	}

	public void addResponseParam(final String s) {
		this.addQuotedParameter("response", s);
	}

	public void addRspauthParam(final String s) {
		this.addQuotedParameter("rspauth", s);
	}

	public void addUnquotedParameter(final String s, final String s2) {
		if (this.value.indexOf(61) < 0) {
			this.value = String.valueOf(this.value) + " ";
		} else {
			this.value = String.valueOf(this.value) + "," + AuthenticationHeader.LWS_SEPARATOR;
		}
		this.value = String.valueOf(this.value) + s + "=" + s2;
	}

	public void addUriParam(final String s) {
		this.addQuotedParameter("uri", s);
	}

	public void addUsernameParam(final String s) {
		this.addQuotedParameter("username", s);
	}

	public String getAlgorithParam() {
		return this.getParameter("algorithm");
	}

	public String getAuthScheme() {
		return new SipParser(this.value).getString();
	}

	public String getAutsParam() {
		return this.getParameter("auts");
	}

	public String getCnonceParam() {
		return this.getParameter("cnonce");
	}

	public String getNcParam() {
		return this.getParameter("nc");
	}

	public String getNextnonceParam() {
		return this.getParameter("nextnonce");
	}

	public String getNonceParam() {
		return this.getParameter("nonce");
	}

	public String getOpaqueParam() {
		return this.getParameter("opaque");
	}

	public String getParameter(final String s) {
		final SipParser sipParser = new SipParser(this.value);
		sipParser.skipString();
		sipParser.skipWSPCRLF();
		while (sipParser.hasMore()) {
			if (sipParser.getWord(new char[]{'=', ' ', '\t'}).equals(s)) {
				sipParser.goTo('=').skipChar().skipWSP();
				final int indexOfCommaHeaderSeparator = sipParser.indexOfCommaHeaderSeparator();
				SipParser sipParser2 = sipParser;
				if (indexOfCommaHeaderSeparator >= 0) {
					sipParser2 = new SipParser(sipParser.getString(indexOfCommaHeaderSeparator - sipParser.getPos()));
				}
				return sipParser2.getStringUnquoted();
			}
			sipParser.goToCommaHeaderSeparator().skipChar().skipWSPCRLF();
		}
		return null;
	}

	public Vector<String> getParameters() {
		final SipParser sipParser = new SipParser(this.value);
		sipParser.skipString();
		sipParser.skipWSPCRLF();
		final Vector<String> vector = new Vector<String>();
		while (sipParser.hasMore()) {
			vector.addElement(sipParser.getWord(new char[]{'=', ' ', '\t'}));
			sipParser.goToCommaHeaderSeparator().skipChar().skipWSPCRLF();
		}
		return vector;
	}

	public String getQopOptionsParam() {
		return this.getParameter("qop");
	}

	public String getQopParam() {
		return this.getParameter("qop");
	}

	public String getRealmParam() {
		return this.getParameter("realm");
	}

	public String getResponseParam() {
		return this.getParameter("response");
	}

	public String getRspauthParam() {
		return this.getParameter("rspauth");
	}

	public String getUriParam() {
		return this.getParameter("uri");
	}

	public String getUsernameParam() {
		return this.getParameter("username");
	}

	public boolean hasAlgorithmParam() {
		return this.hasParameter("algorithm");
	}

	public boolean hasAutsParam() {
		return this.hasParameter("auts");
	}

	public boolean hasCnonceParam() {
		return this.hasParameter("cnonce");
	}

	public boolean hasNcParam() {
		return this.hasParameter("nc");
	}

	public boolean hasNextnonceParam() {
		return this.hasParameter("nextnonce");
	}

	public boolean hasNonceParam() {
		return this.hasParameter("nonce");
	}

	public boolean hasOpaqueParam() {
		return this.hasParameter("opaque");
	}

	public boolean hasParameter(final String s) {
		final SipParser sipParser = new SipParser(this.value);
		sipParser.skipString();
		sipParser.skipWSPCRLF();
		while (sipParser.hasMore()) {
			if (sipParser.getWord(new char[]{'=', ' ', '\t', '\r', '\n'}).equals(s)) {
				return true;
			}
			sipParser.goToCommaHeaderSeparator().skipChar().skipWSPCRLF();
		}
		return false;
	}

	public boolean hasQopOptionsParam() {
		return this.hasParameter("qop");
	}

	public boolean hasQopParam() {
		return this.hasParameter("qop");
	}

	public boolean hasRealmParam() {
		return this.hasParameter("realm");
	}

	public boolean hasResponseParam() {
		return this.hasParameter("response");
	}

	public boolean hasRspauthParam() {
		return this.hasParameter("rspauth");
	}

	public boolean hasUriParam() {
		return this.hasParameter("uri");
	}

	public boolean hasUsernameParam() {
		return this.hasParameter("username");
	}
}
