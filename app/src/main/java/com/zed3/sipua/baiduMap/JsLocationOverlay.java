package com.zed3.sipua.baiduMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.zed3.audio.AudioUtil;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.net.util.NetChecker;
import com.zed3.network.SoapSender;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrps;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.ui.ActvityNotify;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;

import org.zoolu.tools.GroupListInfo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class JsLocationOverlay extends BaseActivity {
	public static final String ACTION_GETSTATUS_MESSAGE = "com.zed3.sipua.ui_groupstatelist";
	public static boolean isPttPressing;
	public static int isRefresh_;
	public static boolean isResume;
	public static JsLocationOverlay mContext;
	static boolean mHasPttGrp;
	static View pttkeyMap;
	static TextView pttkeyMap_text;
	public final String ACTION_3GFlow_ALARM;
	private final String ACTION_ALL_GROUP_CHANGE;
	private final String ACTION_DESTORY_MESSAGE;
	private final String ACTION_GROUP_STATUS;
	private final String ACTION_RECEIVE_TEXT_MESSAGE;
	private final String ACTION_SEND_TEXT_MESSAGE_FAIL;
	private final String ACTION_SEND_TEXT_MESSAGE_SUCCEED;
	private final String ACTION_SEND_TEXT_MESSAGE_TIMEOUT;
	private final String ACTION_SINGLE_2_GROUP;
	public Handler JsPttPressHandler;
	private String TAG;
	ArrayAdapter<String> adapter;
	ArrayList<GroupListInfo> arrayList;
	LinearLayout btn_changegroup;
	LinearLayout btn_home;
	private String currentGrpNum;
	boolean flag;
	Thread gisGetThread;
	int gisMemCount;
	private BroadcastReceiver groupListReceiver;
	boolean hideMyView;
	TextView hide_text;
	ImageButton ib_position;
	ImageButton ib_zoom_in;
	ImageButton ib_zoom_out;
	private IntentFilter intentfilter2;
	boolean isFirstLoc;
	public boolean isPTTUseful;
	boolean isRequest;
	public boolean isStarted;
	private String jsonTag;
	int kmarea;
	int lastnum;
	private IntentFilter mFilter;
	private HashMap<PttGrp, ArrayList<GroupListInfo>> mGroupListsMap;
	private Handler mHandler;
	LocationClient mLocClient;
	private Handler mPttHandler;
	private BroadcastReceiver mReceiver;
	public MyLocationListenner myListener;
	ImageButton position_hide;
	Runnable pttDownRunable;
	PttGrps pttGrps;
	Runnable pttUpRunable;
	RadioGroup.OnCheckedChangeListener radioButtonListener;
	LinearLayout tab_hide;
	LinearLayout tab_show1;
	private Timer timer1;
	TextView title;
	WebView webView;

	static {
		JsLocationOverlay.isRefresh_ = 1;
	}

	public JsLocationOverlay() {
		this.pttGrps = Receiver.GetCurUA().GetAllGrps();
		this.flag = false;
		this.TAG = "TalkBackNew";
		this.isPTTUseful = false;
		this.ACTION_GROUP_STATUS = "com.zed3.sipua.ui_groupcall.group_status";
		this.ACTION_ALL_GROUP_CHANGE = "com.zed3.sipua.ui_groupcall.all_groups_change";
		this.ACTION_SINGLE_2_GROUP = "com.zed3.sipua.ui_groupcall.single_2_group";
		this.ACTION_RECEIVE_TEXT_MESSAGE = "com.zed3.sipua.ui_receive_text_message";
		this.ACTION_SEND_TEXT_MESSAGE_FAIL = "com.zed3.sipua.ui_send_text_message_fail";
		this.ACTION_SEND_TEXT_MESSAGE_SUCCEED = "com.zed3.sipua.ui_send_text_message_succeed";
		this.ACTION_SEND_TEXT_MESSAGE_TIMEOUT = "com.zed3.sipua.ui_send_text_message_timeout";
		this.ACTION_3GFlow_ALARM = "com.zed3.flow.3gflow_alarm";
		this.ACTION_DESTORY_MESSAGE = "com.zed3.sipua.ui_destory_message";
		this.mHandler = new Handler() {
			public void handleMessage(final Message message) {
				super.handleMessage(message);
				switch (message.what) {
					case 1: {
						Log.e(JsLocationOverlay.this.TAG, "GroupRefresh timer1 ==> change All");
						JsLocationOverlay.isRefresh_ = 0;
						JsLocationOverlay.this.ShowCurrentGrp();
						JsLocationOverlay.this.getAllGrpGisInfo(JsLocationOverlay.this.currentGrpNum);
					}
					case 2: {
						if (JsLocationOverlay.this.pttGrps.GetCount() == 0) {
							JsLocationOverlay.this.pttGrps = Receiver.GetCurUA().GetAllGrps();
							return;
						}
						break;
					}
					case 3: {
						final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
						if (getCurGrp != null) {
							JsLocationOverlay.this.initOverlay(null, -1, getCurGrp.getGrpID());
							return;
						}
						break;
					}
				}
			}
		};
		this.mReceiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_groupcall.group_status")) {
					final String string = intent.getExtras().getString("1");
					if (string != null) {
						final String[] split = string.trim().split(" ");
						if (split.length == 1) {
							final String s = split[0];
						} else {
							final String s2 = split[0];
							final String s3 = split[1];
						}
					}
					JsLocationOverlay.mHasPttGrp = (Receiver.GetCurUA().GetCurGrp() != null);
				} else if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_groupcall.single_2_group")) {
					GroupCallUtil.setTalkGrp(intent.getExtras().getString("0"));
					GroupCallUtil.setActionMode("com.zed3.sipua.ui_groupcall.single_2_group");
					if (!UserAgent.isCamerPttDialog) {
						final Intent intent2 = new Intent();
						intent2.setClass(Receiver.mContext, (Class) ActvityNotify.class);
						intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Receiver.mContext.startActivity(intent2);
						return;
					}
					JsLocationOverlay.this.sendBroadcast(new Intent("com.zed3.sipua.camera_ptt_dialog"));
				} else if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_receive_text_message")) {
					if (!intent.getExtras().getString("2").equals(MemoryMg.getInstance().LastSeq)) {
						context.sendBroadcast(new Intent(MessageDialogueActivity.RECEIVE_TEXT_MESSAGE));
					}
				} else {
					if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_send_text_message_fail")) {
						final String stringExtra = intent.getStringExtra("0");
						final Intent intent3 = new Intent(MessageDialogueActivity.SEND_TEXT_FAIL);
						intent3.putExtra("0", stringExtra);
						context.sendBroadcast(intent3);
						return;
					}
					if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_send_text_message_succeed")) {
						final String stringExtra2 = intent.getStringExtra("0");
						final Intent intent4 = new Intent(MessageDialogueActivity.SEND_TEXT_SUCCEED);
						intent4.putExtra("0", stringExtra2);
						context.sendBroadcast(intent4);
						return;
					}
					if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_send_text_message_timeout")) {
						final String stringExtra3 = intent.getStringExtra("E_id");
						final Intent intent5 = new Intent(MessageDialogueActivity.SEND_TEXT_TIMEOUT);
						intent5.putExtra("0", stringExtra3);
						context.sendBroadcast(intent5);
						return;
					}
					if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_destory_message")) {
						MemoryMg.getInstance().IsChangeListener = false;
						final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
						edit.putString("username", "");
						edit.putString("password", "");
						edit.putString("server", "");
						edit.commit();
						return;
					}
					if (intent.getAction().equalsIgnoreCase("com.zed3.sipua_network_changed")) {
						final LinearLayout linearLayout = (LinearLayout) JsLocationOverlay.this.findViewById(R.id.net_tip3);
						if (intent.getIntExtra("network_state", -1) == 1) {
							linearLayout.setVisibility(View.GONE);
							return;
						}
						linearLayout.setVisibility(View.VISIBLE);
						++JsLocationOverlay.isRefresh_;
					} else {
						if (intent.getAction().equalsIgnoreCase("com.zed3.sipua_currentgroup_changed")) {
							JsLocationOverlay.this.ShowCurrentGrp();
							JsLocationOverlay.this.getAllGrpGisInfo(JsLocationOverlay.this.currentGrpNum);
							return;
						}
						if (intent.getAction().equalsIgnoreCase("com.zed3.flow.3gflow_alarm")) {
							Tools.FlowAlertDialog((Context) JsLocationOverlay.this);
							return;
						}
						if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_groupcall.all_groups_change")) {
							JsLocationOverlay.isRefresh_ = 0;
							JsLocationOverlay.this.ShowCurrentGrp();
						}
					}
				}
			}
		};
		this.groupListReceiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				final String action = intent.getAction();
				if (action.equals("com.zed3.sipua.ui_groupcall.clear_grouplist")) {
					GroupListUtil.removeDataOfGroupList();
				}
				if (JsLocationOverlay.this.isStarted && action.equals("com.zed3.sipua_grouplist_update_over")) {
					JsLocationOverlay.this.ShowCurrentGrp();
				}
			}
		};
		this.hideMyView = false;
		this.kmarea = 5;
		this.myListener = new MyLocationListenner();
		this.webView = null;
		this.radioButtonListener = null;
		this.isRequest = false;
		this.isFirstLoc = true;
		this.lastnum = 0;
		this.gisGetThread = null;
		this.gisMemCount = 0;
		this.pttDownRunable = new Runnable() {
			@Override
			public void run() {
				JsLocationOverlay.setPttBackground(JsLocationOverlay.isPttPressing = true);
				final UserAgent getCurUA = Receiver.GetCurUA();
				if (getCurUA != null) {
					getCurUA.OnPttKey(true, UserAgent.PttPRMode.ScreenPress);
					return;
				}
				MyLog.e(JsLocationOverlay.this.TAG, "pttDownRunable ,ua = null");
			}
		};
		this.pttUpRunable = new Runnable() {
			@Override
			public void run() {
				JsLocationOverlay.setPttBackground(JsLocationOverlay.isPttPressing = false);
				final UserAgent getCurUA = Receiver.GetCurUA();
				if (getCurUA != null) {
					getCurUA.OnPttKey(false, UserAgent.PttPRMode.Idle);
					return;
				}
				MyLog.e(JsLocationOverlay.this.TAG, "pttUpRunable ,ua = null");
			}
		};
		this.JsPttPressHandler = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.what) {
					default: {
					}
					case 0: {
						JsLocationOverlay.setPttBackground(JsLocationOverlay.isPttPressing = false);
					}
					case 1: {
						JsLocationOverlay.setPttBackground(JsLocationOverlay.isPttPressing = true);
					}
				}
			}
		};
	}

	private void ParseJson(final String s, final String s2) {
		// TODO
	}

	private void RequestWebService(final String s, final int n) throws Exception {
		new Message();
		final String string = SipUAApp.mContext.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).getString("server", "");
		String s2;
		if (DeviceInfo.http_port.equals("")) {
			s2 = "http://" + string + "/nusoap/IGis.php";
		} else {
			s2 = "http://" + string + ":" + DeviceInfo.http_port + "/nusoap/IGis.php";
		}
		final LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
		linkedHashMap.put("AuthUser", "admin");
		linkedHashMap.put("AuthPwd", "admin");
		linkedHashMap.put("PttGroupNum", s);
		linkedHashMap.put("PageSize", 200);
		linkedHashMap.put("PageNum", n);
		SoapSender.send(s2, "QueryPttGis", linkedHashMap, (SoapSender.ParseSoapReponse) new SoapSender.ParseSoapReponse() {
			@Override
			public void getException(final Exception ex) {
				GrpGisUtils.mGisMap.get(s).setReqestState(3);
			}

			@Override
			public Object parseReponse(final Object o) {
				final String string = o.toString();
				if (!TextUtils.isEmpty((CharSequence) string) && string.contains("{")) {
					JsLocationOverlay.this.ParseJson(string, s);
				} else {
					GrpGisUtils.mGisMap.get(s).setReqestState(3);
					JsLocationOverlay.this.gisMemCount = 0;
				}
				return null;
			}
		});
	}

	private void ShowCurrentGrp() {
		final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
		if (getCurGrp != null) {
			this.currentGrpNum = getCurGrp.grpID;
			if (Receiver.GetCurUA().GetCurGrp() != null) {
				this.title.setText((CharSequence) Receiver.GetCurUA().GetCurGrp().grpName);
			}
			if (JsLocationOverlay.isRefresh_ != 1) {
				GroupListUtil.getDataCurrentGroupList();
			}
			this.mGroupListsMap = GroupListUtil.getGroupListsMap();
			this.arrayList = this.mGroupListsMap.get(getCurGrp);
		}
		JsLocationOverlay.isRefresh_ = 1;
	}

	public static boolean checkHasCurrentGrp(final Context context) {
		return JsLocationOverlay.mHasPttGrp;
	}

	private void getAllGrpGisInfo(final String s) {
		if (NetChecker.check((Context) JsLocationOverlay.mContext, false)) {
			if (GrpGisUtils.mGisMap.get(s) != null && GrpGisUtils.mGisMap.get(s).getMembers() != null) {
				if (GrpGisUtils.mGisMap.get(s).getReqestState() == 2 || System.currentTimeMillis() - GrpGisUtils.mGisMap.get(s).getSuccessTime() < 5000L) {
					return;
				}
			} else {
				final GisQuestStateInfo gisQuestStateInfo = new GisQuestStateInfo();
				gisQuestStateInfo.setGroupNumber(s);
				gisQuestStateInfo.setMembers(new ArrayList<GroupMember>());
				gisQuestStateInfo.setReqestState(2);
				gisQuestStateInfo.setSuccessTime(-1L);
				GrpGisUtils.mGisMap.put(s, gisQuestStateInfo);
			}
			final Message message = new Message();
			message.what = 3;
			message.obj = s;
			this.mHandler.sendMessage(message);
//			this.gisGetThread.start();
		}
	}

	public static JsLocationOverlay getInstance() {
		return JsLocationOverlay.mContext;
	}

	public static void setPttBackground(final boolean b) {
		final View pttkeyMap = JsLocationOverlay.pttkeyMap;
		int backgroundResource;
		if (b) {
			backgroundResource = R.drawable.ptt_down_map;
		} else {
			backgroundResource = R.drawable.ptt_up_map;
		}
		pttkeyMap.setBackgroundResource(backgroundResource);
	}

	public String ShowPttStatus(final PttGrp.E_Grp_State e_Grp_State) {
//		switch (e_Grp_State) {
//			default: {
//				return this.getResources().getString(R.string.status_error);
//			}
//			case 1: {
//				return this.getResources().getString(R.string.status_close);
//			}
//			case 2: {
//				return this.getResources().getString(R.string.status_free);
//			}
//			case 4: {
//				return this.getResources().getString(R.string.status_speaking);
//			}
//			case 5: {
//				return this.getResources().getString(R.string.status_listening);
//			}
//			case 6: {
//				return this.getResources().getString(R.string.status_waiting);
//			}
//		}
		// TODO
		return "";
	}

	public String ShowSpeakerStatus(final String s, final String s2) {
		String string;
		if (s == null || s.equals("")) {
			string = this.getResources().getString(R.string.none_speaker);
		} else {
			string = s;
			if (s2.equals(Settings.getUserName())) {
				string = s;
				if (JsLocationOverlay.isPttPressing) {
					return String.valueOf(s) + this.getResources().getString(R.string.self_speaker);
				}
			}
		}
		return string;
	}

	public void initOverlay(final MapPoint mapPoint, final int n, String string) {
		if (!TextUtils.isEmpty((CharSequence) string)) {
			while (true) {
				Label_0277:
				{
					if (n >= 0 && mapPoint != null) {
						break Label_0277;
					}
					final List<GroupMember> memInMiles = (List<GroupMember>) GrpGisUtils.mGisMap.get(string).members.clone();
					if (memInMiles == null || memInMiles.size() < 1) {
						return;
					}
					this.webView.loadUrl("javascript:clearOverlays()");
					this.requestLocClick();
					try {
						for (final GroupMember groupMember : memInMiles) {
							Log.i("zdx", "point:" + groupMember.getName() + "," + groupMember.getNum() + ", " + groupMember.getPoint() + "," + groupMember.isOnline());
							string = "javascript:addMarker('" + groupMember.getPoint().getLongitude() + "','" + groupMember.getPoint().getLatitude() + "','" + groupMember.getNum() + "','" + new String(groupMember.getName().getBytes(), "UTF-8") + "'," + groupMember.isOnline() + ")";
							this.webView.loadUrl(string);
						}
						return;
					} catch (UnsupportedEncodingException ex) {
						ex.printStackTrace();
						return;
					}
				}
				final List<GroupMember> memInMiles = MapTools.getMemInMiles(mapPoint, n, GrpGisUtils.mGisMap.get(string).members);
				continue;
			}
		}
	}

	public void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_locationoverlay_js);
		GroupListUtil.getData4GroupList();
		this.ib_zoom_in = (ImageButton) this.findViewById(R.id.zoom_in);
		this.ib_zoom_out = (ImageButton) this.findViewById(R.id.zoom_out);
		this.ib_position = (ImageButton) this.findViewById(R.id.position);
		this.tab_show1 = (LinearLayout) this.findViewById(R.id.map_tab_show1);
		(this.tab_hide = (LinearLayout) this.findViewById(R.id.map_tab_hide)).setLongClickable(true);
		this.tab_hide.setClickable(true);
		JsLocationOverlay.mContext = this;
		this.mGroupListsMap = GroupListUtil.getGroupListsMap();
		final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
		if (getCurGrp != null) {
			this.currentGrpNum = getCurGrp.grpID;
		}
		(this.mFilter = new IntentFilter()).addAction("com.zed3.sipua_network_changed");
		this.mFilter.addAction("com.zed3.sipua.ui_groupcall.group_status");
		this.mFilter.addAction("com.zed3.sipua.ui_groupcall.all_groups_change");
		this.mFilter.addAction("com.zed3.sipua.ui_groupcall.single_2_group");
		this.mFilter.addAction("com.zed3.sipua.ui_receive_text_message");
		this.mFilter.addAction("com.zed3.sipua.ui_send_text_message_fail");
		this.mFilter.addAction("com.zed3.sipua.ui_send_text_message_succeed");
		this.mFilter.addAction("com.zed3.sipua.ui_send_text_message_timeout");
		this.mFilter.addAction("com.zed3.sipua.ui_destory_message");
		this.mFilter.addAction("com.zed3.sipua_currentgroup_changed");
		this.mFilter.addAction("com.zed3.flow.3gflow_alarm");
		JsLocationOverlay.mContext.registerReceiver(this.mReceiver, this.mFilter);
		if (this.intentfilter2 == null) {
			(this.intentfilter2 = new IntentFilter()).addAction("com.zed3.sipua.ui_groupstatelist");
			this.intentfilter2.addAction("com.zed3.sipua_grouplist_update_over");
			this.intentfilter2.addAction("com.zed3.sipua.ui_groupcall.all_groups_clear_over");
			this.intentfilter2.addAction("com.zed3.sipua.ui_groupcall.clear_grouplist");
		}
		JsLocationOverlay.mContext.registerReceiver(this.groupListReceiver, this.intentfilter2);
		(this.timer1 = new Timer()).schedule(new TimerTask() {
			@Override
			public void run() {
				final Message message = new Message();
				message.what = 1;
				JsLocationOverlay.this.mHandler.sendMessage(message);
			}
		}, 0L, 30000L);
		(this.webView = (WebView) this.findViewById(R.id.webView)).loadUrl("file:///android_asset/test.html");
		final WebSettings settings = this.webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setBlockNetworkImage(false);
		settings.setBlockNetworkLoads(false);
