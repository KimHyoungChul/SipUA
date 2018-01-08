package com.zed3.flow;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;

public class TotalFlowThread extends Thread {
	boolean Flag;
	private final String TAG;
	private int TagNum;
	private double Total_3GData;
	private double Total_3GDataPTT;
	private double Total_3GDataPTT_old;
	private double Total_3GDataVideo;
	private double Total_3GDataVideo_old;
	private double Total_3GData_old;
	private int Total_Data;
	private int Total_DataPTT;
	private int Total_DataVideo;
	private double Upload_3GData;
	double a;
	boolean alarmFlag;
	private Context context;
	int count;
	boolean is3GFlag;
	Handler mHandle;
	private double subData;
	UserAgent ua;
	boolean wifiFlag;

	public TotalFlowThread(final Context context) {
		this.TAG = "TotalFlowThread";
		this.TagNum = 5;
		this.Flag = false;
		this.Total_Data = 0;
		this.Total_DataPTT = 0;
		this.Total_DataVideo = 0;
		this.is3GFlag = false;
		this.Total_3GData = 0.0;
		this.Total_3GDataPTT = 0.0;
		this.Total_3GDataVideo = 0.0;
		this.Upload_3GData = 0.0;
		this.subData = 0.0;
		this.Total_3GData_old = 0.0;
		this.Total_3GDataPTT_old = 0.0;
		this.Total_3GDataVideo_old = 0.0;
		this.count = 0;
		this.ua = null;
		this.wifiFlag = false;
		this.alarmFlag = false;
		this.a = 0.0;
		this.mHandle = new Handler() {
			public void handleMessage(final Message message) {
				if (message.what == 1) {
					if (message.arg1 != 1) {
						TotalFlowThread.this.AddNotify(0, "");
						return;
					}
					TotalFlowThread.this.AddNotify(1, "您的流量使用已经接近套餐限值，超过套餐限值将消耗国内流量");
					TotalFlowThread.this.alarmFlag = true;
//					final Context access .1 = TotalFlowThread.this.context;
					TotalFlowThread.this.ua.getClass();
				}
			}
		};
		MyLog.i("TotalFlowThread", "TotalFlowThread Start");
		this.context = context;
		this.Flag = true;
		this.alarmFlag = false;
		this.count = 0;
		this.ua = Receiver.GetCurUA();
	}

	private void AddNotify(final int n, final String s) {
		final NotificationManager notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (n == 0) {
			notificationManager.cancel(101);
			return;
		}
		final Notification notification = new Notification();
		notification.icon = R.drawable.icon22;
		notification.defaults = 1;
		notification.audioStreamType = -1;
//		notification.setLatestEventInfo(this.context, (CharSequence) "\u6d41\u91cf\u63d0\u793a", (CharSequence) s, PendingIntent.getActivity(this.context, 0, new Intent(this.context, (Class) TotalFlowView.class), 1073741824));
		notificationManager.notify(101, notification);
	}

	private boolean CheckNetWork() {
		if (NetChecker.isNetworkAvailable(this.context)) {
			if (NetChecker.isWifi(this.context)) {
				this.is3GFlag = false;
				MyLog.i("TotalFlowThread", "mobile NetWork is run but wifi");
			} else if (NetChecker.is3G(this.context)) {
				this.is3GFlag = true;
				MyLog.i("TotalFlowThread", "mobile NetWork is run and is 3G");
			} else {
				this.is3GFlag = false;
				MyLog.i("TotalFlowThread", "mobile NetWork is run but availbale");
			}
		} else {
			this.is3GFlag = false;
			MyLog.e("TotalFlowThread", "mobile NetWork is not run");
		}
		return this.is3GFlag;
	}

	private void ResetData() {
		if (!this.ua.GetCurrentMouth(true).equals(MemoryMg.getInstance().User_3GDBLocalTime.substring(0, 7))) {
			MyLog.i("TotalFlowThread", "ResetData");
			MemoryMg.getInstance().User_3GDBLocalTime = this.ua.GetCurrentMouth(false);
			this.Total_3GData_old = 0.0;
			this.Total_3GDataPTT_old = 0.0;
			this.Total_3GDataVideo_old = 0.0;
			this.Total_3GData = 0.0;
			this.Total_3GDataPTT = 0.0;
			this.Total_3GDataVideo = 0.0;
			this.Upload_3GData = 0.0;
			this.count = 0;
			MemoryMg.getInstance().User_3GRelTotal = 0.0;
			MemoryMg.getInstance().User_3GRelTotalPTT = 0.0;
			MemoryMg.getInstance().User_3GRelTotalVideo = 0.0;
			FlowStatistics.Sip_Send_Data = 0;
			FlowStatistics.Sip_Receive_Data = 0;
			FlowStatistics.Gps_Receive_Data = 0;
			FlowStatistics.Gps_Send_Data = 0;
			FlowStatistics.Voice_Receive_Data = 0;
			FlowStatistics.Voice_Send_Data = 0;
			FlowStatistics.Video_Receive_Data = 0;
			FlowStatistics.Video_Send_Data = 0;
			FlowStatistics.DownLoad_APK = 0;
			this.ua.NetFlowPreferenceEdit("0", "0", "0", this.ua.GetCurrentMouth(false));
			this.ua.Upload3GTotal("0", "0", "0");
		}
	}

	public void StopFlow() {
		this.Flag = false;
		MyLog.i("TotalFlowThread", "StopFlow()");
		this.mHandle.sendMessage(this.mHandle.obtainMessage(1, 0, 0));
	}

