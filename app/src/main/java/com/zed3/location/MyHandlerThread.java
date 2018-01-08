package com.zed3.location;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class MyHandlerThread extends HandlerThread {
	public static int mLimitMaxCount;
	SQLiteDatabase db;
	public GPSInfoDataBase gpsDB;
	public InnerHandler mInnerHandler;

	static {
		MyHandlerThread.mLimitMaxCount = 140000;
	}

	public MyHandlerThread(final String s) {
		super(s);
		this.gpsDB = GPSInfoDataBase.getInstance();
		this.db = this.gpsDB.getWritableDatabase();
	}

	public MyHandlerThread(final String s, final int n) {
		super(s, n);
	}

	protected void onLooperPrepared() {
		super.onLooperPrepared();
		this.mInnerHandler = new InnerHandler(this.getLooper());
	}

	public void sendMessage(final Message message) {
		if (this.isAlive() && this.mInnerHandler != null) {
			this.mInnerHandler.sendMessage(message);
		}
	}

	public void stopSelf() {
		if (this.isAlive()) {
			this.quit();
		}
		if (this.db != null) {
			this.db.close();
			this.db = null;
		}
	}

	private final class InnerHandler extends Handler {
		public InnerHandler(final Looper looper) {
			super(looper);
		}

		public void handleMessage(final Message message) {
			switch (message.what) {
				case 1: {
					GPSInfoDataBase.getInstance().addInfo((GpsInfo) message.obj);
				}
				case 2: {
					GPSInfoDataBase.getInstance().delete((String) message.obj);
				}
				case 3: {
					final String[] array = (String[]) message.obj;
					for (int length = array.length, i = 0; i < length; ++i) {
						GPSInfoDataBase.getInstance().delete(array[i]);
					}
					break;
				}
			}
		}
	}
}
