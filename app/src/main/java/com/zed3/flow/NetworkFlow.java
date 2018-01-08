package com.zed3.flow;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.zed3.sipua.R;

public class NetworkFlow extends Activity {
	private static WindowManager.LayoutParams params;
	private static WindowManager wm;
	private Button btn_floatView;
	private boolean isAdded;

	public NetworkFlow() {
		this.isAdded = false;
	}

	private void createFloatView() {
		(this.btn_floatView = new Button(this.getApplicationContext())).setText(R.string.sus_window);
		NetworkFlow.wm = (WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		NetworkFlow.params = new WindowManager.LayoutParams();
		NetworkFlow.params.type = 2003;
		NetworkFlow.params.format = 1;
		NetworkFlow.params.flags = 40;
		NetworkFlow.params.width = 100;
		NetworkFlow.params.height = 100;
		this.btn_floatView.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			int lastX;
			int lastY;
			int paramX;
			int paramY;

			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					case 0: {
						this.lastX = (int) motionEvent.getRawX();
						this.lastY = (int) motionEvent.getRawY();
						this.paramX = NetworkFlow.params.x;
						this.paramY = NetworkFlow.params.y;
						break;
					}
					case 2: {
						final int n = (int) motionEvent.getRawX();
						final int lastX = this.lastX;
						final int n2 = (int) motionEvent.getRawY();
						final int lastY = this.lastY;
						NetworkFlow.params.x = this.paramX + (n - lastX);
						NetworkFlow.params.y = this.paramY + (n2 - lastY);
						NetworkFlow.wm.updateViewLayout((View) NetworkFlow.this.btn_floatView, (ViewGroup.LayoutParams) NetworkFlow.params);
						break;
					}
				}
				return true;
			}
		});
		NetworkFlow.wm.addView((View) this.btn_floatView, (ViewGroup.LayoutParams) NetworkFlow.params);
		this.isAdded = true;
	}
}
