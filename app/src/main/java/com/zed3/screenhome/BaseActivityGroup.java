package com.zed3.screenhome;

import android.app.ActivityGroup;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.zed3.sipua.welcome.DeviceInfo;

public class BaseActivityGroup extends ActivityGroup {
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		if (DeviceInfo.CONFIG_SUPPORT_HOMEKEY_BLOCK) {
			final Window window = this.getWindow();
			final WindowManager.LayoutParams attributes = window.getAttributes();
			attributes.flags = Integer.MIN_VALUE;
			attributes.systemUiVisibility = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
			window.setAttributes(attributes);
		}
	}

	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		if (DeviceInfo.CONFIG_SUPPORT_HOMEKEY_BLOCK) {
			switch (n) {
				case 4: {
					return true;
				}
			}
		}
		return super.onKeyDown(n, keyEvent);
	}
}
