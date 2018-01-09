package com.zed3.customgroup;

import android.annotation.SuppressLint;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.zed3.addressbook.AbookOpenHelper;
import com.zed3.addressbook.AddressAdapter;
import com.zed3.addressbook.AddressBookUtils;
import com.zed3.addressbook.CompanyMemberActivity;
import com.zed3.addressbook.DataBaseService;
import com.zed3.addressbook.DepartmentAdapter;
import com.zed3.addressbook.Member;
import com.zed3.addressbook.Member.UserType;
import com.zed3.addressbook.MyProgressDialog;
import com.zed3.addressbook.Team;
import com.zed3.addressbook.UserMinuteActivity;
import com.zed3.constant.GroupConstant;
import com.zed3.log.MyLog;
import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.phone.CallerInfo;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.SettingVideoSize;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.ui.lowsdk.PinnedHeaderExpandableListView;
import com.zed3.sipua.ui.lowsdk.PinnedHeaderExpandableListView.HeaderAdapter;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;
import com.zed3.zhejiang.ZhejiangReceivier;

import org.zoolu.tools.GroupListInfo;

import java.lang.reflect.Field;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

public class ContactActivity extends BaseActivity implements OnClickListener, OnQueryTextListener, Observer {
	public static final String CUSTOM_GROUP_ACTION_UPDATE_PERMANENT_GROUP_INFO = "custom_group_action_UPDATE_PERMANENT_GROUP_INFO";
	private static final int MODIFY_RESULT = 1;
	private static final String TAG = "ContactActivity";
	private static final int TIME_OUT = 2;
	private static final int UPDATE_PERMANETN_GROUP_INFO = 3;
	private static final int UPDATE_RESULT = 0;
	private final int GET_ALL_TEAMS = 3;
	private final int GET_TEAMS_BY_PID = 4;
	private final int NO_UPDATE = 1;
	private final int NULL = 0;
	private final int UPDATE_DATA = 2;
	private AddressAdapter adapter;
	private ImageView add_img;
	private LinearLayout addressbook_item_layout;
	private LinearLayout bottomLayout;
	private TextView companyName;
	private TextView companyNum;
	private int count = 0;
	private int currentLayout = 0;
	private CustomGroupStateReceiver customGroupStateReceiver;
	private Map<String, PttCustomGrp> customGroups = new LinkedHashMap();
	private List<Map<String, String>> customGrpData;
	private Map<String, String> customMap = new HashMap();
	private DataBaseService dbService;
	private PinnedHeaderExpandableListView expandableListView;
	private int gropid = -1;
	private List<String> groupData;
	private GrpInfoAdapter grpInfoAdapter;
	private int grpmembercount;
	private IntentFilter intentFilter;
	private boolean isNoMember;
	boolean isQuery = false;
	private List<Map<String, String>> list = new ArrayList();
	private PinnedHeaderExpandableListView listView;
	LinearLayout loadingView;
	private Context mContext;
	private MyProgressDialog mProgressDialog;
	private PttCustomGrp modifiedCustomGrp = new PttCustomGrp();
	private String newGrpName = "";
	private HashMap<PttGrp, ArrayList<GroupListInfo>> permanentGroups = new HashMap();
	private List<Map<String, String>> permanentGrpData;
	private Map<String, ArrayList<GroupListInfo>> permanentGrpMap = new HashMap();
	private List<GroupListInfo> permanentList = new ArrayList();
	private PopupWindow popupWindow;
	private SearchView searchView;
	List<Team> team = null;
	private TextView tv_company;
	private TextView tv_ptt;
	TextView tv_refresh;
	private UserAgent userAgent;
	private View v_addressbook_bottom;

