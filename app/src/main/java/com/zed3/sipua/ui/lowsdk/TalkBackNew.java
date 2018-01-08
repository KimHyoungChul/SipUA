package com.zed3.sipua.ui.lowsdk;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivt.bluetooth.ibridge.BluetoothIBridgeDevice;
import com.zed3.addressbook.AbookOpenHelper;
import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.Member;
import com.zed3.audio.AudioUtil;
import com.zed3.bluetooth.BluetoothSCOStateReceiver;
import com.zed3.bluetooth.OnBluetoothAdapterStateChangedListener;
import com.zed3.bluetooth.OnSppConnectStateChangedListener;
import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.constant.Contants;
import com.zed3.constant.GroupConstant;
import com.zed3.constant.MessageConstant;
import com.zed3.customgroup.CustomGroupManager;
import com.zed3.customgroup.CustomGroupUtil;
import com.zed3.dialog.DialogUtil;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.log.MyLog;
import com.zed3.media.mediaButton.MediaButtonReceiver;
import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.CallManager;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrp.E_Grp_State;
import com.zed3.sipua.PttGrps;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.baiduMap.JsLocationOverlay;
import com.zed3.sipua.baiduMap.LocationOverlayDemo;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.BaseVisualizerView;
import com.zed3.utils.DensityUtil;
import com.zed3.utils.LineUpdateListener;
import com.zed3.utils.LoadingAnimation;
import com.zed3.utils.LogUtil;
import com.zed3.utils.MyHandler;
import com.zed3.utils.ReceiveLineListener;
import com.zed3.utils.Tools;

import org.zoolu.tools.GroupListInfo;

