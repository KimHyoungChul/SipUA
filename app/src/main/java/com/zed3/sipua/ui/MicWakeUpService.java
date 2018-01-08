package com.zed3.sipua.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;

public class MicWakeUpService extends Service {
	private final String TAG;
	long curTime;
	boolean flag;
	MediaPlayer mAudioMediaPlayer;
	long time;

	public MicWakeUpService() {
		this.TAG = "MicWakeUpService";
		this.mAudioMediaPlayer = null;
		this.time = 0L;
		this.curTime = 0L;
		this.flag = false;
	}

	private void PlayAudio() {
		try {
			MyLog.i("MicWakeUpService", "PlayAudio");
			(this.mAudioMediaPlayer = MediaPlayer.create((Context) this, R.raw.imreceive)).setVolume(0.0f, 0.0f);
			this.mAudioMediaPlayer.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			MyLog.i("MicWakeUpService", ex.toString());
		}
	}

	private void ReleaseMediaPlayer() {
		this.flag = true;
		if (this.mAudioMediaPlayer != null) {
			this.mAudioMediaPlayer.release();
			this.mAudioMediaPlayer = null;
		}
		MyLog.i("MicWakeUpService", "Releasing media ReleaseMediaPlayer.");
	}

	public IBinder onBind(final Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		this.PlayAudio();
		this.time = System.currentTimeMillis();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!MicWakeUpService.this.flag) {
					MicWakeUpService.this.curTime = System.currentTimeMillis();
					if (MicWakeUpService.this.curTime - MicWakeUpService.this.time > 10000L) {
						MyLog.i("MicWakeUpService", "is 10 second...?");
						if (MicWakeUpService.this.mAudioMediaPlayer != null) {
							MicWakeUpService.this.mAudioMediaPlayer.start();
						}
						MicWakeUpService.this.time = System.currentTimeMillis();
					}
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void onDestroy() {
		super.onDestroy();
		this.ReleaseMediaPlayer();
		MyLog.i("MicWakeUpService", "TestService stop");
	}

	public void onStart(final Intent intent, final int n) {
	}
}
