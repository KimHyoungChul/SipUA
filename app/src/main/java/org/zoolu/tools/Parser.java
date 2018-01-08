package org.zoolu.tools;

import java.util.Vector;

public class Parser {
	public static char[] CRLF;
	public static char[] SPACE;
	public static char[] WSP;
	public static char[] WSPCRLF;
	protected int index;
	protected String str;

	static {
		Parser.WSP = new char[]{' ', '\t'};
		Parser.SPACE = Parser.WSP;
		Parser.CRLF = new char[]{'\r', '\n'};
		Parser.WSPCRLF = new char[]{' ', '\t', '\r', '\n'};
	}

	public Parser(final String str) {
		if (str == null) {
			throw new RuntimeException("Tried to costruct a new Parser with a null String");
		}
		this.str = str;
		this.index = 0;
	}

	public Parser(final String str, final int index) {
		if (str == null) {
			throw new RuntimeException("Tried to costruct a new Parser with a null String");
		}
		this.str = str;
		this.index = index;
	}

	public Parser(final StringBuffer sb) {
		if (sb == null) {
			throw new RuntimeException("Tried to costruct a new Parser with a null StringBuffer");
		}
		this.str = sb.toString();
		this.index = 0;
	}

	public Parser(final StringBuffer sb, final int index) {
		if (sb == null) {
			throw new RuntimeException("Tried to costruct a new Parser with a null StringBuffer");
		}
		this.str = sb.toString();
		this.index = index;
	}

	public static int compareIgnoreCase(final char c, final char c2) {
		char c3 = c;
		if (isUpAlpha(c)) {
			c3 = (char) (c + ' ');
		}
		char c4 = c2;
		if (isUpAlpha(c2)) {
			c4 = (char) (c2 + ' ');
		}
		return c3 - c4;
	}

	public static boolean isAlpha(final char c) {
		return isUpAlpha(c) || isLowAlpha(c);
	}

	public static boolean isAlphanum(final char c) {
		return isAlpha(c) || isDigit(c);
	}

