package org.zoolu.sip.header;

import org.zoolu.tools.Parser;

public class SubscriptionStateHeader extends ParametricHeader {
	public static final String ACTIVE = "active";
	public static final String PENDING = "pending";
	public static final String TERMINATED = "terminated";
	private static final char[] delim;

	static {
		delim = new char[]{',', ';', ' ', '\t', '\n', '\r'};
	}

	public SubscriptionStateHeader(final String s) {
		super("Subscription-State", s);
	}

	public SubscriptionStateHeader(final Header header) {
		super(header);
	}

	public int getExpires() {
		final String parameter = this.getParameter("expires");
		if (parameter != null) {
			return Integer.parseInt(parameter);
		}
		return -1;
	}

	public String getReason() {
		return this.getParameter("reason");
	}

	public String getState() {
		return new Parser(this.value).getWord(SubscriptionStateHeader.delim);
	}

	public boolean hasExpires() {
		return this.hasParameter("expires");
	}

	public boolean hasReason() {
		return this.hasParameter("reason");
	}

	public boolean isActive() {
		return this.getState().equals("active");
	}

	public boolean isPending() {
		return this.getState().equals("pending");
	}

	public boolean isTerminated() {
		return this.getState().equals("terminated");
	}

	public SubscriptionStateHeader setExpires(final int n) {
		this.setParameter("expires", Integer.toString(n));
		return this;
	}

	public SubscriptionStateHeader setReason(final String s) {
		this.setParameter("reason", s);
		return this;
	}
}
