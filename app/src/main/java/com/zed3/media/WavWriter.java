package com.zed3.media;

import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;

public class WavWriter {
	byte[] buf;
	int i;
	int leftSamplesWritten;
	RandomAccessFile raf;
	int rightSamplesWritten;
	long sampleDataOffset;

	public WavWriter(final String s, final int n) {
		this.raf = null;
		this.leftSamplesWritten = 0;
		this.rightSamplesWritten = 0;
		this.buf = new byte[10000];
		try {
			(this.raf = new RandomAccessFile(s, "rw")).setLength(0L);
			this.raf.writeBytes("RIFF");
			this.raf.writeInt(0);
			this.raf.writeBytes("WAVE");
			this.raf.writeBytes("fmt ");
			this.raf.writeInt(this.B2L(16));
			this.raf.writeShort(this.B2L_s(1));
			this.raf.writeShort(this.B2L_s(2));
			this.raf.writeInt(this.B2L(n));
			this.raf.writeInt(this.B2L(n * 2 * 2));
			this.raf.writeShort(this.B2L_s(4));
			this.raf.writeShort(this.B2L_s(16));
			this.raf.writeBytes("data");
			this.raf.writeInt(0);
			this.sampleDataOffset = this.raf.getFilePointer();
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.e("CallRecorder", "Error creating output file.");
			this.raf = null;
		}
	}

	int B2L(final int n) {
		return ((n & 0xFF) << 24) + ((0xFF00 & n) << 8) + ((0xFF0000 & n) >> 8) + (n >> 24 & 0xFF);
	}

	int B2L_s(final int n) {
		return (n >> 8 & 0xFF) + (n << 8 & 0xFF00);
	}

	void close() {
		while (true) {
			while (true) {
				Label_0117:
				{
					synchronized (this) {
						if (this.raf != null) {
							try {
								if (this.leftSamplesWritten <= this.rightSamplesWritten) {
									break Label_0117;
								}
								final int n = this.leftSamplesWritten;
								this.raf.seek(4L);
								this.raf.writeInt(this.B2L(n * 4 + 36));
								this.raf.seek(40L);
								this.raf.writeInt(this.B2L(n * 4));
								this.raf.close();
								this.raf = null;
							} catch (IOException ex) {
								ex.printStackTrace();
								Log.e("CallRecorder", "Error writing final data to output file.");
							}
						}
						return;
					}
				}
				final int n = this.rightSamplesWritten;
				continue;
			}
		}
	}

	void writeLeft(final short[] array, final int n, final int n2) {
		Label_0101_Outer:
		while (true) {
			while (true) {
				Label_0207:
				while (true) {
					Label_0158:
					{
						synchronized (this) {
							if (this.leftSamplesWritten <= 524288000 && this.raf != null) {
								try {
									this.raf.seek(this.sampleDataOffset + this.leftSamplesWritten * 4);
									this.i = 0;
									if (this.i < n2) {
										break Label_0158;
									}
									this.raf.read(this.buf, 0, n2 * 4);
									this.raf.seek(this.sampleDataOffset + this.leftSamplesWritten * 4);
									this.i = 0;
									if (this.i < n2) {
										break Label_0207;
									}
									this.leftSamplesWritten += n2;
									this.raf.write(this.buf, 0, n2 * 4);
								} catch (IOException ex) {
									ex.printStackTrace();
									Log.e("CallRecorder", "Error writing to output file.");
								}
							}
							return;
						}
					}
					this.buf[this.i * 4 + 2] = (this.buf[this.i * 4 + 3] = 0);
					++this.i;
					continue Label_0101_Outer;
				}
//				final Throwable t;
//				this.buf[this.i * 4 + 1] = (byte) (t[this.i + n] >> 8);
//				this.buf[this.i * 4] = (byte) t[this.i + n];
				++this.i;
				continue;
			}
		}
	}

	void writeRight(final short[] array, final int n, final int n2) {
		Label_0101_Outer:
		while (true) {
			while (true) {
				Label_0205:
				while (true) {
					Label_0158:
					{
						synchronized (this) {
							if (this.rightSamplesWritten <= 524288000 && this.raf != null) {
								try {
									this.raf.seek(this.sampleDataOffset + this.rightSamplesWritten * 4);
									this.i = 0;
									if (this.i < n2) {
										break Label_0158;
									}
									this.raf.read(this.buf, 0, n2 * 4);
									this.raf.seek(this.sampleDataOffset + this.rightSamplesWritten * 4);
									this.i = 0;
									if (this.i < n2) {
										break Label_0205;
									}
									this.rightSamplesWritten += n2;
									this.raf.write(this.buf, 0, n2 * 4);
								} catch (IOException ex) {
									ex.printStackTrace();
									Log.e("CallRecorder", "Error writing to output file.");
								}
							}
							return;
						}
					}
					this.buf[this.i * 4] = (this.buf[this.i * 4 + 1] = 0);
					++this.i;
					continue Label_0101_Outer;
				}
//				final Throwable t;
//				this.buf[this.i * 4 + 3] = (byte) (t[this.i + n] >> 8);
//				this.buf[this.i * 4 + 2] = (byte) t[this.i + n];
				++this.i;
				continue;
			}
		}
	}
}
