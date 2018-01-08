package com.zed3.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.SettingVideoSize;
import com.zed3.sipua.welcome.DeviceInfo;

public class VideoSelectSettingListActivity extends BaseActivity implements View.OnClickListener {
	private void initLeftBtnClickEvent(final View view) {
		((TextView) view.findViewById(R.id.t_leftbtn)).setText(R.string.advanced);
		final LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.btn_leftbtn);
		linearLayout.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				VideoSelectSettingListActivity.this.finish();
			}
		});
		linearLayout.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) VideoSelectSettingListActivity.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) VideoSelectSettingListActivity.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						linearLayout.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(VideoSelectSettingListActivity.this.getResources().getColor(R.color.font_color3));
						linearLayout.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
	}

	private void initSelectListClientEvent(final View view) {
		view.findViewById(R.id.settings_video_connection).setOnClickListener((View.OnClickListener) this);
		if (DeviceInfo.CONFIG_VIDEO_UPLOAD == 0) {
			view.findViewById(R.id.settings_video_upload).setVisibility(View.GONE);
			view.findViewById(R.id.LinearLayout2).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.settings_video_upload).setOnClickListener((View.OnClickListener) this);
		}
		if (DeviceInfo.CONFIG_VIDEO_MONITOR == 0) {
			view.findViewById(R.id.settings_video_monitor).setVisibility(View.GONE);
			view.findViewById(R.id.audiovad_line).setVisibility(View.GONE);
			return;
		}
		view.findViewById(R.id.settings_video_monitor).setOnClickListener((View.OnClickListener) this);
	}

	private void initTitle(final View view, final String text) {
		((TextView) view.findViewById(R.id.title)).setText((CharSequence) text);
	}

	private void onVideoConnectionClicked(final View view) {
		this.startVideoSettingActivity("com.zed3.action.VIDEO_CALL", SipUAApp.getAppContext().getString(R.string.settings_video_connection));
	}

	private void onVideoMonitorClicked(final View view) {
		this.startVideoSettingActivity("com.zed3.action.VIDEO_MONITOR", SipUAApp.getAppContext().getString(R.string.settings_video_monitor));
	}

	private void onVideoUploadClicked(final View view) {
		this.startVideoSettingActivity("com.zed3.action.VIDEO_UPLOAD", SipUAApp.getAppContext().getString(R.string.settings_video_upload));
	}

	private void startVideoSettingActivity(final String s, final String s2) {
		final Intent intent = new Intent((Context) this, (Class) SettingVideoSize.class);
		intent.putExtra("com.zed3.extra.VIDEO_ACTION", s);
		intent.putExtra("com.zed3.extra.VIDEO_TITLE", s2);
		this.startActivity(intent);
	}

	public void onClick(final View view) {
		final int id = view.getId();
		if (R.id.settings_video_connection == id) {
			this.onVideoConnectionClicked(view);
		} else {
			if (R.id.settings_video_upload == id) {
				this.onVideoUploadClicked(view);
				return;
			}
			if (R.id.settings_video_monitor == id) {
				this.onVideoMonitorClicked(view);
			}
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.setting_video_list);
		final View decorView = this.getWindow().getDecorView();
		this.initTitle(decorView, this.getString(R.string.settings_video));
		this.initLeftBtnClickEvent(decorView);
		this.initSelectListClientEvent(decorView);
	}
}
