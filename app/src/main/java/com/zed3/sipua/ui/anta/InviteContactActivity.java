package com.zed3.sipua.ui.anta;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.message.MessageComposeActivity;
import com.zed3.sipua.ui.MeetingMem;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.toast.MyToast;
import com.zed3.utils.DialogMessageTool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InviteContactActivity extends Activity implements View.OnClickListener {
	private static final String TAG = "AntaCallActivity2SS";
	private static PopupWindow contactListPopupWindow;
	public static ArrayList<Linkman> mGridData;
	public static int mIndex;
	public static ArrayList<Linkman> mLinkmans;
	private static PopupWindow userListPopupWindow;
	private List<List<Linkman>> childData;
	private TextView confirm_select;
	private TextView confirm_select2;
	private List<Linkman> contactData;
	private ListView contactList;
	private View contactListClickView;
	public Map<String, Object> contactListClickedItem;
	private ScaleAnimation contactListHideSA;
	private View contactListPopupView;
	private ScaleAnimation contactListShowSA;
	private View contactListViews;
	private LinearLayout contactList_popup_add2userList;
	private LinearLayout contactList_popup_add2userList_views;
	private LinearLayout contactList_popup_added_views;
	private LinearLayout contactList_popup_cancel;
	private List<String> groupData;
	private View hideContactList;
	LinearLayout invite_back;
	public boolean isEditMode;
	private List<Map<String, Object>> linkData;
	private ContactListAdapter mAdapter;
	private MyGridViewAdapter mAdapter_;
	TextView mCancelSelectTV;
	ImageButton mCompleteButton;
	TextView mCompleteTv;
	private int mContactListIndex;
	private List<Map<String, Object>> mContacts;
	private Context mContext;
	private int mDataIndex;
	public boolean mExist;
	private GridView mGridView;
	private ViewGroup mRootView;
	private int mUserListIndex;
	private List<Map<String, Object>> mUsers;
	LinearLayout mettingline;
	private ScaleAnimation sa2;
	private ArrayList<String> selected;
	private View showContactList;
	private View userListClickView;
	public Map<String, Object> userListClickedItem;
	private View userListPopupView;
	private LinearLayout userList_popup_cancel;
	private LinearLayout userList_popup_move;

	static {
		InviteContactActivity.mIndex = -1;
		InviteContactActivity.mLinkmans = new ArrayList<Linkman>();
	}

	public InviteContactActivity() {
		this.mContactListIndex = -1;
		this.mUserListIndex = -1;
	}

	private void getGroupData() {
		this.groupData = new ArrayList<String>();
		this.childData = new ArrayList<List<Linkman>>();
		final ArrayList list = (ArrayList) GroupListUtil.getGroups();
//		for (int i = 0; i < list.size(); ++i) {
//			final ArrayList<Linkman> list2 = new ArrayList<Linkman>();
//			final PttGrp pttGrp = list.get(i);
//			final ArrayList<GroupListInfo> list3 = GroupListUtil.getGroupListsMap().get(pttGrp);
//			if (list3 != null && list3.size() != 0) {
//				int j = 0;
//				while (j < list3.size()) {
//					final GroupListInfo groupListInfo = list3.get(j);
//					while (true) {
//						if (groupListInfo == null) {
//							break Label_0183;
//						}
//						try {
//							final Linkman linkman = new Linkman();
//							linkman.name = groupListInfo.GrpName;
//							linkman.number = groupListInfo.GrpNum;
//							list2.add(linkman);
//							++j;
//						} catch (Exception ex) {
//							MyLog.e("AntaCallActivity2SS", "getGroupData fail");
//							ex.printStackTrace();
//							continue;
//						}
//						break;
//					}
//				}
//			}
//			this.groupData.add(pttGrp.grpName);
//			this.childData.add(list2);
//		}
	}

	private ArrayList<Linkman> getLinkmans(final List<Map<String, Object>> list) {
		InviteContactActivity.mLinkmans.clear();
		for (int i = 0; i < list.size(); ++i) {
			final Map<String, Object> map = list.get(i);
			if (map != null) {
				try {
					final Linkman linkman = new Linkman();
					linkman.name = (String) map.get("title");
					linkman.number = (String) map.get("info");
					for (int j = 0; j < InviteContactActivity.mGridData.size(); ++j) {
						if (InviteContactActivity.mGridData.get(j).number.equals(linkman.number)) {
							linkman.isSelected = true;
						}
					}
					InviteContactActivity.mLinkmans.add(linkman);
				} catch (Exception ex) {
					MyLog.e("AntaCallActivity2SS", "getGroupData fail");
					ex.printStackTrace();
				}
			}
		}
		return InviteContactActivity.mLinkmans;
	}

	private String getNumbers() {
		String string = "";
		String s;
		for (int i = 0; i < InviteContactActivity.mGridData.size(); ++i, string = s) {
			s = (string = String.valueOf(string) + " " + InviteContactActivity.mGridData.get(i).number);
			if (i > 32) {
				break;
			}
		}
		return string;
	}

	private void initNewUI() {
		(this.mCancelSelectTV = (TextView) this.findViewById(R.id.cancel_select_tv)).setOnClickListener((View.OnClickListener) this);
		(this.mettingline = (LinearLayout) this.findViewById(R.id.mettingline)).setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				InviteContactActivity.this.mCompleteTv = (TextView) InviteContactActivity.this.findViewById(R.id.complete_tv);
				switch (motionEvent.getAction()) {
					case 0: {
						InviteContactActivity.this.mCompleteTv.setBackgroundResource(R.color.btn_click_bg);
						InviteContactActivity.this.mCompleteTv.setTextColor(-1);
						return true;
					}
					case 1: {
						InviteContactActivity.this.mCompleteTv.setBackgroundResource(R.color.whole_bg);
						InviteContactActivity.this.mCompleteTv.setTextColor(InviteContactActivity.this.getResources().getColor(R.color.font_color3));
//						final String access .17 = InviteContactActivity.this.getNumbers();
//						if (!access .17.equals("")){
//							final InviteContactActivity this .0 = InviteContactActivity.this;
//							final Context applicationContext = InviteContactActivity.this.getApplicationContext();
//							final String string = InviteContactActivity.this.getResources().getString(R.string.add_member);
//							final StringBuilder append = new StringBuilder(String.valueOf(InviteContactActivity.this.getResources().getString(R.string.invit_member_notify_1))).append(" ").append(InviteContactActivity.mGridData.size()).append(" ");
//							String s;
//							if (InviteContactActivity.mGridData.size() <= 1) {
//								s = InviteContactActivity.this.getResources().getString(R.string.invit_member_notify_3);
//							} else {
//								s = InviteContactActivity.this.getResources().getString(R.string.invit_member_notify_2);
//							}
//							this
//							.0.showMakeMeetingDialog(applicationContext, string, append.append(s).toString(), access
//							.17);
//							return true;
//						}
						break;
					}
				}
				return true;
			}
		});
		(this.invite_back = (LinearLayout) this.findViewById(R.id.invite_back)).setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) InviteContactActivity.this.findViewById(R.id.t_invite_back);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setBackgroundResource(R.color.btn_click_bg);
						textView.setTextColor(-1);
						break;
					}
					case 1: {
						textView.setBackgroundResource(R.color.whole_bg);
						textView.setTextColor(InviteContactActivity.this.getResources().getColor(R.color.font_color3));
						break;
					}
				}
				return false;
			}
		});
		this.invite_back.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				MeetingMem.selectContact = null;
				InviteContactActivity.this.finish();
			}
		});
		this.mContext = (Context) this;
		this.selected = new ArrayList<String>();
		this.linkData = ContactUtil.getUsers();
		this.contactData = new ArrayList<Linkman>();
		for (int i = 0; i < this.linkData.size(); ++i) {
			final Linkman linkman = new Linkman();
			linkman.name = (String) this.linkData.get(i).get("title");
			linkman.number = (String) this.linkData.get(i).get("info");
			this.contactData.add(linkman);
		}
