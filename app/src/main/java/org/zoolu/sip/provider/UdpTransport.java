package org.zoolu.sip.provider;

import org.zoolu.net.IpAddress;
import org.zoolu.net.UdpPacket;
import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.net.UdpSocket;
import org.zoolu.sip.message.Message;

import java.io.IOException;

public class UdpTransport implements Transport, UdpProviderListener {
	public static final String PROTO_UDP = "udp";
	public static Boolean needEncrypt;
	TransportListener listener;
	int port;
	String proto;
	UdpProvider udp_provider;

	public UdpTransport(final int n, final IpAddress ipAddress, final TransportListener listener) throws IOException {
		this.listener = listener;
		final UdpSocket udpSocket = new UdpSocket(n, ipAddress);
		this.udp_provider = new UdpProvider(udpSocket, this);
		this.port = udpSocket.getLocalPort();
	}

	public UdpTransport(final int n, final TransportListener listener) throws IOException {
		this.listener = listener;
		final UdpSocket udpSocket = new UdpSocket(n);
		this.udp_provider = new UdpProvider(udpSocket, this);
		this.port = udpSocket.getLocalPort();
	}

	public UdpTransport(final UdpSocket udpSocket, final TransportListener listener) {
		this.listener = listener;
		this.udp_provider = new UdpProvider(udpSocket, this);
		this.port = udpSocket.getLocalPort();
	}

	public int getPort() {
		return this.port;
	}

	@Override
	public String getProtocol() {
		return "udp";
	}

	@Override
	public void halt() {
		if (this.udp_provider != null) {
			this.udp_provider.halt();
		}
	}

	@Override
	public void onReceivedPacket(final UdpProvider udpProvider, final UdpPacket udpPacket) {
		if (UdpTransport.needEncrypt) {
			final byte[] array = {69, 69, 69, 58, 26, 5, 4, 0, 0, 26, 5, 4};
			final String s = new String(udpPacket.getData());
			if (s.startsWith(new String(array))) {
				final byte[] data = udpPacket.getData();
				for (int i = 0; i < data.length; ++i) {
					data[i] = 0;
				}
				final int length = udpPacket.getLength();
				udpPacket.setData(s.substring(12).getBytes());
				udpPacket.setLength(length - 12);
			}
		}
		final Message message = new Message(udpPacket);
		message.setRemoteAddress(udpPacket.getIpAddress().toString());
		message.setRemotePort(udpPacket.getPort());
		message.setTransport("udp");
		if (this.listener != null) {
			this.listener.onReceivedMessage(this, message);
		}
	}

	@Override
	public void onServiceTerminated(final UdpProvider udpProvider, final Exception ex) {
		if (this.listener != null) {
			this.listener.onTransportTerminated(this, ex);
		}
		final UdpSocket udpSocket = udpProvider.getUdpSocket();
		// TODO
	}

	public void sendMessage(final String s, final String s2, final int port) throws IOException {
		if (this.udp_provider != null) {
			final byte[] bytes = s.getBytes();
			final UdpPacket udpPacket = new UdpPacket(bytes, bytes.length);
			udpPacket.setIpAddress(IpAddress.getByName(s2));
			udpPacket.setPort(port);
			this.udp_provider.send(udpPacket);
		}
	}

	@Override
	public void sendMessage(final Message message, final IpAddress ipAddress, final int port) throws IOException {
		if (this.udp_provider != null) {
			byte[] array2;
			final byte[] array = array2 = message.toString().getBytes();
			if (UdpTransport.needEncrypt) {
				array2 = new String(String.valueOf(new String(new byte[]{69, 69, 69, 58, 26, 5, 4, 0, 0, 26, 5, 4})) + new String(array)).getBytes();
			}
			final UdpPacket udpPacket = new UdpPacket(array2, array2.length);
			udpPacket.setIpAddress(ipAddress);
			udpPacket.setPort(port);
			this.udp_provider.send(udpPacket);
		}
	}

	@Override
	public String toString() {
		if (this.udp_provider != null) {
			return this.udp_provider.toString();
		}
		return null;
	}
}
