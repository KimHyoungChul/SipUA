package com.zed3.sipua.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.toast.MyToast;
import com.zed3.utils.SwitchButton;
import com.zed3.video.DeviceVideoInfo;
import com.zed3.video.VideoManagerService;

public class SettingVideoSize extends BaseActivity {
	public static String[] DT_T98;
	private static final String LOG_TAG;
	public static String[] NORMAL;
	public static final String QVGA = "5";
	public static final String R720P = "2";
	public static final String VGA = "6";
	static final int[] pixArray;
	public static final String sharedPrefsFile = "com.zed3.sipua_preferences";
	private RadioButton backcamera;
	LinearLayout btn_left;
	CheckBox chklock;
	SwitchButton color_correct;
	EditText et_frame;
	EditText et_iframe;
	EditText et_netrate;
	private RadioButton frontcamera;
	SwitchButton fullscreen_ctrl;
	public boolean isFront;
	private String mAction;
	SharedPreferences mSharedPreferences;
	private RadioGroup maingroup;
	SharedPreferences mypre;
	private RadioButton rad_hor;
	private RadioButton rad_rotate;
	private RadioButton rad_ver;
	private RadioButton rb_720p;
	private RadioButton rb_qvga;
	private RadioButton rb_vga;
	private RadioGroup rg_allowloast;
	private RadioGroup screengroup;
	private RadioGroup solutionGroup;

	static {
		SettingVideoSize.NORMAL = new String[]{"1,20,1600", "1,10,4000", "1,10,6400"};
		SettingVideoSize.DT_T98 = new String[]{"1,20,2400", "1,10,6400", "1,10,16000"};
		LOG_TAG = SettingVideoSize.class.getSimpleName();
		pixArray = new int[]{2, 5, 6};
	}

	public SettingVideoSize() {
		this.maingroup = null;
		this.solutionGroup = null;
		this.screengroup = null;
		this.rg_allowloast = null;
		this.mypre = null;
		this.chklock = null;
		this.mAction = "";
	}

