package com.zed3.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.zed3.sipua.R;
import com.zed3.utils.DialogMessageTool;

public class CustomDialog {
	private Button mBtn_cancel;
	private Button mBtn_ok;
	private Context mContext;
	private AlertDialog mDialog;
	private TextView mMessage;
	private TextView mTitle;
	private Window window;

	public CustomDialog(final Context mContext) {
		this.mContext = mContext;
		(this.mDialog = new AlertDialog.Builder(mContext).create()).show();
		(this.window = this.mDialog.getWindow()).setContentView(R.layout.dialog_item);
		this.mTitle = (TextView) this.window.findViewById(R.id.title);
		this.mMessage = (TextView) this.window.findViewById(R.id.message);
		this.mBtn_ok = (Button) this.window.findViewById(R.id.ok);
		this.mBtn_cancel = (Button) this.window.findViewById(R.id.cancel);
	}

	public void dismiss() {
		this.mDialog.dismiss();
	}

	public void setCancelable(final boolean cancelable) {
		this.mDialog.setCancelable(cancelable);
	}

	public void setMessage(String string) {
		string = DialogMessageTool.getString((int) (this.mContext.getResources().getDisplayMetrics().density * 284.0f + 0.5f), this.mMessage.getTextSize(), string);
		this.mMessage.setText((CharSequence) string);
	}

	public void setNegativeButton(final String text, final View.OnClickListener onClickListener) {
		this.mBtn_cancel.setText((CharSequence) text);
		this.mBtn_cancel.setOnClickListener(onClickListener);
	}

	public void setPositiveButton(final String text, final View.OnClickListener onClickListener) {
		this.mBtn_ok.setText((CharSequence) text);
		this.mBtn_ok.setOnClickListener(onClickListener);
	}

	public void setTitle(final int text) {
		this.mTitle.setText(text);
	}

	public void setTitle(final String text) {
		this.mTitle.setText((CharSequence) text);
	}
}