import java.sql.Date;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class TalkBackNew extends BaseActivity implements OnClickListener, Comparator<GroupListInfo>, LineUpdateListener, ReceiveLineListener, OnSppConnectStateChangedListener, OnBluetoothAdapterStateChangedListener {
	public static final String ACTION_YAOBI_PROMPT = "com.zed3.flow.yaobi_prompt";
	private static ImageView group_button_ptt;
	public static boolean isPttPressing;
	public static boolean isResume;
	public static LineUpdateListener lineListener;
	static boolean mHasPttGrp;
	public static boolean mIsBTServiceStarted;
	public static boolean mIsCreate;
	public static TalkBackNew mtContext;
	private static ReceiveLineListener receiveListener;
	public final String ACTION_3GFlow_ALARM = "com.zed3.flow.3gflow_alarm";
	private String TAG = "TalkBackNew";
	ArrayList<GroupListInfo> arrayList;
	private TextView bluetoothModeOnoffBt;
	private TextView bluetoothOnoffBt;
	private Collator collator = Collator.getInstance(Locale.CHINA);
	Handler dismissDialogHandler = new C12783();
	private int groupBodyMumber;
	private BroadcastReceiver groupListReceiver = new C12805();
	private String groupOnlineBodyMumber;
	private ListView group_member_list;
	private ListView group_name_list;
	private TextView group_name_title;
	private View group_set_button;
	private TextView hookModeOnoffBt;
	private IntentFilter intentfilter2;
	boolean isChangeMemaber = false;
	private boolean isGroupChange = true;
	private Boolean isNeedMenberList = Boolean.valueOf(true);
	private boolean isRequestCustomGroupInfo = false;
	public boolean isStarted;
	private E_Grp_State lastPttGrpState = E_Grp_State.GRP_STATE_SHOUDOWN;
	private LinearLayout linear_group_name;
	BaseVisualizerView mBaseVisualizerView;
	private IntentFilter mFilter;
	private HashMap<PttGrp, ArrayList<GroupListInfo>> mGroupListsMap;
	private MyGroupMemberAdapter mGroupMemberAdapter;
	private MyGroupNameAdapter mGroupNameAdapter;
	private Handler mHandler = new C12772();
	private long mLastGetGropMemberMessagesTime;
	private PttGrp mLastPttGrp = null;
	E_Grp_State mLastPttGrpState = null;
	private LoadingAnimation mLoadingAnimation;
	private boolean mPttGroupChangedReceiverRegistered;
	private BroadcastReceiver mReceiver = new C12794();
	private View mRootView;
	private ProgressDialog mSppConnectProcessDialog;
	private String mStatus = "";
	public Handler myHandler = new C12849();
	private SharedPreferences mypre = null;
	private ImageView new_down_up;
	private ImageView new_down_up_popup;
	private TextView new_member_text;
	private LinearLayout new_music;
	private View new_open_close;
	Handler noGroupToastHandler = new C12816();
	private View popuLayout;
	private PopupWindow popuWindow;
	private PopupWindow popupWindow;
	PttGrps pttGrps = null;
	public Handler pttPressHandler = new C12838();
	private int pttkeycode = 0;
	private Handler setCurrentGroupHandler = new C12761();
	private Handler sortHandler = new C12827();
	private TextView speakerModeOnoffBt;
	private Timer timer1;
	private TextView tv_group_speaker;
	private TextView tv_group_status;

	class C12761 extends Handler {
		C12761() {
		}

		public void handleMessage(Message msg) {
			PttGrp grpByIndex = (PttGrp) msg.obj;
			UserAgent ua = Receiver.GetCurUA();
			if (ua != null) {
				PttGrp curGrp = ua.GetCurGrp();
				if (curGrp != null && grpByIndex != null && !curGrp.equals(grpByIndex)) {
					ua.SetCurGrp(ua.getGrpById(grpByIndex.getGrpID()), true);
				}
			}
		}
	}

	class C12772 extends Handler {
		C12772() {
		}

		public void handleMessage(Message msg) {
			StringBuilder builder = new StringBuilder("mHandler#handleMessage() what " + msg.what);
			super.handleMessage(msg);
			if (NetChecker.check(TalkBackNew.mtContext, false)) {
				switch (msg.what) {
					case 1:
//                        long needDelay = C1972e.kh;
						if (msg.arg1 != 0) {
//                            needDelay = (long) msg.arg1;
						}
//                        builder.append(" needDelay " + needDelay);
						long time = System.currentTimeMillis() - TalkBackNew.this.mLastGetGropMemberMessagesTime;
						builder.append(" mLastGetGropMemberMessagesTime is " + TalkBackNew.this.mLastGetGropMemberMessagesTime + " time " + time);
//                        time = needDelay - time;
						if (time <= 0) {
							TalkBackNew.this.getCurrentGrpMemberMessages();
							break;
						}
						sendEmptyMessageDelayed(1, time);
						builder.append(" send delay message to getCurrentGrpMemberMessages delay " + time);
						break;
					case 2:
						TalkBackNew.this.pttGrps = TalkBackNew.this.getGrps();
						if (TalkBackNew.this.pttGrps != null && TalkBackNew.this.pttGrps.GetCount() == 0) {
							TalkBackNew.this.group_name_title.setText(R.string.ptt);
							TalkBackNew.this.tv_group_status.setText(new StringBuilder(String.valueOf(TalkBackNew.this.mStatus)).append(TalkBackNew.this.getResources().getString(R.string.status_none)).toString());
							TalkBackNew.this.tv_group_speaker.setText(R.string.talking_none);
//                            TalkBackNew.this.mLastPttGrp = null;
							TalkBackNew.this.updateOnlineGroups(TalkBackNew.this.mLastPttGrp);
							if (TalkBackNew.this.mGroupMemberAdapter != null) {
								TalkBackNew.this.mGroupMemberAdapter.refreshList(null);
								TalkBackNew.this.mGroupMemberAdapter.notifyDataSetChanged();
							}
						}
						if (TalkBackNew.this.mGroupNameAdapter != null) {
							TalkBackNew.this.mGroupNameAdapter.refreshNameList(TalkBackNew.this.pttGrps);
							TalkBackNew.this.mGroupNameAdapter.notifyDataSetChanged();
							builder.append(" mGroupNameAdapter.notifyDataSetChanged()");
							break;
						}
						break;
					case 3:
						if (TalkBackNew.this.mBaseVisualizerView != null) {
							TalkBackNew.this.mBaseVisualizerView.setTimes(-1);
							break;
						}
						break;
				}
				LogUtil.makeLog(TalkBackNew.this.TAG, builder.toString());
				return;
			}
			builder.append(" bad network ignore");
			LogUtil.makeLog(TalkBackNew.this.TAG, builder.toString());
		}
	}

	class C12783 extends Handler {
		C12783() {
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 2:
					TalkBackNew.this.dismissMyDialog(TalkBackNew.this.mSppConnectProcessDialog);
//                    TalkBackNew.this.mSppConnectProcessDialog = null;
					return;
				default:
					return;
			}
		}
	}

	class C12794 extends BroadcastReceiver {
		StringBuilder builder = new StringBuilder();

		C12794() {
		}

		public void onReceive(Context mtContext, Intent intent) {
			if (this.builder.length() > 0) {
				this.builder.delete(0, this.builder.length());
			}
			this.builder.append("mReceiver#onReceive()");
			Bundle extras = intent.getExtras();
			if (intent.getAction().equalsIgnoreCase(GroupConstant.ACTION_GROUP_STATUS)) {
				TalkBackNew.this.stopCurrentAnimation();
				this.builder.append(" ACTION_GROUP_STATUS");
				Bundle bundle = intent.getExtras();
				String speaker = bundle.getString("1") != null ? bundle.getString("1").trim() : null;
				String userNum = null;
				if (speaker != null) {
					String[] arr = speaker.split(" ");
					if (arr.length == 1) {
						userNum = arr[0];
					} else {
						userNum = arr[0];
						speaker = arr[1];
					}
				}
				this.builder.append(" speaker[" + userNum + "," + speaker + "]");
				PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
//                TalkBackNew.this.isGroupChange = TalkBackNew.this.getGroupChangeState();
//                TalkBackNew.this.pttGrps = TalkBackNew.this.getGrps();
				if (TalkBackNew.this.pttGrps == null || TalkBackNew.this.pttGrps.GetCount() <= 0) {
					TalkBackNew.mHasPttGrp = false;
				} else {
					TalkBackNew.mHasPttGrp = pttGrp != null;
				}
				if (pttGrp != null) {
					MyLog.i("tetetete", "setnew " + TalkBackNew.this.lastPttGrpState.name());
					this.builder.append(" " + pttGrp.toString());
					if (TalkBackNew.this.isGroupChange) {
						LogUtil.makeLog(TalkBackNew.this.TAG, " isGroupChange is true");
						TalkBackNew.this.onCurrentGrpChanged();
						if (!TalkBackNew.this.isNeedMenberList.booleanValue()) {
							TalkBackNew.this.onCurrentGrpMemberMessagesChanged();
						}
					}
					if (pttGrp.state == E_Grp_State.GRP_STATE_IDLE || pttGrp.state == E_Grp_State.GRP_STATE_SHOUDOWN) {
						userNum = "";
						speaker = "";
					}
					if (pttGrp.state == TalkBackNew.this.mLastPttGrpState && pttGrp.state == E_Grp_State.GRP_STATE_IDLE && !TalkBackNew.this.isGroupChange) {
						this.builder.append(" GRP_STATE_IDLE pttGrp.state == mLastPttGrpState ignore");
						LogUtil.makeLog(TalkBackNew.this.TAG, this.builder.toString());
						return;
					}
					if (TalkBackNew.this.mGroupNameAdapter.isCurGrpNull) {
						this.builder.append(" MyGroupNameAdapter#isCurGrpNull is true");
						Message msg = new Message();
						msg.what = 2;
						TalkBackNew.this.mHandler.sendMessage(msg);
					}
					TalkBackNew.this.tv_group_status.setText(new StringBuilder(String.valueOf(TalkBackNew.this.mStatus)).append(TalkBackNew.this.getResources().getString(R.string.status_none)).toString());
					TalkBackNew.this.tv_group_speaker.setText(R.string.talking_none);
					TalkBackNew.this.tv_group_speaker.setText(TalkBackNew.this.ShowSpeakerStatus(speaker, userNum));
					if (pttGrp.state == E_Grp_State.GRP_STATE_INITIATING) {
						TalkBackNew.this.tv_group_status.setText(new StringBuilder(String.valueOf(TalkBackNew.this.mStatus)).append(TalkBackNew.this.ShowPttStatus(pttGrp.state)).toString());
						TalkBackNew.this.stopCurrentAnimation();
//                        TalkBackNew.this.mLoadingAnimation = new LoadingAnimation();
						TalkBackNew.this.mLoadingAnimation.setAppendCount(3).startAnimation(TalkBackNew.this.tv_group_status);
					} else {
						TalkBackNew.this.stopCurrentAnimation();
						TalkBackNew.this.tv_group_status.setText(new StringBuilder(String.valueOf(TalkBackNew.this.mStatus)).append(TalkBackNew.this.ShowPttStatus(pttGrp.state)).toString());
					}
					if (!((pttGrp.state != E_Grp_State.GRP_STATE_SHOUDOWN && pttGrp.state != E_Grp_State.GRP_STATE_IDLE) || TalkBackNew.this.lastPttGrpState == E_Grp_State.GRP_STATE_SHOUDOWN || TalkBackNew.this.lastPttGrpState == E_Grp_State.GRP_STATE_IDLE || TalkBackNew.this.mHandler == null)) {
						TalkBackNew.this.mHandler.sendEmptyMessageDelayed(3, 500);
					}
//                    TalkBackNew.this.lastPttGrpState = pttGrp.state;
				} else {
//                    TalkBackNew.this.lastPttGrpState = E_Grp_State.GRP_STATE_SHOUDOWN;
					TalkBackNew.this.mBaseVisualizerView.setTimes(-1);
					TalkBackNew.this.tv_group_status.setText(new StringBuilder(String.valueOf(TalkBackNew.this.mStatus)).append(TalkBackNew.this.getResources().getString(R.string.status_none)).toString());
					TalkBackNew.this.tv_group_speaker.setText(R.string.talking_none);
				}
				if (TalkBackNew.this.mLastPttGrp != null) {
					TalkBackNew.this.mLastPttGrpState = TalkBackNew.this.mLastPttGrp.state;
				}
			} else if (intent.getAction().equalsIgnoreCase(GroupConstant.ACTION_GROUP_2_GROUP)) {
				this.builder.append(" ACTION_GROUP_2_GROUP");
				GroupCallUtil.setTalkGrp(intent.getExtras().getString("0"));
				GroupCallUtil.setActionMode(GroupConstant.ACTION_GROUP_2_GROUP);
//                startActivity = new Intent();
//                startActivity.setClass(Receiver.mContext, ActvityNotify.class);
//                startActivity.setFlags(DriveFile.MODE_READ_ONLY);
//                SipUAApp.getAppContext().startActivity(startActivity);
			} else if (intent.getAction().equalsIgnoreCase(GroupConstant.ACTION_SINGLE_2_GROUP)) {
				this.builder.append(" ACTION_SINGLE_2_GROUP");
				GroupCallUtil.setTalkGrp(intent.getExtras().getString("0"));
				GroupCallUtil.setActionMode(GroupConstant.ACTION_SINGLE_2_GROUP);
				if (UserAgent.isCamerPttDialog || UserAgent.isTempGrpCallMode) {
					TalkBackNew.this.sendBroadcast(new Intent("com.zed3.sipua.camera_ptt_dialog"));
				} else {
//                    startActivity = new Intent();
//                    startActivity.setClass(Receiver.mContext, ActvityNotify.class);
//                    startActivity.setFlags(DriveFile.MODE_READ_ONLY);
//                    SipUAApp.getAppContext().startActivity(startActivity);
				}
			} else if (intent.getAction().equalsIgnoreCase(MessageConstant.ACTION_SEND_TEXT_MESSAGE_FAIL)) {
				this.builder.append(" ACTION_SEND_TEXT_MESSAGE_FAIL");
//                mE_id = intent.getStringExtra("0");
//                broadCast = new Intent(MessageDialogueActivity.SEND_TEXT_FAIL);
//                broadCast.putExtra("0", mE_id);
//                mtContext.sendBroadcast(broadCast);
			} else if (intent.getAction().equalsIgnoreCase(MessageConstant.ACTION_SEND_TEXT_MESSAGE_SUCCEED)) {
				this.builder.append(" ACTION_SEND_TEXT_MESSAGE_SUCCEED");
//                mE_id = intent.getStringExtra("0");
//                broadCast = new Intent(MessageDialogueActivity.SEND_TEXT_SUCCEED);
//                broadCast.putExtra("0", mE_id);
//                mtContext.sendBroadcast(broadCast);
			} else if (intent.getAction().equalsIgnoreCase(MessageConstant.ACTION_SEND_TEXT_MESSAGE_TIMEOUT)) {
				this.builder.append(" ACTION_SEND_TEXT_MESSAGE_TIMEOUT");
//                mE_id = intent.getStringExtra("E_id");
//                broadCast = new Intent(MessageDialogueActivity.SEND_TEXT_TIMEOUT);
//                broadCast.putExtra("0", mE_id);
//                mtContext.sendBroadcast(broadCast);
			} else if (intent.getAction().equalsIgnoreCase(Contants.ACTION_NEWWORK_CHANGED)) {
				this.builder.append(" ACTION_NEWWORK_CHANGED");
				LinearLayout ll = (LinearLayout) TalkBackNew.this.findViewById(R.id.net_tip2);
				if (intent.getIntExtra(Contants.NETWORK_STATE, -1) == 1) {
//                    ll.setVisibility(View.GONE);
					TalkBackNew.this.pttGrps = TalkBackNew.this.getGrps();
					if (TalkBackNew.this.mGroupNameAdapter != null) {
						TalkBackNew.this.mGroupNameAdapter.refreshNameList(TalkBackNew.this.pttGrps);
						TalkBackNew.this.mGroupNameAdapter.notifyDataSetChanged();
						this.builder.append(" mGroupNameAdapter.notifyDataSetChanged()");
					}
				} else {
//                    ll.setVisibility(View.VISIBLE);
				}
			} else if (intent.getAction().equalsIgnoreCase(Contants.ACTION_CURRENT_GROUP_CHANGED)) {
				this.builder.append(" ACTION_CURRENT_GROUP_CHANGED");
				LogUtil.makeLog(TalkBackNew.this.TAG, " ACTION_CURRENT_GROUP_CHANGED onCurrentGrpMemberMessagesChanged()");
			} else if (intent.getAction().equalsIgnoreCase("com.zed3.flow.3gflow_alarm")) {
				this.builder.append(" ACTION_3GFlow_ALARM");
				Tools.FlowAlertDialog(TalkBackNew.this);
			} else if (intent.getAction().equalsIgnoreCase(GroupConstant.ACTION_ALL_GROUP_CHANGE)) {
				this.builder.append(" ACTION_ALL_GROUP_CHANGE");
				LogUtil.makeLog(TalkBackNew.this.TAG, " ACTION_ALL_GROUP_CHANGE onCurrentGrpMemberMessagesChanged()");
				GroupListUtil.getData4GroupList();
				if (TalkBackNew.this.mHandler != null) {
					TalkBackNew.this.mHandler.sendEmptyMessage(2);
				}
			} else if (intent.getAction().equals(AudioUtil.ACTION_STREAM_CHANGED)) {
				switch (extras.getInt(AudioUtil.KEY_STREAM_INT)) {
					case 0:
						TalkBackNew.this.setVolumeControlStream(0);
						break;
					case 3:
						TalkBackNew.this.setVolumeControlStream(3);
						break;
					default:
						break;
				}
			} else if (intent.getAction().equals(TalkBackNew.ACTION_YAOBI_PROMPT)) {
				MyToast.showToast(true, mtContext, TalkBackNew.this.getString(R.string.call_failed));
			}
			LogUtil.makeLog(TalkBackNew.this.TAG, this.builder.toString());
		}
	}

	class C12805 extends BroadcastReceiver {
		C12805() {
		}

		public void onReceive(Context mtContext, Intent intent) {
			String action = intent.getAction();
			if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_PTT_GROUP_INFO) || action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_INFO_CHANGED) || action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO)) {
				TalkBackNew.this.refreshAdapter();
			}
			if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO)) {
				LogUtil.makeLog(TalkBackNew.this.TAG, action);
				if (TalkBackNew.this.isRequestCustomGroupInfo) {
					TalkBackNew.this.onCurrentGrpMemberMessagesChanged();
				} else {
					TalkBackNew.this.updateOnlineGroups(TalkBackNew.this.mLastPttGrp);
					TalkBackNew.this.mHandler.sendMessage(TalkBackNew.this.mHandler.obtainMessage(2));
				}
			}
			if (action.equals("com.zed3.sipua.ui_groupcall.clear_grouplist")) {
				GroupListUtil.removeDataOfGroupList();
				LogUtil.makeLog(TalkBackNew.this.TAG, " ShowCurrentGrp() groupListReceiver ACTION_CLEAR_GROUPLIST +1");
			}
			if (TalkBackNew.this.isStarted && action.equals("com.zed3.sipua_grouplist_update_over")) {
				LogUtil.makeLog(TalkBackNew.this.TAG, "ACTION_GROUPLIST_UPDATE_OVER  onCurrentGrpMemberMessagesChanged()");
				TalkBackNew.this.onCurrentGrpMemberMessagesChanged();
			}
		}
	}

	class C12816 extends Handler {
		C12816() {
		}

		public void handleMessage(Message msg) {
		}
	}

	class C12827 extends Handler {
		C12827() {
		}

		public void handleMessage(Message msg) {
			if (msg.what == 1) {
//                TalkBackNew.this.updateMemberList(msg.obj);
			}
		}
	}

	class C12838 extends Handler {
		C12838() {
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					MyLog.e(TalkBackNew.this.TAG, "pttPressHandler setPttBackground isPttPressing = false;");
					TalkBackNew.isPttPressing = false;
					TalkBackNew.setPttBackground(false);
					return;
				case 1:
					MyLog.e(TalkBackNew.this.TAG, "pttPressHandler setPttBackground isPttPressing = true;");
					TalkBackNew.isPttPressing = true;
					TalkBackNew.setPttBackground(true);
					return;
				default:
					return;
			}
		}
	}

	class C12849 extends Handler {
		C12849() {
		}

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1:
					int volume = msg.arg1;
					if (TalkBackNew.lineListener == null) {
						return;
					}
					if (TalkBackNew.isPttPressing || GroupCallUtil.mIsPttDown) {
						TalkBackNew.lineListener.showCurrentVolume(volume);
						return;
					}
					return;
				case 2:
					int volume1 = msg.arg1;
					if (TalkBackNew.receiveListener != null) {
						TalkBackNew.receiveListener.showCurrentReceiveVolume(volume1);
						return;
					}
					return;
				default:
					return;
			}
		}
	}

	public TalkBackNew() {
	}

	public static boolean checkHasCurrentGrp(final Context context) {
		return TalkBackNew.mHasPttGrp;
	}

	private int findPos(final List<GroupListInfo> list, final GroupListInfo groupListInfo) {
		for (int i = 0; i < list.size(); ++i) {
			final int size = i;
			if (this.compare(list.get(i), groupListInfo) > 0) {
				return size;
			}
		}
		return list.size();
	}

	private boolean getGroupChangeState() {
		final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
		if (getCurGrp != null && this.mLastPttGrp != getCurGrp) {
			if (this.mLastPttGrp == null || !this.mLastPttGrp.grpID.equalsIgnoreCase(getCurGrp.grpID)) {
				this.mLastPttGrp = getCurGrp;
				return true;
			}
			this.mLastPttGrp = getCurGrp;
		}
		return false;
	}

	private PttGrp getGrpById(final String s) {
		if (TextUtils.isEmpty((CharSequence) s)) {
			return null;
		}
		if (this.pttGrps != null) {
			for (final PttGrp pttGrp : this.pttGrps.getPttGrps()) {
				if (s.equalsIgnoreCase(pttGrp.grpID)) {
					return pttGrp;
				}
			}
		}
		return null;
	}

	private PttGrp getGrpFromMapListById(final String s) {
		if (TextUtils.isEmpty((CharSequence) s) || this.mGroupListsMap == null) {
			return null;
		}
		for (final PttGrp pttGrp : this.mGroupListsMap.keySet()) {
			if (s.equalsIgnoreCase(pttGrp.grpID)) {
				return pttGrp;
			}
		}
		return null;
	}

	private PttGrps getGrps() {
		final UserAgent getCurUA = Receiver.GetCurUA();
		if (getCurUA != null) {
			this.pttGrps = getCurUA.GetAllGrps();
		}
		return this.pttGrps.copyGrps();
	}

	public static TalkBackNew getInstance() {
		return TalkBackNew.mtContext;
	}

	private int getServerListArray() {
		if (DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList;
		}
		if (!DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList1;
		}
		if (DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && !DeviceInfo.CONFIG_SUPPORT_IM) {
			return R.array.msgDialogList2;
		}
		return -1;
	}

	private void initMusicLine() {
		(this.mBaseVisualizerView = new BaseVisualizerView((Context) this)).setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
		this.new_music.addView((View) this.mBaseVisualizerView);
	}

	private boolean isVisibleContactVideo(final String s, final String s2) {
		return (s != null && Member.UserType.toUserType(s) == Member.UserType.MOBILE_GQT && (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE || DeviceInfo.CONFIG_VIDEO_UPLOAD == 1 || DeviceInfo.CONFIG_VIDEO_MONITOR == 1) && s2 != null && s2.equalsIgnoreCase("1")) || (s != null && Member.UserType.toUserType(s) == Member.UserType.SVP && (DeviceInfo.CONFIG_SUPPORT_VIDEO_SINGLE || DeviceInfo.CONFIG_VIDEO_UPLOAD == 1) && s2 != null && s2.equalsIgnoreCase("1"));
	}

	private void refreshAdapter() {
		LogUtil.makeLog(this.TAG, "refreshAdapter()");
		final UserAgent getCurUA = Receiver.GetCurUA();
		if (getCurUA != null) {
			if (!UserAgent.isTempGrpCallMode) {
				this.pttGrps = this.getGrps();
				final PttGrp getCurGrp = getCurUA.GetCurGrp();
				final Vector<PttGrp> pttGrps = this.pttGrps.getPttGrps();
				if (pttGrps != null && pttGrps.size() > 0) {
					if (getCurGrp != null && this.getGrpById(getCurGrp.getGrpID()) != null) {
						this.mLastPttGrp = getCurGrp;
					} else {
						this.mLastPttGrp = this.pttGrps.FirstGrp();
						getCurUA.SetCurGrp(getCurUA.getGrpById(this.mLastPttGrp.getGrpID()), true);
					}
				} else {
					getCurUA.SetCurGrp(this.mLastPttGrp = null, true);
					this.arrayList = null;
					TalkBackNew.mHasPttGrp = false;
				}
			}
			this.mHandler.sendEmptyMessage(2);
			this.updateOnlineGroups(this.mLastPttGrp);
			if (this.isChangeMemaber || this.mLastPttGrp == null) {
				this.mGroupMemberAdapter.refreshList(this.arrayList);
				this.mGroupMemberAdapter.notifyDataSetChanged();
			}
		}
	}

	private void registerPttGroupChangedReceiver() {
		if (!this.mPttGroupChangedReceiverRegistered) {
			(this.mFilter = new IntentFilter()).addAction("com.zed3.sipua_network_changed");
			this.mFilter.addAction("com.zed3.sipua.ui_groupcall.group_status");
			this.mFilter.addAction("com.zed3.sipua.ui_groupcall.all_groups_change");
			this.mFilter.addAction("com.zed3.sipua.ui_groupcall.single_2_group");
			this.mFilter.addAction("com.zed3.sipua.ui_send_text_message_fail");
			this.mFilter.addAction("com.zed3.sipua.ui_send_text_message_timeout");
			this.mFilter.addAction("com.zed3.sipua_currentgroup_changed");
			this.mFilter.addAction("com.zed3.flow.3gflow_alarm");
			this.mFilter.addAction("stream changed");
			this.mFilter.addAction("com.zed3.flow.yaobi_prompt");
			TalkBackNew.mtContext.registerReceiver(this.mReceiver, this.mFilter);
			this.mPttGroupChangedReceiverRegistered = true;
		}
	}

	private void resetGroupNameTitle(PttGrp pttGrp) {
		if (this.getGrps().GetCount() == 0) {
			pttGrp = null;
		}
		if (pttGrp != null) {
			this.group_name_title.setText((CharSequence) pttGrp.grpName);
			return;
		}
		this.group_name_title.setText(R.string.ptt);
	}

	public static void setPttBackground(final boolean b) {
		if (TalkBackNew.group_button_ptt != null) {
			final ImageView group_button_ptt = TalkBackNew.group_button_ptt;
			int imageResource;
			if (b) {
				imageResource = R.drawable.group_list_ptt_down;
			} else {
				imageResource = R.drawable.group_list_ptt_up;
			}
			group_button_ptt.setImageResource(imageResource);
		}
	}

	private void showPopuWindow(View view) {
		if (this.popuWindow == null) {
			this.popuLayout = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.aa_new_popu_layout, (ViewGroup) null);
			(this.group_name_list = (ListView) this.popuLayout.findViewById(R.id.new_group_name_list)).setVerticalScrollBarEnabled(false);
			this.group_name_list.setAdapter((ListAdapter) this.mGroupNameAdapter);
			view = this.mGroupNameAdapter.getView(0, null, (ViewGroup) this.group_name_list);
			view.measure(0, 0);
			final int height = (view.getMeasuredHeight() + this.group_name_list.getDividerHeight()) * 5;
			this.group_name_list.getLayoutParams().height = height;
			this.mGroupNameAdapter.notifyDataSetChanged();
			final int n = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight() / 3;
			this.popuWindow = new PopupWindow(this.popuLayout, DensityUtil.dip2px((Context) this, 250.0f), DensityUtil.dip2px((Context) this, height));
		}
		this.popuWindow.setFocusable(true);
		this.popuWindow.setBackgroundDrawable((Drawable) new BitmapDrawable());
		final RelativeLayout relativeLayout = (RelativeLayout) this.findViewById(R.id.relativrelayout);
		this.popuWindow.showAsDropDown((View) relativeLayout, relativeLayout.getWidth() / 2 - this.popuWindow.getWidth() / 2, 0);
		this.popuWindow.setOnDismissListener((PopupWindow.OnDismissListener) new PopupWindow.OnDismissListener() {
			public void onDismiss() {
				TalkBackNew.this.new_down_up_popup.setImageDrawable(TalkBackNew.this.getResources().getDrawable(R.drawable.new_down1));
			}
		});
		this.group_name_list.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				if (!NetChecker.check((Context) TalkBackNew.mtContext, true)) {
					MyToast.showToast(true, (Context) TalkBackNew.mtContext, R.string.network_exception);
				} else {
					if (TalkBackNew.this.popuWindow != null) {
						TalkBackNew.this.popuWindow.dismiss();
					}
					final PttGrp getGrpByIndex = TalkBackNew.this.pttGrps.GetGrpByIndex(n);
					final UserAgent getCurUA = Receiver.GetCurUA();
					if (getCurUA != null) {
						final PttGrp getCurGrp = getCurUA.GetCurGrp();
						if (getCurGrp != null && getGrpByIndex != null && !getCurGrp.equals(getGrpByIndex)) {
							if (TalkBackNew.isPttPressing) {
								MyToast.showToast(true, (Context) TalkBackNew.mtContext, R.string.release_ptt_and_try_again);
								return;
							}
//                            TalkBackNew.access.17(TalkBackNew.this, true);
							TalkBackNew.this.setCurrentGroupHandler.removeMessages(0);
							final Message obtainMessage = TalkBackNew.this.setCurrentGroupHandler.obtainMessage();
							obtainMessage.obj = getGrpByIndex;
							obtainMessage.what = 0;
							TalkBackNew.this.setCurrentGroupHandler.sendMessageDelayed(obtainMessage, 200L);
						}
					}
				}
			}
		});
	}

	private int sort(final ArrayList<GroupListInfo> list) {
		return 0;
	}

	private ArrayList<GroupListInfo> sortArrayList(final ArrayList<GroupListInfo> list) {
		ArrayList<GroupListInfo> list2 = new ArrayList<GroupListInfo>();
		if (list != null) {
			if (list.size() > 1) {
				list2 = (ArrayList<GroupListInfo>) (ArrayList) this.sequence(list);
			} else {
				list2 = list2;
				if (list.size() == 1) {
					return list;
				}
			}
		}
		return list2;
	}

	private ArrayList<GroupListInfo> sortOnline(final ArrayList<GroupListInfo> list) {
		ArrayList<GroupListInfo> list3;
		final ArrayList<GroupListInfo> list2 = list3 = new ArrayList<GroupListInfo>();
		if (list != null) {
			if (list.size() > 1) {
				list3 = new ArrayList<GroupListInfo>();
				final ArrayList<GroupListInfo> list4 = new ArrayList<GroupListInfo>();
				for (int i = 0; i < list.size(); ++i) {
					if (list.get(i).GrpState.equals("0")) {
						list4.add(list.get(i));
					} else {
						list3.add(list.get(i));
					}
				}
				list3.addAll(list4);
			} else {
				list3 = list2;
				if (list.size() == 1) {
					return list;
				}
			}
		}
		return list3;
	}

	private void stopCurrentAnimation() {
		if (this.mLoadingAnimation != null) {
			this.mLoadingAnimation.stopAnimation();
		}
	}

	private void updateGroupLists() {
		final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
		if (getCurGrp != null) {
			this.updateOnlineGroups(getCurGrp);
		}
	}

	private void updateMemberList(final ArrayList<GroupListInfo> list) {
		synchronized (this) {
			if (this.mGroupMemberAdapter != null) {
				this.mGroupMemberAdapter.refreshList(list);
				this.mGroupMemberAdapter.notifyDataSetChanged();
			}
		}
	}

	private void updateOnlineGroups(final PttGrp pttGrp) {
		// TODO
	}

	public String ShowPttStatus(final PttGrp.E_Grp_State e_Grp_State) {
//        switch (e_Grp_State) {
//            default: {
//                return this.getResources().getString(R.string.error);
//            }
//            case 1: {
//                return this.getResources().getString(R.string.close);
//            }
//            case 2: {
//                return this.getResources().getString(R.string.idle);
//            }
//            case 4: {
//                return this.getResources().getString(R.string.talking);
//            }
//            case 5: {
//                return this.getResources().getString(R.string.listening);
//            }
//            case 6: {
//                return this.getResources().getString(R.string.queueing);
//            }
//            case 3: {
//                return this.getResources().getString(R.string.ptt_requesting);
//            }
//        }
		return "";
	}

	public String ShowSpeakerStatus(final String s, final String s2) {
		if (TextUtils.isEmpty((CharSequence) s)) {
			return this.getResources().getString(R.string.talking_none);
		}
		if (s2.equals(Settings.getUserName())) {
			return this.getResources().getString(R.string.talking_me);
		}
		return String.valueOf(this.getResources().getString(R.string.talking_someOne)) + "\uff08" + s + "\uff09";
	}

	String addReturn(String substring) {
		String string = "";
		for (int i = 0; i < substring.length(); ++i) {
			string = String.valueOf(string) + substring.charAt(i) + "\n";
		}
		substring = string;
		if (string.length() > 1) {
			substring = string.substring(0, string.length() - 1);
		}
		return substring;
	}

	public int compare(final GroupListInfo groupListInfo, final GroupListInfo groupListInfo2) {
		int n = 0;
		if (this.collator.compare(groupListInfo.GrpName, groupListInfo2.GrpName) < 0) {
			n = -1;
		} else if (this.collator.compare(groupListInfo.GrpName, groupListInfo2.GrpName) > 0) {
			return 1;
		}
		return n;
	}

	protected void dismissMyDialog(final ProgressDialog progressDialog) {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}

	String formatGroupName(final String s, final int n) {
		String string = s;
		if (s != null) {
			if (n == 1) {
				if (s.length() <= 12) {
					return this.addReturn(s);
				}
				string = String.valueOf(this.addReturn(s.substring(0, 12))) + "\n...";
			} else if (n == 2) {
				if (s.length() > 6) {
					return String.valueOf(this.addReturn(s.substring(0, 6))) + "\n...";
				}
				return this.addReturn(s);
			} else {
				string = s;
				if (n > 2) {
					if (s.length() > 4) {
						return String.valueOf(this.addReturn(s.substring(0, 2))) + "\n...";
					}
					return this.addReturn(s);
				}
			}
		}
		return string;
	}

	protected void getCurrentGrpMemberMessages() {
		LogUtil.makeLog("TalkBackNew ", " getCurrentGrpMemberMessages()");
		this.mLastGetGropMemberMessagesTime = System.currentTimeMillis();
		this.isGroupChange = true;
		if (this.mLastPttGrp != null) {
			if (this.mLastPttGrp.getType() != 0) {
				this.isRequestCustomGroupInfo = true;
				CustomGroupUtil.getInstance().getCurrentCustomGroupMemberInfo(this.mLastPttGrp.getGrpID());
				return;
			}
			GroupListUtil.getDataCurrentGroupList();
		}
	}

	public String getTime() {
		try {
			return new SimpleDateFormat(" HHmmss ").format(new Date(System.currentTimeMillis()));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.new_open_close: {
				if (!this.isNeedMenberList) {
					if (this.groupOnlineBodyMumber != null) {
						this.new_member_text.setText((CharSequence) ("(" + this.groupBodyMumber + ")"));
					}
					this.isChangeMemaber = false;
					if (this.timer1 != null) {
						this.timer1.cancel();
						this.timer1 = null;
					}
					this.isNeedMenberList = true;
					this.new_down_up.setImageDrawable(this.getResources().getDrawable(R.drawable.new_down));
					this.group_member_list.setVisibility(View.GONE);
					return;
				}
				this.new_member_text.setVisibility(View.VISIBLE);
				this.new_down_up.setImageDrawable(this.getResources().getDrawable(R.drawable.new_up));
				this.group_member_list.setVisibility(View.VISIBLE);
				this.isNeedMenberList = false;
				this.isChangeMemaber = true;
				this.onCurrentGrpMemberMessagesChanged();
				if (this.timer1 == null) {
					(this.timer1 = new Timer()).schedule(new TimerTask() {
						@Override
						public void run() {
							final Message message = new Message();
							message.what = 1;
							TalkBackNew.this.mHandler.sendMessage(message);
							LogUtil.makeLog(TalkBackNew.this.TAG, "GroupRefresh timer1 start");
						}
					}, 0L, 30000L);
				}
				if (this.groupOnlineBodyMumber != null) {
					this.new_member_text.setText((CharSequence) ("(" + this.groupOnlineBodyMumber + "/" + this.groupBodyMumber + ")"));
					return;
				}
				break;
			}
			case R.id.new_group_gps: {
				if (this.pttGrps == null || this.pttGrps.GetCount() == 0) {
					MyToast.showToast(true, (Context) TalkBackNew.mtContext, R.string.no_groups);
					return;
				}
				final Intent intent = null;
				Intent intent2 = null;
				MyLog.v("dd", "DeviceInfo.CONFIG_MAP_TYPE=" + DeviceInfo.CONFIG_MAP_TYPE);
				switch (DeviceInfo.CONFIG_MAP_TYPE) {
					case 0: {
						final int int1 = this.mypre.getInt("maptype", 0);
						Log.v("TalkBackNew", "\u5730\u56fe\u7c7b\u578b\u662f" + int1);
						intent2 = intent;
						if (int1 == 1) {
							intent2 = new Intent((Context) TalkBackNew.mtContext, (Class) JsLocationOverlay.class);
						}
						if (int1 == 0) {
							intent2 = new Intent((Context) TalkBackNew.mtContext, (Class) LocationOverlayDemo.class);
							break;
						}
						break;
					}
					case 1: {
//						intent2 = new Intent((Context) TalkBackNew.mtContext, (Class) GoogleLocationOverlay.class);
						break;
					}
				}
				if (intent2 != null) {
					this.startActivity(intent2);
					return;
				}
				break;
			}
			case R.id.bluetooth_onoff_bt: {
				if (!TalkBackNew.mIsBTServiceStarted) {
					this.reInitBluetoothButton(true);
					ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
					ZMBluetoothManager.getInstance().saveZMBluetoothOnOffState(true);
					return;
				}
				DialogUtil.showSelectDialog((Context) this, this.getResources().getString(R.string.disconnect_hm), this.getResources().getString(R.string.disconnect_hm_notify), this.getResources().getString(R.string.disconnect), (DialogUtil.DialogCallBack) new DialogUtil.DialogCallBack() {
					@Override
					public void onNegativeButtonClick() {
					}

					@Override
					public void onPositiveButtonClick() {
						ZMBluetoothManager.getInstance().disConnectZMBluetooth(SipUAApp.mContext);
						if (ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
							ZMBluetoothManager.getInstance().askUserToDisableBluetooth();
						}
						TalkBackNew.this.reInitBluetoothButton(false);
						ZMBluetoothManager.getInstance().mNeedAskUserToReconnectSpp = false;
						ZMBluetoothManager.getInstance().saveZMBluetoothOnOffState(false);
					}
				});
			}
			case R.id.linear_group_name: {
				if (Receiver.GetCurUA().GetCurGrp() != null) {
					this.showPopuWindow(view);
					this.new_down_up_popup.setImageDrawable(this.getResources().getDrawable(R.drawable.new_up1));
					return;
				}
				break;
			}
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		TalkBackNew.lineListener = this;
		TalkBackNew.mtContext = this;
		ZMBluetoothManager.getInstance().setSppConnectStateListener(this);
		BluetoothSCOStateReceiver.setOnBluetoothAdapterStateChangedListener(this);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(this.mRootView = this.getLayoutInflater().inflate(R.layout.aa_new, (ViewGroup) null));
		this.mStatus = this.getResources().getString(R.string.my_status);
		this.mRootView.setOnClickListener((OnClickListener) this);
		(this.group_member_list = (ListView) this.findViewById(R.id.new_group_member_list)).setVerticalScrollBarEnabled(true);
		(this.linear_group_name = (LinearLayout) this.findViewById(R.id.linear_group_name)).setOnClickListener((OnClickListener) this);
		this.tv_group_status = (TextView) this.findViewById(R.id.new_tv_group_status);
		this.tv_group_speaker = (TextView) this.findViewById(R.id.new_tv_group_speaker);
		this.group_set_button = this.findViewById(R.id.new_group_gps);
		if (DeviceInfo.CONFIG_SUPPORT_PTTMAP) {
			this.group_set_button.setVisibility(View.VISIBLE);
		} else {
			this.group_set_button.setVisibility(View.INVISIBLE);
		}
		this.group_set_button.setOnClickListener((OnClickListener) this);
		this.group_set_button.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final ImageView imageView = (ImageView) TalkBackNew.this.findViewById(R.id.t_add);
				switch (motionEvent.getAction()) {
					case 0: {
						imageView.setBackgroundResource(R.color.btn_click_bg);
						imageView.setImageResource(R.drawable.icon_location_press);
						break;
					}
					case 1: {
						imageView.setBackgroundResource(R.color.whole_bg);
						imageView.setImageResource(R.drawable.icon_loaction_release);
						break;
					}
				}
				return false;
			}
		});
		(this.new_open_close = this.findViewById(R.id.new_open_close)).setOnClickListener((OnClickListener) this);
		this.new_down_up = (ImageView) this.findViewById(R.id.new_down_up);
		this.new_down_up_popup = (ImageView) this.findViewById(R.id.new_down_up_popup);
		this.new_member_text = (TextView) this.findViewById(R.id.new_member_text);
		this.new_music = (LinearLayout) this.findViewById(R.id.new_music);
		this.initMusicLine();
		TalkBackNew.group_button_ptt = (ImageView) this.findViewById(R.id.new_group_button_ptt);
		this.group_name_title = (TextView) this.findViewById(R.id.new_group_name_title);
		TalkBackNew.group_button_ptt.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					case 0: {
						if (!NetChecker.check((Context) TalkBackNew.mtContext, true)) {
							break;
						}
						if (!TalkBackNew.checkHasCurrentGrp((Context) TalkBackNew.mtContext)) {
							MyToast.showToast(true, (Context) TalkBackNew.mtContext, R.string.no_groups);
							return true;
						}
						TalkBackNew.isPttPressing = true;
						MyLog.i("hst", "onTouch... down ");
						TalkBackNew.setPttBackground(true);
						GroupCallUtil.makeGroupCall(true, false, UserAgent.PttPRMode.ScreenPress);
						return true;
					}
					case 1: {
						TalkBackNew.setPttBackground(false);
						MyLog.i("guojunfeng20140222", "up1");
						if (NetChecker.check((Context) TalkBackNew.mtContext, true) && TalkBackNew.checkHasCurrentGrp((Context) TalkBackNew.mtContext)) {
							TalkBackNew.isPttPressing = false;
							MyLog.i("hst", "onTouch... up ");
							TalkBackNew.this.stopCurrentAnimation();
							final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
							if (getCurGrp != null && getCurGrp.state == E_Grp_State.GRP_STATE_INITIATING) {
								TalkBackNew.this.tv_group_status.setText((CharSequence) (String.valueOf(TalkBackNew.this.mStatus) + TalkBackNew.this.ShowPttStatus(E_Grp_State.GRP_STATE_IDLE)));
							}
							GroupCallUtil.makeGroupCall(false, false, UserAgent.PttPRMode.Idle);
							return true;
						}
						break;
					}
				}
				return true;
			}
		});
		PttGrp getCurGrp = null;
		if (this.getGrps().GetCount() > 0) {
			getCurGrp = Receiver.GetCurUA().GetCurGrp();
		}
		if ((this.mLastPttGrp = getCurGrp) != null) {
			this.group_name_title.setText((CharSequence) getCurGrp.grpName);
			this.tv_group_speaker.setText((CharSequence) this.ShowSpeakerStatus(getCurGrp.speaker, getCurGrp.speakerN));
			this.tv_group_status.setText((CharSequence) (String.valueOf(this.mStatus) + this.ShowPttStatus(getCurGrp.state)));
		} else {
			this.group_name_title.setText(R.string.ptt);
			this.tv_group_status.setText((CharSequence) (String.valueOf(this.mStatus) + this.getResources().getString(R.string.status_none)));
			this.tv_group_speaker.setText(R.string.talking_none);
		}
		this.resetGroupNameTitle(getCurGrp);
		this.pttGrps = this.getGrps();
		this.updateGroupLists();
		this.mGroupNameAdapter = new MyGroupNameAdapter((Context) TalkBackNew.mtContext, this.pttGrps);
		this.mGroupMemberAdapter = new MyGroupMemberAdapter((Context) TalkBackNew.mtContext, this.arrayList);
		this.group_member_list.setAdapter((ListAdapter) this.mGroupMemberAdapter);
		this.registerPttGroupChangedReceiver();
		if (this.intentfilter2 == null) {
			(this.intentfilter2 = new IntentFilter()).addAction("com.zed3.sipua_grouplist_update_over");
			this.intentfilter2.addAction("com.zed3.sipua.ui_groupcall.all_groups_clear_over");
			this.intentfilter2.addAction("com.zed3.sipua.ui_groupcall.clear_grouplist");
			this.intentfilter2.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_PTT_GROUP_INFO);
			this.intentfilter2.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_INFO_CHANGED);
			this.intentfilter2.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO);
			this.intentfilter2.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO);
		}
		TalkBackNew.mtContext.registerReceiver(this.groupListReceiver, this.intentfilter2);
		(this.bluetoothOnoffBt = (TextView) this.findViewById(R.id.bluetooth_onoff_bt)).setOnClickListener((OnClickListener) this);
		this.bluetoothOnoffBt.setOnLongClickListener((View.OnLongClickListener) new View.OnLongClickListener() {
			public boolean onLongClick(final View view) {
				TalkBackNew.this.startActivityForResult(new Intent("android.settings.BLUETOOTH_SETTINGS"), 4);
				TalkBackNew.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 3);
				return true;
			}
		});
		(this.bluetoothModeOnoffBt = (TextView) this.findViewById(R.id.mode_bluetooth_onoff_bt)).setOnClickListener((OnClickListener) this);
		this.bluetoothModeOnoffBt.setOnLongClickListener((View.OnLongClickListener) new View.OnLongClickListener() {
			public boolean onLongClick(final View view) {
				TalkBackNew.this.startActivityForResult(new Intent("android.settings.BLUETOOTH_SETTINGS"), 4);
				TalkBackNew.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 3);
				return true;
			}
		});
		this.bluetoothModeOnoffBt.setVisibility(View.GONE);
		(this.hookModeOnoffBt = (TextView) this.findViewById(R.id.mode_hook_onoff_bt)).setOnClickListener((OnClickListener) this);
		this.hookModeOnoffBt.setOnLongClickListener((View.OnLongClickListener) new View.OnLongClickListener() {
			public boolean onLongClick(final View view) {
				return true;
			}
		});
		(this.speakerModeOnoffBt = (TextView) this.findViewById(R.id.mode_speaker_onoff_bt)).setOnClickListener((OnClickListener) this);
		this.speakerModeOnoffBt.setOnLongClickListener((View.OnLongClickListener) new View.OnLongClickListener() {
			public boolean onLongClick(final View view) {
				return true;
			}
		});
		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null) {
			if (!ZMBluetoothManager.getInstance().isDeviceSupportBluetooth()) {
				DialogUtil.showCheckDialog((Context) this, this.getResources().getString(R.string.information), this.getResources().getString(R.string.bluetooth_notify), this.getResources().getString(R.string.ok_know));
				Settings.mNeedBlueTooth = false;
				this.bluetoothOnoffBt.setVisibility(View.GONE);
			}
			final boolean zmBluetoothOnOffState = ZMBluetoothManager.getInstance().getZMBluetoothOnOffState((Context) TalkBackNew.mtContext);
			this.reInitBluetoothButton(zmBluetoothOnOffState);
			if (zmBluetoothOnOffState) {
				ZMBluetoothManager.getInstance().connectZMBluetooth(this.getApplicationContext());
			}
		}
		this.pttGrps = this.getGrps();
		if (this.pttGrps != null) {
			this.updateGroupLists();
		}
		if (this.groupOnlineBodyMumber != null) {
			if (this.isChangeMemaber) {
				this.new_member_text.setText((CharSequence) ("(" + this.groupOnlineBodyMumber + "/" + this.groupBodyMumber + ")"));
			} else {
				this.new_member_text.setText((CharSequence) ("(" + this.groupBodyMumber + ")"));
			}
		}
		MyHandler.setHandler(this.myHandler);
