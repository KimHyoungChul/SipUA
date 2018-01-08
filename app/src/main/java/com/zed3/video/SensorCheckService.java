package com.zed3.video;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.OrientationEventListener;

public class SensorCheckService extends Service {
	MyOrientationEventListener listener;

	public IBinder onBind(final Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		this.listener = new MyOrientationEventListener((Context) this);
		if (this.listener.canDetectOrientation()) {
			this.listener.enable();
		}
	}

	public void onDestroy() {
		if (this.listener != null) {
			this.listener.disable();
		}
		super.onDestroy();
	}

	public void onStart(final Intent intent, final int n) {
		super.onStart(intent, n);
	}

	class MyOrientationEventListener extends OrientationEventListener {
		int lastRotation;

		public MyOrientationEventListener(final Context context) {
			super(context);
			this.lastRotation = 0;
		}

		public void onOrientationChanged(int lastRotation) {
			lastRotation %= 360;
			if ((lastRotation >= 0 && lastRotation < 45) || (lastRotation >= 315 && lastRotation < 360)) {
				lastRotation = 0;
			} else if (lastRotation >= 45 && lastRotation < 135) {
				lastRotation = 1;
			} else if (lastRotation >= 135 && lastRotation < 225) {
				lastRotation = 2;
			} else {
				lastRotation = 3;
			}
			if (this.lastRotation != lastRotation) {
				Log.i("orientationTest", "value = " + lastRotation);
				this.lastRotation = lastRotation;
				DeviceVideoInfo.curAngle = lastRotation * 90;
				SensorCheckService.this.sendBroadcast(new Intent("com.zed3.siupa.ui.restartcamera"));
			}
		}
	}
}
