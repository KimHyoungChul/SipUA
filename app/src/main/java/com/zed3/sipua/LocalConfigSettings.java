package com.zed3.sipua;

import android.content.Context;
import android.content.SharedPreferences;

import com.zed3.log.MyLog;
import com.zed3.sipua.welcome.DeviceInfo;

import java.io.IOException;
import java.util.Properties;

public class LocalConfigSettings {
	public static void loadSettings(final Context context) throws IOException {
		final Properties properties = new Properties();
		properties.load(context.getAssets().open("config.ini"));
		DeviceInfo.CONFIG_UPDATE_URL = properties.getProperty("updateurl");
		DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN = properties.getProperty("autologin").trim().equals("1");
		DeviceInfo.CONFIG_CONFIG_URL = properties.getProperty("autoconfigurl");
		DeviceInfo.MANUAL_CONFIG_URL = "";
		DeviceInfo.CONFIG_SUPPORT_UNICOM_PASSWORD = properties.getProperty("unicompassword").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS = properties.getProperty("unicomflowstatistics").trim().equals("1");
		DeviceInfo.CONFIG_AUDIO_MODE = Integer.parseInt(properties.getProperty("audiomode").trim());
		DeviceInfo.CONFIG_SUPPORT_AUTORUN = properties.getProperty("autorun").trim().equals("1");
		DeviceInfo.CONFIG_CHECK_UPGRADE = properties.getProperty("checkupgrade").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_ENCRYPT = properties.getProperty("encrypt").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_AUDIO_CONFERENCE = properties.getProperty("audioconference").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_EMERGENYCALL = properties.getProperty("emergenycall").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_HOMEKEY_BLOCK = properties.getProperty("homekeyblock").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_VAD = properties.getProperty("vad").trim().equals("1");
		final SharedPreferences sharedPreferences = context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		if (!DeviceInfo.CONFIG_SUPPORT_VAD) {
			sharedPreferences.edit().putString("audiovadchk", "0");
		} else {
			sharedPreferences.edit().putString("audiovadchk", "1");
		}
		DeviceInfo.CONFIG_SUPPORT_LOG = properties.getProperty("log").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_RATE_MONITOR = properties.getProperty("ratemonitor").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_REGISTER_INTERNAL = properties.getProperty("registerinternal").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_AEC = properties.getProperty("aec").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_NS = properties.getProperty("ns").trim().equals("1");
		DeviceInfo.CONFIG_SUPPORT_BLUETOOTH = properties.getProperty("bluetooth").trim().equals("1");
		MyLog.i("dd", "mapType=" + sharedPreferences.getInt("mapType", -1));
	}

	public static final class SdcardConfig {
		public static final String DEFAULT_NEWVERSION_HEARTBEAT = "1";
		private static final SdcardConfig sPool;
		public int mApplicationCreateFreq;
		public String mAudioAccount;
		public boolean mAutoLogin;
		public String mAutoLoginUrl;
		public int mCallFreq;
		public boolean mDebug;
		public int mGoogleFastestLUI;
		public int mGoogleLUI;
		public boolean mGoogleLocation;
		public boolean mGoogleMap;
		public int mGpsLocationTime;
		public boolean mLimitGoogleLUI;
		public boolean mLoadGps;
		public String mLocale;
		public int mNatSendTime;
		public String mNewVersionHeartBeat;
		public boolean mOnlyLogin;
		public int mRecognizerFreq;
		public boolean mReleaseSound;
		public int mSendGpsPkgTime;
		public boolean mSendHeartBeat;
		public int mSendHeartBeatTime;
		public boolean mSpeakGps;
		public float mSpeechRate;
		public int mStandbyFreq;
		public boolean mSupportCpuFreq;
		public boolean mSupportGpsHeartBeat;

		static {
			sPool = new SdcardConfig();
		}

		public SdcardConfig() {
			this.mNewVersionHeartBeat = "1";
			this.mGoogleMap = true;
			this.mGoogleLocation = true;
			this.mDebug = false;
			this.mAutoLogin = true;
			this.mSpeechRate = 1.0f;
			this.mAudioAccount = null;
			this.mGpsLocationTime = -1;
			this.mReleaseSound = false;
			this.mSendHeartBeat = true;
			this.mLoadGps = true;
			this.mOnlyLogin = false;
			this.mSupportCpuFreq = true;
			this.mSupportGpsHeartBeat = true;
			this.mSendHeartBeatTime = -1;
			this.mSendGpsPkgTime = -1;
			this.mRecognizerFreq = 0;
			this.mCallFreq = 3;
			this.mStandbyFreq = 3;
			this.mApplicationCreateFreq = 3;
			this.mNatSendTime = -1;
			this.mGoogleLUI = 15000;
			this.mGoogleFastestLUI = 15000;
			this.mLimitGoogleLUI = true;
		}

		public static SdcardConfig obtain(final String mLocale, final String mAutoLoginUrl) {
			resetPool();
			SdcardConfig.sPool.mLocale = mLocale;
			SdcardConfig.sPool.mAutoLoginUrl = mAutoLoginUrl;
			SdcardConfig.sPool.mNewVersionHeartBeat = "1";
			return SdcardConfig.sPool;
		}

		public static SdcardConfig obtain(final String mLocale, final String mAutoLoginUrl, final String mNewVersionHeartBeat) {
			resetPool();
			SdcardConfig.sPool.mLocale = mLocale;
			SdcardConfig.sPool.mAutoLoginUrl = mAutoLoginUrl;
			SdcardConfig.sPool.mNewVersionHeartBeat = mNewVersionHeartBeat;
			return SdcardConfig.sPool;
		}

		public static SdcardConfig pool() {
			return SdcardConfig.sPool;
		}

		private static void resetPool() {
			SdcardConfig.sPool.mLocale = "";
			SdcardConfig.sPool.mAutoLoginUrl = "";
			SdcardConfig.sPool.mNewVersionHeartBeat = "1";
		}

		public static boolean supportNewVersionHeartBeatProtocal() {
			return SdcardConfig.sPool.mNewVersionHeartBeat.equals("1");
		}
	}
}
