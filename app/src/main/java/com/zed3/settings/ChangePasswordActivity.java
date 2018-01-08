package com.zed3.settings;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.zed3.log.GKFileUtils;
import com.zed3.network.NetStateSingleton;
import com.zed3.screenhome.BaseActivity;
import com.zed3.settings.ChangePasswordVM.ChangePasswordListener;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.welcome.AutoConfigManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends BaseActivity implements ChangePasswordListener {
	private static final String regEx = "[<>&:]";
	private static Pattern pattern = Pattern.compile(regEx);
	private LinearLayout btn_left = null;
	private Button mBtnSave = null;
	private ChangePasswordVM mChangePasswordVM = null;
	private EditText mEditComfirmPwd = null;
	private EditText mEditNewPwd = null;
	private EditText mEditOldPwd = null;
	private String mPwdInPhone = "";
	private String mServerIP = "";
	private int mServerPort = 0;
	private TextView mTextViewBack = null;
	private TextView mTextViewTitle = null;
	private String mUsername = "";

	class C10231 implements OnEditorActionListener {
		C10231() {
		}

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == 5) {
				ChangePasswordActivity.this.mEditNewPwd.requestFocus();
			}
			return false;
		}
	}

	class C10242 implements OnEditorActionListener {
		C10242() {
		}

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == 5) {
				ChangePasswordActivity.this.mEditComfirmPwd.requestFocus();
			}
			return false;
		}
	}

	class C10253 implements OnEditorActionListener {
		C10253() {
		}

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == 6) {
				ChangePasswordActivity.this.mBtnSave.requestFocus();
			}
			return false;
		}
	}

	class C10264 implements TextWatcher {
		C10264() {
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (!ChangePasswordActivity.this.editInputTooLong(s.toString()) && ChangePasswordActivity.this.fliterPassword(s, ChangePasswordActivity.this.mEditOldPwd) && s.toString().length() > 0) {
				ChangePasswordActivity.this.mChangePasswordVM.setOldPassword(s.toString());
				ChangePasswordActivity.this.mBtnSave.setEnabled(true);
			}
		}

		public void afterTextChanged(Editable s) {
			if (s.toString().length() > 0) {
				ChangePasswordActivity.this.mChangePasswordVM.setOldPassword(s.toString());
				ChangePasswordActivity.this.mBtnSave.setEnabled(true);
			}
		}
	}

	class C10275 implements TextWatcher {
		C10275() {
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (!ChangePasswordActivity.this.editInputTooLong(s.toString()) && ChangePasswordActivity.this.fliterPassword(s, ChangePasswordActivity.this.mEditNewPwd) && s.toString().length() > 0) {
				ChangePasswordActivity.this.mChangePasswordVM.setNewPassword(s.toString());
				ChangePasswordActivity.this.mBtnSave.setEnabled(true);
			}
		}

		public void afterTextChanged(Editable s) {
			if (s.toString().length() > 0) {
				ChangePasswordActivity.this.mChangePasswordVM.setNewPassword(s.toString());
				ChangePasswordActivity.this.mBtnSave.setEnabled(true);
			}
		}
	}

	class C10286 implements TextWatcher {
		C10286() {
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (!ChangePasswordActivity.this.editInputTooLong(s.toString()) && ChangePasswordActivity.this.fliterPassword(s, ChangePasswordActivity.this.mEditComfirmPwd) && s.toString().length() > 0) {
				ChangePasswordActivity.this.mChangePasswordVM.setConfirmPassword(s.toString());
				ChangePasswordActivity.this.mBtnSave.setEnabled(true);
			}
		}

		public void afterTextChanged(Editable s) {
			if (s.toString().length() > 0) {
				ChangePasswordActivity.this.mChangePasswordVM.setConfirmPassword(s.toString());
				ChangePasswordActivity.this.mBtnSave.setEnabled(true);
			}
		}
	}

	class C10297 implements OnClickListener {
		C10297() {
		}

		public void onClick(View v) {
			if (!NetStateSingleton.isNetworkAvailable(ChangePasswordActivity.this.getApplicationContext())) {
				Toast.makeText(ChangePasswordActivity.this, "请确认您的网络是否连接！", Toast.LENGTH_LONG).show();
			} else if (ChangePasswordActivity.this.mChangePasswordVM.check()) {
				ChangePasswordActivity.this.mChangePasswordVM.save();
			}
		}
	}

	class C10308 implements OnClickListener {
		C10308() {
		}

		public void onClick(View v) {
			ChangePasswordActivity.this.finish();
		}
	}

	class C10319 implements Runnable {
		private final /* synthetic */ Activity val$activity;
		private final /* synthetic */ String val$str;

		C10319(Activity activity, String str) {
			this.val$activity = activity;
			this.val$str = str;
		}

		public void run() {
			Toast.makeText(this.val$activity, this.val$str, Toast.LENGTH_LONG).show();
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_change_pwd);
		getUsernamePwd();
		this.mChangePasswordVM = new ChangePasswordVM(this.mUsername, this.mPwdInPhone, this.mServerIP, this.mServerPort, this);
		initViews();
		setListeners();
	}

	private void getUsernamePwd() {
		AutoConfigManager acm = new AutoConfigManager(this);
		this.mUsername = acm.fetchLocalUserName();
		this.mPwdInPhone = acm.fetchLocalPwd();
		this.mServerIP = acm.fetchLocalServer();
		this.mServerPort = Integer.valueOf(acm.fetchLocalPort()).intValue();
	}

	private void initViews() {
		this.mTextViewBack = (TextView) findViewById(R.id.t_leftbtn);
		this.mTextViewTitle = (TextView) findViewById(R.id.title);
		this.mEditOldPwd = (EditText) findViewById(R.id.setting_edit_old_pwd);
		this.mEditNewPwd = (EditText) findViewById(R.id.setting_edit_new_pwd);
		this.mEditComfirmPwd = (EditText) findViewById(R.id.setting_edit_confirm_pwd);
		this.mBtnSave = (Button) findViewById(R.id.setting_btn_savepwd);
		this.btn_left = (LinearLayout) findViewById(R.id.btn_leftbtn);
		this.mBtnSave.setEnabled(false);
		this.mTextViewBack.setText(getString(R.string.settings));
		this.mTextViewTitle.setText(getString(R.string.setting_change_pwd));
	}

	private void setListeners() {
		this.mEditOldPwd.setOnEditorActionListener(new C10231());
		this.mEditNewPwd.setOnEditorActionListener(new C10242());
		this.mEditComfirmPwd.setOnEditorActionListener(new C10253());
		this.mEditOldPwd.addTextChangedListener(new C10264());
		this.mEditNewPwd.addTextChangedListener(new C10275());
		this.mEditComfirmPwd.addTextChangedListener(new C10286());
		this.mBtnSave.setOnClickListener(new C10297());
		this.btn_left.setOnClickListener(new C10308());
	}

	private boolean fliterPassword(CharSequence s, EditText editText) {
		if (GKFileUtils.isEmpty(s)) {
			return false;
		}
		String fliter = editInputInvalidChar(s.toString(), this);
		if (fliter == null || fliter.equals(editText.getText().toString())) {
			return false;
		}
		editText.setText(fliter);
		editText.setSelection(editText.length());
		return true;
	}

	public static String editInputInvalidChar(String s, Activity activity) {
		if (GKFileUtils.isEmpty(s) && activity == null) {
			return null;
		}
		Matcher matcher = stringMatcher(s);
		if (!matcher.find()) {
			return s;
		}
		passwordInvalidChar(activity);
		return matcher.replaceAll("");
	}

	private boolean editInputTooLong(String s) {
		if (GKFileUtils.isEmpty(s)) {
			return true;
		}
		if (s.length() <= 21) {
			return false;
		}
		passwordTooLong();
		return true;
	}

	private void logout() {
		closeService();
		((SipUAApp) getApplication()).appLogout();
		reLogin();
	}

	public void passwordIncomplete() {
		toastText(SipUAApp.mContext.getResources().getString(R.string.itemEmpty), this);
	}

	public void twoPasswordNotEqual() {
		toastText(SipUAApp.mContext.getResources().getString(R.string.notEquals), this);
	}

	public void oldPasswordWrong() {
		toastText(SipUAApp.mContext.getResources().getString(R.string.errNum), this);
	}

	public void passwordTooLong() {
		toastText(SipUAApp.mContext.getResources().getString(R.string.tooLong), this);
	}

	public void saveSuccess(String res) {
		toastText(SipUAApp.mContext.getResources().getString(R.string.Success), this);
		logout();
	}

	public void saveFailure(String err) {
		toastText(new StringBuilder(String.valueOf(SipUAApp.mContext.getResources().getString(R.string.Failure))).append(err).toString(), this);
	}

	public void saveTimeout() {
		toastText(SipUAApp.mContext.getResources().getString(R.string.timeOut), this);
	}

	public static void passwordInvalidChar(Activity activity) {
		toastText(SipUAApp.mContext.getResources().getString(R.string.Invalid), activity);
	}

	public static void toastText(String str, Activity activity) {
//		activity.runOnUiThread(new C10319(activity, str));
	}

	public static Matcher stringMatcher(String s) {
		return pattern.matcher(s);
	}
}
