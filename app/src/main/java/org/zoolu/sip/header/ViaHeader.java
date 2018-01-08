package org.zoolu.sip.header;

import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.provider.SipParser;

public class ViaHeader extends ParametricHeader {
	protected static final String branch_param = "branch";
	protected static final String maddr_param = "maddr";
	protected static final String received_param = "received";
	protected static final String rport_param = "rport";
	protected static final String ttl_param = "ttl";

	public ViaHeader(final String s) {
		super("Via", s);
	}

	public ViaHeader(final String s, final int n) {
		super("Via", "SIP/2.0/UDP " + s + ":" + n);
	}

	public ViaHeader(final String s, final String s2, final int n) {
		super("Via", "SIP/2.0/" + s.toUpperCase() + " " + s2 + ":" + n);
	}

	public ViaHeader(final Header header) {
		super(header);
	}

	public String getBranch() {
		return this.getParameter("branch");
	}

	public String getHost() {
		final String sentBy = this.getSentBy();
		final SipParser sipParser = new SipParser(sentBy);
		sipParser.goTo(':');
		String substring = sentBy;
		if (sipParser.hasMore()) {
			substring = sentBy.substring(0, sipParser.getPos());
		}
		return substring;
	}

	public String getMaddr() {
		return this.getParameter("maddr");
	}

	public int getPort() {
		final SipParser sipParser = new SipParser(this.getSentBy());
		sipParser.goTo(':');
		if (sipParser.hasMore()) {
			return sipParser.skipChar().getInt();
		}
		return -1;
	}

	public String getProtocol() {
		return new SipParser(this.value).goTo('/').skipChar().goTo('/').skipChar().skipWSP().getString();
	}

	public String getReceived() {
		return this.getParameter("received");
	}

	public int getRport() {
		final String parameter = this.getParameter("rport");
		if (parameter != null) {
			return Integer.parseInt(parameter);
		}
		return -1;
	}

	public String getSentBy() {
		final SipParser sipParser = new SipParser(this.value);
		sipParser.goTo('/').skipChar().goTo('/').skipString().skipWSP();
		if (!sipParser.hasMore()) {
			return null;
		}
		return this.value.substring(sipParser.getPos(), sipParser.indexOfSeparator());
	}

	public SipURL getSipURL() {
		return new SipURL(this.getHost(), this.getPort());
	}

	public int getTtl() {
		final String parameter = this.getParameter("ttl");
		if (parameter != null) {
			return Integer.parseInt(parameter);
		}
		return -1;
	}

	public boolean hasBranch() {
		return this.hasParameter("branch");
	}

	public boolean hasMaddr() {
		return this.hasParameter("maddr");
	}

	public boolean hasPort() {
		return this.getSentBy().indexOf(":") > 0;
	}

	public boolean hasReceived() {
		return this.hasParameter("received");
	}

	public boolean hasRport() {
		return this.hasParameter("rport");
	}

	public boolean hasTtl() {
		return this.hasParameter("ttl");
	}

	public void setBranch(final String s) {
		this.setParameter("branch", s);
	}

	public void setMaddr(final String s) {
		this.setParameter("maddr", s);
	}

	public void setReceived(final String s) {
		this.setParameter("received", s);
	}

	public void setRport() {
		this.setParameter("rport", null);
	}

	public void setRport(final int n) {
		if (n < 0) {
			this.setParameter("rport", null);
			return;
		}
		this.setParameter("rport", Integer.toString(n));
	}

	public void setTtl(final int n) {
		this.setParameter("ttl", Integer.toString(n));
	}
}