	private void InitRadioButton() {
		this.mypre = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		int n;
		if (!this.mAction.equals("com.zed3.action.VIDEO_UPLOAD") && !this.mAction.equals("com.zed3.action.VIDEO_MONITOR")) {
			n = 0;
		} else {
			n = 1;
		}
		final SharedPreferences mypre = this.mypre;
		final String camera_TYPE_FRONT_OR_POSTPOS = DeviceVideoInfo.CAMERA_TYPE_FRONT_OR_POSTPOS;
		String s;
		if (n != 0) {
			s = "0";
		} else {
			s = "1";
		}
		final String string = mypre.getString(camera_TYPE_FRONT_OR_POSTPOS, s);
		if (string.equals("0")) {
			this.maingroup.check(R.id.backcamera);
		}
		if (string.equals("1")) {
			this.maingroup.check(R.id.frontcamera);
		}
		final String curVideoKey = this.getCurVideoKey();
		final String supportVideoSizeStr = MemoryMg.getInstance().SupportVideoSizeStr;
		MyLog.i("SupportVideoSizeStr", supportVideoSizeStr);
		if (supportVideoSizeStr.length() > 0) {
			final String[] split = supportVideoSizeStr.split(",");
			if (split != null) {
				this.rb_qvga.setVisibility(View.GONE);
				this.rb_vga.setVisibility(View.GONE);
				this.rb_720p.setVisibility(View.GONE);
				for (int length = split.length, i = 0; i < length; ++i) {
					final String s2 = split[i];
					if (s2.equals("320*240")) {
						this.rb_qvga.setVisibility(View.VISIBLE);
					} else if (s2.equals("640*480")) {
						this.rb_vga.setVisibility(View.VISIBLE);
					} else if (s2.equals("1280*720")) {
						this.rb_720p.setVisibility(View.VISIBLE);
					}
				}
			}
		}
		if (curVideoKey.equals("5")) {
			this.solutionGroup.check(R.id.rqvga);
		} else if (curVideoKey.equals("6")) {
			this.solutionGroup.check(R.id.rvga);
		} else {
			this.solutionGroup.check(R.id.r720p);
		}
		(this.rg_allowloast = (RadioGroup) this.findViewById(R.id.rg_packetlost)).check(this.findId());
		this.rg_allowloast.setOnCheckedChangeListener((RadioGroup.OnCheckedChangeListener) new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(final RadioGroup radioGroup, int lostLevel) {
				final int n = 1;
				switch (lostLevel) {
					default: {
						lostLevel = n;
						break;
					}
					case R.id.one: {
						lostLevel = 1;
						break;
					}
					case R.id.two: {
						lostLevel = 2;
						break;
					}
					case R.id.three: {
						lostLevel = 3;
						break;
					}
					case R.id.four: {
						lostLevel = 4;
						break;
					}
					case R.id.five: {
						lostLevel = 5;
						break;
					}
				}
				DeviceVideoInfo.lostLevel = lostLevel;
				SettingVideoSize.this.commit(DeviceVideoInfo.PACKET_LOST_LEVEL, lostLevel);
			}
		});
	}

	private void commit(final String s, final int n) {
		final SharedPreferences.Editor edit = this.mSharedPreferences.edit();
		edit.putInt(s, n);
		edit.commit();
	}

	private void commit(final String s, final String s2) {
		final SharedPreferences.Editor edit = this.mSharedPreferences.edit();
		edit.putString(s, s2);
		edit.commit();
	}

	private void commit(final String s, final boolean b) {
		final SharedPreferences.Editor edit = this.mSharedPreferences.edit();
		edit.putBoolean(s, b);
		edit.commit();
	}

	private int findId() {
		switch (this.mSharedPreferences.getInt(DeviceVideoInfo.PACKET_LOST_LEVEL, 1)) {
			default: {
				return R.id.one;
			}
			case 1: {
				return R.id.one;
			}
			case 2: {
				return R.id.two;
			}
			case 3: {
				return R.id.three;
			}
			case 4: {
				return R.id.four;
			}
			case 5: {
				return R.id.five;
			}
		}
	}

	private String getAction(final Intent intent) {
		return intent.getStringExtra("com.zed3.extra.VIDEO_ACTION");
	}

	public static String getCurVideoSize(final String s, final String s2) {
		String s3 = "";
		if ("2".equals(s)) {
			s3 = "1080*720";
		} else if ("5".equals(s)) {
			s3 = "320*240";
		} else if ("6".equals(s)) {
			s3 = "640*480";
		}
		return VideoManagerService.getDefault().buildColumn(s2, s3);
	}

	public static String getDefaultValue(final String s) {
		String s2 = "";
		if ("2".equals(s)) {
			if (!Build.MODEL.toLowerCase().contains("datang")) {
				return SettingVideoSize.NORMAL[2];
			}
			s2 = SettingVideoSize.DT_T98[2];
		} else if ("5".equals(s)) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				return SettingVideoSize.DT_T98[0];
			}
			return SettingVideoSize.NORMAL[0];
		} else if ("6".equals(s)) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				return SettingVideoSize.DT_T98[1];
			}
			return SettingVideoSize.NORMAL[1];
		}
		return s2;
	}

	public static String getResolution(final String s) {
		String s2 = "";
		if ("720p".equals(s)) {
			s2 = "1280*720";
		} else {
			if ("qvga".equals(s)) {
				return "320*240";
			}
			if ("vga".equals(s)) {
				return "640*480";
			}
		}
		return s2;
	}

	private String getTitle(final Intent intent) {
		return intent.getStringExtra("com.zed3.extra.VIDEO_TITLE");
	}

	public static void setDefaultValue(final String s, final Context context) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor edit = defaultSharedPreferences.edit();
		final int[] pixArray = SettingVideoSize.pixArray;
		for (int length = pixArray.length, i = 0; i < length; ++i) {
			final int n = pixArray[i];
			final String curVideoSize = getCurVideoSize(new StringBuilder(String.valueOf(n)).toString(), s);
			final String string = defaultSharedPreferences.getString(curVideoSize, "aaa");
			final String defaultValue = getDefaultValue(String.valueOf(n));
			if (string.equals("aaa") || string.equals(curVideoSize)) {
				edit.putString(getCurVideoSize(new StringBuilder(String.valueOf(n)).toString(), s), defaultValue);
				edit.commit();
			}
		}
	}

	String getCurVideoKey() {
		String string = "";
		if (this.isFront) {
			if (this.mypre != null) {
				string = this.mypre.getString(DeviceVideoInfo.CAMERA_FRONT_RESOLUTION, "5");
			}
		} else if (this.mypre != null) {
			return this.mypre.getString(DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN, "6");
		}
		return string;
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setSoftInputMode(3);
		this.setContentView(R.layout.settingvideosize);
		final Intent intent = this.getIntent();
		final String action = this.getAction(intent);
		final String title = this.getTitle(intent);
		if (TextUtils.isEmpty((CharSequence) action)) {
			this.finish();
			return;
		}
		this.mAction = action;
		final VideoManagerService default1 = VideoManagerService.getDefault();
		default1.initVideoSettingColumns(action);
		default1.initSettingValue((Context) this, action);
		((TextView) this.findViewById(R.id.title)).setText((CharSequence) title);
		((ImageButton) this.findViewById(R.id.back)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				SettingVideoSize.this.finish();
			}
		});
		this.mSharedPreferences = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		this.color_correct = (SwitchButton) this.findViewById(R.id.color_correct);
		if (this.mSharedPreferences.getBoolean(DeviceVideoInfo.VIDEO_COLOR_CORRECT, false)) {
			this.color_correct.setChecked(true);
		} else {
			this.color_correct.setChecked(false);
		}
		this.color_correct.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
				final boolean b2 = false;
				final boolean boolean1 = SettingVideoSize.this.mSharedPreferences.getBoolean(DeviceVideoInfo.VIDEO_COLOR_CORRECT, false);
				SettingVideoSize.this.commit(DeviceVideoInfo.VIDEO_COLOR_CORRECT, !boolean1);
				DeviceVideoInfo.color_correct = (!boolean1 || b2);
			}
		});
		this.fullscreen_ctrl = (SwitchButton) this.findViewById(R.id.fullscreen_ctrl);
		if (this.mSharedPreferences.getBoolean("full_screen", false)) {
			this.fullscreen_ctrl.setChecked(true);
		} else {
			this.fullscreen_ctrl.setChecked(false);
		}
		this.fullscreen_ctrl.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
				final boolean b2 = false;
				final boolean boolean1 = SettingVideoSize.this.mSharedPreferences.getBoolean("full_screen", false);
				SettingVideoSize.this.commit("full_screen", !boolean1);
				DeviceVideoInfo.supportFullScreen = (!boolean1 || b2);
			}
		});
		setDefaultValue(this.mAction, (Context) this);
		this.screengroup = (RadioGroup) this.findViewById(R.id.screengroup);
		this.rad_ver = (RadioButton) this.findViewById(R.id.ver_screen);
		this.rad_hor = (RadioButton) this.findViewById(R.id.hor_screen);
		this.rad_rotate = (RadioButton) this.findViewById(R.id.rotate_screen);
		if (DeviceVideoInfo.screen_type.equals("ver")) {
			this.screengroup.check(this.rad_ver.getId());
		} else if (DeviceVideoInfo.screen_type.equals("hor")) {
			this.screengroup.check(this.rad_hor.getId());
		} else {
			this.screengroup.check(this.rad_rotate.getId());
		}
		this.screengroup.setOnCheckedChangeListener((RadioGroup.OnCheckedChangeListener) new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(final RadioGroup radioGroup, final int n) {
				String screen_type = "ver";
				if (n == SettingVideoSize.this.rad_ver.getId()) {
					screen_type = "ver";
					DeviceVideoInfo.isHorizontal = false;
					DeviceVideoInfo.supportRotate = false;
					DeviceVideoInfo.onlyCameraRotate = true;
				} else if (n == SettingVideoSize.this.rad_hor.getId()) {
					screen_type = "hor";
					DeviceVideoInfo.isHorizontal = true;
					DeviceVideoInfo.supportRotate = false;
					DeviceVideoInfo.onlyCameraRotate = true;
				} else if (n == SettingVideoSize.this.rad_rotate.getId()) {
					screen_type = "rotate";
					DeviceVideoInfo.isHorizontal = false;
					DeviceVideoInfo.supportRotate = true;
					DeviceVideoInfo.onlyCameraRotate = false;
				}
				DeviceVideoInfo.screen_type = screen_type;
				SettingVideoSize.this.commit(DeviceVideoInfo.SCREEN_TYPE, screen_type);
			}
		});
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.settings_video);
		(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				SettingVideoSize.this.finish();
			}
		});
		this.btn_left.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) SettingVideoSize.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) SettingVideoSize.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						SettingVideoSize.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(SettingVideoSize.this.getResources().getColor(R.color.font_color3));
						SettingVideoSize.this.btn_left.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
		this.solutionGroup = (RadioGroup) this.findViewById(R.id.solution_group);
		this.rb_qvga = (RadioButton) this.findViewById(R.id.rqvga);
		this.rb_vga = (RadioButton) this.findViewById(R.id.rvga);
		this.rb_720p = (RadioButton) this.findViewById(R.id.r720p);
		this.maingroup = (RadioGroup) this.findViewById(R.id.maingroup);
		this.frontcamera = (RadioButton) this.findViewById(R.id.frontcamera);
		this.backcamera = (RadioButton) this.findViewById(R.id.backcamera);
		this.maingroup.setOnCheckedChangeListener((RadioGroup.OnCheckedChangeListener) new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(final RadioGroup radioGroup, final int n) {
				SettingVideoSize.this.mypre = SettingVideoSize.this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
				final SharedPreferences.Editor edit = SettingVideoSize.this.mypre.edit();
				if (n == SettingVideoSize.this.frontcamera.getId()) {
					edit.putString(DeviceVideoInfo.CAMERA_TYPE_FRONT_OR_POSTPOS, "1");
					SettingVideoSize.this.isFront = true;
					final String string = SettingVideoSize.this.mypre.getString(DeviceVideoInfo.CAMERA_FRONT_RESOLUTION, "5");
					if (string.equalsIgnoreCase("5")) {
						SettingVideoSize.this.solutionGroup.check(SettingVideoSize.this.rb_qvga.getId());
					} else if (string.equalsIgnoreCase("6")) {
						SettingVideoSize.this.solutionGroup.check(SettingVideoSize.this.rb_vga.getId());
					} else if (string.equalsIgnoreCase("2")) {
						SettingVideoSize.this.solutionGroup.check(SettingVideoSize.this.rb_720p.getId());
					}
				} else if (n == SettingVideoSize.this.backcamera.getId()) {
					edit.putString(DeviceVideoInfo.CAMERA_TYPE_FRONT_OR_POSTPOS, "0");
					SettingVideoSize.this.isFront = false;
					final String string2 = SettingVideoSize.this.mypre.getString(DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN, "6");
					if (string2.equalsIgnoreCase("5")) {
						SettingVideoSize.this.solutionGroup.check(SettingVideoSize.this.rb_qvga.getId());
					} else if (string2.equalsIgnoreCase("6")) {
						SettingVideoSize.this.solutionGroup.check(SettingVideoSize.this.rb_vga.getId());
					} else if (string2.equalsIgnoreCase("2")) {
						SettingVideoSize.this.solutionGroup.check(SettingVideoSize.this.rb_720p.getId());
					}
				}
				SettingVideoSize.this.updateVedioframe(SettingVideoSize.this.getCurVideoKey());
				edit.commit();
			}
		});
		this.solutionGroup.setOnCheckedChangeListener((RadioGroup.OnCheckedChangeListener) new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(final RadioGroup radioGroup, final int n) {
				SettingVideoSize.this.mypre = SettingVideoSize.this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
				String s = "5";
				final SharedPreferences.Editor edit = SettingVideoSize.this.mypre.edit();
				if (n == SettingVideoSize.this.rb_720p.getId()) {
					if (SettingVideoSize.this.isFront) {
						edit.putString(DeviceVideoInfo.CAMERA_FRONT_RESOLUTION, "2");
						s = "2";
					} else {
						edit.putString(DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN, "2");
						s = "2";
					}
				} else if (n == SettingVideoSize.this.rb_vga.getId()) {
					if (SettingVideoSize.this.isFront) {
						edit.putString(DeviceVideoInfo.CAMERA_FRONT_RESOLUTION, "6");
						s = "6";
					} else {
						edit.putString(DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN, "6");
						s = "6";
					}
				} else if (n == SettingVideoSize.this.rb_qvga.getId()) {
					if (SettingVideoSize.this.isFront) {
						edit.putString(DeviceVideoInfo.CAMERA_FRONT_RESOLUTION, "5");
						s = "5";
					} else {
						edit.putString(DeviceVideoInfo.CAMERA_POSTPOSTION_RESOLUTIN, "5");
						s = "5";
					}
				}
				edit.commit();
				SettingVideoSize.this.updateVedioframe(s);
			}
		});
		this.et_frame = (EditText) this.findViewById(R.id.frame_rate);
		this.et_iframe = (EditText) this.findViewById(R.id.iframerate);
		this.et_netrate = (EditText) this.findViewById(R.id.net_rate);
		this.et_frame.clearFocus();
		this.et_iframe.clearFocus();
		this.et_netrate.clearFocus();
		this.InitRadioButton();
		this.updateVedioframe(this.getCurVideoKey());
	}

	public void onSave(final View view) {
		if (TextUtils.isEmpty((CharSequence) this.et_iframe.getText())) {
			MyToast.showToast(true, (Context) this, "I\u5e27\u95f4\u9694\u4e0d\u80fd\u4e3a\u7a7a");
		} else {
			final String string = this.et_iframe.getText().toString();
			if (string.length() > 2 || Integer.parseInt(string) < 1 || Integer.parseInt(string) > 30) {
				MyToast.showToast(true, (Context) this, "I\u5e27\u95f4\u9694\u8303\u56f41~30");
				return;
			}
			if (TextUtils.isEmpty((CharSequence) this.et_netrate.getText())) {
				MyToast.showToast(true, (Context) this, "\u7801\u7387\u4e0d\u80fd\u4e3a\u7a7a");
				return;
			}
			final String string2 = this.et_frame.getText().toString();
			if (string2.length() > 2 || Integer.parseInt(string2) < 1 || Integer.parseInt(string2) > 30) {
				MyToast.showToast(true, (Context) this, "\u5e27\u7387\u95f4\u9694\u8303\u56f41~30");
				return;
			}
			if (TextUtils.isEmpty((CharSequence) this.et_frame.getText())) {
				MyToast.showToast(true, (Context) this, "\u5e27\u7387\u4e0d\u80fd\u4e3a\u7a7a");
				return;
			}
			final String string3 = this.et_netrate.getText().toString();
			if ((string3.length() > 5 || Integer.parseInt(string3) < 400 || Integer.parseInt(string3) > 32000) && !Build.MODEL.toLowerCase().contains("datang") && !Build.MODEL.toLowerCase().contains("fh688") && !Build.MODEL.toLowerCase().contains("lter")) {
				MyToast.showToast(true, (Context) this, "\u7801\u7387\u8303\u56f4400~32000");
				return;
			}
			final String string4 = this.et_iframe.getText() + "," + this.et_frame.getText() + "," + this.et_netrate.getText();
			if (this.mypre != null) {
				final SharedPreferences.Editor edit = this.mypre.edit();
				edit.putString(getCurVideoSize(this.getCurVideoKey(), this.mAction), string4);
				if (edit.commit()) {
					Toast.makeText(this.getApplicationContext(), (CharSequence) this.getResources().getString(R.string.save_success), Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public void updateVedioframe(String curVideoSize) {
		curVideoSize = getCurVideoSize(curVideoSize, this.mAction);
		final String[] split = this.mypre.getString(curVideoSize, "5,10,2400").split(",");
		if (split.length == 3) {
			this.et_iframe.setText((CharSequence) split[0]);
			this.et_frame.setText((CharSequence) split[1]);
			this.et_netrate.setText((CharSequence) split[2]);
		}
	}
}
