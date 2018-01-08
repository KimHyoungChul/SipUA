package com.zed3.sipua.ui.lowsdk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.addressbook.DataBaseService;
import com.zed3.constant.GroupConstant;
import com.zed3.groupcall.GroupCallUtil;
import com.zed3.log.MyLog;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.PttGrp.E_Grp_State;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.UserAgent.PttPRMode;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.toast.MyToast;
import com.zed3.utils.LoadingAnimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TempGrpCallActivity extends Activity implements OnClickListener, OnItemClickListener {
	public static final String ACTION_TEMP_GRP_CLOSING = "com.zed3.sipua.tmpgrp.closing";
	public static final String ACTION_TEMP_GRP_CREATE_SUCCESS = "com.zed3.sipua.tmpgrp.create_success";
	public static final String ACTION_TEMP_GRP_INVITE = "com.zed3.sipua.tmpgrp.invite";
	private static boolean isPttPressing;
	public static boolean isResume;
	private static TempGrpCallActivity mContext;
	private static boolean mHasPttGrp;
	private static Button mPttBtn;
	public Handler TepttPressHandler = new C12961();
	SimpleAdapter adapter;
	boolean isCreator = false;
	boolean isOtherCompany = false;
	private List<Map<String, Object>> mDataList = new ArrayList();
	private TextView mGroupName;
	private LoadingAnimation mLoadingAnimation;
	private ArrayList<String> mMemberList = new ArrayList();
	private GridView mMembers;
	private TextView mOut;
	private TextView mStatus;
	private String tempGroupName = "";
	private BroadcastReceiver tmpGrpReceiver = new C12972();

	class C12961 extends Handler {
		C12961() {
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					TempGrpCallActivity.isPttPressing = false;
					TempGrpCallActivity.setPttBackground(false);
					return;
				case 1:
					TempGrpCallActivity.isPttPressing = true;
					TempGrpCallActivity.setPttBackground(true);
					return;
				default:
					return;
			}
		}
	}

	class C12972 extends BroadcastReceiver {
		C12972() {
		}

		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(TempGrpCallActivity.ACTION_TEMP_GRP_CLOSING)) {
				TempGrpCallActivity.this.mStatus.setText(TempGrpCallActivity.this.getString(R.string.create_timeout));
				if (intent.getBooleanExtra("resused", false)) {
					MyToast.showToast(true, context, TempGrpCallActivity.this.getString(R.string.tmpgrpcall_refused_tip));
				}
				if (intent.getBooleanExtra("isTimeout", false)) {
					Toast.makeText(TempGrpCallActivity.this, TempGrpCallActivity.this.getString(R.string.temp_group_timeout), Toast.LENGTH_SHORT).show();
				}
				Log.i("zdx", "ACTION_TEMP_GRP_CLOSING");
				TempGrpCallActivity.this.finish();
			} else if (intent.getAction().equalsIgnoreCase(GroupConstant.ACTION_GROUP_STATUS)) {
				TempGrpCallActivity.this.stopCurrentAnimation();
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
				PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
				if (pttGrp == null) {
					TempGrpCallActivity.this.mStatus.setText(TempGrpCallActivity.this.getResources().getString(R.string.status_none));
				} else if (pttGrp.state == E_Grp_State.GRP_STATE_INITIATING) {
					TempGrpCallActivity.this.mStatus.setText(TempGrpCallActivity.this.ShowPttStatus(pttGrp.state));
					TempGrpCallActivity.this.stopCurrentAnimation();
					TempGrpCallActivity.this.mLoadingAnimation = new LoadingAnimation();
					TempGrpCallActivity.this.mLoadingAnimation.setAppendCount(3).startAnimation(TempGrpCallActivity.this.mStatus);
				} else {
					TempGrpCallActivity.this.stopCurrentAnimation();
					TempGrpCallActivity.this.mStatus.setText(TempGrpCallActivity.this.ShowPttStatus(pttGrp.state));
					TempGrpCallActivity.this.mStatus.setText(TempGrpCallActivity.this.ShowSpeakerStatus(speaker, userNum));
				}
			} else if (intent.getAction().equalsIgnoreCase(TempGrpCallActivity.ACTION_TEMP_GRP_INVITE)) {
				ArrayList<String> inviteMembers = intent.getStringArrayListExtra("inviteMembers");
				if (inviteMembers != null && inviteMembers.size() > 0) {
					for (int i = 0; i < inviteMembers.size(); i++) {
						String str = (String) inviteMembers.get(i);
						if (!TempGrpCallActivity.this.mMemberList.contains(str)) {
							TempGrpCallActivity.this.mMemberList.add(str);
						}
					}
					TempGrpCallActivity.this.loadData();
					TempGrpCallActivity.this.adapter.notifyDataSetChanged();
				}
			} else if (intent.getAction().equalsIgnoreCase(TempGrpCallActivity.ACTION_TEMP_GRP_CREATE_SUCCESS)) {
				TempGrpCallActivity.this.mStatus.setText("创建成功");
			}
		}
	}

	class C12983 implements OnTouchListener {
		C12983() {
		}

		public boolean onTouch(View v, MotionEvent event) {
			if (NetChecker.check(TempGrpCallActivity.this, true)) {
				switch (event.getAction()) {
					case 0:
						TempGrpCallActivity.mPttBtn.setBackgroundResource(R.color.loginoutpress);
						TempGrpCallActivity.isPttPressing = true;
						GroupCallUtil.makeGroupCall(true, false, PttPRMode.ScreenPress);
						break;
					case 1:
						PttGrp pttGrp = Receiver.GetCurUA().GetCurGrp();
						if (pttGrp != null && pttGrp.state == E_Grp_State.GRP_STATE_INITIATING) {
							TempGrpCallActivity.this.mStatus.setText(TempGrpCallActivity.this.ShowPttStatus(E_Grp_State.GRP_STATE_IDLE));
						}
						if (TempGrpCallActivity.isPttPressing) {
							TempGrpCallActivity.isPttPressing = false;
							GroupCallUtil.makeGroupCall(false, false, PttPRMode.Idle);
							TempGrpCallActivity.mPttBtn.setBackgroundResource(R.color.loginoutnormal);
							break;
						}
						break;
					default:
						break;
				}
			}
			return false;
		}
	}


	public TempGrpCallActivity() {
		this.mMemberList = new ArrayList<String>();
		this.mDataList = new ArrayList<Map<String, Object>>();
		this.isCreator = false;
		this.tempGroupName = "";
		this.isOtherCompany = false;
		this.TepttPressHandler = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.what) {
					default: {
					}
					case 0: {
						TempGrpCallActivity.setPttBackground(false);
					}
					case 1: {
						TempGrpCallActivity.setPttBackground(true);
					}
				}
			}
		};
		this.tmpGrpReceiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				if (intent.getAction().equals("com.zed3.sipua.tmpgrp.closing")) {
					TempGrpCallActivity.this.mStatus.setText((CharSequence) TempGrpCallActivity.this.getString(R.string.create_timeout));
					if (intent.getBooleanExtra("resused", false)) {
						MyToast.showToast(true, context, TempGrpCallActivity.this.getString(R.string.tmpgrpcall_refused_tip));
					}
					if (intent.getBooleanExtra("isTimeout", false)) {
						Toast.makeText((Context) TempGrpCallActivity.this, (CharSequence) TempGrpCallActivity.this.getString(R.string.temp_group_timeout), Toast.LENGTH_SHORT).show();
					}
					Log.i("zdx", "ACTION_TEMP_GRP_CLOSING");
					TempGrpCallActivity.this.finish();
				} else if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.ui_groupcall.group_status")) {
					TempGrpCallActivity.this.stopCurrentAnimation();
					final Bundle extras = intent.getExtras();
					String trim;
					if (extras.getString("1") != null) {
						trim = extras.getString("1").trim();
					} else {
						trim = null;
					}
					String s = null;
					String s2 = trim;
					if (trim != null) {
						final String[] split = trim.split(" ");
						if (split.length == 1) {
							s = split[0];
							s2 = trim;
						} else {
							s = split[0];
							s2 = split[1];
						}
					}
					final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
					if (getCurGrp == null) {
						TempGrpCallActivity.this.mStatus.setText((CharSequence) TempGrpCallActivity.this.getResources().getString(R.string.status_none));
						return;
					}
					if (getCurGrp.state == PttGrp.E_Grp_State.GRP_STATE_INITIATING) {
						TempGrpCallActivity.this.mStatus.setText((CharSequence) TempGrpCallActivity.this.ShowPttStatus(getCurGrp.state));
						TempGrpCallActivity.this.stopCurrentAnimation();
//						TempGrpCallActivity.access .3
//						(TempGrpCallActivity.this, new LoadingAnimation());
						TempGrpCallActivity.this.mLoadingAnimation.setAppendCount(3).startAnimation(TempGrpCallActivity.this.mStatus);
						return;
					}
					TempGrpCallActivity.this.stopCurrentAnimation();
					TempGrpCallActivity.this.mStatus.setText((CharSequence) TempGrpCallActivity.this.ShowPttStatus(getCurGrp.state));
					TempGrpCallActivity.this.mStatus.setText((CharSequence) TempGrpCallActivity.this.ShowSpeakerStatus(s2, s));
				} else if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.tmpgrp.invite")) {
					final ArrayList stringArrayListExtra = intent.getStringArrayListExtra("inviteMembers");
					if (stringArrayListExtra != null && stringArrayListExtra.size() > 0) {
						for (int i = 0; i < stringArrayListExtra.size(); ++i) {
//							final String s3 = stringArrayListExtra.get(i);
//							if (!TempGrpCallActivity.this.mMemberList.contains(s3)) {
//								TempGrpCallActivity.this.mMemberList.add(s3);
//							}
						}
						TempGrpCallActivity.this.loadData();
						TempGrpCallActivity.this.adapter.notifyDataSetChanged();
					}
				} else if (intent.getAction().equalsIgnoreCase("com.zed3.sipua.tmpgrp.create_success")) {
					TempGrpCallActivity.this.mStatus.setText((CharSequence) "\u521b\u5efa\u6210\u529f");
				}
			}
		};
	}

	public static boolean checkHasCurrentGrp(final Context context) {
		return TempGrpCallActivity.mHasPttGrp;
	}

	public static TempGrpCallActivity getInstance() {
		return TempGrpCallActivity.mContext;
	}

	private void loadData() {
		this.mDataList.clear();
		if (this.mMemberList.size() > 0) {
			for (int i = 0; i < this.mMemberList.size(); ++i) {
				final HashMap<String, Object> hashMap = new HashMap<String, Object>();
				hashMap.put("image", R.drawable.icon_contact);
				String s;
				if (TextUtils.isEmpty((CharSequence) (s = GroupListUtil.getUserName(this.mMemberList.get(i))))) {
					final int length = this.mMemberList.get(i).length();
					if (length > 5) {
						s = this.mMemberList.get(i).substring(length - 5, length);
					} else {
						s = this.mMemberList.get(i);
					}
				}
				hashMap.put("memberName", s);
				this.mDataList.add(hashMap);
			}
		}
		if (this.mMemberList.size() > 1) {
			if (Settings.getUserName().equals(this.mMemberList.get(0))) {
				MyLog.i("gjb", "first num is self!");
				this.isOtherCompany = this.isOtherCompany(this.mMemberList.get(1));
			} else {
				this.isOtherCompany = this.isOtherCompany(this.mMemberList.get(0));
			}
			if (this.isCreator && !this.isOtherCompany) {
				final HashMap<String, String> hashMap2 = new HashMap<String, String>();
//				hashMap2.put("image", R.drawable.meeting_invite);
//				hashMap2.put("memberName", "");
//				this.mDataList.add((Map<String, Object>) hashMap2);
			}
		}
	}

	public static void setPttBackground(final boolean b) {
		final Button mPttBtn = TempGrpCallActivity.mPttBtn;
		int backgroundResource;
		if (b) {
			backgroundResource = R.color.loginoutpress;
		} else {
			backgroundResource = R.color.loginoutnormal;
		}
		mPttBtn.setBackgroundResource(backgroundResource);
	}

	private void stopCurrentAnimation() {
		if (this.mLoadingAnimation != null) {
			this.mLoadingAnimation.stopAnimation();
		}
	}

	public String ShowPttStatus(final PttGrp.E_Grp_State e_Grp_State) {
//		switch (e_Grp_State){
//			default: {
//				return this.getResources().getString(R.string.error);
//			}
//			case 1: {
//				return this.getString(R.string.close);
//			}
//			case 2: {
//				return this.getString(R.string.idle);
//			}
//			case 4: {
//				return this.getString(R.string.talking);
//			}
//			case 5: {
//				return this.getString(R.string.listening);
//			}
//			case 6: {
//				return this.getString(R.string.queueing);
//			}
//			case 3: {
//				return this.getString(R.string.ptt_requesting);
//			}
//		}
		return "";
	}

	public String ShowSpeakerStatus(final String s, final String s2) {
		if (s == null || s.equals("")) {
			return this.getResources().getString(R.string.talking_none);
		}
		if (s2.equals(Settings.getUserName())) {
			return this.getResources().getString(R.string.talking_me);
		}
		return String.valueOf(this.getResources().getString(R.string.talking_someOne)) + "\uff08" + s + "\uff09";
	}

	boolean isOtherCompany(final String s) {
		if (!TextUtils.isEmpty((CharSequence) s)) {
			final String companyId = DataBaseService.getInstance().getCompanyId();
			if (!TextUtils.isEmpty((CharSequence) companyId) && companyId.length() == 6 && (s.length() == 5 || s.length() == 11)) {
				if (s.length() == 5) {
					return TextUtils.isEmpty((CharSequence) companyId);
				}
				if (s.substring(0, 6).equals(companyId) || DataBaseService.getInstance().sameCopmany(s)) {
					return false;
				}
			}
		}
		return true;
	}

	protected void onActivityResult(final int n, final int n2, final Intent intent) {
		if (n == 1 && n2 == -1) {
			final Iterator iterator = intent.getStringArrayListExtra("inviteMembers").iterator();
//			while (iterator.hasNext()) {
//				this.mMemberList.add(iterator.next());
//			}
			this.loadData();
			this.adapter.notifyDataSetChanged();
		}
		super.onActivityResult(n, n2, intent);
	}

	public void onBackPressed() {
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			default: {
			}
			case R.id.tv_out: {
				Receiver.GetCurUA().hangupTmpGrpCall(true);
				this.finish();
			}
		}
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.activity_temp_group_call);
		final Intent intent = this.getIntent();
		this.mMemberList = (ArrayList<String>) intent.getStringArrayListExtra("groupMemberList");
		this.isCreator = intent.getBooleanExtra("isCreator", false);
		this.tempGroupName = intent.getStringExtra("tempGroupName");
		final boolean booleanExtra = intent.getBooleanExtra("callee", false);
		(this.mOut = (TextView) this.findViewById(R.id.tv_out)).setOnClickListener((View.OnClickListener) this);
		TempGrpCallActivity.mContext = this;
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.zed3.sipua.tmpgrp.closing");
		intentFilter.addAction("com.zed3.sipua.ui_groupcall.group_status");
		intentFilter.addAction("com.zed3.sipua.tmpgrp.invite");
		intentFilter.addAction("com.zed3.sipua.tmpgrp.create_success");
		this.registerReceiver(this.tmpGrpReceiver, intentFilter);
		if (booleanExtra && TempGroupCallUtil.isTmpCallClosed()) {
			this.finish();
		} else {
			(this.mGroupName = (TextView) this.findViewById(R.id.tv_group_call_name)).setText((CharSequence) this.tempGroupName);
			this.mMembers = (GridView) this.findViewById(R.id.gv_members);
			this.mStatus = (TextView) this.findViewById(R.id.tv_status);
			(TempGrpCallActivity.mPttBtn = (Button) this.findViewById(R.id.btn_temp_grp_ptt)).setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
				public boolean onTouch(final View view, final MotionEvent motionEvent) {
					if (NetChecker.check((Context) TempGrpCallActivity.this, true)) {
						switch (motionEvent.getAction()) {
							default: {
								return false;
							}
							case 0: {
								TempGrpCallActivity.mPttBtn.setBackgroundResource(R.color.loginoutpress);
//								TempGrpCallActivity.access .0 (true);
								GroupCallUtil.makeGroupCall(true, false, UserAgent.PttPRMode.ScreenPress);
								return false;
							}
							case 1: {
								final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
								if (getCurGrp != null && getCurGrp.state == PttGrp.E_Grp_State.GRP_STATE_INITIATING) {
									TempGrpCallActivity.this.mStatus.setText((CharSequence) TempGrpCallActivity.this.ShowPttStatus(PttGrp.E_Grp_State.GRP_STATE_IDLE));
								}
								if (TempGrpCallActivity.isPttPressing) {
//									TempGrpCallActivity.access .0 (false);
									GroupCallUtil.makeGroupCall(false, false, UserAgent.PttPRMode.Idle);
									TempGrpCallActivity.mPttBtn.setBackgroundResource(R.color.loginoutnormal);
									return false;
								}
								break;
							}
						}
					}
					return false;
				}
			});
			this.loadData();
			this.adapter = new SimpleAdapter((Context) this, (List) this.mDataList, R.layout.item_tempcall_grid, new String[]{"image", "memberName"}, new int[]{R.id.iv_member_image, R.id.tv_member_info});
			this.mMembers.setAdapter((ListAdapter) this.adapter);
			if (this.isCreator && !this.isOtherCompany) {
				this.mMembers.setOnItemClickListener((AdapterView.OnItemClickListener) this);
				this.mStatus.setText((CharSequence) this.getString(R.string.temp_group_creating));
			}
			if (TempGroupCallUtil.mCall != null && TempGroupCallUtil.mCall.isOnCall()) {
				this.mStatus.setText((CharSequence) this.getString(R.string.create_success));
			}
		}
	}

	protected void onDestroy() {
		this.unregisterReceiver(this.tmpGrpReceiver);
		Receiver.saveTmpGrpCallHistory(this.tempGroupName, this.mMemberList);
		super.onDestroy();
	}

	public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
		if (n == this.mDataList.size() - 1) {
			final Intent intent = new Intent();
			intent.setClass((Context) this, (Class) SelectPersonsActivity.class);
			intent.putExtra("isInvite", true);
			intent.putExtra("tempGroupName", this.tempGroupName);
			intent.putStringArrayListExtra("selectedList", (ArrayList) this.mMemberList);
			this.startActivityForResult(intent, 1);
		}
	}

	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		return n == 4 || super.onKeyDown(n, keyEvent);
	}

	public boolean onKeyUp(final int n, final KeyEvent keyEvent) {
		return super.onKeyUp(n, keyEvent);
	}

	protected void onResume() {
		boolean mHasPttGrp = true;
		TempGrpCallActivity.isResume = true;
		TempGrpCallActivity.isPttPressing = false;
		if (Receiver.GetCurUA().GetCurGrp() == null) {
			mHasPttGrp = false;
		}
		TempGrpCallActivity.mHasPttGrp = mHasPttGrp;
		setPttBackground(TempGrpCallActivity.isPttPressing);
		super.onResume();
	}

	protected void onStop() {
		TempGrpCallActivity.isResume = false;
		super.onStop();
	}
}
