package com.zed3.net;

import org.zoolu.tools.Random;

public class RtpPacket {
	static final long ssrc_audio;
	static final long ssrc_video;
	long packedTime;
	byte[] packet;
	int packet_len;

	static {
		ssrc_audio = Random.nextLong();
		ssrc_video = RtpPacket.ssrc_audio / 2L - 1L;
	}

	public RtpPacket(final RtpPacket rtpPacket) {
		this.packet_len = rtpPacket.packet_len;
		this.packedTime = rtpPacket.packedTime;
		this.packet = new byte[this.packet_len];
		System.arraycopy(rtpPacket.packet, 0, this.packet, 0, this.packet_len);
	}

	public RtpPacket(final byte[] packet, final int packet_len, final int n) {
		this.packet = packet;
		this.packet_len = packet_len;
		if (this.packet_len < 12) {
			this.packet_len = 12;
		}
	}

	public RtpPacket(final byte[] packet, final int packet_len, final String s) {
		this.packet = packet;
		this.packet_len = packet_len;
		if (this.packet_len < 13) {
			this.packet_len = 13;
		}
		if (s.equalsIgnoreCase("0")) {
			this.init(15, 0);
			return;
		}
		this.init(15, 1);
	}

	private static boolean getBit(final byte b, final int n) {
		return (b & 0x80) == 0x80;
	}

	private static int getInt(final byte b) {
		return (b + 256) % 256;
	}

	private static int getInt(final byte[] array, final int n, final int n2) {
		return (int) getLong(array, n, n2);
	}

	private static long getLong(final byte[] array, int i, final int n) {
		long n2 = 0L;
		while (i < n) {
			n2 = (n2 << 8) + (array[i] & 0xFF);
			++i;
		}
		return n2;
	}

	private static byte setBit(final boolean b, final byte b2, final int n) {
		if (b) {
			return (byte) (1 << n | b2);
		}
		return (byte) ((1 << n | b2) ^ 1 << n);
	}

	private static void setInt(final int n, final byte[] array, final int n2, final int n3) {
		setLong(n, array, n2, n3);
	}

	private static void setLong(long n, final byte[] array, final int n2, int i) {
		for (--i; i >= n2; --i) {
			array[i] = (byte) (n % 256L);
			n >>= 8;
		}
	}

	public void SetPacketLen(final int packet_len) {
		this.packet_len = packet_len;
	}

	public int getCscrCount() {
		int n = 0;
		if (this.packet_len >= 12) {
			n = (this.packet[0] & 0xF);
		}
		return n;
	}

	public long[] getCscrList() {
		final int cscrCount = this.getCscrCount();
		final long[] array = new long[cscrCount];
		for (int i = 0; i < cscrCount; ++i) {
			array[i] = getLong(this.packet, i * 4 + 12, i * 4 + 16);
		}
		return array;
	}

	public int getHeaderLength() {
		if (this.packet_len >= 12) {
			return this.getCscrCount() * 4 + 12;
		}
		return this.packet_len;
	}

	public int getLength() {
		return this.packet_len;
	}

	public long getPackedTime() {
		return this.packedTime;
	}

	public byte[] getPacket() {
		return this.packet;
	}

	public byte[] getPayload() {
		final int headerLength = this.getHeaderLength();
		final int n = this.packet_len - headerLength;
		final byte[] array = new byte[n];
		for (int i = 0; i < n; ++i) {
			array[i] = this.packet[headerLength + i];
		}
		return array;
	}

	public int getPayloadLength() {
		if (this.packet_len >= 12) {
			return this.packet_len - this.getHeaderLength();
		}
		return 0;
	}

	public int getPayloadType() {
		if (this.packet_len >= 12) {
			return this.packet[1] & 0x7F;
		}
		return -1;
	}

	public int getSequenceNumber() {
		if (this.packet_len >= 12) {
			return getInt(this.packet, 2, 4);
		}
		return 0;
	}

