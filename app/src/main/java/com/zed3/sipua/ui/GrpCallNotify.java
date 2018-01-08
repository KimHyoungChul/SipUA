package com.zed3.sipua.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import com.zed3.groupcall.GroupCallUtil;
import com.zed3.log.MyLog;
import com.zed3.power.MyPowerManager;
import com.zed3.sipua.PttGrp;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.lowsdk.TempGroupCallUtil;
import com.zed3.utils.DialogMessageTool;
import com.zed3.utils.LanguageChange;
import com.zed3.window.MyWindowManager;

import java.util.ArrayList;

public class GrpCallNotify extends Activity {
	private static final int MAX_WAIT_TIME = 8000;
	private static final String TAG = "GrpCallNotify";
	BroadcastReceiver br;
	private TimeCount countTimer;
	private boolean isTempGrpCall;
	private Button mButtonCancel;
	private Button mButtonOk;
	private String mScreanWakeLockKey;
	private TextView mTextView;
	private TextView mTextViewString;
	private int mWidth;
	private ArrayList<String> memberList;
	private String tmpGrpName;
	ToneGenerator toneGenerator;

	public GrpCallNotify() {
		this.toneGenerator = null;
		this.br = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				if (intent.getAction().equals("com.zed3.sipua.tmpgrp.closing")) {
					Receiver.GetCurUA().rejectTmpGrpCall();
					GrpCallNotify.this.finish();
				}
			}
		};
		this.mScreanWakeLockKey = "GrpCallNotify";
		this.isTempGrpCall = false;
	}

	public static void startSelf(Intent intent) {
		final Context appContext = SipUAApp.getAppContext();
		intent = new Intent(intent);
		intent.setClass(appContext, (Class) GrpCallNotify.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		appContext.startActivity(intent);
	}

	public void onConfigurationChanged(final Configuration configuration) {
		LanguageChange.upDateLanguage((Context) this);
		super.onConfigurationChanged(configuration);
	}

	protected void onCreate(final Bundle p0) {
		// TODO
		super.onCreate(p0);
	}

	protected void onDestroy() {
		super.onDestroy();
		LanguageChange.upDateLanguage(SipUAApp.mContext);
		MyLog.e("GrpCallNotify", String.valueOf(this.isTempGrpCall) + "onDestroy ..................... called " + this.toneGenerator + "," + this.countTimer);
		if (this.toneGenerator != null) {
			this.toneGenerator.stopTone();
			this.toneGenerator.release();
			this.toneGenerator = null;
		}
		if (this.countTimer != null) {
			this.countTimer.cancel();
			this.countTimer = null;
		}
		while (true) {
			try {
				this.unregisterReceiver(this.br);
				MyPowerManager.getInstance().releaseScreenWakeLock(this.mScreanWakeLockKey);
				MyWindowManager.getInstance().reenableKeyguard(this);
			} catch (Exception ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
	}

	protected void onNewIntent(final Intent intent) {
		boolean isTempGrpCall = false;
		if (intent != null) {
			isTempGrpCall = isTempGrpCall;
			if (intent.getBooleanExtra("isTempGrpCall", false)) {
				isTempGrpCall = true;
			}
		}
		this.isTempGrpCall = isTempGrpCall;
		if (this.isTempGrpCall) {
			this.mButtonOk.setText(R.string.accept);
			this.mButtonCancel.setText(R.string.decline);
			final Bundle extras = intent.getExtras();
			if (extras != null) {
				final String[] stringArray = extras.getStringArray("pttMembers");
				this.memberList = new ArrayList<String>();
				if (stringArray != null) {
					for (int i = 0; i < stringArray.length; ++i) {
						this.memberList.add(stringArray[i]);
					}
				}
				this.tmpGrpName = extras.getString("0");
				TempGroupCallUtil.tmpGrpName = new String(this.tmpGrpName);
				TempGroupCallUtil.arrayListMembers = (ArrayList<String>) this.memberList.clone();
			}
		}
		MyLog.i("GrpCallNotify", String.valueOf(this.isTempGrpCall) + "onNewIntent ..................... called " + this.toneGenerator + "," + this.countTimer);
	}

	protected void onResume() {
		Receiver.engine((Context) this);
		super.onResume();
		this.mTextView = (TextView) this.findViewById(R.id.notify_content);
		this.mTextViewString = (TextView) this.findViewById(R.id.notify_content_string);
		final String talkGrp = GroupCallUtil.getTalkGrp();
		final PttGrp getGrpByID = Receiver.GetCurUA().GetGrpByID(talkGrp);
		String s;
		String grpName;
		if (getGrpByID == null) {
			s = String.valueOf(this.getResources().getString(R.string.temp_group_invite)) + " " + talkGrp;
			grpName = "";
		} else {
			s = this.getResources().getString(R.string.notify_message_text);
			grpName = getGrpByID.grpName;
		}
		this.mWidth = (int) (240.0f * this.getResources().getDisplayMetrics().density + 0.5f);
		final String string = DialogMessageTool.getString(this.mWidth, this.mTextView.getTextSize(), grpName);
		final String string2 = DialogMessageTool.getString(this.mWidth, this.mTextViewString.getTextSize(), s);
		this.mTextView.setText((CharSequence) string);
		this.mTextViewString.setText((CharSequence) string2);
	}

	void rejectRequest() {
		final String talkGrp = GroupCallUtil.getTalkGrp();
		final PttGrp getCurGrp = Receiver.GetCurUA().GetCurGrp();
		if (getCurGrp != null && getCurGrp.grpID.equals(talkGrp) && Receiver.GetCurUA().IsPttMode()) {
			Receiver.GetCurUA().answerGroupCall(getCurGrp);
		} else if (!this.isTempGrpCall) {
			final PttGrp getGrpByID = Receiver.GetCurUA().GetGrpByID(talkGrp);
			if (getGrpByID != null) {
				Receiver.GetCurUA().grouphangup(getGrpByID);
			}
		} else {
			Receiver.GetCurUA().rejectTmpGrpCall();
		}
		this.finish();
	}

	class TimeCount extends CountDownTimer {
		TextView countView;

		public TimeCount(final long n, final long n2) {
			super(n, n2);
			this.countView = (TextView) GrpCallNotify.this.findViewById(R.id.textView2);
		}

		public void onFinish() {
			this.countView.setText((CharSequence) "(0)");
			GrpCallNotify.this.rejectRequest();
		}

		public void onTick(final long n) {
			this.countView.setText((CharSequence) ("(" + n / 1000L + ")"));
		}
	}
}