	public double calculatePercent(final double n, final double n2) {
		return Math.round(n / n2 * 100.0) / 100.0;
	}

	public double calculateTotal(final double n) {
		return Math.round(n / 1024.0 * 100.0) / 100.0;
	}

	public double calculateTotal(final int n) {
		return Math.round(Double.valueOf(n) / 1024.0 * 100.0) / 100.0;
	}

	@Override
	public void run() {
		while (this.Flag) {
			Label_0512_Outer:
			while (true) {
				while (true) {
					Label_0652:
					{
						while (true) {
							Label_0544:
							{
								try {
									this.ResetData();
									this.Total_DataPTT = FlowStatistics.Voice_Receive_Data + FlowStatistics.Voice_Send_Data;
									this.Total_DataVideo = FlowStatistics.Video_Receive_Data + FlowStatistics.Video_Send_Data;
									this.Total_Data = FlowStatistics.Sip_Send_Data + FlowStatistics.Sip_Receive_Data + FlowStatistics.Gps_Receive_Data + FlowStatistics.Gps_Send_Data + FlowStatistics.DownLoad_APK + this.Total_DataPTT + this.Total_DataVideo;
									MyLog.i("TotalFlowThread", String.valueOf(FlowStatistics.DownLoad_APK) + " UpdateVersionService");
									if (!this.CheckNetWork()) {
										break Label_0652;
									}
									this.wifiFlag = false;
									if (this.Total_3GData_old != 0.0) {
										break Label_0544;
									}
									this.Total_3GData = MemoryMg.getInstance().User_3GRelTotal;
									this.Total_3GDataPTT = MemoryMg.getInstance().User_3GRelTotalPTT;
									this.Total_3GDataVideo = MemoryMg.getInstance().User_3GRelTotalVideo;
									this.Total_3GData_old = this.Total_Data;
									this.Total_3GDataPTT_old = this.Total_DataPTT;
									this.Total_3GDataVideo_old = this.Total_DataVideo;
									if (!this.alarmFlag && MemoryMg.getInstance().User_3GFlowOut > 0.0 && MemoryMg.getInstance().User_3GRelTotal >= MemoryMg.getInstance().User_3GFlowOut * 1024.0 * 1024.0) {
										this.mHandle.sendMessage(this.mHandle.obtainMessage(1, 1, 100));
									}
									if (this.count >= 60 / this.TagNum) {
										MyLog.i("TotalFlowThread", String.valueOf(this.Total_3GData + this.Total_3GDataPTT + this.Total_3GDataVideo) + "save as db");
										this.count = 0;
										this.ua.NetFlowPreferenceEdit(new StringBuilder(String.valueOf(this.Total_3GData)).toString(), new StringBuilder(String.valueOf(this.Total_3GDataPTT)).toString(), new StringBuilder(String.valueOf(this.Total_3GDataVideo)).toString(), this.ua.GetCurrentMouth(false));
									}
									if (this.calculateTotal(this.Upload_3GData) > 200.0) {
										this.Upload_3GData = 0.0;
										MyLog.i("TotalFlowThread", String.valueOf(this.Total_3GData + this.Total_3GDataPTT + this.Total_3GDataVideo) + "save as network");
										this.ua.Upload3GTotal(new StringBuilder(String.valueOf(this.Total_3GData)).toString(), new StringBuilder(String.valueOf(this.Total_3GDataPTT)).toString(), new StringBuilder(String.valueOf(this.Total_3GDataVideo)).toString());
									}
									++this.count;
									MemoryMg.getInstance().User_3GRelTotal = this.Total_3GData;
									MemoryMg.getInstance().User_3GRelTotalPTT = this.Total_3GDataPTT;
									MemoryMg.getInstance().User_3GRelTotalVideo = this.Total_3GDataVideo;
									Thread.sleep(this.TagNum * 1000);
								} catch (Exception ex) {
									MyLog.e("TotalFlowThread", ex.toString());
									ex.printStackTrace();
								}
								break;
							}
							this.subData = this.Total_Data - this.Total_3GData_old;
							this.Upload_3GData += this.subData;
							this.Total_3GData += this.subData;
							this.Total_3GDataPTT += this.Total_DataPTT - this.Total_3GDataPTT_old;
							this.Total_3GDataVideo += this.Total_DataVideo - this.Total_3GDataVideo_old;
							this.Total_3GData_old = this.Total_Data;
							this.Total_3GDataPTT_old = this.Total_DataPTT;
							this.Total_3GDataVideo_old = this.Total_DataVideo;
							continue Label_0512_Outer;
						}
					}
					if (!this.wifiFlag) {
						this.wifiFlag = true;
						this.ua.NetFlowPreferenceEdit(new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GRelTotal)).toString(), new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GRelTotalPTT)).toString(), new StringBuilder(String.valueOf(MemoryMg.getInstance().User_3GRelTotalVideo)).toString(), this.ua.GetCurrentMouth(false));
					}
					this.Total_3GData_old = 0.0;
					this.Total_3GDataPTT_old = 0.0;
					this.Total_3GDataVideo_old = 0.0;
					this.Total_3GData = 0.0;
					this.Total_3GDataPTT = 0.0;
					this.Total_3GDataVideo = 0.0;
					this.Upload_3GData = 0.0;
					this.count = 0;
					continue;
				}
			}
		}
	}
}
