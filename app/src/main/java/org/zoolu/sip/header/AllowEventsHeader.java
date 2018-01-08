package org.zoolu.sip.header;

import java.util.Vector;

public class AllowEventsHeader extends ListHeader {
	public AllowEventsHeader(final String s) {
		super("Allow-Events", s);
	}

	public AllowEventsHeader(final Header header) {
		super(header);
	}

	public void addEvent(final String s) {
		super.addElement(s);
	}

	public Vector<String> getEvents() {
		return super.getElements();
	}

	public void setEvents(final Vector<String> elements) {
		super.setElements(elements);
	}
}
