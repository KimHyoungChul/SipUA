package com.zed3.settings;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.location.GPSInfoDataBase;
import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.toast.MyToast;

import java.util.ArrayList;
import java.util.List;

public class GisDisplayActivity extends Activity implements View.OnClickListener {
	ListView gisDisplayView;
	EditText inputNum;
	TextView resultView;
	final String tag;

	public GisDisplayActivity() {
		this.tag = "GisDisplayActivity";
		this.gisDisplayView = null;
		this.inputNum = null;
		this.resultView = null;
	}

	public List<String> getGisInfo(final int n) {
		final ArrayList<String> list = new ArrayList<String>();
		final long count = GPSInfoDataBase.getInstance().getCount("gps_info");
		MyLog.i("GisDisplayActivity", "getGisInfo count = " + count + " limit = " + n);
		if (count > 0L && n > 0) {
			final Cursor query = GPSInfoDataBase.getInstance().query("gps_info", "real_time desc", 0, n);
			if (query != null) {
				while (query.moveToNext()) {
					final StringBuffer sb = new StringBuffer();
					sb.append("real_time").append(":").append(query.getString(query.getColumnIndex("real_time"))).append("\n");
					sb.append("[x").append(":").append(query.getString(query.getColumnIndex("gps_x"))).append(",");
					sb.append("y").append(":").append(query.getString(query.getColumnIndex("gps_y"))).append("]");
					list.add(sb.toString());
				}
			}
			if (query != null) {
				query.close();
			}
		}
		MyLog.i("GisDisplayActivity", "getGisInfo infoList.size = " + list.size());
		return list;
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			default: {
			}
			case R.id.t_leftbtn: {
				this.finish();
			}
		}
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.gisdisplay);
		final TextView textView = (TextView) this.findViewById(R.id.t_leftbtn);
		textView.setText((CharSequence) "\u5b9a\u4f4d\u8bbe\u7f6e");
		textView.setOnClickListener((View.OnClickListener) this);
		((TextView) this.findViewById(R.id.title)).setText((CharSequence) "GIS\u5b9a\u4f4d\u4fe1\u606f");
//		(this.gisDisplayView = (ListView) this.findViewById(R.id.gislist)).setAdapter((ListAdapter) new ArrayAdapter((Context) this, 17367043, (List) this.getGisInfo(50)));
		this.inputNum = (EditText) this.findViewById(R.id.input_num);
		(this.resultView = (TextView) this.findViewById(R.id.result)).setVisibility(View.GONE);
	}

	public void onRefresh(final View view) {
		final String string = this.inputNum.getText().toString();
		if (string.length() > 0 && string.length() < 4) {
			final List<String> gisInfo = this.getGisInfo(Integer.parseInt(this.inputNum.getText().toString()));
			final int size = gisInfo.size();
			this.resultView.setVisibility(View.VISIBLE);
			this.resultView.setText((CharSequence) ("\u5171\u67e5\u8be2\u5230" + size + "\u6761"));
			MyLog.i("GisDisplayActivity", "refresh count = " + size);
//			this.gisDisplayView.setAdapter((ListAdapter) new ArrayAdapter((Context) this, 17367043, (List) gisInfo));
			return;
		}
		MyToast.showToast(true, (Context) this, "\u8f93\u5165\u67e5\u8be2\u6761\u6570\u6709\u8bef\uff01");
	}
}
