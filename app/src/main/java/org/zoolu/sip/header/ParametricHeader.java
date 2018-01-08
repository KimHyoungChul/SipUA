package org.zoolu.sip.header;

import org.zoolu.sip.provider.SipParser;
import org.zoolu.tools.Parser;

import java.util.Vector;

public abstract class ParametricHeader extends Header {
	protected ParametricHeader(final String s, final String s2) {
		super(s, s2);
	}

	protected ParametricHeader(final Header header) {
		super(header);
	}

	public String getParameter(final String s) {
		final int indexOfFirstSemi = this.indexOfFirstSemi();
		if (indexOfFirstSemi < 0) {
			return null;
		}
		return new SipParser(new Parser(this.getValue(), indexOfFirstSemi).skipChar().skipWSP()).getParameter(s);
	}

	public Vector<String> getParameterNames() {
		final int indexOfFirstSemi = this.indexOfFirstSemi();
		if (indexOfFirstSemi < 0) {
			return new Vector<String>();
		}
		return new SipParser(new Parser(this.getValue(), indexOfFirstSemi).skipChar().skipWSP()).getParameters();
	}

	public boolean hasParameter(final String s) {
		final int indexOfFirstSemi = this.indexOfFirstSemi();
		return indexOfFirstSemi >= 0 && new SipParser(new Parser(this.getValue(), indexOfFirstSemi).skipChar().skipWSP()).hasParameter(s);
	}

	public boolean hasParameters() {
		return this.indexOfFirstSemi() >= 0;
	}

	protected int indexOfFirstSemi() {
		int pos;
		if ((pos = new Parser(this.value).goToSkippingQuoted(';').getPos()) >= this.value.length()) {
			pos = -1;
		}
		return pos;
	}

	public void removeParameter(String substring) {
		final int indexOfFirstSemi = this.indexOfFirstSemi();
		if (indexOfFirstSemi >= 0) {
			final String value = this.getValue();
			final Parser parser = new Parser(value, indexOfFirstSemi);
			while (parser.hasMore()) {
				final int pos = parser.getPos();
				parser.skipChar();
				if (parser.getWord(SipParser.param_separators).equals(substring)) {
					final String substring2 = value.substring(0, pos);
					parser.goToSkippingQuoted(';');
					substring = "";
					if (parser.hasMore()) {
						substring = value.substring(parser.getPos());
					}
					this.setValue(substring2.concat(substring));
					return;
				}
				parser.goTo(';');
			}
		}
	}

	public void removeParameters() {
		if (!this.hasParameters()) {
			return;
		}
		final String value = this.getValue();
		this.setValue(value.substring(0, value.indexOf(59)));
	}

	public void setParameter(String value, final String s) {
		if (this.getValue() == null) {
			this.setValue("");
		}
		if (this.hasParameter(value)) {
			this.removeParameter(value);
		}
		final String s2 = value = this.getValue().concat(";" + value);
		if (s != null) {
			value = s2.concat("=" + s);
		}
		this.setValue(value);
	}
}
