package com.zed3.sipua.welcome;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.SettingVideoSize;
import com.zed3.sipua.ui.Settings;
import com.zed3.utils.LogUtil;

import org.xml.sax.InputSource;
import org.zoolu.sip.provider.SipStack;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class AutoConfigManager {
	public static final String LC_IMEI = "localimei";
	public static final String LC_IMSI = "localimsi";
	public static final String LC_MACADDRESS = "localmacaddress";
	public static final String LC_PHONENUM = "localphoneNum";
	public static final String LC_SIMNUM = "localsimnum";
	AutoConfigManager acm;
	Context context;
	IAutoConfigListener listener;
	String pwd;
	String strUrl;
	String userName;

	public AutoConfigManager(Context ctx) {
		this.context = ctx;
	}

	public void setOnFetchListener(IAutoConfigListener listener) {
		if (listener != null) {
			this.listener = listener;
		}
	}

	public void fetchConfig() {
		parseResponce(get());
	}

	public void getConfig() {
		parseBackResponce(get());
	}

	private String fetchUserName() {
		return this.context.getSharedPreferences("ServerSet", 0).getString("UserName", "");
	}

	private String fetchPwd() {
		return this.context.getSharedPreferences("ServerSet", 0).getString("Password", "");
	}

	private String fetchServer() {
		return this.context.getSharedPreferences("ServerSet", 0).getString("IP", "");
	}

	private String fetchPort() {
		return this.context.getSharedPreferences("ServerSet", 0).getString("Port", "");
	}

	private void save(String server, String port, String userName, String pwd) {
		SharedPreferences sharedPreferences = this.context.getSharedPreferences("ServerSet", 0);
		String ip = SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString(Settings.PREF_CMS_SERVER, "");
		Editor editor = sharedPreferences.edit();
		if (server.equals(ip)) {
			editor.putString("IP", server);
		} else {
			editor.putString("IP", ip);
		}
		editor.putString("Port", port);
		editor.putString("UserName", userName);
		editor.putString("Password", pwd);
		editor.commit();
	}

	private String get() {
		if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
			this.strUrl = DeviceInfo.CONFIG_CONFIG_URL.trim();
		} else {
			this.strUrl = "http://" + SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString(Settings.PREF_CMS_SERVER, "") + ":8000/ptt_http.php";
		}
		String getUrl = packageUrl(this.strUrl);
		MyLog.i("AutoConfigManager", "url = " + getUrl);
		String result = "";
		// TODO
		return result;
	}

	private String packageUrl(String url) {
		StringBuffer sb = new StringBuffer();
		if (!DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
			this.acm = new AutoConfigManager(this.context);
			String username = this.acm.fetchLocalUserName();
			if (!TextUtils.isEmpty(url) && url.startsWith("http://")) {
				sb.append(url).append("?sipuser=").append(username);
			}
		} else if (!TextUtils.isEmpty(url) && url.startsWith("http://")) {
			sb.append(url).append("?simcardno=").append(DeviceInfo.SIMNUM).append("&imsi=").append(DeviceInfo.IMSI).append("&phoneno=").append(DeviceInfo.PHONENUM).append("&imei=").append(DeviceInfo.IMEI).append("&mac=").append(DeviceInfo.MACADDRESS).append("&udid=").append(DeviceInfo.UDID);
		}
		String result = sb.toString();
		if (result.equals("")) {
			return result;
		}
		return result.replaceAll("null", "").replaceAll("NULL", "");
	}

	private void parseBackResponce(String string) {
		if (string == null || string.equals("")) {
			MyLog.i("AutoConfigManager", "responces is null");
			return;
		}
		String str = string.replaceAll("&", "&amp;");
		DocumentBuilder builder = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		InputSource is = new InputSource();
		String a = "";
		String b = "";
		String v = "";
		is.setCharacterStream(new StringReader(str));
		// TODO
	}

	private void parseResponce(String string) {
		LogUtil.makeLog(" AutoConfigManager ", " parseResponce");
		if (string == null || string.equals("")) {
			MyLog.e("AutoConfigManager", "responces is null");
			return;
		}
		String str = string.replaceAll("&", "&amp;");
		DocumentBuilder builder = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		InputSource is = new InputSource();
		String a = "";
		String b = "";
		String c = "";
		String d = "";
		String v = "";
		is.setCharacterStream(new StringReader(str));

		// TODO
	}

	public static void LoadSettings(Context context) {
		MemoryMg.getInstance().GvsTransSize = PreferenceManager.getDefaultSharedPreferences(context).getString("gvstransvideosizekey", SettingVideoSize.QVGA);
		MemoryMg.getInstance().GpsSetTimeModel = PreferenceManager.getDefaultSharedPreferences(context).getInt(Settings.PREF_LOCSETTIME, 1);
		MemoryMg.getInstance().GpsUploadTimeModel = PreferenceManager.getDefaultSharedPreferences(context).getInt(Settings.PREF_LOCUPLOADTIME, 1);
		MemoryMg.getInstance().isAudioVAD = !PreferenceManager.getDefaultSharedPreferences(context).getString(Settings.AUDIO_VADCHK, "0").equals("0");
		SipStack.default_expires = PreferenceManager.getDefaultSharedPreferences(context).getInt(Settings.PREF_REGTIME_EXPIRES, 1800);
		MemoryMg.getInstance().isMicWakeUp = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_MICWAKEUP_ONOFF, true);
		MemoryMg.getInstance().PhoneType = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(Settings.PHONE_MODE, "1"));
	}

	private void gpsParser(String v) {
		if (v != null && v.length() > 0) {
			DeviceInfo.GPS_REMOTE = Integer.parseInt(v);
			AutoLoginService.getDefault().saveGpsRemoteMode(DeviceInfo.GPS_REMOTE);
			MemoryMg.getInstance().GpsLocationModel = PreferenceManager.getDefaultSharedPreferences(this.context).getInt(Settings.PREF_LOCATEMODE, 3);
			MemoryMg.getInstance().GpsLocationModel_EN = PreferenceManager.getDefaultSharedPreferences(this.context).getInt(Settings.PREF_LOCATEMODE_EN, 3);
			MyLog.i("gengjibin", "DeviceInfo.GPS_REMOTE=" + DeviceInfo.GPS_REMOTE);
			Editor it;
			if (DeviceInfo.GPS_REMOTE == 0) {
				MemoryMg.getInstance().GpsLocationModel = 3;
				MemoryMg.getInstance().GpsLocationModel_EN = 3;
				it = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
				it.putInt(Settings.PREF_LOCATEMODE, 3);
				it.putInt(Settings.PREF_LOCATEMODE_EN, 3);
				it.commit();
			} else if (DeviceInfo.GPS_REMOTE == 1) {
				MemoryMg.getInstance().GpsLocationModel = 1;
				it = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
				it.putInt(Settings.PREF_LOCATEMODE, 1);
				it.commit();
			} else if (DeviceInfo.GPS_REMOTE != 2) {
				if (DeviceInfo.GPS_REMOTE == 3) {
					MemoryMg.getInstance().GpsLocationModel = 2;
					it = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
					it.putInt(Settings.PREF_LOCATEMODE, 2);
					it.commit();
				} else if (DeviceInfo.GPS_REMOTE == 4) {
					MemoryMg.getInstance().GpsLocationModel = 0;
					MemoryMg.getInstance().GpsLocationModel_EN = 0;
					it = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
					it.putInt(Settings.PREF_LOCATEMODE, 0);
					it.putInt(Settings.PREF_LOCATEMODE_EN, 0);
					it.commit();
				} else if (DeviceInfo.GPS_REMOTE == 5) {
					MemoryMg.getInstance().GpsLocationModel_EN = 4;
					Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
					editor.putInt(Settings.PREF_LOCATEMODE_EN, 4);
					editor.commit();
				}
			}
			MyLog.d("gpsTrace", "MemoryMg.getInstance().GpsLocationModel = " + MemoryMg.getInstance().GpsLocationModel);
			MyLog.d("gpsTrace", "MemoryMg.getInstance().GpsLocationModel_EN = " + MemoryMg.getInstance().GpsLocationModel_EN);
		}
	}

	public void saveLocalconfig() {
		String imei = DeviceInfo.IMEI;
		String imsi = DeviceInfo.IMSI;
		String macaddress = DeviceInfo.MACADDRESS;
		String phonenum = DeviceInfo.PHONENUM;
		String simnum = DeviceInfo.SIMNUM;
		Editor edit = this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putString(LC_IMEI, imei);
		edit.putString(LC_IMSI, imsi);
		edit.putString(LC_MACADDRESS, macaddress);
		edit.putString(LC_PHONENUM, phonenum);
		edit.putString(LC_SIMNUM, simnum);
		edit.commit();
	}

	public boolean isTheSameHandset() {
		SharedPreferences settings = this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		String lc_imei = settings.getString(LC_IMEI, "");
		String lc_macaddress = settings.getString(LC_MACADDRESS, "");
		if (lc_imei != null && !lc_imei.equals("") && !lc_imei.equalsIgnoreCase("null") && lc_imei.equals(DeviceInfo.IMEI)) {
			return true;
		}
		if (lc_macaddress == null || lc_macaddress.equals("") || lc_macaddress.equalsIgnoreCase("null") || !lc_macaddress.equals(DeviceInfo.MACADDRESS)) {
			return false;
		}
		return true;
	}

	public boolean isTheSameSimCard(String iccid, String imsi) {
		SharedPreferences settings = this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		if (!(iccid == null || iccid.equals("") || iccid.equalsIgnoreCase("null"))) {
			String lc_simNum = settings.getString(LC_SIMNUM, "");
			if (!(lc_simNum.equals(null) || lc_simNum.equals("") || !lc_simNum.equals(iccid))) {
				return true;
			}
		}
		if (!(imsi == null || imsi.equals("") || imsi.equalsIgnoreCase("null"))) {
			String lc_imsi = settings.getString(LC_IMSI, "");
			if (!(lc_imsi.equals(null) || lc_imsi.equals("") || !lc_imsi.equals(imsi))) {
				return true;
			}
		}
		return false;
	}

	public void saveSetting() {
		String user = fetchUserName();
		String pwd = fetchPwd();
		String server = fetchServer();
		String port = fetchPort();
		Editor edit = this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putString("username", user);
		edit.putString("password", pwd);
		edit.putString("server", server);
		edit.putString("port", port);
		edit.commit();
	}

	public void saveUsername(String user) {
		Editor edit = this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putString("username", user);
		edit.commit();
	}

	public void saveSetting(String user, String pwd, String server) {
		Editor edit = this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		edit.putString("username", user);
		edit.putString("password", pwd);
		edit.putString(Settings.PREF_CMS_SERVER, server);
		edit.commit();
	}

	public void saveConfig(String server, String port) {
		SharedPreferences settings = this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		String ip = SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString(Settings.PREF_CMS_SERVER, "");
		Editor edit = settings.edit();
		if (server.equals(ip)) {
			edit.putString("server", server);
		} else {
			edit.putString("server", ip);
		}
		edit.putString("port", port);
		edit.commit();
	}

	public String fetchLocalUserName() {
		return this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("username", "");
	}

	public String fetchLocalPwd() {
		return this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("password", "");
	}

	public String fetchLocalServer() {
		return this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("server", "");
	}

	public String fetchLocalCmsServer() {
		return this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString(Settings.PREF_CMS_SERVER, "");
	}

	public String fetchLocalPort() {
		return this.context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("port", "7080");
	}
}
