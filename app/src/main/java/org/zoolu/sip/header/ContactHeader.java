package org.zoolu.sip.header;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.provider.SipParser;
import org.zoolu.tools.DateFormat;
import org.zoolu.tools.Parser;

import java.util.Date;

public class ContactHeader extends EndPointHeader {
	public ContactHeader() {
		super(new Header("Contact", null));
		this.value = "*";
	}

	public ContactHeader(final NameAddress nameAddress) {
		super("Contact", nameAddress);
	}

	public ContactHeader(final NameAddress nameAddress, final String s, final String s2) {
		super("Contact", nameAddress);
		if (s != null) {
			this.setParameter("q", s);
		}
		this.setParameter("+g.3gpp.icsi-ref", s2);
	}

	public ContactHeader(final SipURL sipURL) {
		super("Contact", sipURL);
	}

	public ContactHeader(final Header header) {
		super(header);
	}

	public int getExpires() {
		int n = -1;
		final String parameter = this.getParameter("expires");
		if (parameter != null) {
			if (parameter.indexOf("GMT") < 0) {
				return new SipParser(parameter).getInt();
			}
			if ((n = (int) ((new SipParser(new Parser(parameter).getStringUnquoted()).getDate().getTime() - System.currentTimeMillis()) / 1000L)) < 0) {
				n = 0;
			}
		}
		return n;
	}

	public Date getExpiresDate() {
		final Date date = null;
		final String parameter = this.getParameter("expires");
		Date date2 = date;
		if (parameter != null) {
			if (parameter.indexOf("GMT") >= 0) {
				date2 = new SipParser(new Parser(parameter).getStringUnquoted()).getDate();
			} else {
				final long n = new SipParser(parameter).getInt();
				date2 = date;
				if (n >= 0L) {
					return new Date(System.currentTimeMillis() + 1000L * n);
				}
			}
		}
		return date2;
	}

	public boolean hasExpires() {
		return this.hasParameter("expires");
	}

	public boolean isExpired() {
		return this.getExpires() == 0;
	}

	public boolean isStar() {
		return this.value.indexOf(42) >= 0;
	}

	public ContactHeader removeExpires() {
		this.removeParameter("expires");
		return this;
	}

	public ContactHeader setExpires(final int n) {
		this.setParameter("expires", Integer.toString(n));
		return this;
	}

	public ContactHeader setExpires(final Date date) {
		this.setParameter("expires", "\"" + DateFormat.formatEEEddMMM(date) + "\"");
		return this;
	}
}
