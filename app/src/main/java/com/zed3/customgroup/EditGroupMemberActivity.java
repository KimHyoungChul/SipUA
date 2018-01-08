package com.zed3.customgroup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EditGroupMemberActivity extends BaseActivity implements OnClickListener, OnQueryTextListener, OnCloseListener {
	private static final int ADD = 2;
	private static final int CREATE = 1;
	private static final int DELETE = 3;
	private static final int GET_DATA = 0;
	private static final String TAG = "EditGroupMemberActivity";
	private static final int TIME_OUT = 4;
	private static int type_flag = 0;
	private int canSelectedMembers = 0;
	private String createId = null;
	private String currentUserName;
	private String currentUserNum;
	private CustomGroupStateReceiver customGroupStateReceiver;
	private Handler editorHandler = new C09441();
	private GroupMemberAdapter groupMemberAdapter;
	private String grp_name;
	private int hasSelectedMembers = 0;
	private IntentFilter intentFilter;
	private ListView listView;
	private List<GroupInfoItem> mCheckdedMemberList = new ArrayList();
	private Context mContext;
	private List<GroupInfoItem> mInitialMemberInfos = new ArrayList();
	private PttCustomGrp pttCustomGrp = new PttCustomGrp();
	private SearchView searchView;
	private TextView tv_cancel;
	private TextView tv_ok;
	private TextView tv_title;

	static {
		EditGroupMemberActivity.type_flag = 0;
	}

	class C09441 extends Handler {
		C09441() {
		}

		public void handleMessage(Message msg) {
			CustomGroupUtil.getInstance().dismissProgressDialog();
			int reasonCode;
			Intent intent;
			switch (msg.what) {
				case 0:
					EditGroupMemberActivity.this.mInitialMemberInfos = EditGroupMemberActivity.this.getAdapterData((List) msg.obj);
					EditGroupMemberActivity.this.setAdapter(EditGroupMemberActivity.this.mInitialMemberInfos);
					return;
				case 1:
					if (msg.arg1 == 0) {
						reasonCode = msg.arg2;
						if (reasonCode == 488 || reasonCode == 480) {
							CustomGroupUtil.getInstance().showToast(EditGroupMemberActivity.this.mContext, (int) R.string.create_failure);
							return;
						} else {
							CustomGroupUtil.getInstance().showFailureReason(EditGroupMemberActivity.this.mContext, reasonCode);
							return;
						}
					} else if (msg.arg1 == 1) {
						intent = new Intent(EditGroupMemberActivity.this.mContext, GroupMemberListAcitivity.class);
						Bundle bundle1 = new Bundle();
						bundle1.putString("editor_type", "create_success");
						bundle1.putString("grp_name", EditGroupMemberActivity.this.grp_name);
						bundle1.putString("grp_type", "custom");
						bundle1.putSerializable("custom_member_info", EditGroupMemberActivity.this.getEditedCustomGroupInfo());
						intent.putExtras(bundle1);
						EditGroupMemberActivity.this.startActivity(intent);
						EditGroupMemberActivity.this.finish();
						return;
					} else {
						return;
					}
				case 2:
					if (msg.arg1 == 0) {
						reasonCode = msg.arg2;
						if (reasonCode == 488 || reasonCode == 480) {
							CustomGroupUtil.getInstance().showToast(EditGroupMemberActivity.this.mContext, (int) R.string.add_failure);
							return;
						} else {
							CustomGroupUtil.getInstance().showFailureReason(EditGroupMemberActivity.this.mContext, reasonCode);
							return;
						}
					} else if (msg.arg1 == 1) {
						intent = new Intent(EditGroupMemberActivity.this.mContext, GroupMemberListAcitivity.class);
						Bundle bundle2 = new Bundle();
						bundle2.putString("editor_type", "add_success");
						bundle2.putString("grp_name", EditGroupMemberActivity.this.grp_name);
						bundle2.putString("grp_type", "custom");
						bundle2.putSerializable("custom_member_info", EditGroupMemberActivity.this.getEditedCustomGroupInfo());
						intent.putExtras(bundle2);
						EditGroupMemberActivity.this.startActivity(intent);
						EditGroupMemberActivity.this.finish();
						return;
					} else {
						return;
					}
				case 3:
					if (msg.arg1 == 0) {
						reasonCode = msg.arg2;
						if (reasonCode == 488 || reasonCode == 480) {
							CustomGroupUtil.getInstance().showToast(EditGroupMemberActivity.this.mContext, (int) R.string.delete_failure);
							return;
						} else {
							CustomGroupUtil.getInstance().showFailureReason(EditGroupMemberActivity.this.mContext, reasonCode);
							return;
						}
					} else if (msg.arg1 == 1) {
						intent = new Intent(EditGroupMemberActivity.this.mContext, GroupMemberListAcitivity.class);
						Bundle bundle3 = new Bundle();
						bundle3.putString("editor_type", "delete_success");
						bundle3.putString("grp_name", EditGroupMemberActivity.this.grp_name);
						bundle3.putString("grp_type", "custom");
						bundle3.putSerializable("custom_member_info", EditGroupMemberActivity.this.getEditedCustomGroupInfo());
						intent.putExtras(bundle3);
						EditGroupMemberActivity.this.startActivity(intent);
						EditGroupMemberActivity.this.finish();
						return;
					} else {
						return;
					}
				case 4:
					CustomGroupUtil.getInstance().showToast(EditGroupMemberActivity.this.mContext, (int) R.string.time_out);
					return;
				default:
					return;
			}
		}
	}

	class C09452 implements Runnable {
		C09452() {
		}

		public void run() {
			List<GroupInfoItem> allMemberInfos = EditGroupMemberActivity.this.getAllMemberInfosFromDB();
			Message message = Message.obtain();
			message.what = 0;
			message.obj = allMemberInfos;
			EditGroupMemberActivity.this.editorHandler.sendMessage(message);
		}
	}

	class C09463 implements OnItemClickListener {
		C09463() {
		}

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			CheckBox checkBox = (CheckBox) view.findViewById(R.id.grp_img);
			if (EditGroupMemberActivity.type_flag == 2 || checkBox.isChecked() || EditGroupMemberActivity.this.hasSelectedMembers < EditGroupMemberActivity.this.canSelectedMembers) {
				checkBox.toggle();
				boolean isChecked = checkBox.isChecked();
				EditGroupMemberActivity.this.groupMemberAdapter.getSelectMap().put(Integer.valueOf(position), Boolean.valueOf(isChecked));
				EditGroupMemberActivity editGroupMemberActivity;
				if (isChecked) {
					editGroupMemberActivity = EditGroupMemberActivity.this;
					editGroupMemberActivity.hasSelectedMembers = editGroupMemberActivity.hasSelectedMembers + 1;
				} else {
					editGroupMemberActivity = EditGroupMemberActivity.this;
					editGroupMemberActivity.hasSelectedMembers = editGroupMemberActivity.hasSelectedMembers - 1;
				}
				EditGroupMemberActivity.this.setSelectedCount(EditGroupMemberActivity.this.hasSelectedMembers, EditGroupMemberActivity.this.canSelectedMembers);
				GroupInfoItem mGroupInfoItem = (GroupInfoItem) parent.getItemAtPosition(position);
				if (EditGroupMemberActivity.this.mCheckdedMemberList.contains(mGroupInfoItem)) {
					((GroupInfoItem) EditGroupMemberActivity.this.mCheckdedMemberList.get(EditGroupMemberActivity.this.mCheckdedMemberList.indexOf(mGroupInfoItem))).setGrp_img(isChecked);
				} else {
					mGroupInfoItem.setGrp_img(isChecked);
					EditGroupMemberActivity.this.mCheckdedMemberList.add(mGroupInfoItem);
				}
				LogUtil.makeLog(EditGroupMemberActivity.TAG, "onItemClick()#type:" + EditGroupMemberActivity.type_flag + " hasSelectedMembers:" + EditGroupMemberActivity.this.hasSelectedMembers + " canSelectedMembers:" + EditGroupMemberActivity.this.canSelectedMembers);
				return;
			}
			CustomGroupUtil.getInstance().showToast(EditGroupMemberActivity.this.mContext, (int) R.string.add_member_error);
		}
	}

