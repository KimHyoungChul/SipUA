package com.zed3.customgroup;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
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
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.DepartmentAdapter;
import com.zed3.addressbook.UserMinuteActivity;
import com.zed3.log.MyLog;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.LogUtil;
import com.zed3.zhejiang.ZhejiangReceivier;

import org.zoolu.tools.GroupListInfo;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupMemberListAcitivity extends BaseActivity implements OnClickListener, OnQueryTextListener {
	private static final int DESTROY_RESULT = 2;
	private static final int EXIT_RESULT = 3;
	private static final int SET_ADAPTER = 0;
	private static final String TAG = "GroupMemberListAcitivity";
	private static final int TIME_OUT = 4;
	private static final int UPDATE_RESULT = 1;
	private LinearLayout btn_left;
	private LinearLayout choice_layout;
	private int currentGroupType = 0;
	private String currentGrpName;
	private CustomGroupStateReceiver customGroupStateReceiver;
	private PttCustomGrp customList = new PttCustomGrp();
	private String editor_type = null;
	private DepartmentAdapter grpMemberListAdapter;
	private IntentFilter intentFilter;
	private ListView listView;
	private Context mContext;
	List<Map<String, String>> mmList = new ArrayList();
	private Handler myHandler = new C09491();
	private List<GroupListInfo> permanentList = new ArrayList();
	private SearchView searchView;
	private TextView tv_grp_add;
	private TextView tv_grp_delete;
	private TextView tv_grp_dissolve;
	private TextView tv_grp_exit;
	private TextView tv_grp_name;
	private UserAgent userAgent;

	class C09491 extends Handler {

		class C09471 implements Comparator<Map<String, String>> {
			C09471() {
			}

			public int compare(Map<String, String> arg0, Map<String, String> arg1) {
				return ((String) arg0.get(UserMinuteActivity.USER_MNAME)).compareTo((String) arg1.get(UserMinuteActivity.USER_MNAME));
			}
		}

		class C09482 implements Comparator<Map<String, String>> {
			C09482() {
			}

			public int compare(Map<String, String> arg0, Map<String, String> arg1) {
				return ((String) arg0.get(UserMinuteActivity.USER_MNAME)).compareTo((String) arg1.get(UserMinuteActivity.USER_MNAME));
			}
		}

		C09491() {
		}

		public void handleMessage(Message msg) {
			CustomGroupUtil.getInstance().dismissProgressDialog();
			int reasonCode;
			switch (msg.what) {
				case 0:
					List<CustomGroupMemberInfo> infoList = (List<CustomGroupMemberInfo>) msg.obj;
					List<Map<String, String>> statusList0 = new ArrayList();
					List<Map<String, String>> statusList1 = new ArrayList();
					DataBaseService dbBaseService = DataBaseService.getInstance();
					for (CustomGroupMemberInfo cgi : infoList) {
						Log.i("jiangkai", "name " + cgi.getMemberName() + " status " + cgi.getMemberStatus());
						Map<String, String> mmMap;
						String str;
						Object memberNum;
						if (cgi.getMemberStatus().equals("0")) {
							mmMap = new HashMap();
							if (dbBaseService.sameCopmany(cgi.getMemberNum())) {
								mmMap = dbBaseService.getMember(cgi.getMemberNum());
							} else {
								str = UserMinuteActivity.USER_MNAME;
								memberNum = (cgi.getMemberName() == null || cgi.getMemberName().equals("")) ? cgi.getMemberNum() : cgi.getMemberName();
								mmMap.put(str, (String) memberNum);
								mmMap.put("number", cgi.getMemberNum());
							}
							mmMap.put(ZhejiangReceivier.STATUS, cgi.getMemberStatus());
							statusList0.add(mmMap);
						} else {
							mmMap = new HashMap();
							if (dbBaseService.sameCopmany(cgi.getMemberNum())) {
								mmMap = dbBaseService.getMember(cgi.getMemberNum());
							} else {
								str = UserMinuteActivity.USER_MNAME;
								memberNum = (cgi.getMemberName() == null || cgi.getMemberName().equals("")) ? cgi.getMemberNum() : cgi.getMemberName();
								mmMap.put(str, (String) memberNum);
								mmMap.put("number", cgi.getMemberNum());
							}
							mmMap.put(ZhejiangReceivier.STATUS, cgi.getMemberStatus());
							statusList1.add(mmMap);
						}
					}
					if (statusList1.size() > 0) {
						Collections.sort(statusList1, new C09471());
					}
					if (statusList0.size() > 0) {
						Collections.sort(statusList0, new C09482());
					}
					GroupMemberListAcitivity.this.mmList.addAll(statusList1);
					GroupMemberListAcitivity.this.mmList.addAll(statusList0);
					GroupMemberListAcitivity.this.grpMemberListAdapter = new DepartmentAdapter(GroupMemberListAcitivity.this.mContext, null, DataBaseService.getInstance());
					GroupMemberListAcitivity.this.grpMemberListAdapter.getData(GroupMemberListAcitivity.this.mmList);
					GroupMemberListAcitivity.this.listView.setAdapter(GroupMemberListAcitivity.this.grpMemberListAdapter);
					return;
				case 1:
					GroupMemberListAcitivity.this.refreshAdapter();
					return;
				case 2:
					if (msg.arg1 == 0) {
						reasonCode = msg.arg2;
						if (reasonCode == 453) {
							CustomGroupUtil.getInstance().showFailureReason(GroupMemberListAcitivity.this.mContext, reasonCode);
							return;
						} else {
							CustomGroupUtil.getInstance().showToast(GroupMemberListAcitivity.this.mContext, (int) R.string.dissolve_failure);
							return;
						}
					} else if (msg.arg1 == 1) {
						CustomGroupUtil.getInstance().showToast(GroupMemberListAcitivity.this.mContext, (int) R.string.dissolve_success);
						GroupMemberListAcitivity.this.updateInfo();
						return;
					} else {
						return;
					}
				case 3:
					if (msg.arg1 == 0) {
						reasonCode = msg.arg2;
						if (reasonCode == 456) {
							CustomGroupUtil.getInstance().showFailureReason(GroupMemberListAcitivity.this.mContext, reasonCode);
							return;
						} else {
							CustomGroupUtil.getInstance().showToast(GroupMemberListAcitivity.this.mContext, (int) R.string.exit_failure);
							return;
						}
					} else if (msg.arg1 == 1) {
						CustomGroupUtil.getInstance().showToast(GroupMemberListAcitivity.this.mContext, (int) R.string.exit_success);
						GroupMemberListAcitivity.this.updateInfo();
						return;
					} else {
						return;
					}
				case 4:
					CustomGroupUtil.getInstance().showToast(GroupMemberListAcitivity.this.mContext, (int) R.string.time_out);
					return;
				default:
					return;
			}
		}
	}

	class C09502 implements OnClickListener {
		C09502() {
		}

		public void onClick(View v) {
			GroupMemberListAcitivity.this.finish();
		}
	}

	class C09513 implements OnTouchListener {
		C09513() {
		}

		public boolean onTouch(View v, MotionEvent event) {
			TextView tv = (TextView) GroupMemberListAcitivity.this.findViewById(R.id.t_leftbtn);
			TextView tv_left = (TextView) GroupMemberListAcitivity.this.findViewById(R.id.left_icon);
			switch (event.getAction()) {
				case 0:
					tv.setTextColor(-1);
					GroupMemberListAcitivity.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
					tv_left.setBackgroundResource(R.drawable.map_back_press);
					break;
				case 1:
					tv.setTextColor(GroupMemberListAcitivity.this.getResources().getColor(R.color.font_color3));
					GroupMemberListAcitivity.this.btn_left.setBackgroundResource(R.color.whole_bg);
					tv_left.setBackgroundResource(R.drawable.map_back_release);
					break;
			}
			return false;
		}
	}

	class C09524 implements Runnable {
		C09524() {
		}

		public void run() {
			List<CustomGroupMemberInfo> infoList = new ArrayList();
			if (GroupMemberListAcitivity.this.currentGroupType == 0 && GroupMemberListAcitivity.this.permanentList != null) {
				for (int i = 0; i < GroupMemberListAcitivity.this.permanentList.size(); i++) {
					GroupListInfo groupListInfo = (GroupListInfo) GroupMemberListAcitivity.this.permanentList.get(i);
					CustomGroupMemberInfo grpMemberInfo = new CustomGroupMemberInfo();
					grpMemberInfo.setMemberName(groupListInfo.getGrpName());
					grpMemberInfo.setMemberNum(groupListInfo.getGrpNum());
					grpMemberInfo.setMemberStatus(groupListInfo.getGrpState());
					infoList.add(grpMemberInfo);
				}
			} else if (GroupMemberListAcitivity.this.currentGroupType == 1 && GroupMemberListAcitivity.this.customList != null) {
				if (GroupMemberListAcitivity.this.editor_type != null && (GroupMemberListAcitivity.this.editor_type.equals("create_success") || GroupMemberListAcitivity.this.editor_type.equals("delete_success") || GroupMemberListAcitivity.this.editor_type.equals("add_success"))) {
					GroupMemberListAcitivity.this.updateCustomList();
				}
				infoList = GroupMemberListAcitivity.this.customList.getMember_list();
			}
			if (GroupMemberListAcitivity.this.currentGroupType == 1) {
				infoList = GroupMemberListAcitivity.this.moveCreatorToFirst(GroupMemberListAcitivity.this.customList.getGroupCreatorNum(), infoList);
			}
			GroupMemberListAcitivity.this.myHandler.sendMessage(GroupMemberListAcitivity.this.myHandler.obtainMessage(0, infoList));
		}
	}

	class C09535 implements DialogInterface.OnClickListener {
		C09535() {
		}

		public void onClick(DialogInterface dialog, int which) {
			GroupMemberListAcitivity.this.sendRequestMessage(3);
		}
	}

	class C09546 implements DialogInterface.OnClickListener {
		C09546() {
		}

		public void onClick(DialogInterface dialog, int which) {
		}
	}

	class C09557 implements DialogInterface.OnClickListener {
		C09557() {
		}

		public void onClick(DialogInterface dialog, int which) {
			GroupMemberListAcitivity.this.sendRequestMessage(5);
		}
	}

	class C09568 implements DialogInterface.OnClickListener {
		C09568() {
		}

		public void onClick(DialogInterface dialog, int which) {
		}
	}

	class C09579 implements OnTouchListener {
		C09579() {
		}

		public boolean onTouch(View v, MotionEvent event) {
			Drawable add_drawable;
			switch (event.getAction()) {
				case 0:
					add_drawable = GroupMemberListAcitivity.this.getResources().getDrawable(R.drawable.add_members_press);
					add_drawable.setBounds(0, 0, add_drawable.getMinimumWidth(), add_drawable.getMinimumHeight());
					GroupMemberListAcitivity.this.tv_grp_add.setCompoundDrawables(null, add_drawable, null, null);
					break;
				case 1:
					add_drawable = GroupMemberListAcitivity.this.getResources().getDrawable(R.drawable.add_members_nor);
					add_drawable.setBounds(0, 0, add_drawable.getMinimumWidth(), add_drawable.getMinimumHeight());
					GroupMemberListAcitivity.this.tv_grp_add.setCompoundDrawables(null, add_drawable, null, null);
					break;
			}
			return false;
		}
	}

	public GroupMemberListAcitivity() {
		this.permanentList = new ArrayList<GroupListInfo>();
		this.customList = new PttCustomGrp();
		this.currentGroupType = 0;
		this.editor_type = null;
		this.mmList = new ArrayList<Map<String, String>>();
	}

	private void InitTitle() {
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.back);
		this.tv_grp_name = (TextView) this.findViewById(R.id.title);
		(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				GroupMemberListAcitivity.this.finish();
			}
		});
		this.btn_left.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) GroupMemberListAcitivity.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) GroupMemberListAcitivity.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						GroupMemberListAcitivity.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(GroupMemberListAcitivity.this.getResources().getColor(R.color.font_color3));
						GroupMemberListAcitivity.this.btn_left.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
	}

