package com.zed3.addressbook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;

public class UserMinuteActivity extends Activity {
	public static final String USER_AUDIO = "audio";
	public static final String USER_DEPARTMENT = "department";
	public static final String USER_DTYPE = "dtype";
	public static final String USER_GPS = "gps";
	public static final String USER_MNAME = "mname";
	public static final String USER_MTYPE = "mtype";
	public static final String USER_NUMBER = "number";
	public static final String USER_PHONE = "phone";
	public static final String USER_PICTUREUPLPAD = "pictureupload";
	public static final String USER_POSITION = "position";
	public static final String USER_PTTMAP = "pttmap";
	public static final String USER_SEX = "sex";
	public static final String USER_SMSSWITCH = "smsswitch";
	public static final String USER_TEXT = "text";
	public static final String USER_VIDEO = "video";

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_user_message_detail);
		final Intent intent = this.getIntent();
		final String stringExtra = intent.getStringExtra("mname");
		final String stringExtra2 = intent.getStringExtra("position");
		final String stringExtra3 = intent.getStringExtra("sex");
		final String stringExtra4 = intent.getStringExtra("phone");
		final String stringExtra5 = intent.getStringExtra("dtype");
		final String stringExtra6 = intent.getStringExtra("video");
		final String stringExtra7 = intent.getStringExtra("audio");
		final String stringExtra8 = intent.getStringExtra("pttmap");
		final String stringExtra9 = intent.getStringExtra("gps");
		final String stringExtra10 = intent.getStringExtra("pictureupload");
		final String stringExtra11 = intent.getStringExtra("smsswitch");
		final String stringExtra12 = intent.getStringExtra("department");
		final String stringExtra13 = intent.getStringExtra("mtype");
		final String stringExtra14 = intent.getStringExtra("number");
		intent.getStringExtra("text");
		MyLog.e("dd", "video========>" + stringExtra6);
		if (stringExtra != null) {
			((TextView) this.findViewById(R.id.user_name)).setText((CharSequence) stringExtra);
		}
		if (stringExtra14 != null) {
			((TextView) this.findViewById(R.id.user_number)).setText((CharSequence) stringExtra14);
		}
		if (stringExtra2 != null) {
			((TextView) this.findViewById(R.id.user_position)).setText((CharSequence) stringExtra2);
		}
		final TextView textView = (TextView) this.findViewById(R.id.user_sex);
		if (stringExtra3 != null && stringExtra3.equalsIgnoreCase("Male")) {
			textView.setText(R.string.Male);
		} else {
			textView.setText(R.string.Female);
		}
		if (stringExtra12 != null) {
			((TextView) this.findViewById(R.id.user_department)).setText((CharSequence) stringExtra12);
		}
		if (stringExtra4 != null) {
			((TextView) this.findViewById(R.id.user_phone)).setText((CharSequence) stringExtra4);
		}
		if (stringExtra5 != null) {
			((TextView) this.findViewById(R.id.user_terminal)).setText((CharSequence) stringExtra5);
		}
		final TextView textView2 = (TextView) this.findViewById(R.id.user_video);
		final TextView textView3 = (TextView) this.findViewById(R.id.user_function);
		final TextView textView4 = (TextView) this.findViewById(R.id.user_audio);
		final TextView textView5 = (TextView) this.findViewById(R.id.user_pttmap);
		final TextView textView6 = (TextView) this.findViewById(R.id.user_gps);
		final TextView textView7 = (TextView) this.findViewById(R.id.user_pictureupload);
		final TextView textView8 = (TextView) this.findViewById(R.id.user_smsswitch);
		final TextView textView9 = (TextView) this.findViewById(R.id.user_dtype);
		if (Member.UserType.toUserType(stringExtra13) == Member.UserType.MOBILE_GQT) {
			textView3.setVisibility(View.GONE);
			if (stringExtra6 != null && stringExtra6.equalsIgnoreCase("1")) {
				textView2.setVisibility(View.VISIBLE);
			} else {
				textView2.setVisibility(View.GONE);
			}
			if (stringExtra7 != null && stringExtra7.equalsIgnoreCase("1")) {
				textView4.setVisibility(View.VISIBLE);
			} else {
				textView4.setVisibility(View.GONE);
			}
			if (stringExtra8 != null && stringExtra8.equalsIgnoreCase("1")) {
				textView5.setVisibility(View.VISIBLE);
			} else {
				textView5.setVisibility(View.GONE);
			}
			if (stringExtra9 != null && stringExtra9.equalsIgnoreCase("0")) {
				textView6.setVisibility(View.GONE);
			} else {
				textView6.setVisibility(View.VISIBLE);
			}
			if (stringExtra10 != null && stringExtra10.equalsIgnoreCase("1")) {
				textView7.setVisibility(View.VISIBLE);
			} else {
				textView7.setVisibility(View.GONE);
			}
			if (stringExtra11 != null && stringExtra11.equalsIgnoreCase("1")) {
				textView8.setVisibility(View.VISIBLE);
			} else {
				textView8.setVisibility(View.GONE);
			}
		} else if (Member.UserType.toUserType(stringExtra13) == Member.UserType.SVP) {
			textView3.setText((CharSequence) "\u8c03\u8bd5\u53f0\u7528\u6237");
			textView2.setVisibility(View.GONE);
			textView4.setVisibility(View.GONE);
			textView5.setVisibility(View.GONE);
			textView6.setVisibility(View.GONE);
			textView9.setVisibility(View.GONE);
			textView7.setVisibility(View.GONE);
			textView8.setVisibility(View.GONE);
		} else if (Member.UserType.toUserType(stringExtra13) == Member.UserType.VIDEO_MONITOR_GVS || Member.UserType.toUserType(stringExtra13) == Member.UserType.VIDEO_MONITOR_GB28181) {
			textView3.setText((CharSequence) "\u89c6\u9891\u76d1\u63a7\u7528\u6237");
			textView2.setVisibility(View.GONE);
			textView4.setVisibility(View.GONE);
			textView5.setVisibility(View.GONE);
			textView6.setVisibility(View.GONE);
			textView9.setVisibility(View.GONE);
			textView7.setVisibility(View.GONE);
			textView8.setVisibility(View.GONE);
		}
		this.findViewById(R.id.btn_home_user).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				UserMinuteActivity.this.finish();
			}
		});
	}
}
