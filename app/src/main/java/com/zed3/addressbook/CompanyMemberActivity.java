package com.zed3.addressbook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.sipua.R;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class CompanyMemberActivity extends Activity implements Observer {
	private final int GET_MEMBERS_BY_PID;
	private DepartmentAdapter adapter;
	TextView back;
	Context context;
	DataBaseService dbService;
	private ListView listView;
	Handler mHandler;
	String mname;
	String pid;
	TextView title;

	public CompanyMemberActivity() {
		this.GET_MEMBERS_BY_PID = 0;
		this.mHandler = new Handler() {
			public void dispatchMessage(final Message message) {
				super.dispatchMessage(message);
			}

			public void handleMessage(final Message message) {
				switch (message.what) {
					default: {
					}
					case 0: {
						CompanyMemberActivity.this.adapter.getData((List<Map<String, String>>) message.obj);
						CompanyMemberActivity.this.listView.setAdapter((ListAdapter) CompanyMemberActivity.this.adapter);
					}
				}
			}
		};
	}

	public List<Map<String, String>> getMemberData(final String s) {
		return this.dbService.getMembersByPid(s);
	}

	public void log(final String s) {
		Log.i("xxxxxx", s);
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.companymember);
		(this.back = (TextView) this.findViewById(R.id.tv_back)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				CompanyMemberActivity.this.finish();
			}
		});
		this.title = (TextView) this.findViewById(R.id.tv_department_title);
		final Intent intent = this.getIntent();
		this.pid = intent.getStringExtra("mid");
		this.mname = intent.getStringExtra("mname");
		this.title.setText((CharSequence) this.mname);
		this.context = (Context) this;
		this.listView = (ListView) this.findViewById(R.id.companymember_listview);
		this.adapter = new DepartmentAdapter(this.context, this.mname, this.dbService);
		(this.dbService = DataBaseService.getInstance()).addObserver(this);
		this.getMemberData(this.pid);
	}

	protected void onStop() {
		super.onStop();
		this.dbService.deleteObserver(this);
	}

	public void update(final Observable observable, final Object o) {
		if (o != null && o instanceof DataBaseService.WorkArgs) {
			// TODO
		}
	}
}
