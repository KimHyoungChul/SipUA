package com.zed3.sipua.ui.splash;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.zed3.log.MyLog;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.DES;
import com.zed3.utils.MD5;

public class UnionLogin extends Activity {
	private EditText editTxt;
	private boolean isError;
	private Button loginBtn;
	BroadcastReceiver loginReceiver;
	SharedPreferences mypre;
	private final String sharedPrefsFile;
	UserAgent ua;

	public UnionLogin() {
		this.sharedPrefsFile = "com.zed3.sipua_preferences";
		this.loginBtn = null;
		this.editTxt = null;
		this.ua = null;
		this.mypre = null;
		this.loginReceiver = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				if (!intent.getBooleanExtra("loginstatus", false)) {
					Receiver.engine((Context) UnionLogin.this).halt();
					new AlertDialog.Builder((Context) UnionLogin.this).setTitle(R.string.information).setMessage((CharSequence) UnionLogin.this.getResources().getString(R.string.the_wrong_pwd)).setPositiveButton((CharSequence) UnionLogin.this.getResources().getString(R.string.ok), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialogInterface, final int n) {
							UnionLogin.this.finish();
						}
					}).create().show();
					return;
				}
				if (UnionLogin.this.isError) {
					UnionLogin.this.ua = Receiver.GetCurUA();
					UnionLogin.this.mypre = UnionLogin.this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
					UnionLogin.this.CalActivity(UnionLogin.this.editTxt.getText().toString());
					return;
				}
				UnionLogin.this.ua = Receiver.GetCurUA();
				UnionLogin.this.mypre = UnionLogin.this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
				UnionLogin.this.loginBtn.setEnabled(true);
				if (!UnionLogin.this.mypre.getString("unionpassword", "0").equals("0")) {
					UnionLogin.this.ua.UploadUnionPwd(UnionLogin.this.mypre.getString("unionpassword", "0"));
					UnionLogin.this.startActivity(new Intent((Context) UnionLogin.this, (Class) MainActivity.class));
					UnionLogin.this.finish();
					return;
				}
				UnionLogin.this.CalActivity(UnionLogin.this.editTxt.getText().toString());
			}
		};
		this.isError = false;
	}

	private void CalActivity(String encryptDES) {
		try {
			final String md5 = MD5.toMd5(this.fetchUserName());
			final String string = String.valueOf(md5.substring(0, 4)) + md5.substring(28);
			encryptDES = DES.encryptDES(encryptDES, string);
			MyLog.i("unionlogin", "md5:" + md5 + " key:" + string + " DesBase64:" + encryptDES);
			this.ua.UploadUnionPwd(encryptDES);
			if (this.mypre != null) {
				final SharedPreferences.Editor edit = this.mypre.edit();
				edit.putString("unionpassword", encryptDES);
				edit.commit();
			}
			this.startActivity(new Intent((Context) this, (Class) MainActivity.class));
			this.finish();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void CheckLogin() {
		this.ua = Receiver.GetCurUA();
		this.mypre = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
		if (!this.mypre.getString("unionpassword", "0").equals("0")) {
			this.ua.UploadUnionPwd(this.mypre.getString("unionpassword", "0"));
			this.startActivity(new Intent((Context) this, (Class) MainActivity.class));
			this.finish();
		}
	}

	private String fetchUserName() {
		return this.getSharedPreferences("ServerSet", 0).getString("UserName", "");
	}

	protected void onCreate(Bundle extras) {
		super.onCreate(extras);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		extras = this.getIntent().getExtras();
		if (extras != null) {
			this.isError = extras.getBoolean("unionepwderror");
		}
		this.registerReceiver(this.loginReceiver, new IntentFilter("com.zed3.sipua.login"));
		if (!this.isError) {
			if (Receiver.mSipdroidEngine != null && Receiver.mSipdroidEngine.isRegistered(true)) {
				this.CheckLogin();
			} else {
				this.mypre = this.getSharedPreferences("com.zed3.sipua_preferences", Context.MODE_PRIVATE);
				if (!this.mypre.getString("unionpassword", "0").equals("0")) {
					(this.ua = Receiver.GetCurUA()).UploadUnionPwd(this.mypre.getString("unionpassword", "0"));
					this.startActivity(new Intent((Context) this, (Class) MainActivity.class));
					this.finish();
				}
			}
		}
		this.setContentView(R.layout.unionlogin);
		(this.loginBtn = (Button) this.findViewById(R.id.loginbtn)).setEnabled(true);
		this.loginBtn.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				if (UnionLogin.this.editTxt.getText().toString().length() < 1) {
					return;
				}
				if (Receiver.mSipdroidEngine == null) {
					Receiver.engine((Context) UnionLogin.this);
				} else {
					Receiver.mSipdroidEngine.StartEngine();
				}
				UnionLogin.this.loginBtn.setEnabled(false);
			}
		});
		this.editTxt = (EditText) this.findViewById(R.id.editTxt);
	}

	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(this.loginReceiver);
	}

	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		return super.onKeyDown(n, keyEvent);
	}
}