//		this.webView.addJavascriptInterface((Object) new Controler4Js((Context) this), "jsInterface");
		this.ib_zoom_in.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				JsLocationOverlay.this.webView.loadUrl("javascript:setZoom(1)");
			}
		});
		this.ib_zoom_out.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				JsLocationOverlay.this.webView.loadUrl("javascript:setZoom(-1)");
			}
		});
		this.ib_position.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				JsLocationOverlay.this.requestLocClick();
			}
		});
		(this.mLocClient = new LocationClient((Context) this)).registerLocationListener(this.myListener);
		final LocationClientOption locOption = new LocationClientOption();
		locOption.setOpenGps(true);
		locOption.setCoorType("bd09ll");
		locOption.setScanSpan(1000);
		if (MemoryMg.getInstance().GpsLocationModel == 1) {
			locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
		} else if (MemoryMg.getInstance().GpsLocationModel == 0 || MemoryMg.getInstance().GpsLocationModel == 2) {
			locOption.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
		}
		this.mLocClient.setLocOption(locOption);
		if (MemoryMg.getInstance().GpsLocationModel != 3) {
			this.mLocClient.start();
		}
		(this.btn_home = (LinearLayout) this.findViewById(R.id.btn_home)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				JsLocationOverlay.this.finish();
			}
		});
