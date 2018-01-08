package com.zed3.broadcastptt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;

public class SettingsBroadcastActivity extends BaseActivity {
	private EditText actionDownET;
	private EditText actionUpET;
	private SharedPreferences globleSettings;
	private EditText keyCodeET;
	private Button saveBtn;

	private boolean checkParameter(final String s, final String s2, final String s3) {
		return true;
	}

	private void getParameter() {
		final String string = this.globleSettings.getString("broadcast_action_down", "");
		final String string2 = this.globleSettings.getString("broadcast_action_up", "");
		final String string3 = this.globleSettings.getString("broadcast_keycode", "");
		if (!TextUtils.isEmpty((CharSequence) string)) {
			this.actionDownET.setText((CharSequence) string);
		}
		if (!TextUtils.isEmpty((CharSequence) string2)) {
			this.actionUpET.setText((CharSequence) string2);
		}
		if (!TextUtils.isEmpty((CharSequence) string3)) {
			this.keyCodeET.setText((CharSequence) string3);
		}
	}

	private void initViews() {
		this.actionDownET = (EditText) this.findViewById(R.id.setting_broadcast_action_down);
		this.actionUpET = (EditText) this.findViewById(R.id.setting_broadcast_action_up);
		this.keyCodeET = (EditText) this.findViewById(R.id.setting_broadcast_keycode);
		(this.saveBtn = (Button) this.findViewById(R.id.setting_broadcast_btn_save)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				if (SettingsBroadcastActivity.this.saveParameter()) {
					Toast.makeText((Context) SettingsBroadcastActivity.this, (CharSequence) "\u4fdd\u5b58\u6210\u529f", Toast.LENGTH_SHORT).show();
					final SipUAApp sipUAApp = (SipUAApp) SettingsBroadcastActivity.this.getApplicationContext();
				}
			}
		});
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.advanced);
		((TextView) this.findViewById(R.id.title)).setText(R.string.setting_broadcast);
		final LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.btn_leftbtn);
		linearLayout.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				SettingsBroadcastActivity.this.finish();
			}
		});
		linearLayout.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) SettingsBroadcastActivity.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) SettingsBroadcastActivity.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						linearLayout.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(SettingsBroadcastActivity.this.getResources().getColor(R.color.font_color3));
						linearLayout.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
	}

	private boolean saveParameter() {
		final boolean b = false;
		final String string = this.actionDownET.getText().toString();
		final String string2 = this.actionUpET.getText().toString();
		final String string3 = this.keyCodeET.getText().toString();
		boolean b2 = b;
		if (this.checkParameter(string, string2, string3)) {
			this.globleSettings.edit().putString("broadcast_keycode", string3).commit();
			b2 = b;
			if (this.globleSettings.edit().putString("broadcast_action_down", string).commit()) {
				b2 = b;
				if (this.globleSettings.edit().putString("broadcast_action_up", string2).commit()) {
					b2 = true;
				}
			}
		}
		return b2;
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.activtity_setting_broadcast);
		this.initViews();
		this.globleSettings = ((SipUAApp) this.getApplication()).getSettings();
		this.getParameter();
	}
}
