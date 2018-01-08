package com.zed3.zhejiang;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrps;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.SipdroidEngine;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.message.SmsMmsDatabase;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.SettingNew;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.ui.splash.SplashActivity;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.utils.Tools;

import org.zoolu.tools.GroupListInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ZhejiangReceivier extends BroadcastReceiver {
	public static final String ACTIONG_RECEIVER_SMS = "TEXT_MESSAGE_CHANGED";
	public static String ACTIONG_SMS_GET;
	public static String ACTIONG_SMS_SENT_SUCCESS;
	public static final String ACTION_ALL_GET = "com.zed3.sipua.all_get";
	public static final String ACTION_ALL_SENT = "com.zed3.sipua.all_sent";
	public static final String ACTION_CALL = "com.zed3.sipua.call";
	public static final String ACTION_GROUP_CHANGE = "com.zed3.sipua.group_change";
	public static final String ACTION_GROUP_CHANGE_SUCCESS = "com.zed3.sipua.Group_change_success";
	public static final String ACTION_GROUP_GET = "com.zed3.sipua.group_get";
	public static final String ACTION_GROUP_MEMBER_GET = "com.zed3.sipua.group_member_get";
	public static final String ACTION_GROUP_MEMBER_SENT = "com.zed3.sipua.group_member_sent";
	public static final String ACTION_GROUP_SENT = "com.zed3.sipua.group_sent";
	public static final String ACTION_GROUP_STATUS_GET = "com.zed3.sipua.group_status_get";
	public static final String ACTION_GROUP_STATUS_SENT = "com.zed3.sipua.group_status_sent";
	public static final String ACTION_LOGIN = "com.zed3.sipua.login_gqt";
	public static final String ACTION_LOGINOUT_SUCCESS = "com.zed3.sipua.loginout_success";
	public static final String ACTION_LOGIN_SUCCESS = "com.zed3.sipua.login_success";
	public static final String ACTION_LOGOUT = "com.zed3.sipua.logout";
	public static final String ACTION_PTT = "com.zed3.sipua.ptt";
	public static final String ACTION_SETTING = "com.zed3.sipua.setting";
	public static final int AUDIO_CALL = 1;
	public static final String CALL_TYPE = "call_type";
	public static final String CurrentSpeaker = "current_speaker";
	public static final String GROUPNAME = "groupname";
	public static final String GROUPNUMBER = "groupnumber";
	public static final String GROUP_CHANGE_STATUS = "groupchangestatus";
	public static final String GROUP_MEMBER = "groupmember";
	public static final String LOGINOUT_STATUS = "loginoutstatus";
	public static final String LOGIN_STATUS = "loginstatus";
	public static final String NUMBER = "number";
	public static final String PASSWORD = "password";
	public static final String PORT = "port";
	public static final String PROXY = "proxy";
	public static final int PTT_DOWN = 0;
	public static final String PTT_STATUS = "ptt_status";
	public static final int PTT_UP = 1;
	public static String SEND_TEXT_FAIL;
	public static String SEND_TEXT_SUCCEED;
	private static String SMS_SENT_BODY;
	private static String SMS_SENT_NUM;
	public static final String STATUS = "status";
	public static final String USERNAME = "username";
	public static final int VIDEO_CALL = 2;
	private static String currentSpeaker;
	private static String myStatus;
	private final String ACTION_GROUP_STATUS;
	private final String ACTION_RECEIVE_TEXT_MESSAGE;
	private final String ACTION_SEND_TEXT_MESSAGE_FAIL;
	private final String ACTION_SEND_TEXT_MESSAGE_SUCCEED;
	private final String ACTION_SMS_SENT;
	private Context context;

	static {
		ZhejiangReceivier.currentSpeaker = null;
		ZhejiangReceivier.myStatus = null;
		ZhejiangReceivier.SMS_SENT_NUM = "";
		ZhejiangReceivier.SMS_SENT_BODY = "";
		ZhejiangReceivier.SEND_TEXT_SUCCEED = "SEND_MESSAGE_SUCCEED";
		ZhejiangReceivier.SEND_TEXT_FAIL = "SEND_MESSAGE_FAIL";
		ZhejiangReceivier.ACTIONG_SMS_SENT_SUCCESS = "com.zed3.sipua.sms_sent_success";
		ZhejiangReceivier.ACTIONG_SMS_GET = "com.zed3.sipua.sms_get";
	}

	public ZhejiangReceivier() {
		this.ACTION_GROUP_STATUS = "com.zed3.sipua.ui_groupcall.group_status";
		this.ACTION_SMS_SENT = "com.zed3.sipua.sms_sent";
		this.ACTION_RECEIVE_TEXT_MESSAGE = "com.zed3.sipua.ui_receive_text_message";
		this.ACTION_SEND_TEXT_MESSAGE_FAIL = "com.zed3.sipua.ui_send_text_message_fail";
		this.ACTION_SEND_TEXT_MESSAGE_SUCCEED = "com.zed3.sipua.ui_send_text_message_succeed";
	}

	private List<GroupInfo> getGroupName() {
		final PttGrps getAllGrps = Receiver.GetCurUA().GetAllGrps();
		final ArrayList<GroupInfo> list = new ArrayList<GroupInfo>();
		for (int i = 0; i < getAllGrps.GetCount(); ++i) {
			final GroupInfo groupInfo = new GroupInfo();
			final PttGrp getGrpByIndex = getAllGrps.GetGrpByIndex(i);
			groupInfo.groupMember = new ArrayList<GroupInfo.GroupMember>();
			final ArrayList<GroupListInfo> list2 = GroupListUtil.getGroupListsMap().get(getGrpByIndex);
			if (list2 != null) {
				for (int j = 0; j < list2.size(); ++j) {
					final GroupInfo.GroupMember groupMember = new GroupInfo.GroupMember();
					groupMember.memberNumber = list2.get(j).GrpNum;
					groupMember.memberName = list2.get(j).GrpName;
					groupInfo.groupMember.add(groupMember);
				}
			}
			groupInfo.groupNumber = getGrpByIndex.grpID;
			groupInfo.groupName = getGrpByIndex.grpName;
			list.add(groupInfo);
		}
		return list;
	}

	private String getMsgId(final Context context) {
		final StringBuilder sb = new StringBuilder();
		sb.append("00000000");
		sb.append(String.valueOf((System.currentTimeMillis() - SipdroidEngine.serverTimeVal) / 1000L));
		sb.append(Tools.getRandomCharNum(14));
		return sb.toString();
	}

	private void initMsg() {
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		final String fetchLocalServer = autoConfigManager.fetchLocalServer();
		final String fetchLocalUserName = autoConfigManager.fetchLocalUserName();
		final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(Receiver.mContext);
		final Cursor mQuery = smsMmsDatabase.mQuery("message_talk", "status= 0 and type='sms' and server_ip = '" + fetchLocalServer + "'" + "and local_number = '" + fetchLocalUserName + "'", null, null);
		if (mQuery != null && mQuery.getCount() > 0 && mQuery.moveToLast()) {
			final String string = mQuery.getString(mQuery.getColumnIndex("address"));
			final String string2 = mQuery.getString(mQuery.getColumnIndex("contact_name"));
			final String string3 = mQuery.getString(mQuery.getColumnIndex("body"));
			final Intent intent = new Intent(ZhejiangReceivier.ACTIONG_SMS_GET);
			final Bundle bundle = new Bundle();
			bundle.putString("number", string);
			bundle.putString("name", string2);
			bundle.putString("body", string3);
			intent.putExtras(bundle);
			Receiver.mContext.sendBroadcast(intent);
			System.out.println("----\u53d1\u9001\u5e7f\u64ad--com.zed3.sipua.sms_get");
			System.out.println("-----number =" + string + "--body=" + string3);
		}
		if (mQuery != null) {
			mQuery.close();
		}
		if (smsMmsDatabase != null) {
			smsMmsDatabase.close();
		}
	}

	private void login(final String s, final String s2, final String s3, final String s4) {
		final Intent intent = new Intent(Receiver.mContext, (Class) SplashActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Receiver.mContext.startActivity(intent);
		if (s != null && s2 != null && s4 != null && s3 != null) {
			System.out.println("-----11username:" + s + ",password:" + s2 + ",proxy:" + s4 + ",port:" + s3);
			final SharedPreferences.Editor edit = Receiver.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
			edit.putString("username", s);
			edit.putString("password", s2);
			edit.putString("server", s4);
			edit.putString("port", s3);
			edit.commit();
		}
	}

	private void sendGroupInfo() {
//		final String json = new Gson().toJson(this.getGroupName());
//		System.out.println("-------str:" + json);
		final Intent intent = new Intent("com.zed3.sipua.all_sent");
		final Bundle bundle = new Bundle();
//		bundle.putString("jsonString", json);
		intent.putExtras(bundle);
		Receiver.mContext.sendBroadcast(intent);
	}

	private void sendMessage(final String s, final String s2) {
		final String sendTextMessage = Receiver.GetCurUA().SendTextMessage(s, s2, this.getMsgId(this.context));
		final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(Receiver.mContext);
		final ContentValues contentValues = new ContentValues();
		contentValues.put("body", s2);
		contentValues.put("mark", 1);
		contentValues.put("address", s);
		contentValues.put("status", 1);
		contentValues.put("date", this.getCurrentTime());
		contentValues.put("E_id", sendTextMessage);
		contentValues.put("send", 2);
		contentValues.put("type", "sms");
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		contentValues.put("server_ip", autoConfigManager.fetchLocalServer());
		contentValues.put("local_number", autoConfigManager.fetchLocalUserName());
		smsMmsDatabase.insert("message_talk", contentValues);
	}

	public static void setStatus(final String currentSpeaker, final String myStatus) {
		ZhejiangReceivier.currentSpeaker = currentSpeaker;
		ZhejiangReceivier.myStatus = myStatus;
	}

	public String ShowPttStatus(final PttGrp.E_Grp_State e_Grp_State) {
		// TODO
		return "";
	}

	public String getCurrentTime() {
		try {
			return new SimpleDateFormat(" yyyy-MM-dd HH:mm ").format(System.currentTimeMillis() - SipdroidEngine.serverTimeVal);
		} catch (Exception ex) {
			return null;
		}
	}

	public void onReceive(final Context context, Intent intent) {
		System.out.println("-----intent.Action:" + intent.getAction());
		if (intent.getAction().equals("com.zed3.sipua.login_gqt")) {
			System.out.println("--------\u6536\u5230\u767b\u9646\u5e7f\u64ad----");
			final Bundle extras = intent.getExtras();
			final String string = extras.getString("username");
			final String string2 = extras.getString("password");
			final String string3 = extras.getString("proxy");
			final String string4 = extras.getString("port");
			final SharedPreferences sharedPreferences = context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
			final String string5 = sharedPreferences.getString("username", "");
			final String string6 = sharedPreferences.getString("password", "");
			if (Receiver.mSipdroidEngine == null || !Receiver.mSipdroidEngine.isRegistered(true)) {
				System.out.println("-----\u5f00\u59cb\u767b\u5f55");
				this.login(string, string2, string4, string3);
				final Intent intent2 = new Intent("com.zed3.sipua.login_success");
				final Bundle bundle = new Bundle();
				bundle.putBoolean("loginstatus", true);
				intent2.putExtras(bundle);
				Receiver.mContext.sendBroadcast(intent2);
				return;
			}
			System.out.println("-----mSipdroidEngine isRegistered");
			if (!string.equals(string5) || !string2.equals(string6)) {
				System.out.println("-----username and password not same");
				final SharedPreferences.Editor edit = context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
				edit.putString("username", "");
				edit.putString("password", "");
				edit.putString("server", "");
				edit.putString("port", "");
				edit.commit();
				Tools.exitApp(context);
				this.login(string, string2, string4, string3);
				final Intent intent3 = new Intent("com.zed3.sipua.login_success");
				final Bundle bundle2 = new Bundle();
				bundle2.putBoolean("loginstatus", true);
				intent3.putExtras(bundle2);
				Receiver.mContext.sendBroadcast(intent3);
				return;
			}
			System.out.println("-----username and password same");
			final Intent intent4 = new Intent("com.zed3.sipua.login_success");
			final Bundle bundle3 = new Bundle();
			bundle3.putBoolean("loginstatus", true);
			intent4.putExtras(bundle3);
			Receiver.mContext.sendBroadcast(intent4);
			System.out.println("-----\u5df2\u7ecf\u767b\u5f55\u4e86--");
		} else {
			if (intent.getAction().equals("com.zed3.sipua.logout")) {
				intent = new Intent("com.zed3.sipua.loginout_success");
				final Bundle bundle4 = new Bundle();
				bundle4.putBoolean("loginoutstatus", true);
				intent.putExtras(bundle4);
				Receiver.mContext.sendBroadcast(intent);
				final SharedPreferences.Editor edit2 = context.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
				edit2.putString("username", "");
				edit2.putString("password", "");
				edit2.putString("server", "");
				edit2.putString("port", "");
				edit2.commit();
				Tools.exitApp(context);
				return;
			}
			if (intent.getAction().equals("com.zed3.sipua.call")) {
				final Bundle extras2 = intent.getExtras();
				final String string7 = extras2.getString("number");
				final int int1 = extras2.getInt("call_type");
				if (int1 == 1) {
					CallUtil.makeAudioCall(context, string7, null);
					return;
				}
				if (int1 == 2) {
					CallUtil.makeVideoCall(context, string7, null, "videobut");
				}
			} else {
				if (intent.getAction().equals("com.zed3.sipua.group_get")) {
					this.sentGroupName();
					return;
				}
				if (intent.getAction().equals("com.zed3.sipua.group_change")) {
					final String string8 = intent.getExtras().getString("groupnumber");
					System.out.println("------groupNum----" + string8);
					final UserAgent getCurUA = Receiver.GetCurUA();
					final PttGrps getAllGrps = Receiver.GetCurUA().GetAllGrps();
					if (string8 == null || getAllGrps == null) {
						this.sentGroupChangeStatus(false);
						return;
					}
					final PttGrp getGrpByID = getAllGrps.GetGrpByID(string8);
					if (getGrpByID != null) {
						getCurUA.SetCurGrp(getGrpByID);
						this.sentGroupChangeStatus(true);
					}
				} else {
					if (intent.getAction().equals("com.zed3.sipua.group_status_get")) {
						System.out.println("------ACTION_GROUP_STATUS_GET-----");
						this.sentStatus();
						return;
					}
					if (intent.getAction().equals("com.zed3.sipua.group_member_get")) {
						System.out.println("-------ACTION_GROUP_MEMBER_GET-------");
						this.sentGroupMember();
						return;
					}
					if (intent.getAction().equals("com.zed3.sipua.ptt")) {
						System.out.println("---------------");
						final int int2 = intent.getExtras().getInt("ptt_status");
						if (int2 == 0) {
							System.out.println("-------PTT_DOWN--------");
							GroupCallUtil.makeGroupCall(true, false, UserAgent.PttPRMode.SideKeyPress);
							return;
						}
						if (int2 == 1) {
							System.out.println("-------PTT_UP--------");
							GroupCallUtil.makeGroupCall(false, false, UserAgent.PttPRMode.Idle);
						}
					} else if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_groupcall.group_status")) {
						final Bundle extras3 = intent.getExtras();
						if (Receiver.mSipdroidEngine != null && Receiver.mSipdroidEngine.isRegistered()) {
							final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
							String trim;
							if (extras3.getString("1") != null) {
								trim = extras3.getString("1").trim();
							} else {
								trim = null;
							}
							String s = trim;
							if (trim != null) {
								final String[] split = trim.split(" ");
								if (split.length == 1) {
									final String s2 = split[0];
									s = trim;
								} else {
									final String s3 = split[0];
									s = split[1];
								}
							}
							if (getCurGrp != null) {
								setStatus(s, this.ShowPttStatus(getCurGrp.state));
								this.sentStatus();
							}
						}
					} else {
						if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.setting")) {
							intent = new Intent(context, (Class) SettingNew.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(intent);
							return;
						}
						if (intent.getAction().equals("com.zed3.sipua.sms_sent")) {
							final Bundle extras4 = intent.getExtras();
							ZhejiangReceivier.SMS_SENT_NUM = extras4.getString("number");
							ZhejiangReceivier.SMS_SENT_BODY = extras4.getString("body");
							this.sendMessage(ZhejiangReceivier.SMS_SENT_NUM, ZhejiangReceivier.SMS_SENT_BODY);
							return;
						}
						if (intent.getAction().equals("com.zed3.sipua.ui_send_text_message_succeed")) {
							final Intent intent5 = new Intent(ZhejiangReceivier.ACTIONG_SMS_SENT_SUCCESS);
							final Bundle bundle5 = new Bundle();
							bundle5.putBoolean("success", true);
							intent5.putExtras(bundle5);
							Receiver.mContext.sendBroadcast(intent5);
							System.out.println("----\u77ed\u4fe1\u53d1\u9001\u6210\u529f----");
							return;
						}
						if (intent.getAction().equals("com.zed3.sipua.ui_send_text_message_fail")) {
							final Intent intent6 = new Intent(ZhejiangReceivier.ACTIONG_SMS_SENT_SUCCESS);
							final Bundle bundle6 = new Bundle();
							bundle6.putBoolean("success", false);
							intent6.putExtras(bundle6);
							Receiver.mContext.sendBroadcast(intent6);
							System.out.println("----\u77ed\u4fe1\u53d1\u9001\u5931\u8d25----");
							return;
						}
						if (intent.getAction().equals("com.zed3.sipua.ui_receive_text_message")) {
							System.out.println("--------\u6536\u5230\u4e00\u6761\u77ed\u4fe1-----");
							this.initMsg();
							return;
						}
						if (intent.getAction().equals("com.zed3.sipua.all_get")) {
							System.out.println("-----\u6536\u5230\u83b7\u53d6\u901a\u8baf\u5f55\u5e7f\u64ad");
							this.sendGroupInfo();
						}
					}
				}
			}
		}
	}

	public void sentGroupChangeStatus(final Boolean b) {
		final Intent intent = new Intent("com.zed3.sipua.Group_change_success");
		final Bundle bundle = new Bundle();
		bundle.putBoolean("groupchangestatus", (boolean) b);
		intent.putExtras(bundle);
		Receiver.mContext.sendBroadcast(intent);
	}

	public void sentGroupMember() {
		final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
		if (getCurGrp != null) {
			GroupListUtil.getDataCurrentGroupList();
			final ArrayList<GroupListInfo> list = GroupListUtil.getGroupListsMap().get(getCurGrp);
			final StringBuilder sb = new StringBuilder();
			if (list != null) {
				for (int i = 0; i < list.size(); ++i) {
					sb.append(list.get(i).toString());
				}
			}
			String substring;
			final String s = substring = null;
			if (sb != null) {
				substring = s;
				if (sb.length() >= 1) {
					substring = sb.substring(0, sb.length() - 1);
				}
			}
			System.out.println("------\u7ec4\u6210\u5458\u5217\u8868--" + substring);
			final Intent intent = new Intent("com.zed3.sipua.group_member_sent");
			final Bundle bundle = new Bundle();
			bundle.putString("groupmember", substring);
			intent.putExtras(bundle);
			Receiver.mContext.sendBroadcast(intent);
		}
	}

	public void sentGroupName() {
		final PttGrps getAllGrps = Receiver.GetCurUA().GetAllGrps();
		int getCount = 0;
		if (getAllGrps != null) {
			getCount = getAllGrps.GetCount();
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getCount; ++i) {
			final String grpName = getAllGrps.GetGrpByIndex(i).grpName;
			final String grpID = getAllGrps.GetGrpByIndex(i).grpID;
			sb.append(grpName);
			sb.append(",");
			sb.append(grpID);
			sb.append(";");
		}
		if (sb != null && sb.toString().length() >= 1) {
			final String substring = sb.substring(0, sb.toString().length() - 1);
			System.out.println("---------groupListStr:" + substring);
			final Intent intent = new Intent("com.zed3.sipua.group_sent");
			final Bundle bundle = new Bundle();
			bundle.putString("groupname", substring);
			intent.putExtras(bundle);
			Receiver.mContext.sendBroadcast(intent);
		}
	}

	public void sentStatus() {
		final Intent intent = new Intent("com.zed3.sipua.group_status_sent");
		final Bundle bundle = new Bundle();
		bundle.putString("current_speaker", ZhejiangReceivier.currentSpeaker);
		bundle.putString("status", ZhejiangReceivier.myStatus);
		intent.putExtras(bundle);
		Receiver.mContext.sendBroadcast(intent);
	}
}
