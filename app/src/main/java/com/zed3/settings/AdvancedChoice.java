package com.zed3.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.bluetooth.ZMBluetoothManager;
import com.zed3.broadcastptt.SettingsBroadcastActivity;
import com.zed3.dialog.DialogUtil;
import com.zed3.location.MemoryMg;
import com.zed3.log.CrashHandler;
import com.zed3.log.MyLog;
import com.zed3.power.MyPowerManager;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.SwitchButton;
import com.zed3.utils.Tools;

import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.provider.UdpTransport;

import java.util.Locale;

public class AdvancedChoice extends BaseActivity implements OnClickListener {
	LinearLayout audio_set;
	LinearLayout bgdate_show;
	SwitchButton bluetooth_onoff;
	LinearLayout bluetooth_set;
	TextView bluetooth_summary;
	private LinearLayout broadcast_llyt;
	LinearLayout btn_left;
	int count = 0;
	TextView currentLanguage;
	TextView currentScreenWakeupPeriodInfo;
	SwitchButton encrypt_onoff;
	LinearLayout encrypt_set;
	int flag = 0;
	SwitchButton flow_ctrl;
	LinearLayout flow_set;
	TextView flow_summary;
	LinearLayout gpsOnOff;
	SwitchButton gps_ctrl;
	TextView gpssummary;
	LinearLayout groupcall_set;
	LinearLayout language;
	int languageId = 0;
	private LinearLayout llytFullWakelock;
	TextView locateModetxt;
	SwitchButton log_ctrl;
	LinearLayout log_set;
	TextView logsummary;
	private SharedPreferences mSharedPreferences;
	LinearLayout map_set;
	int maptype = 0;
	TextView msgencry_summary;
	LinearLayout position_set;
	int regTime;
	LinearLayout registertime_btn;
	TextView registertime_summary;
	LinearLayout screenWakeupPeriod;
	int screenWakeupPeriodIndex = 0;
	private SwitchButton switchFullWakelock;
	private TextView txtFullWakelock;
	LinearLayout video_set;
	LinearLayout wakeup_onoff;
	SwitchButton wakeup_swt;
	TextView wakeupsummary;

	class C10071 implements OnClickListener {

		class C10051 implements DialogInterface.OnClickListener {
			C10051() {
			}

			public void onClick(DialogInterface dialog, int which) {
				AdvancedChoice.this.languageId = which;
			}
		}

		class C10062 implements DialogInterface.OnClickListener {
			C10062() {
			}

			public void onClick(DialogInterface dialog, int which) {
				if (AdvancedChoice.this.languageId != AdvancedChoice.this.flag) {
//					if (MainActivity.getInstance() != null) {
//						MainActivity.getInstance().finish();
//					}
					AdvancedChoice.this.mSharedPreferences.edit().putInt("languageId", AdvancedChoice.this.languageId).commit();
					Resources resources = AdvancedChoice.this.getResources();
					Configuration config = resources.getConfiguration();
					DisplayMetrics dm = resources.getDisplayMetrics();
					Receiver.GetCurUA().GPSCloseLock();
					switch (AdvancedChoice.this.languageId) {
						case 0:
							config.locale = Locale.getDefault();
							AdvancedChoice.this.currentLanguage.setText(R.string.language_d);
							if (!AdvancedChoice.this.getResources().getConfiguration().locale.getCountry().equals("CN")) {
								DeviceInfo.CONFIG_MAP_TYPE = 1;
								AdvancedChoice.this.mSharedPreferences.edit().putInt(Settings.PREF_MAP_TYPE, DeviceInfo.CONFIG_MAP_TYPE).commit();
								break;
							}
							DeviceInfo.CONFIG_MAP_TYPE = 0;
							AdvancedChoice.this.mSharedPreferences.edit().putInt(Settings.PREF_MAP_TYPE, DeviceInfo.CONFIG_MAP_TYPE).commit();
							break;
						case 1:
							config.locale = Locale.SIMPLIFIED_CHINESE;
							DeviceInfo.CONFIG_MAP_TYPE = 0;
							AdvancedChoice.this.mSharedPreferences.edit().putInt(Settings.PREF_MAP_TYPE, DeviceInfo.CONFIG_MAP_TYPE).commit();
							AdvancedChoice.this.currentLanguage.setText(R.string.language_c);
							break;
						case 2:
							config.locale = Locale.ENGLISH;
							DeviceInfo.CONFIG_MAP_TYPE = 1;
							AdvancedChoice.this.currentLanguage.setText(R.string.language_e);
							if (!(DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN || DeviceInfo.GPS_REMOTE == 0)) {
								AdvancedChoice.this.mSharedPreferences.edit().putInt(Settings.PREF_MAP_TYPE, DeviceInfo.CONFIG_MAP_TYPE).commit();
								break;
							}
						case 3:
							config.locale = Locale.TAIWAN;
							AdvancedChoice.this.currentLanguage.setText(R.string.language_tw);
							DeviceInfo.CONFIG_MAP_TYPE = 1;
							if (!(DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN || DeviceInfo.GPS_REMOTE == 0)) {
								AdvancedChoice.this.mSharedPreferences.edit().putInt(Settings.PREF_MAP_TYPE, DeviceInfo.CONFIG_MAP_TYPE).commit();
								break;
							}
					}
					if (Tools.getCurrentGpsMode() != 3) {
						Receiver.GetCurUA().GPSOpenLock();
					}
					resources.updateConfiguration(config, dm);
					dialog.dismiss();
					AdvancedChoice.this.startActivity(new Intent(AdvancedChoice.this, MainActivity.class));
					AdvancedChoice.this.sendBroadcast(new Intent("SettingLanguage"));
					AdvancedChoice.this.finish();
				}
			}
		}

