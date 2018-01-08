package com.zed3.sipua.ui;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.zed3.addressbook.UserMinuteActivity;
import com.zed3.codecs.Codecs;
import com.zed3.flow.FlowRefreshService;
import com.zed3.location.GpsTools;
import com.zed3.location.MemoryMg;
import com.zed3.log.CrashHandler;
import com.zed3.settings.AboutActivity;
import com.zed3.settings.AdvancedChoice;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.autoUpdate.UpdateVersionService;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.sipua.welcome.LoginActivity;
import com.zed3.toast.MyToast;

import org.zoolu.sip.provider.SipStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, DialogInterface.OnClickListener {
	public static final String AMR_MODE = "amrMode";
	public static final String AUDIO_VADCHK = "audiovadchk";
	public static final boolean DEFAULT_3G = false;
	public static final int DEFAULT_ACCOUNT = 0;
	public static final String DEFAULT_AMR_MODE = "4.75";
	public static final boolean DEFAULT_AUTO_DEMAND = false;
	public static final boolean DEFAULT_AUTO_HEADSET = false;
	public static final boolean DEFAULT_AUTO_ON = false;
	public static final boolean DEFAULT_AUTO_ONDEMAND = false;
	public static final boolean DEFAULT_BLUETOOTH = false;
	public static final boolean DEFAULT_CALLBACK = false;
	public static final boolean DEFAULT_CALLRECORD = false;
	public static final boolean DEFAULT_CALLTHRU = false;
	public static final String DEFAULT_CALLTHRU2 = "";
	public static final String DEFAULT_CODECS;
	public static final String DEFAULT_COMPRESSION;
	public static final String DEFAULT_DNS = "";
	public static final String DEFAULT_DOMAIN = "";
	public static final float DEFAULT_EARGAIN = 0.5f;
	public static final boolean DEFAULT_EDGE = false;
	public static final String DEFAULT_EXCLUDEPAT = "";
	public static final String DEFAULT_FROMUSER = "";
	public static final float DEFAULT_HEARGAIN = 0.25f;
	public static final float DEFAULT_HMICGAIN = 1.0f;
	public static final boolean DEFAULT_IMPROVE = false;
	public static final boolean DEFAULT_KEEPON = false;
	public static final long DEFAULT_LOCAL_TIME = -1L;
	public static final boolean DEFAULT_MESSAGE = false;
	public static final float DEFAULT_MICGAIN = 0.5f;
	public static final boolean DEFAULT_MMTEL = false;
	public static final String DEFAULT_MMTEL_QVALUE = "1.00";
	public static final boolean DEFAULT_MWI_ENABLED = true;
	public static final boolean DEFAULT_NODATA = false;
	public static final boolean DEFAULT_NODEFAULT = false;
	public static final boolean DEFAULT_NOPORT = false;
	public static final boolean DEFAULT_NOTIFY = false;
	public static final int DEFAULT_OLDPOLICY = 0;
	public static final int DEFAULT_OLDRING = 0;
	public static final boolean DEFAULT_OLDVALID = false;
	public static final int DEFAULT_OLDVIBRATE = 0;
	public static final int DEFAULT_OLDVIBRATE2 = 0;
	public static final boolean DEFAULT_ON = false;
	public static final boolean DEFAULT_ON_VPN = false;
	public static final boolean DEFAULT_OWNWIFI = false;
	public static final boolean DEFAULT_PAR = false;
	public static final String DEFAULT_PASSWORD = "";
	public static final String DEFAULT_PHONE_MODE = "1";
	public static final String DEFAULT_PORT;
	public static final boolean DEFAULT_POS = false;
	public static final String DEFAULT_POSURL = "";
	public static final String DEFAULT_PREF = "PSTN";
	public static final String DEFAULT_PREFIX = "";
	public static final String DEFAULT_PREF_AUTORUN = "1";
	public static final String DEFAULT_PREF_BDGPSTOAST = "true";
	public static final boolean DEFAULT_PREF_GPSONOFF = false;
	public static final int DEFAULT_PREF_GPSOPENMODLE = 0;
	public static final String DEFAULT_PREF_GPSTOAST = "true";
	public static final int DEFAULT_PREF_LOCATEMODE = 3;
	public static final String DEFAULT_PREF_LOCATIONUPLOADTOAST = "true";
	public static final int DEFAULT_PREF_LOCSETTIME = 1;
	public static final int DEFAULT_PREF_LOCUPLOADTIME = 1;
	public static final String DEFAULT_PREF_VIDEOCALL_ONOFF = "1";
	public static final String DEFAULT_PROTOCOL = "tcp";
	public static final String DEFAULT_PTIME_MODE = "20";
	public static final long DEFAULT_REALTIME = -1L;
	public static final boolean DEFAULT_REGISTRATION = true;
	public static final String DEFAULT_SEARCH = "";
	public static final boolean DEFAULT_SELECTWIFI = false;
	public static final String DEFAULT_SERVER = "";
	public static final long DEFAULT_SERVER_UNIX_TIME = -1L;
	public static final boolean DEFAULT_SETMODE = false;
	public static final String DEFAULT_SIPRINGTONE = "";
	public static final boolean DEFAULT_STUN = false;
	public static final String DEFAULT_STUN_SERVER = "stun.ekiga.net";
	public static final String DEFAULT_STUN_SERVER_PORT = "3478";
	public static final long DEFAULT_UNIX_TIME = -1L;
	public static final String DEFAULT_USERNAME = "";
	public static final String DEFAULT_VAD_MODE = "0";
	public static final boolean DEFAULT_VPN = false;
	public static final String DEFAULT_VQUALITY = "low";
	public static final boolean DEFAULT_WIFI_DISABLED = false;
	public static final boolean DEFAULT_WLAN = true;
	public static final String HIGH_PRI_KEY = "highPriority";
	public static boolean ISFIRST_LOGIN = false;
	public static final String LOCAL_TIME = "LocalTime";
	public static final String LOW_PRI_KEY = "lowPriority";
	public static final String PERF_AUDIO_CONFERENCE = "audio_conference";
	public static final String PERF_CHECK_UPDATE = "check_update";
	public static final String PERF_PIC_UPLOAD = "pic_upload";
	public static final String PERF_PTT_MAP = "ptt_map";
	public static final String PERF_SMS = "spt_sms";
	public static final String PHONE_MODE = "phoneMode";
	public static final String PREF_3G = "3g";
	public static final String PREF_ACCOUNT = "account";
	public static final String PREF_AUDIO_SWITCH = "audio_switch";
	public static final String PREF_AUTORUN = "autorunkey";
	public static final String PREF_AUTO_DEMAND = "auto_demand";
	public static final String PREF_AUTO_HEADSET = "auto_headset";
	public static final String PREF_AUTO_ON = "auto_on";
	public static final String PREF_AUTO_ONDEMAND = "auto_on_demand";
	public static final String PREF_BDGPSTOAST = "bdgps_toast";
	public static final String PREF_BLUETOOTH = "bluetooth";
	public static final String PREF_BLUETOOTH_ONOFF = "bluetoothonoff";
	public static final String PREF_BROADCAST_ACTION_DOWN = "broadcast_action_down";
	public static final String PREF_BROADCAST_ACTION_UP = "broadcast_action_up";
	public static final String PREF_BROADCAST_KEYCODE = "broadcast_keycode";
	public static final String PREF_CALLBACK = "callback";
	public static final String PREF_CALLRECORD = "callrecord";
	public static final String PREF_CALLTHRU = "callthru";
	public static final String PREF_CALLTHRU2 = "callthru2";
	public static final String PREF_CMS_SERVER = "cms_server";
	public static final String PREF_CODECS = "codecs_new";
	public static final String PREF_COMPRESSION = "compression";
	public static final String PREF_DNS = "dns";
	public static final String PREF_DOMAIN = "domain";
	public static final String PREF_EARGAIN = "eargain";
	public static final String PREF_EDGE = "edge";
	public static final String PREF_EXCLUDEPAT = "excludepat";
	public static final String PREF_FROMUSER = "fromuser";
	public static final String PREF_FULLWAKELOCK_ONOFF = "fullwakelock_onoff";
	public static final String PREF_GPSONOFF = "gpsOnOffKey";
	public static final String PREF_GPSOPENMODLE = "gpsOpenModleModle";
	public static final String PREF_GPSTOAST = "gps_toast";
	public static final String PREF_GPS_REMOTE = "gps_remote";
	public static final String PREF_HEARGAIN = "heargain";
	public static final String PREF_HMICGAIN = "hmicgain";
	public static final String PREF_IMPROVE = "improve";
	public static final String PREF_KEEPON = "keepon";
	public static final String PREF_LOCATEMODE = "locateModle";
	public static final String PREF_LOCATEMODE_EN = "locateModle_en";
	public static final String PREF_LOCATIONUPLOADTOAST = "location_upload_toast";
	public static final String PREF_LOCSETTIME = "locateSetTime";
	public static final String PREF_LOCUPLOADTIME = "locateUploadTime";
	public static final String PREF_LOG = "logOnOffKey";
	public static final String PREF_MAP_TYPE = "mapType";
	public static final String PREF_MESSAGE = "vmessage";
	public static final String PREF_MICGAIN = "micgain";
	public static final String PREF_MICWAKEUP_ONOFF = "micwakeuponoff";
	public static final String PREF_MMTEL = "mmtel";
	public static final String PREF_MMTEL_QVALUE = "mmtel_qvalue";
	public static final String PREF_MSG_ENCRYPT = "msg_encrypt";
	public static final String PREF_MWI_ENABLED = "MWI_enabled";
	public static final String PREF_NODATA = "nodata";
	public static final String PREF_NODEFAULT = "nodefault";
	public static final String PREF_NOPORT = "noport";
	public static final String PREF_NOTIFY = "notify";
	public static final String PREF_OLDPOLICY = "oldpolicy";
	public static final String PREF_OLDRING = "oldring";
	public static final String PREF_OLDVALID = "oldvalid";
	public static final String PREF_OLDVIBRATE = "oldvibrate";
	public static final String PREF_OLDVIBRATE2 = "oldvibrate2";
	public static final String PREF_ON = "on";
	public static final String PREF_ON_VPN = "on_vpn";
	public static final String PREF_OWNWIFI = "ownwifi";
	public static final String PREF_PAR = "par";
	public static final String PREF_PASSWORD = "password";
	public static final String PREF_PORT = "port";
	public static final String PREF_POS = "pos";
	public static final String PREF_POSURL = "posurl";
	public static final String PREF_PREF = "pref";
	public static final String PREF_PREFIX = "prefix";
	public static final String PREF_PROTOCOL = "protocol";
	public static final String PREF_REGISTRATION = "registration";
	public static final String PREF_REGTIME_EXPIRES = "regtime_expires";
	public static final String PREF_SEARCH = "search";
	public static final String PREF_SELECTWIFI = "selectwifi";
	public static final String PREF_SERVER = "server";
	public static final String PREF_SETMODE = "setmode";
	public static final String PREF_SIPRINGTONE = "sipringtone";
	public static final String PREF_STUN = "stun";
	public static final String PREF_STUN_SERVER = "stun_server";
	public static final String PREF_STUN_SERVER_PORT = "stun_server_port";
	public static final String PREF_USERNAME = "username";
	public static final String PREF_VIDEOCALL_ONOFF = "videoCallKey";
	public static final String PREF_VPN = "vpn";
	public static final String PREF_VQUALITY = "vquality";
	public static final String PREF_WIFI_DISABLED = "wifi_disabled";
	public static final String PREF_WLAN = "wlan";
	public static final String PTIME_MODE = "ptime";
	public static final String REALTIME = "Realtime";
	public static final String RESTORE_AFTER_OTHER_GROUP = "restoreAfterOtherGrp";
	public static final String SAME_PRI_KEY = "samePriority";
	public static final String SERVER_UNIX_TIME = "serverUnixTime";
	public static final String UNIX_TIME = "UnixTime";
	public static final String VAL_PREF_ASK = "ASK";
	public static final String VAL_PREF_PSTN = "PSTN";
	public static final String VAL_PREF_SIP = "SIP";
	public static final String VAL_PREF_SIPONLY = "SIPONLY";
	public static final String VOICE_LARGE = "voice_large";
	public static final String VOICE_LARGE_VALUE = "0";
	public static Settings context;
	public static Context mContext;
	public static boolean mNeedBlueTooth = false;
	public static String mPassword;
	public static SharedPreferences mSharedPreferences;
	public static String mUserName;
	public static boolean needBDGPS = false;
	public static boolean needGPS = false;
	private static boolean needRestart = false;
	public static boolean needSendLocateBroadcast = false;
	public static boolean needVideoCall = false;
	private static final String profilePath = "/sdcard/Zed3/";
	private static SharedPreferences settings;
	public static final String sharedPrefsFile = "com.zed3.sipua_preferences";
	private Preference gpsOnOffPreference;
	Handler gpsStataHandler;
	protected boolean isDestroied;
	protected boolean isStarted;
	CheckBoxPreference mCheckbox0;
	CheckBoxPreference mCheckbox_flow;
	Dialog mDialog;
	String mKey;
	private boolean needLog;
	private boolean needToast;
	private final String sharedPrefsPath;
	private Thread threadForGps;
	EditText transferText;

	static {
		Settings.needGPS = true;
		Settings.needBDGPS = true;
		Settings.context = null;
		Settings.needRestart = true;
		DEFAULT_PORT = new StringBuilder().append(SipStack.default_port).toString();
		DEFAULT_CODECS = null;
		DEFAULT_COMPRESSION = null;
		Settings.ISFIRST_LOGIN = true;
		Settings.needVideoCall = false;
		Settings.mNeedBlueTooth = false;
	}

	public Settings() {
		this.sharedPrefsPath = "/data/data/com.zed3.sipua/shared_prefs/";
		this.gpsStataHandler = new Handler() {
			public void handleMessage(final Message message) {
				Settings.this.gpsOnOffPreference.setSummary((CharSequence) Settings.this.getGpsOnOffSummary());
			}
		};
		this.needToast = true;
		this.needLog = true;
	}

	private void exitApp() {
		Settings.mContext.stopService(new Intent(Settings.mContext, (Class) FlowRefreshService.class));
		Receiver.engine(Settings.mContext).expire(-1);
		while (true) {
			try {
				Thread.sleep(800L);
				Receiver.engine(Settings.mContext).halt();
				this.stopService(new Intent(Settings.mContext, (Class) RegisterService.class));
				Receiver.alarm(0, OneShotAlarm.class);
				this.startActivity(new Intent((Context) this, (Class) LoginActivity.class));
				this.finish();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
	}

	public static float getEarGain() {
		try {
			final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
			String s;
			if (Receiver.headset > 0) {
				s = "heargain";
			} else {
				s = "eargain";
			}
			return Float.valueOf(defaultSharedPreferences.getString(s, "0.5"));
		} catch (NumberFormatException ex) {
			return 0.5f;
		}
	}

	private String getGpsOnOffSummary() {
		final boolean providerEnabled = ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled("gps");
		Settings.mSharedPreferences.edit().putBoolean("gpsOnOffKey", providerEnabled).commit();
		if (providerEnabled) {
			return this.getString(R.string.gpsOnOff_summaryOn);
		}
		return this.getString(R.string.gpsOnOff_summaryOff);
	}

	public static float getMicGain() {
		if (Receiver.headset > 0 || Receiver.bluetooth > 0) {
			try {
				return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(PREF_HMICGAIN, "1.0")).floatValue();
			} catch (NumberFormatException e) {
				return 1.0f;
			}
		}
		try {
			return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(PREF_MICGAIN, "0.5")).floatValue();
		} catch (NumberFormatException e2) {
			return 0.5f;
		}
	}

	public static String getPassword() {
		return Settings.mPassword = SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("password", "");
	}

	public static String getProfileNameString(final SharedPreferences sharedPreferences) {
		String s = sharedPreferences.getString("server", "");
		if (!sharedPreferences.getString("domain", "").equals("")) {
			s = sharedPreferences.getString("domain", "");
		}
		return String.valueOf(sharedPreferences.getString("username", "")) + "@" + s;
	}

	public static String getUserName() {
		return Settings.mUserName = SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("username", "");
	}

	private String getVersion(final Context context) {
		String versionName;
		if (context == null) {
			versionName = "Unknown";
		} else {
			try {
				final String s = versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
				if (s.contains(" + ")) {
					return String.valueOf(s.substring(0, s.indexOf(" + "))) + "b";
				}
			} catch (PackageManager.NameNotFoundException ex) {
				return "Unknown";
			}
		}
		return versionName;
	}

	private void restoreTitle(final String s, final String s2) {
		((PreferenceScreen) this.findPreference((CharSequence) s)).setOnPreferenceClickListener((Preference.OnPreferenceClickListener) new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				((PreferenceScreen) preference).setTitle((CharSequence) s2);
				return true;
			}
		});
	}

	private void setDefaultValues() {
		(Settings.settings = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE)).registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) this);
		this.updateSummaries();
	}

	private void setEditMaxLength(final String s, final int n) {
		((EditTextPreference) this.findPreference((CharSequence) s)).getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(n)});
	}

	private void setSummaries(final String s, final String summary) {
		this.getPreferenceScreen();
		this.getPreferenceScreen().findPreference((CharSequence) s).setSummary((CharSequence) summary);
	}

	private void setSummaries(final String s, final String s2, final String s3) {
		this.getPreferenceScreen();
		this.getPreferenceScreen().findPreference((CharSequence) s).setSummary((CharSequence) (String.valueOf(Settings.settings.getString(s, s2)) + s3));
	}

	public void copyFile(File in, File out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		try {
			byte[] buf = new byte[1024];
			while (true) {
				int i = fis.read(buf);
				if (i == -1) {
					break;
				}
				fos.write(buf, 0, i);
			}
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
		} catch (Exception e) {
			throw e;
		} catch (Throwable th) {
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}

	public void onClick(final DialogInterface dialogInterface, final int n) {
		final String string = this.transferText.getText().toString();
		if (string.length() > 5 || (!string.equals("") && Integer.valueOf(string) > 65535) || (!string.equals("") && Integer.valueOf(string) < 1)) {
			MyToast.showToast(true, (Context) this, "\u8bf7\u8f93\u51651-65535\u4e4b\u95f4\u7684\u6b63\u786e\u7aef\u53e3\u53f7");
			final SharedPreferences.Editor edit = Settings.settings.edit();
			edit.putString(this.mKey, Settings.DEFAULT_PORT);
			edit.commit();
			if (this.mDialog != null) {
				this.mDialog.cancel();
				this.mDialog = null;
			}
		} else if (string.equals("")) {
			MyToast.showToast(true, (Context) this, "\u7aef\u53e3\u53f7\u4e0d\u80fd\u4e3a\u7a7a");
			final SharedPreferences.Editor edit2 = Settings.settings.edit();
			edit2.putString(this.mKey, Settings.DEFAULT_PORT);
			edit2.commit();
			if (this.mDialog != null) {
				this.mDialog.cancel();
				this.mDialog = null;
			}
		} else if (Integer.valueOf(string) > 0 && Integer.valueOf(string) < 65536) {
			final SharedPreferences.Editor edit3 = Settings.settings.edit();
			edit3.putString(this.mKey, this.transferText.getText().toString());
			edit3.commit();
		}
	}

	public void onCreate(final Bundle bundle) {
		Settings.mContext = this.getApplicationContext();
		super.onCreate(bundle);
		this.getListView().setBackgroundColor(-1);
		this.getWindowManager();
		this.setDefaultValues();
		Codecs.check();
		((PreferenceScreen) this.findPreference((CharSequence) "aboutkey")).setOnPreferenceClickListener((Preference.OnPreferenceClickListener) new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				final Intent intent = new Intent();
				intent.setClass((Context) Settings.this, (Class) AboutActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Settings.this.startActivity(intent);
				return false;
			}
		});
		((PreferenceScreen) this.findPreference((CharSequence) "advanced_choice")).setOnPreferenceClickListener((Preference.OnPreferenceClickListener) new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				final Intent intent = new Intent();
				intent.setClass((Context) Settings.this, (Class) AdvancedChoice.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Settings.this.startActivity(intent);
				return false;
			}
		});
		final PreferenceScreen preferenceScreen = (PreferenceScreen) this.findPreference((CharSequence) "logoff");
		if (!DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
			preferenceScreen.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) new Preference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(final Preference preference) {
					final SharedPreferences.Editor edit = Settings.settings.edit();
					edit.putString("username", "");
					edit.putString("password", "");
					edit.commit();
					Settings.this.exitApp();
					return false;
				}
			});
		} else {
			((PreferenceScreen) this.findPreference((CharSequence) "parent")).removePreference((Preference) preferenceScreen);
		}
		((PreferenceScreen) this.findPreference((CharSequence) "checkVersion")).setOnPreferenceClickListener((Preference.OnPreferenceClickListener) new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				final String string = Settings.settings.getString("server", "");
				if (!TextUtils.isEmpty((CharSequence) string) && string.split("\\.").length == 4) {
					new UpdateVersionService((Context) Settings.this, string).checkUpdate(true);
				}
				return false;
			}
		});
	}

	public void onDestroy() {
		Settings.settings.unregisterOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) this);
		super.onDestroy();
	}

	class C12112 implements Runnable {
		C12112() {
		}

		public void run() {
			Settings.this.isDestroied = false;
			while (!Settings.this.isDestroied) {
				while (Settings.this.isDestroied) {
					Settings.this.gpsStataHandler.sendEmptyMessage(0);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	protected void onResume() {
		if (this.threadForGps == null) {
			this.threadForGps = new Thread(new C12112());
			this.threadForGps.start();
		} else {
			mSharedPreferences.edit().putBoolean(PREF_GPSONOFF, ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(UserMinuteActivity.USER_GPS)).commit();
		}
		Receiver.engine(this);
		super.onResume();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		boolean z = false;
		mSharedPreferences = sharedPreferences;
		if (MemoryMg.getInstance().IsChangeListener) {
			try {
				if (!Thread.currentThread().getName().equals("main")) {
					return;
				}
				if (key.startsWith("port") && sharedPreferences.getString(key, DEFAULT_PORT).equals("0")) {
					this.transferText = new InstantAutoCompleteTextView(this, null);
					this.transferText.setInputType(2);
					this.transferText.setTextColor(-16777216);
					this.transferText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
					this.mKey = key;
					if (this.mDialog != null) {
						this.mDialog.cancel();
					}
//					this.mDialog = new Builder(this).setTitle(Receiver.mContext.getString(R.string.settings_port)).setView(this.transferText).setPositiveButton(17039370, this).create();
					this.mDialog.show();
					this.mDialog.setCanceledOnTouchOutside(false);
//					this.mDialog.setOnKeyListener((DialogInterface.OnKeyListener) new C12178());
					this.mDialog.setOwnerActivity(this);
//					this.mDialog.getWindow().clearFlags(MetadataChangeSet.INDEXABLE_TEXT_SIZE_LIMIT_BYTES);
					return;
				}
				if (key.startsWith("server")) {
					GpsTools.setServer(sharedPreferences.getString("server", ""));
					Receiver.engine(this).updateDNS();
					Receiver.engine(this).halt();
					Receiver.engine(this).StartEngine();
				} else if (sharedPreferences.getBoolean(PREF_CALLBACK, false) && sharedPreferences.getBoolean(PREF_CALLTHRU, false)) {
					CharSequence charSequence;
					PreferenceScreen preferenceScreen = getPreferenceScreen();
					if (key.equals(PREF_CALLBACK)) {
						charSequence = PREF_CALLTHRU;
					} else {
						charSequence = PREF_CALLBACK;
					}
					((CheckBoxPreference) preferenceScreen.findPreference(charSequence)).setChecked(false);
				} else if (key.startsWith(PREF_WLAN) || key.startsWith(PREF_3G) || key.startsWith(PREF_EDGE) || key.startsWith(PREF_DOMAIN) || key.startsWith("server") || key.startsWith("port") || key.equals(PREF_STUN) || key.equals(PREF_STUN_SERVER) || key.equals(PREF_STUN_SERVER_PORT) || key.equals(PREF_MMTEL) || key.equals(PREF_MMTEL_QVALUE) || key.startsWith(PREF_PROTOCOL) || key.startsWith(PREF_VPN) || key.equals(PREF_POS) || key.equals(PREF_POSURL) || key.startsWith(PREF_FROMUSER) || key.equals(PREF_AUTO_ONDEMAND) || key.equals(PREF_MWI_ENABLED) || key.equals(PREF_MSG_ENCRYPT) || key.equals(PREF_REGISTRATION) || key.equals(PREF_KEEPON)) {
					if (key.equals("username")) {
						sendBroadcast(new Intent("com.zed3.sipua.ui_groupcall.clear_grouplist"));
					}
					if (needRestart) {
						Receiver.engine(this).halt();
						Receiver.engine(this).StartEngine();
					}
				} else if (key.startsWith(HIGH_PRI_KEY) || key.startsWith(SAME_PRI_KEY) || key.startsWith(LOW_PRI_KEY)) {
					sharedPreferences.edit().commit();
				} else if (key.startsWith(PREF_WLAN) || key.startsWith(PREF_3G) || key.startsWith(PREF_EDGE) || key.startsWith(PREF_OWNWIFI)) {
					updateSleep();
				} else if (key.equals("gpsfailtoolkey")) {
					MemoryMg.getInstance().GPSSatelliteFailureTip = sharedPreferences.getBoolean(key, false);
				} else if (!key.equals("gpstoolkey")) {
					if (key.equals("version_information")) {
//						new Builder(context).setMessage(getString(R.string.about).replace("\\n", "\n").replace("${VERSION}", getVersion(context))).setTitle(getString(R.string.menu_about)).setCancelable(true).show();
					} else if (!(AMR_MODE.equals(key) || PREF_AUTORUN.equals(key))) {
						if (PREF_VIDEOCALL_ONOFF.equals(key)) {
							if (!sharedPreferences.getString(key, "1").equals("0")) {
								z = true;
							}
							needVideoCall = z;
						} else if (PREF_LOG.equals(key)) {
							if (sharedPreferences.getBoolean(key, false)) {
								CrashHandler.getInstance().init(this, true);
							} else {
								CrashHandler.EndLog();
							}
						}
					}
				}
				updateSummaries();
			} catch (Exception e) {
				Log.e("settings tag", e.toString());
				e.printStackTrace();
			}
		}
	}

	protected void onStart() {
		this.isStarted = true;
		super.onStart();
	}

	protected void onStop() {
		this.isStarted = false;
		Settings.settings.edit().commit();
		super.onStop();
	}

	void updateSleep() {
		final ContentResolver contentResolver = this.getContentResolver();
//		final int int1 = Settings.System.getInt(contentResolver, "wifi_sleep_policy", -1);
//		final int updateSleepPolicy = this.updateSleepPolicy();
//		if (updateSleepPolicy != int1) {
//			int n;
//			if (updateSleepPolicy == 0) {
//				n = R.string.settings_policy_default;
//			} else {
//				n = R.string.settings_policy_never;
//			}
//			MyToast.showToast(true, (Context) this, n);
//			Settings.System.putInt(contentResolver, "wifi_sleep_policy", updateSleepPolicy);
//		}
	}

	int updateSleepPolicy() {
		final boolean b = true;
//		final int int1 = Settings.System.getInt(this.getContentResolver(), "wifi_sleep_policy", -1);
		final boolean b2 = false;
		final boolean b3 = true;
		final boolean b4 = false;
		int n = b3 ? 1 : 0;
		boolean b5 = b4;
		int n2 = b2 ? 1 : 0;
		if (!Settings.settings.getString("username", "").equals("")) {
			n = (b3 ? 1 : 0);
			b5 = b4;
			n2 = (b2 ? 1 : 0);
			if (!Settings.settings.getString("server", "").equals("")) {
				b5 = true;
				final boolean b6 = false | Settings.settings.getBoolean("wlan", true);
				boolean b7 = b;
				if (!Settings.settings.getBoolean("3g", false)) {
					b7 = b;
					if (!Settings.settings.getBoolean("edge", false)) {
						b7 = false;
					}
				}
				final boolean b8 = true & b7;
				n2 = (b6 ? 1 : 0);
				n = (b8 ? 1 : 0);
			}
		}
		final boolean boolean1 = Settings.settings.getBoolean("ownwifi", false);
		if (n == 0 || !b5 || boolean1) {
			if (n2 == 0) {
//				final int n3 = int1;
//				if (!boolean1) {
//					return n3;
//				}
			}
			return 2;
		}
		return 0;
	}

	public void updateSummaries() {
		this.setSummaries("username", "", "");
		this.setSummaries("server", "", "");
		this.setSummaries("port", Settings.DEFAULT_PORT, "");
		String s;
		if (Settings.settings.getString("autorunkey", "1").equals("1")) {
			s = this.getString(R.string.autoRun_on);
		} else {
			s = this.getString(R.string.autoRun_off);
		}
		this.setSummaries("autorunkey", s);
	}
}
