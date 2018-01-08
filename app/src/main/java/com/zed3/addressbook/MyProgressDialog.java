package com.zed3.addressbook;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class MyProgressDialog extends ProgressDialog {
	public static final String TAG = "ProgressDialog";
	private Handler mHandler;
	private long mTimeOut;
	private OnTimeOutListener mTimeOutListener;
	private Timer mTimer;

	public MyProgressDialog(final Context context) {
		super(context);
		this.mTimeOut = 0L;
		this.mTimeOutListener = null;
		this.mTimer = null;
		this.mHandler = new Handler() {
			public void handleMessage(final Message message) {
				if (MyProgressDialog.this.mTimeOutListener != null) {
					MyProgressDialog.this.mTimeOutListener.onTimeOut(MyProgressDialog.this);
					MyProgressDialog.this.dismiss();
				}
			}
		};
	}

	public static MyProgressDialog createProgressDialog(final Context context, final long n, final OnTimeOutListener onTimeOutListener) {
		final MyProgressDialog myProgressDialog = new MyProgressDialog(context);
		if (n != 0L) {
			myProgressDialog.setTimeOut(n, onTimeOutListener);
		}
		return myProgressDialog;
	}

	public void onStart() {
		super.onStart();
		if (this.mTimeOut != 0L) {
			(this.mTimer = new Timer()).schedule(new TimerTask() {
				@Override
				public void run() {
					MyProgressDialog.this.mHandler.sendMessage(MyProgressDialog.this.mHandler.obtainMessage());
				}
			}, this.mTimeOut);
		}
	}

	protected void onStop() {
		super.onStop();
		if (this.mTimer != null) {
			this.mTimer.cancel();
			this.mTimer = null;
		}
	}

	public void setTimeOut(final long mTimeOut, final OnTimeOutListener mTimeOutListener) {
		this.mTimeOut = mTimeOut;
		if (mTimeOutListener != null) {
			this.mTimeOutListener = mTimeOutListener;
		}
	}

	public interface OnTimeOutListener {
		void onTimeOut(final ProgressDialog p0);
	}
}
