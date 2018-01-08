package com.zed3.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.utils.LogUtil;

import java.io.InputStream;
import java.util.HashMap;

public class TipSoundPlayer {
	private static boolean mIsPlaying = false;
	public static long sPlayBeginTime = 0L;
	public static final int sPlayNeedTime = 400;
	protected static String tag;
	private AudioManager mAudioManager;
	private AudioTrack mAudioTrack;
	private Object mLock;
	private HashMap<Integer, Integer> mSoundLoadIDMap;
	private HashMap<Integer, Integer> mSoundLoadIDMap4NormalMode;
	private SoundPool mSoundPool;
	private SoundPool mSoundPool4NormalMode;
	private boolean mUseSoundQueue;
	private AsyncTipSoundHandleThread sTipSoundHandleThread;

	static {
		TipSoundPlayer.tag = "TipSoundPlayer";
	}

	private TipSoundPlayer() {
		this.sTipSoundHandleThread = new AsyncTipSoundHandleThread();
		this.mLock = new Object();
		this.mSoundLoadIDMap = new HashMap<Integer, Integer>();
		this.mSoundLoadIDMap4NormalMode = new HashMap<Integer, Integer>();
		this.mUseSoundQueue = false;
		this.mAudioManager = (AudioManager) SipUAApp.mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	public static TipSoundPlayer getInstance() {
		return InnerTipSoundPlayer.sDefault;
	}

	private void play(int read) {
		final int minBufferSize = AudioTrack.getMinBufferSize(8000, 2, 2);
		final InputStream openRawResource = SipUAApp.mContext.getResources().openRawResource(read);
		try {
			openRawResource.available();
			final byte[] array = new byte[minBufferSize];
			if (this.mAudioTrack == null) {
				this.mAudioTrack = new AudioTrack(0, 8000, 2, 2, minBufferSize, 1);
			}
			this.mAudioTrack.play();
			final byte[] array2 = new byte[320];
			while (true) {
				read = openRawResource.read(array2, 0, 320);
				if (read == -1) {
					break;
				}
				this.mAudioTrack.write(array2, 0, read);
			}
			this.mAudioTrack.write(array2, 0, array2.length);
			openRawResource.close();
			this.mAudioTrack.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void playWAVTipSound(final int arg1) {
		TipSoundPlayer.mIsPlaying = true;
		if (this.mUseSoundQueue) {
			synchronized (this.mLock) {
				if (!this.sTipSoundHandleThread.isAlive() || this.sTipSoundHandleThread.getState() == Thread.State.TERMINATED) {
					this.sTipSoundHandleThread = new AsyncTipSoundHandleThread();
				}
				final Message obtain = Message.obtain();
				obtain.arg1 = arg1;
				this.sTipSoundHandleThread.post(obtain);
				return;
			}
		}
		if (!this.mAudioManager.isBluetoothScoOn()) {
			if (!this.mAudioManager.isBluetoothA2dpOn() && this.mAudioManager.getMode() != 0) {
				LogUtil.makeLog(TipSoundPlayer.tag, "playWAVTipSound()  mSoundPool.play()");
				this.mSoundPool.play((int) this.mSoundLoadIDMap.get(arg1), 1.0f, 1.0f, 0, 0, 1.0f);
			} else {
				LogUtil.makeLog(TipSoundPlayer.tag, "playWAVTipSound()  mSoundPool4NormalMode.play()");
				this.mSoundPool4NormalMode.play((int) this.mSoundLoadIDMap4NormalMode.get(arg1), 1.0f, 1.0f, 0, 0, 1.0f);
			}
		} else {
			LogUtil.makeLog(TipSoundPlayer.tag, "playWAVTipSound()  mSoundPool.play()");
			this.mSoundPool.play((int) this.mSoundLoadIDMap.get(arg1), 1.0f, 1.0f, 0, 0, 1.0f);
		}
		TipSoundPlayer.sPlayBeginTime = System.currentTimeMillis();
	}

	public void exit() {
		if (this.mSoundPool != null) {
			this.mSoundPool.release();
		}
		if (this.mSoundPool4NormalMode != null) {
			this.mSoundPool4NormalMode.release();
		}
	}

	public void init(final Context context) {
		int i = 0;
		final Sound[] values = Sound.values();
		this.mSoundPool = new SoundPool(values.length, 0, 0);
		this.mSoundPool4NormalMode = new SoundPool(values.length, 3, 0);
		while (i < values.length) {
			final int resId = values[i].getResId();
			this.mSoundLoadIDMap.put(resId, this.mSoundPool.load(context, resId, 1));
			this.mSoundLoadIDMap4NormalMode.put(resId, this.mSoundPool4NormalMode.load(context, resId, 1));
			++i;
		}
	}

	public boolean isPlaying() {
		return TipSoundPlayer.mIsPlaying;
	}

	public void play(final Sound sound) {
		if (sound.existSoundResId()) {
			this.playWAVTipSound(sound.getResId());
			return;
		}
		Toast.makeText(SipUAApp.mContext, (CharSequence) "sound not found", Toast.LENGTH_SHORT).show();
	}

	public void quit() {
		this.sTipSoundHandleThread.quit();
		if (this.mAudioTrack != null) {
			this.mAudioTrack.stop();
			this.mAudioTrack.release();
			this.mAudioTrack = null;
		}
	}

	private class AsyncTipSoundHandleThread extends HandlerThread {
		private InnerHanler mHanler;

		public AsyncTipSoundHandleThread() {
			super("BackgroundThread", 0);
			this.ensureThreadLocked();
		}

		private void ensureThreadLocked() {
			this.start();
			this.mHanler = new InnerHanler(this.getLooper());
		}

		public void post(final Message message) {
			synchronized (AsyncTipSoundHandleThread.class) {
				this.mHanler.sendMessage(message);
			}
		}

		private final class InnerHanler extends Handler {
			public InnerHanler(final Looper looper) {
				super(looper);
			}

			public void handleMessage(final Message message) {
				if (message != null) {
					TipSoundPlayer.this.play(message.arg1);
				}
			}
		}
	}

	private static final class InnerTipSoundPlayer {
		public static TipSoundPlayer sDefault;

		static {
			InnerTipSoundPlayer.sDefault = new TipSoundPlayer();
		}
	}

	public enum Sound {
		MESSAGE_ACCEPT("MESSAGE_ACCEPT", 4, R.raw.imreceive),
		PTT_ACCEPT("PTT_ACCEPT", 2, R.raw.pttaccept8k16bit),
		PTT_DOWN("PTT_DOWN", 0, R.raw.on8k16bit),
		PTT_RELEASE("PTT_RELEASE", 3, R.raw.pttrelease8k16bit),
		PTT_UP("PTT_UP", 1, R.raw.off8k16bit);

		private int mSoundId;

		private Sound(final String s, final int n) {
			this.mSoundId = -1;
		}

		private Sound(final String s, final int n, final int mSoundId) {
			this.mSoundId = -1;
			this.mSoundId = mSoundId;
		}

		public boolean existSoundResId() {
			return this.mSoundId != -1;
		}

		public int getResId() {
			return this.mSoundId;
		}
	}
}
