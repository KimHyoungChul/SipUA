package com.zed3.addressbook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService.ChangedType;
import com.zed3.addressbook.DataBaseService.WorkArgs;
import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class DepartmentActivity extends Activity implements Observer, SearchView.OnQueryTextListener {
	private final int GET_MEMBERS_BY_PID;
	private final int GET_MEMBERS_BY_TYPE;
	private final int GET_TEAMS_BY_PID;
	private String Spell;
	private DepartmentAdapter adapter;
	LinearLayout addressbook_layout;
	private List<Map<String, String>> arr;
	private AddressTopAdapter atopAdapter;
	LinearLayout btn_left;
	Context context;
	DataBaseService dbService;
	private int flag;
	private List<Map<String, String>> list;
	private ListView listView;
	Handler mHandler;
	private String mId;
	private List<Map<String, String>> mList;
	private String mPid;
	private SearchView mSerachView;
	List<Map<String, String>> memlist;
	String mname;
	private List<String> mtid;
	String pid;
	private ListView topListView;

	public DepartmentActivity() {
		this.GET_MEMBERS_BY_PID = 0;
		this.GET_TEAMS_BY_PID = 1;
		this.GET_MEMBERS_BY_TYPE = 3;
		this.memlist = null;
		this.mHandler = new Handler() {
			public void handleMessage(final Message message) {
				switch (message.what) {
					case 0: {
						DepartmentActivity.this.memlist = (List<Map<String, String>>) message.obj;
					}
					case 1: {
						final List list = (List) message.obj;
						if (list.isEmpty()) {
							MyLog.e("ee", " wo hai shi zou 1 ");
							if (DepartmentActivity.this.memlist != null) {
								Collections.sort(DepartmentActivity.this.memlist, new Comparator<Map<String, String>>() {
									@Override
									public int compare(final Map<String, String> map, final Map<String, String> map2) {
										return map.get("mname").compareTo((String) map2.get("mname"));
									}
								});
								Log.e("lele", "\u5237\u65b0\u554a      2===" + DepartmentActivity.this.memlist.size());
								DepartmentActivity.this.adapter.getData(DepartmentActivity.this.memlist);
								DepartmentActivity.this.listView.setAdapter((ListAdapter) DepartmentActivity.this.adapter);
								return;
							}
							break;
						} else {
							MyLog.e("ee", " wo hai shi zou 2 ");
							if (DepartmentActivity.this.memlist != null) {
								Collections.sort(DepartmentActivity.this.memlist, new Comparator<Map<String, String>>() {
									@Override
									public int compare(final Map<String, String> map, final Map<String, String> map2) {
										return map.get("mname").compareTo((String) map2.get("mname"));
									}
								});
								DepartmentActivity.this.adapter.getData(DepartmentActivity.this.memlist);
								DepartmentActivity.this.listView.setAdapter((ListAdapter) DepartmentActivity.this.adapter);
							}
							if (list != null) {
								if (DepartmentActivity.this.memlist == null) {
									DepartmentActivity.this.memlist = new ArrayList<Map<String, String>>();
								}
								DepartmentActivity.this.memlist.addAll(list);
								DepartmentActivity.this.adapter.getData(DepartmentActivity.this.memlist);
								DepartmentActivity.this.listView.setAdapter((ListAdapter) DepartmentActivity.this.adapter);
								return;
							}
							break;
						}
					}
					case 3: {
						final List list2 = (List) message.obj;
						final View inflate = View.inflate(DepartmentActivity.this.context, R.layout.companymember, (ViewGroup) null);
						((TextView) inflate.findViewById(R.id.tv_department_hint)).setVisibility(View.GONE);
						DepartmentActivity.this.setContentView(inflate);
						DepartmentActivity.this.adapter.getData(list2);
						DepartmentActivity.this.listView.setAdapter((ListAdapter) DepartmentActivity.this.adapter);
					}
				}
			}
		};
		this.flag = 0;
		this.mtid = new ArrayList<String>();
	}

	private void InitTitle() {
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.back);
		((TextView) this.findViewById(R.id.title)).setText((CharSequence) this.mname);
		(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				DepartmentActivity.this.finish();
			}
		});
		this.btn_left.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) DepartmentActivity.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) DepartmentActivity.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						DepartmentActivity.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(DepartmentActivity.this.getResources().getColor(R.color.font_color3));
						DepartmentActivity.this.btn_left.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
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

	private void getTids(final String s) {
		final List<Map<String, String>> teamsByPid = this.dbService.getTeamsByPid(s, false);
		Log.i("jiangkai", String.valueOf(s) + "   " + teamsByPid);
		if (teamsByPid.size() > 0) {
			for (final Map<String, String> map : teamsByPid) {
				this.mtid.add(map.get("tid"));
				this.getTids(map.get("tid"));
			}
		}
	}

	private void getType(final String s, String sectionId, final String s2) {
		if (!this.dbService.isNoTeams(s2)) {
			++this.flag;
			final String string = "'or mtype ='" + s + "'and tid = '" + s2;
			this.arr = this.dbService.getMembersByType(s, s2);
			if (this.arr.size() != 0) {
				this.Spell = String.valueOf(string) + sectionId;
			} else {
				this.Spell = sectionId;
			}
			sectionId = this.dbService.getSectionId(s2);
			this.getType(s, this.Spell, sectionId);
		} else if (this.flag != 0) {
			this.Spell = String.valueOf(this.Spell) + "'";
		}
	}

	private void getjuniorType(final String s, String sectionId, final String s2) {
		if (!this.dbService.isNoTeams(s2)) {
			++this.flag;
			this.Spell = "mtype = '" + s + "' and tid = '" + s2;
			final String string = "'or mtype ='" + s + "'and tid = '" + s2 + "'";
			this.arr = this.dbService.getMembersByType(s, s2);
			if (this.arr.size() != 0) {
				if (this.flag == 1) {
					this.Spell = String.valueOf(this.Spell) + sectionId;
				} else {
					this.Spell = String.valueOf(sectionId) + string;
				}
			} else {
				this.Spell = sectionId;
			}
			sectionId = this.dbService.getSectionId(s2);
			this.getjuniorType(s, this.Spell, sectionId);
		}
	}

	public void log(final String s) {
		Log.i("xxxxxx", s);
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		final Intent intent = this.getIntent();
		this.pid = intent.getStringExtra("tid");
		this.mname = intent.getStringExtra("mname");
		this.mPid = intent.getStringExtra("pid");
		this.mId = intent.getStringExtra("id");
		this.context = (Context) this;
		this.adapter = new DepartmentAdapter(this.context, this.mname, this.dbService);
		this.atopAdapter = new AddressTopAdapter(this.context);
		(this.dbService = DataBaseService.getInstance()).addObserver(this);
		final View inflate = View.inflate(this.context, R.layout.companymember, (ViewGroup) null);
		((TextView) inflate.findViewById(R.id.tv_department_hint)).setVisibility(View.GONE);
		this.listView = (ListView) inflate.findViewById(R.id.companymember_listview);
		this.setContentView(inflate);
		(this.mSerachView = (SearchView) this.findViewById(R.id.msearch_view)).setOnQueryTextListener((SearchView.OnQueryTextListener) this);
		this.InitTitle();
		this.dbService.getMembersByPid(this.pid);
	}

	protected void onDestroy() {
		SipUAApp.isDepartmentActivity = false;
		super.onDestroy();
	}

	protected void onPause() {
		SipUAApp.isDepartmentActivity = false;
		super.onPause();
		this.dbService.deleteObserver(this);
	}

	class C08834 implements Comparator<Map<String, String>> {
		C08834() {
		}

		public int compare(Map<String, String> arg0, Map<String, String> arg1) {
			return ((String) arg0.get(UserMinuteActivity.USER_MNAME)).compareTo((String) arg1.get(UserMinuteActivity.USER_MNAME));
		}
	}

	public boolean onQueryTextChange(String newText) {
		List<Member> member = new ArrayList();
		if (TextUtils.isEmpty(newText)) {
			this.dbService.getMembersByPid(this.pid);
		} else {
			String tidString = "tid = '" + this.pid + "'";
			this.mtid.clear();
			getTids(this.pid);
			if (this.mtid.size() != 0) {
				for (String s : this.mtid) {
					tidString = new StringBuilder(String.valueOf(tidString)).append(" or tid = '").append(s).append("'").toString();
				}
			}
			List<Map<String, String>> mmList = getSearchMemberData(this.dbService.queryMembersByKeyword(this, newText, tidString));
			Collections.sort(mmList, new C08834());
			DepartmentAdapter customAdapter = new DepartmentAdapter(this, newText, this.dbService);
			customAdapter.getData(mmList);
			this.listView.setAdapter(customAdapter);
		}
		return false;
	}

	public boolean onQueryTextSubmit(final String s) {
		((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 2);
		return false;
	}

	protected void onResume() {
		SipUAApp.isDepartmentActivity = true;
		super.onResume();
	}

	protected void onStop() {
		SipUAApp.isDepartmentActivity = false;
		super.onStop();
	}

	public void update(Observable observable, Object data) {
		if (data != null && (data instanceof WorkArgs)) {
			WorkArgs args = (WorkArgs) data;
			ChangedType ctype = args.type;
			Message msg = new Message();
//			switch (ctype) {
//				case 6:
//					List<Map<String, String>> memlist = args.object;
//					if (memlist != null && memlist.size() > 0) {
//						msg.what = 0;
//						msg.obj = memlist;
//						this.mHandler.sendMessage(msg);
//					}
//					this.dbService.getTeamsByPid(this.pid, true);
//					return;
//				case 15:
//					List<Map<String, String>> teamlist = args.object;
//					msg.what = 1;
//					msg.obj = teamlist;
//					this.mHandler.sendMessage(msg);
//					return;
//				case 16:
//					List<Map<String, String>> list = args.object;
//					if (list != null && list.size() > 0) {
//						msg.what = 3;
//						msg.obj = list;
//						this.mHandler.sendMessage(msg);
//						return;
//					}
//					return;
//				default:
//					return;
//			}
		}
	}
}
