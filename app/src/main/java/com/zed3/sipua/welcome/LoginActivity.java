package com.zed3.sipua.welcome;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.addressbook.DataBaseService;
import com.zed3.net.util.NetChecker;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;

import java.lang.ref.WeakReference;

public class LoginActivity extends BaseActivity implements IAutoConfigListener {
	AutoConfigManager acm;
	boolean beginNetState;
	BroadcastReceiver br;
	String callkey;
	Handler charHandler;
	CheckBox chkbtn;
	int cursorIndex;
	DataBaseService dbService;
	EditText[] dd;
	EditText et_port;
	EditText et_pwd;
	EditText et_userName;
	EditText first;
	EditText forth;
	Handler hd;
	LinearLayout ll;
	BroadcastReceiver loginReceiver;
	private View mRootView;
	String monitorkey;
	ProgressDialog pd;
	String pix;
	EditText second;
	Thread t_fetchInfo;
	String text;
	EditText third;
	String upLoadkey;
	SharedPreferences vgaShare;
	boolean vgas;
	private String video_720p_frame;
	private String video_720p_fre;
	private int video_call_720p_netrate;
	private int video_call_qvga_netrate;
	private int video_call_vga_netrate;
	private int video_monitor_720p_netrate;
	private int video_monitor_qvga_netrate;
	private int video_monitor_vga_netrate;
	private String video_qvga_frame;
	private String video_qvga_fre;
	private int video_upload_720p_netrate;
	private int video_upload_qvga_netrate;
	private int video_upload_vga_netrate;
	private String video_vga_frame;
	private String video_vga_fre;

