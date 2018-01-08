package org.zoolu.net;

import android.content.Context;

import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpAddress {
	public static String localIpAddress = "127.0.0.1";
	String address;
	InetAddress inet_address;

	public static Context getUIContext() {
		if (Receiver.mContext == null) {
			return SipUAApp.mContext;
		}
		return Receiver.mContext;
	}

	IpAddress(InetAddress iaddress) {
		init(null, iaddress);
	}

	private void init(String address, InetAddress iaddress) {
		this.address = address;
		this.inet_address = iaddress;
	}

	InetAddress getInetAddress() {
		if (this.inet_address == null) {
			try {
				this.inet_address = InetAddress.getByName(this.address);
			} catch (UnknownHostException e) {
				this.inet_address = null;
			}
		}
		return this.inet_address;
	}

	public IpAddress(String address) {
		init(address, null);
	}

	public IpAddress(IpAddress ipaddr) {
		init(ipaddr.address, ipaddr.inet_address);
	}

	public Object clone() {
		return new IpAddress(this);
	}

	public boolean equals(Object obj) {
		try {
			if (toString().equals(((IpAddress) obj).toString())) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public String toString() {
		if (this.address == null && this.inet_address != null) {
			this.address = this.inet_address.getHostAddress();
		}
		return this.address;
	}

	public static IpAddress getByName(String host_addr) throws UnknownHostException {
		return new IpAddress(InetAddress.getByName(host_addr));
	}

	public static void setLocalIpAddress() {
		localIpAddress = "127.0.0.1";
		// TODO
	}
}
