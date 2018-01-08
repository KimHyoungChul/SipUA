package com.zed3.sipua.message;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;

import java.util.Timer;
import java.util.TimerTask;

public class AlarmService extends Service {
	private long downtime = 0;
	private Handler handler;
	private ImageView img;
	private Context mContext;
	private WindowManager mWindowManager;
	private Timer timer = null;
	private Timer timer1 = null;
	private long uptime = 0;
	private View view;
	private LayoutParams wmParams;

	class C10941 implements OnTouchListener {
		int lastX;
		int lastY;
		int paramX;
		int paramY;

		class C10911 extends TimerTask {
			C10911() {
			}

			public void run() {
				Looper.prepare();
				if (CallUtil.isInCall()) {
					Receiver.engine(SipUAApp.mContext).rejectcall();
				}
				Looper.loop();
			}
		}

		class C10932 extends TimerTask {

			class C10921 implements Runnable {
				C10921() {
				}

				public void run() {
					AlarmService.this.img.setImageResource(R.drawable.a_key_alarm);
				}
			}

			C10932() {
			}

			public void run() {
				Looper.prepare();
				if (DeviceInfo.svpnumber.equals("")) {
					MyToast.showToast(true, Receiver.mContext, Receiver.mContext.getString(R.string.unavailable_cno));
				} else {
					DeviceInfo.isEmergency = true;
					CallUtil.makeSOSCall(Receiver.mContext, DeviceInfo.svpnumber, null);
					AlarmService.this.handler.post(new C10921());
				}
				Looper.loop();
			}
		}

		C10941() {
		}

		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case 0:
					AlarmService.this.img.setImageResource(R.drawable.a_key_alarm_down);
					AlarmService.this.downtime = System.currentTimeMillis();
					this.lastX = (int) event.getRawX();
					this.lastY = (int) event.getRawY();
					this.paramX = AlarmService.this.wmParams.x;
					this.paramY = AlarmService.this.wmParams.y;
					AlarmService.this.timer = new Timer();
					AlarmService.this.timer1 = new Timer();
					AlarmService.this.timer1.schedule(new C10911(), 1000);
					AlarmService.this.timer.schedule(new C10932(), 2000);
					break;
				case 1:
					AlarmService.this.img.setImageResource(R.drawable.a_key_alarm);
					AlarmService.this.uptime = System.currentTimeMillis();
					if (AlarmService.this.timer != null) {
						AlarmService.this.timer.cancel();
						AlarmService.this.timer = null;
					}
					if (AlarmService.this.timer1 != null) {
						AlarmService.this.timer1.cancel();
						AlarmService.this.timer1 = null;
						break;
					}
					break;
				case 2:
					int dx = ((int) event.getRawX()) - this.lastX;
					int dy = ((int) event.getRawY()) - this.lastY;
					if (Math.abs(dx) >= 2 && Math.abs(dy) >= 2) {
						AlarmService.this.wmParams.x = this.paramX + dx;
						AlarmService.this.wmParams.y = this.paramY + dy;
						AlarmService.this.mWindowManager.updateViewLayout(v, AlarmService.this.wmParams);
						break;
					}
			}
			return true;
		}
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		MyLog.i("AlarmService", "--++>>onCreate()");
		this.view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.yijian_gaojing, null);
		this.img = (ImageView) this.view.findViewById(R.id.a_key_alarm);
		this.handler = new Handler();
		this.mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		this.wmParams = new LayoutParams();
		this.wmParams.type = 2002;
		LayoutParams layoutParams = this.wmParams;
		layoutParams.flags |= 8;
		this.wmParams.x = 250;
		this.wmParams.y = 250;
		this.wmParams.width = -2;
		this.wmParams.height = -2;
		this.wmParams.format = 1;
		this.mWindowManager.addView(this.view, this.wmParams);
		this.view.setOnTouchListener(new C10941());
		super.onCreate();
	}

	public void onDestroy() {
		MyLog.e("AlarmService", "--++>>onDestroy()");
		this.mWindowManager.removeView(this.view);
		super.onDestroy();
	}
}
