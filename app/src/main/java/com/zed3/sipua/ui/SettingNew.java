package com.zed3.sipua.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zed3.addressbook.DataBaseService;
import com.zed3.customgroup.CustomGroupUtil;
import com.zed3.flow.FlowRefreshService;
import com.zed3.flow.TotalFlowView;
import com.zed3.location.MemoryMg;
import com.zed3.log.CrashHandler;
import com.zed3.screenhome.BaseActivity;
import com.zed3.settings.AboutActivity;
import com.zed3.settings.AdvancedChoice;
import com.zed3.settings.ChangePasswordActivity;
import com.zed3.settings.PinformationActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.autoUpdate.UpdateVersionService;
import com.zed3.sipua.message.AlarmService;
import com.zed3.sipua.ui.lowsdk.TalkBackNew;
import com.zed3.sipua.ui.splash.SplashActivity;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.SwitchButton;
import com.zed3.utils.Tools;

public class SettingNew extends BaseActivity implements View.OnClickListener {
	private final int CHECKUPDATEOVER;
	LinearLayout autoRunLayout;
	SwitchButton autoacceptbt;
	LinearLayout exit_app_new;
	boolean flag1;
	boolean flag2;
	Handler hd;
	ImageView imgVideoBtn;
	LinearLayout linePinformation;
	LinearLayout lineSuper;
	LinearLayout lineabout;
	LinearLayout lineflow;
	LinearLayout lineupdate;
	Button loginout;
	LinearLayout lytChgPwd;
	SharedPreferences mSharedpreferences;
	SwitchButton mSlipButton;
	SharedPreferences mypre;
	ProgressDialog pd;
	private final String sharedPrefsFile;

	public SettingNew() {
		this.sharedPrefsFile = "com.zed3.sipua_preferences";
		this.flag1 = false;
		this.flag2 = false;
		this.mSlipButton = null;
		this.autoacceptbt = null;
		this.imgVideoBtn = null;
		this.lineSuper = null;
		this.lineflow = null;
		this.lytChgPwd = null;
		this.lineupdate = null;
		this.lineabout = null;
		this.linePinformation = null;
		this.exit_app_new = null;
		this.loginout = null;
		this.mypre = null;
		this.mSharedpreferences = null;
		this.CHECKUPDATEOVER = 1;
		this.hd = new Handler() {
			public void handleMessage(final Message message) {
				super.handleMessage(message);
				switch (message.what) {
					case 1: {
						if (SettingNew.this.pd != null) {
							SettingNew.this.pd.dismiss();
							SettingNew.this.pd = null;
							return;
						}
						break;
					}
				}
			}
		};
	}

	private void InitRadioButton() {
		if (this.mypre.getString("autorunkey", "1").equals("1")) {
			if (this.mSlipButton != null) {
				this.mSlipButton.setChecked(true);
			}
		} else if (this.mSlipButton != null) {
			this.mSlipButton.setChecked(false);
		}
	}

