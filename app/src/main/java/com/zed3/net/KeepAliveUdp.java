package com.zed3.net;

import org.zoolu.net.SocketAddress;
import org.zoolu.net.UdpPacket;
import org.zoolu.net.UdpSocket;

import java.io.IOException;

public class KeepAliveUdp extends Thread {
	protected long delta_time;
	long expire;
	boolean stop;
	protected SocketAddress target;
	UdpPacket udp_packet;
	UdpSocket udp_socket;

	protected KeepAliveUdp(final SocketAddress target, final long delta_time) {
		this.udp_packet = null;
		this.expire = 0L;
		this.stop = false;
		this.target = target;
		this.delta_time = delta_time;
	}

	public KeepAliveUdp(final UdpSocket udpSocket, final SocketAddress target, final long delta_time) {
		this.udp_packet = null;
		this.expire = 0L;
		this.stop = false;
		this.target = target;
		this.delta_time = delta_time;
		this.init(udpSocket, null);
		this.start();
	}

	public KeepAliveUdp(final UdpSocket udpSocket, final SocketAddress target, final UdpPacket udpPacket, final long delta_time) {
		this.udp_packet = null;
		this.expire = 0L;
		this.stop = false;
		this.target = target;
		this.delta_time = delta_time;
		this.init(udpSocket, udpPacket);
		this.start();
	}

	private void init(final UdpSocket udp_socket, final UdpPacket udpPacket) {
		this.udp_socket = udp_socket;
		UdpPacket udp_packet = udpPacket;
		if (udpPacket == null) {
			final byte[] array2;
			final byte[] array = array2 = new byte[2];
			array2[0] = 13;
			array2[1] = 10;
			udp_packet = new UdpPacket(array, 0, array.length);
		}
		if (this.target != null) {
			udp_packet.setIpAddress(this.target.getAddress());
			udp_packet.setPort(this.target.getPort());
		}
		this.udp_packet = udp_packet;
	}

	public long getDeltaTime() {
		return this.delta_time;
	}

	public SocketAddress getDestSoAddress() {
		return this.target;
	}

	public void halt() {
		this.stop = true;
	}

	public boolean isRunning() {
		return !this.stop;
	}

	@Override
	public void run() {
		while (true) {
			try {
				while (!this.stop) {
					Thread.sleep(this.delta_time);
					if (this.expire > 0L && System.currentTimeMillis() > this.expire) {
						this.halt();
					}
				}
				this.udp_socket = null;
			} catch (Exception ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
	}

	public void sendToken() throws IOException {
		if (!this.stop && this.target != null && this.udp_socket != null) {
			this.udp_socket.send(this.udp_packet);
		}
	}

	public void setDeltaTime(final long delta_time) {
		this.delta_time = delta_time;
	}

	public void setDestSoAddress(final SocketAddress target) {
		this.target = target;
		if (this.udp_packet != null && this.target != null) {
			this.udp_packet.setIpAddress(this.target.getAddress());
			this.udp_packet.setPort(this.target.getPort());
		}
	}

	public void setExpirationTime(final long n) {
		if (n == 0L) {
			this.expire = 0L;
			return;
		}
		this.expire = System.currentTimeMillis() + n;
	}

	@Override
	public String toString() {
		Object string = null;
		if (this.udp_socket != null) {
			string = "udp:" + this.udp_socket.getLocalAddress() + ":" + this.udp_socket.getLocalPort() + "-->" + this.target.toString();
		}
		return String.valueOf(string) + " (" + this.delta_time + "ms)";
	}
}
