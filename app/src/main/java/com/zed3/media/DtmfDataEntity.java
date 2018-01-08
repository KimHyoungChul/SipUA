package com.zed3.media;

import java.util.HashMap;

public class DtmfDataEntity {
	public static HashMap<Character, Byte> rtpEventMap;
	String dtmf;
	int seqn;
	long time;

	static {
		DtmfDataEntity.rtpEventMap = new HashMap<Character, Byte>() {
			{
				this.put('0', (byte) 0);
				this.put('1', (byte) 1);
				this.put('2', (byte) 2);
				this.put('3', (byte) 3);
				this.put('4', (byte) 4);
				this.put('5', (byte) 5);
				this.put('6', (byte) 6);
				this.put('7', (byte) 7);
				this.put('8', (byte) 8);
				this.put('9', (byte) 9);
				this.put('*', (byte) 10);
				this.put('#', (byte) 11);
				this.put('A', (byte) 12);
				this.put('B', (byte) 13);
				this.put('C', (byte) 14);
				this.put('D', (byte) 15);
			}
		};
	}

	public DtmfDataEntity(final long time, final int seqn, final String dtmf) {
		this.time = 0L;
		this.seqn = 0;
		this.time = time;
		this.seqn = seqn;
		this.dtmf = dtmf;
	}
}
