package com.zed3.audio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.utils.DialogMessageTool;

public class CustomDialog extends Activity implements View.OnClickListener {
	private static final String CONTROL_DEVICE_NAME = "control_device_name";
	private static final String CONTROL_STATE = "control_state";
	private static final int STATE_AUDIO_RECORD_STARTPLAYING_ERROR = 0;
	private static final int STATE_AUDIO_RECORD_STOP_ERROR = 1;
	private static final String tag = "CustomDialog";
	private TextView mCancelTV;
	private TextView mCommitTV;
	private String mDeviceName;
	private TextView mMsgTV;
	private int mState;
	private TextView mTitleTV;

	public CustomDialog() {
		this.mState = -1;
		this.mDeviceName = "";
	}

	public static void askUserToCheckAudio() {
		final Intent intent = new Intent(SipUAApp.mContext, (Class) CustomDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("control_state", 0);
		SipUAApp.mContext.startActivity(intent);
	}

	public static void askUserToConnectBluetooth() {
		final Intent intent = new Intent(SipUAApp.mContext, (Class) CustomDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("control_state", 1);
		SipUAApp.mContext.startActivity(intent);
	}

	private void initTextViews(String string, final String s, final String text, final String text2) {
		this.mTitleTV.setText((CharSequence) string);
		string = DialogMessageTool.getString((int) (this.getResources().getDisplayMetrics().density * 296.0f + 0.5f), this.mMsgTV.getTextSize(), s);
		this.mMsgTV.setText((CharSequence) string);
		this.mCancelTV.setText((CharSequence) text);
		this.mCommitTV.setText((CharSequence) text2);
	}

	protected void onActivityResult(final int n, final int n2, final Intent intent) {
		switch (n) {
			default: {
				this.finish();
				Log.e("CustomDialog", "unknow state error");
				break;
			}
			case 0: {
				this.finish();
				break;
			}
			case 1: {
				this.finish();
				break;
			}
		}
		super.onActivityResult(n, n2, intent);
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			default: {
			}
			case R.id.ok_tv: {
				switch (this.mState) {
					default: {
						Log.e("CustomDialog", "unknow state error");
					}
					case 0:
					case 1: {
						this.finish();
						return;
					}
				}
			}
			case R.id.cancel_tv: {
				switch (this.mState) {
					default: {
						Log.e("CustomDialog", "unknow state error");
						this.finish();
						break;
					}
					case 0: {
						this.finish();
						break;
					}
					case 1: {
						this.finish();
						break;
					}
				}
				this.finish();
			}
		}
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.zmbluetooth_control_dialog);
		this.mTitleTV = (TextView) this.findViewById(R.id.title_tv);
		this.mMsgTV = (TextView) this.findViewById(R.id.msg_tv);
		this.mCancelTV = (TextView) this.findViewById(R.id.cancel_tv);
		this.mCommitTV = (TextView) this.findViewById(R.id.ok_tv);
		this.mCancelTV.setOnClickListener((View.OnClickListener) this);
		this.mCommitTV.setOnClickListener((View.OnClickListener) this);
		final Intent intent = this.getIntent();
		this.mState = intent.getIntExtra("control_state", -1);
		this.mDeviceName = intent.getStringExtra("control_device_name");
		switch (this.mState) {
			default: {
				this.finish();
			}
			case 0: {
				this.initTextViews(this.getResources().getString(R.string.au_anomaly), String.valueOf(this.getResources().getString(R.string.au_anomaly_notify_1)) + this.getResources().getString(R.string.au_anomaly_notify_2), this.getResources().getString(R.string.cancel), this.getResources().getString(R.string.ok_know));
			}
			case 1: {
				this.initTextViews(this.getResources().getString(R.string.bl_notify_1), this.getResources().getString(R.string.bl_notify_2), this.getResources().getString(R.string.cancel), this.getResources().getString(R.string.bl_notify_ok));
			}
		}
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onResume() {
		super.onResume();
		if (this.mState == -1) {
			Log.e("CustomDialog", "unknow state error");
			this.finish();
		}
	}
}
