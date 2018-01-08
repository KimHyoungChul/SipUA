package org.zoolu.sip.header;

import org.zoolu.tools.Parser;

public class EventHeader extends ParametricHeader {
	private static final char[] delim;

	static {
		delim = new char[]{',', ';', ' ', '\t', '\n', '\r'};
	}

	public EventHeader(final String s) {
		super("Event", s);
	}

	public EventHeader(final String s, final String s2) {
		super("Event", s);
		if (s2 != null) {
			this.setParameter("id", s2);
		}
	}

	public EventHeader(final Header header) {
		super(header);
	}

	public String getEvent() {
		return new Parser(this.value).getWord(EventHeader.delim);
	}

	public String getId() {
		return this.getParameter("id");
	}

	public boolean hasId() {
		return this.hasParameter("id");
	}
}