//	static /* synthetic */ void access.0(
//	final EditGroupMemberActivity editGroupMemberActivity, final List mInitialMemberInfos)
//
//	{
//		editGroupMemberActivity.mInitialMemberInfos = (List<GroupInfoItem>) mInitialMemberInfos;
//	}
//
//	static /* synthetic */ void access.10(
//	final EditGroupMemberActivity editGroupMemberActivity, final int hasSelectedMembers)
//
//	{
//		editGroupMemberActivity.hasSelectedMembers = hasSelectedMembers;
//	}

	private List<GroupInfoItem> deleteCurrentUserFromList(final List<GroupInfoItem> list) {
		final ArrayList<GroupInfoItem> list2 = new ArrayList<GroupInfoItem>();
		for (final GroupInfoItem groupInfoItem : list) {
			if (!groupInfoItem.getGrp_uNumber().equals(this.currentUserNum)) {
				list2.add(groupInfoItem);
			}
		}
		return list2;
	}

	private String getMemberStringInfo() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.mCheckdedMemberList.size(); ++i) {
			final GroupInfoItem groupInfoItem = this.mCheckdedMemberList.get(i);
			if (groupInfoItem.isGrp_img()) {
				sb.append(groupInfoItem.getGrp_uNumber()).append(";");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.lastIndexOf(";"));
		}
		String s2;
		final String s = s2 = sb.toString().trim();
		if (EditGroupMemberActivity.type_flag == 0) {
			s2 = String.valueOf(this.currentUserNum) + ";" + s;
		}
		LogUtil.makeLog("EditGroupMemberActivity", "getMemberStringInfo()#memberNumberStr = " + s2);
		return s2;
	}

	private void handleEditGroupMembers() {
		LogUtil.makeLog("EditGroupMemberActivity", "handleEditGroupMembers() type_flag = " + EditGroupMemberActivity.type_flag);
		CustomGroupUtil.getInstance().showProgressDialog(this.mContext, this.getResources().getString(R.string.progress_title), this.getResources().getString(R.string.progress_message));
		final UserAgent getCurUA = Receiver.GetCurUA();
		if (getCurUA != null) {
			final String memberStringInfo = this.getMemberStringInfo();
			switch (EditGroupMemberActivity.type_flag) {
				case 0: {
					final StringBuilder sb = new StringBuilder();
					sb.append(this.currentUserNum).append(",").append(this.currentUserName).append(",,").append(this.grp_name);
					this.createId = getCurUA.SendCustomGroupMessage(0, sb.toString().trim(), memberStringInfo, null, null, this.createId);
				}
				case 1: {
					getCurUA.SendCustomGroupMessage(1, CustomGroupManager.getInstance().getGroupStringInfo(this.pttCustomGrp), memberStringInfo, null, null, null);
				}
				case 2: {
					getCurUA.SendCustomGroupMessage(2, CustomGroupManager.getInstance().getGroupStringInfo(this.pttCustomGrp), memberStringInfo, null, null, null);
				}
			}
		}
	}

	private void initViewsAndListeners() {
		this.tv_ok = (TextView) this.findViewById(R.id.tv_ok);
		this.tv_cancel = (TextView) this.findViewById(R.id.tv_cancel);
		this.tv_title = (TextView) this.findViewById(R.id.tv_title);
		this.searchView = (SearchView) this.findViewById(R.id.search_view);
		this.listView = (ListView) this.findViewById(R.id.grp_listview);
		this.tv_ok.setOnClickListener((View.OnClickListener) this);
		this.tv_cancel.setOnClickListener((View.OnClickListener) this);
		this.searchView.setOnQueryTextListener((SearchView.OnQueryTextListener) this);
		this.searchView.setOnCloseListener((SearchView.OnCloseListener) this);
	}

	private void setAdapter(List<GroupInfoItem> data) {
		setSelectedCount(this.hasSelectedMembers, this.canSelectedMembers);
		this.groupMemberAdapter = new GroupMemberAdapter(this.mContext);
		this.groupMemberAdapter.setData(data);
		this.listView.setAdapter(this.groupMemberAdapter);
		this.listView.setOnItemClickListener(new C09463());
		CustomGroupUtil.getInstance().dismissProgressDialog();
	}

	private void refreshAdapter(List<GroupInfoItem> data, String searchWord) {
		if (this.groupMemberAdapter != null) {
			this.groupMemberAdapter.search_word = searchWord;
			this.groupMemberAdapter.setData(data);
			this.groupMemberAdapter.notifyDataSetChanged();
		}
	}

	public List<GroupInfoItem> getAdapterData(List<GroupInfoItem> list) {
		LogUtil.makeLog(TAG, "getAdapterData()#type:" + type_flag);
		List<GroupInfoItem> data = new ArrayList();
		List<CustomGroupMemberInfo> currentMemberInfos;
		switch (type_flag) {
			case 0:
				data = deleteCurrentUserFromList(list);
				this.hasSelectedMembers = 0;
				this.canSelectedMembers = 39;
				return data;
			case 1:
				if (this.pttCustomGrp == null) {
					return data;
				}
				currentMemberInfos = this.pttCustomGrp.getMember_list();
				if (currentMemberInfos == null || currentMemberInfos.size() <= 0) {
					return data;
				}
				List<GroupInfoItem> currentMembers = getCurrentMembersByNumbers(list, currentMemberInfos);
				data = deleteCurrentUserFromList(getUnCurrentMembersByNumbers(list, currentMembers));
				this.hasSelectedMembers = 0;
				this.canSelectedMembers = 40 - currentMembers.size();
				return data;
			case 2:
				if (this.pttCustomGrp == null) {
					return data;
				}
				currentMemberInfos = this.pttCustomGrp.getMember_list();
				if (currentMemberInfos == null || currentMemberInfos.size() <= 0) {
					return data;
				}
				data = deleteCurrentUserFromList(getCurrentMembersByNumbers(list, currentMemberInfos));
				this.canSelectedMembers = data.size();
				return data;
			default:
				return data;
		}
	}

	public List<GroupInfoItem> getAllMemberInfosFromDB() {
		return DataBaseService.getInstance().getAllMembers();
	}

	public List<GroupInfoItem> getCurrentMembersByNumbers(final List<GroupInfoItem> list, final List<CustomGroupMemberInfo> list2) {
		final ArrayList<GroupInfoItem> list3 = new ArrayList<GroupInfoItem>();
		for (int i = 0; i < list2.size(); ++i) {
			final GroupInfoItem groupInfoItem = new GroupInfoItem();
			groupInfoItem.setGrp_uNumber(list2.get(i).getMemberNum());
			groupInfoItem.setGrp_uName(list2.get(i).getMemberName());
			list3.add(groupInfoItem);
		}
		return list3;
	}

	public PttCustomGrp getEditedCustomGroupInfo() {
		final PttCustomGrp pttCustomGrp = new PttCustomGrp();
		if (EditGroupMemberActivity.type_flag == 0) {
			pttCustomGrp.setGroupCreatorName(this.currentUserName);
			pttCustomGrp.setGroupCreatorNum(this.currentUserNum);
			pttCustomGrp.setGroupName(this.grp_name);
			pttCustomGrp.setGroupNum("");
		} else if (this.pttCustomGrp != null) {
			pttCustomGrp.setGroupCreatorName(this.pttCustomGrp.getGroupCreatorName());
			pttCustomGrp.setGroupCreatorNum(this.pttCustomGrp.getGroupCreatorNum());
			pttCustomGrp.setGroupName(this.pttCustomGrp.getGroupName());
			pttCustomGrp.setGroupNum(this.pttCustomGrp.getGroupNum());
		}
		final ArrayList<CustomGroupMemberInfo> list = new ArrayList<CustomGroupMemberInfo>();
		List<CustomGroupMemberInfo> member_list;
		if (EditGroupMemberActivity.type_flag == 0) {
			final CustomGroupMemberInfo customGroupMemberInfo = new CustomGroupMemberInfo();
			customGroupMemberInfo.setMemberName(this.currentUserName);
			customGroupMemberInfo.setMemberNum(this.currentUserNum);
			customGroupMemberInfo.setMemberStatus("3");
			list.add(customGroupMemberInfo);
			for (int i = 0; i < this.mCheckdedMemberList.size(); ++i) {
				final GroupInfoItem groupInfoItem = this.mCheckdedMemberList.get(i);
				if (groupInfoItem.isGrp_img()) {
					final CustomGroupMemberInfo customGroupMemberInfo2 = new CustomGroupMemberInfo();
					customGroupMemberInfo2.setMemberName(groupInfoItem.getGrp_uName());
					customGroupMemberInfo2.setMemberNum(groupInfoItem.getGrp_uNumber());
					customGroupMemberInfo2.setMemberStatus(CustomGroupUtil.getInstance().getStringByResId(this.mContext, R.string.the_status_1));
					list.add(customGroupMemberInfo2);
				}
			}
			member_list = list;
		} else if (EditGroupMemberActivity.type_flag == 1) {
			final List<CustomGroupMemberInfo> member_list2 = this.pttCustomGrp.getMember_list();
			int n = 0;
			while (true) {
				member_list = member_list2;
				if (n >= this.mCheckdedMemberList.size()) {
					break;
				}
				final GroupInfoItem groupInfoItem2 = this.mCheckdedMemberList.get(n);
				if (groupInfoItem2.isGrp_img()) {
					final CustomGroupMemberInfo customGroupMemberInfo3 = new CustomGroupMemberInfo();
					customGroupMemberInfo3.setMemberName(groupInfoItem2.getGrp_uName());
					customGroupMemberInfo3.setMemberNum(groupInfoItem2.getGrp_uNumber());
					customGroupMemberInfo3.setMemberStatus(CustomGroupUtil.getInstance().getStringByResId(this.mContext, R.string.the_status_1));
					member_list2.add(customGroupMemberInfo3);
				}
				++n;
			}
		} else {
			member_list = list;
			if (EditGroupMemberActivity.type_flag == 2) {
				final CustomGroupMemberInfo customGroupMemberInfo4 = new CustomGroupMemberInfo();
				customGroupMemberInfo4.setMemberName(this.pttCustomGrp.getGroupCreatorName());
				customGroupMemberInfo4.setMemberNum(this.pttCustomGrp.getGroupCreatorNum());
				customGroupMemberInfo4.setMemberStatus("3");
				list.add(customGroupMemberInfo4);
				for (int j = 0; j < this.mCheckdedMemberList.size(); ++j) {
					final GroupInfoItem groupInfoItem3 = this.mCheckdedMemberList.get(j);
					if (groupInfoItem3.isGrp_img()) {
						this.mInitialMemberInfos.remove(groupInfoItem3);
					}
				}
				final Iterator<GroupInfoItem> iterator = this.mInitialMemberInfos.iterator();
				while (true) {
					member_list = list;
					if (!iterator.hasNext()) {
						break;
					}
					final GroupInfoItem groupInfoItem4 = iterator.next();
					final CustomGroupMemberInfo customGroupMemberInfo5 = new CustomGroupMemberInfo();
					customGroupMemberInfo5.setMemberName(groupInfoItem4.getGrp_uName());
					customGroupMemberInfo5.setMemberNum(groupInfoItem4.getGrp_uNumber());
					customGroupMemberInfo5.setMemberStatus(CustomGroupUtil.getInstance().getStringByResId(this.mContext, R.string.the_status_1));
					list.add(customGroupMemberInfo5);
				}
			}
		}
		pttCustomGrp.setMember_list(member_list);
		return pttCustomGrp;
	}

	public List<GroupInfoItem> getUnCurrentMembersByNumbers(final List<GroupInfoItem> list, final List<GroupInfoItem> list2) {
		final ArrayList<GroupInfoItem> list3 = new ArrayList<GroupInfoItem>();
		int i = 0;
		Label_0011:
		while (i < list.size()) {
			final GroupInfoItem groupInfoItem = list.get(i);
			final boolean b = false;
			while (true) {
				for (int j = 0; j < list2.size(); ++j) {
					if (groupInfoItem.getGrp_uNumber().equals(list2.get(j).getGrp_uNumber())) {
						final int n = 1;
						if (n == 0) {
							list3.add(groupInfoItem);
						}
						++i;
						continue Label_0011;
					}
				}
				final int n = b ? 1 : 0;
				continue;
			}
		}
		return list3;
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			default: {
			}
			case R.id.tv_ok: {
				if (this.hasSelectedMembers == 0 && EditGroupMemberActivity.type_flag != 0) {
					this.finish();
					return;
				}
				this.handleEditGroupMembers();
			}
			case R.id.tv_cancel: {
				if (EditGroupMemberActivity.type_flag == 0) {
					this.startActivity(new Intent(this.mContext, (Class) MainActivity.class));
					return;
				}
				this.finish();
			}
		}
	}

	public boolean onClose() {
		LogUtil.makeLog("EditGroupMemberActivity", "onClose()");
		this.refreshAdapter(this.mInitialMemberInfos, null);
		return true;
	}

	@Override
	protected void onCreate(Bundle extras) {
		LogUtil.makeLog("EditGroupMemberActivity", "onCreate()");
		super.onCreate(extras);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.contact_edit_grpmember);
		this.mContext = (Context) this;
		this.currentUserName = CustomGroupUtil.getInstance().getCurrentUserDisplayName(this.mContext);
		this.currentUserNum = CustomGroupUtil.getInstance().getCurrentUserNum(this.mContext);
		this.customGroupStateReceiver = new CustomGroupStateReceiver();
		(this.intentFilter = new IntentFilter()).addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_CREATE_SUCCESS);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_CREATE_FAILURE);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_ADD_SUCCESS);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_ADD_FAILURE);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_DELETE_SUCCESS);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_DELETE_FAILURE);
		this.intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_REQUEST_TIME_OUT);
		this.registerReceiver((BroadcastReceiver) this.customGroupStateReceiver, this.intentFilter);
		this.initViewsAndListeners();
		extras = this.getIntent().getExtras();
		if (extras != null) {
			this.grp_name = extras.getString("custom_grp_name");
			final String string = extras.getString("type");
			if (string != null) {
				if (string.equals("create")) {
					EditGroupMemberActivity.type_flag = 0;
					this.tv_title.setText((CharSequence) this.getResources().getString(R.string.custom_grp_add));
				} else if (string.equals("add")) {
					EditGroupMemberActivity.type_flag = 1;
					this.tv_title.setText((CharSequence) this.getResources().getString(R.string.custom_grp_add));
					this.pttCustomGrp = (PttCustomGrp) this.getIntent().getSerializableExtra("current_grp_info");
				} else if (string.equals("delete")) {
					EditGroupMemberActivity.type_flag = 2;
					this.tv_title.setText((CharSequence) this.getResources().getString(R.string.grp_delete));
					this.pttCustomGrp = (PttCustomGrp) this.getIntent().getSerializableExtra("current_grp_info");
				}
			}
		}
		CustomGroupUtil.getInstance().showProgressDialog(this.mContext, this.getResources().getString(R.string.progress_title), this.getResources().getString(R.string.progress_message));
		new Thread(new Runnable() {
			@Override
			public void run() {
				final List<GroupInfoItem> allMemberInfosFromDB = EditGroupMemberActivity.this.getAllMemberInfosFromDB();
				final Message obtain = Message.obtain();
				obtain.what = 0;
				obtain.obj = allMemberInfosFromDB;
				EditGroupMemberActivity.this.editorHandler.sendMessage(obtain);
			}
		}).start();
	}

	protected void onDestroy() {
		LogUtil.makeLog("EditGroupMemberActivity", "onDestroy()");
		super.onDestroy();
		if (this.intentFilter != null) {
			this.unregisterReceiver((BroadcastReceiver) this.customGroupStateReceiver);
		}
	}

	@Override
	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		if (n == 4 && EditGroupMemberActivity.type_flag == 0) {
			this.startActivity(new Intent(this.mContext, (Class) MainActivity.class));
		}
		return super.onKeyDown(n, keyEvent);
	}

	public boolean onQueryTextChange(final String s) {
		LogUtil.makeLog("EditGroupMemberActivity", "onQueryTextChange()");
		List<GroupInfoItem> list;
		if (!TextUtils.isEmpty((CharSequence) s)) {
			list = CustomGroupUtil.getInstance().searchListBykeyWord(s, this.mInitialMemberInfos);
		} else {
			list = this.mInitialMemberInfos;
		}
		this.refreshAdapter(list, s);
		return true;
	}

	public boolean onQueryTextSubmit(final String s) {
		return false;
	}

	public void setSelectedCount(final int n, final int n2) {
		this.tv_ok.setText((CharSequence) (String.valueOf(this.getResources().getString(R.string.custom_grp_ok)) + "(" + n + "/" + n2 + ")"));
	}

	public class CustomGroupStateReceiver extends BroadcastReceiver {
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (action != null) {
				LogUtil.makeLog("EditGroupMemberActivity", "CustomGroupStateReceiver#onReceive() " + action);
				final Message obtain = Message.obtain();
				if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_CREATE_SUCCESS)) {
					obtain.what = 1;
					obtain.arg1 = 1;
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_CREATE_FAILURE)) {
					obtain.what = 1;
					obtain.arg1 = 0;
					obtain.arg2 = intent.getIntExtra("reasonCode", 488);
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_ADD_SUCCESS)) {
					obtain.what = 2;
					obtain.arg1 = 1;
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_ADD_FAILURE)) {
					obtain.what = 2;
					obtain.arg1 = 0;
					obtain.arg2 = intent.getIntExtra("reasonCode", 488);
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_DELETE_SUCCESS)) {
					obtain.what = 3;
					obtain.arg1 = 1;
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_DELETE_FAILURE)) {
					obtain.what = 3;
					obtain.arg1 = 0;
					obtain.arg2 = intent.getIntExtra("reasonCode", 488);
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_REQUEST_TIME_OUT)) {
					obtain.what = 4;
				}
				EditGroupMemberActivity.this.editorHandler.sendMessage(obtain);
			}
		}
	}

	public class GroupMemberAdapter extends BaseAdapter {
		private Context mContext;
		private List<GroupInfoItem> mList = new ArrayList();
		private String search_word;
		private Map<Integer, Boolean> selectMap = new HashMap();

		public GroupMemberAdapter(Context context) {
			this.mContext = context;
			this.search_word = "";
		}

		public void setData(List<GroupInfoItem> list) {
			this.mList = list;
			initSelectState(list);
		}

		public void initSelectState(List<GroupInfoItem> list) {
			if (list != null) {
				this.selectMap.clear();
				for (int i = 0; i < list.size(); i++) {
					this.selectMap.put(Integer.valueOf(i), Boolean.valueOf(((GroupInfoItem) list.get(i)).isGrp_img()));
				}
			}
		}

		public Map<Integer, Boolean> getSelectMap() {
			return this.selectMap;
		}

		public int getCount() {
			if (this.mList != null) {
				return this.mList.size();
			}
			return 0;
		}

		public Object getItem(int position) {
			return this.mList.get(position);
		}

		public long getItemId(int position) {
			return (long) position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(this.mContext).inflate(R.layout.contact_member_item, null);
				viewHolder.grp_img = (CheckBox) convertView.findViewById(R.id.grp_img);
				viewHolder.grp_uName = (TextView) convertView.findViewById(R.id.grp_uName);
				viewHolder.grp_uNumber = (TextView) convertView.findViewById(R.id.grp_uNumber);
				viewHolder.grp_uDept = (TextView) convertView.findViewById(R.id.grp_uDept);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			GroupInfoItem groupInfoItem = (GroupInfoItem) this.mList.get(position);
			if (this.selectMap == null || this.selectMap.size() <= 0) {
				viewHolder.grp_img.setChecked(false);
			} else {
				viewHolder.grp_img.setChecked(((Boolean) this.selectMap.get(Integer.valueOf(position))).booleanValue());
			}
			String name = groupInfoItem.getGrp_uName();
			String number = groupInfoItem.getGrp_uNumber();
			String dept = groupInfoItem.getGrp_uDept();
			if (name == null || !name.toLowerCase().contains(this.search_word.toLowerCase())) {
				viewHolder.grp_uName.setText(name);
			} else {
				viewHolder.grp_uName.setText(getHighLightText(name, this.search_word));
			}
			if (number == null || !number.contains(this.search_word)) {
				viewHolder.grp_uNumber.setText(number);
			} else {
				viewHolder.grp_uNumber.setText(getHighLightText(number, this.search_word));
			}
			if (dept == null || !dept.toLowerCase().contains(this.search_word.toLowerCase())) {
				viewHolder.grp_uDept.setText(dept);
			} else {
				viewHolder.grp_uDept.setText(getHighLightText(dept, this.search_word));
			}
			return convertView;
		}

		private CharSequence getHighLightText(String str, String keyword) {
			int index = str.toLowerCase().indexOf(keyword.toLowerCase());
			int len = keyword.length();
			return Html.fromHtml(str.substring(0, index) + "<u><font color=#FF0000>" + str.substring(index, index + len) + "</font></u>" + str.substring(index + len, str.length()));
		}
	}

	private static class ViewHolder {
		private CheckBox grp_img;
		private TextView grp_uDept;
		private TextView grp_uName;
		private TextView grp_uNumber;

		private ViewHolder() {
		}
	}

}
