package org.zoolu.sip.header;

import org.zoolu.sip.provider.SipParser;

import java.util.Vector;

public class AuthenticationInfoHeader extends AuthenticationHeader {
	public AuthenticationInfoHeader() {
		super("Authentication-Info", "");
	}

	public AuthenticationInfoHeader(final String s) {
		super("Authentication-Info", s);
	}

	public AuthenticationInfoHeader(final Vector<String> vector) {
		super("Authentication-Info", "", vector);
	}

	public AuthenticationInfoHeader(final Header header) {
		super(header);
	}

	@Override
	public String getAuthScheme() {
		return null;
	}

	@Override
	public String getParameter(final String s) {
		final SipParser sipParser = new SipParser(this.value);
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

	@Override
	public Vector<String> getParameters() {
		final SipParser sipParser = new SipParser(this.value);
		sipParser.skipWSPCRLF();
		final Vector<String> vector = new Vector<String>();
		while (sipParser.hasMore()) {
			vector.addElement(sipParser.getWord(new char[]{'=', ' ', '\t'}));
			sipParser.goToCommaHeaderSeparator().skipChar().skipWSPCRLF();
		}
		return vector;
	}

	@Override
	public boolean hasParameter(final String s) {
		final SipParser sipParser = new SipParser(this.value);
		sipParser.skipWSPCRLF();
		while (sipParser.hasMore()) {
			if (sipParser.getWord(new char[]{'=', ' ', '\t', '\r', '\n'}).equals(s)) {
				return true;
			}
			sipParser.goToCommaHeaderSeparator().skipChar().skipWSPCRLF();
		}
		return false;
	}
}
