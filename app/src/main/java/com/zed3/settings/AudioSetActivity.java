package com.zed3.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zed3.audio.AudioSettings;
import com.zed3.codecs.AmrNB;
import com.zed3.codecs.EncodeRate;
import com.zed3.location.MemoryMg;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.SwitchButton;

public class AudioSetActivity extends BaseActivity implements View.OnClickListener {
	public static int curCodecNum;
	SwitchButton aec_swtich;
	SwitchButton agc_switch;
	LinearLayout arm_rate;
	LinearLayout audiovad;
	TextView audiovadvalue;
	LinearLayout btn_left;
	private SharedPreferences mSharedPreferences;
	LinearLayout phone_type;
	TextView phonetype_value;
	TextView ptimeValue;
	LinearLayout ptime_set;
	TextView rateValue;
	TextView voiceLagerValue;
	LinearLayout voice_Lager;

	static {
		AudioSetActivity.curCodecNum = 114;
	}

	private void commit(final String s, String replace) {
		replace = replace.replace("kbit/s", "").replace("ms", "");
		final SharedPreferences.Editor edit = this.mSharedPreferences.edit();
		edit.putString(s, replace);
		edit.commit();
	}

	private void commit(final String s, final boolean b) {
		final SharedPreferences.Editor edit = this.mSharedPreferences.edit();
		edit.putBoolean(s, b);
		edit.commit();
	}

	private void commit2(final String s, final String s2) {
		if (Build.VERSION.SDK_INT > 20) {
			return;
		}
		final SharedPreferences.Editor edit = this.mSharedPreferences.edit();
		edit.putString(s, s2);
		if (s2.equals("0")) {
			MemoryMg.getInstance().isAudioVAD = false;
		} else {
			MemoryMg.getInstance().isAudioVAD = true;
		}
		edit.commit();
	}

	private void commit3(final String s, final String s2) {
		final SharedPreferences.Editor edit = this.mSharedPreferences.edit();
		edit.putString(s, s2);
		edit.commit();
	}