//		this.btn_home.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
//			public boolean onTouch(final View view, final MotionEvent motionEvent) {
//				final TextView textView = (TextView) JsLocationOverlay.this.findViewById(R.id.t_home);
//				final TextView textView2 = (TextView) JsLocationOverlay.this.findViewById(R.id.left_icon);
//				switch (motionEvent.getAction()) {
//					case 0: {
//						textView.setTextColor(-1);
//						JsLocationOverlay.this.btn_home.setBackgroundResource(R.color.btn_click_bg);
//						textView2.setBackgroundResource(R.drawable.map_back_press);
//						break;
//					}
//					case 1: {
//						textView.setTextColor(JsLocationOverlay.this.getResources().getColor(R.color.font_color3));
//						JsLocationOverlay.this.btn_home.setBackgroundResource(R.color.whole_bg);
//						textView2.setBackgroundResource(R.drawable.map_back_release);
//						break;
//					}
//				}
//				return false;
//			}
//		});
		this.title = (TextView) this.findViewById(R.id.map_title);
		if (Receiver.GetCurUA().GetCurGrp() != null) {
			this.title.setText((CharSequence) Receiver.GetCurUA().GetCurGrp().grpName);
		}
		(this.btn_changegroup = (LinearLayout) this.findViewById(R.id.btn_changegroup)).setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) JsLocationOverlay.this.findViewById(R.id.t_spin);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						textView.setBackgroundResource(R.color.btn_click_bg);
						break;
					}
					case 1: {
						textView.setTextColor(JsLocationOverlay.this.getResources().getColor(R.color.font_color3));
						textView.setBackgroundResource(R.color.whole_bg);
						break;
					}
				}
				return false;
			}
		});
		this.btn_changegroup.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final AlertDialog create = new AlertDialog.Builder((Context) JsLocationOverlay.this).setAdapter((ListAdapter) new MyAdapter((Context) JsLocationOverlay.this, JsLocationOverlay.this.pttGrps), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialogInterface, final int n) {
						JsLocationOverlay.this.title.setText((CharSequence) JsLocationOverlay.this.pttGrps.GetGrpByIndex(n).grpName);
						if (!NetChecker.check((Context) JsLocationOverlay.mContext, true)) {
							MyToast.showToast(true, (Context) JsLocationOverlay.mContext, JsLocationOverlay.this.getResources().getString(R.string.group_notify));
						} else if (Receiver.GetCurUA().GetCurGrp() != JsLocationOverlay.this.pttGrps.GetGrpByIndex(n)) {
							Receiver.GetCurUA().SetCurGrp(JsLocationOverlay.this.pttGrps.GetGrpByIndex(n));
							JsLocationOverlay.this.getAllGrpGisInfo(JsLocationOverlay.this.currentGrpNum);
						}
					}
				}).create();
				((Dialog) create).show();
				((Dialog) create).setCanceledOnTouchOutside(true);
			}
		});
		(JsLocationOverlay.pttkeyMap = this.findViewById(R.id.pttkeymap)).setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					case 0: {
						if (NetChecker.check((Context) JsLocationOverlay.mContext, true)) {
							JsLocationOverlay.pttkeyMap.setBackgroundResource(R.drawable.ptt_down_map);
							GroupCallUtil.makeGroupCall(JsLocationOverlay.isPttPressing = true, false, UserAgent.PttPRMode.ScreenPress);
							return true;
						}
						break;
					}
					case 1: {
						if (NetChecker.check((Context) JsLocationOverlay.mContext, true) && JsLocationOverlay.isPttPressing) {
							GroupCallUtil.makeGroupCall(JsLocationOverlay.isPttPressing = false, false, UserAgent.PttPRMode.Idle);
							JsLocationOverlay.pttkeyMap.setBackgroundResource(R.drawable.ptt_up_map);
							return true;
						}
						break;
					}
				}
				return true;
			}
		});
		this.position_hide = (ImageButton) this.findViewById(R.id.imageposition_hide);
		this.hide_text = (TextView) this.findViewById(R.id.hide_text);
		this.position_hide.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				JsLocationOverlay.this.requestLocClick();
			}
		});
		this.hide_text.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				JsLocationOverlay.this.tab_show1.setVisibility(View.VISIBLE);
				JsLocationOverlay.this.tab_hide.setVisibility(View.GONE);
			}
		});
		this.requestLocClick();
		this.mPttHandler = new Handler();
	}

	protected void onDestroy() {
		if (this.timer1 != null) {
//			this.timer1.cancel();
			this.timer1 = null;
		}
		while (true) {
			while (true) {
				try {
					if (this.mFilter != null) {
						JsLocationOverlay.mContext.unregisterReceiver(this.mReceiver);
					} else {
						MyLog.i("GroupCallActivity", "recv unregister fail! mFilter is null. ");
					}
					if (this.intentfilter2 != null) {
						JsLocationOverlay.mContext.unregisterReceiver(this.groupListReceiver);
						JsLocationOverlay.mContext = null;
						super.onDestroy();
						return;
					}
				} catch (Exception ex) {
					MyLog.e("GroupCallActivity", "unregisterReceiver fail: " + ex.toString());
					continue;
				}
				MyLog.i("GroupCallActivity", "groupListReceiver unregister fail! intentfilter2 is null. ");
				continue;
			}
		}
	}

	@Override
	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		return super.onKeyDown(n, keyEvent);
	}

	public boolean onKeyUp(final int n, final KeyEvent keyEvent) {
		Log.i("zdx", "jsLocation onKeyUp");
		return super.onKeyUp(n, keyEvent);
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onResume() {
		JsLocationOverlay.isResume = true;
		if (this.timer1 == null) {
			(this.timer1 = new Timer()).schedule(new TimerTask() {
				@Override
				public void run() {
					final Message message = new Message();
					message.what = 1;
					JsLocationOverlay.this.mHandler.sendMessage(message);
				}
			}, 0L, 30000L);
		}
		Receiver.engine((Context) JsLocationOverlay.mContext);
		if (!NetChecker.check((Context) this, false)) {
			((LinearLayout) this.findViewById(R.id.net_tip3)).setVisibility(View.VISIBLE);
		} else {
			((LinearLayout) this.findViewById(R.id.net_tip3)).setVisibility(View.GONE);
		}
		JsLocationOverlay.mHasPttGrp = (Receiver.GetCurUA().GetCurGrp() != null);
		setPttBackground(JsLocationOverlay.isPttPressing);
		AudioUtil.getInstance().setVolumeControlStream(this);
		super.onResume();
	}

	protected void onStart() {
		this.isStarted = true;
		if (this.pttGrps.GetCount() == 0) {
			JsLocationOverlay.isRefresh_ = 0;
			this.ShowCurrentGrp();
		}
		super.onStart();
	}

	protected void onStop() {
		if (this.timer1 != null) {
			this.timer1.cancel();
			this.timer1 = null;
		}
		JsLocationOverlay.isResume = false;
		this.isStarted = false;
		super.onStop();
	}

	public void requestLocClick() {
		this.isRequest = true;
		this.mLocClient.requestLocation();
	}

	public class MyAdapter extends BaseAdapter {
		private Context mContext;
		private PttGrps pttGrps;

		public MyAdapter(final Context mContext, final PttGrps pttGrps) {
			this.pttGrps = pttGrps;
			this.mContext = mContext;
		}

		public int getCount() {
			if (this.pttGrps.GetCount() == 0) {
				return 1;
			}
			return this.pttGrps.GetCount();
		}

		public Object getItem(final int n) {
			if (this.pttGrps == null || this.pttGrps.GetCount() < 1) {
				return "\u4e0d\u5728\u4efb\u4f55\u7ec4";
			}
			return "\u5207\u6362";
		}

		public long getItemId(final int n) {
			return n;
		}

		public View getView(final int n, View inflate, final ViewGroup viewGroup) {
			inflate = LayoutInflater.from(this.mContext).inflate(R.layout.spinner_item, (ViewGroup) null);
			if (inflate != null) {
				((TextView) inflate.findViewById(R.id.item)).setText((CharSequence) this.pttGrps.GetGrpByIndex(n).grpName);
			}
			return inflate;
		}
	}

	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(final BDLocation bdLocation) {
			if (bdLocation != null && (JsLocationOverlay.this.isRequest || JsLocationOverlay.this.isFirstLoc)) {
				JsLocationOverlay.this.webView.loadUrl("javascript:locate(" + bdLocation.getLongitude() + "," + bdLocation.getLatitude() + ")");
				JsLocationOverlay.this.isRequest = false;
				JsLocationOverlay.this.isFirstLoc = false;
			}
		}

		public void onReceivePoi(final BDLocation bdLocation) {
			if (bdLocation == null) {
			}
		}
	}
}
