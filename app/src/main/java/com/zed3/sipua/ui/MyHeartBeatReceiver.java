package com.zed3.sipua.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.zed3.utils.LogUtil;

public class MyHeartBeatReceiver extends BroadcastReceiver {
	public static void process() {
		synchronized (MyHeartBeatReceiver.class) {
			MyHeartBeatMessageHandler instance = MyHeartBeatMessageHandler.getInstance();
			instance.removeMessages(0);
			instance.sendMessageDelayed(instance.obtainMessage(0), 35000L);
			Receiver.alarm(30, MyHeartBeatReceiver.class);
			if (Receiver.mSipdroidEngine != null && Receiver.mSipdroidEngine.isRegistered(true)) {
				Receiver.GetCurUA().sendHeartBeat();
			}
		}
	}

	public static void start(final String msg) {
		synchronized (MyHeartBeatReceiver.class) {
			LogUtil.makeLog("heartbeattraces", new StringBuilder(String.valueOf(msg)).append(" MyHeartBeatReceiver#start").toString());
			process();
		}
	}

	public static void stop(final String msg) {
		synchronized (MyHeartBeatReceiver.class) {
			LogUtil.makeLog("heartbeattraces", new StringBuilder(String.valueOf(msg)).append(" MyHeartBeatReceiver#start").toString());
			if (MyHeartBeatMessageHandler.getInstance() != null) {
				MyHeartBeatMessageHandler.getInstance().removeMessages(0);
			}
			Receiver.alarm(0, MyHeartBeatReceiver.class);
		}
	}

	public void onReceive(final Context context, final Intent intent) {
		process();
	}

	public static class MyHeartBeatMessageHandler extends Handler {
		private static MyHeartBeatMessageHandler sInstance;

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			MyHeartBeatReceiver.process();
		}

		public static MyHeartBeatMessageHandler getInstance() {
			return sInstance;
		}

		public static synchronized void createInstance() {
			synchronized (MyHeartBeatMessageHandler.class) {
				if (sInstance == null) {
					if (Thread.currentThread().getName().equals("main")) {
						sInstance = new MyHeartBeatMessageHandler();
					} else {
						throw new RuntimeException("invoke not in main thread exception ");
					}
				}
			}
		}
	}
}
