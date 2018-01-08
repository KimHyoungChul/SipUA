package org.zoolu.net;

import com.zed3.flow.FlowStatistics;

import java.io.IOException;
import java.io.InterruptedIOException;

public class UdpProvider extends Thread {
	public static final int BUFFER_SIZE = 65535;
	public static final int DEFAULT_SOCKET_TIMEOUT = 2000;
	long alive_time;
	int flow;
	int flow_send;
	boolean is_running;
	UdpProviderListener listener;
	int minimum_length;
	UdpSocket socket;
	int socket_timeout;
	boolean stop;

	public UdpProvider(final UdpSocket udpSocket, final long n, final UdpProviderListener udpProviderListener) {
		this.init(udpSocket, n, udpProviderListener);
		this.start();
	}

	public UdpProvider(final UdpSocket udpSocket, final UdpProviderListener udpProviderListener) {
		this.init(udpSocket, 0L, udpProviderListener);
		this.start();
	}

	private void init(final UdpSocket socket, final long alive_time, final UdpProviderListener listener) {
		this.listener = listener;
		this.socket = socket;
		this.socket_timeout = 2000;
		this.alive_time = alive_time;
		this.minimum_length = 0;
		this.stop = false;
		this.is_running = true;
	}

	public int getMinimumReceivedDataLength() {
		return this.minimum_length;
	}

	public int getSoTimeout() {
		return this.socket_timeout;
	}

	public UdpSocket getUdpSocket() {
		return this.socket;
	}

	public void halt() {
		this.stop = true;
		this.socket.close();
	}

	public boolean isRunning() {
		return this.is_running;
	}

	@Override
	public void run() {
		final byte[] array = new byte[65535];
		final UdpPacket udpPacket = new UdpPacket(array, array.length);
		final Exception ex = null;
		long n = 0L;
		UdpPacket udpPacket2 = udpPacket;
		if (this.alive_time > 0L) {
			n = System.currentTimeMillis() + this.alive_time;
			udpPacket2 = udpPacket;
		}
		while (true) {
			try {
				while (!this.stop) {
					try {
						this.socket.receive(udpPacket2);
						if (udpPacket2.getLength() + 42 > 60) {
							this.flow = udpPacket2.getLength() + 42;
						} else {
							this.flow = 60;
						}
						FlowStatistics.Sip_Receive_Data += this.flow;
						long n2 = n;
						if (udpPacket2.getLength() >= this.minimum_length) {
							if (this.listener != null) {
								this.listener.onReceivedPacket(this, udpPacket2);
							}
							n2 = n;
							if (this.alive_time > 0L) {
								n2 = System.currentTimeMillis() + this.alive_time;
							}
						}
						udpPacket2 = new UdpPacket(array, array.length);
						n = n2;
					} catch (InterruptedIOException ex3) {
						if (this.alive_time > 0L && System.currentTimeMillis() > n) {
							this.halt();
							continue;
						}
						continue;
					}
				}
				final Exception ex2 = ex;
				this.is_running = false;
				if (this.listener != null) {
					this.listener.onServiceTerminated(this, ex2);
				}
				this.listener = null;
			} catch (Exception ex2) {
				this.stop = true;
				continue;
			}
			break;
		}
	}

	public void send(final UdpPacket udpPacket) throws IOException {
		if (!this.stop) {
			this.socket.send(udpPacket);
			if (udpPacket.getLength() + 42 > 60) {
				this.flow_send = udpPacket.getLength() + 42;
			} else {
				this.flow_send = 60;
			}
			FlowStatistics.Sip_Send_Data += this.flow_send;
		}
	}

	public void setMinimumReceivedDataLength(final int minimum_length) {
		this.minimum_length = minimum_length;
	}

	public void setSoTimeout(final int socket_timeout) {
		this.socket_timeout = socket_timeout;
	}

	@Override
	public String toString() {
		return "udp:" + this.socket.getLocalAddress() + ":" + this.socket.getLocalPort();
	}
}
