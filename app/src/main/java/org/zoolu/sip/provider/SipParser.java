package org.zoolu.sip.provider;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.message.Message;
import org.zoolu.tools.DateFormat;
import org.zoolu.tools.Parser;

import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SipParser extends Parser {
	public static char[] MARK;
	public static char[] SEPARATOR;
	public static char[] param_separators;
	public static char[] uri_separators;

	static {
		SipParser.MARK = new char[]{'-', '_', '.', '!', '~', '*', '\'', '|'};
		SipParser.SEPARATOR = new char[]{' ', '\t', '\r', '\n', '(', ')', '<', '>', ',', ';', '\\', '\"', '/', '[', ']', '?', '=', '{', '}'};
		SipParser.uri_separators = new char[]{' ', '>', '\n', '\r'};
		SipParser.param_separators = new char[]{' ', '=', ';', ',', '\n', '\r'};
	}

	public SipParser(final String s) {
		super(s);
	}

	public SipParser(final String s, final int n) {
		super(s, n);
	}

	public SipParser(final StringBuffer sb) {
		super(sb);
	}

	public SipParser(final StringBuffer sb, final int n) {
		super(sb, n);
	}

	public SipParser(final Parser parser) {
		super(parser.getWholeString(), parser.getPos());
	}

	public static boolean isMark(final char c) {
		return Parser.isAnyOf(SipParser.MARK, c);
	}

	public static boolean isSeparator(final char c) {
		return Parser.isAnyOf(SipParser.SEPARATOR, c);
	}

	public static boolean isUnreserved(final char c) {
		return Parser.isAlphanum(c) || isMark(c);
	}

	public Date getDate() {
		try {
			final Date eeEddMMM = DateFormat.parseEEEddMMM(this.str, this.index);
			this.index = this.str.indexOf("GMT", this.index) + 3;
			return eeEddMMM;
		} catch (Exception ex) {
			ex.printStackTrace();
			this.index = this.str.length();
			return null;
		}
	}

	public Header getHeader() {
		if (this.hasMore()) {
			final String string = this.getString(this.indexOfEOH() - this.getPos());
			this.goToNextLine();
			final int index = string.indexOf(58);
			if (index >= 0) {
				return new Header(string.substring(0, index).trim(), string.substring(index + 1).trim());
			}
		}
		return null;
	}

	public Header getHeader(final String s) {
		final SipParser sipParser = new SipParser(this.str, this.indexOfHeader(s));
		if (sipParser.hasMore()) {
			sipParser.skipN(s.length());
			final int n = sipParser.indexOf(':') + 1;
			final int indexOfEOH = sipParser.indexOfEOH();
			if (n <= indexOfEOH) {
				final String trim = this.str.substring(n, indexOfEOH).trim();
				this.index = indexOfEOH;
				return new Header(s, trim);
			}
		}
		return null;
	}

	public NameAddress getNameAddress() {
		final int pos = this.getPos();
		final int index = this.indexOf("<sip:");
		if (index < 0) {
			SipURL sipURL;
			if ((sipURL = this.getSipURL()) == null) {
				this.setPos(pos);
				sipURL = new SipURL(this.getString());
			}
			return new NameAddress(sipURL);
		}
		final String trim = this.getString(index - pos).trim();
		final SipURL sipURL2 = this.getSipURL();
		String substring = trim;
		if (trim.length() > 0) {
			substring = trim;
			if (trim.charAt(0) == '\"') {
				substring = trim;
				if (trim.charAt(trim.length() - 1) == '\"') {
					substring = trim.substring(1, trim.length() - 1);
				}
			}
		}
		if (substring.length() == 0) {
			return new NameAddress(sipURL2);
		}
		return new NameAddress(substring, sipURL2);
	}

	public String getParameter(final String s) {
		while (this.hasMore()) {
			if (this.getWord(SipParser.param_separators).equals(s)) {
				this.skipWSP();
				if (this.nextChar() == '=') {
					this.skipChar();
					return this.getWordSkippingQuoted(SipParser.param_separators);
				}
				break;
			} else {
				this.goToSkippingQuoted(';');
				if (!this.hasMore()) {
					continue;
				}
				this.skipChar();
			}
		}
		return null;
	}

	public Vector<String> getParameters() {
		final Vector<String> vector = new Vector<String>();
		while (this.hasMore()) {
			final String word = this.getWord(SipParser.param_separators);
			if (word.length() > 0) {
				vector.addElement(new String(word));
			}
			this.goToSkippingQuoted(';');
			if (this.hasMore()) {
				this.skipChar();
			}
		}
		return vector;
	}

	public RequestLine getRequestLine() {
		final String string = this.getString();
		this.skipWSP();
		final String string2 = this.getString(this.indexOfEOH() - this.getPos());
		this.goToNextLine();
		return new RequestLine(string, new SipParser(string2).getSipURL());
	}

	public Message getSipMessage() {
		this.skipCRLF();
		String s;
		if (this.getPos() == 0) {
			s = this.str;
		} else {
			s = this.getRemainingString();
		}
		final Message message = new Message(s);
		if (message.hasContentLengthHeader()) {
			final int contentLength = message.getContentLengthHeader().getContentLength();
			final int pos = this.getPos();
			this.goToEndOfLastHeader();
			if (this.hasMore()) {
				this.goTo('\n');
				if (this.hasMore()) {
					this.skipChar().goTo('\n');
					if (this.hasMore()) {
						final int index = this.skipChar().getPos() + contentLength;
						if (index <= this.str.length()) {
							this.index = index;
							return new Message(this.str.substring(pos, index));
						}
					}
				}
			}
		}
		return null;
	}

	public SipURL getSipURL() {
		this.goTo("sip:");
		if (!this.hasMore()) {
			return null;
		}
		final int pos = this.getPos();
		int n;
		if ((n = this.indexOf(SipParser.uri_separators)) < 0) {
			n = this.str.length();
		}
		final String string = this.getString(n - pos);
		if (this.hasMore()) {
			this.skipChar();
		}
		return new SipURL(string);
	}

	public StatusLine getStatusLine() {
		if (!this.getString(4).equalsIgnoreCase("SIP/")) {
			this.index = this.str.length();
			return null;
		}
		this.skipString().skipWSP();
		final int int1 = this.getInt();
		final String trim = this.getString(this.indexOfEOH() - this.getPos()).trim();
		this.goToNextLine();
		return new StatusLine(int1, trim);
	}

	public SipParser goToBody() {
		this.goToEndOfLastHeader();
		this.goTo('\n').skipChar();
		this.goTo('\n').skipChar();
		return this;
	}

	public SipParser goToCommaHeaderSeparator() {
		final int indexOfCommaHeaderSeparator = this.indexOfCommaHeaderSeparator();
		if (indexOfCommaHeaderSeparator < 0) {
			this.index = this.str.length();
			return this;
		}
		this.index = indexOfCommaHeaderSeparator;
		return this;
	}

	public SipParser goToEndOfLastHeader() {
		this.goTo(new String[]{"\r\n\r\n", "\n\n"});
		if (!this.hasMore()) {
			if (this.str.startsWith("\r\n", this.str.length() - 2)) {
				this.index = this.str.length() - 2;
			} else {
				if (this.str.charAt(this.str.length() - 1) == '\n') {
					this.index = this.str.length() - 1;
					return this;
				}
				this.index = this.str.length();
				return this;
			}
		}
		return this;
	}

	public SipParser goToNextHeader() {
		this.index = this.indexOfEOH();
		this.goToNextLine();
		return this;
	}

	public boolean hasParameter(final String s) {
		while (this.hasMore()) {
			if (this.getWord(SipParser.param_separators).equals(s)) {
				return true;
			}
			this.goToSkippingQuoted(';');
			if (!this.hasMore()) {
				continue;
			}
			this.skipChar();
		}
		return false;
	}

	public int indexOfCommaHeaderSeparator() {
		int n = 0;
		int n2;
		for (int i = this.index; i < this.str.length(); ++i, n = n2) {
			final char char1 = this.str.charAt(i);
			n2 = n;
			if (char1 == '\"') {
				if (n != 0) {
					n2 = 0;
				} else {
					n2 = 1;
				}
			}
			if (n2 == 0) {
				final int n3 = i;
				if (char1 == ',') {
					return n3;
				}
			}
		}
		return -1;
	}

	public int indexOfEOH() {
		final SipParser sipParser = new SipParser(this.str, this.index);
		int pos;
		do {
			sipParser.goTo(SipParser.CRLF);
			int length;
			if (!sipParser.hasMore()) {
				length = this.str.length();
			} else {
				pos = sipParser.getPos();
				sipParser.goToNextLine();
				length = pos;
				if (sipParser.hasMore()) {
					continue;
				}
			}
			return length;
		} while (Parser.isWSP(sipParser.nextChar()));
		return pos;
	}

	public int indexOfHeader(String hname) {
		Matcher m = Pattern.compile("^" + hname + ": ", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(this.str);
		if (m.find(this.index)) {
			return m.start();
		}
		return this.str.length();
	}

	public int indexOfNextHeader() {
		final SipParser sipParser = new SipParser(this.str, this.index);
		sipParser.goToNextHeader();
		return sipParser.getPos();
	}

	public int indexOfSeparator() {
		int index;
		for (index = this.index; index < this.str.length() && !isSeparator(this.str.charAt(index)); ++index) {
		}
		return index;
	}
}
