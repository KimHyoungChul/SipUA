package com.zed3.sipua.ui.lowsdk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.CallHistoryDatabase;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class MemberRecordActivity extends BaseActivity {
	private MyAdapter adpter;
	private LinearLayout btn_left;
	private CallHistoryDatabase db;
	private Cursor mCursor;
	private ListView mRecordList;
	private String mUNum;
	private String mUname;

	private List<Map<String, Object>> GetDataFromDB(final String p0) {
		return null;
	}

	private void init() {
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.back);
		((TextView) this.findViewById(R.id.title)).setText((CharSequence) this.mUname);
		(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				MemberRecordActivity.this.finish();
			}
		});
		this.btn_left.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) MemberRecordActivity.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) MemberRecordActivity.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						MemberRecordActivity.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(MemberRecordActivity.this.getResources().getColor(R.color.font_color3));
						MemberRecordActivity.this.btn_left.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
	}

	private void initCallHistoryViews() {
		(this.mRecordList = (ListView) this.findViewById(R.id.record_member_listview)).setOnItemLongClickListener((AdapterView.OnItemLongClickListener) new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				new AlertDialog.Builder((Context) MemberRecordActivity.this).setTitle(R.string.options_one).setItems(R.array.recordlist_longclick, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialogInterface, final int n) {
						switch (n) {
							default: {
							}
							case 0: {
								new AlertDialog.Builder((Context) MemberRecordActivity.this).setTitle(R.string.delete_log).setMessage((CharSequence) MemberRecordActivity.this.getResources().getString(R.string.delete_member_notify)).setPositiveButton((CharSequence) MemberRecordActivity.this.getResources().getString(R.string.delete_ok), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
									public void onClick(final DialogInterface dialogInterface, final int n) {
										MemberRecordActivity.this.db.delete(CallHistoryDatabase.Table_Name, "_id=" + MemberRecordActivity.this.adpter.getItemId(n));
//										new RecordTask().execute((Object[]) new String[]{MemberRecordActivity.this.mUNum});
									}
								}).setNegativeButton((CharSequence) MemberRecordActivity.this.getResources().getString(R.string.cancel), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
									public void onClick(final DialogInterface dialogInterface, final int n) {
									}
								}).show();
							}
						}
					}
				}).show();
				return true;
			}
		});
		this.mRecordList.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				final Map map = (Map) MemberRecordActivity.this.adpter.getItem(n);
				CallUtil.makeVideoCall((Context) MemberRecordActivity.this, map.get("number").toString(), map.get("name").toString(), "");
			}
		});
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.member_record_list);
		final Intent intent = this.getIntent();
		this.mUname = intent.getStringExtra("user_nam");
		this.mUNum = intent.getStringExtra("user_num");
		this.db = CallHistoryDatabase.getInstance(SipUAApp.mContext);
		this.init();
		this.initCallHistoryViews();
	}

	protected void onDestroy() {
		super.onDestroy();
		if (this.db != null) {
			this.db.close();
		}
	}

	protected void onResume() {
		super.onResume();
//		new RecordTask().execute((Object[]) new String[]{this.mUNum});
	}

	public class MyAdapter extends BaseAdapter {
		long begin;
		String begin_str;
		List<Map<String, Object>> dbList;
		DataBaseService dbService;
		long end;
		private LayoutInflater mInflater;
		String name;
		String number;
		String type;
		ViewHolder vHolder;

		public MyAdapter(final Context context, final List<Map<String, Object>> dbList) {
			this.dbService = DataBaseService.getInstance();
			this.vHolder = null;
			this.dbList = null;
			this.mInflater = LayoutInflater.from(context);
			this.dbList = dbList;
		}

		public int getCount() {
			if (this.dbList == null) {
				return 0;
			}
			return this.dbList.size();
		}

		public Object getItem(final int n) {
			return this.dbList.get(n);
		}

		public long getItemId(final int n) {
			return Integer.parseInt(String.valueOf(this.dbList.get(n).get("_id")));
		}

		public View getView(final int n, View inflate, final ViewGroup viewGroup) {
			if (inflate == null) {
				inflate = this.mInflater.inflate(R.layout.member_record_item, (ViewGroup) null);
//				this.vHolder = new ViewHolder((ViewHolder) null);
				this.vHolder.numberTextView = (TextView) inflate.findViewById(R.id.call_history_name);
				this.vHolder.time = (TextView) inflate.findViewById(R.id.call_history_time);
				this.vHolder.img = (ImageView) inflate.findViewById(R.id.call_history_type);
				this.vHolder.duration = (TextView) inflate.findViewById(R.id.record_time);
				inflate.setTag((Object) this.vHolder);
			} else {
				this.vHolder = (ViewHolder) inflate.getTag();
			}
//			this.type = this.dbList.get(n).get("type");
			if (this.name == null) {
				this.name = "null";
			}
//			this.number = this.dbList.get(n).get("number");
			if (this.number == null) {
				this.number = "nullnumber";
			}
//			this.begin = this.dbList.get(n).get("begin");
//			this.end = this.dbList.get(n).get("end");
//			this.begin_str = this.dbList.get(n).get("begin_str");
			if (!this.begin_str.equals("")) {
				this.vHolder.time.setText((CharSequence) SipdroidActivity.startTimeContainsDetails(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(this.begin_str.trim(), new ParsePosition(0)).getTime()));
			}
			final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SipUAApp.mContext.getResources().getString(R.string.formatdate));
			final long n2 = this.end - this.begin;
			this.vHolder.numberTextView.setText((CharSequence) this.number);
			if (this.type.equals("CallIn")) {
				this.vHolder.img.setImageResource(R.drawable.iconfont_jieru_01);
				String s2;
				final String s = s2 = simpleDateFormat.format(n2);
				if (n2 < 3600000L) {
					s2 = s.substring(4, s.length());
				}
				this.vHolder.duration.setText((CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.hr)) + " " + s2));
			} else {
				if (this.type.equals("CallUnak")) {
					this.vHolder.img.setImageResource(R.drawable.iconfont_jieru_02);
					final String format = simpleDateFormat.format(n2);
					this.vHolder.time.setText((CharSequence) Html.fromHtml("<font color=red>" + this.vHolder.time.getText().toString() + "</font>"));
					this.vHolder.numberTextView.setText((CharSequence) Html.fromHtml("<font color=red>" + this.vHolder.numberTextView.getText().toString() + "</font>"));
					this.vHolder.duration.setText((CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.xl)) + " " + format.substring(7, format.length())));
					return inflate;
				}
				if (this.type.equals("CallOut")) {
					this.vHolder.img.setImageResource(R.drawable.iconfont_jieru_03);
					String s4;
					final String s3 = s4 = simpleDateFormat.format(n2);
					if (n2 < 3600000L) {
						s4 = s3.substring(4, s3.length());
					}
					this.vHolder.duration.setText((CharSequence) (String.valueOf(SipUAApp.mContext.getResources().getString(R.string.hc)) + " " + s4));
					return inflate;
				}
				if (this.type.equals("CallUnout")) {
					this.vHolder.img.setImageResource(R.drawable.iconfont_jieru_03);
					this.vHolder.duration.setText((CharSequence) SipUAApp.mContext.getResources().getString(R.string.wjt));
					return inflate;
				}
			}
			return inflate;
		}

		public void refreshListView(final List<Map<String, Object>> dbList) {
			this.dbList = dbList;
		}
	}

	class RecordTask extends AsyncTask<String, Void, List<Map<String, Object>>> {
		protected List<Map<String, Object>> doInBackground(final String... array) {
			return MemberRecordActivity.this.GetDataFromDB(array[0]);
		}

		protected void onPostExecute(final List<Map<String, Object>> list) {
//			super.onPostExecute((Object) list);
//			if (list != null) {
//				MemberRecordActivity.access .0
//				(MemberRecordActivity.this, new MyAdapter((Context) MemberRecordActivity.this, list))
//				;
//			}
			MemberRecordActivity.this.mRecordList.setAdapter((ListAdapter) MemberRecordActivity.this.adpter);
		}

		protected void onPreExecute() {
			super.onPreExecute();
		}
	}

	private class ViewHolder {
		TextView duration;
		ImageView img;
		TextView numberTextView;
		TextView time;
	}
}
