package com.zed3.settings;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;

public class AboutActivity extends BaseActivity {
	LinearLayout btn_left;

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.setting_about);
		final TextView textView = (TextView) this.findViewById(R.id.title);
		final TextView textView2 = (TextView) this.findViewById(R.id.version_name);
		textView.setText(R.string.setting_about);
		((ImageButton) this.findViewById(R.id.back)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				AboutActivity.this.finish();
			}
		});
		while (true) {
			try {
				textView2.setText((CharSequence) this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
				((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.settings);
				(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
					public void onClick(final View view) {
						AboutActivity.this.finish();
					}
				});
				this.btn_left.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
					public boolean onTouch(final View view, final MotionEvent motionEvent) {
						final TextView textView = (TextView) AboutActivity.this.findViewById(R.id.t_leftbtn);
						final TextView textView2 = (TextView) AboutActivity.this.findViewById(R.id.left_icon);
						switch (motionEvent.getAction()) {
							case 0: {
								textView.setTextColor(-1);
								AboutActivity.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
								textView2.setBackgroundResource(R.drawable.map_back_press);
								break;
							}
							case 1: {
								textView.setTextColor(AboutActivity.this.getResources().getColor(R.color.font_color3));
								AboutActivity.this.btn_left.setBackgroundResource(R.color.whole_bg);
								textView2.setBackgroundResource(R.drawable.map_back_release);
								break;
							}
						}
						return false;
					}
				});
			} catch (PackageManager.NameNotFoundException ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
	}
}
