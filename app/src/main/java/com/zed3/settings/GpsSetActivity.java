package com.zed3.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.location.MemoryMg;
import com.zed3.log.MyLog;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.utils.Tools;

public class GpsSetActivity extends BaseActivity implements View.OnClickListener {
	private static SharedPreferences mSharedPreferences;
	final int MaxTapTimes;
	final int MaxUseTime;
	long beginTime;
	LinearLayout btn_left;
	int count;
	TextView locatemodetxt;
	LinearLayout postion_set;
	LinearLayout postion_settime;
	LinearLayout postion_uploadtime;
	TextView settimetxt;
	TextView uploadtimetxt;

	public GpsSetActivity() {
		this.beginTime = 0L;
		this.count = 0;
		this.MaxTapTimes = 4;
		this.MaxUseTime = 3500;
	}

	private void commit(final String s, final int n) {
		final SharedPreferences.Editor edit = GpsSetActivity.mSharedPreferences.edit();
		edit.putInt(s, n);
		edit.commit();
	}

	private Dialog createDialog(final int title, final int n) {
		// TODO
		return null;
	}

	private Dialog createSetTimeDialog(final int n) {
		return (Dialog) new AlertDialog.Builder((Context) this).setTitle(R.string.setting_position_7).setSingleChoiceItems(R.array.gps_txt_settime, n, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialogInterface, final int gpsSetTimeModel) {
				if (Tools.getCurrentGpsMode() != 3) {
					Receiver.GetCurUA().GPSCloseLock();
				}
				MemoryMg.getInstance().GpsSetTimeModel = gpsSetTimeModel;
				GpsSetActivity.this.commit("locateSetTime", gpsSetTimeModel);
				GpsSetActivity.this.updateSunmary();
				MyLog.i("GpsSetActivity", "whichButton is:" + gpsSetTimeModel);
				dialogInterface.dismiss();
				if (Tools.getCurrentGpsMode() != 3) {
					Receiver.GetCurUA().GPSOpenLock();
				}
			}
		}).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialogInterface, final int n) {
			}
		}).create();
	}

	private Dialog createUploadTimeDialog(final int n) {
		return (Dialog) new AlertDialog.Builder((Context) this).setTitle(R.string.setting_position_8).setSingleChoiceItems(R.array.gps_txt_settime, n, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialogInterface, final int gpsUploadTimeModel) {
				if (Tools.getCurrentGpsMode() != 3) {
					Receiver.GetCurUA().GPSCloseLock();
				}
				MemoryMg.getInstance().GpsUploadTimeModel = gpsUploadTimeModel;
				GpsSetActivity.this.commit("locateUploadTime", gpsUploadTimeModel);
				GpsSetActivity.this.updateSunmary();
				MyLog.i("GpsSetActivity", "whichButton is:" + gpsUploadTimeModel);
				dialogInterface.dismiss();
				if (Tools.getCurrentGpsMode() != 3) {
					Receiver.GetCurUA().GPSOpenLock();
				}
			}
		}).setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialogInterface, final int n) {
			}
		}).create();
	}

	private int findWhich(final String s, final int n) {
		return GpsSetActivity.mSharedPreferences.getInt(s, n);
	}

	private void updateSunmary() {
		final int currentGpsMode = Tools.getCurrentGpsMode();
		MyLog.i("ddupdateSunmary", "mode=" + currentGpsMode);
		if (currentGpsMode == 0) {
			this.locatemodetxt.setText(R.string.setting_position_3);
		} else if (currentGpsMode == 1) {
			this.locatemodetxt.setText(R.string.setting_position_4);
		} else if (currentGpsMode == 2) {
			this.locatemodetxt.setText(R.string.setting_position_5);
		} else if (currentGpsMode == 3) {
			this.locatemodetxt.setText(R.string.setting_position_6);
		} else if (currentGpsMode == 4) {
			this.locatemodetxt.setText(R.string.setting_position_9);
		}
		final int which = this.findWhich("locateSetTime", 1);
		if (which == 0) {
			this.settimetxt.setText((CharSequence) "5S");
		} else if (which == 1) {
			this.settimetxt.setText((CharSequence) "15S");
		} else if (which == 2) {
			this.settimetxt.setText((CharSequence) "30S");
		} else if (which == 3) {
			this.settimetxt.setText((CharSequence) "80S");
		}
		final int which2 = this.findWhich("locateUploadTime", 1);
		if (which2 == 0) {
			this.uploadtimetxt.setText((CharSequence) "5S");
		} else {
			if (which2 == 1) {
				this.uploadtimetxt.setText((CharSequence) "15S");
				return;
			}
			if (which2 == 2) {
				this.uploadtimetxt.setText((CharSequence) "30S");
				return;
			}
			if (which2 == 3) {
				this.uploadtimetxt.setText((CharSequence) "80S");
			}
		}
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.postion_set: {
				if (DeviceInfo.GPS_REMOTE != 2) {
					Toast.makeText((Context) this, R.string.positioning_mode, Toast.LENGTH_SHORT).show();
					return;
				}
				final int currentGpsMode = Tools.getCurrentGpsMode();
				MyLog.i("gpsmode", "mode!!!!!" + currentGpsMode);
				int n = currentGpsMode;
				// TODO
				break;
			}
			case R.id.postion_settime: {
				this.createSetTimeDialog(this.findWhich("locateSetTime", 1)).show();
			}
			case R.id.postion_uploadtime: {
				this.createUploadTimeDialog(this.findWhich("locateUploadTime", 1)).show();
			}
			case R.id.wholepage: {
				if (this.beginTime == 0L) {
					this.beginTime = SystemClock.uptimeMillis();
				}
				if (SystemClock.uptimeMillis() - this.beginTime > 3500L) {
					this.beginTime = SystemClock.uptimeMillis();
					this.count = 1;
					return;
				}
				if (++this.count == 4) {
					this.startActivity(new Intent((Context) this, (Class) GisDisplayActivity.class));
					return;
				}
				break;
			}
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.setting_gpsset);
		this.findViewById(R.id.wholepage).setOnClickListener((View.OnClickListener) this);
		GpsSetActivity.mSharedPreferences = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		((TextView) this.findViewById(R.id.title)).setText(R.string.setting_position);
		(this.postion_set = (LinearLayout) this.findViewById(R.id.postion_set)).setOnClickListener((View.OnClickListener) this);
		(this.postion_settime = (LinearLayout) this.findViewById(R.id.postion_settime)).setOnClickListener((View.OnClickListener) this);
		(this.postion_uploadtime = (LinearLayout) this.findViewById(R.id.postion_uploadtime)).setOnClickListener((View.OnClickListener) this);
		this.locatemodetxt = (TextView) this.findViewById(R.id.locatemodetxt);
		this.settimetxt = (TextView) this.findViewById(R.id.settimetxt);
		this.uploadtimetxt = (TextView) this.findViewById(R.id.uploadtimetxt);
		this.updateSunmary();
		((TextView) this.findViewById(R.id.t_leftbtn)).setText(R.string.advanced);
		(this.btn_left = (LinearLayout) this.findViewById(R.id.btn_leftbtn)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				GpsSetActivity.this.finish();
			}
		});
		this.btn_left.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) GpsSetActivity.this.findViewById(R.id.t_leftbtn);
				final TextView textView2 = (TextView) GpsSetActivity.this.findViewById(R.id.left_icon);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						GpsSetActivity.this.btn_left.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(GpsSetActivity.this.getResources().getColor(R.color.font_color3));
						GpsSetActivity.this.btn_left.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
	}

	protected void onPause() {
		super.onPause();
		this.beginTime = 0L;
		this.count = 0;
	}
}
