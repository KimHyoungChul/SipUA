package com.zed3.sipua.baiduMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.zed3.addressbook.DataBaseService;
import com.zed3.customgroup.CustomGroupManager;
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
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;

import org.zoolu.tools.GroupListInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationOverlayDemo extends BaseActivity {
	public static boolean isPttPressing;
	public static int isRefresh_;
	public static boolean isResume;
	public static LocationOverlayDemo mContext;
	static boolean mHasPttGrp;
	static View pttkeyMap;
	static TextView pttkeyMap_text;
	public final String ACTION_3GFlow_ALARM;
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
	private boolean isFirstTime;
	public boolean isPTTUseful;
	boolean isRequest;
	public boolean isStarted;
	OverlayItem item;
	int kmarea;
	int lastnum;
	private List<GroupMember> list;
	LocationData locData;
	OverlayItem mCurItem;
	private Dialog mDialog;
	private IntentFilter mFilter;
	private HashMap<PttGrp, ArrayList<GroupListInfo>> mGroupListsMap;
	private Handler mHandler;
	LocationClient mLocClient;
	private MapController mMapController;
	MapView mMapView;
	private MyOverlay mOverlay;
	private Handler mPttHandler;
	private BroadcastReceiver mReceiver;
	private MyAdapter myAdapter;
	public MyLocationListenner myListener;
	locationOverlay myLocationOverlay;
	PopupClickListener mypopListener;
	String number;
	private PopupOverlay overlayPop;
	private TextView overlayPopupText;
	private PopupOverlay pop;
	private View popupInfo;
	private View popupLeft;
	private View popupRight;
	private TextView popupText;
	ImageButton position_hide;
	Handler progressHandler;
	Runnable pttDownRunable;
	PttGrps pttGrps;
	public Handler pttPressHandler;
	Runnable pttUpRunable;
	RadioGroup.OnCheckedChangeListener radioButtonListener;
	String speaker;
	LinearLayout tab_hide;
	LinearLayout tab_show1;
	private ArrayList<String> tempGrpList;
	private Timer timer1;
	TextView title;
	String userNum;
	private View viewCache;
	private View viewCacheOverlay;

	static {
		LocationOverlayDemo.isRefresh_ = 1;
	}

	public LocationOverlayDemo() {
		this.pttGrps = Receiver.GetCurUA().GetAllGrps();
		this.flag = false;
		this.speaker = null;
		this.userNum = null;
		this.TAG = "TalkBackNew";
		this.isPTTUseful = false;
		this.ACTION_3GFlow_ALARM = "com.zed3.flow.3gflow_alarm";
		this.mHandler = new Handler() {
			public void handleMessage(final Message message) {
				super.handleMessage(message);
				switch (message.what) {
					case 1: {
						Log.e(LocationOverlayDemo.this.TAG, "GroupRefresh timer1 ==> change All");
						LocationOverlayDemo.isRefresh_ = 0;
						LocationOverlayDemo.this.ShowCurrentGrp();
						LocationOverlayDemo.this.getAllGrpGisInfo(LocationOverlayDemo.this.currentGrpNum);
					}
					case 2: {
						if (LocationOverlayDemo.this.pttGrps.GetCount() == 0) {
							LocationOverlayDemo.this.pttGrps = Receiver.GetCurUA().GetAllGrps();
							return;
						}
						break;
					}
					case 3: {
						final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
						if (message.obj == null || getCurGrp == null || !getCurGrp.getGrpID().equalsIgnoreCase((String) message.obj)) {
							break;
						}
						if (LocationOverlayDemo.this.mOverlay != null) {
							LocationOverlayDemo.this.mOverlay.removeAll();
						}
						if (LocationOverlayDemo.this.mMapView != null) {
							LocationOverlayDemo.this.mMapView.refresh();
							LocationOverlayDemo.this.initOverlay(null, -1, LocationOverlayDemo.this.number, getCurGrp.getGrpID());
							return;
						}
						break;
					}
					case 5: {
						if (LocationOverlayDemo.this.mMapView != null) {
							LocationOverlayDemo.this.mMapView.getController().setZoom(message.getData().getInt("zoom"));
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
					final Bundle extras = intent.getExtras();
					String trim;
					if (extras.getString("1") != null) {
						trim = extras.getString("1").trim();
					} else {
						trim = null;
					}
					if (trim != null) {
						final String[] split = trim.split(" ");
						if (split.length == 1) {
							LocationOverlayDemo.this.userNum = split[0];
						} else {
							LocationOverlayDemo.this.userNum = split[0];
							final String s = split[1];
						}
					}
					LocationOverlayDemo.this.ShowCurrentGrp();
					final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
					LocationOverlayDemo.mHasPttGrp = (getCurGrp != null);
					if (!LocationOverlayDemo.mHasPttGrp) {
						LocationOverlayDemo.this.initOverlay(null, -1, null, null);
						return;
					}
					LocationOverlayDemo.this.initOverlay(null, -1, null, getCurGrp.getGrpID());
				} else if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_groupcall.single_2_group")) {
					GroupCallUtil.setTalkGrp(intent.getExtras().getString("0"));
					GroupCallUtil.setActionMode("com.zed3.sipua.ui_groupcall.single_2_group");
					if (!UserAgent.isCamerPttDialog && !UserAgent.isTempGrpCallMode) {
						final Intent intent2 = new Intent();
						intent2.setClass(Receiver.mContext, (Class) ActvityNotify.class);
						intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Receiver.mContext.startActivity(intent2);
						return;
					}
					LocationOverlayDemo.this.sendBroadcast(new Intent("com.zed3.sipua.camera_ptt_dialog"));
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
					if (intent.getAction().equalsIgnoreCase("com.zed3.sipua_network_changed")) {
						final LinearLayout linearLayout = (LinearLayout) LocationOverlayDemo.this.findViewById(R.id.net_tip3);
						if (intent.getIntExtra("network_state", -1) == 1) {
							linearLayout.setVisibility(View.GONE);
							return;
						}
						linearLayout.setVisibility(View.VISIBLE);
						++LocationOverlayDemo.isRefresh_;
					} else {
						if (intent.getAction().equalsIgnoreCase("com.zed3.sipua_currentgroup_changed")) {
							LocationOverlayDemo.this.ShowCurrentGrp();
							LocationOverlayDemo.this.getAllGrpGisInfo(LocationOverlayDemo.this.currentGrpNum);
							return;
						}
						if (intent.getAction().equalsIgnoreCase("com.zed3.flow.3gflow_alarm")) {
							Tools.FlowAlertDialog((Context) LocationOverlayDemo.this);
							return;
						}
						if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_groupcall.all_groups_change")) {
							LocationOverlayDemo.isRefresh_ = 0;
							LocationOverlayDemo.this.ShowCurrentGrp();
						}
					}
				}
			}
		};
		this.groupListReceiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				final String action = intent.getAction();
				if ((action != null && action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_PTT_GROUP_INFO)) || action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_INFO_CHANGED) || action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO)) {
					LogUtil.makeLog(LocationOverlayDemo.this.TAG, "onReceive()#action = " + action);
					LocationOverlayDemo.this.pttGrps = Receiver.GetCurUA().GetAllGrps();
					LocationOverlayDemo.this.myAdapter.notifyDataSetChanged();
					if (LocationOverlayDemo.this.mDialog != null && LocationOverlayDemo.this.mDialog.isShowing()) {
						LocationOverlayDemo.this.mDialog.dismiss();
						LocationOverlayDemo.this.mDialog.show();
					}
				}
				if (action.equals("com.zed3.sipua.ui_groupcall.clear_grouplist")) {
					GroupListUtil.removeDataOfGroupList();
				}
				if (LocationOverlayDemo.this.isStarted && action.equals("com.zed3.sipua_grouplist_update_over")) {
					LocationOverlayDemo.this.ShowCurrentGrp();
				}
			}
		};
		this.progressHandler = new Handler() {
			public void handleMessage(final Message message) {
			}
		};
		this.hideMyView = false;
		this.kmarea = 5;
		this.locData = null;
		this.myListener = new MyLocationListenner();
		this.myLocationOverlay = null;
		this.pop = null;
		this.popupText = null;
		this.viewCache = null;
		this.mMapView = null;
		this.mMapController = null;
		this.radioButtonListener = null;
		this.isRequest = false;
		this.isFirstLoc = true;
		this.lastnum = 0;
		this.tempGrpList = new ArrayList<String>();
		this.mOverlay = null;
		this.overlayPop = null;
		this.overlayPopupText = null;
		this.viewCacheOverlay = null;
		this.popupInfo = null;
		this.popupLeft = null;
		this.popupRight = null;
		this.mCurItem = null;
		this.isFirstTime = true;
		this.mypopListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(final int n) {
				final String snippet = LocationOverlayDemo.this.mCurItem.getSnippet();
				final String memberAudioType = DataBaseService.getInstance().getMemberAudioType(snippet);
				final String memberVideoType = DataBaseService.getInstance().getMemberVideoType(snippet);
				if (n == 0) {
					LocationOverlayDemo.this.overlayPop.hidePop();
					if (!"1".equals(memberVideoType)) {
						MyToast.showToast(true, (Context) LocationOverlayDemo.mContext, String.valueOf(memberAudioType) + "   " + memberVideoType + "  " + LocationOverlayDemo.mContext.getResources().getString(R.string.ve_service_not));
						return;
					}
					CallUtil.makeVideoCall((Context) LocationOverlayDemo.mContext, snippet, null, "videobut");
				} else if (n == 2) {
					LocationOverlayDemo.this.overlayPop.hidePop();
					if ("1".equals(memberAudioType) && DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
						new AlertDialog.Builder((Context) LocationOverlayDemo.mContext).setItems(R.array.audioDialog, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialogInterface, final int n) {
								switch (n) {
									default: {
									}
									case 0: {
										if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
											if (DeviceInfo.CONFIG_AUDIO_MODE == 1) {
												CallUtil.makeAudioCall((Context) LocationOverlayDemo.mContext, snippet, null);
												return;
											}
											LocationOverlayDemo.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + LocationOverlayDemo.this.number)));
											return;
										} else {
											if (MemoryMg.getInstance().PhoneType == 1) {
												CallUtil.makeAudioCall((Context) LocationOverlayDemo.mContext, snippet, null);
												return;
											}
											LocationOverlayDemo.mContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + LocationOverlayDemo.this.number)));
											return;
										}
									}
								}
							}
						}).show();
						return;
					}
					MyToast.showToast(true, (Context) LocationOverlayDemo.mContext, LocationOverlayDemo.mContext.getResources().getString(R.string.vc_service_not));
				}
			}
		};
		this.gisGetThread = null;
		this.gisMemCount = 0;
		this.pttDownRunable = new Runnable() {
			@Override
			public void run() {
				LocationOverlayDemo.setPttBackground(LocationOverlayDemo.isPttPressing = true);
				final UserAgent getCurUA = Receiver.GetCurUA();
				if (getCurUA != null) {
					getCurUA.OnPttKey(true, UserAgent.PttPRMode.ScreenPress);
					return;
				}
				MyLog.e(LocationOverlayDemo.this.TAG, "pttDownRunable ,ua = null");
			}
		};
		this.pttUpRunable = new Runnable() {
			@Override
			public void run() {
				LocationOverlayDemo.setPttBackground(LocationOverlayDemo.isPttPressing = false);
				final UserAgent getCurUA = Receiver.GetCurUA();
				if (getCurUA != null) {
					getCurUA.OnPttKey(false, UserAgent.PttPRMode.Idle);
					return;
				}
				MyLog.e(LocationOverlayDemo.this.TAG, "pttUpRunable ,ua = null");
			}
		};
		this.pttPressHandler = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.what) {
					default: {
					}
					case 0: {
						LocationOverlayDemo.setPttBackground(LocationOverlayDemo.isPttPressing = false);
					}
					case 1: {
						LocationOverlayDemo.setPttBackground(LocationOverlayDemo.isPttPressing = true);
					}
				}
			}
		};
	}

	private void ParseJson(final String p0, final String p1) {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     3: dup
		//     4: aload_1
		//     5: invokespecial   org/json/JSONObject.<init>:(Ljava/lang/String;)V
		//     8: astore          7
		//    10: aload           7
		//    12: ldc_w           "Content"
		//    15: invokevirtual   org/json/JSONObject.getJSONArray:(Ljava/lang/String;)Lorg/json/JSONArray;
		//    18: astore_1
		//    19: aload_0
		//    20: aload           7
		//    22: ldc_w           "TotalGIS"
		//    25: invokevirtual   org/json/JSONObject.getString:(Ljava/lang/String;)Ljava/lang/String;
		//    28: invokestatic    java/lang/Integer.parseInt:(Ljava/lang/String;)I
		//    31: putfield        com/zed3/sipua/baiduMap/LocationOverlayDemo.gisMemCount:I
		//    34: iconst_0
		//    35: istore_3
		//    36: aload_1
		//    37: ifnull          45
		//    40: aload_1
		//    41: invokevirtual   org/json/JSONArray.length:()I
		//    44: istore_3
		//    45: ldc_w           "LocationOverLayDemo"
		//    48: new             Ljava/lang/StringBuilder;
		//    51: dup
		//    52: ldc_w           "ParseJson : total = "
		//    55: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//    58: aload_0
		//    59: getfield        com/zed3/sipua/baiduMap/LocationOverlayDemo.gisMemCount:I
		//    62: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//    65: ldc_w           ",curPageSize = "
		//    68: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//    71: iload_3
		//    72: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//    75: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//    78: invokestatic    com/zed3/log/MyLog.i:(Ljava/lang/String;Ljava/lang/String;)V
		//    81: iconst_0
		//    82: istore          4
		//    84: goto            649
		//    87: aload_1
		//    88: iload           4
		//    90: invokevirtual   org/json/JSONArray.getJSONObject:(I)Lorg/json/JSONObject;
		//    93: astore          7
		//    95: new             Ljava/lang/StringBuilder;
		//    98: dup
		//    99: ldc_w           "Info print all==>"
		//   102: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   105: iload           4
		//   107: invokevirtual   java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		//   110: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   113: aload           7
		//   115: invokevirtual   org/json/JSONObject.toString:()Ljava/lang/String;
		//   118: invokestatic    android/util/Log.e:(Ljava/lang/String;Ljava/lang/String;)I
		//   121: pop
		//   122: new             Lcom/zed3/sipua/baiduMap/GroupMember;
		//   125: dup
		//   126: invokespecial   com/zed3/sipua/baiduMap/GroupMember.<init>:()V
		//   129: astore          8
		//   131: aload           7
		//   133: ldc_w           "Latitude"
		//   136: invokevirtual   org/json/JSONObject.optString:(Ljava/lang/String;)Ljava/lang/String;
		//   139: ldc_w           "null"
		//   142: invokevirtual   java/lang/String.equalsIgnoreCase:(Ljava/lang/String;)Z
		//   145: ifne            656
		//   148: aload           7
		//   150: ldc_w           "Longitude"
		//   153: invokevirtual   org/json/JSONObject.optString:(Ljava/lang/String;)Ljava/lang/String;
		//   156: ldc_w           "null"
		//   159: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//   162: ifne            656
		//   165: aload           7
		//   167: ldc_w           "Latitude"
		//   170: invokevirtual   org/json/JSONObject.optString:(Ljava/lang/String;)Ljava/lang/String;
		//   173: ifnull          656
		//   176: aload           7
		//   178: ldc_w           "Longitude"
		//   181: invokevirtual   org/json/JSONObject.optString:(Ljava/lang/String;)Ljava/lang/String;
		//   184: ifnonnull       190
		//   187: goto            656
		//   190: aload           7
		//   192: ldc_w           "Latitude"
		//   195: invokevirtual   org/json/JSONObject.optString:(Ljava/lang/String;)Ljava/lang/String;
		//   198: ldc_w           "0.000000"
		//   201: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//   204: ifeq            228
		//   207: aload           7
		//   209: ldc_w           "Longitude"
		//   212: invokevirtual   org/json/JSONObject.optString:(Ljava/lang/String;)Ljava/lang/String;
		//   215: ldc_w           "0.000000"
		//   218: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//   221: istore          6
		//   223: iload           6
		//   225: ifne            656
		//   228: aload           8
		//   230: new             Lcom/baidu/platform/comapi/basestruct/GeoPoint;
		//   233: dup
		//   234: aload           7
		//   236: ldc_w           "Latitude"
		//   239: invokevirtual   org/json/JSONObject.optString:(Ljava/lang/String;)Ljava/lang/String;
		//   242: invokestatic    java/lang/Double.parseDouble:(Ljava/lang/String;)D
		//   245: ldc2_w          1000000.0
		//   248: dmul
		//   249: d2i
		//   250: aload           7
		//   252: ldc_w           "Longitude"
		//   255: invokevirtual   org/json/JSONObject.optString:(Ljava/lang/String;)Ljava/lang/String;
		//   258: invokestatic    java/lang/Double.parseDouble:(Ljava/lang/String;)D
		//   261: ldc2_w          1000000.0
		//   264: dmul
		//   265: d2i
		//   266: invokespecial   com/baidu/platform/comapi/basestruct/GeoPoint.<init>:(II)V
		//   269: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.setGeo:(Lcom/baidu/platform/comapi/basestruct/GeoPoint;)V
		//   272: aload           8
		//   274: aload           7
		//   276: ldc_w           "User"
		//   279: invokevirtual   org/json/JSONObject.optString:(Ljava/lang/String;)Ljava/lang/String;
		//   282: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.setNum:(Ljava/lang/String;)V
		//   285: aload           8
		//   287: aload           8
		//   289: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.getNum:()Ljava/lang/String;
		//   292: invokestatic    com/zed3/sipua/ui/contact/ContactUtil.getUserName:(Ljava/lang/String;)Ljava/lang/String;
		//   295: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.setName:(Ljava/lang/String;)V
		//   298: aload_0
		//   299: getfield        com/zed3/sipua/baiduMap/LocationOverlayDemo.arrayList:Ljava/util/ArrayList;
		//   302: ifnull          328
		//   305: aload           8
		//   307: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.getName:()Ljava/lang/String;
		//   310: ifnonnull       328
		//   313: iconst_0
		//   314: istore          5
		//   316: iload           5
		//   318: aload_0
		//   319: getfield        com/zed3/sipua/baiduMap/LocationOverlayDemo.arrayList:Ljava/util/ArrayList;
		//   322: invokevirtual   java/util/ArrayList.size:()I
		//   325: if_icmplt       523
		//   328: aload_0
		//   329: getfield        com/zed3/sipua/baiduMap/LocationOverlayDemo.arrayList:Ljava/util/ArrayList;
		//   332: ifnull          350
		//   335: iconst_0
		//   336: istore          5
		//   338: iload           5
		//   340: aload_0
		//   341: getfield        com/zed3/sipua/baiduMap/LocationOverlayDemo.arrayList:Ljava/util/ArrayList;
		//   344: invokevirtual   java/util/ArrayList.size:()I
		//   347: if_icmplt       572
		//   350: aload           8
		//   352: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.getName:()Ljava/lang/String;
		//   355: ifnonnull       368
		//   358: aload           8
		//   360: aload           8
		//   362: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.getNum:()Ljava/lang/String;
		//   365: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.setName:(Ljava/lang/String;)V
		//   368: ldc_w           "guojunfeng2013"
		//   371: new             Ljava/lang/StringBuilder;
		//   374: dup
		//   375: ldc_w           "result:"
		//   378: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   381: aload           8
		//   383: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.getName:()Ljava/lang/String;
		//   386: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   389: aload           8
		//   391: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.getNum:()Ljava/lang/String;
		//   394: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
		//   397: aload           8
		//   399: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.getGeo:()Lcom/baidu/platform/comapi/basestruct/GeoPoint;
		//   402: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
		//   405: aload           8
		//   407: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.isOnline:()Z
		//   410: invokevirtual   java/lang/StringBuilder.append:(Z)Ljava/lang/StringBuilder;
		//   413: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   416: invokestatic    android/util/Log.e:(Ljava/lang/String;Ljava/lang/String;)I
		//   419: pop
		//   420: aload           8
		//   422: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.getNum:()Ljava/lang/String;
		//   425: invokestatic    com/zed3/sipua/ui/Settings.getUserName:()Ljava/lang/String;
		//   428: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//   431: ifne            656
		//   434: getstatic       com/zed3/sipua/baiduMap/GrpGisUtils.mGisMap:Ljava/util/Map;
		//   437: aload_2
		//   438: invokeinterface java/util/Map.get:(Ljava/lang/Object;)Ljava/lang/Object;
		//   443: checkcast       Lcom/zed3/sipua/baiduMap/GisQuestStateInfo;
		//   446: getfield        com/zed3/sipua/baiduMap/GisQuestStateInfo.members:Ljava/util/ArrayList;
		//   449: aload           8
		//   451: invokevirtual   java/util/ArrayList.add:(Ljava/lang/Object;)Z
		//   454: pop
		//   455: goto            656
		//   458: astore_1
		//   459: aload_1
		//   460: invokevirtual   org/json/JSONException.printStackTrace:()V
		//   463: ldc_w           "=====>"
		//   466: new             Ljava/lang/StringBuilder;
		//   469: dup
		//   470: ldc_w           "json parse exception..."
		//   473: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   476: aload_1
		//   477: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
		//   480: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   483: invokestatic    android/util/Log.e:(Ljava/lang/String;Ljava/lang/String;)I
		//   486: pop
		//   487: return
		//   488: astore          7
		//   490: ldc_w           "=====>"
		//   493: new             Ljava/lang/StringBuilder;
		//   496: dup
		//   497: ldc_w           "json parse NumberFormatException..."
		//   500: invokespecial   java/lang/StringBuilder.<init>:(Ljava/lang/String;)V
		//   503: aload           7
		//   505: invokevirtual   java/lang/StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
		//   508: invokevirtual   java/lang/StringBuilder.toString:()Ljava/lang/String;
		//   511: invokestatic    android/util/Log.e:(Ljava/lang/String;Ljava/lang/String;)I
		//   514: pop
		//   515: aload           7
		//   517: invokevirtual   java/lang/NumberFormatException.printStackTrace:()V
		//   520: goto            656
		//   523: aload_0
		//   524: getfield        com/zed3/sipua/baiduMap/LocationOverlayDemo.arrayList:Ljava/util/ArrayList;
		//   527: iload           5
		//   529: invokevirtual   java/util/ArrayList.get:(I)Ljava/lang/Object;
		//   532: checkcast       Lorg/zoolu/tools/GroupListInfo;
		//   535: getfield        org/zoolu/tools/GroupListInfo.GrpNum:Ljava/lang/String;
		//   538: aload           8
		//   540: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.getNum:()Ljava/lang/String;
		//   543: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//   546: ifeq            665
		//   549: aload           8
		//   551: aload_0
		//   552: getfield        com/zed3/sipua/baiduMap/LocationOverlayDemo.arrayList:Ljava/util/ArrayList;
		//   555: iload           5
		//   557: invokevirtual   java/util/ArrayList.get:(I)Ljava/lang/Object;
		//   560: checkcast       Lorg/zoolu/tools/GroupListInfo;
		//   563: getfield        org/zoolu/tools/GroupListInfo.GrpName:Ljava/lang/String;
		//   566: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.setName:(Ljava/lang/String;)V
		//   569: goto            328
		//   572: aload_0
		//   573: getfield        com/zed3/sipua/baiduMap/LocationOverlayDemo.arrayList:Ljava/util/ArrayList;
		//   576: iload           5
		//   578: invokevirtual   java/util/ArrayList.get:(I)Ljava/lang/Object;
		//   581: checkcast       Lorg/zoolu/tools/GroupListInfo;
		//   584: getfield        org/zoolu/tools/GroupListInfo.GrpNum:Ljava/lang/String;
		//   587: aload           8
		//   589: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.getNum:()Ljava/lang/String;
		//   592: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//   595: ifeq            640
		//   598: aload_0
		//   599: getfield        com/zed3/sipua/baiduMap/LocationOverlayDemo.arrayList:Ljava/util/ArrayList;
		//   602: iload           5
		//   604: invokevirtual   java/util/ArrayList.get:(I)Ljava/lang/Object;
		//   607: checkcast       Lorg/zoolu/tools/GroupListInfo;
		//   610: getfield        org/zoolu/tools/GroupListInfo.GrpState:Ljava/lang/String;
		//   613: ldc_w           "0"
		//   616: invokevirtual   java/lang/String.equals:(Ljava/lang/Object;)Z
		//   619: ifeq            631
		//   622: aload           8
		//   624: iconst_0
		//   625: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.setOnline:(Z)V
		//   628: goto            350
		//   631: aload           8
		//   633: iconst_1
		//   634: invokevirtual   com/zed3/sipua/baiduMap/GroupMember.setOnline:(Z)V
		//   637: goto            350
		//   640: iload           5
		//   642: iconst_1
		//   643: iadd
		//   644: istore          5
		//   646: goto            338
		//   649: iload           4
		//   651: iload_3
		//   652: if_icmplt       87
		//   655: return
		//   656: iload           4
		//   658: iconst_1
		//   659: iadd
		//   660: istore          4
		//   662: goto            649
		//   665: iload           5
		//   667: iconst_1
		//   668: iadd
		//   669: istore          5
		//   671: goto            316
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  ---------------------------------
		//  0      34     458    488    Lorg/json/JSONException;
		//  40     45     458    488    Lorg/json/JSONException;
		//  45     81     458    488    Lorg/json/JSONException;
		//  87     187    458    488    Lorg/json/JSONException;
		//  190    223    458    488    Lorg/json/JSONException;
		//  228    272    488    523    Ljava/lang/NumberFormatException;
		//  228    272    458    488    Lorg/json/JSONException;
		//  272    313    458    488    Lorg/json/JSONException;
		//  316    328    458    488    Lorg/json/JSONException;
		//  328    335    458    488    Lorg/json/JSONException;
		//  338    350    458    488    Lorg/json/JSONException;
		//  350    368    458    488    Lorg/json/JSONException;
		//  368    455    458    488    Lorg/json/JSONException;
		//  490    520    458    488    Lorg/json/JSONException;
		//  523    569    458    488    Lorg/json/JSONException;
		//  572    628    458    488    Lorg/json/JSONException;
		//  631    637    458    488    Lorg/json/JSONException;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0228:
		//     at com.strobel.decompiler.ast.Error.expressionLinkedFromMultipleLocations(Error.java:27)
		//     at com.strobel.decompiler.ast.AstOptimizer.mergeDisparateObjectInitializations(AstOptimizer.java:2596)
		//     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:235)
		//     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:757)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:655)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:532)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:499)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:141)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:130)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:105)
		//     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
		//     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
		//     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
		//     at us.deathmarine.luyten.FileSaver.access.300(FileSaver.java:45)
		//     at us.deathmarine.luyten.FileSaver.4.run(FileSaver.java:112)
		//     at java.lang.Thread.run(Thread.java:745)
		//
		throw new IllegalStateException("An error occurred while decompiling this method.");
	}

	private void QuestSJ(final String s, final int n, final long n2) throws Exception {
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
					LocationOverlayDemo.this.ParseJson(string, s);
				} else {
					GrpGisUtils.mGisMap.get(s).setReqestState(3);
					LocationOverlayDemo.this.gisMemCount = 0;
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
			if (LocationOverlayDemo.isRefresh_ != 1) {
				GroupListUtil.getDataCurrentGroupList();
			}
			this.mGroupListsMap = GroupListUtil.getGroupListsMap();
			this.arrayList = this.mGroupListsMap.get(getCurGrp);
		}
		LocationOverlayDemo.isRefresh_ = 1;
	}

	public static boolean checkHasCurrentGrp(final Context context) {
		return LocationOverlayDemo.mHasPttGrp;
	}

	private int findPos(final PttGrp pttGrp, final List<PttGrp> list) {
		if (this.pttGrps != null && this.pttGrps.GetCount() > 0 && pttGrp != null) {
			int n = 0;
			for (final PttGrp pttGrp2 : list) {
				final int n2 = n;
				if (pttGrp.grpID.equals(pttGrp2.grpID)) {
					return n2;
				}
				++n;
			}
		}
		return -1;
	}

	private void getAllGrpGisInfo(final String s) {
		if (NetChecker.check((Context) LocationOverlayDemo.mContext, false)) {
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
			(this.gisGetThread = new Thread() {
				@Override
				public void run() {
					if (GrpGisUtils.mGisMap.get(s).members != null) {
						GrpGisUtils.mGisMap.get(s).members.clear();
						Log.e("\u5207\u6362\u7ec4", String.valueOf(s) + GrpGisUtils.mGisMap.get(s).members.toString());
					}
					// TODO
				}
			}).setName("GisGetThread");
			this.gisGetThread.start();
		}
	}

	public static LocationOverlayDemo getInstance() {
		return LocationOverlayDemo.mContext;
	}

	private List<String> getList(final List<GroupMember> list) {
		final ArrayList<String> list2 = new ArrayList<String>();
		final Iterator<GroupMember> iterator = list.iterator();
		while (iterator.hasNext()) {
			list2.add(iterator.next().getNum());
		}
		return list2;
	}

	public static void setPttBackground(final boolean b) {
		final View pttkeyMap = LocationOverlayDemo.pttkeyMap;
		int backgroundResource;
		if (b) {
			backgroundResource = R.drawable.ptt_down_map;
		} else {
			backgroundResource = R.drawable.ptt_up_map;
		}
		pttkeyMap.setBackgroundResource(backgroundResource);
	}

	public String ShowPttStatus(final PttGrp.E_Grp_State e_Grp_State) {
		// TODO
		return "";
//        switch (e_Grp_State) {
//            default: {
//                return this.getResources().getString(R.string.status_error);
//            }
//            case 1: {
//                return this.getResources().getString(R.string.status_close);
//            }
//            case 2: {
//                return this.getResources().getString(R.string.status_free);
//            }
//            case 4: {
//                return this.getResources().getString(R.string.status_speaking);
//            }
//            case 5: {
//                return this.getResources().getString(R.string.status_listening);
//            }
//            case 6: {
//                return this.getResources().getString(R.string.status_waiting);
//            }
//        }
	}

	public String ShowSpeakerStatus(final String s, final String s2) {
		String string;
		if (s == null || s.equals("")) {
			string = this.getResources().getString(R.string.none_speaker);
		} else {
			string = s;
			if (s2.equals(Settings.getUserName())) {
				string = s;
				if (LocationOverlayDemo.isPttPressing) {
					return String.valueOf(s) + this.getResources().getString(R.string.self_speaker);
				}
			}
		}
		return string;
	}

	public void createPaopao() {
		this.viewCache = this.getLayoutInflater().inflate(R.layout.custom_text_view, (ViewGroup) null);
		this.popupText = (TextView) this.viewCache.findViewById(R.id.textcache);
		this.pop = new PopupOverlay(this.mMapView, new PopupClickListener() {
			@Override
			public void onClickedPopup(final int n) {
				LocationOverlayDemo.this.pop.hidePop();
			}
		});
	}

	public String getTime() {
		try {
			return new SimpleDateFormat(" HHmmss ").format(new Date(System.currentTimeMillis()));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void initOverlay(final GeoPoint geoPoint, final int n, final String s, final String s2) {
		if (!TextUtils.isEmpty((CharSequence) s2) && this.mMapView != null) {
			this.viewCacheOverlay = this.getLayoutInflater().inflate(R.layout.custom_text_view, (ViewGroup) null);
			this.popupInfo = this.viewCacheOverlay.findViewById(R.id.popinfo);
			this.popupLeft = this.viewCacheOverlay.findViewById(R.id.popleft);
			this.popupRight = this.viewCacheOverlay.findViewById(R.id.popright);
			this.overlayPopupText = (TextView) this.viewCacheOverlay.findViewById(R.id.textcache);
			this.overlayPop = new PopupOverlay(this.mMapView, this.mypopListener);
			this.list = null;
			if (n < 0 || geoPoint == null) {
				final GisQuestStateInfo gisQuestStateInfo = GrpGisUtils.mGisMap.get(s2);
				if (gisQuestStateInfo != null && gisQuestStateInfo.members != null) {
					this.list = (List<GroupMember>) GrpGisUtils.mGisMap.get(s2).members.clone();
				}
			} else {
				this.list = MapTools.getMemInMiles(geoPoint, n, GrpGisUtils.mGisMap.get(s2).members);
			}
			if (this.list != null && this.list.size() >= 1) {
				if (s != null && !MemoryMg.getInstance().TerminalNum.equals(s) && !this.getList(this.list).contains(s)) {
					MyToast.showToast(true, (Context) LocationOverlayDemo.mContext, R.string.no_user_location);
				}
				for (final GroupMember groupMember : this.list) {
					this.item = new OverlayItem(groupMember.getGeo(), groupMember.getName(), groupMember.getNum());
					if (!groupMember.isOnline()) {
						this.item.setMarker(this.getResources().getDrawable(R.drawable.icon_notonline));
					}
					if (this.userNum != null && groupMember.getNum().equals(this.userNum)) {
						this.item.setMarker(this.getResources().getDrawable(R.drawable.icon_talkback));
					}
					if (s != null && groupMember.getNum().equalsIgnoreCase(s)) {
						this.mMapController.animateTo(groupMember.getGeo());
						this.item.setMarker(this.getResources().getDrawable(R.drawable.icon_pitch));
						this.mCurItem = this.item;
						this.overlayPopupText.setText((CharSequence) this.mCurItem.getTitle());
						this.overlayPop.showPopup(new Bitmap[]{BMapUtil.getBitmapFromView(this.popupLeft), BMapUtil.getBitmapFromView(this.popupInfo), BMapUtil.getBitmapFromView(this.popupRight)}, this.mCurItem.getPoint(), 32);
					}
					if (this.mOverlay != null) {
						this.mOverlay.addItem(this.item);
					}
				}
				if (geoPoint == null && n < 0 && this.isFirstTime && this.mMapView != null && this.mMapView.getOverlays() != null) {
					this.mMapView.getOverlays().add(this.mOverlay);
					this.isFirstTime = false;
				}
				this.mMapView.refresh();
				this.number = null;
			}
		}
	}

	public void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_locationoverlay);
		GroupListUtil.getData4GroupList();
		this.ib_zoom_in = (ImageButton) this.findViewById(R.id.zoom_in);
		this.ib_zoom_out = (ImageButton) this.findViewById(R.id.zoom_out);
		this.ib_position = (ImageButton) this.findViewById(R.id.position);
		this.tab_show1 = (LinearLayout) this.findViewById(R.id.map_tab_show1);
		(this.tab_hide = (LinearLayout) this.findViewById(R.id.map_tab_hide)).setLongClickable(true);
		this.tab_hide.setClickable(true);
		LocationOverlayDemo.mContext = this;
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
		this.mFilter.addAction("com.zed3.sipua_currentgroup_changed");
		this.mFilter.addAction("com.zed3.flow.3gflow_alarm");
		LocationOverlayDemo.mContext.registerReceiver(this.mReceiver, this.mFilter);
		if (this.intentfilter2 == null) {
			(this.intentfilter2 = new IntentFilter()).addAction("com.zed3.sipua_grouplist_update_over");
			this.intentfilter2.addAction("com.zed3.sipua.ui_groupcall.all_groups_clear_over");
			this.intentfilter2.addAction("com.zed3.sipua.ui_groupcall.clear_grouplist");
			this.intentfilter2.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_PTT_GROUP_INFO);
			this.intentfilter2.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_INFO_CHANGED);
			this.intentfilter2.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO);
		}
		LocationOverlayDemo.mContext.registerReceiver(this.groupListReceiver, this.intentfilter2);
		this.number = this.getIntent().getStringExtra("transmitnumber");
		(this.timer1 = new Timer()).schedule(new TimerTask() {
			@Override
			public void run() {
				final Message message = new Message();
				message.what = 1;
				LocationOverlayDemo.this.mHandler.sendMessage(message);
			}
		}, 0L, 30000L);
		this.mMapView = (MapView) this.findViewById(R.id.bmapView);
		this.mMapController = this.mMapView.getController();
		this.mMapView.getController().setZoom(14.0f);
		this.ib_zoom_in.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final int n = Math.round(LocationOverlayDemo.this.mMapView.getZoomLevel()) + 1;
				if (n <= 19) {
					LocationOverlayDemo.this.mMapView.getController().setZoom(n);
				}
			}
		});
		this.ib_zoom_out.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final int n = Math.round(LocationOverlayDemo.this.mMapView.getZoomLevel()) - 1;
				if (n >= 3) {
					LocationOverlayDemo.this.mMapView.getController().setZoom(n);
				}
			}
		});
		this.ib_position.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				LocationOverlayDemo.this.requestLocClick();
			}
		});
		this.mMapView.getController().enableClick(true);
		this.createPaopao();
		this.mOverlay = new MyOverlay(this.getResources().getDrawable(R.drawable.icon_gcoding), this.mMapView);
		this.mLocClient = new LocationClient((Context) this);
		this.locData = new LocationData();
		this.mLocClient.registerLocationListener(this.myListener);
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
		this.myLocationOverlay = new locationOverlay(this.mMapView);
		this.mMapView.getOverlays().add(this.myLocationOverlay);
		this.myLocationOverlay.enableCompass();
		this.mMapView.refresh();
		(this.btn_home = (LinearLayout) this.findViewById(R.id.btn_home)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				LocationOverlayDemo.this.finish();
			}
		});
		this.btn_home.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) LocationOverlayDemo.this.findViewById(R.id.t_home);
				final TextView textView2 = (TextView) LocationOverlayDemo.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						LocationOverlayDemo.this.btn_home.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(LocationOverlayDemo.this.getResources().getColor(R.color.font_color3));
						LocationOverlayDemo.this.btn_home.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
		this.title = (TextView) this.findViewById(R.id.map_title);
		if (Receiver.GetCurUA().GetCurGrp() != null) {
			this.title.setText((CharSequence) Receiver.GetCurUA().GetCurGrp().grpName);
		}
		(this.btn_changegroup = (LinearLayout) this.findViewById(R.id.btn_changegroup)).setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) LocationOverlayDemo.this.findViewById(R.id.t_spin);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						textView.setBackgroundResource(R.color.btn_click_bg);
						break;
					}
					case 1: {
						textView.setTextColor(LocationOverlayDemo.this.getResources().getColor(R.color.font_color3));
						textView.setBackgroundResource(R.color.whole_bg);
						break;
					}
				}
				return false;
			}
		});