	private Dialog createDialog(final int title, final int n, final int n2) {
		return (Dialog) new AlertDialog.Builder((Context) this).setTitle(title).setSingleChoiceItems(n, n2, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialogInterface, final int n) {
				if (title == R.string.setting_ARM_title) {
					AudioSetActivity.this.commit("amrMode", AudioSetActivity.this.getResources().getStringArray(n)[n]);
					final EncodeRate.Mode modeFromString = AmrNB.getModeFromString(AudioSetActivity.this.mSharedPreferences.getString("amrMode", "4.75"));
//					for (final CodecBase codecBase : Codecs.codecs) {
//						if ("AMR".equals(codecBase.name())) {
//							((AmrNB) codecBase).setRate(modeFromString);
//						}
//					}
				} else if (title == R.string.setting_PIME_title) {
					AudioSetActivity.this.commit("ptime", AudioSetActivity.this.getResources().getStringArray(n)[n]);
					final String string = AudioSetActivity.this.mSharedPreferences.getString("ptime", "20");
					if (!TextUtils.isEmpty((CharSequence) string) && TextUtils.isDigitsOnly((CharSequence) string) && string.length() < 4) {
						SettingsInfo.ptime = Integer.parseInt(string);
					}
				} else if (title == R.string.setting_PIME_detection) {
					AudioSetActivity.this.commit2("audiovadchk", new StringBuilder(String.valueOf(n)).toString());
				} else if (title == R.string.vc_type) {
					if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
						DeviceInfo.CONFIG_AUDIO_MODE = n;
					} else {
						MemoryMg.getInstance().PhoneType = n;
					}
					AudioSetActivity.this.commit("phoneMode", new StringBuilder(String.valueOf(n)).toString());
				} else if (title == R.string.setting_voice_large) {
					AudioSetActivity.this.commit3("voice_large", AudioSetActivity.this.getResources().getStringArray(n)[n]);
				}
				AudioSetActivity.this.updateSunmary();
				dialogInterface.dismiss();
			}
		}).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialogInterface, final int n) {
			}
		}).create();
	}

	private int findWhich(final String s, final String[] array) {
		if (array.length > 0) {
			for (int i = 0; i < array.length; ++i) {
				final int n = i;
				if (array[i].contains(s)) {
					return n;
				}
			}
		}
		return 0;
	}

	private int findWhich1(final String s, final String[] array) {
		if (array.length > 0) {
			for (int i = 0; i < array.length; ++i) {
				final int n = i;
				if (array[i].equals(s)) {
					return n;
				}
			}
		}
		return 0;
	}

	private void updateSunmary() {
		final String string = this.mSharedPreferences.getString("amrMode", "4.75");
		final TextView rateValue = this.rateValue;
		final StringBuilder sb = new StringBuilder(String.valueOf(string));
		String s;
		if ("Auto".equals(string)) {
			s = "";
		} else {
			s = "kbit/s";
		}
		rateValue.setText((CharSequence) sb.append(s).toString());
		this.ptimeValue.setText((CharSequence) (String.valueOf(this.mSharedPreferences.getString("ptime", "20")) + "ms"));
		final String string2 = this.mSharedPreferences.getString("audiovadchk", "0");
		final TextView audiovadvalue = this.audiovadvalue;
		int text;
		if (string2.equals("0")) {
			text = R.string.setting_detection_1;
		} else {
			text = R.string.setting_detection_2;
		}
		audiovadvalue.setText(text);
		final String string3 = this.mSharedPreferences.getString("phoneMode", "1");
		final TextView phonetype_value = this.phonetype_value;
		int text2;
		if (string3.equals("0")) {
			text2 = R.string.vc_type_1;
		} else {
			text2 = R.string.vc_type_2;
		}
		phonetype_value.setText(text2);
		final String string4 = this.mSharedPreferences.getString("voice_large", "0");
		this.voiceLagerValue.setText((CharSequence) string4);
		if (string4.equals("0")) {
			MemoryMg.Voice = 1.0f;
		} else {
			if (string4.equals("1.25")) {
				MemoryMg.Voice = 1.25f;
				return;
			}
			if (string4.equals("1.5")) {
				MemoryMg.Voice = 1.5f;
				return;
			}
			if (string4.equals("1.75")) {
				MemoryMg.Voice = 1.75f;
				return;
			}
			if (string4.equals("2")) {
				MemoryMg.Voice = 2.0f;
			}
		}
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			default: {
			}
			case R.id.arm_rate: {
				this.createDialog(R.string.setting_ARM_title, R.array.amrMode_txt_list, this.findWhich(this.mSharedPreferences.getString("amrMode", "4.75"), this.getResources().getStringArray(R.array.amrMode_txt_list))).show();
			}
			case R.id.ptime: {
				this.createDialog(R.string.setting_PIME_title, R.array.ptime_name_list, this.findWhich(this.mSharedPreferences.getString("ptime", "20"), this.getResources().getStringArray(R.array.ptime_name_list))).show();
			}
			case R.id.audiovad: {
				String s;
				if (this.mSharedPreferences.getString("audiovadchk", "0").equals("0")) {
					s = this.getResources().getString(R.string.setting_detection_1);
				} else {
					s = this.getResources().getString(R.string.setting_detection_2);
				}
				this.createDialog(R.string.setting_PIME_detection, R.array.gpstools_txt_list, this.findWhich(s, this.getResources().getStringArray(R.array.gpstools_txt_list))).show();
			}
			case R.id.phone_type: {
				this.createDialog(R.string.vc_type, R.array.phonetype_list, this.findWhich(this.mSharedPreferences.getString("phoneMode", "1"), this.getResources().getStringArray(R.array.phonetype_val_list))).show();
			}
			case R.id.voice_large: {
				this.createDialog(R.string.setting_voice_large, R.array.voice_large_select_list, this.findWhich1(this.mSharedPreferences.getString("voice_large", "0"), this.getResources().getStringArray(R.array.voice_large_select_list))).show();
			}
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.setting_audioset);
		this.mSharedPreferences = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		((TextView) this.findViewById(R.id.title)).setText(R.string.setting_voice_call);
		(this.arm_rate = (LinearLayout) this.findViewById(R.id.arm_rate)).setOnClickListener((View.OnClickListener) this);
		(this.ptime_set = (LinearLayout) this.findViewById(R.id.ptime)).setOnClickListener((View.OnClickListener) this);
		this.audiovad = (LinearLayout) this.findViewById(R.id.audiovad);
		if (DeviceInfo.CONFIG_SUPPORT_VAD) {
			this.audiovad.setOnClickListener((View.OnClickListener) this);
		} else {
			if (!this.mSharedPreferences.getString("audiovadchk", "0").equals("0")) {
				this.commit2("audiovadchk", "0");
			}
			this.audiovad.setVisibility(View.GONE);
			this.findViewById(R.id.audiovad_line).setVisibility(View.GONE);
		}
		this.phone_type = (LinearLayout) this.findViewById(R.id.phone_type);
		this.rateValue = (TextView) this.findViewById(R.id.rate_value);
		this.ptimeValue = (TextView) this.findViewById(R.id.ptimevalue);
		this.audiovadvalue = (TextView) this.findViewById(R.id.audiovadvalue);
		this.phonetype_value = (TextView) this.findViewById(R.id.phonetype_value);
		this.aec_swtich = (SwitchButton) this.findViewById(R.id.aec_switch);
		if (this.mSharedPreferences.getBoolean("AEC_SWITCH", true)) {
			this.aec_swtich.setChecked(true);
		} else {
			this.aec_swtich.setChecked(false);
		}
		this.aec_swtich.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
				final boolean b2 = false;
				final boolean boolean1 = AudioSetActivity.this.mSharedPreferences.getBoolean("AEC_SWITCH", true);
				AudioSetActivity.this.commit("AEC_SWITCH", !boolean1);
				AudioSettings.isAECOpen = (!boolean1 || b2);
			}
		});
		this.agc_switch = (SwitchButton) this.findViewById(R.id.agc_switch);
		if (this.mSharedPreferences.getBoolean("AGC_SWITCH", false)) {
			this.agc_switch.setChecked(true);
		} else {
			this.agc_switch.setChecked(false);
		}
		this.agc_switch.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
				final boolean b2 = false;
				final boolean boolean1 = AudioSetActivity.this.mSharedPreferences.getBoolean("AGC_SWITCH", false);
				AudioSetActivity.this.commit("AGC_SWITCH", !boolean1);
				AudioSettings.isAGCOpen = (!boolean1 || b2);
			}
		});
		(this.voice_Lager = (LinearLayout) this.findViewById(R.id.voice_large)).setOnClickListener((View.OnClickListener) this);
		this.voiceLagerValue = (TextView) this.findViewById(R.id.voice_large_value);
		this.updateSunmary();
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.advanced);
		(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				AudioSetActivity.this.finish();
			}
		});
		this.btn_left.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) AudioSetActivity.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) AudioSetActivity.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						AudioSetActivity.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(AudioSetActivity.this.getResources().getColor(R.color.font_color3));
						AudioSetActivity.this.btn_left.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
	}
}
