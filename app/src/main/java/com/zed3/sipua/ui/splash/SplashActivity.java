package com.zed3.sipua.ui.splash;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zed3.log.MyLog;
import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.OneShotAlarm;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.RegisterService;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.sipua.welcome.IAutoConfigListener;
import com.zed3.sipua.welcome.LoginActivity;
import com.zed3.toast.MyToast;
import com.zed3.utils.NetworkListenerService;
import com.zed3.utils.Tools;
import com.zed3.zhejiang.ZhejiangReceivier;

import org.zoolu.sip.header.BaseSipHeaders;

public class SplashActivity extends BaseActivity implements IAutoConfigListener {
	private static final int FETCH_INFO_OK = 0;
	private static final int AutoLogin = 2;
	private static final int ManuLogin = 3;
	private static final int BeginRegister = 4;
	private static final int NOPHONENUM = 5;
	private static final int NOSIMCARD = 6;
	private static final int UNKONWNSTATE = 7;
	public static boolean isConferenceVideo = false;
	public static boolean isGsm = false;
	public static boolean isMessage = false;
	private final String TAG = "SplashActivity";
	ImageButton about_btn_show;
	BroadcastReceiver loginReceiver = new C13122();
	Context mContext;
	AutoConfigManager mManager;
	NotificationManager notificationManager;
	Thread t_fetchInfo;
	TextView tv_version;
	private boolean isUnLogin = true;
	private boolean isDialog = false;
	private boolean isThreadStart = false;
	private Looper mLooper;
	private TelephonyManager manager;
	private SharedPreferences sharedPreferences;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case FETCH_INFO_OK:
					String result = msg.getData().getString("result");
					if (result.contains("passwdIncorrect")) {
						result = SplashActivity.this.getResources().getString(R.string.wrong_password);
					} else if (result.contains("userNotExist")) {
						result = SplashActivity.this.getResources().getString(R.string.account_not_exist);
					} else if (result.contains("Timeout")) {
						result = SplashActivity.this.getResources().getString(R.string.timeout);
					} else if (result.contains("netbroken")) {
						result = SplashActivity.this.getResources().getString(R.string.network_exception);
					} else if (result.contains("userOrpwderror")) {
						result = SplashActivity.this.getResources().getString(R.string.wrong_name_or_pwd);
					} else if (result.contains("versionTooLow")) {
						result = SplashActivity.this.getResources().getString(R.string.version_too_low);
					} else if (result.contains("Already Login")) {
						result = SplashActivity.this.getResources().getString(R.string.logged_in_another_place);
					} else if (result.contains("Temporarily Unavailable")) {
						result = SplashActivity.this.getResources().getString(R.string.service_unavailable);
					}
					MyToast.showToast(true, SplashActivity.this, result);
					SplashActivity.this.handler.sendEmptyMessageDelayed(110, 1000);
					return;
				case AutoLogin:
					MyLog.i("Hu.", "enter by auto login************************************");
					if (SplashActivity.this.isUnLogin) {
						SplashActivity.this.startActivity(new Intent(SplashActivity.this, UnionLogin.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					} else {
						SplashActivity.this.startActivity(new Intent(SplashActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					}
					SplashActivity.this.finish();
					return;
				case ManuLogin:
					Bundle bd = msg.getData();
					SplashActivity.this.isDialog = true;
					if (bd != null) {
						String reason = bd.getString("reason");
						if (!(reason == null || reason.equals(""))) {
							Dialog dialog = new Builder(SplashActivity.this).setTitle(R.string.information).setMessage(reason).setPositiveButton(SplashActivity.this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									SplashActivity.this.finish();
									if (SplashActivity.this.sharedPreferences.getBoolean("NetworkListenerService", false)) {
										SplashActivity.this.sharedPreferences.edit().putBoolean("NetworkListenerService", false).apply();
										SplashActivity.this.stopService(new Intent(SplashActivity.this.mContext, NetworkListenerService.class));
									}
									System.exit(0);
									SplashActivity.this.isDialog = false;
								}
							}).create();
							dialog.setCanceledOnTouchOutside(false);
							dialog.show();
						}
					}
					if (!DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
						SplashActivity.this.startActivity(new Intent(SplashActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
						SplashActivity.this.finish();
						return;
					}
					return;
				case BeginRegister:
					if (SplashActivity.this.isUnLogin) {
						SplashActivity.this.startActivity(new Intent(SplashActivity.this, UnionLogin.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
						SplashActivity.this.finish();
						return;
					}
					Receiver.engine(SplashActivity.this.mContext).registerMore();
					return;
				case NOPHONENUM:
					MyToast.showToast(true, SplashActivity.this.mContext, SplashActivity.this.getResources().getString(R.string.fail_to_telnumber));
					return;
				case NOSIMCARD:
					MyToast.showToast(true, SplashActivity.this.mContext, (int) R.string.no_simcard);
					return;
				case UNKONWNSTATE:
					MyToast.showToast(true, SplashActivity.this.mContext, (int) R.string.unknown_state);
					return;
				case 110:
					SplashActivity.this.finish();
					return;
				default:
					return;
			}
		}
	};

	public void onCreate(Bundle bundle) {
		MyLog.i("SplashActivity", "SplashActivity SplashActivity SplashActivity");
		super.onCreate(bundle);
		manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		sharedPreferences = getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		mContext = SipUAApp.mContext;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// TODO 注册接收服务
//		isUnLogin = DeviceInfo.CONFIG_SUPPORT_UNICOM_PASSWORD;
//		if (!isUnLogin) {
//			registerReceiver(loginReceiver, new IntentFilter("com.zed3.sipua.login"));
//			if (Receiver.mSipdroidEngine != null && Receiver.mSipdroidEngine.isRegistered(true)) {
//				MyLog.i("HT", "launch from oncreate" + Receiver.mSipdroidEngine);
//				this.handler.sendEmptyMessage(2);
//				return;
//			}
//		}
		setContentView(R.layout.splash);
		Editor edit = getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putBoolean("autoAnswerKey", false);
		edit.commit();
		RelativeLayout rl_splash_main = (RelativeLayout) findViewById(R.id.rl_splash_main);
		AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
		aa.setDuration(2000);
		rl_splash_main.startAnimation(aa);
		rl_splash_main.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		if (!DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
			// 自动登录
			handler.sendEmptyMessageDelayed(3, 2000);
		} else if (NetChecker.check(this, true)) {
			// 网络连接正常
			mManager = new AutoConfigManager(this);
			mManager.setOnFetchListener(this);
			t_fetchInfo = new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					SplashActivity.this.mLooper = Looper.myLooper();
					SplashActivity.this.fetchInfo();
					Looper.loop();
				}
			});
			t_fetchInfo.setName("AutoLogin_Thread");
			t_fetchInfo.start();
			isThreadStart = true;
		} else {
			// 网络连接异常
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(1000L);
							SplashActivity.this.finish();
						} catch (InterruptedException ex) {
							ex.printStackTrace();
							continue;
						}
						break;
					}
				}
			}).start();
			return;
		}
		if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
			showVersion();
		}
		// TODO 启动GPS线程
		//SipUAApp.getInstance().resumeGpsThread();
	}

	private void showVersion() {
		TextView versionname = (TextView) findViewById(R.id.versionname1);
		AutoConfigManager autoConfigManager = new AutoConfigManager(this);
		String URL = DeviceInfo.MANUAL_CONFIG_URL;
		Log.v("huangfujianurl", "AAAAA===" + URL);
		String aliip = "182.92.181.155";
		String gungzhoupublicip = "163.177.41.114";
		String gungzhoupublicapn = "n21.zed-3.com.cn";
		String gungzhouprivateip = "192.168.0.101";
		String testip = "218.249.39.218";
		String showtext = "";
		if (URL.contains("n1.zed-3.com.cn") || URL.contains(aliip)) {
			showtext = "@p1";
		} else if (URL.contains(gungzhoupublicip) || URL.contains(gungzhoupublicapn)) {
			showtext = "@p2";
		} else if (URL.contains(gungzhouprivateip)) {
			showtext = "@p3";
		} else if (URL.contains(testip)) {
			showtext = "@p0";
		}
		try {
			versionname.setText(new StringBuilder(BaseSipHeaders.Via_short).append(getPackageManager().getPackageInfo(getPackageName(), 0).versionName).append(showtext).toString());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void quitFetchTask() {
		if (this.mLooper != null) {
			try {
				this.mLooper.quit();
			} finally {
				this.mLooper = null;
			}
		}
	}

	private void fetchInfo() {
		fetchLocalInfo(0);
		if (DeviceInfo.isSameSimCard) {
			if (DeviceInfo.isSameHandset) {
				DeviceInfo.PHONENUM = this.sharedPreferences.getString(AutoConfigManager.LC_PHONENUM, "");
				DeviceInfo.SIMNUM = this.sharedPreferences.getString(AutoConfigManager.LC_SIMNUM, "");
				DeviceInfo.IMSI = this.sharedPreferences.getString(AutoConfigManager.LC_IMSI, "");
				DeviceInfo.IMEI = this.sharedPreferences.getString(AutoConfigManager.LC_IMEI, "");
				DeviceInfo.MACADDRESS = this.sharedPreferences.getString(AutoConfigManager.LC_MACADDRESS, "");
				MyLog.i("SplashActivity", "非首次登录（同一张sim卡装在同一部手机上）\r\nDeviceInfo.PHONENUM>>" + DeviceInfo.PHONENUM + "\r\n" + "DeviceInfo.SIMNUM ICCId>>" + DeviceInfo.SIMNUM + "\r\n" + "DeviceInfo.IMSI IMSI>>" + DeviceInfo.IMSI + "\r\n" + "DeviceInfo.IMEI IMEI >>" + DeviceInfo.IMEI + "\r\n" + "DeviceInfo.MACADDRESS MACADDRESS >>" + DeviceInfo.MACADDRESS);
			} else {
				fetchLocalInfo(0);
			}
		} else if (DeviceInfo.isSameHandset) {
			DeviceInfo.IMEI = this.sharedPreferences.getString(AutoConfigManager.LC_IMEI, "");
			DeviceInfo.MACADDRESS = this.sharedPreferences.getString(AutoConfigManager.LC_MACADDRESS, "");
			MyLog.i("SplashActivity", "非首次登录（同一部手机）\r\nDeviceInfo.IMEI IMEI>>" + DeviceInfo.IMEI + "\r\n" + "DeviceInfo.MACADDRESS MACADDRESS>>" + DeviceInfo.MACADDRESS);
		} else {
			fetchLocalInfo(0);
		}
		if (Tools.isConnect(this.mContext)) {
			this.mManager.fetchConfig();
			return;
		}
		TimeOut();
		this.mManager.setOnFetchListener(null);
	}

	private void fetchLocalInfo(int time) {
		if (checkSimCardState(this.manager)) {
			if (time == 0) {
				getSimCardInfo();
			} else {
				for (int i = 0; i < time; i++) {
					try {
						Thread.sleep(1000);
						getSimCardInfo();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (DeviceInfo.PHONENUM == null || DeviceInfo.PHONENUM.equals("") || DeviceInfo.PHONENUM.equalsIgnoreCase("null")) {
					this.handler.sendEmptyMessage(5);
				}
			}
			if (this.mManager.isTheSameSimCard(DeviceInfo.SIMNUM, DeviceInfo.IMSI)) {
				DeviceInfo.isSameSimCard = true;
			} else {
				DeviceInfo.isSameSimCard = false;
			}
			MyLog.i("SplashActivity", "当前手机sim卡信息：DeviceInfo.PHONENUM >>" + DeviceInfo.PHONENUM + "\r\n" + "DeviceInfo.SIMNUM ICCId >>" + DeviceInfo.SIMNUM + "\r\n" + "DeviceInfo.IMSI IMSI >>" + DeviceInfo.IMSI);
		} else {
			DeviceInfo.isSameSimCard = false;
		}
		getHandsetInfo();
		if (this.mManager.isTheSameHandset()) {
			DeviceInfo.isSameHandset = true;
		} else {
			DeviceInfo.isSameHandset = false;
		}
	}

	private boolean checkSimCardState(TelephonyManager manager) {
		switch (manager.getSimState()) {
			case 1:
				this.handler.sendEmptyMessage(6);
				return false;
			case 5:
				return true;
			default:
				this.handler.sendEmptyMessage(7);
				return false;
		}
	}

	@SuppressLint("MissingPermission")
	private void getSimCardInfo() {
		DeviceInfo.PHONENUM = this.manager.getLine1Number();
		if (!(DeviceInfo.PHONENUM == null || DeviceInfo.PHONENUM == "" || !DeviceInfo.PHONENUM.startsWith("+86"))) {
			DeviceInfo.PHONENUM = DeviceInfo.PHONENUM.replace("+86", "");
		}
		DeviceInfo.SIMNUM = this.manager.getSimSerialNumber();
		DeviceInfo.IMSI = this.manager.getSubscriberId();
	}

	@SuppressLint("MissingPermission")
	private void getHandsetInfo() {
		DeviceInfo.IMEI = this.manager.getDeviceId();
		DeviceInfo.MACADDRESS = getLocalMacAddress();
		MyLog.i("SplashActivity", "当前手机设备信息： DeviceInfo.IMEI IMEI >>" + DeviceInfo.IMEI + "\r\n" + " DeviceInfo.MACADDRESS MACADDRESS >>" + DeviceInfo.MACADDRESS);
	}

	private String getLocalMacAddress() {
		@SuppressLint("WifiManagerLeak")
		String macAddress = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
		if (macAddress == null || macAddress.equals("")) {
			return "";
		}
		if (macAddress.contains(":")) {
			return macAddress.replaceAll(":", "");
		}
		return macAddress;
	}

	protected void onDestroy() {
		super.onDestroy();
		if (this.handler != null) {
			this.handler.removeMessages(110);
		}
		if (!this.isUnLogin) {
			unregisterReceiver(this.loginReceiver);
		}
		if (this.mManager != null) {
			this.mManager.setOnFetchListener(null);
		}
		this.isThreadStart = false;
	}

	public void TimeOut() {
		Message msg = new Message();
		msg.what = 3;
		Bundle bd = new Bundle();
		bd.putString("reason", getResources().getString(R.string.netvork_connecttion_timeout));
		msg.setData(bd);
		this.handler.sendMessage(msg);
	}

	public void FetchConfigFailed() {
		Message msg = new Message();
		msg.what = 3;
		Bundle bd = new Bundle();
		bd.putString("reason", getResources().getString(R.string.account_not_exist));
		msg.setData(bd);
		this.handler.sendMessage(msg);
	}

	public void parseFailed() {
		Message message = new Message();
		message.what = 3;
		Bundle bundle = new Bundle();
		bundle.putString("reason", getResources().getString(R.string.error_parse));
		message.setData(bundle);
		this.handler.sendMessage(message);
	}

	public boolean checkConfig() {
		return false;
	}

	private void exitApp() {
		Receiver.engine(this).expire(-1);
		Receiver.engine(this).halt();
		stopService(new Intent(this, RegisterService.class));
		Receiver.alarm(0, OneShotAlarm.class);
		System.exit(0);
	}

	public void ParseConfigOK() {
		quitFetchTask();
		this.mManager.saveSetting();
		if (this.mManager != null) {
			this.mManager.saveLocalconfig();
		}
		this.handler.sendEmptyMessage(4);
	}

	public void AccountDisabled() {
		Message msg = new Message();
		msg.what = 3;
		Bundle bd = new Bundle();
		bd.putString("reason", getResources().getString(R.string.account_disabled));
		msg.setData(bd);
		this.handler.sendMessage(msg);
	}

	class C13122 extends BroadcastReceiver {
		C13122() {
		}

		public void onReceive(Context context, Intent intent) {
			if (intent.getBooleanExtra(ZhejiangReceivier.LOGIN_STATUS, false)) {
				Intent loginIntent = new Intent();
				loginIntent.setClass(SplashActivity.this, MainActivity.class);
				SplashActivity.this.startActivity(loginIntent);
				SplashActivity.this.finish();
				return;
			}
			Message msg = new Message();
			msg.what = 0;
			Bundle data = new Bundle();
			data.putString("result", intent.getStringExtra("result"));
			msg.setData(data);
			SplashActivity.this.handler.sendMessage(msg);
		}
	}

	class C13133 implements View.OnClickListener {
		C13133() {
		}

		public void onClick(View v) {
		}
	}

	class C13144 implements Runnable {
		C13144() {
		}

		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			SplashActivity.this.finish();
		}
	}

	class C13155 implements Runnable {
		C13155() {
		}

		public void run() {
			Looper.prepare();
			SplashActivity.this.mLooper = Looper.myLooper();
			SplashActivity.this.fetchInfo();
			Looper.loop();
		}
	}
}
