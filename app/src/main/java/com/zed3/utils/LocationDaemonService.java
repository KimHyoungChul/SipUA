package com.zed3.utils;

import android.os.Handler;
import android.os.Looper;

import com.zed3.log.MyLog;

public class LocationDaemonService implements Runnable {
	private static final long DAEMON_DELAY_TIME = 180000L;
	private static final long LOCATE_TIME_OUT_LIMIT = 180000L;
	private static final String TAG;
	private static final LocationDaemonService sDefault;
	private static long sLastLocationTime;
	private boolean mCalled;
	private Handler mHandler;
	private int mLocationCount;
	private Looper mLooper;
	private OnCatchLocationException mOnCatchException;
	Runnable mRunner;

	static {
		TAG = LocationDaemonService.class.getSimpleName();
		sDefault = new LocationDaemonService();
		LocationDaemonService.sLastLocationTime = 0L;
	}

	public LocationDaemonService() {
		this.mLocationCount = 0;
		this.mCalled = false;
		this.mRunner = new Runnable() {
			@Override
			public void run() {
				Label_0140:
				{
					try {
						LocationDaemonService.this.minutesNotifier();
						final long currentTimeMillis = System.currentTimeMillis();
						if (LocationDaemonService.this.mLocationCount == 0) {
							MyLog.d(LocationDaemonService.TAG, "run location count 0");
							LocationDaemonService.this.onCatchLocationExceptionNotifier();
						} else {
							if (currentTimeMillis - LocationDaemonService.sLastLocationTime <= 180000L) {
								break Label_0140;
							}
							MyLog.d(LocationDaemonService.TAG, "run location time out");
							LocationDaemonService.this.onCatchLocationExceptionNotifier();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						MyLog.e(LocationDaemonService.TAG, "location daemon service exception = " + ex.getMessage());
						return;
//						MyLog.d(LocationDaemonService.TAG, "run location count = " + LocationDaemonService.this.mLocationCount);
					} finally {
						LocationDaemonService.this.mHandler.postDelayed(LocationDaemonService.this.mRunner, 180000L);
					}
				}
			}
		};
	}

	public static LocationDaemonService getDefault() {
		return LocationDaemonService.sDefault;
	}

	private void onStartRun() {
		this.mHandler.postDelayed(this.mRunner, 180000L);
	}

	private void reset() {
		this.mCalled = false;
		LocationDaemonService.sLastLocationTime = 0L;
		this.mLocationCount = 0;
	}

	private void stop() {
		MyLog.d(LocationDaemonService.TAG, "stop daemon service");
		if (!this.mCalled) {
			MyLog.d(LocationDaemonService.TAG, "stopped");
		} else {
			if (this.mHandler != null) {
				this.mHandler.removeCallbacks(this.mRunner);
				this.mHandler = null;
			}
			this.reset();
			if (this.mLooper != null) {
				this.mLooper.quit();
				this.mLooper = null;
			}
		}
	}

	protected void minutesNotifier() {
		if (this.mOnCatchException != null) {
			MyLog.d(LocationDaemonService.TAG, "minutesNotifier()");
			return;
		}
		MyLog.e(LocationDaemonService.TAG, "minutesNotifier() mOnCatchException is null");
	}

	protected void onCatchLocationExceptionNotifier() {
		if (this.mOnCatchException != null) {
			MyLog.d(LocationDaemonService.TAG, "onCatchLocationExceptionNotifier()");
			this.mOnCatchException.catchException();
			return;
		}
		MyLog.e(LocationDaemonService.TAG, "onCatchLocationExceptionNotifier() mOnCatchException is null");
	}

	public void onLocationChanged() {
		MyLog.d(LocationDaemonService.TAG, "onLocationChanged enter");
		LocationDaemonService.sLastLocationTime = System.currentTimeMillis();
		++this.mLocationCount;
	}

	@Override
	public void run() {
		Looper.prepare();
		this.mLooper = Looper.myLooper();
		this.mHandler = new Handler(this.mLooper);
		this.onStartRun();
		Looper.loop();
	}

	public LocationDaemonService setOnCatchExceptionHandler(final OnCatchLocationException mOnCatchException) {
		this.mOnCatchException = mOnCatchException;
		return this;
	}

	public void start() {
		MyLog.d(LocationDaemonService.TAG, "start daemon service enter");
		if (this.mCalled) {
			MyLog.d(LocationDaemonService.TAG, "started");
			return;
		}
		new Thread(this).start();
		this.mCalled = true;
	}

	public interface OnCatchLocationException {
		void catchException();
	}
}
