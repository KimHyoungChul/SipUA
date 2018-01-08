package com.zed3.addressbook;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Base64;
import android.util.Log;

import com.zed3.flow.FlowStatistics;
import com.zed3.log.MyLog;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.MsgParser;
import com.zed3.sipua.welcome.AutoLoginService;
import com.zed3.sipua.welcome.DeviceInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AddressBookUtils {
	public static boolean ISREQUEST = false;
	private static final String TAG = "AddressBookUtils";
	private static Context context;
	static DataBaseService dbService;
	static List<Map<String, String>> list;
	private static Member mem;
	static List<Map<String, String>> memlist;
	private static volatile int reDecode;

	static {
		AddressBookUtils.list = new ArrayList<Map<String, String>>();
		AddressBookUtils.memlist = new ArrayList<Map<String, String>>();
		AddressBookUtils.dbService = DataBaseService.getInstance();
		AddressBookUtils.reDecode = 0;
		AddressBookUtils.ISREQUEST = false;
	}

	public AddressBookUtils(final Context context) {
		AddressBookUtils.context = context;
	}

	private static List<Map<String, String>> addMembers(final List<Member> list) {
		if (list != null) {
			final int size = list.size();
			int i = 0;
			while (i < size) {
				final Member member = list.get(i);
				log("member:" + member.toString());
				Log.v("lfh", "addTeamList\uff082\uff09" + member.toString());
				final ContentValues contentValues = new ContentValues();
				contentValues.put("number", member.getNumber());
				contentValues.put("mname", member.getmName());
				contentValues.put("mtype", member.getMtype());
				contentValues.put("showflag", member.getShowflag());
				contentValues.put("dtype", member.getDtype());
				contentValues.put("sex", member.getSex());
				contentValues.put("position", member.getPosition());
				contentValues.put("phone", member.getPhone());
				contentValues.put("video", member.getVideo());
				contentValues.put("audio", member.getAudio());
				contentValues.put("pttmap", member.getPttmap());
				contentValues.put("gps", member.getGps());
				contentValues.put("pictureupload", member.getPictureupload());
				contentValues.put("smsswitch", member.getSmsswitch());
				if (member.getParent() != null) {
					contentValues.put("tid", member.getParent().getId());
				}
				Cursor cursor = null;
				Cursor members = null;
				while (true) {
					try {
						final Cursor cursor2 = cursor = (members = AddressBookUtils.dbService.getMembers(member.getNumber()));
						if (!cursor2.moveToNext()) {
							members = cursor2;
							cursor = cursor2;
							AddressBookUtils.dbService.insertMembers(contentValues);
						}
						if (cursor2 != null) {
							cursor2.close();
						}
						++i;
					} catch (Exception ex) {
						cursor = members;
						ex.printStackTrace();
						if (members != null) {
							members.close();
						}
						continue;
					} finally {
						if (cursor != null) {
							cursor.close();
						}
					}
					break;
				}
			}
		}
		return AddressBookUtils.memlist;
	}

	public static void addMsg(final List<String> list) {
		Log.i("lan", "add msg");
		final ContentValues contentValues = new ContentValues();
		int i = 0;
		while (i < list.size()) {
			final String s = list.get(i);
			contentValues.put("message", (String) list.get(i));
			Cursor cursor = null;
			Cursor getmsg = null;
			while (true) {
				try {
					final Cursor cursor2 = cursor = (getmsg = AddressBookUtils.dbService.getmsg(s));
					if (!cursor2.moveToNext()) {
						getmsg = cursor2;
						cursor = cursor2;
						AddressBookUtils.dbService.insertMsgs(contentValues);
					}
					if (cursor2 != null) {
						cursor2.close();
					}
					++i;
				} catch (Exception ex) {
					cursor = getmsg;
					ex.printStackTrace();
					if (getmsg != null) {
						getmsg.close();
					}
					continue;
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
				break;
			}
		}
	}

	public static void addTeam(final Team team) {
		log(" ================addTeam enter=================");
		Label_0246:
		{
			if (team == null) {
				break Label_0246;
			}
			log("team = " + team.toString() + " , parent = " + team.getParent());
			final ContentValues contentValues = new ContentValues();
			contentValues.put("tid", team.getId().toString());
			contentValues.put("name", team.getName().toString());
			Label_0253:
			{
				if (team.getParent() == null) {
					break Label_0253;
				}
				contentValues.put("pid", team.getParent().getId().toString());
				while (true) {
					Cursor cursor = null;
					Cursor teamByid = null;
					while (true) {
						try {
							final Cursor cursor2 = cursor = (teamByid = AddressBookUtils.dbService.getTeamByid(team.getId().toString()));
							if (!cursor2.moveToNext()) {
								teamByid = cursor2;
								cursor = cursor2;
								AddressBookUtils.dbService.insertTeam(contentValues);
								teamByid = cursor2;
								cursor = cursor2;
								Log.i("jiangkai", "  getName() " + team.getName().toString() + "   getId() " + team.getId().toString());
								teamByid = cursor2;
								cursor = cursor2;
								Log.v("huangfujian", "dbService.insertTeam(teamValues)\uff083\uff09" + contentValues);
							}
							if (cursor2 != null) {
								cursor2.close();
							}
							AddressBookUtils.memlist = addMembers(team.getMemberList());
							log(" ================addTeam exit=================");
							contentValues.put("pid", "-2");
							return;
						} catch (Exception ex) {
							cursor = teamByid;
							ex.printStackTrace();
							if (teamByid != null) {
								teamByid.close();
							}
							continue;
						} finally {
							if (cursor != null) {
								cursor.close();
							}
						}
					}
				}
			}
		}
	}

	public static List<Map<String, String>> addTeamList(final List<Team> list) {
		if (list != null) {
			for (int size = list.size(), i = 0; i < size; ++i) {
				final HashMap<String, String> hashMap = new HashMap<String, String>();
				final Team team = list.get(i);
				hashMap.put("name", team.getName().toString());
				AddressBookUtils.list.add(hashMap);
				addTeam(team);
				Log.v("huangfujian", "addTeamList\uff082\uff09" + list);
			}
		}
		return AddressBookUtils.list;
	}

	public static String getMsgList() {
		final String string = "http://" + SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", 0).getString("cms_server", "") + "/webapi/api-msgtpl.php";
		final StringBuffer sb = new StringBuffer();
		sb.append(string).append("?AuthUser=999999&AuthPwd=999999&act=gettpl&ver=").append(DataBaseService.getInstance().getMsgVersion());
		final String string2 = sb.toString();
		final String s = "";
		final DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
		((HttpClient) defaultHttpClient).getParams().setParameter("http.connection.timeout", (Object) 5000);
		((HttpClient) defaultHttpClient).getParams().setParameter("http.socket.timeout", (Object) 5000);
		final HttpGet httpGet = new HttpGet(string2);
		String s2 = s;
		try {
			while (true) {
				int statusCode;
				try {
					final HttpResponse execute = ((HttpClient) defaultHttpClient).execute((HttpUriRequest) httpGet);
					s2 = s;
					statusCode = execute.getStatusLine().getStatusCode();
					if (statusCode == 200) {
						s2 = s;
						final HttpEntity entity = execute.getEntity();
						s2 = s;
						final String trim = EntityUtils.toString(entity).trim();
						if (trim != null) {
							s2 = trim;
							final String unzipMsgCode = unzipMsgCode(trim);
							s2 = trim;
							if (unzipMsgCode.equals("00")) {
								s2 = trim;
								final String unzipMsgVersion = unzipMsgVersion(trim);
								s2 = trim;
								AddressBookUtils.dbService.insertMsgVersion(unzipMsgVersion);
								s2 = trim;
								final List<String> unzipMsgData = unzipMsgData(trim);
								if (unzipMsgData != null) {
									s2 = trim;
									AddressBookUtils.dbService.deleteMsg();
									s2 = trim;
									addMsg(unzipMsgData);
								}
							} else {
								s2 = trim;
								if (!unzipMsgCode.equals("01")) {
									s2 = trim;
									unzipMsgCode.equals("-1");
								}
							}
						}
						s2 = trim;
						FlowStatistics.DownLoad_APK += (int) entity.getContentLength();
						s2 = trim;
						return s2;
					}
				} catch (Exception ex) {
					ex.printStackTrace(new PrintWriter(new StringWriter()));
					return s2;
				}
				s2 = s;
				if (statusCode == 404) {
					return s2;
				}
				s2 = s;
				if (statusCode == 403) {
					break;
				}
				return s2;
			}
			return "";
		} finally {
			((HttpClient) defaultHttpClient).getConnectionManager().shutdown();
		}
	}

	public static void getNewAddressBook2() {
		Log.v("huangfujian", "public static boolean ISREQUEST::" + AddressBookUtils.ISREQUEST);
		if (!NetChecker.check(Receiver.mContext, false)) {
			Log.v("huangfujian", "//\u5224\u65ad\u5f53\u524d\u7f51\u7edc\u72b6\u6001####");
			return;
		}
		if (AddressBookUtils.ISREQUEST) {
			Log.i("ContactNewActivity", "\u6b63\u5728\u8bf7\u6c42\u5e76\u5904\u7406\u6570\u636e\u4e2d,\u65e0\u6cd5\u7ee7\u7eed\u8bf7\u6c42");
			return;
		}
		AddressBookUtils.ISREQUEST = true;
		final String alversion = DataBaseService.getInstance().getAlversion();
		final String companyId = DataBaseService.getInstance().getCompanyId();
		final String companyShowflag = DataBaseService.getInstance().getCompanyShowflag();
		MyLog.i("dd", "alversion =" + alversion);
		Log.i("zzzxxx", "AddressBookActivit alversion=" + alversion);
		final SharedPreferences sharedPreferences = SipUAApp.getAppContext().getSharedPreferences("com.zed3.sipua_preferences", 0);
		new Thread() {
			private final /* synthetic */ String val$password = sharedPreferences.getString("password", "");
			private final /* synthetic */ String val$username = sharedPreferences.getString("username", "");

			@Override
			public void run() {
				String string;
				if (DeviceInfo.http_port.equals("")) {
					string = "";
				} else {
					string = ":" + DeviceInfo.http_port;
				}
				final String string2 = "http://" + SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", 0).getString("server", "") + string + "/nusoap/IGqt.php";
				log("url=" + string2);
				final LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
				linkedHashMap.put("AuthUser", this.val$username);
				linkedHashMap.put("AuthPwd", this.val$password);
				linkedHashMap.put("ALVersion", alversion);
//				SoapSender.send(string2, "GetAddressList", (LinkedHashMap<String, Object>) linkedHashMap, (SoapSender.ParseSoapReponse) new SoapSender.ParseSoapReponse() {
//					@Override
//					public void getException(final Exception ex) {
//						DataBaseService.getInstance().getAllTeams(0);
//					}
//
//					@Override
//					public Object parseReponse(final Object o) {
//						try {
//							final String string = o.toString();
//							if (string != null && string.length() != 0) {
//								if (string.equals("Failure:AuthFailed") || string.equals("Failure:NoUpdate") || string.equals("Failure\uff1aNoUpdate") || string.equals("Failure:NoUpdate " + companyId + " " + companyShowflag) || string.equals("Failure:OtherReason")) {
//									MyLog.i("gengjibin", "NoUpdate");
//								} else {
//									final List<Team> unZipAndParseXML = AddressBookUtils.unZipAndParseXML(string);
//									Log.v("huangfujian", "teams = unZipAndParseXML(result);\uff080\uff09" + unZipAndParseXML);
//									if (unZipAndParseXML != null) {
//										AddressBookUtils.dbService.deleteAll();
//										MyLog.i("ee", "tems=" + unZipAndParseXML);
//										AddressBookUtils.addTeamList(unZipAndParseXML);
//										Receiver.getGDProcess().addressBookUpdate();
//									}
//								}
//							}
//							return null;
//						} catch (Exception ex) {
//							ex.printStackTrace();
//							DataBaseService.getInstance().getAllTeams(0);
//							return null;
//						} finally {
//							DataBaseService.getInstance().getAllTeams(0);
//						}
//					}
//				});
			}
		}.start();
	}

	private static void gpsParser(final String s) {
//		if (s != null && s.length() > 0) {
//			DeviceInfo.GPS_REMOTE = Integer.parseInt(s);
//			AutoLoginService.getDefault().saveGpsRemoteMode(DeviceInfo.GPS_REMOTE);
//			MemoryMg.getInstance().GpsLocationModel = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).getInt("locateModle", 3);
//			MemoryMg.getInstance().GpsLocationModel_EN = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).getInt("locateModle_en", 3);
//			MyLog.i("dd", "DeviceInfo.GPS_REMOTE=" + DeviceInfo.GPS_REMOTE);
//			if (DeviceInfo.GPS_REMOTE == 0) {
//				MemoryMg.getInstance().GpsLocationModel = 3;
//				MemoryMg.getInstance().GpsLocationModel_EN = 3;
//				final SharedPreferences$Editor edit = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).edit();
//				edit.putInt("locateModle", 3);
//				edit.putInt("locateModle_en", 3);
//				edit.commit();
//			} else if (DeviceInfo.GPS_REMOTE == 1) {
//				MemoryMg.getInstance().GpsLocationModel = 1;
//				final SharedPreferences$Editor edit2 = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).edit();
//				edit2.putInt("locateModle", 1);
//				edit2.commit();
//			} else if (DeviceInfo.GPS_REMOTE != 2) {
//				if (DeviceInfo.GPS_REMOTE == 3) {
//					MemoryMg.getInstance().GpsLocationModel = 2;
//					final SharedPreferences$Editor edit3 = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).edit();
//					edit3.putInt("locateModle", 2);
//					edit3.commit();
//				} else if (DeviceInfo.GPS_REMOTE == 4) {
//					MemoryMg.getInstance().GpsLocationModel = 0;
//					MemoryMg.getInstance().GpsLocationModel_EN = 0;
//					final SharedPreferences$Editor edit4 = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).edit();
//					edit4.putInt("locateModle", 0);
//					edit4.putInt("locateModle_en", 0);
//					edit4.commit();
//				} else if (DeviceInfo.GPS_REMOTE == 5) {
//					MemoryMg.getInstance().GpsLocationModel_EN = 4;
//					final SharedPreferences$Editor edit5 = PreferenceManager.getDefaultSharedPreferences(SipUAApp.mContext).edit();
//					edit5.putInt("locateModle_en", 4);
//					edit5.commit();
//				}
//			}
//			MyLog.d("gpsTrace", "MemoryMg.getInstance().GpsLocationModel = " + MemoryMg.getInstance().GpsLocationModel);
//			MyLog.d("gpsTrace", "MemoryMg.getInstance().GpsLocationModel_EN = " + MemoryMg.getInstance().GpsLocationModel_EN);
//		}
	}

	private static void log(final String s) {
		Log.i("xxxx", s);
	}

	private static void setConfig() {
		final boolean b = true;
		MyLog.i("gengjibin", "Settings.getUserName()=" + Settings.getUserName());
		AddressBookUtils.mem = AddressBookUtils.dbService.getStringbyItems(Settings.getUserName());
		final String video = AddressBookUtils.mem.getVideo();
		MyLog.i("gengjibin", "video=" + video);
		DeviceInfo.CONFIG_SUPPORT_VIDEO = (video != null && video.equals("1"));
		AutoLoginService.getDefault().saveVideoSwitch(DeviceInfo.CONFIG_SUPPORT_VIDEO);
		MyLog.i("gengjibin", "DeviceInfo.CONFIG_SUPPORT_VIDEO=" + DeviceInfo.CONFIG_SUPPORT_VIDEO);
		final String audio = AddressBookUtils.mem.getAudio();
		MyLog.i("gengjibin", "audio=" + audio);
		DeviceInfo.CONFIG_SUPPORT_AUDIO = (audio != null && audio.equals("1"));
		AutoLoginService.getDefault().saveAudioSwitch(DeviceInfo.CONFIG_SUPPORT_AUDIO);
		MyLog.i("gengjibin", "DeviceInfo.CONFIG_SUPPORT_AUDIO=" + DeviceInfo.CONFIG_SUPPORT_AUDIO);
		final String pictureupload = AddressBookUtils.mem.getPictureupload();
		MyLog.i("gengjibin", "pictureupload=" + pictureupload);
		DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD = (pictureupload != null && pictureupload.equals("1"));
		AutoLoginService.getDefault().savePicUpload(DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD);
		MyLog.i("gengjibin", "DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD=" + DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD);
		final String smsswitch = AddressBookUtils.mem.getSmsswitch();
		MyLog.i("gengjibin", "smsswitch=" + smsswitch);
		DeviceInfo.CONFIG_SUPPORT_IM = (smsswitch != null && smsswitch.equals("1"));
		AutoLoginService.getDefault().saveSupportSMS(DeviceInfo.CONFIG_SUPPORT_IM);
		MyLog.i("gengjibin", "DeviceInfo.CONFIG_SUPPORT_IM=" + DeviceInfo.CONFIG_SUPPORT_IM);
		final String pttmap = AddressBookUtils.mem.getPttmap();
		MyLog.i("gengjibin", "pttmap=" + pttmap);
		DeviceInfo.CONFIG_SUPPORT_PTTMAP = (pttmap != null && pttmap.equals("1") && b);
		AutoLoginService.getDefault().savePttMapMode(DeviceInfo.CONFIG_SUPPORT_PTTMAP);
		MyLog.i("gengjibin", "DeviceInfo.CONFIG_SUPPORT_PTTMAP=" + DeviceInfo.CONFIG_SUPPORT_PTTMAP);
		final String gps = AddressBookUtils.mem.getGps();
		MyLog.i("gengjibin", "gps=" + gps);
		gpsParser(gps);
		SipUAApp.mContext.sendBroadcast(new Intent(MainActivity.ACTION_UI_REFRESH));
		SipUAApp.mContext.sendBroadcast(new Intent(MainActivity.ACTION_UI_REFRESH));
	}

	public static void setConfig1() {
		final boolean b = true;
		AddressBookUtils.mem = AddressBookUtils.dbService.getStringbyItems(Settings.getUserName());
		final String video = AddressBookUtils.mem.getVideo();
		DeviceInfo.CONFIG_SUPPORT_VIDEO = (video != null && video.equals("1"));
		AutoLoginService.getDefault().saveVideoSwitch(DeviceInfo.CONFIG_SUPPORT_VIDEO);
		final String audio = AddressBookUtils.mem.getAudio();
		DeviceInfo.CONFIG_SUPPORT_AUDIO = (audio != null && audio.equals("1") && b);
		AutoLoginService.getDefault().saveAudioSwitch(DeviceInfo.CONFIG_SUPPORT_AUDIO);
	}

	public static List<Team> unZipAndParseXML(final String s) {
		List<Team> team = null;
		try {
			final String replace = ZipUtils.uncompress(Base64.decode(s.getBytes(), 0)).replace("&", "&amp;");
			log("AddressBookUtils unzipString=" + replace);
			MyLog.d("unzipString", replace);
			team = new SaxParseService().getTeam(new ByteArrayInputStream(replace.getBytes("UTF-8")));
			return team;
		} catch (Exception ex) {
			ex.printStackTrace();
			MyLog.e("", "unZipAndParseXML:" + ex.toString());
			if (AddressBookUtils.reDecode < 3) {
				++AddressBookUtils.reDecode;
				return unZipAndParseXML(s);
			}
			return team;
		}
	}

	public static String unzipMsgCode(String code) {
		try {
			code = new MsgParser().getCode(new ByteArrayInputStream(code.getBytes("UTF-8")));
			return code;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static List<String> unzipMsgData(final String s) {
		try {
			return new MsgParser().getMsg(new ByteArrayInputStream(s.getBytes("UTF-8")));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static String unzipMsgVersion(String version) {
		try {
			version = new MsgParser().getVersion(new ByteArrayInputStream(version.getBytes("UTF-8")));
			return version;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void updateAlVersion(final String s) {
		log("updateAlVersion enter");
		Log.i("xxxxxx", "HttpUtils updateALVersion newVersion=" + s);
		if (Integer.parseInt(s) > Integer.parseInt(DataBaseService.getInstance().getAlversion())) {
			getNewAddressBook2();
		}
		log("updateAlVersion exit");
	}
}
