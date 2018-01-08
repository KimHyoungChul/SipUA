package com.zed3.sipua.ui.anta;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.toast.MyToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntaCallUtil {
	private static final String ANTA_LIST = "ANTA_USER_LIST";
	private static final String NUMBER = "info";
	private static SharedPreferences defaultSharedPreferences;
	private static SharedPreferences.Editor edit;
	public static boolean isAntaCall;
	private static List<Map<String, Object>> mContacts;
	private static boolean mIsGroupBroadcast;
	static ArrayList<Map<String, Object>> userListData;

	static {
		AntaCallUtil.isAntaCall = false;
		AntaCallUtil.mContacts = new ArrayList<Map<String, Object>>();
		AntaCallUtil.userListData = new ArrayList<Map<String, Object>>();
	}

	public static boolean add(final Map<String, Object> map) {
		// TODO
//		final String s = map.get("info");
//		for (int i = 0; i < AntaCallUtil.userListData.size(); ++i) {
//			if (s.equals(AntaCallUtil.userListData.get(i).get("info"))) {
//				return false;
//			}
//		}
//		AntaCallUtil.userListData.add(AntaCallUtil.userListData.size(), map);
		return true;
	}

	public static boolean checkExist(final Map<String, Object> map) {
		// TODO
//		final String s = map.get("info");
//		for (int i = 0; i < AntaCallUtil.userListData.size(); ++i) {
//			if (((String) AntaCallUtil.userListData.get(i).get("info")).equals(s)) {
//				return true;
//			}
//		}
		return false;
	}

	public static boolean findAndRemoveCurrentUserFromList() {
		final String userName = Settings.getUserName();
		for (int i = 0; i < AntaCallUtil.userListData.size(); ++i) {
			if (((String) AntaCallUtil.userListData.get(i).get("info")).equals(userName)) {
				AntaCallUtil.userListData.remove(i);
				return true;
			}
		}
		return false;
	}

	public static List<Map<String, Object>> getContacts() {
		// TODO
		return null;
	}

	private static String getCreateTime() {
		return new SimpleDateFormat(" yyyy/MM/dd HH:mm:ss ").format(new Date(System.currentTimeMillis()));
	}

	private static String[] getNumberArray(final String s) {
		return s.split(" ", 32);
	}

	private static String getNumbers() {
		String string = "";
		for (int n = 0; n < AntaCallUtil.userListData.size() && n <= 32; ++n) {
			string = String.valueOf(string) + " " + (String) AntaCallUtil.userListData.get(n).get("info");
		}
		return string.trim();
	}

	private static void getNumbers(final String[] array) {
		for (int i = 0; i < array.length; ++i) {
			final String s = array[i];
			final String userName = ContactUtil.getUserName(array[i]);
			if (userName != null) {
				if (userName.equals(Settings.getUserName())) {
					break;
				}
				final HashMap<String, Object> hashMap = new HashMap<String, Object>();
				hashMap.put("title", userName);
				hashMap.put("info", s);
				AntaCallUtil.userListData.add(hashMap);
			}
		}
	}

	public static List<Map<String, Object>> getUsers() {
		readData();
		return AntaCallUtil.userListData;
	}

	public static boolean isIsGroupBroadcast() {
		return AntaCallUtil.mIsGroupBroadcast;
	}

	public static String mCreateTime() {
		return getCreateTime();
	}

	public static void makeAntaCall(final boolean b) {
		AntaCallUtil.isAntaCall = true;
		Settings.getUserName();
		final String numbers = getNumbers();
		if (numbers != null && !"".equals(numbers)) {
			makeAntaCall(b, numbers);
		}
	}

	public static void makeAntaCall(final boolean isGroupBroadcast, final String s) {
		if (CallUtil.checkGsmCallInCall()) {
			MyToast.showToast(true, SipUAApp.mContext, R.string.gsm_in_call);
		} else {
			setIsGroupBroadcast(isGroupBroadcast);
			if (s == null) {
				throw new RuntimeException("antacall null numbers exception");
			}
			if ("".equals(s)) {
				throw new RuntimeException("antacall numbers no number exception");
			}
			if (!Receiver.mSipdroidEngine.isRegistered()) {
				MyToast.showToast(true, SipUAApp.mContext, R.string.notfast_1);
				return;
			}
			if (Receiver.call_state == 3 || Receiver.call_state == 2) {
				MyToast.showToast(true, SipUAApp.mContext, R.string.vedio_calling_notify);
				return;
			}
			AntaCallUtil.isAntaCall = true;
			final String userName = Settings.getUserName();
			if (s != null && !"".equals(s)) {
				Receiver.engine(SipUAApp.mContext).antaCall1(userName, s, true, isGroupBroadcast);
				CallUtil.initNameAndNumber(s, SipUAApp.mContext.getString(R.string.host_me));
			}
		}
	}

	public static void reInit() {
		setIsGroupBroadcast(false);
		AntaCallUtil.isAntaCall = false;
	}

	private static List<Map<String, Object>> readData() {
		if (AntaCallUtil.defaultSharedPreferences == null) {
			AntaCallUtil.defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext);
		}
		final String string = AntaCallUtil.defaultSharedPreferences.getString("ANTA_USER_LIST", "");
		if (string.equals("")) {
			AntaCallUtil.userListData.clear();
			return AntaCallUtil.userListData;
		}
		AntaCallUtil.userListData.clear();
		getNumbers(getNumberArray(string));
		return AntaCallUtil.userListData;
	}

	public static Map<String, Object> remove(final int n) {
		if (n > -1 && n < AntaCallUtil.userListData.size()) {
			return AntaCallUtil.userListData.remove(n);
		}
		return null;
	}

	public static List<Map<String, Object>> removeAddedContact(final List<Map<String, Object>> list) {
		// TODO
		return null;
	}

	public static String saveUserList() {
		final String numbers = getNumbers();
		if (AntaCallUtil.edit == null) {
			AntaCallUtil.edit = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).edit();
		}
		AntaCallUtil.edit.putString("ANTA_USER_LIST", numbers);
		AntaCallUtil.edit.commit();
		return numbers;
	}

	public static void setIsGroupBroadcast(final boolean mIsGroupBroadcast) {
		AntaCallUtil.mIsGroupBroadcast = mIsGroupBroadcast;
	}
}