	public LoginActivity() {
		this.pd = null;
		this.beginNetState = true;
		this.dd = new EditText[]{this.first, this.second, this.third, this.forth};
		this.cursorIndex = 0;
		this.dbService = DataBaseService.getInstance();
		this.ll = null;
		this.chkbtn = null;
		this.video_call_qvga_netrate = 0;
		this.video_upload_qvga_netrate = 0;
		this.video_upload_vga_netrate = 0;
		this.video_upload_720p_netrate = 0;
		this.video_monitor_qvga_netrate = 0;
		this.video_monitor_vga_netrate = 0;
		this.video_monitor_720p_netrate = 0;
		this.video_qvga_fre = null;
		this.video_qvga_frame = null;
		this.video_vga_fre = null;
		this.video_vga_frame = null;
		this.video_720p_fre = null;
		this.video_720p_frame = null;
		this.pix = null;
		this.callkey = null;
		this.upLoadkey = null;
		this.monitorkey = null;
		this.vgaShare = null;
		this.loginReceiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				if (intent.getBooleanExtra("loginstatus", false)) {
					final Intent intent2 = new Intent();
					intent2.setClass((Context) LoginActivity.this, (Class) MainActivity.class);
					LoginActivity.this.startActivity(intent2);
					LoginActivity.this.finish();
					return;
				}
				final Message message = new Message();
				message.what = 0;
				final Bundle data = new Bundle();
				data.putString("result", intent.getStringExtra("result"));
				message.setData(data);
				LoginActivity.this.hd.sendMessage(message);
			}
		};
		this.charHandler = new MyHandler(this);
		this.hd = new LoginHandler(this);
		this.br = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				final boolean check = NetChecker.check((Context) LoginActivity.this, false);
				if (LoginActivity.this.beginNetState != check && !LoginActivity.this.beginNetState) {
					LoginActivity.this.login(LoginActivity.this.mRootView);
				}
				LoginActivity.this.beginNetState = check;
			}
		};
	}

	private void getSPVideoSize() {
		final String[] split = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("com.zed3.action.VIDEO_CALL.320*240", "2,20,1600").split(",");
		final String[] split2 = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("com.zed3.action.VIDEO_CALL.640*480", "1,10,4000").split(",");
		final String[] split3 = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("com.zed3.action.VIDEO_CALL.1080*720", "1,10,6400").split(",");
		final String[] split4 = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("com.zed3.action.VIDEO_UPLOAD.320*240", "2,20,1600").split(",");
		final String[] split5 = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("com.zed3.action.VIDEO_UPLOAD.640*480", "1,10,4000").split(",");
		final String[] split6 = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("com.zed3.action.VIDEO_UPLOAD.1080*720", "1,10,6400").split(",");
		final String[] split7 = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("com.zed3.action.VIDEO_MONITOR.320*240", "2,20,1600").split(",");
		final String[] split8 = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("com.zed3.action.VIDEO_MONITOR.640*480", "1,10,4000").split(",");
		final String[] split9 = PreferenceManager.getDefaultSharedPreferences((Context) this).getString("com.zed3.action.VIDEO_MONITOR.1080*720", "1,10,6400").split(",");
		if (split.length == 3) {
			this.video_call_qvga_netrate = Integer.parseInt(split[2]);
			this.video_qvga_fre = split[0];
			this.video_qvga_frame = split[1];
		}
		if (split2.length == 3) {
			this.video_call_vga_netrate = Integer.parseInt(split2[2]);
			this.video_vga_fre = split2[0];
			this.video_vga_frame = split2[1];
		}
		if (split3.length == 3) {
			this.video_call_720p_netrate = Integer.parseInt(split3[2]);
			this.video_720p_fre = split3[0];
			this.video_720p_frame = split3[1];
		}
		if (split4.length == 3) {
			this.video_upload_qvga_netrate = Integer.parseInt(split4[2]);
		}
		if (split5.length == 3) {
			this.video_upload_vga_netrate = Integer.parseInt(split5[2]);
		}
		if (split6.length == 3) {
			this.video_upload_720p_netrate = Integer.parseInt(split6[2]);
		}
		if (split7.length == 3) {
			this.video_monitor_qvga_netrate = Integer.parseInt(split7[2]);
		}
		if (split8.length == 3) {
			this.video_monitor_vga_netrate = Integer.parseInt(split8[2]);
		}
		if (split9.length == 3) {
			this.video_monitor_720p_netrate = Integer.parseInt(split9[2]);
		}
		this.judgeNetrate();
	}

	private void isVideoNetrateChanged() {
		final SharedPreferences.Editor edit = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE).edit();
		if (this.video_call_qvga_netrate < 400 || this.video_call_qvga_netrate > 32000) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				edit.putString("com.zed3.action.VIDEO_CALL.320*240", String.valueOf(this.video_qvga_fre) + "," + this.video_qvga_frame + "," + "2400");
			} else {
				edit.putString("com.zed3.action.VIDEO_CALL.320*240", String.valueOf(this.video_qvga_fre) + "," + this.video_qvga_frame + "," + "1600");
				Log.e("TANGJIAN", "1600");
			}
			edit.commit();
		}
		if (this.video_upload_qvga_netrate < 400 || this.video_upload_qvga_netrate > 32000) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				edit.putString("com.zed3.action.VIDEO_UPLOAD.320*240", String.valueOf(this.video_qvga_fre) + "," + this.video_qvga_frame + "," + "2400");
			} else {
				edit.putString("com.zed3.action.VIDEO_UPLOAD.320*240", String.valueOf(this.video_qvga_fre) + "," + this.video_qvga_frame + "," + "1600");
			}
			edit.commit();
		}
		if (this.video_monitor_qvga_netrate < 400 || this.video_monitor_qvga_netrate > 32000) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				edit.putString("com.zed3.action.VIDEO_MONITOR.320*240", String.valueOf(this.video_qvga_fre) + "," + this.video_qvga_frame + "," + "2400");
			} else {
				edit.putString("com.zed3.action.VIDEO_MONITOR.320*240", String.valueOf(this.video_qvga_fre) + "," + this.video_qvga_frame + "," + "1600");
			}
			edit.commit();
		}
		if (this.video_call_vga_netrate < 400 || this.video_call_vga_netrate > 32000) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				edit.putString("com.zed3.action.VIDEO_CALL.640*480", String.valueOf(this.video_vga_fre) + "," + this.video_vga_frame + "," + "6400");
			} else {
				edit.putString("com.zed3.action.VIDEO_CALL.640*480", String.valueOf(this.video_vga_fre) + "," + this.video_vga_frame + "," + "4000");
				Log.e("TANGJIAN", "4000");
			}
			edit.commit();
		}
		if (this.video_upload_vga_netrate < 400 || this.video_upload_vga_netrate > 32000) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				edit.putString("com.zed3.action.VIDEO_UPLOAD.640*480", String.valueOf(this.video_vga_fre) + "," + this.video_vga_frame + "," + "6400");
			} else {
				edit.putString("com.zed3.action.VIDEO_UPLOAD.640*480", String.valueOf(this.video_vga_fre) + "," + this.video_qvga_frame + "," + "4000");
			}
			edit.commit();
		}
		if (this.video_monitor_vga_netrate < 400 || this.video_monitor_vga_netrate > 32000) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				edit.putString("com.zed3.action.VIDEO_MONITOR.640*480", String.valueOf(this.video_vga_fre) + "," + this.video_vga_frame + "," + "6400");
			} else {
				edit.putString("com.zed3.action.VIDEO_MONITOR.640*480", String.valueOf(this.video_vga_fre) + "," + this.video_vga_frame + "," + "4000");
			}
			edit.commit();
		}
		if (this.video_call_720p_netrate < 400 || this.video_call_720p_netrate > 32000) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				edit.putString("com.zed3.action.VIDEO_CALL.1080*720", String.valueOf(this.video_720p_fre) + "," + this.video_720p_frame + "," + "16000");
			} else {
				edit.putString("com.zed3.action.VIDEO_CALL.1080*720", String.valueOf(this.video_720p_fre) + "," + this.video_720p_frame + "," + "6400");
				Log.e("TANGJIAN", "6400");
			}
			edit.commit();
		}
		if (this.video_upload_720p_netrate < 400 || this.video_upload_720p_netrate > 32000) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				edit.putString("com.zed3.action.VIDEO_UPLOAD.1080*720", String.valueOf(this.video_720p_fre) + "," + this.video_720p_frame + "," + "16000");
			} else {
				edit.putString("com.zed3.action.VIDEO_UPLOAD.1080*720", String.valueOf(this.video_720p_fre) + "," + this.video_720p_frame + "," + "6400");
			}
			edit.commit();
		}
		if (this.video_monitor_720p_netrate < 400 || this.video_monitor_720p_netrate > 32000) {
			if (Build.MODEL.toLowerCase().contains("datang")) {
				edit.putString("com.zed3.action.VIDEO_MONITOR.1080*720", String.valueOf(this.video_720p_fre) + "," + this.video_720p_frame + "," + "16000");
			} else {
				edit.putString("com.zed3.action.VIDEO_MONITOR.1080*720", String.valueOf(this.video_720p_fre) + "," + this.video_720p_frame + "," + "6400");
			}
			edit.commit();
		}
		Log.e("TANGJIAN", "commit=============");
	}

	private void judgeNetrate() {
		if (this.vgas) {
			if (this.video_call_vga_netrate == 500) {
				this.video_call_vga_netrate *= 8;
			}
			if (this.video_call_720p_netrate == 800) {
				this.video_call_720p_netrate *= 8;
			}
			if (this.video_upload_vga_netrate == 500) {
				this.video_upload_vga_netrate *= 8;
			}
			if (this.video_upload_720p_netrate == 800) {
				this.video_upload_720p_netrate *= 8;
			}
			if (this.video_monitor_vga_netrate == 500) {
				this.video_monitor_vga_netrate *= 8;
			}
			if (this.video_monitor_720p_netrate == 800) {
				this.video_monitor_720p_netrate *= 8;
			}
		}
		this.vgaShare.edit().putBoolean("vgas", true).commit();
	}

	private void login() {
		if (Receiver.mSipdroidEngine == null) {
			Receiver.engine((Context) this);
			return;
		}
		if (!Receiver.mSipdroidEngine.isRegistered(true)) {
			Receiver.mSipdroidEngine.StartEngine();
			return;
		}
		this.finish();
	}

	private String packetIp() {
		String string = "";
		for (int i = 0; i < 4; ++i) {
			string = String.valueOf(string) + this.dd[i].getText().toString() + ".";
		}
		return string.substring(0, string.length() - 1);
	}

	private void setIp(final String s) {
		if (!TextUtils.isEmpty((CharSequence) s.replace(".", ""))) {
			final String[] split = s.split("\\.");
			if (split.length == 4) {
				for (int i = 0; i < 4; ++i) {
					this.dd[i].setText((CharSequence) split[i]);
				}
			}
		}
	}

	private void showversion() {
		final TextView textView = (TextView) this.findViewById(R.id.versionname);
		new AutoConfigManager((Context) this);
		final String manual_CONFIG_URL = DeviceInfo.MANUAL_CONFIG_URL;
		Log.v("huangfujianurl", "AAAAA===" + manual_CONFIG_URL);
		// TODO
	}

	@Override
	public void AccountDisabled() {
		final Message message = new Message();
		message.what = 0;
		final Bundle data = new Bundle();
		data.putString("result", "AccountDisabled");
		message.setData(data);
		this.hd.sendMessage(message);
	}

	@Override
	public void FetchConfigFailed() {
		final Message message = new Message();
		message.what = 0;
		final Bundle data = new Bundle();
		data.putString("result", "FetchConfigFailed");
		message.setData(data);
		this.hd.sendMessage(message);
	}

	@Override
	public void ParseConfigOK() {
		this.login();
	}

	@Override
	public void TimeOut() {
		final Message message = new Message();
		message.what = 0;
		final Bundle data = new Bundle();
		data.putString("result", "connectTimeOut");
		message.setData(data);
		this.hd.sendMessage(message);
	}

	public boolean checkEditText(final EditText editText) {
		boolean b = true;
		if (editText.getText().length() < 1) {
			b = false;
		}
		return b;
	}

	public void login(final View view) {
		this.isVideoNetrateChanged();
		if (!this.checkEditText(this.et_userName)) {
			MyToast.showToast(true, (Context) this, this.getResources().getString(R.string.userName_is_blank));
			return;
		}
		final String string = this.et_userName.getText().toString();
		this.acm.saveUsername(string);
		if (!this.checkEditText(this.et_pwd)) {
			MyToast.showToast(true, (Context) this, this.getResources().getString(R.string.pwd_is_blank));
			return;
		}
		final String string2 = this.et_pwd.getText().toString();
		final String packetIp = this.packetIp();
		if (packetIp.isEmpty() || packetIp.split("\\.").length != 4) {
			MyToast.showToast(true, (Context) this, this.getResources().getString(R.string.server_ip_wrong));
			return;
		}
		if (!this.acm.fetchLocalServer().equals(packetIp)) {
			this.dbService.insertAlVersion("0");
		}
		this.acm.saveSetting(string, string2, packetIp);
		if (NetChecker.check((Context) this, false)) {
			this.acm.setOnFetchListener(this);
			if (this.pd == null) {
				(this.pd = new ProgressDialog((Context) this)).setMessage((CharSequence) this.getResources().getString(R.string.loginning));
				this.pd.show();
				this.pd.setOnCancelListener((DialogInterface.OnCancelListener) new DialogInterface.OnCancelListener() {
					public void onCancel(final DialogInterface dialogInterface) {
						Tools.exitApp((Context) LoginActivity.this);
					}
				});
				this.pd.setCanceledOnTouchOutside(false);
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					LoginActivity.this.acm.getConfig();
				}
			}).start();
			return;
		}
		final Message message = new Message();
		message.what = 0;
		final Bundle data = new Bundle();
		data.putString("result", "netbroken");
		message.setData(data);
		this.hd.sendMessage(message);
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(this.mRootView = this.getLayoutInflater().inflate(R.layout.login, (ViewGroup) null));
		this.vgaShare = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		this.vgas = this.vgaShare.getBoolean("vgas", false);
		this.getSPVideoSize();
		this.dd[0] = (EditText) this.findViewById(R.id.first);
		this.dd[1] = (EditText) this.findViewById(R.id.second);
		this.dd[2] = (EditText) this.findViewById(R.id.third);
		this.dd[3] = (EditText) this.findViewById(R.id.forth);
		this.dd[0].setOnKeyListener((View.OnKeyListener) new MyOnKeyListener(0));
		this.dd[1].setOnKeyListener((View.OnKeyListener) new MyOnKeyListener(1));
		this.dd[2].setOnKeyListener((View.OnKeyListener) new MyOnKeyListener(2));
		this.dd[3].setOnKeyListener((View.OnKeyListener) new MyOnKeyListener(3));
		this.dd[0].addTextChangedListener((TextWatcher) new MyTextWatch(0));
		this.dd[1].addTextChangedListener((TextWatcher) new MyTextWatch(1));
		this.dd[2].addTextChangedListener((TextWatcher) new MyTextWatch(2));
		this.dd[3].addTextChangedListener((TextWatcher) new MyTextWatch(3));
		this.acm = new AutoConfigManager((Context) this);
		(this.et_pwd = (EditText) this.findViewById(R.id.pwd)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
		(this.et_userName = (EditText) this.findViewById(R.id.username)).requestFocus();
		this.et_userName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
		this.et_userName.addTextChangedListener((TextWatcher) new TextWatcher() {
			public void afterTextChanged(final Editable editable) {
			}

			public void beforeTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
			}

			public void onTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
			}
		});
		this.et_port = (EditText) this.findViewById(R.id.port);
		this.ll = (LinearLayout) this.findViewById(R.id.advancedPannel);
		(this.chkbtn = (CheckBox) this.findViewById(R.id.chkbtn)).setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
				if (b) {
					LoginActivity.this.ll.setVisibility(View.VISIBLE);
					LoginActivity.this.ll.requestFocus();
					return;
				}
				LoginActivity.this.ll.setVisibility(View.GONE);
			}
		});
		final String fetchLocalUserName = this.acm.fetchLocalUserName();
		final String fetchLocalPwd = this.acm.fetchLocalPwd();
		this.et_userName.setText((CharSequence) fetchLocalUserName);
		this.et_pwd.setText((CharSequence) this.acm.fetchLocalPwd());
		this.et_port.setText((CharSequence) this.acm.fetchLocalPort());
		this.setIp(this.acm.fetchLocalCmsServer());
		this.registerReceiver(this.loginReceiver, new IntentFilter("com.zed3.sipua.login"));
		this.beginNetState = NetChecker.check((Context) this, false);
		if (!TextUtils.isEmpty((CharSequence) fetchLocalUserName) && !TextUtils.isEmpty((CharSequence) fetchLocalPwd)) {
			this.login(this.mRootView);
		}
		this.registerReceiver(this.br, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		final int config_LOCAL_LOCATE_MODE = DeviceInfo.CONFIG_LOCAL_LOCATE_MODE;
		this.showversion();
	}

	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(this.loginReceiver);
		if (this.pd != null) {
			this.pd.dismiss();
			this.pd = null;
		}
		this.unregisterReceiver(this.br);
	}

	protected void onResume() {
		this.chkbtn.setText(R.string.server_ip);
		super.onResume();
	}

	@Override
	public void parseFailed() {
		final Message message = new Message();
		message.what = 0;
		final Bundle data = new Bundle();
		data.putString("result", "parseFailed");
		message.setData(data);
		this.hd.sendMessage(message);
	}

	public void save(final View view) {
	}

	public void showIp(final View view) {
		Toast.makeText((Context) this, (CharSequence) this.packetIp(), Toast.LENGTH_SHORT).show();
	}

	public void sipLoginSuccess() {
	}

	void toStep(final int n) {
		this.dd[n].requestFocus();
	}

	private static class LoginHandler extends Handler {
		WeakReference<LoginActivity> mActivity;

		public LoginHandler(final LoginActivity loginActivity) {
			this.mActivity = new WeakReference<LoginActivity>(loginActivity);
		}

		public void handleMessage(final Message message) {
			super.handleMessage(message);
			switch (message.what) {
				default: {
				}
				case 0: {
					if (this.mActivity.get().pd != null) {
						this.mActivity.get().pd.dismiss();
						this.mActivity.get().pd = null;
					}
					final String string = message.getData().getString("result");
					if (!"netbroken".equalsIgnoreCase(string) && !"FetchConfigFailed".equalsIgnoreCase(string) && !"connectTimeOut".equalsIgnoreCase(string) && !"AccountDisabled".equalsIgnoreCase(string) && !"parseFailed".equalsIgnoreCase(string)) {
						Receiver.engine(this.mActivity.get()).halt();
					}
					String s;
					if (string.contains("passwdIncorrect")) {
						s = SipUAApp.mContext.getResources().getString(R.string.wrong_password);
					} else if (string.contains("userNotExist")) {
						s = SipUAApp.mContext.getResources().getString(R.string.account_not_exist);
					} else if (string.contains("Timeout")) {
						s = SipUAApp.mContext.getResources().getString(R.string.timeout);
					} else if (string.contains("netbroken")) {
						s = SipUAApp.mContext.getResources().getString(R.string.network_exception);
					} else if (string.contains("userOrpwderror")) {
						s = SipUAApp.mContext.getResources().getString(R.string.wrong_name_or_pwd);
					} else if (string.contains("versionTooLow")) {
						s = SipUAApp.mContext.getResources().getString(R.string.version_too_low);
					} else if (string.contains("Already Login")) {
						s = SipUAApp.mContext.getResources().getString(R.string.logged_in_another_place);
					} else if (string.contains("Temporarily Unavailable")) {
						s = SipUAApp.mContext.getResources().getString(R.string.service_unavailable);
					} else if (string.contains("AccountDisabled")) {
						s = SipUAApp.mContext.getResources().getString(R.string.account_disabled);
					} else if (string.contains("FetchConfigFailed")) {
						s = SipUAApp.mContext.getResources().getString(R.string.account_not_exist);
					} else if (string.contains("connectTimeOut")) {
						s = SipUAApp.mContext.getResources().getString(R.string.netvork_connecttion_timeout);
					} else {
						s = string;
						if (string.contains("parseFailed")) {
							s = SipUAApp.mContext.getResources().getString(R.string.error_parse);
						}
					}
					MyToast.showToast(true, this.mActivity.get(), s);
				}
			}
		}
	}

	private static class MyHandler extends Handler {
		WeakReference<LoginActivity> mActivity;

		public MyHandler(final LoginActivity loginActivity) {
			this.mActivity = new WeakReference<LoginActivity>(loginActivity);
		}

		public void handleMessage(final Message message) {
			super.handleMessage(message);
			final EditText editText = this.mActivity.get().dd[message.what];
			editText.setText((CharSequence) editText.getText().toString().substring(0, editText.length() - 1));
		}
	}

	public class MyOnKeyListener implements View.OnKeyListener {
		int editNum;

		public MyOnKeyListener(final int editNum) {
			this.editNum = -1;
			this.editNum = editNum;
		}

		public boolean onKey(final View view, final int n, final KeyEvent keyEvent) {
			if (67 == n && LoginActivity.this.dd[this.editNum].length() == 0 && this.editNum > 0) {
				LoginActivity.this.toStep(this.editNum - 1);
			}
			return false;
		}
	}

	public class MyTextWatch implements TextWatcher {
		int editNum;

		public MyTextWatch(final int editNum) {
			this.editNum = editNum;
		}

		public void afterTextChanged(final Editable editable) {
		}

		public void beforeTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
		}

		public void onTextChanged(final CharSequence charSequence, int length, final int n, final int n2) {
			LoginActivity.this.text = charSequence.toString();
			length = LoginActivity.this.text.length();
			if (length > 0 && String.valueOf(LoginActivity.this.text.charAt(length - 1)).equals(".")) {
				if (length == 1) {
					LoginActivity.this.charHandler.sendEmptyMessage(this.editNum);
				} else if (this.editNum < 3) {
					LoginActivity.this.toStep(this.editNum + 1);
					LoginActivity.this.charHandler.sendEmptyMessage(this.editNum);
				} else if (this.editNum == 3) {
					LoginActivity.this.charHandler.sendEmptyMessage(this.editNum);
				}
			}
			LoginActivity.this.text = LoginActivity.this.text.replace(".", "");
			if (LoginActivity.this.text.length() > 3 || (!TextUtils.isEmpty((CharSequence) LoginActivity.this.text) && Integer.parseInt(LoginActivity.this.text) > 255)) {
				if (this.editNum < 3) {
					LoginActivity.this.toStep(this.editNum + 1);
				}
				LoginActivity.this.charHandler.sendEmptyMessage(this.editNum);
			} else if (LoginActivity.this.text.length() == 3) {
				if (this.editNum < 3) {
					LoginActivity.this.toStep(this.editNum + 1);
					return;
				}
				LoginActivity.this.toStep(this.editNum);
			}
		}
	}
}
