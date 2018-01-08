package com.zed3.sipua.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;

public class UserConfrimActivity extends BaseActivity implements View.OnClickListener {
	public static final String EXTRA_DIALOG_CONTENT = "com.zed3.extra.DIALOG_CONTENT";
	public static final String EXTRA_DIALOG_TITLE = "com.zed3.extra.DIALOG_TITLE";
	private static OnButtonClickListener sLis;

	public static void registerButtonClickListener(final OnButtonClickListener sLis) {
		UserConfrimActivity.sLis = sLis;
	}

	public static void startConfrimActivity(final String s, final String s2) {
		final Context appContext = SipUAApp.getAppContext();
		final Intent intent = new Intent(appContext, (Class) UserConfrimActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("com.zed3.extra.DIALOG_TITLE", s);
		intent.putExtra("com.zed3.extra.DIALOG_CONTENT", s2);
		appContext.startActivity(intent);
	}

	public static void unregisterButtonClickListener() {
		UserConfrimActivity.sLis = null;
	}

	public OnButtonClickListener getButtonClickListener() {
		return UserConfrimActivity.sLis;
	}

	public void onBackPressed() {
		super.onBackPressed();
		final OnButtonClickListener buttonClickListener = this.getButtonClickListener();
		if (buttonClickListener != null) {
			buttonClickListener.onCancel();
		}
	}

	public void onClick(final View view) {
		final int id = view.getId();
		final OnButtonClickListener buttonClickListener = this.getButtonClickListener();
		if (R.id.button_ok == id) {
			if (buttonClickListener != null) {
				buttonClickListener.onConfrim();
			}
		} else if (R.id.button_cancel == id && buttonClickListener != null) {
			buttonClickListener.onCancel();
		}
		this.finish();
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_video_user_confrim);
		final Intent intent = this.getIntent();
		final String stringExtra = intent.getStringExtra("com.zed3.extra.DIALOG_TITLE");
		final String stringExtra2 = intent.getStringExtra("com.zed3.extra.DIALOG_CONTENT");
		final TextView textView = (TextView) this.findViewById(R.id.dlg_title);
		final TextView textView2 = (TextView) this.findViewById(R.id.dlg_content);
		textView.setText((CharSequence) stringExtra);
		textView2.setText((CharSequence) stringExtra2);
		this.findViewById(R.id.button_ok).setOnClickListener((View.OnClickListener) this);
		this.findViewById(R.id.button_cancel).setOnClickListener((View.OnClickListener) this);
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onResume() {
		super.onResume();
	}

	public interface OnButtonClickListener {
		void onCancel();

		void onConfrim();
	}
}
