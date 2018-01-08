package com.zed3.screenhome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.zed3.addressbook.DataBaseService;
import com.zed3.customgroup.CustomGroupManager;
import com.zed3.customgroup.CustomGroupParserListener;
import com.zed3.customgroup.CustomGroupUtil;
import com.zed3.customgroup.GroupInfoItem;
import com.zed3.flow.FlowRefreshService;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.message.AlarmService;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.RegisterService;
import com.zed3.sipua.ui.splash.SplashActivity;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.LogUtil;

import java.io.Serializable;
import java.util.List;

public class BaseActivity extends Activity implements CustomGroupParserListener {
	private static final int DELETE_MEMBER_NOTIFICATION = 1;
	private static final int DESTROY_CUSTOM_GROUP_NOTIFICATION = 2;
	private static final int ADD_MEMBER_NOTIFICATION = 3;
	private static final int LEAVE_CUSTOM_GROUP_NOTIFICATION = 4;
	private static final String TAG = "BaseActivity";
	private Context mContext;
	private UserAgent userAgent;
	private Handler handler = new Handler() {
		@SuppressLint({"HandlerLeak"})
		public void handleMessage(final Message message) {
			switch (message.what) {
				case DELETE_MEMBER_NOTIFICATION: {
					final Bundle data = message.getData();
					if (data == null) {
						break;
					}
					final String string = data.getString("groupCreatorName");
					final String string2 = data.getString("groupNum");
					final String string3 = data.getString("groupName");
					final List list = (List) data.getSerializable("memberList");
					if (list != null && list.size() > 0) {
						final StringBuilder sb = new StringBuilder();
						if (list.contains(CustomGroupUtil.getInstance().getCurrentUserNum(BaseActivity.this.mContext))) {
							CustomGroupUtil.getInstance().deleteElementFromCustomGrpMap(string2, string3);
							CustomGroupUtil.getInstance().deleteElementFromGroupListMap(string2);
							sb.append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.delete_member_notification));
						} else {
							final List<String> membersNameFromDB = CustomGroupUtil.getInstance().getMembersNameFromDB(list);
							if (membersNameFromDB != null && membersNameFromDB.size() > 0) {
								if (list.size() == 1) {
									sb.append(membersNameFromDB.get(0));
								} else if (list.size() >= 2 && membersNameFromDB.size() >= 2) {
									sb.append(membersNameFromDB.get(0)).append(",").append(membersNameFromDB.get(1)).append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.public_notification2));
								}
							}
							sb.append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.delete_member_notification1));
						}
						if (CustomGroupUtil.getInstance().isCurrentLanguageChinese(BaseActivity.this.mContext)) {
							sb.append(string).append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.public_notification)).append(string3).append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.delete_member_notification2));
						} else {
							sb.append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.public_notification)).append(string3).append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.delete_member_notification2)).append(string);
						}
						CustomGroupUtil.getInstance().showToast(BaseActivity.this.mContext, sb.toString().trim());
						BaseActivity.this.sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO));
						return;
					}
					break;
				}
				case DESTROY_CUSTOM_GROUP_NOTIFICATION: {
					final Bundle data2 = message.getData();
					if (data2 != null) {
						final String string4 = data2.getString("groupCreatorName");
						final String string5 = data2.getString("groupNum");
						final String string6 = data2.getString("groupName");
						CustomGroupUtil.getInstance().deleteElementFromCustomGrpMap(string5, string6);
						CustomGroupUtil.getInstance().deleteElementFromGroupListMap(string5);
						BaseActivity.this.sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO));
						final StringBuilder sb2 = new StringBuilder();
						sb2.append(string4).append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.disslove_group_notification)).append(string6);
						CustomGroupUtil.getInstance().showToast(BaseActivity.this.mContext, sb2.toString().trim());
						return;
					}
					break;
				}
				case ADD_MEMBER_NOTIFICATION: {
					final Bundle data3 = message.getData();
					if (data3 == null) {
						break;
					}
					final String string7 = data3.getString("groupCreatorName");
					final String string8 = data3.getString("groupName");
					final List list2 = (List) data3.getSerializable("memberList");
					if (list2 != null && list2.size() > 0) {
						final StringBuilder sb3 = new StringBuilder();
						if (list2.contains(CustomGroupUtil.getInstance().getCurrentUserNum(BaseActivity.this.mContext))) {
							sb3.append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.add_member_notification));
						} else {
							final List<String> membersNameFromDB2 = CustomGroupUtil.getInstance().getMembersNameFromDB(list2);
							if (list2.size() == 1) {
								sb3.append(membersNameFromDB2.get(0));
							} else if (list2.size() >= 2) {
								final int size = membersNameFromDB2.size();
								if (size == 1) {
									sb3.append(membersNameFromDB2.get(0)).append(",").append(list2.get(1));
								} else if (size >= 2) {
									sb3.append(membersNameFromDB2.get(0)).append(",").append(membersNameFromDB2.get(1));
								}
								sb3.append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.public_notification2));
							}
							sb3.append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.add_member_notification1));
						}
						if (CustomGroupUtil.getInstance().isCurrentLanguageChinese(BaseActivity.this.mContext)) {
							sb3.append(string7).append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.public_notification)).append(string8);
						} else {
							sb3.append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.public_notification)).append(string8).append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.delete_member_notification2)).append(string7);
						}
						CustomGroupUtil.getInstance().showToast(BaseActivity.this.mContext, sb3.toString().trim());
						BaseActivity.this.sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO));
						return;
					}
					break;
				}
				case LEAVE_CUSTOM_GROUP_NOTIFICATION: {
					final Bundle data4 = message.getData();
					if (data4 != null) {
						final String string9 = data4.getString("groupCreatorName");
						final String string10 = data4.getString("groupName");
						final String string11 = data4.getString("leaveNumber");
						String grp_uName = "";
						for (final GroupInfoItem groupInfoItem : DataBaseService.getInstance().getAllMembers()) {
							if (groupInfoItem.getGrp_uNumber().equals(string11)) {
								grp_uName = groupInfoItem.getGrp_uName();
								break;
							}
						}
						final StringBuilder sb4 = new StringBuilder();
						if (!grp_uName.equals("")) {
							sb4.append(grp_uName);
						} else {
							sb4.append(string11);
						}
						sb4.append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.leave_group_notification));
						if (CustomGroupUtil.getInstance().isCurrentLanguageChinese(BaseActivity.this.mContext)) {
							sb4.append(string9).append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.public_notification)).append(string10);
						} else {
							sb4.append(string10).append(CustomGroupUtil.getInstance().getStringByResId(BaseActivity.this.mContext, R.string.delete_member_notification2)).append(string9);
						}
						CustomGroupUtil.getInstance().showToast(BaseActivity.this.mContext, sb4.toString().trim());
						BaseActivity.this.sendBroadcast(new Intent(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO));
						return;
					}
					break;
				}
			}
		}
	};

	@Override
	public void parseDeleteMemberInfoCompleted(String groupCreatorName, String groupNum, String groupName, List<String> memberList) {
		LogUtil.makeLog("BaseActivity", "parseDeleteMemberInfoCompleted() " + memberList.toString());
		final Message obtain = Message.obtain();
		obtain.what = 1;
		final Bundle data = new Bundle();
		data.putString("groupCreatorName", groupCreatorName);
		data.putString("groupNum", groupNum);
		data.putString("groupName", groupName);
		data.putSerializable("memberList", (Serializable) memberList);
		obtain.setData(data);
		this.handler.sendMessage(obtain);
	}

	@Override
	public void parseDestroyCustomGroupInfoCompleted(String groupCreatorName, String groupNum, String groupName) {
		LogUtil.makeLog("BaseActivity", "parseDestroyCustomGroupInfoCompleted() " + groupName);
		final Message obtain = Message.obtain();
		obtain.what = 2;
		final Bundle data = new Bundle();
		data.putString("groupCreatorName", groupCreatorName);
		data.putString("groupNum", groupNum);
		data.putString("groupName", groupName);
		obtain.setData(data);
		this.handler.sendMessage(obtain);
	}

	@Override
	public void parseAddMemberInfoCompleted(String groupCreatorName, String groupName, List<String> memberList) {
		LogUtil.makeLog("BaseActivity", "parseAddMemberInfoCompleted() " + memberList.toString());
		final Message obtain = Message.obtain();
		obtain.what = 3;
		final Bundle data = new Bundle();
		data.putString("groupCreatorName", groupCreatorName);
		data.putString("groupName", groupName);
		data.putSerializable("memberList", (Serializable) memberList);
		obtain.setData(data);
		this.handler.sendMessage(obtain);
	}

	@Override
	public void parseLeaveCustomGroupInfoCompleted(String groupCreatorName, String groupName, String leaveNumber) {
		LogUtil.makeLog("BaseActivity", "parseLeaveCustomGroupInfoCompleted() " + leaveNumber);
		final Message obtain = Message.obtain();
		obtain.what = 4;
		final Bundle data = new Bundle();
		data.putString("groupCreatorName", groupCreatorName);
		data.putString("groupName", groupName);
		data.putString("leaveNumber", leaveNumber);
		obtain.setData(data);
		this.handler.sendMessage(obtain);
	}

	public void closeService() {
		this.stopService(new Intent((Context) this, (Class) FlowRefreshService.class));
		this.stopService(new Intent((Context) this, (Class) RegisterService.class));
		this.stopService(new Intent((Context) this, (Class) AlarmService.class));
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.mContext = (Context) this;
		if (Receiver.mSipdroidEngine != null && Receiver.mSipdroidEngine.isRegistered(true)) {
			this.userAgent = Receiver.GetCurUA();
		}
		if (this.userAgent != null) {
			this.userAgent.GetAllGrps().setOnParseCompledtedListener(this);
		}
		if (DeviceInfo.CONFIG_SUPPORT_HOMEKEY_BLOCK) {
			final Window window = this.getWindow();
			final WindowManager.LayoutParams attributes = window.getAttributes();
			attributes.flags = Integer.MIN_VALUE;
			attributes.systemUiVisibility = 512;
			window.setAttributes(attributes);
		}
	}

	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		if (DeviceInfo.CONFIG_SUPPORT_HOMEKEY_BLOCK) {
			switch (n) {
				case 4: {
					final Intent intent = new Intent("android.intent.action.MAIN");
					intent.addCategory("android.intent.category.HOME");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					this.startActivity(intent);
					return false;
				}
			}
		}
		return super.onKeyDown(n, keyEvent);
	}

	public void reLogin() {
		this.sendBroadcast(new Intent("com.zed3.sipua.exitActivity"));
		final Intent intent = new Intent((Context) this, (Class) SplashActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
		this.finish();
	}
}
