package com.zed3.customgroup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.UserAgent;
import com.zed3.sipua.ui.Receiver;
import com.zed3.utils.LogUtil;

public class CustomGroupDialogActivity extends BaseActivity implements View.OnClickListener {
	private static final String TAG = "CustomGroupDialogActivity";
	private Button btn_cancel;
	private Button btn_ok;
	private EditText grp_name;
	private Context mContext;

	private void initViewsAndListeners() {
		this.grp_name = (EditText) this.findViewById(R.id.custom_grp_name);
		this.btn_ok = (Button) this.findViewById(R.id.custom_grp_ok);
		this.btn_cancel = (Button) this.findViewById(R.id.custom_grp_cancel);
		this.btn_ok.setOnClickListener((View.OnClickListener) this);
		this.btn_cancel.setOnClickListener((View.OnClickListener) this);
	}

	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.custom_grp_ok: {
				final String trim = this.grp_name.getText().toString().trim();
				if (trim == null || trim.equals("")) {
					break;
				}
				final UserAgent getCurUA = Receiver.GetCurUA();
				if (getCurUA == null) {
					break;
				}
				if (getCurUA.getCustomGroupMap().containsKey(trim)) {
					CustomGroupUtil.getInstance().showToast(this.mContext, R.string.name_repeat);
					return;
				}
				final Intent intent = new Intent(this.mContext, (Class) EditGroupMemberActivity.class);
				intent.putExtra("type", "create");
				intent.putExtra("custom_grp_name", trim);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent);
			}
			case R.id.custom_grp_cancel: {
				this.finish();
			}
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		LogUtil.makeLog("CustomGroupDialogActivity", "onCreate()");
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.contact_custom_grp_dialog);
		((CustomGroupDialogActivity) (this.mContext = (Context) this)).initViewsAndListeners();
	}

	protected void onResume() {
		LogUtil.makeLog("CustomGroupDialogActivity", "onResume()");
		this.btn_ok.setTextColor(this.getResources().getColor(R.color.gray));
		this.btn_ok.setEnabled(false);
		this.grp_name.addTextChangedListener((TextWatcher) new TextWatcher() {
			public void afterTextChanged(final Editable editable) {
			}

			public void beforeTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
			}

			public void onTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
				if (charSequence.length() > 0) {
					CustomGroupDialogActivity.this.btn_ok.setTextColor(CustomGroupDialogActivity.this.getResources().getColor(R.color.black));
					CustomGroupDialogActivity.this.btn_ok.setEnabled(true);
					final String string = CustomGroupDialogActivity.this.grp_name.getText().toString();
					final String replaceAll = string.replaceAll("[^( ().#a-zA-Z0-9\\u4e00-\\u9fa5)]", "");
					if (!string.equals(replaceAll)) {
						CustomGroupDialogActivity.this.grp_name.setText((CharSequence) replaceAll);
					}
					CustomGroupDialogActivity.this.grp_name.setSelection(CustomGroupDialogActivity.this.grp_name.length());
					CustomGroupDialogActivity.this.grp_name.length();
					return;
				}
				CustomGroupDialogActivity.this.btn_ok.setTextColor(CustomGroupDialogActivity.this.getResources().getColor(R.color.gray));
				CustomGroupDialogActivity.this.btn_ok.setEnabled(false);
			}
		});
		super.onResume();
	}
}
