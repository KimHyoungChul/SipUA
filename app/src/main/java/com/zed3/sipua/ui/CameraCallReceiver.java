package com.zed3.sipua.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.splash.UnionLogin;
import com.zed3.toast.MyToast;

public class CameraCallReceiver extends BroadcastReceiver {
	final Handler handle;
	Context mContext;

	public CameraCallReceiver() {
		this.mContext = null;
		this.handle = new Handler() {
			public void handleMessage(final Message message) {
				if (message.what == 1) {
					final Intent intent = new Intent(CameraCallReceiver.this.mContext, (Class) DemoCallScreen.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					CameraCallReceiver.this.mContext.startActivity(intent);
				} else if (message.what == 2) {
					MyToast.showToast(true, CameraCallReceiver.this.mContext, CameraCallReceiver.this.mContext.getResources().getString(R.string.wrong_service_pwd));
					final SharedPreferences.Editor edit = CameraCallReceiver.this.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
					edit.putString("unionpassword", "0");
					edit.commit();
					Receiver.engine(CameraCallReceiver.this.mContext).expire(-1);
					Receiver.onText(3, null, 0, 0L);
					while (true) {
						try {
							Thread.sleep(800L);
							Receiver.engine(CameraCallReceiver.this.mContext).halt();
							CameraCallReceiver.this.mContext.stopService(new Intent(CameraCallReceiver.this.mContext, (Class) RegisterService.class));
							Receiver.alarm(0, OneShotAlarm.class);
							Receiver.alarm(0, MyHeartBeatReceiver.class);
							final Intent intent2 = new Intent(CameraCallReceiver.this.mContext, (Class) UnionLogin.class);
							intent2.putExtra("unionepwderror", true);
							intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							CameraCallReceiver.this.mContext.startActivity(intent2);
						} catch (InterruptedException ex) {
							ex.printStackTrace();
							continue;
						}
						break;
					}
				}
			}
		};
	}

	public void onReceive(final Context mContext, final Intent intent) {
		this.mContext = mContext;
		if (intent.getAction().toString().equals("android.intent.action.StartDemoCallScreen")) {
			MyLog.i("CameraCallReceiver", "android.intent.action.StartDemoCallScreen");
			this.handle.sendMessage(this.handle.obtainMessage(1));
		} else if (intent.getAction().toString().equals("android.intent.action.RestartUnionLogin")) {
			this.handle.sendMessage(this.handle.obtainMessage(2));
		}
	}
}