	public static boolean isAnyOf(final char[] array, final char c) {
		for (int i = 0; i < array.length; ++i) {
			if (array[i] == c) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCR(final char c) {
		return c == '\r';
	}

	public static boolean isCRLF(final char c) {
		return isAnyOf(Parser.CRLF, c);
	}

	public static boolean isChar(final char c) {
		return c > ' ' && c <= '~';
	}

	public static boolean isDigit(final char c) {
		return c >= '0' && c <= '9';
	}

	public static boolean isHT(final char c) {
		return c == '\t';
	}

	public static boolean isLF(final char c) {
		return c == '\n';
	}

	public static boolean isLowAlpha(final char c) {
		return c >= 'a' && c <= 'z';
	}

	public static boolean isSP(final char c) {
		return c == ' ';
	}

	public static boolean isUpAlpha(final char c) {
		return c >= 'A' && c <= 'Z';
	}

	public static boolean isWSP(final char c) {
		return isAnyOf(Parser.WSP, c);
	}

	public static boolean isWSPCRLF(final char c) {
		return isAnyOf(Parser.WSPCRLF, c);
	}

	public char charAt(final int n) {
		return this.str.charAt(this.index + n);
	}

	public char getChar() {
		return this.str.charAt(this.index++);
	}

	public double getDouble() {
		return Double.parseDouble(this.getString());
	}

	public int getInt() {
		return Integer.parseInt(this.getString());
	}

	public String getLine() {
		int index;
		for (index = this.index; index < this.str.length() && !isCRLF(this.str.charAt(index)); ++index) {
		}
		final String substring = this.str.substring(this.index, index);
		this.index = index;
		if (this.index < this.str.length()) {
			if (!this.str.startsWith("\r\n", this.index)) {
				++this.index;
				return substring;
			}
			this.index += 2;
		}
		return substring;
	}

	public int getPos() {
		return this.index;
	}

	public String getRemainingString() {
		return this.str.substring(this.index);
	}

	public String getString() {
		int index;
		for (index = this.index; index < this.str.length() && !isChar(this.str.charAt(index)); ++index) {
		}
		int index2;
		for (index2 = index; index2 < this.str.length() && isChar(this.str.charAt(index2)); ++index2) {
		}
		this.index = index2;
		return this.str.substring(index, index2);
	}

	public String getString(final int n) {
		final int index = this.index;
		this.index = index + n;
		return this.str.substring(index, this.index);
	}

	public String[] getStringArray() {
		final Vector<String> stringVector = this.getStringVector();
		final String[] array = new String[stringVector.size()];
		for (int i = 0; i < stringVector.size(); ++i) {
			array[i] = stringVector.elementAt(i);
		}
		return array;
	}

	public String getStringUnquoted() {
		while (this.index < this.str.length() && !isChar(this.str.charAt(this.index))) {
			++this.index;
		}
		if (this.index == this.str.length()) {
			return this.str.substring(this.index, this.index);
		}
		if (this.str.charAt(this.index) == '\"') {
			final int index = this.str.indexOf("\"", this.index + 1);
			if (index > 0) {
				final String substring = this.str.substring(this.index + 1, index);
				this.index = index + 1;
				return substring;
			}
		}
		return this.getString();
	}

	public Vector<String> getStringVector() {
		final Vector<String> vector = new Vector<String>();
		do {
			vector.addElement(this.getString());
		} while (this.hasMore());
		return vector;
	}

	public String getWholeString() {
		return this.str;
	}

	public String getWord(final char[] array) {
		int index;
		for (index = this.index; index < this.str.length() && isAnyOf(array, this.str.charAt(index)); ++index) {
		}
		int index2;
		for (index2 = index; index2 < this.str.length() && !isAnyOf(array, this.str.charAt(index2)); ++index2) {
		}
		this.index = index2;
		return this.str.substring(index, index2);
	}

	public String[] getWordArray(final char[] array) {
		final Vector<String> wordVector = this.getWordVector(array);
		final String[] array2 = new String[wordVector.size()];
		for (int i = 0; i < wordVector.size(); ++i) {
			array2[i] = wordVector.elementAt(i);
		}
		return array2;
	}

	public String getWordSkippingQuoted(final char[] array) {
		int index;
		for (index = this.index; index < this.str.length() && isAnyOf(array, this.str.charAt(index)); ++index) {
		}
		int n;
		int index2 = 0;
		int n2;
//		for (n = 0, index2 = index; index2 < this.str.length() && (!isAnyOf(array, this.str.charAt(index2)) || n != 0); ++index2, n = n2) {
//			n2 = n;
//			if (this.str.charAt(index2) == '\"') {
//				if (n != 0) {
//					n2 = 0;
//				} else {
//					n2 = 1;
//				}
//			}
//		}
//		this.index = index2;
		// TODO
		return this.str.substring(index, index2);
	}

	public Vector<String> getWordVector(final char[] array) {
		final Vector<String> vector = new Vector<String>();
		do {
			vector.addElement(this.getWord(array));
		} while (this.hasMore());
		return vector;
	}

	public Parser goTo(final char c) {
		this.index = this.str.indexOf(c, this.index);
		if (this.index < 0) {
			this.index = this.str.length();
		}
		return this;
	}

	public Parser goTo(final String s) {
		this.index = this.str.indexOf(s, this.index);
		if (this.index < 0) {
			this.index = this.str.length();
		}
		return this;
	}

	public Parser goTo(final char[] array) {
		this.index = this.indexOf(array);
		if (this.index < 0) {
			this.index = this.str.length();
		}
		return this;
	}

	public Parser goTo(final String[] array) {
		this.index = this.indexOf(array);
		if (this.index < 0) {
			this.index = this.str.length();
		}
		return this;
	}

	public Parser goToIgnoreCase(final String s) {
		this.index = this.indexOfIgnoreCase(s);
		if (this.index < 0) {
			this.index = this.str.length();
		}
		return this;
	}

	public Parser goToIgnoreCase(final String[] array) {
		this.index = this.indexOfIgnoreCase(array);
		if (this.index < 0) {
			this.index = this.str.length();
		}
		return this;
	}

	public Parser goToNextLine() {
		while (this.index < this.str.length() && !isCRLF(this.str.charAt(this.index))) {
			++this.index;
		}
		if (this.index < this.str.length()) {
			if (!this.str.startsWith("\r\n", this.index)) {
				++this.index;
				return this;
			}
			this.index += 2;
		}
		return this;
	}

	public Parser goToSkippingQuoted(final char c) {
		// TODO
		return null;
	}

	public boolean hasMore() {
		return this.length() > 0;
	}

	public int indexOf(final char c) {
		return this.str.indexOf(c, this.index);
	}

	public int indexOf(final String s) {
		return this.str.indexOf(s, this.index);
	}

	public int indexOf(final char[] array) {
		int n;
		int index;
		for (n = 0, index = this.index; index < this.str.length() && n == 0; ++index) {
			for (int i = 0; i < array.length; ++i) {
				if (this.str.charAt(index) == array[i]) {
					n = 1;
					break;
				}
			}
		}
		if (n != 0) {
			return index - 1;
		}
		return -1;
	}

	public int indexOf(final String[] array) {
		int n;
		int index;
		for (n = 0, index = this.index; index < this.str.length() && n == 0; ++index) {
			for (int i = 0; i < array.length; ++i) {
				if (this.str.startsWith(array[i], index)) {
					n = 1;
					break;
				}
			}
		}
		if (n != 0) {
			return index - 1;
		}
		return -1;
	}

	public int indexOfIgnoreCase(final String s) {
		final Parser parser = new Parser(this.str, this.index);
		while (parser.hasMore()) {
			if (parser.startsWithIgnoreCase(s)) {
				return parser.getPos();
			}
			parser.skipChar();
		}
		return -1;
	}

	public int indexOfIgnoreCase(final String[] array) {
		final Parser parser = new Parser(this.str, this.index);
		while (parser.hasMore()) {
			if (parser.startsWithIgnoreCase(array)) {
				return parser.getPos();
			}
			parser.skipChar();
		}
		return -1;
	}

	public int indexOfNextLine() {
		final Parser parser = new Parser(this.str, this.index);
		parser.goToNextLine();
		final int pos = parser.getPos();
		if (pos < this.str.length()) {
			return pos;
		}
		return -1;
	}

	public int length() {
		return this.str.length() - this.index;
	}

	public char nextChar() {
		return this.charAt(0);
	}

	public Parser setPos(final int index) {
		this.index = index;
		return this;
	}

	public Parser skipCRLF() {
		while (this.index < this.str.length() && isCRLF(this.str.charAt(this.index))) {
			++this.index;
		}
		return this;
	}

	public Parser skipChar() {
		if (this.index < this.str.length()) {
			++this.index;
		}
		return this;
	}

	public Parser skipChars(final char[] array) {
		while (this.index < this.str.length() && isAnyOf(array, this.nextChar())) {
			++this.index;
		}
		return this;
	}

	public Parser skipN(final int n) {
		this.index += n;
		if (this.index > this.str.length()) {
			this.index = this.str.length();
		}
		return this;
	}

	public Parser skipString() {
		this.getString();
		return this;
	}

	public Parser skipWSP() {
		while (this.index < this.str.length() && isSP(this.str.charAt(this.index))) {
			++this.index;
		}
		return this;
	}

	public Parser skipWSPCRLF() {
		while (this.index < this.str.length() && isWSPCRLF(this.str.charAt(this.index))) {
			++this.index;
		}
		return this;
	}

	public boolean startsWith(final String s) {
		return this.str.startsWith(s, this.index);
	}

	public boolean startsWith(final String[] array) {
		for (int i = 0; i < array.length; ++i) {
			if (this.str.startsWith(array[i], this.index)) {
				return true;
			}
		}
		return false;
	}

	public boolean startsWithIgnoreCase(final String s) {
		for (int n = 0; n < s.length() && this.index + n < this.str.length(); ++n) {
			if (compareIgnoreCase(s.charAt(n), this.str.charAt(this.index + n)) != 0) {
				return false;
			}
		}
		return true;
	}

	public boolean startsWithIgnoreCase(final String[] array) {
		final boolean b = true;
		for (int i = 0; i < array.length; ++i) {
			int n = 1;
			int n2 = 0;
			int n3;
			while (true) {
				n3 = n;
				if (n2 >= array[i].length()) {
					break;
				}
				if (this.index + n2 >= this.str.length()) {
					n3 = n;
					break;
				}
				if (compareIgnoreCase(array[i].charAt(n2), this.str.charAt(this.index + n2)) == 0) {
					n = 1;
				} else {
					n = 0;
				}
				n3 = n;
				if (n == 0) {
					break;
				}
				++n2;
			}
			final boolean b2 = b;
			if (n3 != 0) {
				return b2;
			}
		}
		return false;
	}

	public Parser subParser(final int n) {
		return new Parser(this.str.substring(this.index, this.index + n));
	}

	@Override
	public String toString() {
		return this.getRemainingString();
	}
}