	public long getSscr() {
		if (this.packet_len >= 12) {
			return getLong(this.packet, 8, 12);
		}
		return 0L;
	}

	public long getTimestamp() {
		if (this.packet_len >= 12) {
			return getLong(this.packet, 4, 8);
		}
		return 0L;
	}

	public int getVersion() {
		int n = 0;
		if (this.packet_len >= 12) {
			n = (this.packet[0] >> 6 & 0x3);
		}
		return n;
	}

	public boolean hasExtension() {
		boolean bit = false;
		if (this.packet_len >= 12) {
			bit = getBit(this.packet[0], 4);
		}
		return bit;
	}

	public boolean hasMarker() {
		return this.packet_len >= 12 && getBit(this.packet[1], 7);
	}

	public boolean hasPadding() {
		boolean bit = false;
		if (this.packet_len >= 12) {
			bit = getBit(this.packet[0], 5);
		}
		return bit;
	}

	public void init(final int n, final int n2) {
		long n3;
		if (n2 == 0) {
			n3 = RtpPacket.ssrc_audio;
		} else {
			n3 = RtpPacket.ssrc_video;
		}
		this.init(n, n3);
	}

	public void init(final int payloadType, final int sequenceNumber, final long timestamp, final long sscr) {
		this.setVersion(2);
		this.setPayloadType(payloadType);
		this.setSequenceNumber(sequenceNumber);
		this.setTimestamp(timestamp);
		this.setSscr(sscr);
	}

	public void init(final int n, final long n2) {
		this.init(n, Random.nextInt(), Random.nextLong(), n2);
	}

	public void setCmr(final byte b) {
		if (this.packet_len >= 13) {
			this.packet[12] = (byte) (b & 0x7F);
			this.packet[12] = (byte) (this.packet[12] << 4 & 0xF0);
		}
	}

	public void setCscrList(long[] array) {
		if (this.packet_len >= 12) {
			int length;
			if ((length = array.length) > 15) {
				length = 15;
			}
			this.packet[0] = (byte) ((this.packet[0] >> 4 << 4) + length);
			array = new long[length];
			for (int i = 0; i < length; ++i) {
				setLong(array[i], this.packet, i * 4 + 12, i * 4 + 16);
			}
		}
	}

	public void setExtension(final boolean b) {
		if (this.packet_len >= 12) {
			this.packet[0] = setBit(b, this.packet[0], 4);
		}
	}

	public void setMarker(final boolean b) {
		if (this.packet_len >= 12) {
			this.packet[1] = setBit(b, this.packet[1], 7);
		}
	}

	public void setPackedTime(final long packedTime) {
		this.packedTime = packedTime;
	}

	public void setPadding(final boolean b) {
		if (this.packet_len >= 12) {
			this.packet[0] = setBit(b, this.packet[0], 5);
		}
	}

	public void setPayload(final byte[] array, final int n) {
		if (this.packet_len >= 12) {
			final int headerLength = this.getHeaderLength();
			for (int i = 0; i < n; ++i) {
				this.packet[headerLength + i] = array[i];
			}
			this.packet_len = headerLength + n;
		}
	}

	public void setPayloadLength(final int n) {
		this.packet_len = this.getHeaderLength() + n;
	}

	public void setPayloadType(final int n) {
		if (this.packet_len >= 12) {
			this.packet[1] = (byte) ((this.packet[1] & 0x80) | (n & 0x7F));
		}
	}

	public void setSequenceNumber(final int n) {
		if (this.packet_len >= 12) {
			setInt(n, this.packet, 2, 4);
		}
	}

	public void setSscr(final long n) {
		if (this.packet_len >= 12) {
			setLong(n, this.packet, 8, 12);
		}
	}

	public void setTimestamp(final long n) {
		if (this.packet_len >= 12) {
			setLong(n, this.packet, 4, 8);
		}
	}

	public void setVersion(final int n) {
		if (this.packet_len >= 12) {
			this.packet[0] = (byte) ((this.packet[0] & 0x3F) | (n & 0x3) << 6);
		}
	}
}
