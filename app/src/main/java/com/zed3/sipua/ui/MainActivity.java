package com.zed3.sipua.ui;

import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zed3.broadcastptt.PttBroadcastReceiver;
import com.zed3.codecs.EncodeRate;
import com.zed3.customgroup.ContactActivity;
import com.zed3.log.MyLog;
import com.zed3.screenhome.BaseActivityGroup;
import com.zed3.sipua.R;
import com.zed3.utils.LogUtil;

public class MainActivity extends BaseActivityGroup implements OnClickListener {
	// 底部导航栏
	private ImageView iv_contact, iv_groupcall, iv_singlecall, iv_message,iv_phototransfer, iv_meeting, iv_setting;
	private TextView tv_contact, tv_groupcall, tv_singlecall, tv_message, tv_phototransfer, tv_meeting, tv_setting;
	private LinearLayout ll_contact, ll_groupcall, ll_setting, ll_meeting;
	private FrameLayout fl_singlecall, fl_message, fl_phototransfer;

	String selectTag = "";
	private Context mContext;
	private View popupView, mRootView;
	private LinearLayout l_more, lll;
	private LinearLayout ll_actvityarea, ll_bottom;
	private boolean isShowing = false;
	private ScaleAnimation sa;
	private PopupWindow v_SettingTransfer = null;

	public static String ACTION_UI_REFRESH = "ui_refresh";
	public static String READ_MESSAGE = "read_message_update_count";
	public static final String TAG = "MainActivity";
	public static EncodeRate.Mode mode = EncodeRate.Mode.MR475;
	private long begin;
	private PttBroadcastReceiver broadcastReceiver;
	private long end;
	private int functionNum = 0;
	private IntentFilter mFilter;
	private TextView tv_singlecallpoint;
	private TextView msgPoint;
	private TextView msgPointPhoto;
	private TextView tv_setting_pop;
	private BroadcastReceiver recv;

	static MainActivity gInst;

	OnClickListener contactlistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			tv_contact.setTextColor(Color.WHITE);
			ll_contact.setBackgroundResource(R.drawable.main_tab_item_select);
			iv_contact.setBackgroundResource(R.drawable.tab_contact_after);

			startIntent(ContactActivity.class);
			dismissPopupWindow();
			selectTag = "contact";
		}
	};

	OnClickListener groupcalllistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			tv_groupcall.setTextColor(Color.WHITE);
			ll_groupcall.setBackgroundResource(R.drawable.main_tab_item_select);
			iv_groupcall.setBackgroundResource(R.drawable.tab_groupcall_after);

//			startIntent(GroupCallActivity.class);
			dismissPopupWindow();
			selectTag = "groupcall";
		}
	};

	OnClickListener singlecalllistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			tv_singlecall.setTextColor(Color.WHITE);
			fl_singlecall.setBackgroundResource(R.drawable.main_tab_item_select);
			View v_icon = findViewById(R.id.icon_singlecall);
			v_icon.setBackgroundResource(R.drawable.tab_singlecall_after);

//			startIntent(SingleCallActivity.class);
			dismissPopupWindow();
			selectTag = "singlecall";
		}
	};

	OnClickListener messagelistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			tv_message.setTextColor(Color.WHITE);
			fl_message.setBackgroundResource(R.drawable.main_tab_item_select);
			iv_message.setBackgroundResource(R.drawable.tab_mesage_down);

//			startIntent(MessageComposeActivity.class);
			dismissPopupWindow();
			selectTag = "message";
		}
	};

	OnClickListener phototransferlistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			tv_phototransfer.setTextColor(Color.WHITE);
			fl_phototransfer.setBackgroundResource(R.drawable.main_tab_item_select);
			iv_phototransfer.setBackgroundResource(R.drawable.tab_photo_down);

//			startIntent(PhotoTransferActivity.class);
			dismissPopupWindow();
			selectTag = "phototransfer";
		}
	};

	OnClickListener conferencelistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			tv_meeting.setTextColor(Color.WHITE);
			ll_meeting.setBackgroundResource(R.drawable.main_tab_item_select);
			iv_meeting.setBackgroundResource(R.drawable.tab_meeting_after);

//			startIntent(ConferenceSendActivity.class);
			dismissPopupWindow();
			selectTag = "meeting";
		}
	};

	OnClickListener settinglistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetAll();
			tv_setting.setTextColor(Color.WHITE);
			ll_setting.setBackgroundResource(R.drawable.main_tab_item_select);
			iv_setting.setBackgroundResource(R.drawable.tab_setting_after);

//			startIntent(SettingActivity.class);
			dismissPopupWindow();
			selectTag = "setting";
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MyLog.d("testcrash", "MainActivity#onCreate enter");
		LogUtil.makeLog(TAG, "onCreate()");

		// TODO 通话历史

		super.onCreate(savedInstanceState);