//        this.btn_changegroup.setOnClickListener((View.OnClickListener)new View.OnClickListener() {
//            public void onClick(final View view) {
//                LocationOverlayDemo.access.17(LocationOverlayDemo.this, (Dialog)new AlertDialog.Builder((Context)LocationOverlayDemo.this).setAdapter((ListAdapter)LocationOverlayDemo.this.myAdapter, (DialogInterface.OnClickListener)new DialogInterface.OnClickListener() {
//                    public void onClick(final DialogInterface dialogInterface, final int n) {
//                        if (!NetChecker.check((Context)LocationOverlayDemo.mContext, true)) {
//                            MyToast.showToast(true, (Context)LocationOverlayDemo.mContext, LocationOverlayDemo.this.getResources().getString(R.string.group_notify));
//                            return;
//                        }
//                        if (Receiver.GetCurUA().GetCurGrp() != LocationOverlayDemo.this.pttGrps.GetGrpByIndex(n)) {
//                            if (LocationOverlayDemo.isPttPressing) {
//                                MyToast.showToast(true, (Context)LocationOverlayDemo.mContext, R.string.release_ptt_and_try_again);
//                                return;
//                            }
//                            Receiver.GetCurUA().SetCurGrp(LocationOverlayDemo.this.pttGrps.GetGrpByIndex(n), true);
//                            LocationOverlayDemo.access.16(LocationOverlayDemo.this, LocationOverlayDemo.this.pttGrps.GetGrpByIndex(n).grpID);
//                            LocationOverlayDemo.this.getAllGrpGisInfo(LocationOverlayDemo.this.currentGrpNum);
//                        }
//                        LocationOverlayDemo.this.title.setText((CharSequence)LocationOverlayDemo.this.pttGrps.GetGrpByIndex(n).grpName);
//                    }
//                }).create());
//                LocationOverlayDemo.this.mDialog.show();
//                LocationOverlayDemo.this.mDialog.setCanceledOnTouchOutside(true);
//            }
//        });
		(LocationOverlayDemo.pttkeyMap = this.findViewById(R.id.pttkeymap)).setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					case 0: {
						if (NetChecker.check((Context) LocationOverlayDemo.mContext, true)) {
							LocationOverlayDemo.pttkeyMap.setBackgroundResource(R.drawable.ptt_down_map);
							GroupCallUtil.makeGroupCall(LocationOverlayDemo.isPttPressing = true, false, UserAgent.PttPRMode.ScreenPress);
							return true;
						}
						break;
					}
					case 1: {
						if (NetChecker.check((Context) LocationOverlayDemo.mContext, true) && LocationOverlayDemo.isPttPressing) {
							GroupCallUtil.makeGroupCall(LocationOverlayDemo.isPttPressing = false, false, UserAgent.PttPRMode.Idle);
							LocationOverlayDemo.pttkeyMap.setBackgroundResource(R.drawable.ptt_up_map);
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
				LocationOverlayDemo.this.requestLocClick();
			}
		});
		this.hide_text.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				LocationOverlayDemo.this.tab_show1.setVisibility(View.VISIBLE);
				LocationOverlayDemo.this.tab_hide.setVisibility(View.GONE);
			}
		});
		this.requestLocClick();
		this.mPttHandler = new Handler();
		this.myAdapter = new MyAdapter((Context) this, this.pttGrps);
	}

	protected void onDestroy() {
		if (this.timer1 != null) {
			this.timer1.cancel();
			this.timer1 = null;
		}
		while (true) {
			while (true) {
				try {
					if (this.mFilter != null) {
						LocationOverlayDemo.mContext.unregisterReceiver(this.mReceiver);
					} else {
						MyLog.i("GroupCallActivity", "recv unregister fail! mFilter is null. ");
					}
					if (this.intentfilter2 != null) {
						LocationOverlayDemo.mContext.unregisterReceiver(this.groupListReceiver);
						this.mLocClient.unRegisterLocationListener(this.myListener);
						if (this.mLocClient != null) {
							this.mLocClient.stop();
						}
						if (this.mOverlay != null) {
							this.mOverlay.removeAll();
							this.mOverlay = null;
						}
						this.mMapView.destroy();
						this.mMapView = null;
						LocationOverlayDemo.mContext = null;
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
		return super.onKeyUp(n, keyEvent);
	}

	protected void onPause() {
		this.mMapView.onPause();
		super.onPause();
	}

	protected void onRestoreInstanceState(final Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		this.mMapView.onRestoreInstanceState(bundle);
	}

	protected void onResume() {
		this.mMapView.onResume();
		this.number = this.getIntent().getStringExtra("transmitnumber");
		LocationOverlayDemo.isResume = true;
		if (this.timer1 == null) {
			(this.timer1 = new Timer()).schedule(new TimerTask() {
				@Override
				public void run() {
					final Message message = new Message();
					message.what = 1;
					LocationOverlayDemo.this.mHandler.sendMessage(message);
				}
			}, 0L, 30000L);
		}
		Receiver.engine((Context) LocationOverlayDemo.mContext);
		if (!NetChecker.check((Context) this, false)) {
			((LinearLayout) this.findViewById(R.id.net_tip3)).setVisibility(View.VISIBLE);
		} else {
			((LinearLayout) this.findViewById(R.id.net_tip3)).setVisibility(View.GONE);
		}
		LocationOverlayDemo.mHasPttGrp = (Receiver.GetCurUA().GetCurGrp() != null);
		setPttBackground(LocationOverlayDemo.isPttPressing);
		super.onResume();
	}

	protected void onSaveInstanceState(final Bundle bundle) {
		super.onSaveInstanceState(bundle);
		this.mMapView.onSaveInstanceState(bundle);
	}

	protected void onStart() {
		this.isStarted = true;
		if (this.pttGrps.GetCount() == 0) {
			LocationOverlayDemo.isRefresh_ = 0;
			this.ShowCurrentGrp();
		}
		super.onStart();
	}

	protected void onStop() {
		if (this.timer1 != null) {
			this.timer1.cancel();
			this.timer1 = null;
		}
		LocationOverlayDemo.isResume = false;
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
			if (bdLocation == null) {
				return;
			}
			LocationOverlayDemo.this.locData.latitude = bdLocation.getLatitude();
			LocationOverlayDemo.this.locData.longitude = bdLocation.getLongitude();
			LocationOverlayDemo.this.locData.accuracy = bdLocation.getRadius();
			LocationOverlayDemo.this.locData.direction = bdLocation.getDirection();
			LocationOverlayDemo.this.myLocationOverlay.setData(LocationOverlayDemo.this.locData);
			if (LocationOverlayDemo.this.mMapView != null) {
				LocationOverlayDemo.this.mMapView.refresh();
			}
			if (LocationOverlayDemo.this.isRequest || LocationOverlayDemo.this.isFirstLoc) {
				Log.d("LocationOverlay", "receive location, animate to it");
				LocationOverlayDemo.this.mMapController.animateTo(new GeoPoint((int) (LocationOverlayDemo.this.locData.latitude * 1000000.0), (int) (LocationOverlayDemo.this.locData.longitude * 1000000.0)));
				LocationOverlayDemo.this.isRequest = false;
			}
			LocationOverlayDemo.this.isFirstLoc = false;
		}

		public void onReceivePoi(final BDLocation bdLocation) {
			if (bdLocation == null) {
			}
		}
	}

	public class MyOverlay extends ItemizedOverlay {
		private ArrayList<OverlayItem> overlayItemList;

		public MyOverlay(final Drawable drawable, final MapView mapView) {
			super(drawable, mapView);
			this.overlayItemList = new ArrayList<OverlayItem>();
		}

		@Override
		protected OverlayItem createItem(final int n) {
			return this.overlayItemList.get(n);
		}

		public boolean onTap(final int n) {
			LocationOverlayDemo.this.item = this.getItem(n);
			if (LocationOverlayDemo.this.item != null) {
				(LocationOverlayDemo.this.mCurItem = LocationOverlayDemo.this.item).setMarker(LocationOverlayDemo.this.getResources().getDrawable(R.drawable.icon_pitch));
				LocationOverlayDemo.this.mOverlay.updateItem(LocationOverlayDemo.this.mCurItem);
				LocationOverlayDemo.this.mMapView.refresh();
				LocationOverlayDemo.this.overlayPopupText.setText((CharSequence) this.getItem(n).getTitle());
				LocationOverlayDemo.this.overlayPop.showPopup(new Bitmap[]{BMapUtil.getBitmapFromView(LocationOverlayDemo.this.popupLeft), BMapUtil.getBitmapFromView(LocationOverlayDemo.this.popupInfo), BMapUtil.getBitmapFromView(LocationOverlayDemo.this.popupRight)}, LocationOverlayDemo.this.item.getPoint(), 32);
			}
			return true;
		}

		@Override
		public boolean onTap(final GeoPoint geoPoint, final MapView mapView) {
			if (LocationOverlayDemo.this.overlayPop != null) {
				LocationOverlayDemo.this.overlayPop.hidePop();
			}
			if (LocationOverlayDemo.this.mOverlay != null) {
				LocationOverlayDemo.this.mOverlay.removeAll();
			}
			if (LocationOverlayDemo.this.list != null) {
				for (final GroupMember groupMember : LocationOverlayDemo.this.list) {
					LocationOverlayDemo.this.item = new OverlayItem(groupMember.getGeo(), groupMember.getName(), groupMember.getNum());
					if (!groupMember.isOnline()) {
						LocationOverlayDemo.this.item.setMarker(LocationOverlayDemo.this.getResources().getDrawable(R.drawable.icon_notonline));
					}
					if (LocationOverlayDemo.this.mOverlay != null) {
						LocationOverlayDemo.this.mOverlay.addItem(LocationOverlayDemo.this.item);
					}
				}
			}
			mapView.refresh();
			return false;
		}
	}

	public class locationOverlay extends MyLocationOverlay {
		public locationOverlay(final MapView mapView) {
			super(mapView);
		}

		@Override
		protected boolean dispatchTap() {
			LocationOverlayDemo.this.popupText.setBackgroundResource(R.drawable.popup);
			LocationOverlayDemo.this.popupText.setText((CharSequence) "\u6211\u7684\u4f4d\u7f6e");
			if (LocationOverlayDemo.this.overlayPop != null) {
				LocationOverlayDemo.this.overlayPop.hidePop();
			}
			return true;
		}
	}
}