		C10071() {
		}

		public void onClick(View v) {
			new Builder(AdvancedChoice.this).setSingleChoiceItems(AdvancedChoice.this.getResources().getStringArray(R.array.languageList), AdvancedChoice.this.mSharedPreferences.getInt("languageId", 0), new C10051()).setTitle(R.string.select_language).setPositiveButton(AdvancedChoice.this.getResources().getString(R.string.save), new C10062()).create().show();
		}
	}

	class C10092 implements OnClickListener {
		C10092() {
		}

		public void onClick(View v) {
			final String[] llist = AdvancedChoice.this.getResources().getStringArray(R.array.screen_wakeup_period_List);
			new Builder(AdvancedChoice.this).setSingleChoiceItems(llist, AdvancedChoice.this.mSharedPreferences.getInt(MyPowerManager.KEY_SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX, 0), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which != AdvancedChoice.this.screenWakeupPeriodIndex) {
						AdvancedChoice.this.screenWakeupPeriodIndex = which;
						AdvancedChoice.this.mSharedPreferences.edit().putInt(MyPowerManager.KEY_SCREEN_WAKEUP_PERIOD_DEFAULT_INDEX, AdvancedChoice.this.screenWakeupPeriodIndex).commit();
						MyPowerManager powerManager = MyPowerManager.getInstance();
						powerManager.setScreenWakeupPeriod(powerManager.getScreenWakeupPeriodFromArray(AdvancedChoice.this.screenWakeupPeriodIndex));
						AdvancedChoice.this.currentScreenWakeupPeriodInfo.setText(llist[AdvancedChoice.this.screenWakeupPeriodIndex]);
						dialog.dismiss();
					}
				}
			}).setTitle(R.string.select_screen_wakeup_period).create().show();
		}
	}

	class C10103 implements OnClickListener {
		C10103() {
		}

		public void onClick(View v) {
			AdvancedChoice.this.finish();
		}
	}

	class C10114 implements OnClickListener {
		C10114() {
		}

		public void onClick(View v) {
			AdvancedChoice.this.finish();
		}
	}

	class C10125 implements OnTouchListener {
		C10125() {
		}

		public boolean onTouch(View v, MotionEvent event) {
			TextView tv = (TextView) AdvancedChoice.this.findViewById(R.id.t_leftbtn);
			TextView tv_left = (TextView) AdvancedChoice.this.findViewById(R.id.left_icon);
			switch (event.getAction()) {
				case 0:
					tv.setTextColor(-1);
					AdvancedChoice.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
					tv_left.setBackgroundResource(R.drawable.map_back_press);
					break;
				case 1:
					tv.setTextColor(AdvancedChoice.this.getResources().getColor(R.color.font_color3));
					AdvancedChoice.this.btn_left.setBackgroundResource(R.color.whole_bg);
					tv_left.setBackgroundResource(R.drawable.map_back_release);
					break;
			}
			return false;
		}
	}

	class C10136 implements OnClickListener {
		C10136() {
		}

		public void onClick(View v) {
			AdvancedChoice.this.regTime = AdvancedChoice.this.mSharedPreferences.getInt(Settings.PREF_REGTIME_EXPIRES, 1800);
			AdvancedChoice.this.showDialog_Layout();
		}
	}

	class C10147 implements OnCheckedChangeListener {
		C10147() {
		}

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			boolean z = false;
			MyLog.e("advancedchoice", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
			boolean flag = AdvancedChoice.this.mSharedPreferences.getBoolean("flowOnOffKey", false);
			AdvancedChoice advancedChoice = AdvancedChoice.this;
			String str = "flowOnOffKey";
			if (!flag) {
				z = true;
			}
			advancedChoice.commit(str, z);
			//new Intent().setFlags(2);
			new Intent().setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			Intent intent = new Intent();
			intent.setAction("com.zed3.flow.FlowRefreshService");
			if (flag) {
				AdvancedChoice.this.stopService(intent);
			} else {
				AdvancedChoice.this.startService(intent);
			}
			AdvancedChoice.this.updateSummary();
		}
	}

	class C10158 implements OnCheckedChangeListener {
		C10158() {
		}

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			AdvancedChoice.this.commit(Settings.PREF_MSG_ENCRYPT, !AdvancedChoice.this.mSharedPreferences.getBoolean(Settings.PREF_MSG_ENCRYPT, false));
			AdvancedChoice.this.updateSummary();
			UdpTransport.needEncrypt = Boolean.valueOf(AdvancedChoice.this.mSharedPreferences.getBoolean(Settings.PREF_MSG_ENCRYPT, false));
			if (Receiver.mSipdroidEngine == null) {
				Receiver.engine(AdvancedChoice.this);
			} else {
				Receiver.mSipdroidEngine.register(true);
			}
		}
	}

	class C10169 implements OnCheckedChangeListener {
		C10169() {
		}

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			boolean z = true;
			boolean flag = AdvancedChoice.this.mSharedPreferences.getBoolean(Settings.PREF_GPSONOFF, true);
			AdvancedChoice advancedChoice = AdvancedChoice.this;
			String str = Settings.PREF_GPSONOFF;
			if (flag) {
				z = false;
			}
			advancedChoice.commit(str, z);
			MyLog.v("dd", "flag=" + flag);
			if (flag) {
				Receiver.GetCurUA().GPSCloseLock();
			} else {
				Receiver.GetCurUA().GPSOpenLock();
			}
			AdvancedChoice.this.updateSummary();
		}
	}

	private void commit(final String s, final boolean b) {
		final SharedPreferences.Editor edit = this.mSharedPreferences.edit();
		edit.putBoolean(s, b);
		edit.commit();
	}

	private void setOnCheckedChangeListener() {
		this.flow_ctrl.setOnCheckedChangeListener(new C10147());
		this.encrypt_onoff.setOnCheckedChangeListener(new C10158());
		this.gps_ctrl.setOnCheckedChangeListener(new C10169());
		this.log_ctrl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				AdvancedChoice.this.commit(Settings.PREF_LOG, isChecked);
				if (isChecked) {
					CrashHandler.getInstance().init(AdvancedChoice.this, true);
				} else {
					CrashHandler.EndLog();
				}
				AdvancedChoice.this.updateSummary();
			}
		});
		this.bluetooth_onoff.setOnCheckedChangeListener((OnCheckedChangeListener) new OnCheckedChangeListener() {
			boolean mIsCloseByUser = true;
			boolean mIsOpenByUser = true;

			public void onCheckedChanged(final CompoundButton compoundButton, final boolean mNeedBlueTooth) {
				AdvancedChoice.this.commit("bluetoothonoff", mNeedBlueTooth);
				Settings.mNeedBlueTooth = mNeedBlueTooth;
				if (mNeedBlueTooth) {
					if (this.mIsOpenByUser) {
						ZMBluetoothManager.getInstance().connectZMBluetooth(SipUAApp.mContext);
					}
					AdvancedChoice.this.updateSummary();
					return;
				}
				if (ZMBluetoothManager.getInstance().getSPPConnectedDevices().size() > 0) {
					DialogUtil.showSelectDialog(AdvancedChoice.this, AdvancedChoice.this.getResources().getString(R.string.close_hm), AdvancedChoice.this.getResources().getString(R.string.close_hm_notify), AdvancedChoice.this.getResources().getString(R.string.disconnect), (DialogUtil.DialogCallBack) new DialogUtil.DialogCallBack() {
						@Override
						public void onNegativeButtonClick() {
							AdvancedChoice.this.commit("bluetoothonoff", true);
							Settings.mNeedBlueTooth = true;
							// TODO
//							OnCheckedChangeListener.this.mIsOpenByUser = false;
							AdvancedChoice.this.bluetooth_onoff.setChecked(true);
							AdvancedChoice.this.updateSummary();
						}

						@Override
						public void onPositiveButtonClick() {
							ZMBluetoothManager.getInstance().mNeedAskUserToReconnectSpp = false;
							ZMBluetoothManager.getInstance().disConnectZMBluetooth(SipUAApp.mContext);
							ZMBluetoothManager.getInstance().exit(SipUAApp.mContext);
							if (ZMBluetoothManager.getInstance().isBluetoothAdapterEnabled()) {
								ZMBluetoothManager.getInstance().askUserToDisableBluetooth();
							}
							// TODO
//							OnCheckedChangeListener.this.mIsOpenByUser = true;
							AdvancedChoice.this.updateSummary();
						}
					});
					return;
				}
				AdvancedChoice.this.updateSummary();
			}
		});
		this.wakeup_swt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				boolean flag = AdvancedChoice.this.mSharedPreferences.getBoolean(Settings.PREF_MICWAKEUP_ONOFF, true);
				AdvancedChoice.this.commit(Settings.PREF_MICWAKEUP_ONOFF, !flag);
				if (flag) {
					MemoryMg.getInstance().isMicWakeUp = false;
				} else {
					MemoryMg.getInstance().isMicWakeUp = true;
				}
				AdvancedChoice.this.updateSummary();
			}
		});
		this.switchFullWakelock.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				AdvancedChoice.this.commit(Settings.PREF_FULLWAKELOCK_ONOFF, isChecked);
				AdvancedChoice.this.updateSummary();
				((SipUAApp) AdvancedChoice.this.getApplication()).wakeLock(!isChecked);
			}
		});
	}

	private void showDialog_Layout() {
		View textEntryView = LayoutInflater.from(this).inflate(R.layout.dialoglayout, null);
		final EditText edtInput = (EditText) textEntryView.findViewById(R.id.edtInput);
		edtInput.setInputType(2);
		edtInput.setText(new StringBuilder(String.valueOf(this.regTime)).toString());
		Builder builder = new Builder(this);
		builder.setCancelable(false);
		builder.setIcon(R.drawable.icon22);
		builder.setTitle(R.string.setting_register_dialog_title);
		builder.setView(textEntryView);
		builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			@SuppressLint("ApplySharedPref")
			public void onClick(DialogInterface dialog, int whichButton) {
				String str = edtInput.getText().toString();
				if (str.length() > 0) {
					return;
				}
				try {
					int val = Integer.parseInt(str);
					if (val < 60) {
						Toast.makeText((Context) AdvancedChoice.this, (CharSequence) AdvancedChoice.this.getResources().getString(R.string.setting_register_notify), Toast.LENGTH_SHORT).show();
						return;
					}
					Editor edit = AdvancedChoice.this.mSharedPreferences.edit();
					edit.putInt(Settings.PREF_REGTIME_EXPIRES, val);
					edit.commit();
					registertime_summary.setText(new StringBuilder(String.valueOf(val)).append(AdvancedChoice.this.getResources().getString(R.string.second)).toString());
					SipStack.default_expires = val;
					if (Receiver.mSipdroidEngine == null) {
						Receiver.engine(AdvancedChoice.this);
					} else {
						Receiver.mSipdroidEngine.register(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		builder.show();
	}

	private void updateSummary() {
		if (this.mSharedPreferences.getBoolean("logOnOffKey", false)) {
			this.log_ctrl.setChecked(true);
			this.logsummary.setText(R.string.rate_suspension_2);
		} else {
			this.log_ctrl.setChecked(false);
			this.logsummary.setText(R.string.rate_suspension_1);
		}
		if (this.mSharedPreferences.getBoolean("gpsOnOffKey", true)) {
			this.gps_ctrl.setChecked(true);
			this.gpssummary.setText(R.string.rate_suspension_2);
		} else {
			this.gps_ctrl.setChecked(false);
			this.gpssummary.setText(R.string.rate_suspension_1);
		}
		if (this.mSharedPreferences.getBoolean("flowOnOffKey", false)) {
			this.flow_ctrl.setChecked(true);
			this.flow_summary.setText(R.string.rate_suspension_2);
		} else {
			this.flow_ctrl.setChecked(false);
			this.flow_summary.setText(R.string.rate_suspension_1);
		}
		if (this.mSharedPreferences.getBoolean("msg_encrypt", false)) {
			MyLog.e("AdvancedChoice", "PREF_MSG_ENCRYPT true");
			this.encrypt_onoff.setChecked(true);
			this.msgencry_summary.setText(R.string.encryption_2);
		} else {
			MyLog.e("AdvancedChoice", "PREF_MSG_ENCRYPT false");
			this.encrypt_onoff.setChecked(false);
			this.msgencry_summary.setText(R.string.encryption_1);
		}
		if (this.mSharedPreferences.getBoolean("bluetoothonoff", false)) {
			this.bluetooth_onoff.setChecked(true);
			this.bluetooth_summary.setText(R.string.rate_suspension_2);
		} else {
			this.bluetooth_onoff.setChecked(false);
			this.bluetooth_summary.setText(R.string.rate_suspension_1);
		}
		if (this.mSharedPreferences.getBoolean("micwakeuponoff", true)) {
			this.wakeup_swt.setChecked(true);
			this.wakeupsummary.setText(R.string.rate_suspension_2);
		} else {
			this.wakeup_swt.setChecked(false);
			this.wakeupsummary.setText(R.string.rate_suspension_1);
		}
		this.regTime = this.mSharedPreferences.getInt("regtime_expires", 1800);
		this.registertime_summary.setText((CharSequence) (String.valueOf(this.regTime) + this.getResources().getString(R.string.second)));
		if (this.mSharedPreferences.getBoolean("fullwakelock_onoff", true)) {
			this.switchFullWakelock.setChecked(true);
			this.txtFullWakelock.setText(R.string.setting_fullwakeup_lock);
			return;
		}
		this.switchFullWakelock.setChecked(false);
		this.txtFullWakelock.setText(R.string.setting_fullwakeup_unlock);
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			default: {
			}
			case R.id.settings_broadcast: {
				this.startActivity(new Intent((Context) this, (Class) SettingsBroadcastActivity.class));
			}
			case R.id.audio_set: {
				final Intent intent = new Intent();
				intent.setClass((Context) this, (Class) AudioSetActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent);
			}
			case R.id.map_set: {
				// TODO delete @SuppressLint("ApplySharedPref")
				new Builder(this).setSingleChoiceItems(getResources().getStringArray(R.array.maptype), this.mSharedPreferences.getInt("maptype", 0), new DialogInterface.OnClickListener() {
					@SuppressLint("ApplySharedPref")
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								AdvancedChoice.this.mSharedPreferences.edit().putInt("maptype", 0).commit();
								return;
							case 1:
								AdvancedChoice.this.mSharedPreferences.edit().putInt("maptype", 1).commit();
								return;
							default:
								return;
						}
					}
				}).setTitle(R.string.select_maptype).setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.i("AdvancedChoice", "地图类型" + AdvancedChoice.this.maptype);
						dialog.dismiss();
					}
				}).create().show();
				return;
			}
			case R.id.video_set: {
				final Intent intent2 = new Intent();
				intent2.setClass((Context) this, (Class) VideoSelectSettingListActivity.class);
				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent2);
			}
			case R.id.groupcallcoming_set: {
				final Intent intent3 = new Intent();
				intent3.setClass((Context) this, (Class) GroupCallComingSetActivity.class);
				intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent3);
			}
			case R.id.postion_set: {
				final Intent intent4 = new Intent();
				intent4.setClass((Context) this, (Class) GpsSetActivity.class);
				intent4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent4);
			}
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.setting_adchoice);
		this.count = 0;
		this.mSharedPreferences = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		this.languageId = this.mSharedPreferences.getInt("languageId", 0);
		final String[] stringArray = this.getResources().getStringArray(R.array.screen_wakeup_period_List);
		this.screenWakeupPeriodIndex = this.mSharedPreferences.getInt("screen_wakeup_period_index", 0);
		(this.currentScreenWakeupPeriodInfo = (TextView) this.findViewById(R.id.current_screen_wakeup_period_info)).setText((CharSequence) stringArray[this.screenWakeupPeriodIndex]);
		this.flag = this.languageId;
		this.currentLanguage = (TextView) this.findViewById(R.id.currentLanguage);
		switch (this.languageId) {
			case 0: {
				this.currentLanguage.setText(R.string.language_d);
				break;
			}
			case 1: {
				this.currentLanguage.setText(R.string.language_c);
				break;
			}
			case 2: {
				this.currentLanguage.setText(R.string.language_e);
				break;
			}
			case 3: {
				this.currentLanguage.setText(R.string.language_tw);
				break;
			}
		}
		this.language = (LinearLayout) findViewById(R.id.language);
		this.language.setOnClickListener(new C10071());
		this.screenWakeupPeriod = (LinearLayout) findViewById(R.id.screen_wakeup_set);
		this.screenWakeupPeriod.setOnClickListener(new C10092());

		((TextView) this.findViewById(R.id.title)).setText(R.string.advanced_option);
		((ImageButton) this.findViewById(R.id.back)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				AdvancedChoice.this.finish();
			}
		});
		(this.map_set = (LinearLayout) this.findViewById(R.id.map_set)).setOnClickListener((View.OnClickListener) this);
		(this.broadcast_llyt = (LinearLayout) this.findViewById(R.id.settings_broadcast)).setOnClickListener((View.OnClickListener) this);
		this.audio_set = (LinearLayout) this.findViewById(R.id.audio_set);
		if (DeviceInfo.CONFIG_SUPPORT_AUDIO) {
			this.audio_set.setVisibility(View.VISIBLE);
		} else {
			this.audio_set.setVisibility(View.GONE);
		}
		this.audio_set.setOnClickListener((View.OnClickListener) this);
		this.video_set = (LinearLayout) this.findViewById(R.id.video_set);
		if (DeviceInfo.CONFIG_SUPPORT_VIDEO) {
			this.video_set.setVisibility(View.VISIBLE);
		} else {
			this.video_set.setVisibility(View.GONE);
		}
		this.video_set.setOnClickListener((View.OnClickListener) this);
		(this.groupcall_set = (LinearLayout) this.findViewById(R.id.groupcallcoming_set)).setOnClickListener((View.OnClickListener) this);
		(this.position_set = (LinearLayout) this.findViewById(R.id.postion_set)).setOnClickListener((View.OnClickListener) this);
		this.locateModetxt = (TextView) this.findViewById(R.id.locatemodetxt);
		this.encrypt_set = (LinearLayout) this.findViewById(R.id.msgencry_set);
		if (DeviceInfo.ENCRYPT_REMOTE) {
			this.encrypt_set.setVisibility(View.GONE);
			this.findViewById(R.id.msgencry_set_line).setVisibility(View.GONE);
			UdpTransport.needEncrypt = true;
		} else {
			if (!DeviceInfo.CONFIG_SUPPORT_ENCRYPT) {
				this.encrypt_set.setVisibility(View.GONE);
				this.findViewById(R.id.msgencry_set_line).setVisibility(View.GONE);
			} else {
				this.encrypt_set.setOnClickListener((View.OnClickListener) this);
			}
			UdpTransport.needEncrypt = this.mSharedPreferences.getBoolean("msg_encrypt", false);
		}
		(this.bgdate_show = (LinearLayout) this.findViewById(R.id.bgdate_show)).setOnClickListener((View.OnClickListener) this);
		this.flow_set = (LinearLayout) this.findViewById(R.id.flowOnOff);
		if (!DeviceInfo.CONFIG_SUPPORT_RATE_MONITOR) {
			this.flow_set.setVisibility(View.GONE);
			this.findViewById(R.id.flowOnOff_line).setVisibility(View.GONE);
		} else {
			this.flow_set.setOnClickListener((View.OnClickListener) this);
		}
		this.log_set = (LinearLayout) this.findViewById(R.id.logOnOff);
		if (!DeviceInfo.CONFIG_SUPPORT_LOG) {
			this.log_set.setVisibility(View.GONE);
			this.findViewById(R.id.log_onoff_line).setVisibility(View.GONE);
		} else {
			this.log_set.setOnClickListener((View.OnClickListener) this);
		}
		(this.gpsOnOff = (LinearLayout) this.findViewById(R.id.gpsOnOff)).setOnClickListener((View.OnClickListener) this);
		this.log_ctrl = (SwitchButton) this.findViewById(R.id.log_ctrl);
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.settings);
		(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				AdvancedChoice.this.finish();
			}
		});
		this.btn_left.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) AdvancedChoice.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) AdvancedChoice.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						AdvancedChoice.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(AdvancedChoice.this.getResources().getColor(R.color.font_color3));
						AdvancedChoice.this.btn_left.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
		this.gps_ctrl = (SwitchButton) this.findViewById(R.id.gps_ctrl);
		this.logsummary = (TextView) this.findViewById(R.id.logsummary);
		this.gpssummary = (TextView) this.findViewById(R.id.gpssummary);
		this.flow_ctrl = (SwitchButton) this.findViewById(R.id.flow_ctrl);
		this.encrypt_onoff = (SwitchButton) this.findViewById(R.id.encrypt_onoff);
		this.msgencry_summary = (TextView) this.findViewById(R.id.msgencry_summary);
		this.flow_summary = (TextView) this.findViewById(R.id.flowonoff);
		this.registertime_summary = (TextView) this.findViewById(R.id.registertime_summary);
		this.registertime_btn = (LinearLayout) this.findViewById(R.id.registertime_btn);
		if (!DeviceInfo.CONFIG_SUPPORT_REGISTER_INTERNAL) {
			this.registertime_btn.setVisibility(View.GONE);
			this.findViewById(R.id.register_internal).setVisibility(View.GONE);
		} else {
			this.registertime_btn.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					AdvancedChoice.this.regTime = AdvancedChoice.this.mSharedPreferences.getInt("regtime_expires", 1800);
					AdvancedChoice.this.showDialog_Layout();
				}
			});
		}
		this.bluetooth_summary = (TextView) this.findViewById(R.id.bluetooth_summary);
		this.bluetooth_onoff = (SwitchButton) this.findViewById(R.id.bluetooth_onoff);
		this.wakeupsummary = (TextView) this.findViewById(R.id.wakeupsummary);
		this.wakeup_swt = (SwitchButton) this.findViewById(R.id.wakeup_swt);
		this.llytFullWakelock = (LinearLayout) this.findViewById(R.id.settings_llyt_fullwakelock);
		this.switchFullWakelock = (SwitchButton) this.findViewById(R.id.settings_switch_fullwakelock);
		this.txtFullWakelock = (TextView) this.findViewById(R.id.settings_txt_fullwakelock);
		this.llytFullWakelock.setVisibility(View.GONE);
		this.updateSummary();
		this.setOnCheckedChangeListener();
		if (!this.getResources().getConfiguration().locale.getCountry().equals("CN")) {
			this.map_set.setVisibility(View.GONE);
		}
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onResume() {
		this.updateSummary();
		super.onResume();
	}
}