//        ((BaseActivity) (TalkBackNew.receiveListener = this)).onCreate(bundle);
		TalkBackNew.mIsCreate = true;
		this.speakerModeOnoffBt.setVisibility(View.GONE);
		this.hookModeOnoffBt.setVisibility(View.GONE);
		final String string = PreferenceManager.getDefaultSharedPreferences((Context) TalkBackNew.mtContext).getString("grpID", "");
		if (!TextUtils.isEmpty((CharSequence) string)) {
			final UserAgent getCurUA = Receiver.GetCurUA();
			final PttGrp getGrpByID = getCurUA.GetGrpByID(string);
			final PttGrp getCurGrp2 = getCurUA.GetCurGrp();
			if (getCurGrp2 != null && getGrpByID != null && getCurGrp2 != getGrpByID) {
				getCurUA.SetCurGrp(getCurUA.getGrpById(getGrpByID.getGrpID()), true);
			}
		}
		if (!NetChecker.check((Context) this, false)) {
			((LinearLayout) this.findViewById(R.id.net_tip2)).setVisibility(View.VISIBLE);
			return;
		}
		((LinearLayout) this.findViewById(R.id.net_tip2)).setVisibility(View.GONE);
	}

	protected void onCurrentGrpChanged() {
		final PttGrp mLastPttGrp = this.mLastPttGrp;
		Tools.saveGrpID(this.mLastPttGrp.grpID);
		if (mLastPttGrp != null) {
			this.mGroupListsMap = GroupListUtil.getGroupListsMap();
			this.group_name_title.setText((CharSequence) mLastPttGrp.grpName);
			this.updateOnlineGroups(mLastPttGrp);
		} else {
			this.group_name_title.setText(R.string.ptt);
		}
		this.mHandler.sendEmptyMessage(2);
		final Message obtainMessage = this.mHandler.obtainMessage();
		obtainMessage.what = 1;
		obtainMessage.arg1 = 500;
		this.mHandler.sendMessage(obtainMessage);
	}

	protected void onCurrentGrpMemberMessagesChanged() {
		final StringBuilder sb = new StringBuilder(" onCurrentGrpMemberMessagesChanged() ");
		final PttGrp mLastPttGrp = this.mLastPttGrp;
		if (mLastPttGrp == null) {
			this.group_name_title.setText(R.string.ptt);
			return;
		}
		this.updateOnlineGroups(mLastPttGrp);
		if (this.mGroupListsMap.get(this.getGrpFromMapListById(mLastPttGrp.getGrpID())) != null) {
			this.arrayList = (ArrayList<GroupListInfo>) this.mGroupListsMap.get(this.getGrpFromMapListById(mLastPttGrp.getGrpID())).clone();
		}
		if (this.arrayList == null) {
			sb.append(" arrayList is null return");
			LogUtil.makeLog(this.TAG, sb.toString());
			return;
		}
		int n = 0;
		while (true) {
			if (n >= this.arrayList.size()) {
				try {
					final ArrayList obj = (ArrayList) this.arrayList.clone();
					LogUtil.makeLog(this.TAG, sb.toString());
					final Message message = new Message();
					message.what = 1;
					message.obj = obj;
					this.sortHandler.sendMessage(message);
					return;
				} catch (Exception ex) {
					sb.append(" Exceptioned when arrayList.clone()!");
					LogUtil.makeLog(this.TAG, sb.toString());
					return;
				}
			}
			if (this.arrayList.get(n).GrpName.trim().length() == 0) {
				final String userName = GroupListUtil.getUserName(this.arrayList.get(n).GrpNum);
				if (userName != null) {
					this.arrayList.get(n).GrpName = userName;
				} else if (this.arrayList.get(n).GrpNum.startsWith("1")) {
					this.arrayList.get(n).GrpName = this.arrayList.get(n).GrpNum;
				} else if (this.arrayList.get(n).GrpNum.length() >= 5) {
					this.arrayList.get(n).GrpName = this.arrayList.get(n).GrpNum.substring(this.arrayList.get(n).GrpNum.length() - 5, this.arrayList.get(n).GrpNum.length());
				} else {
					this.arrayList.get(n).GrpName = this.arrayList.get(n).GrpNum;
				}
			}
			++n;
		}
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
						TalkBackNew.mtContext.unregisterReceiver(this.mReceiver);
					} else {
						MyLog.i("GroupCallActivity", "recv unregister fail! mFilter is null. ");
					}
					if (this.intentfilter2 != null) {
						TalkBackNew.mtContext.unregisterReceiver(this.groupListReceiver);
						MyHandler.setHandler(null);
						TalkBackNew.lineListener = null;
						TalkBackNew.receiveListener = null;
						TalkBackNew.mtContext = null;
						TalkBackNew.mIsCreate = false;
						ZMBluetoothManager.getInstance().removeSppConnectStateListener(this);
						BluetoothSCOStateReceiver.reMoveOnBluetoothAdapterStateChangedListener(this);
						super.onDestroy();
						return;
					}
				} catch (Exception ex) {
					MyLog.i("GroupCallActivity", "unregisterReceiver fail: " + ex.toString());
					continue;
				}
				MyLog.i("GroupCallActivity", "groupListReceiver unregister fail! intentfilter2 is null. ");
				continue;
			}
		}
	}

	public void onDeviceConnectFailed(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
		this.dismissMyDialog(this.mSppConnectProcessDialog);
		this.mSppConnectProcessDialog = DialogUtil.showProcessDailog((Context) this, String.valueOf(this.getResources().getString(R.string.connecting_failed)) + bluetoothIBridgeDevice.getDeviceName());
		if (this.mSppConnectProcessDialog != null) {
			final Message obtainMessage = this.dismissDialogHandler.obtainMessage();
			obtainMessage.what = 2;
			this.dismissDialogHandler.sendMessageDelayed(obtainMessage, 2000L);
		}
	}

	public void onDeviceConnected(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
		this.dismissMyDialog(this.mSppConnectProcessDialog);
		this.mSppConnectProcessDialog = DialogUtil.showProcessDailog((Context) this, String.valueOf(this.getResources().getString(R.string.hm_connected)) + bluetoothIBridgeDevice.getDeviceName());
		if (this.mSppConnectProcessDialog != null) {
			final Message obtainMessage = this.dismissDialogHandler.obtainMessage();
			obtainMessage.what = 2;
			this.dismissDialogHandler.sendMessageDelayed(obtainMessage, 2000L);
		}
	}

	public void onDeviceConnectting(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
		this.mSppConnectProcessDialog = DialogUtil.showProcessDailog((Context) this, String.valueOf(this.getResources().getString(R.string.connecting_hm)) + bluetoothIBridgeDevice.getDeviceName());
	}

	public void onDeviceDisconnected(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
		this.dismissMyDialog(this.mSppConnectProcessDialog);
		this.mSppConnectProcessDialog = DialogUtil.showProcessDailog((Context) this, String.valueOf(this.getResources().getString(R.string.hm_disconnected)) + bluetoothIBridgeDevice.getDeviceName());
		if (this.mSppConnectProcessDialog != null) {
			final Message obtainMessage = this.dismissDialogHandler.obtainMessage();
			obtainMessage.what = 2;
			this.dismissDialogHandler.sendMessageDelayed(obtainMessage, 2000L);
		}
	}

	public void onDeviceDisconnectting(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
		this.dismissMyDialog(this.mSppConnectProcessDialog);
		this.mSppConnectProcessDialog = DialogUtil.showProcessDailog((Context) this, String.valueOf(this.getResources().getString(R.string.disconnecting_hm)) + bluetoothIBridgeDevice.getDeviceName());
	}

	public void onDeviceFound(final BluetoothIBridgeDevice bluetoothIBridgeDevice) {
	}

	@Override
	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		if (n != 4) {
			if (this.pttkeycode != 0 && n == this.pttkeycode) {
				if (!NetChecker.check((Context) TalkBackNew.mtContext, true)) {
					return false;
				}
				if (!checkHasCurrentGrp((Context) TalkBackNew.mtContext)) {
					MyToast.showToast(true, (Context) TalkBackNew.mtContext, R.string.no_groups);
					return false;
				}
				TalkBackNew.isPttPressing = true;
				TalkBackNew.lineListener = this;
				GroupCallUtil.makeGroupCall(true, false, UserAgent.PttPRMode.SideKeyPress);
				setPttBackground(true);
			}
			return super.onKeyDown(n, keyEvent);
		}
		final Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);
		return false;
	}

	public boolean onKeyUp(final int n, final KeyEvent keyEvent) {
		if (this.pttkeycode != 0 && n == this.pttkeycode) {
			if (!NetChecker.check((Context) TalkBackNew.mtContext, true) || !checkHasCurrentGrp((Context) TalkBackNew.mtContext)) {
				return false;
			}
			GroupCallUtil.makeGroupCall(TalkBackNew.isPttPressing = false, false, UserAgent.PttPRMode.Idle);
			setPttBackground(false);
			this.mBaseVisualizerView.setTimes(-1);
		}
		return super.onKeyUp(n, keyEvent);
	}

	public boolean onOptionsItemSelected(final MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case 1: {
				Tools.exitApp((Context) this);
				this.finish();
				break;
			}
		}
		return super.onOptionsItemSelected(menuItem);
	}

	protected void onPause() {
		super.onPause();
		this.stopCurrentAnimation();
		final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
		if (getCurGrp != null && getCurGrp.state == PttGrp.E_Grp_State.GRP_STATE_INITIATING) {
			this.tv_group_status.setText((CharSequence) (String.valueOf(this.mStatus) + this.ShowPttStatus(PttGrp.E_Grp_State.GRP_STATE_IDLE)));
		}
		if (Receiver.GetCurUA().getCurPttPRMode() == UserAgent.PttPRMode.ScreenPress) {
			GroupCallUtil.makeGroupCallNoTip(false, false, UserAgent.PttPRMode.Idle);
		} else if (Receiver.GetCurUA().getCurPttPRMode() == UserAgent.PttPRMode.SideKeyPress && CallManager.getManager().hasInCommingCall()) {
			GroupCallUtil.makeGroupCallNoTip(false, false, UserAgent.PttPRMode.Idle);
		}
		this.mBaseVisualizerView.setTimes(-1);
	}

	public void onResume() {
		int visibility = 0;
		final StringBuilder sb = new StringBuilder("TalkBackNew.onResume()");
		TalkBackNew.isResume = true;
		if (DeviceInfo.CONFIG_SUPPORT_PTTMAP) {
			this.group_set_button.setVisibility(View.VISIBLE);
		} else {
			this.group_set_button.setVisibility(View.INVISIBLE);
		}
		Receiver.engine((Context) TalkBackNew.mtContext);
		if (this.timer1 == null && this.isChangeMemaber) {
			this.timer1 = new Timer();
			Log.e(this.TAG, "GroupRefresh timer1 start");
			this.timer1.schedule(new TimerTask() {
				@Override
				public void run() {
					TalkBackNew.this.pttGrps = TalkBackNew.this.getGrps();
					if (TalkBackNew.this.pttGrps != null) {
						final Message message = new Message();
						message.what = 1;
						TalkBackNew.this.mHandler.sendMessage(message);
						LogUtil.makeLog(TalkBackNew.this.TAG, " mHandler onResume Timer1");
					}
					Log.e(TalkBackNew.this.TAG, "GroupRefresh timer1 runcurThread : " + Thread.currentThread().getName());
				}
			}, 0L, 30000L);
		}
		super.onResume();
		final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
		if (this.pttGrps != null && this.pttGrps.GetCount() > 0) {
			TalkBackNew.mHasPttGrp = (getCurGrp != null);
		} else {
			TalkBackNew.mHasPttGrp = false;
		}
		setPttBackground(TalkBackNew.isPttPressing);
		if (Settings.mNeedBlueTooth && ZMBluetoothManager.getInstance() != null) {
			this.reInitBluetoothButton(TalkBackNew.mIsBTServiceStarted = ZMBluetoothManager.getInstance().isSPPConnected());
		}
		final TextView bluetoothOnoffBt = this.bluetoothOnoffBt;
		if (!Settings.mNeedBlueTooth) {
			visibility = 8;
		}
		bluetoothOnoffBt.setVisibility(visibility);
		final TextView bluetoothModeOnoffBt = this.bluetoothModeOnoffBt;
		if (TalkBackNew.mIsBTServiceStarted || BluetoothSCOStateReceiver.isBluetoothAdapterEnabled) {
		}
		bluetoothModeOnoffBt.setVisibility(View.GONE);
		LogUtil.makeLog(this.TAG, sb.toString());
		this.pttGrps = this.getGrps();
		if (this.mGroupNameAdapter != null) {
			this.mGroupNameAdapter.refreshNameList(this.pttGrps);
			this.mGroupNameAdapter.notifyDataSetChanged();
		}
	}

	public void onStart() {
		this.isStarted = true;
		this.mypre = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		final String string = this.mypre.getString("pttkey", "140");
		if (!string.equals("")) {
			this.pttkeycode = Integer.parseInt(string);
		}
		MediaButtonReceiver.registerMediaButtonEventReceiver(SipUAApp.mContext);
		super.onStart();
	}

	public void onStateOff() {
		this.bluetoothModeOnoffBt.setVisibility(View.GONE);
		ZMBluetoothManager.getInstance().disConnectZMBluetooth((Context) TalkBackNew.mtContext);
	}

	public void onStateOn() {
		final TextView bluetoothModeOnoffBt = this.bluetoothModeOnoffBt;
		if (!TalkBackNew.mIsBTServiceStarted) {
		}
		bluetoothModeOnoffBt.setVisibility(View.GONE);
	}

	public void onStateTurnningOff() {
	}

	public void onStateTurnningOn() {
	}

	public void onStop() {
		if (this.timer1 != null) {
			this.timer1.cancel();
			this.timer1 = null;
		}
		TalkBackNew.isResume = false;
		this.isStarted = false;
		LogUtil.makeLog(this.TAG, "onStop()");
		super.onStop();
	}

	public void reInitBluetoothButton(final boolean mIsBTServiceStarted) {
		Log.i(this.TAG, "isBTServiceStarted = " + mIsBTServiceStarted);
		TalkBackNew.mIsBTServiceStarted = mIsBTServiceStarted;
		final TextView bluetoothOnoffBt = this.bluetoothOnoffBt;
		String s;
		if (mIsBTServiceStarted) {
			s = "#FFbe0a0b";
		} else {
			s = "#FF565759";
		}
		bluetoothOnoffBt.setBackgroundColor(Color.parseColor(s));
		final TextView bluetoothOnoffBt2 = this.bluetoothOnoffBt;
		String text;
		if (mIsBTServiceStarted) {
			text = this.getResources().getString(R.string.disconnect_hm);
		} else {
			text = this.getResources().getString(R.string.connect_hm);
		}
		bluetoothOnoffBt2.setText((CharSequence) text);
	}

	public List<GroupListInfo> sequence(final List<GroupListInfo> list) {
		final ArrayList<GroupListInfo> list2 = new ArrayList<GroupListInfo>();
		for (int i = 0; i < list.size(); ++i) {
			if (i == 0) {
				list2.add(list.get(i));
			} else {
				list2.add(this.findPos(list2, list.get(i)), list.get(i));
			}
		}
		return list2;
	}

	public void showCurrentReceiveVolume(final int times) {
		final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
		if (getCurGrp != null && getCurGrp.state != PttGrp.E_Grp_State.GRP_STATE_IDLE && getCurGrp.state != PttGrp.E_Grp_State.GRP_STATE_SHOUDOWN) {
			this.mBaseVisualizerView.setTimes(times);
		}
	}

	public void showCurrentVolume(final int times) {
		this.mBaseVisualizerView.setTimes(times);
	}

	public void unregisterPttGroupChangedReceiver() {
		if (!this.mPttGroupChangedReceiverRegistered) {
			return;
		}
		while (true) {
			try {
				this.unregisterReceiver(this.mReceiver);
				this.mPttGroupChangedReceiverRegistered = false;
			} catch (Exception ex) {
				ex.printStackTrace();
				MyLog.w(this.TAG, ex.toString());
				continue;
			}
			break;
		}
	}

	public class MyGroupMemberAdapter extends BaseAdapter {
		private String audio;
		private Context context_;
		private AbookOpenHelper dbOpenHelper;
		DataBaseService dbService;
		private TalkBackViewHolder holder;
		private LayoutInflater layoutInflater;
		private int left;
		ArrayList<GroupListInfo> list;
		private Member mem;
		private String pictureupload;
		private String smsswitch;
		private ArrayList<String> tempGrpList;
		private String type;
		private String video;

		public MyGroupMemberAdapter(final Context context_, final ArrayList<GroupListInfo> list) {
			this.dbService = DataBaseService.getInstance();
			this.tempGrpList = new ArrayList<String>();
			this.context_ = context_;
			this.layoutInflater = LayoutInflater.from(this.context_);
			this.list = list;
		}

		public int getCount() {
			if (this.list != null) {
				return this.list.size();
			}
			return 0;
		}

		public Object getItem(final int n) {
			return this.list.get(n);
		}

		public long getItemId(final int n) {
			return n;
		}

		public View getView(final int n, View inflate, final ViewGroup viewGroup) {
			if (inflate == null) {
				this.holder = new TalkBackViewHolder();
				inflate = this.layoutInflater.inflate(R.layout.aa_list_item_group_member, (ViewGroup) null);
				inflate.setBackgroundColor(TalkBackNew.this.getResources().getColor(R.color.black_));
				this.holder.tv1 = (TextView) inflate.findViewById(R.id.member_list_name);
				this.holder.im = (ImageView) inflate.findViewById(R.id.member_list_video_call);
				this.holder.voiceBtn = (ImageView) inflate.findViewById(R.id.call_voice_btn);
				this.holder.msgBtn = (ImageView) inflate.findViewById(R.id.call_msg_btn);
				this.holder.line_sub = (LinearLayout) inflate.findViewById(R.id.line_sub);
				this.holder.line_sub2 = (LinearLayout) inflate.findViewById(R.id.line_sub2);
				inflate.setTag((Object) this.holder);
			} else {
				this.holder = (TalkBackViewHolder) inflate.getTag();
			}
			if (this.list.get(n).GrpState.equals("0")) {
				this.holder.im.setImageDrawable(TalkBackNew.this.getResources().getDrawable(R.drawable.ptt_videonormal));
				this.holder.voiceBtn.setImageDrawable(TalkBackNew.this.getResources().getDrawable(R.drawable.ptt_voice));
				this.holder.msgBtn.setImageDrawable(TalkBackNew.this.getResources().getDrawable(R.drawable.list_message_btn));
				this.holder.tv1.setTextColor(TalkBackNew.this.getResources().getColor(R.color.notOnLine));
			} else {
				this.holder.im.setImageDrawable(TalkBackNew.this.getResources().getDrawable(R.drawable.ptt_video));
				this.holder.voiceBtn.setImageDrawable(TalkBackNew.this.getResources().getDrawable(R.drawable.ptt_voicepress));
				this.holder.msgBtn.setImageDrawable(TalkBackNew.this.getResources().getDrawable(R.drawable.list_message_btn_down));
				this.holder.tv1.setTextColor(TalkBackNew.this.getResources().getColor(R.color.onLine));
			}
			if (this.list.get(n).GrpName.trim().length() == 0) {
				this.holder.tv1.setText((CharSequence) this.list.get(n).GrpNum);
			} else {
				this.holder.tv1.setText((CharSequence) this.list.get(n).GrpName);
			}
			final String grpNum = this.list.get(n).GrpNum;
			MyLog.i("ee", "mNumber==>" + grpNum);
			this.mem = this.dbService.getStringbyItem(grpNum);
			this.video = this.mem.getVideo();
			MyLog.i("ee", "video==>" + this.video);
			this.audio = this.mem.getAudio();
			this.pictureupload = this.mem.getPictureupload();
			this.smsswitch = this.mem.getSmsswitch();
			this.type = this.mem.getMtype();
			MyLog.i("ee", "type==>" + this.type);
			this.left = 0;
			if (!TalkBackNew.this.isVisibleContactVideo(this.type, this.video)) {
				this.holder.line_sub.setVisibility(View.GONE);
				this.holder.im.setVisibility(View.GONE);
			} else {
				this.holder.line_sub.setVisibility(View.VISIBLE);
				this.holder.im.setVisibility(View.VISIBLE);
			}
			if (!DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE || (!TextUtils.isEmpty((CharSequence) this.audio) && this.audio.equalsIgnoreCase("0"))) {
				this.holder.line_sub.setVisibility(View.GONE);
				this.holder.voiceBtn.setVisibility(View.GONE);
			} else if (this.audio != null && this.audio.equalsIgnoreCase("0")) {
				this.holder.line_sub.setVisibility(View.GONE);
				this.holder.voiceBtn.setVisibility(View.GONE);
			} else {
				this.holder.voiceBtn.setVisibility(View.VISIBLE);
				++this.left;
			}
			if (TalkBackNew.this.getServerListArray() == -1) {
				this.holder.line_sub2.setVisibility(View.GONE);
				this.holder.msgBtn.setVisibility(View.GONE);
			} else if (!TextUtils.isEmpty((CharSequence) this.type) && !TextUtils.isEmpty((CharSequence) this.smsswitch)) {
				if (Member.UserType.toUserType(this.type) == Member.UserType.VIDEO_MONITOR_GB28181 || Member.UserType.toUserType(this.type) == Member.UserType.VIDEO_MONITOR_GVS || (Member.UserType.toUserType(this.type) == Member.UserType.MOBILE_GQT && this.pictureupload.equalsIgnoreCase("0") && this.smsswitch.equalsIgnoreCase("0"))) {
					this.holder.line_sub2.setVisibility(View.GONE);
					this.holder.msgBtn.setVisibility(View.GONE);
				} else if (!DeviceInfo.CONFIG_SUPPORT_PICTURE_UPLOAD && Member.UserType.toUserType(this.type) == Member.UserType.MOBILE_GQT && this.smsswitch.equalsIgnoreCase("0")) {
					this.holder.line_sub2.setVisibility(View.GONE);
					this.holder.msgBtn.setVisibility(View.GONE);
				} else if (!DeviceInfo.CONFIG_SUPPORT_IM && Member.UserType.toUserType(this.type) == Member.UserType.MOBILE_GQT && this.pictureupload.equalsIgnoreCase("0")) {
					this.holder.line_sub2.setVisibility(View.GONE);
					this.holder.msgBtn.setVisibility(View.GONE);
				} else {
					this.holder.msgBtn.setVisibility(View.VISIBLE);
					this.holder.line_sub2.setVisibility(View.VISIBLE);
					++this.left;
				}
			} else {
				this.holder.line_sub2.setVisibility(View.GONE);
				this.holder.msgBtn.setVisibility(View.GONE);
			}
			if (this.holder.voiceBtn.getVisibility() == View.GONE && this.holder.im.getVisibility() == View.GONE) {
				this.holder.line_sub2.setVisibility(View.GONE);
			}
			this.holder.im.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					CallUtil.makeVideoCall((Context) TalkBackNew.mtContext, grpNum, null, "videobut");
				}
			});
			this.holder.tv1.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					if (DeviceInfo.CONFIG_SUPPORT_PTTMAP) {
						final Intent intent = null;
						Intent intent2 = null;
						final String grpNum = MyGroupMemberAdapter.this.list.get(n).getGrpNum();
						MyLog.i("dd", "section=" + grpNum);
						switch (DeviceInfo.CONFIG_MAP_TYPE) {
							case 0: {
								final int int1 = TalkBackNew.this.mypre.getInt("maptype", 0);
								Log.v("TalkBackNew", "\u5730\u56fe\u7c7b\u578b\u662f" + int1);
								intent2 = intent;
								if (int1 == 1) {
									intent2 = new Intent((Context) TalkBackNew.mtContext, (Class) JsLocationOverlay.class);
								}
								if (int1 == 0) {
									intent2 = new Intent((Context) TalkBackNew.mtContext, (Class) LocationOverlayDemo.class);
								}
								intent2.putExtra("transmitnumber", grpNum);
								break;
							}
							case 1: {
//								intent2 = new Intent((Context) TalkBackNew.mtContext, (Class) GoogleLocationOverlay.class);
								intent2.putExtra("transmitnumber", grpNum);
								break;
							}
						}
						if (intent2 != null) {
							TalkBackNew.this.startActivity(intent2);
						}
					}
				}
			});
			return inflate;
		}

		public void refreshList(final ArrayList<GroupListInfo> list) {
			this.list = list;
		}
	}

	public class MyGroupNameAdapter extends BaseAdapter {
		private Context context_;
		private String groupName;
		private boolean isCurGrpNull;
		private LayoutInflater layoutInflater;
		private int length;
		private PttGrps pttGrps;

		public MyGroupNameAdapter(final Context context_, final PttGrps pttGrps) {
			this.isCurGrpNull = true;
			this.context_ = context_;
			this.layoutInflater = LayoutInflater.from(this.context_);
			this.pttGrps = pttGrps;
		}

		public int getCount() {
			if (this.pttGrps != null) {
				return this.pttGrps.GetCount();
			}
			return 0;
		}

		public Object getItem(final int n) {
			return this.pttGrps.GetGrpByIndex(n);
		}

		public long getItemId(final int n) {
			return n;
		}

		public View getView(final int n, final View view, final ViewGroup viewGroup) {
			View inflate = view;
			if (view == null) {
				inflate = this.layoutInflater.inflate(R.layout.aa_list_item_group_name, (ViewGroup) null);
			}
			final TextView textView = (TextView) inflate.findViewById(R.id.aa_list_item_groupname);
			if (this.pttGrps == null) {
				this.length = 0;
				return null;
			}
			this.length = this.pttGrps.GetCount();
			if (this.pttGrps != null && Receiver.GetCurUA() != null && Receiver.GetCurUA().GetCurGrp() != null && Receiver.GetCurUA().GetCurGrp().grpID.equals(this.pttGrps.GetGrpByIndex(n).grpID)) {
				inflate.setBackgroundResource(R.color.font_color);
				textView.setTextColor(TalkBackNew.this.getResources().getColor(R.color.black));
			} else {
				inflate.setBackgroundResource(R.color.font_color2);
				textView.setTextColor(TalkBackNew.this.getResources().getColor(R.color.white));
			}
			textView.setText((CharSequence) (this.groupName = this.pttGrps.GetGrpByIndex(n).grpName));
			return inflate;
		}

		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			final StringBuilder sb = new StringBuilder("MyGroupNameAdapter#notifyDataSetChanged()");
			final UserAgent getCurUA = Receiver.GetCurUA();
			if (getCurUA == null) {
				sb.append(" GetCurUA() is null");
				LogUtil.makeLog(TalkBackNew.this.TAG, sb.toString());
				return;
			}
			final PttGrp getCurGrp = getCurUA.GetCurGrp();
			TalkBackNew.this.resetGroupNameTitle(getCurGrp);
			if (getCurGrp == null) {
				this.isCurGrpNull = true;
				sb.append(" GetCurGrp is null");
				LogUtil.makeLog(TalkBackNew.this.TAG, sb.toString());
				return;
			}
			this.isCurGrpNull = false;
			LogUtil.makeLog(TalkBackNew.this.TAG, sb.toString());
		}

		public void refreshNameList(final PttGrps pttGrps) {
			this.pttGrps = pttGrps;
		}
	}

	public class TalkBackViewHolder {
		public ImageView contact_video;
		public ImageView im;
		public LinearLayout line_sub;
		public LinearLayout line_sub2;
		public ImageView msgBtn;
		public TextView tv1;
		public ImageView voiceBtn;

		public void TalkBackViewHolder() {
		}
	}
}
