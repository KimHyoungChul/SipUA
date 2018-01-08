package com.zed3.sipua.ui.anta;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.message.MessageComposeActivity;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.toast.MyToast;
import com.zed3.utils.DialogMessageTool;
import com.zed3.utils.Tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AntaCallActivity2 extends Activity implements OnClickListener {
	private static final String TAG = "AntaCallActivity2SS";
	private static PopupWindow contactListPopupWindow;
	private static int currentClickedViewPosition;
	public static boolean isClicked = false;
	private static boolean isCreated;
	public static ArrayList<Linkman> mGridData;
	public static int mIndex = -1;
	public static ArrayList<Linkman> mLinkmans = new ArrayList();
	private static PopupWindow userListPopupWindow;
	private ImageButton back_btn;
	private ListView callList;
	LinearLayout cancelline;
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
	private LinearLayout contactList_popup_added;
	private LinearLayout contactList_popup_added_views;
	private LinearLayout contactList_popup_cancel;
	private ImageButton contact_btn;
	private View currentClickedView;
	private View editModePopupView;
	private List<String> groupData;
	private String groupName;
	private View hideContactList;
	public boolean isEditMode;
	private List<Map<String, Object>> linkData;
	private ContactListAdapter mAdapter;
	private MyGridViewAdapter mAdapter_;
	TextView mCancelSelectTV;
	ImageButton mCompleteButton;
	TextView mCompleteTv;
	private ArrayList<Linkman> mContactData;
	private int mContactListIndex = -1;
	private List<Map<String, Object>> mContacts;
	private AntaCallActivity2 mContext;
	private int mDataIndex;
	public boolean mExist;
	private GridView mGridView;
	private ViewGroup mRootView;
	private int mUserListIndex = -1;
	private List<Map<String, Object>> mUsers;
	private View makeConferenceCall;
	private View makeGroupBroadcastCall;
	LinearLayout mettingline;
	private boolean needAddContactMenu = true;
	private LinearLayout popupDelectCancel;
	private LinearLayout popupDelectDelect;
	private BroadcastReceiver receiver;
	private ScaleAnimation sa1;
	private ScaleAnimation sa2;
	private ArrayList<String> selected;
	private View showContactList;
	private View userAdd;
	private View userListClickView;
	public Map<String, Object> userListClickedItem;
	private View userListPopupView;
	private LinearLayout userList_popup_cancel;
	private LinearLayout userList_popup_move;


	class C12181 implements OnTouchListener {
		TextView tv;

		C12181() {
			this.tv = (TextView) AntaCallActivity2.this.findViewById(R.id.cancel_tv);
		}

		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case 0:
					this.tv.setBackgroundResource(R.color.btn_click_bg);
					this.tv.setTextColor(-1);
					break;
				case 1:
					this.tv.setBackgroundResource(R.color.whole_bg);
					this.tv.setTextColor(AntaCallActivity2.this.getResources().getColor(R.color.font_color3));
					break;
			}
			return false;
		}
	}

	class C12192 implements OnTouchListener {
		C12192() {
		}

		public boolean onTouch(View v, MotionEvent event) {
			AntaCallActivity2.this.mCompleteTv = (TextView) AntaCallActivity2.this.findViewById(R.id.complete_tv);
			switch (event.getAction()) {
				case 0:
					AntaCallActivity2.this.mCompleteTv.setBackgroundResource(R.color.btn_click_bg);
					AntaCallActivity2.this.mCompleteTv.setTextColor(-1);
					AntaCallActivity2.isClicked = true;
					break;
				case 1:
					AntaCallActivity2.this.mCompleteTv.setBackgroundResource(R.color.whole_bg);
					AntaCallActivity2.this.mCompleteTv.setTextColor(AntaCallActivity2.this.getResources().getColor(R.color.font_color3));
					String numberString = AntaCallActivity2.this.getNumbers();
					if (!numberString.equals("")) {
						String string;
						AntaCallActivity2 antaCallActivity2 = AntaCallActivity2.this;
						Context applicationContext = AntaCallActivity2.this.getApplicationContext();
						String string2 = AntaCallActivity2.this.getResources().getString(R.string.begin_conference);
						StringBuilder append = new StringBuilder(String.valueOf(AntaCallActivity2.this.getResources().getString(R.string.start_conference_notify_1))).append(" ").append(AntaCallActivity2.mGridData.size()).append(" ");
						if (AntaCallActivity2.mGridData.size() <= 1) {
							string = AntaCallActivity2.this.getResources().getString(R.string.start_conference_notify_3);
						} else {
							string = AntaCallActivity2.this.getResources().getString(R.string.start_conference_notify_2);
						}
						antaCallActivity2.showMakeMeetingDialog(applicationContext, string2, append.append(string).toString(), numberString);
						break;
					}
					Toast.makeText(AntaCallActivity2.this.getApplicationContext(), AntaCallActivity2.this.getResources().getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
					break;
			}
			return true;
		}
	}

	class C12203 implements OnItemClickListener {
		private Linkman linkman;

		C12203() {
		}

		public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
			MyHandler mh = new MyHandler();
			Message msg = Message.obtain();
			msg.what = 1;
			mh.sendMessage(msg);
			String number = ((Linkman) AntaCallActivity2.mGridData.get(position)).number;
			for (int i = 0; i < AntaCallActivity2.mLinkmans.size(); i++) {
				this.linkman = (Linkman) AntaCallActivity2.mLinkmans.get(i);
				if (this.linkman.number.equals(number)) {
					this.linkman.isSelected = false;
				}
			}
			AntaCallActivity2.this.selected.remove(((Linkman) AntaCallActivity2.mGridData.get(position)).number);
			AntaCallActivity2.mGridData.remove(position);
			AntaCallActivity2.this.updateBtn();
			AntaCallActivity2.this.mAdapter.notifyDataSetChanged();
		}
	}

	class C12214 implements OnClickListener {
		C12214() {
		}

		public void onClick(View v) {
			AntaCallActivity2.this.finish();
		}
	}

	class C12225 implements OnClickListener {
		C12225() {
		}

		public void onClick(View v) {
			if (AntaCallActivity2.mGridData.size() < 1) {
				MyToast.showToast(true, AntaCallActivity2.this.mContext, AntaCallActivity2.this.getResources().getString(R.string.wrong_notify));
				return;
			}
			StringBuffer sb = new StringBuffer("");
			StringBuffer dsb = new StringBuffer("");
			int a = AntaCallActivity2.mGridData.size();
			int b = 0;
			Iterator it = AntaCallActivity2.mGridData.iterator();
			while (it.hasNext()) {
				Linkman man = (Linkman) it.next();
				b++;
				sb.append(man.number);
				dsb.append(man.name);
				if (b < a) {
					sb.append(";");
					dsb.append(";");
				}
			}
			String memberNumber = sb.toString();
			String memberName = dsb.toString();
			Intent intent = new Intent(AntaCallActivity2.this.mContext, MessageComposeActivity.class);
			intent.putExtra("name", memberName);
			intent.putExtra("number", memberNumber);
			if (memberName.contains(";")) {
				intent.putExtra("type", "mass");
			}
			AntaCallActivity2.this.startActivity(intent);
			AntaCallActivity2.this.finish();
		}
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
		AntaCallActivity2.mLinkmans.clear();
//		for (int i = 0; i < list.size(); ++i) {
//			final Map<String, Object> map = list.get(i);
//			if (map != null) {
//				try {
//					final Linkman linkman = new Linkman();
//					linkman.name = map.get("title");
//					linkman.number = map.get("info");
//					for (int j = 0; j < AntaCallActivity2.mGridData.size(); ++j) {
//						if (AntaCallActivity2.mGridData.get(j).number.equals(linkman.number)) {
//							linkman.isSelected = true;
//						}
//					}
//					AntaCallActivity2.mLinkmans.add(linkman);
//				} catch (Exception ex) {
//					MyLog.e("AntaCallActivity2SS", "getGroupData fail");
//					ex.printStackTrace();
//				}
//			}
//		}
		return AntaCallActivity2.mLinkmans;
	}

	private String getNumbers() {
		String string = "";
		String s;
		for (int i = 0; i < AntaCallActivity2.mGridData.size(); ++i, string = s) {
			s = (string = String.valueOf(string) + " " + AntaCallActivity2.mGridData.get(i).number);
			if (i > 32) {
				break;
			}
		}
		return string;
	}

	public static String getVersion(final Context context) {
		String versionName;
		if (context == null) {
			versionName = "Unknown";
		} else {
			try {
				final String s = versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
				if (s.contains(" + ")) {
					return String.valueOf(s.substring(0, s.indexOf(" + "))) + "b";
				}
			} catch (PackageManager.NameNotFoundException ex) {
				return "Unknown";
			}
		}
		return versionName;
	}

	private void initNewUI() {
		(this.mCancelSelectTV = (TextView) this.findViewById(R.id.cancel_select_tv)).setOnClickListener((View.OnClickListener) this);
		(this.cancelline = (LinearLayout) this.findViewById(R.id.cancel_line)).setOnClickListener((View.OnClickListener) this);
		this.cancelline.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			TextView tv = (TextView) AntaCallActivity2.this.findViewById(R.id.cancel_tv);

			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				switch (motionEvent.getAction()) {
					case 0: {
						this.tv.setBackgroundResource(R.color.btn_click_bg);
						this.tv.setTextColor(-1);
						break;
					}
					case 1: {
						this.tv.setBackgroundResource(R.color.whole_bg);
						this.tv.setTextColor(AntaCallActivity2.this.getResources().getColor(R.color.font_color3));
						break;
					}
				}
				return false;
			}
		});
		(this.mettingline = (LinearLayout) this.findViewById(R.id.mettingline)).setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				AntaCallActivity2.this.mCompleteTv = (TextView) AntaCallActivity2.this.findViewById(R.id.complete_tv);
				switch (motionEvent.getAction()) {
					default: {
						return true;
					}
					case 0: {
						AntaCallActivity2.this.mCompleteTv.setBackgroundResource(R.color.btn_click_bg);
						AntaCallActivity2.this.mCompleteTv.setTextColor(-1);
						return AntaCallActivity2.isClicked = true;
					}
					case 1: {
						AntaCallActivity2.this.mCompleteTv.setBackgroundResource(R.color.whole_bg);
						AntaCallActivity2.this.mCompleteTv.setTextColor(AntaCallActivity2.this.getResources().getColor(R.color.font_color3));
//						final String access .17 = AntaCallActivity2.this.getNumbers();
//						if (!access .17.equals("")){
//							final AntaCallActivity2 this .0 = AntaCallActivity2.this;
//							final Context applicationContext = AntaCallActivity2.this.getApplicationContext();
//							final String string = AntaCallActivity2.this.getResources().getString(R.string.begin_conference);
//							final StringBuilder append = new StringBuilder(String.valueOf(AntaCallActivity2.this.getResources().getString(R.string.start_conference_notify_1))).append(" ").append(AntaCallActivity2.mGridData.size()).append(" ");
//							String s;
//							if (AntaCallActivity2.mGridData.size() <= 1) {
//								s = AntaCallActivity2.this.getResources().getString(R.string.start_conference_notify_3);
//							} else {
//								s = AntaCallActivity2.this.getResources().getString(R.string.start_conference_notify_2);
//							}
//							this
//							.0.showMakeMeetingDialog(applicationContext, string, append.append(s).toString(), access
//							.17);
//							return true;
//						}
						Toast.makeText(AntaCallActivity2.this.getApplicationContext(), (CharSequence) AntaCallActivity2.this.getResources().getString(R.string.no_selected), Toast.LENGTH_SHORT).show();
						return true;
					}
				}
			}
		});
		this.mContext = this;
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
//		if (this.contactData.size() > 0) {
//			this.childData.add(this.contactData);
//			this.groupData.add(this.getResources().getString(R.string.contact));
//		}
//		new Linkman();
//		AntaCallActivity2.mGridData = new ArrayList<Linkman>();
//		this.updateBtn();
//		this.mGridView = (GridView) this.findViewById(R.id.grid_selected_member);
//		final MyHandler myHandler2 = new MyHandler((MyHandler) null);
//		final Message obtain2 = Message.obtain();
//		obtain2.what = 1;
//		myHandler2.sendMessage(obtain2);
//		this.mGridView.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
//			private Linkman linkman;
//
//			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
//				final MyHandler myHandler = new MyHandler((MyHandler) null);
//				final Message obtain = Message.obtain();
//				obtain.what = 1;
//				myHandler.sendMessage(obtain);
//				final String number = AntaCallActivity2.mGridData.get(n).number;
//				for (int i = 0; i < AntaCallActivity2.mLinkmans.size(); ++i) {
//					this.linkman = AntaCallActivity2.mLinkmans.get(i);
//					if (this.linkman.number.equals(number)) {
//						this.linkman.isSelected = false;
//					}
//				}
//				AntaCallActivity2.this.selected.remove(AntaCallActivity2.mGridData.get(n).number);
//				AntaCallActivity2.mGridData.remove(n);
//				AntaCallActivity2.this.updateBtn();
//				AntaCallActivity2.this.mAdapter.notifyDataSetChanged();
//			}
//		});
		this.confirm_select = (TextView) this.findViewById(R.id.confirm_select);
		(this.confirm_select2 = (TextView) this.findViewById(R.id.confirm_select2)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				AntaCallActivity2.this.finish();
			}
		});
		this.confirm_select.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				if (AntaCallActivity2.mGridData.size() < 1) {
					MyToast.showToast(true, (Context) AntaCallActivity2.this.mContext, AntaCallActivity2.this.getResources().getString(R.string.wrong_notify));
					return;
				}
				final StringBuffer sb = new StringBuffer("");
				final StringBuffer sb2 = new StringBuffer("");
				final int size = AntaCallActivity2.mGridData.size();
				int n = 0;
				for (final Linkman linkman : AntaCallActivity2.mGridData) {
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
				final Intent intent = new Intent((Context) AntaCallActivity2.this.mContext, (Class) MessageComposeActivity.class);
				intent.putExtra("name", string2);
				intent.putExtra("number", string);
				if (string2.contains(";")) {
					intent.putExtra("type", "mass");
				}
				AntaCallActivity2.this.startActivity(intent);
				AntaCallActivity2.this.finish();
			}
		});
	}

	private void showMakeMeetingDialog(final Context context, final String text, final String s, final String s2) {
		if (CallUtil.checkGsmCallInCall()) {
			MyToast.showToast(true, SipUAApp.mContext, R.string.gsm_in_call);
			return;
		}
		final AlertDialog create = new AlertDialog.Builder((Context) this).create();
		create.show();
		final Window window = create.getWindow();
		window.setContentView(R.layout.shrew_exit_dialog);
		final TextView textView = (TextView) window.findViewById(R.id.btn_ok);
		final TextView textView2 = (TextView) window.findViewById(R.id.contact_user_title);
		final TextView textView3 = (TextView) window.findViewById(R.id.msg_tv);
		textView2.setText((CharSequence) text);
		if (this.mContext == null) {
			this.mContext = this;
		}
		textView3.setText((CharSequence) DialogMessageTool.getString((int) (this.mContext.getResources().getDisplayMetrics().density * 296.0f + 0.5f), textView3.getTextSize(), s));
		textView.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				create.dismiss();
				AntaCallUtil.makeAntaCall(false, s2);
			}
		});
		((TextView) window.findViewById(R.id.btn_cancel)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				create.cancel();
			}
		});
	}

	private void updateBtn() {
		final TextView textView = (TextView) this.findViewById(R.id.cancel_tv);
		this.mCompleteTv = (TextView) this.findViewById(R.id.complete_tv);
		if (AntaCallActivity2.mGridData.size() < 1) {
			this.cancelline.setEnabled(false);
			this.mettingline.setEnabled(false);
			textView.setTextColor(this.getResources().getColor(R.color.font_color2));
			this.mCompleteTv.setTextColor(this.getResources().getColor(R.color.font_color2));
			return;
		}
		this.cancelline.setEnabled(true);
		this.mettingline.setEnabled(true);
		textView.setTextColor(this.getResources().getColor(R.color.font_color3));
		this.mCompleteTv.setTextColor(this.getResources().getColor(R.color.font_color3));
	}

	void clearListSelect() {
		if (AntaCallActivity2.mLinkmans != null && AntaCallActivity2.mLinkmans.size() >= 1) {
			for (final Linkman linkman : AntaCallActivity2.mLinkmans) {
				if (linkman.isSelected) {
					linkman.isSelected = false;
				}
			}
			if (this.mAdapter != null) {
				this.mAdapter.notifyDataSetChanged();
			}
		}
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
					final String string = this.getResources().getString(R.string.start_conference);
					final StringBuilder append = new StringBuilder(String.valueOf(this.getResources().getString(R.string.start_conference_notify_1))).append(" ").append(AntaCallActivity2.mGridData.size()).append(" ");
					String s;
					if (AntaCallActivity2.mGridData.size() <= 1) {
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
				final Map<String, Object> map = this.mContacts.get(position);
				final Linkman linkman = AntaCallActivity2.mLinkmans.get(position);
				final boolean isSelected = linkman.isSelected;
				if (isSelected) {
					selectTag.setSelected(!isSelected);
					linkman.isSelected = (!isSelected || b2);
					view.setBackgroundResource(R.drawable.select_off);
					if (AntaCallActivity2.mGridData.contains(linkman)) {
						AntaCallActivity2.mGridData.remove(linkman);
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
				AntaCallActivity2.mGridData.add(linkman);
				this.updateBtn();
//				final MyHandler myHandler2 = new MyHandler((MyHandler) null);
//				final Message obtain2 = Message.obtain();
//				obtain2.what = 1;
//				myHandler2.sendMessage(obtain2);
			}
			case R.id.cancel_line: {
				if (AntaCallActivity2.mGridData != null) {
					AntaCallActivity2.mGridData.clear();
					this.updateBtn();
				}
//				final MyHandler myHandler3 = new MyHandler((MyHandler) null);
//				final Message obtain3 = Message.obtain();
//				obtain3.what = 1;
//				myHandler3.sendMessage(obtain3);
				this.clearListSelect();
			}
		}
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.mContext = this;
		(this.mRootView = (ViewGroup) this.getWindow().getLayoutInflater().inflate(R.layout.antacall2, (ViewGroup) null)).setOnClickListener((View.OnClickListener) this);
		this.setContentView((View) this.mRootView);
		this.contactList = (ListView) this.mRootView.findViewById(R.id.contact_list);
		AntaCallActivity2.isClicked = false;
		this.initNewUI();
	}

	public boolean onCreateOptionsMenu(final Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(final MenuItem menuItem) {
		final boolean onOptionsItemSelected = super.onOptionsItemSelected(menuItem);
		switch (menuItem.getItemId()) {
			default: {
				return onOptionsItemSelected;
			}
			case 1: {
				Tools.exitApp((Context) this);
				return onOptionsItemSelected;
			}
		}
	}

	protected void onResume() {
//		final MyHandler myHandler = new MyHandler((MyHandler) null);
//		final Message obtain = Message.obtain();
//		obtain.what = 3;
//		myHandler.sendMessageDelayed(obtain, 100L);
//		final MyHandler myHandler2 = new MyHandler((MyHandler) null);
//		final Message obtain2 = Message.obtain();
//		obtain2.what = 1;
//		myHandler2.sendMessage(obtain2);
		super.onResume();
	}

	class CallListOnItemClickListener implements OnItemClickListener {
		public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
//			AntaCallActivity2.access .5 (AntaCallActivity2.this, n);
//			AntaCallActivity2.access .6 (AntaCallActivity2.this, view);
//			AntaCallActivity2.this.userListClickedItem = AntaCallActivity2.this.mUsers.get(n);
//			AntaCallActivity2.access .4 (AntaCallActivity2.this, n);
		}
	}

	private class CellHolder {
		TextView name;
		TextView number;

		private CellHolder() {
		}
	}

	class ContactListAdapter extends BaseAdapter {
		private List<Map<String, Object>> mData;
		private LayoutInflater mInflater;
		private String tag;

		public ContactListAdapter(final List<Map<String, Object>> mData) {
			this.tag = "ContactListAdapter";
			this.mData = mData;
			this.mInflater = LayoutInflater.from((Context) AntaCallActivity2.this.mContext);
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
				(tag.select = (ImageView) inflate.findViewById(R.id.select_iv)).setOnClickListener((View.OnClickListener) AntaCallActivity2.this);
			} else {
				tag = (ViewHolder) inflate.getTag();
			}
			final Map<String, Object> map = this.mData.get(n);
			Log.i(this.tag, "position = " + n);
			tag.title.setText((CharSequence) map.get("title"));
			tag.info.setText((CharSequence) this.mData.get(n).get("info"));
			final boolean isSelected = AntaCallActivity2.mLinkmans.get(n).isSelected;
			final SelectTag tag2 = new SelectTag();
			tag2.setSelected(isSelected);
			tag2.setPosition(n);
			tag.select.setTag((Object) tag2);
			inflate.setTag((Object) tag);
			final ImageView select = tag.select;
			if (isSelected) {
				n = R.drawable.select_on;
			} else {
				n = R.drawable.select_off;
			}
			select.setBackgroundResource(n);
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
//			AntaCallActivity2.access .1 (AntaCallActivity2.this, n);
//			AntaCallActivity2.access .2 (AntaCallActivity2.this, view);
//			AntaCallActivity2.this.contactListClickedItem = AntaCallActivity2.this.mContacts.get(n);
//			AntaCallActivity2.this.mExist = AntaCallUtil.checkExist(AntaCallActivity2.this.contactListClickedItem);
//			AntaCallActivity2.access .4 (AntaCallActivity2.this, n);
		}
	}

	private class MyGridViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyGridViewAdapter(final Context context) {
			this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return AntaCallActivity2.mGridData.size();
		}

		public Object getItem(final int n) {
			return AntaCallActivity2.mGridData.get(n);
		}

		public long getItemId(final int n) {
			return n;
		}

		public View getView(final int n, View inflate, final ViewGroup viewGroup) {
			final Linkman linkman = AntaCallActivity2.mGridData.get(n);
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
					if (AntaCallActivity2.mGridData == null || AntaCallActivity2.mGridData.size() == 0) {
//						AntaCallActivity2.access .8
//						(AntaCallActivity2.this, new MyGridViewAdapter((Context) AntaCallActivity2.this.mContext))
//						;
						AntaCallActivity2.this.mGridView.setAdapter((ListAdapter) AntaCallActivity2.this.mAdapter_);
						return;
					}
					AntaCallActivity2.this.mGridView.setLayoutParams((ViewGroup.LayoutParams) new LinearLayout.LayoutParams(AntaCallActivity2.mGridData.size() * 122 + 10, -2));
					AntaCallActivity2.this.mGridView.setColumnWidth(120);
					AntaCallActivity2.this.mGridView.setHorizontalSpacing(2);
//					AntaCallActivity2.this.mGridView.setStretchMode(0);
					AntaCallActivity2.this.mGridView.setNumColumns(AntaCallActivity2.mGridData.size());
//					AntaCallActivity2.access .8
//					(AntaCallActivity2.this, new MyGridViewAdapter((Context) AntaCallActivity2.this.mContext))
//					;
					AntaCallActivity2.this.mGridView.setAdapter((ListAdapter) AntaCallActivity2.this.mAdapter_);
				}
				case 2: {
					AntaCallActivity2.this.getGroupData();
				}
				case 3: {
//					AntaCallActivity2.access .12
//					(AntaCallActivity2.this, MeetingCompareTool.getInstance().sortByDefault(AntaCallUtil.getContacts()))
//					;
					AntaCallActivity2.mLinkmans = AntaCallActivity2.this.getLinkmans(AntaCallActivity2.this.mContacts);
					if (AntaCallActivity2.this.mAdapter == null) {
//						AntaCallActivity2.access .15
//						(AntaCallActivity2.this, new ContactListAdapter(AntaCallActivity2.this.mContacts))
//						;
//						AntaCallActivity2.this.mAdapter.setData(AntaCallActivity2.this.mContacts);
//						AntaCallActivity2.this.contactList.setAdapter((ListAdapter) AntaCallActivity2.this.mAdapter);
						return;
					}
					AntaCallActivity2.this.mAdapter.setData(AntaCallActivity2.this.mContacts);
					AntaCallActivity2.this.mAdapter.notifyDataSetChanged();
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
