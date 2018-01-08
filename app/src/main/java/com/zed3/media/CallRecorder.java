package com.zed3.media;

import android.text.format.Time;

import java.io.File;

public class CallRecorder {
	WavWriter callWav;
	boolean incomingStopped;
	boolean outgoingStopped;

	public CallRecorder(final String s, final int n) {
		this.outgoingStopped = false;
		this.incomingStopped = false;
		this.callWav = null;
		String format2445 = s;
		if (s == null) {
			final Time time = new Time();
			time.setToNow();
			format2445 = time.format2445();
		}
		new File("/sdcard/Sipdroid_Recordings/").mkdirs();
		this.callWav = new WavWriter("/sdcard/Sipdroid_Recordings/" + format2445 + ".wav", n);
	}

	private void checkClose() {
		if (this.outgoingStopped && this.incomingStopped && this.callWav != null) {
			this.callWav.close();
			this.callWav = null;
		}
	}

	public void stopIncoming() {
		this.incomingStopped = true;
		this.checkClose();
	}

	public void stopOutgoing() {
		this.outgoingStopped = true;
		this.checkClose();
	}

	public void writeIncoming(final short[] array, final int n, final int n2) {
		if (this.callWav == null) {
			return;
		}
		this.callWav.writeLeft(array, n, n2);
	}

	public void writeOutgoing(final short[] array, final int n, final int n2) {
		if (this.callWav == null) {
			return;
		}
		this.callWav.writeRight(array, n, n2);
	}
}