//	static /* synthetic */ void access.1(
//	final GroupMemberListAcitivity groupMemberListAcitivity, final DepartmentAdapter grpMemberListAdapter)
//
//	{
//		groupMemberListAcitivity.grpMemberListAdapter = grpMemberListAdapter;
//	}

	private void getAdapterData() {
		LogUtil.makeLog(TAG, "getAdapterData()");
		CustomGroupUtil.getInstance().showProgressDialog(this.mContext, getResources().getString(R.string.progress_title), getResources().getString(R.string.progress_message));
		new Thread(new C09524()).start();
	}

	private List<Map<String, String>> getKeyGrpMembers(final List<Map<String, String>> list, final String s) {
		final ArrayList<Map<String, String>> list2 = new ArrayList<Map<String, String>>();
		for (final Map<String, String> map : list) {
			if (map.get("mname").contains(s) || map.get("mname").toLowerCase().contains(s.toLowerCase()) || map.get("number").contains(s)) {
				list2.add(map);
			}
		}
		return list2;
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

	private void initBottomLayout() {
		if (this.currentGroupType == 0) {
			this.choice_layout.setVisibility(View.GONE);
		} else if (this.currentGroupType == 1) {
			this.choice_layout.setVisibility(View.VISIBLE);
			final String groupCreatorNum = this.customList.getGroupCreatorNum();
			if (CustomGroupUtil.getInstance().isCustomGroupCreator(this.mContext, groupCreatorNum)) {
				this.tv_grp_add.setVisibility(View.VISIBLE);
				this.tv_grp_dissolve.setVisibility(View.VISIBLE);
				this.tv_grp_delete.setVisibility(View.VISIBLE);
				this.tv_grp_exit.setVisibility(View.GONE);
				return;
			}
			this.tv_grp_add.setVisibility(View.GONE);
			this.tv_grp_dissolve.setVisibility(View.GONE);
			this.tv_grp_delete.setVisibility(View.GONE);
			this.tv_grp_exit.setVisibility(View.VISIBLE);
			if (CustomGroupUtil.getInstance().isConsole(DataBaseService.getInstance().getMemberType(groupCreatorNum))) {
				this.tv_grp_exit.setTextColor(this.getResources().getColor(R.color.gray));
				final Drawable drawable = this.getResources().getDrawable(R.drawable.dissolve_nor);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				this.tv_grp_exit.setCompoundDrawables((Drawable) null, drawable, (Drawable) null, (Drawable) null);
				this.tv_grp_exit.setEnabled(false);
				return;
			}
			this.tv_grp_exit.setTextColor(this.getResources().getColor(R.color.black));
			final Drawable drawable2 = this.getResources().getDrawable(R.drawable.quit_group_nor);
			drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
			this.tv_grp_exit.setCompoundDrawables((Drawable) null, drawable2, (Drawable) null, (Drawable) null);
			this.tv_grp_exit.setEnabled(true);
		}
	}

	private void initViewsAndListeners() {
		this.tv_grp_add = (TextView) this.findViewById(R.id.grp_add);
		this.tv_grp_dissolve = (TextView) this.findViewById(R.id.grp_dissolve);
		this.tv_grp_delete = (TextView) this.findViewById(R.id.grp_delete);
		this.tv_grp_exit = (TextView) this.findViewById(R.id.grp_exit);
		this.choice_layout = (LinearLayout) this.findViewById(R.id.layout_bottom);
		this.listView = (ListView) this.findViewById(R.id.grp_member_listview);
		this.tv_grp_add.setOnClickListener((View.OnClickListener) this);
		this.tv_grp_dissolve.setOnClickListener((View.OnClickListener) this);
		this.tv_grp_delete.setOnClickListener((View.OnClickListener) this);
		this.tv_grp_exit.setOnClickListener((View.OnClickListener) this);
	}

	private boolean isCreator(final String s, final String s2) {
		return s.equals(s2);
	}

	private List<CustomGroupMemberInfo> moveCreatorToFirst(final String s, final List<CustomGroupMemberInfo> list) {
		if (list != null && list.size() > 1) {
			for (final CustomGroupMemberInfo customGroupMemberInfo : list) {
				if (customGroupMemberInfo.getMemberNum().equals(s)) {
					list.remove(customGroupMemberInfo);
					list.add(0, customGroupMemberInfo);
					return list;
				}
			}
		}
		return list;
	}

	private void sendRequestMessage(final int n) {
		this.userAgent = Receiver.GetCurUA();
		CustomGroupUtil.getInstance().showProgressDialog(this.mContext, this.getResources().getString(R.string.progress_title), this.getResources().getString(R.string.progress_message));
		final String groupStringInfo = CustomGroupManager.getInstance().getGroupStringInfo(this.customList);
		String currentUserNum = "";
		String groupNum = "";
		if (n == 5) {
			currentUserNum = CustomGroupUtil.getInstance().getCurrentUserNum(this.mContext);
			groupNum = this.customList.getGroupNum();
		}
		this.userAgent.SendCustomGroupMessage(n, groupStringInfo, null, currentUserNum, groupNum, null);
	}

	private void setOnTouchListener() {
		this.tv_grp_add.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					default: {
						return false;
					}
					case 0: {
						final Drawable drawable = GroupMemberListAcitivity.this.getResources().getDrawable(R.drawable.add_members_press);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
						GroupMemberListAcitivity.this.tv_grp_add.setCompoundDrawables((Drawable) null, drawable, (Drawable) null, (Drawable) null);
						return false;
					}
					case 1: {
						final Drawable drawable2 = GroupMemberListAcitivity.this.getResources().getDrawable(R.drawable.add_members_nor);
						drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
						GroupMemberListAcitivity.this.tv_grp_add.setCompoundDrawables((Drawable) null, drawable2, (Drawable) null, (Drawable) null);
						return false;
					}
				}
			}
		});
		this.tv_grp_delete.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					default: {
						return false;
					}
					case 0: {
						final Drawable drawable = GroupMemberListAcitivity.this.getResources().getDrawable(R.drawable.delete_members_press);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
						GroupMemberListAcitivity.this.tv_grp_delete.setCompoundDrawables((Drawable) null, drawable, (Drawable) null, (Drawable) null);
						return false;
					}
					case 1: {
						final Drawable drawable2 = GroupMemberListAcitivity.this.getResources().getDrawable(R.drawable.delete_members_nor);
						drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
						GroupMemberListAcitivity.this.tv_grp_delete.setCompoundDrawables((Drawable) null, drawable2, (Drawable) null, (Drawable) null);
						return false;
					}
				}
			}
		});
		this.tv_grp_dissolve.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					default: {
						return false;
					}
					case 0: {
						final Drawable drawable = GroupMemberListAcitivity.this.getResources().getDrawable(R.drawable.dissolve_press);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
						GroupMemberListAcitivity.this.tv_grp_dissolve.setCompoundDrawables((Drawable) null, drawable, (Drawable) null, (Drawable) null);
						return false;
					}
					case 1: {
						final Drawable drawable2 = GroupMemberListAcitivity.this.getResources().getDrawable(R.drawable.dissolve_nor);
						drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
						GroupMemberListAcitivity.this.tv_grp_dissolve.setCompoundDrawables((Drawable) null, drawable2, (Drawable) null, (Drawable) null);
						return false;
					}
				}
			}
		});
		this.tv_grp_exit.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					default: {
						return false;
					}
					case 0: {
						final Drawable drawable = GroupMemberListAcitivity.this.getResources().getDrawable(R.drawable.quit_group_press);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
						GroupMemberListAcitivity.this.tv_grp_exit.setCompoundDrawables((Drawable) null, drawable, (Drawable) null, (Drawable) null);
						return false;
					}
					case 1: {
						final Drawable drawable2 = GroupMemberListAcitivity.this.getResources().getDrawable(R.drawable.quit_group_nor);
						drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
						GroupMemberListAcitivity.this.tv_grp_exit.setCompoundDrawables((Drawable) null, drawable2, (Drawable) null, (Drawable) null);
						return false;
					}
				}
			}
		});
	}

	private List<CustomGroupMemberInfo> sortOnLineMembers(final List<CustomGroupMemberInfo> list) {
		new ArrayList();
		if (list != null && list.size() > 1) {
			final ArrayList<CustomGroupMemberInfo> list2 = new ArrayList<CustomGroupMemberInfo>();
			final ArrayList<CustomGroupMemberInfo> list3 = new ArrayList<CustomGroupMemberInfo>();
			for (int i = 0; i < list.size(); ++i) {
				final CustomGroupMemberInfo customGroupMemberInfo = list.get(i);
				if (customGroupMemberInfo.getMemberStatus().equals("0")) {
					list3.add(customGroupMemberInfo);
				} else {
					list2.add(customGroupMemberInfo);
				}
			}
			if (list3.size() > 0) {
				for (int j = 0; j < list3.size(); ++j) {
					list2.add((CustomGroupMemberInfo) list3.get(j));
				}
			}
			return list2;
		}
		return list;
	}

	private void updateCustomList() {
		this.userAgent = Receiver.GetCurUA();
		final PttCustomGrp customList = this.userAgent.getAllCustomGroups().get(this.userAgent.getCustomGroupMap().get(this.currentGrpName));
		if (customList != null) {
			this.customList = customList;
			if (this.customList.getMember_list().size() > 0 && this.customList.getMember_list().get(0).getMemberStatus() == null) {
				this.updateCustomList();
			}
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
			default: {
			}
			case R.id.back: {
				this.startActivity(new Intent(this.mContext, (Class) MainActivity.class));
				if (this.currentGroupType == 1) {
					this.sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO));
				}
				this.finish();
			}
			case R.id.grp_add: {
				final Intent intent = new Intent(this.mContext, (Class) EditGroupMemberActivity.class);
				final Bundle bundle = new Bundle();
				bundle.putString("type", "add");
				bundle.putString("custom_grp_name", this.tv_grp_name.getText().toString().trim());
				bundle.putSerializable("current_grp_info", (Serializable) this.customList);
				intent.putExtras(bundle);
				this.startActivity(intent);
			}
			case R.id.grp_dissolve: {
				Builder builder = new Builder(this.mContext);
				builder.setTitle(R.string.grp_dissolve);
				builder.setMessage(R.string.grp_clear);
				builder.setPositiveButton(R.string.dissolve, new C09535());
				builder.setNegativeButton(R.string.custom_grp_cancel, new C09546()).show();
			}
			case R.id.grp_delete: {
				final Intent intent2 = new Intent(this.mContext, (Class) EditGroupMemberActivity.class);
				final Bundle bundle2 = new Bundle();
				bundle2.putString("type", "delete");
				bundle2.putString("custom_grp_name", this.tv_grp_name.getText().toString().trim());
				bundle2.putSerializable("current_grp_info", (Serializable) this.customList);
				intent2.putExtras(bundle2);
				this.startActivity(intent2);
			}
			case R.id.grp_exit: {
				Builder dialog = new Builder(this.mContext);
				dialog.setTitle(R.string.custom_grp_exit);
				dialog.setMessage(R.string.custom_grp_exit_tip);
				dialog.setPositiveButton(R.string.Exit, new C09557());
				dialog.setNegativeButton(R.string.custom_grp_cancel, new C09568()).show();
			}
		}
	}

	@Override
	protected void onCreate(Bundle extras) {
		super.onCreate(extras);
		LogUtil.makeLog("GroupMemberListAcitivity", "onCreate()");
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.contact_grpmember_list);
		this.mContext = (Context) this;
		this.userAgent = Receiver.GetCurUA();
		this.customGroupStateReceiver = new CustomGroupStateReceiver();
		(this.intentFilter = new IntentFilter()).addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_INFO_CHANGED);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_DESTROY_SUCCESS);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_DESTROY_FAILURE);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_LEAVE_SUCCESS);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_LEAVE_FAILURE);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_REQUEST_TIME_OUT);
		this.registerReceiver((BroadcastReceiver) this.customGroupStateReceiver, this.intentFilter);
		this.initViewsAndListeners();
		this.setOnTouchListener();
		this.InitTitle();
		extras = this.getIntent().getExtras();
		if (extras != null) {
			this.currentGrpName = extras.getString("grp_name");
			if (this.currentGrpName != null && !this.currentGrpName.equals("")) {
				this.tv_grp_name.setText((CharSequence) this.currentGrpName);
			}
			final String string = extras.getString("grp_type");
			if (string != null && !string.equals("")) {
				if (string.equals("permanent")) {
					this.currentGroupType = 0;
					this.permanentList = (List<GroupListInfo>) this.getIntent().getSerializableExtra("permanent_member_info");
				} else if (string.equals("custom")) {
					this.currentGroupType = 1;
					this.customList = (PttCustomGrp) this.getIntent().getSerializableExtra("custom_member_info");
					if (this.customList == null) {
						this.customList = new PttCustomGrp();
						MyLog.e("ErrorLog", "customList == null,error Happened!!!");
					}
					final Map<String, PttCustomGrp> customGrpMap = this.userAgent.GetAllGrps().getCustomGrpMap();
					final PttCustomGrp customList = customGrpMap.get(this.customList.getGroupNum());
					if (customList != null && customGrpMap != null && this.customList.getMember_list() != null && customList.getMember_list() != null && this.customList.getMember_list().size() != customList.getMember_list().size()) {
						this.customList = customList;
						this.sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO));
					}
					this.editor_type = extras.getString("editor_type");
					if (this.editor_type != null) {
						LogUtil.makeLog("GroupMemberListAcitivity", this.editor_type);
						if (this.editor_type.equals("create_success")) {
							this.userAgent.addCustomGroupLength();
							CustomGroupUtil.getInstance().showToast(this.mContext, R.string.create_success);
						} else if (this.editor_type.equals("add_success")) {
							CustomGroupUtil.getInstance().showToast(this.mContext, R.string.add_success);
						} else if (this.editor_type.equals("delete_success")) {
							CustomGroupUtil.getInstance().showToast(this.mContext, R.string.delete_success);
							(this.userAgent = Receiver.GetCurUA()).SendCustomGroupMessage(7, null, null, null, this.customList.getGroupNum(), null);
						}
					}
				}
			}
		}
		(this.searchView = (SearchView) this.findViewById(R.id.msearch_view)).setOnQueryTextListener((SearchView.OnQueryTextListener) this);
		this.initBottomLayout();
		this.getAdapterData();
	}

	protected void onDestroy() {
		super.onDestroy();
		LogUtil.makeLog("GroupMemberListAcitivity", "onDestroy()");
		if (this.intentFilter != null) {
			this.unregisterReceiver((BroadcastReceiver) this.customGroupStateReceiver);
		}
	}

	@Override
	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		if (n == 4) {
			this.startActivity(new Intent(this.mContext, (Class) MainActivity.class));
			if (this.currentGroupType == 1) {
				this.sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO));
			}
		}
		return super.onKeyDown(n, keyEvent);
	}

	public boolean onQueryTextChange(final String s) {
		if (!TextUtils.isEmpty((CharSequence) s)) {
			final List<Map<String, String>> keyGrpMembers = this.getKeyGrpMembers(this.mmList, s);
			if (keyGrpMembers.size() >= 0) {
				final DepartmentAdapter adapter = new DepartmentAdapter(this.mContext, s, DataBaseService.getInstance());
				adapter.getData(keyGrpMembers);
				this.listView.setAdapter((ListAdapter) adapter);
				return false;
			}
		} else if (this.grpMemberListAdapter != null) {
			this.listView.setAdapter((ListAdapter) this.grpMemberListAdapter);
			return false;
		}
		return false;
	}

	@SuppressLint("WrongConstant")
	public boolean onQueryTextSubmit(final String s) {
		// TODO delete @SuppressLint("WrongConstant")
		((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		return false;
	}

	protected void onResume() {
		super.onResume();
		LogUtil.makeLog("GroupMemberListAcitivity", "onResume()");
	}

	public void refreshAdapter() {
		LogUtil.makeLog("GroupMemberListAcitivity", "refreshAdapter()");
		this.userAgent = Receiver.GetCurUA();
		final PttCustomGrp customList = this.userAgent.getAllCustomGroups().get(this.userAgent.getCustomGroupMap().get(this.currentGrpName));
		if (customList != null) {
			this.customList = customList;
			this.moveCreatorToFirst(this.customList.getGroupCreatorNum(), this.customList.getMember_list());
		}
	}

	public void updateInfo() {
		LogUtil.makeLog("GroupMemberListAcitivity", "updateInfo()");
		if (this.customList != null) {
			final String groupNum = this.customList.getGroupNum();
			CustomGroupUtil.getInstance().deleteElementFromCustomGrpMap(groupNum, this.customList.getGroupName());
			CustomGroupUtil.getInstance().deleteElementFromGroupListMap(groupNum);
		}
		this.startActivity(new Intent(this.mContext, (Class) MainActivity.class));
		this.sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO));
		this.finish();
	}

	public class CustomGroupStateReceiver extends BroadcastReceiver {
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (action != null) {
				LogUtil.makeLog("GroupMemberListAcitivity", "CustomGroupStateReceiver#onReceive() " + action);
				final Message obtain = Message.obtain();
				if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_INFO_CHANGED) || action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO)) {
					obtain.what = 1;
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_DESTROY_SUCCESS)) {
					obtain.what = 2;
					obtain.arg1 = 1;
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_DESTROY_FAILURE)) {
					obtain.what = 2;
					obtain.arg1 = 0;
					obtain.arg2 = intent.getIntExtra("reasonCode", 488);
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_LEAVE_SUCCESS)) {
					obtain.what = 3;
					obtain.arg1 = 1;
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_LEAVE_FAILURE)) {
					obtain.what = 3;
					obtain.arg1 = 0;
					obtain.arg2 = intent.getIntExtra("reasonCode", 488);
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_REQUEST_TIME_OUT)) {
					obtain.what = 4;
				}
				GroupMemberListAcitivity.this.myHandler.sendMessage(obtain);
			}
		}
	}

	private static class ViewHolder {
		private ImageView iv_call;
		private ImageView iv_msg;
		private ImageView iv_video;
		private TextView member_name;
		private TextView member_number;
		private ImageView member_type;
	}
}
