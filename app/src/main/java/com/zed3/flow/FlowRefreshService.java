package com.zed3.flow;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;

import java.util.Timer;
import java.util.TimerTask;

public class FlowRefreshService extends Service {
	public static Boolean isAddView_ = Boolean.valueOf(true);
	private int Gps_Receive_Data = 0;
	private int Gps_Send_Data = 0;
	private int Sip_Receive_Data = 0;
	private int Sip_Send_Data = 0;
	private int Total_Data = 0;
	private int Total_Data_old = 0;
	private int Video_Receive_Data = 0;
	private int Video_Send_Data = 0;
	private int Voice_Receive_Data = 0;
	private int Voice_Send_Data = 0;
	TextView gps_receive;
	TextView gps_send;
	private int isEasy = 0;
	private boolean isMoved = false;
	Handler mHandle = new C09641();
	TextView sip_receive;
	TextView sip_send;
	Timer timer = null;
	TextView total_flow;
	TextView total_rate;
	TimerTask tt;
	TextView video_lost_count;
	TextView video_receive;
	TextView video_send;
	View view;
	View view_;
	TextView voice_receive;
	TextView voice_send;
	WindowManager wm;
	LayoutParams wmParams;
	LayoutParams wmParams_;

	class C09641 extends Handler {
		C09641() {
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					if (FlowRefreshService.this.isEasy == 0) {
						FlowRefreshService flowRefreshService = FlowRefreshService.this;
						flowRefreshService.isEasy = flowRefreshService.isEasy + 1;
						return;
					}
					MyLog.e("Flow===>", "fresh UI UI UI==>>>");
					MyLog.e("Flow===>", new StringBuilder(String.valueOf(FlowStatistics.Voice_Send_Data)).toString());
					MyLog.e("Flow===>", new StringBuilder(String.valueOf(FlowStatistics.Voice_Send_Data)).toString());
					FlowRefreshService.this.sip_send.setText(FlowStatistics.Sip_Send + "KB/s");
					FlowRefreshService.this.sip_receive.setText(FlowStatistics.Sip_Receive + "KB/s");
					FlowRefreshService.this.gps_send.setText(FlowStatistics.Gps_Send + "KB/s");
					FlowRefreshService.this.gps_receive.setText(FlowStatistics.Gps_Receive + "KB/s");
					FlowRefreshService.this.voice_send.setText(FlowStatistics.Voice_Send + "KB/s");
					FlowRefreshService.this.voice_receive.setText(FlowStatistics.Voice_Receive + "KB/s");
					FlowRefreshService.this.video_send.setText(FlowStatistics.Video_Send + "KB/s");
					FlowRefreshService.this.video_receive.setText(FlowStatistics.Video_Receive + "KB/s");
					FlowRefreshService.this.total_rate.setText(FlowStatistics.Total + "KB/s");
					FlowRefreshService.this.video_lost_count.setText(new StringBuilder(String.valueOf(FlowStatistics.Video_Packet_Lost)).toString());
					FlowRefreshService.this.total_flow.setText(FlowStatistics.Total_Flow + "KB");
					MyLog.e("Build.MODEL==》", Build.MODEL);
					return;
				default:
					return;
			}
		}
	}

	class C09652 implements OnTouchListener {
		int dx;
		int dy;
		int lastX;
		int lastY;
		int paramX;
		int paramY;

		C09652() {
		}

		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case 0:
					System.out.println("--------------ACTION_DOWN");
					FlowRefreshService.this.isMoved = false;
					this.lastX = (int) event.getRawX();
					this.lastY = (int) event.getRawY();
					this.dx = 0;
					this.dy = 0;
					this.paramX = FlowRefreshService.this.wmParams.x;
					this.paramY = FlowRefreshService.this.wmParams.y;
					break;
				case 1:
					System.out.println("--------------ACTION_UP");
					if (Math.abs(this.dx) >= 10 || Math.abs(this.dy) >= 10) {
						FlowRefreshService.this.isMoved = true;
						break;
					}
				case 2:
					System.out.println("--------------ACTION_MOVE");
					this.dx = ((int) event.getRawX()) - this.lastX;
					this.dy = ((int) event.getRawY()) - this.lastY;
					FlowRefreshService.this.wmParams.x = this.paramX + this.dx;
					FlowRefreshService.this.wmParams.y = this.paramY + this.dy;
					FlowRefreshService.this.wm.updateViewLayout(v, FlowRefreshService.this.wmParams);
					break;
			}
			return FlowRefreshService.this.isMoved;
		}
	}

	class C09673 implements OnClickListener {

		class C09661 extends TimerTask {
			C09661() {
			}

			public void run() {
				FlowRefreshService.this.Total_Data = ((((((FlowStatistics.Sip_Send_Data + FlowStatistics.Sip_Receive_Data) + FlowStatistics.Gps_Receive_Data) + FlowStatistics.Gps_Send_Data) + FlowStatistics.Voice_Receive_Data) + FlowStatistics.Voice_Send_Data) + FlowStatistics.Video_Receive_Data) + FlowStatistics.Video_Send_Data;
				if (FlowStatistics.Sip_Send_Data - FlowRefreshService.this.Sip_Send_Data > 0) {
					FlowStatistics.Sip_Send = new StringBuilder(String.valueOf(FlowRefreshService.this.calculate(FlowStatistics.Sip_Send_Data - FlowRefreshService.this.Sip_Send_Data))).toString();
					FlowRefreshService.this.Sip_Send_Data = FlowStatistics.Sip_Send_Data;
				} else {
					FlowStatistics.Sip_Send = "0.00";
					FlowRefreshService.this.Sip_Send_Data = FlowStatistics.Sip_Send_Data;
				}
				if (FlowStatistics.Sip_Receive_Data - FlowRefreshService.this.Sip_Receive_Data > 0) {
					FlowStatistics.Sip_Receive = new StringBuilder(String.valueOf(FlowRefreshService.this.calculate(FlowStatistics.Sip_Receive_Data - FlowRefreshService.this.Sip_Receive_Data))).toString();
					FlowRefreshService.this.Sip_Receive_Data = FlowStatistics.Sip_Receive_Data;
				} else {
					FlowStatistics.Sip_Receive = "0.00";
					FlowRefreshService.this.Sip_Receive_Data = FlowStatistics.Sip_Receive_Data;
				}
				if (FlowStatistics.Gps_Receive_Data - FlowRefreshService.this.Gps_Receive_Data > 0) {
					FlowStatistics.Gps_Receive = new StringBuilder(String.valueOf(FlowRefreshService.this.calculate(FlowStatistics.Gps_Receive_Data - FlowRefreshService.this.Gps_Receive_Data))).toString();
					FlowRefreshService.this.Gps_Receive_Data = FlowStatistics.Gps_Receive_Data;
				} else {
					FlowStatistics.Gps_Receive = "0.00";
					FlowRefreshService.this.Gps_Receive_Data = FlowStatistics.Gps_Receive_Data;
				}
				if (FlowStatistics.Gps_Send_Data - FlowRefreshService.this.Gps_Send_Data > 0) {
					FlowStatistics.Gps_Send = new StringBuilder(String.valueOf(FlowRefreshService.this.calculate(FlowStatistics.Gps_Send_Data - FlowRefreshService.this.Gps_Send_Data))).toString();
					FlowRefreshService.this.Gps_Send_Data = FlowStatistics.Gps_Send_Data;
				} else {
					FlowStatistics.Gps_Send = "0.00";
					FlowRefreshService.this.Gps_Send_Data = FlowStatistics.Gps_Send_Data;
				}
				if (FlowStatistics.Voice_Receive_Data - FlowRefreshService.this.Voice_Receive_Data > 0) {
					FlowStatistics.Voice_Receive = new StringBuilder(String.valueOf(FlowRefreshService.this.calculate(FlowStatistics.Voice_Receive_Data - FlowRefreshService.this.Voice_Receive_Data))).toString();
					FlowRefreshService.this.Voice_Receive_Data = FlowStatistics.Voice_Receive_Data;
				} else {
					FlowStatistics.Voice_Receive = "0.00";
					FlowRefreshService.this.Voice_Receive_Data = FlowStatistics.Voice_Receive_Data;
				}
				if (FlowStatistics.Voice_Send_Data - FlowRefreshService.this.Voice_Send_Data > 0) {
					FlowStatistics.Voice_Send = new StringBuilder(String.valueOf(FlowRefreshService.this.calculate(FlowStatistics.Voice_Send_Data - FlowRefreshService.this.Voice_Send_Data))).toString();
					FlowRefreshService.this.Voice_Send_Data = FlowStatistics.Voice_Send_Data;
				} else {
					FlowStatistics.Voice_Send = "0.00";
					FlowRefreshService.this.Voice_Send_Data = FlowStatistics.Voice_Send_Data;
				}
				if (FlowStatistics.Video_Send_Data - FlowRefreshService.this.Video_Send_Data > 0) {
					FlowStatistics.Video_Send = new StringBuilder(String.valueOf(FlowRefreshService.this.calculate(FlowStatistics.Video_Send_Data - FlowRefreshService.this.Video_Send_Data))).toString();
					FlowRefreshService.this.Video_Send_Data = FlowStatistics.Video_Send_Data;
				} else {
					FlowStatistics.Video_Send = "0.00";
					FlowRefreshService.this.Video_Send_Data = FlowStatistics.Video_Send_Data;
				}
				if (FlowStatistics.Video_Receive_Data - FlowRefreshService.this.Video_Receive_Data > 0) {
					FlowStatistics.Video_Receive = new StringBuilder(String.valueOf(FlowRefreshService.this.calculate(FlowStatistics.Video_Receive_Data - FlowRefreshService.this.Video_Receive_Data))).toString();
					FlowRefreshService.this.Video_Receive_Data = FlowStatistics.Video_Receive_Data;
				} else {
					FlowStatistics.Video_Receive = "0.00";
					FlowRefreshService.this.Video_Receive_Data = FlowStatistics.Video_Receive_Data;
				}
				if (FlowRefreshService.this.Total_Data - FlowRefreshService.this.Total_Data_old > 0) {
					FlowStatistics.Total = new StringBuilder(String.valueOf(FlowRefreshService.this.calculate(FlowRefreshService.this.Total_Data - FlowRefreshService.this.Total_Data_old))).toString();
					FlowRefreshService.this.Total_Data_old = FlowRefreshService.this.Total_Data;
				} else {
					FlowStatistics.Total = "0.00";
					FlowRefreshService.this.Total_Data_old = FlowRefreshService.this.Total_Data;
				}
				FlowStatistics.Total_Flow = FlowRefreshService.this.calculateTotal(FlowRefreshService.this.Total_Data);
				Message ms = new Message();
				ms.what = 1;
				FlowRefreshService.this.mHandle.sendMessage(ms);
			}
		}

		C09673() {
		}

		public void onClick(View v) {
			System.out.println("--------------onClick");
			if (FlowRefreshService.isAddView_.booleanValue()) {
				FlowRefreshService.isAddView_ = Boolean.valueOf(false);
				try {
					FlowRefreshService.this.wm.addView(FlowRefreshService.this.view_, FlowRefreshService.this.wmParams_);
				} catch (Exception e) {
					Log.e("Coolpad or HUAWEI error", e.toString());
					e.printStackTrace();
				}
				if (FlowRefreshService.this.timer == null) {
					FlowRefreshService.this.timer = new Timer();
					FlowRefreshService.this.timer.schedule(new C09661(), 0, 3000);
					return;
				}
				return;
			}
			if (FlowRefreshService.this.timer != null) {
				FlowRefreshService.this.timer.cancel();
				FlowRefreshService.this.timer = null;
			}
			if (!FlowRefreshService.isAddView_.booleanValue()) {
				try {
					FlowRefreshService.this.wm.removeView(FlowRefreshService.this.view_);
				} catch (Exception e2) {
					Log.e("Coolpad or HUAWEI error", e2.toString());
					e2.printStackTrace();
				}
			}
			FlowRefreshService.isAddView_ = Boolean.valueOf(true);
		}
	}

	class C09684 implements OnTouchListener {
		int lastX;
		int lastY;
		int paramX;
		int paramY;

		C09684() {
		}

		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case 0:
					this.lastX = (int) event.getRawX();
					this.lastY = (int) event.getRawY();
					this.paramX = FlowRefreshService.this.wmParams_.x;
					this.paramY = FlowRefreshService.this.wmParams_.y;
					break;
				case 2:
					int dx = ((int) event.getRawX()) - this.lastX;
					int dy = ((int) event.getRawY()) - this.lastY;
					FlowRefreshService.this.wmParams_.x = this.paramX + dx;
					FlowRefreshService.this.wmParams_.y = this.paramY + dy;
					FlowRefreshService.this.wm.updateViewLayout(v, FlowRefreshService.this.wmParams_);
					break;
			}
			return true;
		}
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		this.view_ = View.inflate(this, R.layout.flow_details_message, null);
		this.sip_send = (TextView) this.view_.findViewById(R.id.sip_send);
		this.sip_receive = (TextView) this.view_.findViewById(R.id.sip_receive);
		this.gps_send = (TextView) this.view_.findViewById(R.id.gps_send);
		this.gps_receive = (TextView) this.view_.findViewById(R.id.gps_receive);
		this.voice_send = (TextView) this.view_.findViewById(R.id.voice_send);
		this.voice_receive = (TextView) this.view_.findViewById(R.id.voice_receive);
		this.video_send = (TextView) this.view_.findViewById(R.id.video_send);
		this.video_receive = (TextView) this.view_.findViewById(R.id.video_receive);
		this.total_rate = (TextView) this.view_.findViewById(R.id.total_rate);
		this.video_lost_count = (TextView) this.view_.findViewById(R.id.video_lost_count);
		this.total_flow = (TextView) this.view_.findViewById(R.id.total_flow);
		initView();
	}

	public void onDestroy() {
		this.wm.removeView(this.view);
		if (!isAddView_.booleanValue()) {
			this.wm.removeView(this.view_);
		}
		isAddView_ = Boolean.valueOf(true);
		if (this.timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
		super.onDestroy();
	}

	private void initView() {
		this.view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.window, null);
		this.wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		this.wmParams = new LayoutParams();
		this.wmParams.type = 2002;
		LayoutParams layoutParams = this.wmParams;
		layoutParams.flags |= 8;
		this.wmParams.x = 250;
		this.wmParams.y = 50;
		this.wmParams.width = -2;
		this.wmParams.height = -2;
		this.wmParams.format = 1;
		this.wmParams_ = new LayoutParams();
		this.wmParams_.type = 2002;
		layoutParams = this.wmParams_;
		layoutParams.flags |= 8;
		this.wmParams_.x = -100;
		this.wmParams_.y = 0;
		this.wmParams_.width = -2;
		this.wmParams_.height = -2;
		this.wmParams_.format = 1;
		this.wm.addView(this.view, this.wmParams);
		this.view.setOnTouchListener(new C09652());
		this.view.setOnClickListener(new C09673());
		this.view_.setOnTouchListener(new C09684());
	}

	public double calculate(int data) {
		return ((double) Math.round(((Double.valueOf((double) data).doubleValue() / 1024.0d) / 3.0d) * 100.0d)) / 100.0d;
	}

	public double calculateTotal(int data) {
		return ((double) Math.round((Double.valueOf((double) data).doubleValue() / 1024.0d) * 100.0d)) / 100.0d;
	}
}
