package org.zoolu.sip.header;

public class StatusLine {
	protected int code;
	protected String reason;

	public StatusLine(final int code, final String reason) {
		this.code = code;
		this.reason = reason;
	}

	public Object clone() {
		return new StatusLine(this.getCode(), this.getReason());
	}

	@Override
	public boolean equals(final Object o) {
		final boolean b = false;
		try {
			final StatusLine statusLine = (StatusLine) o;
			boolean b2 = b;
			if (statusLine.getCode() == this.getCode()) {
				final boolean equals = statusLine.getReason().equals(this.getReason());
				b2 = b;
				if (equals) {
					b2 = true;
				}
			}
			return b2;
		} catch (Exception ex) {
			return false;
		}
	}

	public int getCode() {
		return this.code;
	}

	public String getReason() {
		return this.reason;
	}

	@Override
	public String toString() {
		return "SIP/2.0 " + this.code + " " + this.reason + "\r\n";
	}
}
