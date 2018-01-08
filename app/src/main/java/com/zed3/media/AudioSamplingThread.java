package com.zed3.media;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.zed3.codecs.Codec;

public class AudioSamplingThread extends HandlerThread implements Handler.Callback {
	private CallRecorder call_recorder;
	private Codec codec;
	private int mu;

	public AudioSamplingThread(final String s, final int n) {
		super(s, n);
	}

	public boolean handleMessage(final Message message) {
		return false;
	}
}
