package org.zoolu.net;

public class SocketAddress {
	IpAddress ipaddr;
	int port;

	public SocketAddress(String s) {
		int int1 = -1;
		final int index = s.indexOf(58);
		if (index >= 0) {
			final String substring = s.substring(0, index);
			try {
				int1 = Integer.parseInt(s.substring(index + 1));
				s = substring;
			} catch (Exception ex) {
				s = substring;
			}
		}
		this.init(new IpAddress(s), int1);
	}

	public SocketAddress(final String s, final int n) {
		this.init(new IpAddress(s), n);
	}

	public SocketAddress(final IpAddress ipAddress, final int n) {
		this.init(ipAddress, n);
	}

	public SocketAddress(final SocketAddress socketAddress) {
		this.init(socketAddress.ipaddr, socketAddress.port);
	}

	private void init(final IpAddress ipaddr, final int port) {
		this.ipaddr = ipaddr;
		this.port = port;
	}

	public Object clone() {
		return new SocketAddress(this);
	}

	@Override
	public boolean equals(final Object o) {
		try {
			final SocketAddress socketAddress = (SocketAddress) o;
			if (this.port != socketAddress.port) {
				return false;
			}
			if (this.ipaddr.equals(socketAddress.ipaddr)) {
				return true;
			}
		} catch (Exception ex) {
		}
		return false;
	}

	public IpAddress getAddress() {
		return this.ipaddr;
	}

	public int getPort() {
		return this.port;
	}

	@Override
	public String toString() {
		return String.valueOf(this.ipaddr.toString()) + ":" + this.port;
	}
}
