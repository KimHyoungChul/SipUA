package com.zed3.window;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;

import com.zed3.utils.LogUtil;

public class MyWindowManager {
	private static final String TAG = "MyWindowManager";
	private static KeyguardManager.KeyguardLock mKeyguardLock;

	public static MyWindowManager getInstance() {
		return InstanceCreater.sInstance;
	}

	private void makeLog(final String s, final String s2) {
		LogUtil.makeLog(s, s2);
	}

	public void disableKeyguard(final Activity activity) {
		while (true) {
			while (true) {
				final StringBuilder sb;
				Label_0106:
				{
					synchronized (this) {
						sb = new StringBuilder("disableKeyguard(by " + activity.getClass() + ")");
						if (Build.MODEL.contains("Coolpad 7296")) {
							activity.getWindow().addFlags(4194304);
							sb.append(" add window flag FLAG_DISMISS_KEYGUARD");
						} else {
							if (Build.VERSION.SDK_INT < 14) {
								break Label_0106;
							}
							activity.getWindow().addFlags(524288);
							sb.append(" add window flag FLAG_SHOW_WHEN_LOCKED");
						}
						this.makeLog("MyWindowManager", sb.toString());
						return;
					}
				}
				final Activity activity2 = null;
				final KeyguardManager keyguardManager = (KeyguardManager) activity2.getSystemService(Context.KEYGUARD_SERVICE);
				MyWindowManager.mKeyguardLock = keyguardManager.newKeyguardLock(activity2.getClass().toString());
				if (keyguardManager.inKeyguardRestrictedInputMode()) {
					MyWindowManager.mKeyguardLock.disableKeyguard();
					sb.append(" disableKeyguard");
					continue;
				}
				continue;
			}
		}
	}

	public void reenableKeyguard(final Activity activity) {
		synchronized (this) {
			final StringBuilder sb = new StringBuilder("reenableKeyguard(by " + activity.getClass() + ")");
			if (MyWindowManager.mKeyguardLock != null) {
				MyWindowManager.mKeyguardLock.reenableKeyguard();
				sb.append(" reenableKeyguard()");
			} else {
				sb.append(" mKeyguardLock is null ignore");
			}
			this.makeLog("MyWindowManager", sb.toString());
		}
	}

	private static final class InstanceCreater {
		public static MyWindowManager sInstance;

		static {
			InstanceCreater.sInstance = new MyWindowManager();
		}
	}
}