	public void onClick(final View view) {
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.settingnew);
		this.mypre = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		this.mSharedpreferences = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		final LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.acceptauto);
		this.autoRunLayout = (LinearLayout) this.findViewById(R.id.autorun);
		if (DeviceInfo.AUTORUN_REMOTE) {
			this.autoRunLayout.setVisibility(View.GONE);
			this.findViewById(R.id.autorun_line0).setVisibility(View.GONE);
		} else {
			this.autoRunLayout.setVisibility(View.VISIBLE);
			this.findViewById(R.id.autorun_line0).setVisibility(View.VISIBLE);
		}
		this.autoacceptbt = (SwitchButton) this.findViewById(R.id.imgviewbt);
		Log.e("TANGJIAN", "autoAnswerKey4444:" + this.mSharedpreferences.getBoolean("autoAnswerKey", false));
		this.updateSummary();
		this.autoacceptbt.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
				boolean b2 = false;
				Log.e("TANGJIAN", "\u6253\u5f00\u81ea\u52a8\u63a5\u542c\uff01");
				final boolean boolean1 = SettingNew.this.mSharedpreferences.getBoolean("autoAnswerKey", false);
				final SharedPreferences.Editor edit = SettingNew.this.mSharedpreferences.edit();
				if (!boolean1) {
					b2 = true;
				}
				edit.putBoolean("autoAnswerKey", b2);
				edit.commit();
				if (!boolean1) {
					CrashHandler.getInstance().init((Context) SettingNew.this, true);
				} else {
					CrashHandler.EndLog();
				}
				Log.e("TANGJIAN", "updateSummary()");
				SettingNew.this.updateSummary();
			}
		});
		this.mSlipButton = (SwitchButton) this.findViewById(R.id.imgviewbtn);
		if ("1".equals(this.mypre.getString("autorunkey", "0"))) {
			this.mSlipButton.setChecked(true);
		} else {
			this.mSlipButton.setChecked(false);
		}
		this.mSlipButton.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
				final SharedPreferences.Editor edit = SettingNew.this.mypre.edit();
				String s;
				if (b) {
					s = "1";
				} else {
					s = "0";
				}
				edit.putString("autorunkey", s);
				edit.commit();
			}
		});
		this.lineflow = (LinearLayout) this.findViewById(R.id.lineflow);
		if (!DeviceInfo.CONFIG_SUPPORT_UNICOM_FLOWSTATISTICS) {
			this.lineflow.setVisibility(View.GONE);
			((LinearLayout) this.findViewById(R.id.lineflow_underline)).setVisibility(View.GONE);
		}
		this.lineflow.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				if (MemoryMg.getInstance().User_3GTotal > 0.0) {
					SettingNew.this.startActivity(new Intent((Context) SettingNew.this, (Class) TotalFlowView.class));
					return;
				}
				Toast.makeText((Context) SettingNew.this, (CharSequence) SettingNew.this.getResources().getString(R.string.monitoring_notify), Toast.LENGTH_LONG).show();
			}
		});
		(this.lineSuper = (LinearLayout) this.findViewById(R.id.linesuper)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				SettingNew.this.startActivity(new Intent((Context) SettingNew.this, (Class) AdvancedChoice.class));
			}
		});
		this.lineupdate = (LinearLayout) this.findViewById(R.id.lineupdate);
		if (DeviceInfo.CONFIG_CHECK_UPGRADE) {
			this.lineupdate.setVisibility(View.VISIBLE);
			this.lineupdate.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					final String string = SettingNew.this.mypre.getString("server", "");
					if (!TextUtils.isEmpty((CharSequence) string) && string.split("\\.").length == 4) {
						if (SettingNew.this.pd == null) {
							(SettingNew.this.pd = new ProgressDialog((Context) SettingNew.this)).setMessage((CharSequence) SettingNew.this.getResources().getString(R.string.setting_updating));
							SettingNew.this.pd.setCancelable(false);
						}
						if (SettingNew.this.pd != null) {
							SettingNew.this.pd.show();
						}
						new Thread(new Runnable() {
							@Override
							public void run() {
								Looper.prepare();
								new UpdateVersionService((Context) SettingNew.this, string).checkUpdate(true);
								SettingNew.this.hd.sendEmptyMessage(1);
								Looper.loop();
							}
						}).start();
						return;
					}
					MyToast.showToast(true, (Context) SettingNew.this, SettingNew.this.getResources().getString(R.string.ip_wrong));
				}
			});
		} else {
			this.lineupdate.setVisibility(View.GONE);
			this.findViewById(R.id.linedate).setVisibility(View.GONE);
		}
		(this.lineabout = (LinearLayout) this.findViewById(R.id.lineabout)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				SettingNew.this.startActivity(new Intent((Context) SettingNew.this, (Class) AboutActivity.class));
			}
		});
		(this.linePinformation = (LinearLayout) this.findViewById(R.id.lineinformation)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				SettingNew.this.startActivity(new Intent((Context) SettingNew.this, (Class) PinformationActivity.class));
			}
		});
		this.loginout = (Button) this.findViewById(R.id.loginout);
		if (DeviceInfo.CONFIG_SUPPORT_AUTOLOGIN) {
			this.loginout.setVisibility(View.GONE);
		}
		this.loginout.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				final SharedPreferences.Editor edit = SettingNew.this.mSharedpreferences.edit();
				edit.putBoolean("autoAnswerKey", false);
				edit.commit();
				view.setClickable(false);
				Settings.ISFIRST_LOGIN = true;
				DataBaseService.dbService = null;
				if (TalkBackNew.getInstance() != null) {
					TalkBackNew.getInstance().unregisterPttGroupChangedReceiver();
				}
				CustomGroupUtil.getInstance().clearPttGroupInfo();
				Tools.cleanGrpID();
				DeviceInfo.svpnumber = "";
				DeviceInfo.https_port = "";
				DeviceInfo.http_port = "";
				DeviceInfo.defaultrecnum = "";
				final SharedPreferences.Editor edit2 = SettingNew.this.mypre.edit();
				edit2.putString("password", "");
				edit2.commit();
				Tools.onPreLogOut();
				Settings.mUserName = null;
				Settings.mPassword = null;
				SettingNew.this.stopService(new Intent((Context) SettingNew.this, (Class) FlowRefreshService.class));
				Receiver.engine((Context) SettingNew.this).expire(-1);
				Receiver.onText(3, null, 0, 0L);
				SettingNew.this.getSharedPreferences("notifyInfo", 0).edit().clear().commit();
				while (true) {
					try {
						Thread.sleep(800L);
						Receiver.engine((Context) SettingNew.this).halt();
						SettingNew.this.stopService(new Intent((Context) SettingNew.this, (Class) AlarmService.class));
						DeviceInfo.ISAlarmShowing = false;
						Receiver.alarm(0, OneShotAlarm.class);
						Receiver.alarm(0, MyHeartBeatReceiver.class);
						SettingNew.this.sendBroadcast(new Intent("com.zed3.sipua.exitActivity"));
						final Intent intent = new Intent((Context) SettingNew.this, (Class) SplashActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						SettingNew.this.startActivity(intent);
						SettingNew.this.finish();
					} catch (InterruptedException ex) {
						ex.printStackTrace();
						continue;
					}
					break;
				}
			}
		});
		(this.lytChgPwd = (LinearLayout) this.findViewById(R.id.line_change_pwd)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				SettingNew.this.startActivity(new Intent((Context) SettingNew.this.getApplication(), (Class) ChangePasswordActivity.class));
			}
		});
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	public boolean onOptionsItemSelected(final MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case 1: {
				Tools.exitApp((Context) this);
				break;
			}
		}
		return super.onOptionsItemSelected(menuItem);
	}

	public void updateSummary() {
		Log.e("TANGJIAN", "flagA");
		if (this.mSharedpreferences.getBoolean("autoAnswerKey", false)) {
			this.autoacceptbt.setChecked(true);
			Log.e("TANGJIAN", "autoAnswerKey0000:" + this.mSharedpreferences.getBoolean("autoAnswerKey", false));
			return;
		}
		this.autoacceptbt.setChecked(false);
		Log.e("TANGJIAN", "autoAnswerKey1111:" + this.mSharedpreferences.getBoolean("autoAnswerKey", false));
	}
}
