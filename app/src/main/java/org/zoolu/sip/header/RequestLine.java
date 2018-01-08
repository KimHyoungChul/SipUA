package org.zoolu.sip.header;

import org.zoolu.sip.address.SipURL;

public class RequestLine {
	protected String method;
	protected SipURL url;

	public RequestLine(final String method, final String s) {
		this.method = method;
		this.url = new SipURL(s);
	}

	public RequestLine(final String method, final SipURL url) {
		this.method = method;
		this.url = url;
	}

	public Object clone() {
		return new RequestLine(this.getMethod(), this.getAddress());
	}

	@Override
	public boolean equals(final Object o) {
		final boolean b = false;
		try {
			final RequestLine requestLine = (RequestLine) o;
			boolean b2 = b;
			if (requestLine.getMethod().equals(this.getMethod())) {
				final boolean equals = requestLine.getAddress().equals(this.getAddress());
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

	public SipURL getAddress() {
		return this.url;
	}

	public String getMethod() {
		return this.method;
	}

	@Override
	public String toString() {
		return String.valueOf(this.method) + " " + this.url + " SIP/2.0\r\n";
	}
}
