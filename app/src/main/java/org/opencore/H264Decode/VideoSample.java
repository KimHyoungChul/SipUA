package org.opencore.H264Decode;

public class VideoSample {
	public byte[] data;
	public int timestamp;

	public VideoSample(final byte[] data, final int timestamp) {
		this.data = null;
		this.timestamp = 0;
		this.data = data;
		this.timestamp = timestamp;
	}
}
