package com.zed3.dialog;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

public class DialogUtil {
	private static DialogUtil instance = new DialogUtil();

	public static abstract class DialogCallBack {
		public abstract void onNegativeButtonClick();

		public abstract void onPositiveButtonClick();
	}

	private DialogUtil() {
	}

	public static DialogUtil getInstance() {
		return instance;
	}

	public static synchronized void showCheckDialog(Context context, String title, String message, String check) {
		synchronized (DialogUtil.class) {
			if (context instanceof Activity) {
				Builder dialog = new Builder(context);
				dialog.setTitle(title);
				dialog.setMessage(message);
//				dialog.setPositiveButton(check, new C09581());
				dialog.show();
				dialog.setCancelable(false);
			} else {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		}
	}

	public static synchronized void showSelectDialog(Context context, String title, String message, String check, DialogCallBack callBack) {
		synchronized (DialogUtil.class) {
			if (context instanceof Activity) {
				CustomDialog dialog = new CustomDialog(context);
				dialog.setTitle(title);
				dialog.setMessage(message);
//				dialog.setPositiveButton(check, new C09592(dialog, callBack));
//				dialog.setNegativeButton(context.getResources().getString(R.string.cancel), new C09603(dialog, callBack));
				dialog.setCancelable(false);
			} else {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		}
	}

	public static synchronized ProgressDialog showProcessDailog(Context context, String message) {
		ProgressDialog pd;
		synchronized (DialogUtil.class) {
			if (context instanceof Activity) {
				pd = new ProgressDialog(context);
//				pd.setOnCancelListener(new C09614());
				pd.setCanceledOnTouchOutside(false);
				pd.setMessage(message);
				pd.show();
			} else {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
				pd = null;
			}
		}
		return pd;
	}

	public static synchronized void dismissProcessDailog(ProgressDialog processDailog) {
		synchronized (DialogUtil.class) {
			if (processDailog != null) {
				if (processDailog.isShowing()) {
					processDailog.dismiss();
				}
			}
		}
	}
}