//		checkUserData();
//		initDeviceInfo();
//		this.functionNum = getfunctionNum();
//		String language = getResources().getConfiguration().locale.getLanguage();
//		SharedPreferences sharedPreferences = getSharedPreferences("com.zed3.sipua_preferences", 0);
//		if (sharedPreferences.getBoolean("NetworkListenerService", false)) {
//			sharedPreferences.edit().putBoolean("NetworkListenerService", false).commit();
//			stopService(new Intent(this.mContext, NetworkListenerService.class));
//		}
//		int languageId = sharedPreferences.getInt("languageId", 0);
//		if ((languageId == 0 && !language.equals(Locale.getDefault().getLanguage())) || ((languageId == 1 && !language.equals("zh")) || (languageId == 2 && !language.equals("en")))) {
//			LanguageChange.upDateLanguage(this.mContext);
//		}
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mRootView = getLayoutInflater().inflate(R.layout.activity_main, null);
		setContentView(mRootView);
		mContext = this;
		gInst = this;

		tv_singlecallpoint = findViewById(R.id.singlecallpoint);
		msgPoint = findViewById(R.id.msgpoint);
		msgPointPhoto = findViewById(R.id.msgpoint_photo);
		lll = findViewById(R.id.tab_bottm_size);

		tabInit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.popup_setting:
				resetAll();
				this.tv_setting_pop.setTextColor(-1);
				this.l_more.setBackgroundResource(R.drawable.main_tab_item_select);
//				startIntent(SettingNew.class);
				dismissPopupWindow();
				break;
			case R.id.popup_meetting:
				resetAll();
				this.tv_setting_pop.setTextColor(-1);
				View v_icon = findViewById(R.id.icon_more);
				this.l_more.setBackgroundResource(R.drawable.main_tab_item_select);
				v_icon.setBackgroundResource(R.drawable.tab_setting_after);
//				startIntent(AntaCallActivity2.class);
				dismissPopupWindow();
				break;
			default:
				break;
		}
	}

	public void tabInit() {
		ll_bottom = findViewById(R.id.tab_bottm_size);
		ll_actvityarea = findViewById(R.id.LinearLayout);

		ll_contact = findViewById(R.id.tab_contact);
		iv_contact = findViewById(R.id.icon_contact);
		tv_contact = findViewById(R.id.tab_1_text);

		ll_groupcall = findViewById(R.id.tab_groupcall);
		iv_groupcall = findViewById(R.id.icon_groupcall);
		tv_groupcall = findViewById(R.id.tab_2_text);

		fl_singlecall = findViewById(R.id.tab_singlecall);
		iv_singlecall = findViewById(R.id.icon_singlecall);
		tv_singlecall = findViewById(R.id.tab_3_text);

		fl_message = findViewById(R.id.tab_message);
		iv_message = findViewById(R.id.icon_message);
		tv_message = findViewById(R.id.tab_4_text);

		fl_phototransfer = findViewById(R.id.tab_photo_transfer);
		iv_phototransfer = findViewById(R.id.icon_photo_transfer);
		tv_phototransfer = findViewById(R.id.tab_5_text);

		ll_meeting = findViewById(R.id.tab_meeting);
		iv_meeting = findViewById(R.id.icon_meeting);
		tv_meeting = findViewById(R.id.tab_6_text);

		ll_setting = findViewById(R.id.tab_setting);
		iv_setting = findViewById(R.id.icon_setting);
		tv_setting = findViewById(R.id.tab_7_text);

		ll_contact.setOnClickListener(contactlistener);
		ll_groupcall.setOnClickListener(groupcalllistener);
		fl_singlecall.setOnClickListener(singlecalllistener);
		fl_message.setOnClickListener(messagelistener);
		fl_phototransfer.setOnClickListener(phototransferlistener);
		ll_meeting.setOnClickListener(conferencelistener);
		ll_setting.setOnClickListener(settinglistener);

		ll_contact.performClick();

	}

	void resetAll() {
		int clr = getResources().getColor(R.color.font_color3);

		ll_contact.setBackgroundDrawable(null);
		iv_contact.setBackgroundResource(R.drawable.tab_contact_before);
		tv_contact.setTextColor(clr);

		ll_groupcall.setBackgroundDrawable(null);
		iv_groupcall.setBackgroundResource(R.drawable.tab_groupcall_before);
		tv_groupcall.setTextColor(clr);

		fl_singlecall.setBackgroundDrawable(null);
		iv_singlecall.setBackgroundResource(R.drawable.tab_singlecall_before);
		tv_singlecall.setTextColor(clr);

		fl_message.setBackgroundDrawable(null);
		iv_message.setBackgroundResource(R.drawable.tab_message);
		tv_message.setTextColor(clr);

		fl_phototransfer.setBackgroundDrawable(null);
		iv_phototransfer.setBackgroundResource(R.drawable.tab_photo_up);
		tv_phototransfer.setTextColor(clr);

		ll_meeting.setBackgroundDrawable(null);
		iv_meeting.setBackgroundResource(R.drawable.tab_meeting_before);
		tv_meeting.setTextColor(clr);

		ll_setting.setBackgroundResource(R.drawable.setting_meetting_selector);
		iv_setting.setBackgroundResource(R.drawable.tab_setting_before);
		tv_setting.setTextColor(clr);
	}

	public void dismissPopupWindow() {
		if (v_SettingTransfer != null && v_SettingTransfer.isShowing()) {
			v_SettingTransfer.dismiss();
			isShowing = false;
		}
	}

	public void startIntent(final Class<?> clazz) {
		Intent intent = new Intent((Context) this, (Class) clazz);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startIntent(clazz.getSimpleName(), intent);
	}

	public void startIntent(final String s, final Intent intent) {
		LocalActivityManager localActivityManager = getLocalActivityManager();
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		View decorView = localActivityManager.startActivity(s, intent).getDecorView();
		ll_actvityarea.removeAllViews();
		ll_actvityarea.setPadding(0, 0, 0, 0);
		ll_actvityarea.addView(decorView, new ViewGroup.LayoutParams(-1, -1));
	}

}