//		final MyHandler myHandler = new MyHandler((MyHandler) null);
//		final Message obtain = Message.obtain();
//		obtain.what = 2;
//		myHandler.sendMessageDelayed(obtain, 100L);
		if (this.contactData.size() > 0) {
			this.childData.add(this.contactData);
			this.groupData.add(this.getResources().getString(R.string.contact));
		}
		InviteContactActivity.mGridData = new ArrayList<Linkman>();
		this.updateBtn();
		this.mGridView = (GridView) this.findViewById(R.id.grid_selected_member);
//		final MyHandler myHandler2 = new MyHandler((MyHandler) null);
//		final Message obtain2 = Message.obtain();
//		obtain2.what = 1;
//		myHandler2.sendMessage(obtain2);
		this.mGridView.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			private Linkman linkman;

			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
//				final MyHandler myHandler = new MyHandler((MyHandler) null);
//				final Message obtain = Message.obtain();
//				obtain.what = 1;
//				myHandler.sendMessage(obtain);
				final String number = InviteContactActivity.mGridData.get(n).number;
				for (int i = 0; i < InviteContactActivity.mLinkmans.size(); ++i) {
					this.linkman = InviteContactActivity.mLinkmans.get(i);
					if (this.linkman.number.equals(number)) {
						this.linkman.isSelected = false;
					}
				}
				InviteContactActivity.this.selected.remove(InviteContactActivity.mGridData.get(n).number);
				InviteContactActivity.mGridData.remove(n);
				InviteContactActivity.this.updateBtn();
				InviteContactActivity.this.mAdapter.notifyDataSetChanged();
			}
		});
		this.confirm_select = (TextView) this.findViewById(R.id.confirm_select);
		(this.confirm_select2 = (TextView) this.findViewById(R.id.confirm_select2)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				InviteContactActivity.this.finish();
			}
		});
		this.confirm_select.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				if (InviteContactActivity.mGridData.size() < 1) {
					MyToast.showToast(true, InviteContactActivity.this.mContext, InviteContactActivity.this.getResources().getString(R.string.wrong_notify));
					return;
				}
				final StringBuffer sb = new StringBuffer("");
				final StringBuffer sb2 = new StringBuffer("");
				final int size = InviteContactActivity.mGridData.size();
				int n = 0;
				for (final Linkman linkman : InviteContactActivity.mGridData) {
					final int n2 = n + 1;
					sb.append(linkman.number);
					sb2.append(linkman.name);
					if ((n = n2) < size) {
						sb.append(";");
						sb2.append(";");
						n = n2;
					}
				}
				final String string = sb.toString();
				final String string2 = sb2.toString();
				final Intent intent = new Intent(InviteContactActivity.this.mContext, (Class) MessageComposeActivity.class);
				intent.putExtra("name", string2);
				intent.putExtra("number", string);
				if (string2.contains(";")) {
					intent.putExtra("type", "mass");
				}
				InviteContactActivity.this.startActivity(intent);
				InviteContactActivity.this.finish();
			}
		});
	}

	private void showMakeMeetingDialog(final Context context, final String text, final String s, final String s2) {
		final AlertDialog create = new AlertDialog.Builder((Context) this).create();
		create.show();
		final Window window = create.getWindow();
		window.setContentView(R.layout.shrew_exit_dialog);
		final TextView textView = (TextView) window.findViewById(R.id.btn_ok);
		if (text.equals(this.getResources().getString(R.string.begin_conference))) {
			textView.setText(R.string.start_conference_ok);
		} else if (text.equals(this.getResources().getString(R.string.add_member))) {
			textView.setText(R.string.invite);
		}
		final TextView textView2 = (TextView) window.findViewById(R.id.contact_user_title);
		final TextView textView3 = (TextView) window.findViewById(R.id.msg_tv);
		textView2.setText((CharSequence) text);
		if (this.mContext == null) {
			this.mContext = (Context) this;
		}
		textView3.setText((CharSequence) DialogMessageTool.getString((int) (this.mContext.getResources().getDisplayMetrics().density * 296.0f + 0.5f), textView3.getTextSize(), s));
		textView.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				create.dismiss();
				final StringBuffer sb = new StringBuffer();
				final Iterator<Linkman> iterator = InviteContactActivity.mGridData.iterator();
				while (iterator.hasNext()) {
					sb.append("*2*").append(iterator.next().number).append("*");
				}
				if (TextUtils.isEmpty((CharSequence) sb.toString())) {
					MeetingMem.toSend = "";
					MeetingMem.inviteContact = null;
				} else {
					MeetingMem.toSend = sb.toString();
					MeetingMem.inviteContact = InviteContactActivity.mGridData;
				}
				InviteContactActivity.this.finish();
			}
		});
		((TextView) window.findViewById(R.id.btn_cancel)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				create.cancel();
			}
		});
	}

	private void updateBtn() {
		this.mCompleteTv = (TextView) this.findViewById(R.id.complete_tv);
		if (InviteContactActivity.mGridData.size() < 1) {
			this.mettingline.setEnabled(false);
			this.mCompleteTv.setTextColor(this.getResources().getColor(R.color.font_color2));
			return;
		}
		this.mettingline.setEnabled(true);
		this.mCompleteTv.setTextColor(this.getResources().getColor(R.color.font_color3));
	}

	public void onClick(final View view) {
		final boolean b = false;
		final boolean b2 = false;
		switch (view.getId()) {
			default: {
			}
			case R.id.complete_tv: {
				final String numbers = this.getNumbers();
				if (!numbers.equals("")) {
					final Context applicationContext = this.getApplicationContext();
					final String string = this.getResources().getString(R.string.begin_conference);
					final StringBuilder append = new StringBuilder(String.valueOf(this.getResources().getString(R.string.start_conference_notify_1))).append(" ").append(InviteContactActivity.mGridData.size()).append(" ");
					String s;
					if (InviteContactActivity.mGridData.size() <= 1) {
						s = this.getResources().getString(R.string.start_conference_notify_3);
					} else {
						s = this.getResources().getString(R.string.start_conference_notify_2);
					}
					this.showMakeMeetingDialog(applicationContext, string, append.append(s).toString(), numbers);
					return;
				}
				Toast.makeText(this.getApplicationContext(), (CharSequence) this.getResources().getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
			}
			case R.id.select_iv: {
				final SelectTag selectTag = (SelectTag) view.getTag();
				final int position = selectTag.getPosition();
				selectTag.isSelected();
				final Linkman linkman = InviteContactActivity.mLinkmans.get(position);
				final boolean isSelected = linkman.isSelected;
				if (isSelected) {
					selectTag.setSelected(!isSelected);
					linkman.isSelected = (!isSelected || b2);
					view.setBackgroundResource(R.drawable.select_off);
					if (InviteContactActivity.mGridData.contains(linkman)) {
						InviteContactActivity.mGridData.remove(linkman);
						this.updateBtn();
					}
//					final MyHandler myHandler = new MyHandler((MyHandler) null);
//					final Message obtain = Message.obtain();
//					obtain.what = 1;
//					myHandler.sendMessage(obtain);
					return;
				}
				selectTag.setSelected(!isSelected);
				linkman.isSelected = (!isSelected || b);
				view.setBackgroundResource(R.drawable.select_on);
				InviteContactActivity.mGridData.add(linkman);
				this.updateBtn();
//				final MyHandler myHandler2 = new MyHandler((MyHandler) null);
//				final Message obtain2 = Message.obtain();
//				obtain2.what = 1;
//				myHandler2.sendMessage(obtain2);
			}
		}
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.mContext = (Context) this;
		(this.mRootView = (ViewGroup) this.getWindow().getLayoutInflater().inflate(R.layout.invite_contact, (ViewGroup) null)).setOnClickListener((View.OnClickListener) this);
		this.setContentView((View) this.mRootView);
		this.contactList = (ListView) this.mRootView.findViewById(R.id.contact_list);
		this.initNewUI();
	}

	protected void onResume() {
//		final MyHandler myHandler = new MyHandler((MyHandler) null);
//		final Message obtain = Message.obtain();
//		obtain.what = 3;
//		myHandler.sendMessageDelayed(obtain, 50L);
		super.onResume();
	}

	List<Map<String, Object>> reSetFilledData(final List<Map<String, Object>> list, final ArrayList<Linkman> list2) {
//		final ArrayList<Object> list3 = new ArrayList<Object>();
//		final ArrayList<Map<String, Object>> list4 = (ArrayList<Map<String, Object>>) new ArrayList<Object>();
//		for (int i = 0; i < list.size(); ++i) {
//			final Map<String, Object> map = list.get(i);
//			for (int j = 0; j < list2.size(); ++j) {
//				final Linkman linkman = list2.get(j);
//				if (map.get("title").equals(linkman.name) && map.get("info").equals(linkman.number)) {
//					list3.add(map);
//					final HashMap<String, String> hashMap = new HashMap<String, String>();
//					hashMap.put("title", linkman.name);
//					hashMap.put("info", linkman.number);
//					hashMap.put("disable", "true");
//					list4.add((Map<String, Object>) hashMap);
//				}
//			}
//		}
//		list.removeAll(list3);
//		if (list4.size() > 0) {
//			final Iterator<Object> iterator = list4.iterator();
//			while (iterator.hasNext()) {
//				list.add(0, iterator.next());
//			}
//		}
		return list;
	}

	class CallListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
//			InviteContactActivity.access .5 (InviteContactActivity.this, n);
//			InviteContactActivity.access .6 (InviteContactActivity.this, view);
//			InviteContactActivity.this.userListClickedItem = InviteContactActivity.this.mUsers.get(n);
//			InviteContactActivity.access .4 (InviteContactActivity.this, n);
		}
	}

	private class CellHolder {
		TextView name;
		TextView number;
	}

	class ContactListAdapter extends BaseAdapter {
		private List<Map<String, Object>> mData;
		private LayoutInflater mInflater;
		private String tag;

		public ContactListAdapter(final List<Map<String, Object>> mData) {
			this.tag = "ContactListAdapter";
			this.mData = mData;
			this.mInflater = LayoutInflater.from(InviteContactActivity.this.mContext);
		}

		public boolean areAllItemsEnabled() {
			return true;
		}

		public int getCount() {
			return this.mData.size();
		}

		public Object getItem(final int n) {
			return this.mData.get(n);
		}

		public long getItemId(final int n) {
			return n;
		}

		public View getView(int n, View inflate, final ViewGroup viewGroup) {
			ViewHolder tag;
			if (inflate == null) {
				tag = new ViewHolder();
				inflate = this.mInflater.inflate(R.layout.anta_contact_list_item_2, (ViewGroup) null);
				inflate.setSelected(true);
				inflate.setEnabled(true);
				tag.img = (ImageView) inflate.findViewById(R.id.img);
				tag.title = (TextView) inflate.findViewById(R.id.title);
				tag.info = (TextView) inflate.findViewById(R.id.info);
				tag.select = (ImageView) inflate.findViewById(R.id.select_iv);
			} else {
				tag = (ViewHolder) inflate.getTag();
			}
			final Map<String, Object> map = this.mData.get(n);
			Log.i(this.tag, "position = " + n);
			tag.title.setText((CharSequence) map.get("title"));
			tag.info.setText((CharSequence) this.mData.get(n).get("info"));
			if ("true".equals(this.mData.get(n).get("disable"))) {
				tag.select.setBackgroundResource(R.drawable.select_on);
				tag.select.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
					public void onClick(final View view) {
						Toast.makeText((Context) InviteContactActivity.this, (CharSequence) InviteContactActivity.this.getResources().getString(R.string.select_contact_notify), Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				final boolean isSelected = InviteContactActivity.mLinkmans.get(n).isSelected;
				final SelectTag tag2 = new SelectTag();
				tag2.setSelected(isSelected);
				tag2.setPosition(n);
				tag.select.setTag((Object) tag2);
				final ImageView select = tag.select;
				if (isSelected) {
					n = R.drawable.select_on;
				} else {
					n = R.drawable.select_off;
				}
				select.setBackgroundResource(n);
				tag.select.setOnClickListener((View.OnClickListener) InviteContactActivity.this);
			}
			inflate.setTag((Object) tag);
			return inflate;
		}

		public boolean isEnabled(final int n) {
			return super.isEnabled(n);
		}

		public void setData(final List<Map<String, Object>> mData) {
			this.mData = mData;
		}
	}

	class ContactListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
//			InviteContactActivity.access .1 (InviteContactActivity.this, n);
//			InviteContactActivity.access .2 (InviteContactActivity.this, view);
//			InviteContactActivity.this.contactListClickedItem = InviteContactActivity.this.mContacts.get(n);
//			InviteContactActivity.this.mExist = AntaCallUtil.checkExist(InviteContactActivity.this.contactListClickedItem);
//			InviteContactActivity.access .4 (InviteContactActivity.this, n);
		}
	}

	private class MyGridViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyGridViewAdapter(final Context context) {
			this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return InviteContactActivity.mGridData.size();
		}

		public Object getItem(final int n) {
			return InviteContactActivity.mGridData.get(n);
		}

		public long getItemId(final int n) {
			return n;
		}

		public View getView(final int n, View inflate, final ViewGroup viewGroup) {
			final Linkman linkman = InviteContactActivity.mGridData.get(n);
			CellHolder tag = null;
			if (inflate == null) {
//				tag = new CellHolder((CellHolder) null);
				inflate = this.mInflater.inflate(R.layout.custom_gridview_item, (ViewGroup) null);
				tag.name = (TextView) inflate.findViewById(R.id.custom_name);
				tag.number = (TextView) inflate.findViewById(R.id.custom_number);
				inflate.setTag((Object) tag);
			} else {
				tag = (CellHolder) inflate.getTag();
			}
			tag.name.setText((CharSequence) linkman.name);
			tag.number.setText((CharSequence) linkman.number);
			return inflate;
		}
	}

	private class MyHandler extends Handler {
		public void handleMessage(final Message message) {
			switch (message.what) {
				default: {
				}
				case 1: {
					if (InviteContactActivity.mGridData == null || InviteContactActivity.mGridData.size() == 0) {
//						InviteContactActivity.access .8
//						(InviteContactActivity.this, new MyGridViewAdapter(InviteContactActivity.this.mContext))
//						;
						InviteContactActivity.this.mGridView.setAdapter((ListAdapter) InviteContactActivity.this.mAdapter_);
						return;
					}
					InviteContactActivity.this.mGridView.setLayoutParams((ViewGroup.LayoutParams) new LinearLayout.LayoutParams(InviteContactActivity.mGridData.size() * 122 + 10, -2));
					InviteContactActivity.this.mGridView.setColumnWidth(120);
					InviteContactActivity.this.mGridView.setHorizontalSpacing(2);
//					InviteContactActivity.this.mGridView.setStretchMode(0);
					InviteContactActivity.this.mGridView.setNumColumns(InviteContactActivity.mGridData.size());
//					InviteContactActivity.access .8
//					(InviteContactActivity.this, new MyGridViewAdapter(InviteContactActivity.this.mContext))
//					;
//					InviteContactActivity.this.mGridView.setAdapter((ListAdapter) InviteContactActivity.this.mAdapter_);
				}
				case 2: {
					InviteContactActivity.this.getGroupData();
				}
				case 3: {
//					InviteContactActivity.access .12
//					(InviteContactActivity.this, MeetingCompareTool.getInstance().sortByDefault(AntaCallUtil.getContacts()))
//					;
//					InviteContactActivity.access .12
//					(InviteContactActivity.this, InviteContactActivity.this.reSetFilledData(InviteContactActivity.this.mContacts, MeetingMem.selectContact))
//					;
					InviteContactActivity.mLinkmans = InviteContactActivity.this.getLinkmans(InviteContactActivity.this.mContacts);
					if (InviteContactActivity.this.mAdapter == null) {
//						InviteContactActivity.access .15
//						(InviteContactActivity.this, new ContactListAdapter(InviteContactActivity.this.mContacts))
//						;
						InviteContactActivity.this.mAdapter.setData(InviteContactActivity.this.mContacts);
						InviteContactActivity.this.contactList.setAdapter((ListAdapter) InviteContactActivity.this.mAdapter);
						return;
					}
					InviteContactActivity.this.mAdapter.setData(InviteContactActivity.this.mContacts);
					InviteContactActivity.this.mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	public class SelectTag {
		private boolean isSelected;
		private int position;

		public int getPosition() {
			return this.position;
		}

		public boolean isSelected() {
			return this.isSelected;
		}

		public void setPosition(final int position) {
			this.position = position;
		}

		public void setSelected(final boolean isSelected) {
			this.isSelected = isSelected;
		}
	}

	public final class ViewHolder {
		public View add;
		public ImageView img;
		public TextView info;
		public View remove;
		public ImageView select;
		public TextView title;
	}
}
