package com.zed3.sipua.welcome;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;
import com.zed3.utils.LogUtil;

public class AutoLoginService {
	private static final String LOG_TAG = "testcrash";
	private static AutoLoginService sDefault;

	static {
		AutoLoginService.sDefault = new AutoLoginService();
	}

	private boolean existControlParams() {
		return this.getDefaultSharedPreferences().getAll().size() > 0;
	}

	public static AutoLoginService getDefault() {
		return AutoLoginService.sDefault;
	}

	private SharedPreferences getDefaultSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(SipUAApp.getAppContext());
	}

	private SharedPreferences.Editor getDefaultSharedPreferencesEdit() {
		return this.getDefaultSharedPreferences().edit();
	}

	private int getInt(final String s) {
		return this.getDefaultSharedPreferences().getInt(s, -1);
	}

	private String getString(final String s) {
		return this.getDefaultSharedPreferences().getString(s, (String) null);
	}

	public boolean existLoginParams() {
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.mContext);
		return !TextUtils.isEmpty((CharSequence) autoConfigManager.fetchLocalServer()) || !TextUtils.isEmpty((CharSequence) autoConfigManager.fetchLocalPwd()) || !TextUtils.isEmpty((CharSequence) autoConfigManager.fetchLocalUserName());
	}

	public boolean getAudioConference() {
		return this.getBoolean("audio_conference");
	}

	public String getAudioMode() {
		return this.getString("phoneMode");
	}

	public boolean getAudioSwitch() {
		return this.getBoolean("audio_switch");
	}

	public boolean getBoolean(final String s) {
		return this.getDefaultSharedPreferences().getBoolean(s, false);
	}

	public boolean getBoolean(final String s, final boolean b) {
		return this.getDefaultSharedPreferences().getBoolean(s, b);
	}

	public boolean getEncryptRemote() {
		return this.getBoolean("encyptOnOff");
	}

	public int getGpsRemote() {
		return this.getInt("gps_remote");
	}

	public boolean getPicUpload() {
		return this.getBoolean("pic_upload");
	}

	public boolean getPttMapMode() {
		return this.getBoolean("ptt_map");
	}

	public String getStartDevice() {
		return this.getString("autorunkeybydpmp");
	}

	public boolean getSupportSMS() {
		return this.getBoolean("spt_sms");
	}

	public boolean getVideoSwitch() {
		return this.getBoolean("isVideo");
	}

	public void initDeviceInfo() {
		MyLog.d("testcrash", "AutoLoginService#initDeviceInfo() enter ****************");
		LogUtil.makeLog("testcrash", " initDeviceInfo()");
		MyLog.d("testcrash", "\u89c6\u9891\u5f00\u5173DeviceInfo#CONFIG_SUPPORT_VIDEO = " + DeviceInfo.CONFIG_SUPPORT_VIDEO);
		MyLog.d("testcrash", "\u8bed\u97f3\u901a\u8bdd\u5f00\u5173DeviceInfo#CONFIG_SUPPORT_AUDIO = " + DeviceInfo.CONFIG_SUPPORT_AUDIO);
		final String audioMode = this.getAudioMode();
		if (!TextUtils.isEmpty((CharSequence) audioMode)) {
			DeviceInfo.CONFIG_AUDIO_MODE = Integer.parseInt(audioMode);
		}
		MyLog.d("testcrash", "\u8bed\u97f3\u901a\u8bdd\u65b9\u5f0fDeviceInfo#CONFIG_AUDIO_MODE = " + DeviceInfo.CONFIG_AUDIO_MODE);
		DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE = this.getAudioConference();
		MyLog.d("testcrash", "\u8bed\u97f3\u4f1a\u8baeDeviceInfo#CONFIG_SUPPORT_AUDIO_CONFERENCE = " + DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE);
		DeviceInfo.AUTORUN_REMOTE = this.isStartGQT();
		MyLog.d("testcrash", "\u5f00\u673a\u542f\u52a8DeviceInfo#AUTORUN_REMOTE = " + DeviceInfo.AUTORUN_REMOTE);
		DeviceInfo.CONFIG_CHECK_UPGRADE = this.isCheckUpdate();
		MyLog.d("testcrash", "\u7a0b\u5e8f\u68c0\u67e5\u66f4\u65b0DeviceInfo#CONFIG_CHECK_UPGRADE = " + DeviceInfo.CONFIG_CHECK_UPGRADE);
		DeviceInfo.ENCRYPT_REMOTE = this.getEncryptRemote();
		MyLog.d("testcrash", "\u4fe1\u4ee4\u52a0\u5bc6DeviceInfo#ENCRYPT_REMOTE = " + DeviceInfo.ENCRYPT_REMOTE);
		DeviceInfo.CONFIG_SUPPORT_PTTMAP = this.getPttMapMode();
		MyLog.d("testcrash", "\u5bf9\u8bb2\u5730\u56fe\u6a21\u5f0fDeviceInfo#CONFIG_SUPPORT_PTTMAP = " + DeviceInfo.CONFIG_SUPPORT_PTTMAP);
		DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD = this.getPicUpload();
		MyLog.d("testcrash", "\u56fe\u7247\u62cd\u4f20DeviceInfo#CONFIG_SUPPORT_PICTURE_UPLOAD = " + DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD);
		DeviceInfo.CONFIG_SUPPORT_IM = this.getSupportSMS();
		MyLog.d("testcrash", "\u77ed\u6d88\u606fDeviceInfo#CONFIG_SUPPORT_IM = " + DeviceInfo.CONFIG_SUPPORT_IM);
		DeviceInfo.GPS_REMOTE = this.getGpsRemote();
		MyLog.d("testcrash", "GPS DeviceInfo#GPS_REMOTE = " + DeviceInfo.GPS_REMOTE);
		final SharedPreferences sharedPreferences = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		MemoryMg.getInstance().GpsLocationModel = sharedPreferences.getInt("locateModle", 3);
		MemoryMg.getInstance().GpsLocationModel_EN = sharedPreferences.getInt("locateModle_en", 3);
	}

	public boolean isCheckUpdate() {
		return this.getBoolean("check_update", true);
	}

	public boolean isStartGQT() {
		final String startDevice = this.getStartDevice();
		return !TextUtils.isEmpty((CharSequence) startDevice) && startDevice.equals("1");
	}

	public void saveAudioConference(final boolean b) {
		this.saveBoolean("audio_conference", b);
	}

	public void saveAudioMode(final int n) {
		String s;
		if (n == 0) {
			s = "0";
		} else {
			s = "1";
		}
		this.saveString("phoneMode", s);
	}

	public void saveAudioSwitch(final boolean b) {
		this.saveBoolean("audio_switch", b);
	}

	public void saveBoolean(final String s, final boolean b) {
		final SharedPreferences.Editor defaultSharedPreferencesEdit = this.getDefaultSharedPreferencesEdit();
		defaultSharedPreferencesEdit.putBoolean(s, b);
		defaultSharedPreferencesEdit.commit();
	}

	public void saveCheckUpdate(final boolean b) {
		this.saveBoolean("check_update", b);
	}

	public void saveEncryptRemote(final boolean b) {
		this.saveBoolean("encyptOnOff", b);
	}

	public void saveGpsRemoteMode(final int n) {
		this.saveInt("gps_remote", n);
	}

	public void saveInt(final String s, final int n) {
		final SharedPreferences.Editor defaultSharedPreferencesEdit = this.getDefaultSharedPreferencesEdit();
		defaultSharedPreferencesEdit.putInt(s, n);
		defaultSharedPreferencesEdit.commit();
	}

	public void savePicUpload(final boolean b) {
		this.saveBoolean("pic_upload", b);
	}

	public void savePttMapMode(final boolean b) {
		this.saveBoolean("ptt_map", b);
	}

	public void saveStartDevice(final String s) {
		this.saveString("autorunkeybydpmp", s);
	}

	public void saveString(final String s, final String s2) {
		final SharedPreferences.Editor defaultSharedPreferencesEdit = this.getDefaultSharedPreferencesEdit();
		defaultSharedPreferencesEdit.putString(s, s2);
		defaultSharedPreferencesEdit.commit();
	}

	public void saveSupportSMS(final boolean b) {
		this.saveBoolean("spt_sms", b);
	}

	public void saveVideoSwitch(final boolean b) {
		this.saveBoolean("isVideo", b);
	}

	public static final class WorkerArgs {
		private static WorkerArgs sPool;
		private String key;
		private Callback mCallback;
		private String value;

		static {
			WorkerArgs.sPool = new WorkerArgs();
		}

		public static WorkerArgs pool() {
			WorkerArgs.sPool.setKey(null);
			WorkerArgs.sPool.setValue(null);
			WorkerArgs.sPool.setCallback(null);
			return WorkerArgs.sPool;
		}

		public Callback getCallback() {
			return this.mCallback;
		}

		public String getKey() {
			return this.key;
		}

		public String getValue() {
			return this.value;
		}

		public WorkerArgs setCallback(final Callback mCallback) {
			this.mCallback = mCallback;
			return this;
		}

		public WorkerArgs setKey(final String key) {
			this.key = key;
			return this;
		}

		public WorkerArgs setValue(final String value) {
			this.value = value;
			return this;
		}

		public interface Callback {
			void callback(final SharedPreferences.Editor p0);
		}
	}
}