	OnLongClickListener customGrpLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			PttCustomGrp pttCustomGrp = ContactActivity.this.userAgent.getAllCustomGroups().get(
					ContactActivity.this.customMap.get(((TextView)view.findViewById(R.id.grp_name)).getText().toString().trim()));
			if (CustomGroupUtil.getInstance().isCustomGroupCreator(ContactActivity.this.mContext, pttCustomGrp.getGroupCreatorNum())) {
				ContactActivity.this.showModifyDialog(pttCustomGrp, ((TextView) view.findViewById(R.id.grp_name)).getText().toString().trim());
			} else {
				CustomGroupUtil.getInstance().showToast(ContactActivity.this.mContext, R.string.modify_failure_notify2);
			}
			return true;
		}
	};


	OnLongClickListener fixgGrpLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			CustomGroupUtil.getInstance().showToast(ContactActivity.this.mContext, R.string.modify_failure_notify);
			return true;
		}
	};


	OnClickListener customGrpClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intentToGrpInfoList = new Intent(ContactActivity.this.mContext, GroupMemberListAcitivity.class);
			String currentGrpName = ((TextView) v.findViewById(R.id.grp_name)).getText().toString().trim();
			Bundle bundle = new Bundle();
			bundle.putString("grp_name", currentGrpName);
			bundle.putString("grp_type", "custom");
			ContactActivity.this.customMap = ContactActivity.this.userAgent.getCustomGroupMap();
			String str = "custom_member_info";
			bundle.putSerializable(str, ContactActivity.this.customGroups.get((String) ContactActivity.this.customMap.get(currentGrpName.trim())));
			intentToGrpInfoList.putExtras(bundle);
			ContactActivity.this.startActivity(intentToGrpInfoList);
		}
	};

	OnTouchListener addImgTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case 0:
					ContactActivity.this.add_img.setImageResource(R.drawable.navbar_add_press);
					break;
				case 1:
					ContactActivity.this.add_img.setImageResource(R.drawable.navbar_add_nor);
					break;
			}
			return false;
		}
	};

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					ContactActivity.this.refreshAdapter();
					return;
				case 1:
					CustomGroupUtil.getInstance().dismissProgressDialog();
					if (msg.arg1 == 0) {
						int reasonCode = msg.arg2;
						if (reasonCode == 450 || reasonCode == 453) {
							CustomGroupUtil.getInstance().showFailureReason(ContactActivity.this.mContext, reasonCode);
							return;
						} else {
							CustomGroupUtil.getInstance().showToast(ContactActivity.this.mContext, R.string.modify_failure);
							return;
						}
					} else if (msg.arg1 == 1) {
						CustomGroupUtil.getInstance().showToast(ContactActivity.this.mContext, R.string.modify_success);
						ContactActivity.this.modifyCustomGroupMap();
						ContactActivity.this.refreshAdapter();
						return;
					} else {
						return;
					}
				case 2:
					CustomGroupUtil.getInstance().showToast(ContactActivity.this.mContext, R.string.time_out);
					return;
				case 3:
					ContactActivity.this.updatePermanentGroupInfo();
					return;
				default:
					return;
			}
		}
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(final Message message) {
			super.handleMessage(message);
			switch (message.what) {
				case 0:
					loadingView.setVisibility(View.GONE);
					break;
				case 1:
					Log.i("xxxxxxx", "NO_UPDATE........");
					dbService.getAllTeams(1);
					break;
				case 2:
//					String mResutl = (String) message.obj;
					break;
				case 3:
					team = (List) message.obj;
					MyLog.d("xxxxxxx", "GET_TEAMS_BY_PID");
					MyLog.e("ee", " wo hai shi zou 5");
					List<Map<String, String>> teamlist = new ArrayList();
					List<Map<String, String>> list = new ArrayList();
					if (currentLayout == 0 && !isQuery) {
						setAddressBook();
						for (Team t : team) {
							String pid = dbService.getPidByTid(t.getId());
							if (pid != null) {
								if (pid.equals(CallerInfo.PRIVATE_NUMBER)) {
									Map<String, String> ta = new HashMap();
									ta.put("tid", t.getId());
									ta.put("name", t.getName());
									ta.put("pid", pid);
									ContactActivity.this.count = 0;
									ta.put("count", new StringBuilder(String.valueOf(getCount(t.getId()))).toString());
									teamlist.add(ta);
								}
							}
						}
						Map<String, String> All = new HashMap();
						Map<String, String> Console = new HashMap();
						Map<String, String> GVS = new HashMap();
						SharedPreferences sharedPreferences = ContactActivity.this.getSharedPreferences("com.zed3.sipua_preferences", 0);
						SharedPreferences sharedPreferences2 = sharedPreferences;
						String All_name = sharedPreferences2.getString("allName", ContactActivity.this.mContext.getResources().getString(R.string.all));
						sharedPreferences2 = sharedPreferences;
						String Console_name = sharedPreferences2.getString("consoleName", ContactActivity.this.mContext.getResources().getString(R.string.console));
						sharedPreferences2 = sharedPreferences;
						String GVS_name = sharedPreferences2.getString("gvsName", ContactActivity.this.mContext.getResources().getString(R.string.gvs));
						String All_sort = sharedPreferences.getString("allSort", "0");
						String Console_sort = sharedPreferences.getString("consoleSort", "1");
						String GVS_sort = sharedPreferences.getString("gvsSort", SettingVideoSize.R720P);
						All.put("name", All_name);
						All.put("sort", All_sort);
						All.put("flag", "all");
						Console.put("name", Console_name);
						Console.put("sort", Console_sort);
						Console.put("flag", "console");
						GVS.put("name", GVS_name);
						GVS.put("flag", "gvs");
						GVS.put("sort", GVS_sort);
						All.put("count", new StringBuilder(String.valueOf(ContactActivity.this.dbService.getMembersNumber("mtype!='" + UserType.GRP_NUM.convert() + "'"))).toString());
						Console.put(AbookOpenHelper.TABLE_ID, UserType.SVP.convert());
						Console.put("count", new StringBuilder(String.valueOf(ContactActivity.this.dbService.getMembersNumber("mtype='" + UserType.SVP.convert() + "'"))).toString());
						GVS.put(AbookOpenHelper.TABLE_ID, UserType.VIDEO_MONITOR_GVS.convert());
						GVS.put("count", new StringBuilder(String.valueOf(ContactActivity.this.dbService.getMembersNumber("mtype='" + UserType.VIDEO_MONITOR_GVS.convert() + "' or mtype='" + UserType.VIDEO_MONITOR_GB28181.convert() + "'"))).toString());
						if (All_sort.equals("0")) {
							list.add(All);
						}
						if (Console_sort.equals("0")) {
							list.add(Console);
						}
						if (GVS_sort.equals("0")) {
							list.add(GVS);
						}
						if (All_sort.equals("1")) {
							list.add(All);
						}
						if (Console_sort.equals("1")) {
							list.add(Console);
						}
						if (GVS_sort.equals("1")) {
							list.add(GVS);
						}
						if (All_sort.equals(SettingVideoSize.R720P)) {
							list.add(All);
						}
						if (Console_sort.equals(SettingVideoSize.R720P)) {
							list.add(Console);
						}
						if (GVS_sort.equals(SettingVideoSize.R720P)) {
							list.add(GVS);
						}
						List<Map<String, String>> svpList = ContactActivity.this.dbService.getMembersByType("mtype='" + UserType.SVP.convert() + "'");
						List<Map<String, String>> gvsList = ContactActivity.this.dbService.getMembersByType("mtype='" + UserType.VIDEO_MONITOR_GVS.convert() + "' or mtype='" + UserType.VIDEO_MONITOR_GB28181.convert() + "'");
						Collections.sort(svpList, new Comparator<Map<String, String>>() {
							@Override
							public int compare(Map<String, String> map, Map<String, String> map2) {
								return map.get(UserMinuteActivity.USER_MNAME).compareTo((String)map2.get(UserMinuteActivity.USER_MNAME));
							}
						});
						Collections.sort(gvsList, new Comparator<Map<String, String>>() {
							@Override
							public int compare(Map<String, String> map, Map<String, String> map2) {
								return map.get(UserMinuteActivity.USER_MNAME).compareTo((String)map2.get(UserMinuteActivity.USER_MNAME));
							}
						});
						if (ContactActivity.this.listView.getHeaderView() == null) {
							ContactActivity.this.listView.setHeaderView(ContactActivity.this.getLayoutInflater().inflate(R.layout.contact_grp_name_list, ContactActivity.this.listView, false));
						}
						ContactActivity.this.adapter.getData(list, teamlist, svpList, gvsList, ContactActivity.this.listView);
						ContactActivity.this.setCompany(ContactActivity.this.team);
						ContactActivity.this.listView.setAdapter(ContactActivity.this.adapter);
						if (ContactActivity.this.mProgressDialog != null && ContactActivity.this.mProgressDialog.isShowing()) {
							ContactActivity.this.mProgressDialog.dismiss();
						}
						ContactActivity.this.tv_refresh.setVisibility(View.VISIBLE);
					}
					loadingView.setVisibility(View.GONE);
					if (gropid == -1) {
						if (listView != null) {
							listView.expandGroup(0, false);
						}
					} else if (!listView.isGroupExpanded(gropid)) {
						listView.expandGroup(gropid, false);
					}

					if (adapter != null) {
						ContactActivity.this.listView.setOnGroupExpandListener((ExpandableListView.OnGroupExpandListener)new ExpandableListView.OnGroupExpandListener() {
							public void onGroupExpand(final int groupPosition) {
								ContactActivity.this.gropid = groupPosition;
								for (int i = 0; i < ContactActivity.this.adapter.getGroupCount(); ++i) {
									if (groupPosition != i) {
										ContactActivity.this.listView.collapseGroup(i);
									}
								}
							}
						});
						return;
					}
					break;
				default:
					return;
			}
		}
	};

	public class CustomGroupStateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null) {
				LogUtil.makeLog(ContactActivity.TAG, "CustomGroupStateReceiver#onReceive() " + action);
				if (action.equals("com.zed3.sipua_grouplist_update_over") || action.equals(GroupConstant.ACTION_ALL_GROUP_CHANGE)) {
					updatePermanentGroupInfo();
					return;
				}

				Message message = Message.obtain();
				if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_MODIFY_SUCCESS)) {
					message.what = 1;
					message.arg1 = 1;
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_MODIFY_FAILURE)) {
					message.what = 1;
					message.arg1 = 0;
					message.arg2 = intent.getIntExtra("reasonCode", 488);
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_REQUEST_TIME_OUT)) {
					message.what = 2;
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_INFO_CHANGED)
						|| action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO)) {
					message.what = 0;
				} else if (action.equals(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO)) {
					message.what = 0;
				} else if (action.equals(ContactActivity.CUSTOM_GROUP_ACTION_UPDATE_PERMANENT_GROUP_INFO)) {
					message.what = 3;
				}
				ContactActivity.this.handler.sendMessage(message);
			}
		}
	}

	class FixGrpClickListener implements OnClickListener {
		int childPosition = -1;

		public FixGrpClickListener(int position) {
			this.childPosition = position;
		}

		public void onClick(View view) {
			Intent intent = new Intent(ContactActivity.this.mContext, GroupMemberListAcitivity.class);
			String currentGrpName = ((TextView) view.findViewById(R.id.grp_name)).getText().toString().trim();
			Bundle bundle = new Bundle();
			bundle.putString("grp_name", currentGrpName);
			bundle.putString("grp_type", "permanent");
			bundle.putSerializable("permanent_member_info", ContactActivity.this.permanentGrpMap.get(
					(ContactActivity.this.permanentGrpData.get(this.childPosition)).get("curGrp_ID")));
			intent.putExtras(bundle);
			ContactActivity.this.startActivity(intent);
		}
	}

	private static class ViewHolder {
		private TextView grp_count;
		private ImageView grp_icon;
		private TextView grp_name;

		private ViewHolder() {
		}
	}

	public class GrpInfoAdapter extends BaseExpandableListAdapter implements HeaderAdapter {
		private Context context;
		private List<Map<String, String>> customList = new ArrayList();
		private List<String> groupList = new ArrayList();
		private SparseIntArray groupStatusMap = new SparseIntArray();
		private PinnedHeaderExpandableListView mListView;
		private List<Map<String, String>> permanentList = new ArrayList();

		public GrpInfoAdapter(Context context, List<String> groupList, List<Map<String, String>> permanentList, List<Map<String, String>> customList, PinnedHeaderExpandableListView list) {
			this.context = context;
			this.groupList = groupList;
			this.permanentList = permanentList;
			this.customList = customList;
			this.mListView = list;
		}

		public int getGroupCount() {
			return this.groupList.size();
		}

		public int getChildrenCount(int groupPosition) {
			if (groupPosition == 0) {
				if (this.permanentList != null) {
					return this.permanentList.size();
				}
			} else if (groupPosition == 1 && this.customList != null) {
				return this.customList.size();
			}
			return 0;
		}

		public Object getGroup(int groupPosition) {
			return this.groupList.get(groupPosition);
		}

		public Object getChild(int groupPosition, int childPosition) {
			if (groupPosition == 0) {
				return this.permanentList.get(childPosition);
			}
			if (groupPosition == 1) {
				return this.customList.get(childPosition);
			}
			return null;
		}

		public long getGroupId(int groupPosition) {
			return (long) groupPosition;
		}

		public long getChildId(int groupPosition, int childPosition) {
			return (long) childPosition;
		}

		public boolean hasStableIds() {
			return false;
		}

		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(this.context).inflate(R.layout.contact_grp_name_list, null);
				viewHolder.grp_name = (TextView) convertView.findViewById(R.id.grp_name);
				viewHolder.grp_count = (TextView) convertView.findViewById(R.id.grp_count);
				convertView.findViewById(R.id.icon).setVisibility(View.INVISIBLE);
				viewHolder.grp_icon = (ImageView) convertView.findViewById(R.id.imageicon);
				convertView.findViewById(R.id.team_layout).setBackgroundResource(R.color.whole_bg);
				convertView.findViewById(R.id.member_layout).setVisibility(View.GONE);
				convertView.setTag(viewHolder);
				convertView.setTag(R.id.grp_name, Integer.valueOf(groupPosition));
				convertView.setTag(R.id.grp_count, Integer.valueOf(-1));
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.grp_icon.setVisibility(View.VISIBLE);
			viewHolder.grp_icon.setImageResource(R.drawable.iconfont_zhankai);
			if (!isExpanded) {
				viewHolder.grp_icon.setImageResource(R.drawable.iconfont_shousuozhankai);
			}
			viewHolder.grp_name.setText((CharSequence) this.groupList.get(groupPosition));
			viewHolder.grp_count.setText(new StringBuilder(String.valueOf(ContactActivity.this.grpmembercount)).append(this.context.getResources().getString(R.string.person)).toString());
			convertView.setTag(R.id.grp_name, Integer.valueOf(groupPosition));
			convertView.setTag(R.id.grp_count, Integer.valueOf(-1));
			return convertView;
		}

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(this.context).inflate(R.layout.contact_grp_name_list, null);
				viewHolder.grp_name = (TextView) convertView.findViewById(R.id.grp_name);
				viewHolder.grp_count = (TextView) convertView.findViewById(R.id.grp_count);
				convertView.setTag(viewHolder);
				convertView.setTag(R.id.grp_name, Integer.valueOf(groupPosition));
				convertView.setTag(R.id.grp_count, Integer.valueOf(childPosition));
				convertView.findViewById(R.id.member_layout).setVisibility(View.GONE);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			String grp_name = "";
			String grp_count = "";
			if (groupPosition == 0) {
				grp_name = (String) ((Map) this.permanentList.get(childPosition)).get("curGrp_name");
				grp_count = (String) ((Map) this.permanentList.get(childPosition)).get("curGrp_member_count");
				convertView.setOnLongClickListener(ContactActivity.this.fixgGrpLongClickListener);
				convertView.setOnClickListener(new FixGrpClickListener(childPosition));
			} else if (groupPosition == 1) {
				grp_name = (String) ((Map) this.customList.get(childPosition)).get("curGrp_name");
				grp_count = (String) ((Map) this.customList.get(childPosition)).get("curGrp_member_count");
				convertView.setOnLongClickListener(ContactActivity.this.customGrpLongClickListener);
				convertView.setOnClickListener(ContactActivity.this.customGrpClickListener);
			}
			viewHolder.grp_name.setText(grp_name);
			viewHolder.grp_count.setText(new StringBuilder(String.valueOf(grp_count)).append(this.context.getResources().getString(R.string.person)).toString());
			return convertView;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		public int getHeaderState(int groupPosition, int childPosition) {
			if (childPosition == getChildrenCount(groupPosition) - 1) {
				return 2;
			}
			if (childPosition != -1 || this.mListView.isGroupExpanded(groupPosition)) {
				return 1;
			}
			return 0;
		}

		public void configureHeader(View header, int groupPosition, int childPosition, int alpha) {
			String groupData = (String) this.groupList.get(groupPosition);
			String grp_count = new StringBuilder(String.valueOf(ContactActivity.this.grpmembercount)).append(this.context.getResources().getString(R.string.person)).toString();
			((TextView) header.findViewById(R.id.grp_name)).setText(groupData);
			((TextView) header.findViewById(R.id.grp_count)).setText(grp_count);
			RelativeLayout team_layout = (RelativeLayout) header.findViewById(R.id.team_layout);
			header.findViewById(R.id.icon).setVisibility(View.INVISIBLE);
			header.findViewById(R.id.member_layout).setVisibility(View.GONE);
			ImageView grp_icon = (ImageView) header.findViewById(R.id.imageicon);
			team_layout.setBackgroundResource(R.color.whole_bg);
			grp_icon.setVisibility(View.VISIBLE);
			grp_icon.setImageResource(R.drawable.iconfont_zhankai);
		}

		public void setGroupClickStatus(int groupPosition, int status) {
			this.groupStatusMap.put(groupPosition, status);
		}

		public int getGroupClickStatus(int groupPosition) {
			if (this.groupStatusMap.keyAt(groupPosition) >= 0) {
				return this.groupStatusMap.get(groupPosition);
			}
			return 0;
		}
	}

	private int getCount(String tid) {
		List<Map<String, String>> nextlist = this.dbService.getTeamsByPid(tid, false);
		this.count += this.dbService.getMembersNumber("tid='" + tid + "' and mtype!='" + UserType.GRP_NUM.convert() + "'");
		if (nextlist.size() > 0) {
			for (Map<String, String> m : nextlist) {
				if (Integer.valueOf((String) m.get("tid")) == Integer.valueOf((String) m.get("pid"))) {
					return 0;
				}
				getCount((String) m.get("tid"));
			}
		}
		return this.count;
	}

	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.makeLog(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_contact);
		mContext = this;

		customGroupStateReceiver = new CustomGroupStateReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction(GroupConstant.ACTION_ALL_GROUP_CHANGE);
		intentFilter.addAction("com.zed3.sipua_grouplist_update_over");
		intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_MODIFY_SUCCESS);
		intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_MODIFY_FAILURE);
		intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_INFO_CHANGED);
		intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_LOCAL_INFO);
		intentFilter.addAction(CustomGroupManager.CUSTOM_GROUP_ACTION_UPDATE_GROUP_MEMBER_INFO);
		intentFilter.addAction(CUSTOM_GROUP_ACTION_UPDATE_PERMANENT_GROUP_INFO);
		registerReceiver(customGroupStateReceiver, intentFilter);

		initViewsAndListeners();
		dbService = DataBaseService.getInstance();
		dbService.addObserver(this);
		AddressBookUtils.setConfig1();
		userAgent = Receiver.GetCurUA();
		Log.i(TAG, "AddressBookActivit alversion=" + dbService.getAlversion());
		if (Settings.ISFIRST_LOGIN) {
			if (!refreshAddressBook()) {
				dbService.getAllTeams(1);
			}
			Settings.ISFIRST_LOGIN = false;
		} else if (AddressBookUtils.ISREQUEST) {
			loadingView.setVisibility(View.VISIBLE);
			tv_refresh.setVisibility(View.GONE);
		} else {
			dbService.getAllTeams(1);
		}
		Log.i(TAG, "onCreate");
		if (DeviceInfo.GQT_MAIL_LIST == 1 && DeviceInfo.TALK_BACK == 0) {
			this.tv_ptt.setVisibility(View.GONE);
		}
		if (DeviceInfo.GQT_MAIL_LIST == 0 && DeviceInfo.TALK_BACK == 1) {
			this.tv_company.setVisibility(View.GONE);
			this.bottomLayout.setVisibility(View.INVISIBLE);
			this.mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					ContactActivity.this.bottomLayout.setVisibility(View.VISIBLE);
					ContactActivity.this.tv_ptt.setBackgroundResource(R.drawable.navbar_tab_right_select);
					ContactActivity.this.setBottomLayout(1);
					ContactActivity.this.loadingView.setVisibility(View.GONE);
					ContactActivity.this.tv_refresh.setVisibility(View.GONE);
					ContactActivity.this.isQuery = false;
				}
			}, 1000L);
		}
	}

	private void initViewsAndListeners() {
		tv_refresh = findViewById(R.id.tv_refresh);
		searchView = findViewById(R.id.msearch_view);
		tv_company = findViewById(R.id.tv_company);
		tv_ptt = findViewById(R.id.tv_talkback);
		add_img = findViewById(R.id.add_img);

		tv_refresh.setOnClickListener(this);
		searchView.setOnQueryTextListener(this);
		tv_company.setOnClickListener(this);
		tv_ptt.setOnClickListener(this);
		add_img.setOnClickListener(this);

		add_img.setOnTouchListener(addImgTouchListener);

		v_addressbook_bottom = View.inflate(this.mContext, R.layout.addressbook_bottom, null);
		bottomLayout = findViewById(R.id.bottomLayout);
		loadingView = findViewById(R.id.loadingView);
	}

	private void judgeAndDealLoading() {
		if (AddressBookUtils.ISREQUEST) {
			this.loadingView.setVisibility(View.VISIBLE);
		} else {
			this.loadingView.setVisibility(View.GONE);
		}
	}

	public void onClick(View v) {
		MyLog.e("ee", "onClick");
		this.searchView.setIconified(true);
		switch (v.getId()) {
			case R.id.tv_refresh:
				Log.i(TAG, AddressBookUtils.ISREQUEST ? "正在数据加载中" : "未进行数据加载");
				refreshAddressBook();
				return;
			case R.id.tv_company:
				this.tv_company.setBackgroundResource(R.drawable.navbar_tab_left_select);
				this.tv_ptt.setBackgroundResource(R.drawable.navbar_tab_right_nor);
				setBottomLayout(0);
				Log.i(TAG, "加载情况：" + AddressBookUtils.ISREQUEST);
				judgeAndDealLoading();
				this.tv_refresh.setVisibility(View.VISIBLE);
				this.isQuery = false;
				return;
			case R.id.tv_talkback:
				this.tv_company.setBackgroundResource(R.drawable.navbar_tab_left_nor);
				this.tv_ptt.setBackgroundResource(R.drawable.navbar_tab_right_select);
				setBottomLayout(1);
				this.loadingView.setVisibility(View.GONE);
				this.tv_refresh.setVisibility(View.GONE);
				this.isQuery = false;
				return;
			case R.id.add_img:
				showPopupWindow();
				return;
			default:
				return;
		}
	}

	private boolean refreshAddressBook() {
		boolean state = AddressBookUtils.ISREQUEST;
		Log.i(TAG, Settings.ISFIRST_LOGIN + ":::::" + state);
		MyLog.e("dd", "state +==" + state);
		if (!state) {
			if (!NetChecker.check(Receiver.mContext, true)) {
				return false;
			}
			AddressBookUtils.getNewAddressBook2();
		}
		this.loadingView.setVisibility(View.VISIBLE);
		return true;
	}

	private void setBottomLayout(int type) {
		switch (type) {
			case 0:
				MyLog.e("ee", "setBottomLayout 0");
				this.currentLayout = 0;
				this.bottomLayout.removeAllViews();
				this.dbService.getAllTeams(1);
				return;
			case 1:
				MyLog.e("ee", "setBottomLayout 1");
				this.currentLayout = 1;
				this.permanentList.clear();
				this.bottomLayout.removeAllViews();
				View bottomView = View.inflate(this.mContext, R.layout.contact_custom_group, null);
				this.expandableListView = (PinnedHeaderExpandableListView) bottomView.findViewById(R.id.grp_list);
				this.bottomLayout.addView(bottomView, this.bottomLayout.getWidth(), this.bottomLayout.getHeight());
				getAdapterData();
				setExpandableListViewAdapter();
				return;
			default:
				return;
		}
	}

	private void setAddressBook() {
		MyLog.d("xxxxxxx", "ContactActivity#setAddressBook enter");
		this.addressbook_item_layout = (LinearLayout) this.v_addressbook_bottom.findViewById(R.id.addressbook_item_layout);
		this.companyName = (TextView) this.v_addressbook_bottom.findViewById(R.id.tv_company_name);
		this.companyNum = (TextView) this.v_addressbook_bottom.findViewById(R.id.tv_company_num);
		this.listView = (PinnedHeaderExpandableListView) this.v_addressbook_bottom.findViewById(R.id.addressbook_bottom_listview);
		this.listView.setGroupIndicator(null);
		this.bottomLayout.removeAllViews();
		this.bottomLayout.addView(this.v_addressbook_bottom, this.bottomLayout.getLayoutParams().width, this.bottomLayout.getLayoutParams().height);
		this.listView.setVisibility(View.VISIBLE);
		this.adapter = new AddressAdapter(this.mContext);
		this.addressbook_item_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(ContactActivity.this.mContext, CompanyMemberActivity.class);
				if (ContactActivity.this.team != null && ContactActivity.this.team.size() > 0) {
					String mid = ((Team) ContactActivity.this.team.get(0)).getId();
					String mname = ((Team) ContactActivity.this.team.get(0)).getName();
					if (ContactActivity.this.dbService.isNoMember(mid)) {
						ContactActivity.this.addressbook_item_layout.setOnClickListener(null);
						return;
					}
					intent.putExtra("mid", mid);
					intent.putExtra(UserMinuteActivity.USER_MNAME, mname);
					ContactActivity.this.startActivity(intent);
				}
			}
		});
		MyLog.d("xxxxxxx", "ContactActivity#setAddressBook exit");
	}

	private void getTypeCount(String id, String mid) {
		if (!this.dbService.isNoTeams(id)) {
			this.isNoMember = this.dbService.isNoMemberByType(mid, id);
			getTypeCount(this.dbService.getSectionId(id), mid);
		}
	}

	private void setAllAddressBook() {
		this.addressbook_item_layout = (LinearLayout) this.v_addressbook_bottom.findViewById(R.id.addressbook_item_layout);
		this.companyName = (TextView) this.v_addressbook_bottom.findViewById(R.id.tv_company_name);
		this.companyNum = (TextView) this.v_addressbook_bottom.findViewById(R.id.tv_company_num);
		ExpandableListView listView = (ExpandableListView) this.v_addressbook_bottom.findViewById(R.id.addressbook_bottom_listview);
		this.bottomLayout.removeAllViews();
		this.bottomLayout.addView(this.v_addressbook_bottom, this.bottomLayout.getLayoutParams().width, this.bottomLayout.getLayoutParams().height);
		listView.setVisibility(View.GONE);
		this.addressbook_item_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(ContactActivity.this.mContext, CompanyMemberActivity.class);
				if (ContactActivity.this.team != null && ContactActivity.this.team.size() > 0) {
					String mid = ((Team) ContactActivity.this.team.get(0)).getId();
					String mname = ((Team) ContactActivity.this.team.get(0)).getName();
					if (ContactActivity.this.dbService.isNoMember(mid)) {
						ContactActivity.this.addressbook_item_layout.setOnClickListener(null);
						return;
					}
					intent.putExtra("mid", mid);
					intent.putExtra(UserMinuteActivity.USER_MNAME, mname);
					ContactActivity.this.startActivity(intent);
				}
			}
		});
	}

	private void setCompany(List<Team> team) {
		String name = ((Team) team.get(0)).getName().toString();
		int number = this.dbService.getMembersNumber();
		this.companyName.setText(this.mContext.getResources().getString(R.string.all));
		this.companyNum.setText(new StringBuilder(String.valueOf(number)).toString());
	}

	private String currentUserSection() {
		return this.dbService.getPid(Settings.getUserName());
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				Tools.exitApp(this.mContext);
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@SuppressLint("WrongConstant")
	public boolean onQueryTextSubmit(String query) {
		// TODO delete @SuppressLint("WrongConstant")
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		return false;
	}

	public boolean onQueryTextChange(String newText) {
		List<Map<String, String>> mmList = null;
		if (TextUtils.isEmpty(newText)) {
			if (this.currentLayout == 1) {
				this.tv_ptt.callOnClick();
			} else {
				this.tv_company.callOnClick();
			}
			this.dbService.addObserver(this);
			if (!AddressBookUtils.ISREQUEST) {
				this.loadingView.setVisibility(View.GONE);
			}
			if (this.bottomLayout.getChildCount() == 0) {
				this.loadingView.setVisibility(View.VISIBLE);
				this.dbService.getAllTeams(1);
			}
			setBottomLayout(this.currentLayout);
		} else {
			this.isQuery = true;
			this.loadingView.setVisibility(View.GONE);
			this.tv_refresh.setVisibility(View.GONE);
			String showflag = this.dbService.getCompanyShowflag();
			if (this.currentLayout == 1) {
				mmList = getKeyGrpMembers(getGrpMembers(), newText);
			} else {
				mmList = getSearchMemberData(this.dbService.queryMembersByKeyword(this.mContext, newText));
			}
			this.bottomLayout.removeAllViews();
			this.bottomLayout.addView(View.inflate(this.mContext, R.layout.activity_main, null), this.bottomLayout.getLayoutParams().width, this.bottomLayout.getLayoutParams().height);
			ListView listView = (ListView) findViewById(R.id.listview);
			Collections.sort(mmList, new Comparator<Map<String, String>>() {
				public int compare(Map<String, String> arg0, Map<String, String> arg1) {
					return ((String) arg0.get(UserMinuteActivity.USER_MNAME)).compareTo((String) arg1.get(UserMinuteActivity.USER_MNAME));
				}
			});
			DepartmentAdapter customAdapter = new DepartmentAdapter(this.mContext, newText, this.dbService);
			customAdapter.getData(mmList);
			listView.setAdapter(customAdapter);
		}
		return true;
	}

	private List<Map<String, String>> getSearchMemberData(List<Member> member) {
		List<Map<String, String>> mList = new ArrayList();
		MyLog.e("dd", "new member start " + System.currentTimeMillis());
		for (int i = 0; i < member.size(); i++) {
			Map<String, String> map = new HashMap();
			String pid = this.dbService.getPid(((Member) member.get(i)).getNumber());
			map.put(UserMinuteActivity.USER_MNAME, ((Member) member.get(i)).getmName());
			map.put("number", ((Member) member.get(i)).getNumber());
			map.put(UserMinuteActivity.USER_AUDIO, ((Member) member.get(i)).getAudio());
			map.put(UserMinuteActivity.USER_SMSSWITCH, ((Member) member.get(i)).getSmsswitch());
			map.put(UserMinuteActivity.USER_PICTUREUPLPAD, ((Member) member.get(i)).getPictureupload());
			map.put(UserMinuteActivity.USER_VIDEO, ((Member) member.get(i)).getVideo());
			map.put("title", this.dbService.getTeamName(pid));
			map.put(UserMinuteActivity.USER_SEX, ((Member) member.get(i)).getSex());
			map.put(UserMinuteActivity.USER_MTYPE, ((Member) member.get(i)).getMtype());
			map.put(UserMinuteActivity.USER_GPS, ((Member) member.get(i)).getGps());
			map.put(UserMinuteActivity.USER_PTTMAP, ((Member) member.get(i)).getPttmap());
			map.put(UserMinuteActivity.USER_DTYPE, ((Member) member.get(i)).getDtype());
			map.put(UserMinuteActivity.USER_PHONE, ((Member) member.get(i)).getPhone());
			map.put(UserMinuteActivity.USER_POSITION, ((Member) member.get(i)).getPosition());
			mList.add(map);
		}
		MyLog.e("dd", "new member end " + System.currentTimeMillis());
		return mList;
	}

	public void getAdapterData() {
		LogUtil.makeLog(TAG, "getAdapterData()");
		this.userAgent = Receiver.GetCurUA();
		Log.e("TANGJIAN", "固定组数据： == " + GroupListUtil.getGroupListsMap());
		this.permanentGroups = GroupListUtil.getGroupListsMap();
		Log.e("TANGJIAN", "所有自建组数据： == " + this.userAgent.getAllCustomGroups());
		Log.e("TANGJIAN", "自建组号码数据： == " + this.userAgent.getAllCustomGroups());
		this.customGroups = this.userAgent.getAllCustomGroups();
		this.customMap = this.userAgent.getCustomGroupMap();
	}

	private void refreshAdapter() {
		LogUtil.makeLog(TAG, "refreshAdapter()");
		this.userAgent = Receiver.GetCurUA();
		this.customGroups = this.userAgent.getAllCustomGroups();
		this.customMap = this.userAgent.getCustomGroupMap();
		if (this.grpInfoAdapter != null && this.customGroups != null) {
			this.grpInfoAdapter.customList = CompareUtil.getInstance().sortByGrpOrder(getCustomChildrenData(this.customGroups), false);
			this.grpInfoAdapter.notifyDataSetChanged();
		}
	}

	private void updatePermanentGroupInfo() {
		LogUtil.makeLog(TAG, "updatePermanentGroupInfo()");
		this.permanentGroups = GroupListUtil.getGroupListsMap();
		this.permanentGrpData = CompareUtil.getInstance().sortByGrpOrder(getPermanentChildrenData(this.permanentGroups), true);
		if (this.grpInfoAdapter != null && this.permanentGroups != null) {
			this.grpInfoAdapter.permanentList = CompareUtil.getInstance().sortByGrpOrder(getPermanentChildrenData(this.permanentGroups), true);
			this.grpInfoAdapter.notifyDataSetChanged();
		}
	}

	private void setExpandableListViewAdapter() {
		LogUtil.makeLog(TAG, "setExpandableListViewAdapter()");
		this.groupData = getGroupData();
		this.permanentGrpData = CompareUtil.getInstance().sortByGrpOrder(getPermanentChildrenData(this.permanentGroups), true);
		this.customGrpData = CompareUtil.getInstance().sortByGrpOrder(getCustomChildrenData(this.customGroups), false);
		if (this.expandableListView.getHeaderView() == null) {
			this.expandableListView.setHeaderView(getLayoutInflater().inflate(R.layout.contact_grp_name_list, this.expandableListView, false));
		}
		this.grpInfoAdapter = new GrpInfoAdapter(this.mContext, this.groupData, this.permanentGrpData, this.customGrpData, this.expandableListView);
		this.expandableListView.setAdapter(this.grpInfoAdapter);
		this.expandableListView.setGroupIndicator(null);
		for (int i = 0; i < this.grpInfoAdapter.getGroupCount(); i++) {
			this.expandableListView.expandGroup(i, false);
		}
	}

	private void showModifyDialog(PttCustomGrp pttCustomGrp, String old_grp_name) {
		View view = LayoutInflater.from(this.mContext).inflate(R.layout.contact_custom_grp_dialog, null);
		((TextView) view.findViewById(R.id.dialog_title)).setText(this.mContext.getResources().getString(R.string.modify_title));
		final EditText newName = (EditText) view.findViewById(R.id.custom_grp_name);
		newName.setText(old_grp_name);
		newName.setSelectAllOnFocus(true);
		newName.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String editable = newName.getText().toString();
				String string = editable.replaceAll("[^( ().#a-zA-Z0-9\\u4e00-\\u9fa5)]", "");
				if (!editable.equals(string)) {
					newName.setText(string);
				}
				newName.setSelection(newName.length());
				count = newName.length();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		Button btn_ok = (Button) view.findViewById(R.id.custom_grp_ok);
		Button btn_cancel = (Button) view.findViewById(R.id.custom_grp_cancel);
		final Dialog dialog = new Dialog(this.mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(view);
		dialog.setCancelable(false);
		dialog.show();
		final String str = old_grp_name;
		final PttCustomGrp pttCustomGrp2 = pttCustomGrp;
		btn_ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String new_group_name = newName.getText().toString().trim();
				if (new_group_name == null || new_group_name.equals("")) {
					CustomGroupUtil.getInstance().showToast(ContactActivity.this.mContext, (int) R.string.modify_failure_notify3);
					return;
				}
				dialog.dismiss();
				if (!new_group_name.equals(str)) {
					CustomGroupUtil.getInstance().showProgressDialog(ContactActivity.this.mContext, ContactActivity.this.getResources().getString(R.string.progress_title), ContactActivity.this.getResources().getString(R.string.progress_message));
					ContactActivity.this.modifiedCustomGrp = pttCustomGrp2;
					ContactActivity.this.newGrpName = new_group_name;
					StringBuilder sBuilder = new StringBuilder();
					sBuilder.append(pttCustomGrp2.getGroupCreatorNum()).append(",").append(pttCustomGrp2.getGroupCreatorName()).append(",").append(pttCustomGrp2.getGroupNum()).append(",").append(new_group_name);
					ContactActivity.this.userAgent.SendCustomGroupMessage(4, sBuilder.toString().trim(), null, null, null, null);
				}
			}
		});
		btn_cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	private void modifyCustomGroupMap() {
		LogUtil.makeLog(TAG, "modifyCustomGroupMap()");
		this.userAgent = Receiver.GetCurUA();
		Map<String, PttCustomGrp> customGrpMap = this.userAgent.getAllCustomGroups();
		Map<String, String> map = this.userAgent.getCustomGroupMap();
		if (this.modifiedCustomGrp != null) {
			String groupNum = this.modifiedCustomGrp.getGroupNum();
			String groupName = this.modifiedCustomGrp.getGroupName();
			if (customGrpMap.containsKey(groupNum)) {
				this.modifiedCustomGrp.setGroupName(this.newGrpName);
				customGrpMap.put(groupNum, this.modifiedCustomGrp);
				CustomGroupUtil.getInstance().removeElementByKey2(groupName, map);
				map.put(this.newGrpName, groupNum);
				this.userAgent.updateAllCustomGroups(customGrpMap);
				this.userAgent.updateCustomGroupMap(map);
			}
		}
		CustomGroupUtil.getInstance().updateTalkBack(this.mContext);
	}

	private void showPopupWindow() {
		// TODO
	}

	public void makeTempGrpCall() {
		// TODO
	}

	private String getTemporaryName() {
		return new StringBuilder(String.valueOf(getResources().getString(R.string.ptt_grp))).append(getTime()).toString();
	}

	public String getTime() {
		try {
			return new SimpleDateFormat(" HHmmss ").format(new Date(System.currentTimeMillis()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void setDialogClosable(DialogInterface dialog, boolean isClosable) {
		try {
			Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, Boolean.valueOf(isClosable));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void dismissPopupWindow() {
		if (this.popupWindow != null) {
			this.popupWindow.dismiss();
		}
	}

	private List<String> getGroupData() {
		List<String> groupData = new ArrayList();
		groupData.add(getResources().getString(R.string.permanent_grp));
		return groupData;
	}

	private synchronized List<Map<String, String>> getPermanentChildrenData(HashMap<PttGrp, ArrayList<GroupListInfo>> permanentGroups) {
		List<Map<String, String>> permanentGrpData;
		LogUtil.makeLog(TAG, "getPermanentChildrenData()");
		this.grpmembercount = 0;
		permanentGrpData = new ArrayList();
		if (!permanentGroups.isEmpty()) {
			for (Entry<PttGrp, ArrayList<GroupListInfo>> entry : permanentGroups.entrySet()) {
				PttGrp pttGrp = (PttGrp) entry.getKey();
				if (pttGrp != null && pttGrp.getType() == 0) {
					ArrayList<GroupListInfo> groupInfos = (ArrayList) entry.getValue();
					this.permanentGrpMap.put(pttGrp.getGrpID(), groupInfos);
					this.permanentList.addAll(groupInfos);
					Map<String, String> map = new HashMap();
					map.put("curGrp_name", pttGrp.getGrpName());
					map.put("curGrp_ID", pttGrp.getGrpID());
					map.put("curGrp_member_count", new StringBuilder(String.valueOf(groupInfos.size())).toString());
					this.grpmembercount += groupInfos.size();
					permanentGrpData.add(map);
				}
			}
		}
		if (permanentGrpData != null && permanentGrpData.size() > 0) {
			LogUtil.makeLog(TAG, permanentGrpData.toString());
		}
		return permanentGrpData;
	}

	private List<Map<String, String>> getCustomChildrenData(Map<String, PttCustomGrp> customGroups) {
		LogUtil.makeLog(TAG, "getCustomChildrenData()");
		List<Map<String, String>> customGrpData = new ArrayList();
		if (!customGroups.isEmpty()) {
			for (Entry value : customGroups.entrySet()) {
				PttCustomGrp pttCustomGrp = (PttCustomGrp) value.getValue();
				Map<String, String> map = new HashMap();
				map.put("curGrp_name", pttCustomGrp.getGroupName());
				map.put("curGrp_ID", pttCustomGrp.getGroupNum());
				map.put("curGrp_member_count", new StringBuilder(String.valueOf(pttCustomGrp.getMember_list().size())).toString());
				customGrpData.add(map);
			}
			if (customGrpData != null && customGrpData.size() > 0) {
				LogUtil.makeLog(TAG, customGrpData.toString());
			}
		}
		return customGrpData;
	}

	protected void onDestroy() {
		LogUtil.makeLog(TAG, "onDestroy()");
		super.onDestroy();
		this.dbService.deleteObserver(this);
		if (this.intentFilter != null) {
			unregisterReceiver(this.customGroupStateReceiver);
		}
	}

	public void update(Observable observable, Object data) {
		// TODO
//		if (data != null && (data instanceof WorkArgs)) {
//			WorkArgs args = (WorkArgs) data;
//			switch ($SWITCH_TABLE$com$zed3$addressbook$DataBaseService$ChangedType()[args.type.ordinal()]) {
//				case 1:
//					List<Team> teams = args.object;
//					Message msg = new Message();
//					if (teams == null || teams.size() <= 0) {
//						msg.what = 0;
//					} else if (!SipUAApp.isDepartmentActivity) {
//						msg.what = 3;
//						msg.obj = teams;
//					} else {
//						return;
//					}
//					this.mHandler.sendMessage(msg);
//					return;
//				case 15:
//					MyLog.e("ee", " wo hai shi zou 3");
//					List<Map<String, String>> list = args.object;
//					if (list != null && list.size() > 0) {
//						MyLog.e("ee", " wo hai shi zou 4");
//						Message msg1 = new Message();
//						msg1.what = 4;
//						msg1.obj = list;
//						this.mHandler.sendMessage(msg1);
//						return;
//					}
//					return;
//				default:
//					return;
//			}
//		}
	}

	protected void onStart() {
		super.onStart();
	}

	protected void onResume() {
		MyLog.e("ee", "onResume");
		super.onResume();
		this.searchView.setIconified(true);
		this.dbService.addObserver(this);
		this.searchView.setIconified(true);
		if (!AddressBookUtils.ISREQUEST) {
			this.loadingView.setVisibility(View.GONE);
		}
		if (this.bottomLayout.getChildCount() == 0) {
			this.loadingView.setVisibility(View.VISIBLE);
			this.dbService.getAllTeams(1);
		}
		setBottomLayout(this.currentLayout);
	}

	protected void onPause() {
		MyLog.e("ee", " onPause");
		super.onPause();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode != 4) {
			return super.onKeyDown(keyCode, event);
		}
		Intent intent_ = new Intent("android.intent.action.MAIN");
		intent_.addCategory("android.intent.category.HOME");
		intent_.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent_);
		return false;
	}

	private List<Map<String, String>> getKeyGrpMembers(List<Map<String, String>> grplist, String newtext) {
		List<Map<String, String>> mmList = new ArrayList();
		for (Map<String, String> m : grplist) {
			if (((String) m.get(UserMinuteActivity.USER_MNAME)).contains(newtext) || ((String) m.get(UserMinuteActivity.USER_MNAME)).toLowerCase().contains(newtext.toLowerCase()) || ((String) m.get("number")).contains(newtext)) {
				mmList.add(m);
			}
		}
		return mmList;
	}

	private List<Map<String, String>> getGrpMembers() {
		List<Map<String, String>> mmList = new ArrayList();
		for (int i = 0; i < this.permanentList.size(); i++) {
			GroupListInfo groupListInfo = (GroupListInfo) this.permanentList.get(i);
			Map<String, String> mmMap = new HashMap();
			if (this.dbService.sameCopmany(groupListInfo.getGrpNum())) {
				mmMap = this.dbService.getMember(groupListInfo.getGrpNum());
			} else {
				String str = UserMinuteActivity.USER_MNAME;
				Object grpNum = (groupListInfo.getGrpName() == null || groupListInfo.getGrpName().equals("")) ? groupListInfo.getGrpNum() : groupListInfo.getGrpName();
				mmMap.put(str, (String) grpNum);
				mmMap.put("number", groupListInfo.getGrpNum());
			}
			if (!this.isQuery) {
				mmMap.put(ZhejiangReceivier.STATUS, groupListInfo.getGrpState());
			}
			mmList.add(mmMap);
		}
		return new ArrayList(new HashSet(mmList));
	}
}
