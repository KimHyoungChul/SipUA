package com.zed3.toast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.utils.Tools;

public class MyToast {
	private static long lastTime;
	private static Toast toastStart;

	private static View getToastView(final Context context, final String text) {
		final View inflate = LayoutInflater.from(context).inflate(R.layout.toast, (ViewGroup) null);
		((TextView) inflate.findViewById(R.id.message)).setText((CharSequence) text);
		return inflate;
	}

	private static void makeText(final Context context, final View view) {
		if (MyToast.toastStart == null) {
			(MyToast.toastStart = new Toast(context)).setDuration(Toast.LENGTH_LONG);
			MyToast.toastStart.setView(view);
		} else {
			MyToast.toastStart.setView(view);
		}
		MyToast.toastStart.show();
	}

	public static void showToast(final boolean b, final Context context, final int n) {
		Context mContext = context;
		if (context == null) {
			mContext = SipUAApp.mContext;
		}
		showToast(b, mContext, mContext.getResources().getString(n));
	}

	public static void showToast(final boolean b, final Context context, final String s) {
		Context mContext = context;
		if (context == null) {
			mContext = SipUAApp.mContext;
		}
		if (b && !Tools.isRunBackGroud(mContext)) {
			makeText(mContext, getToastView(mContext, s));
		}
	}

	public static void showToastInBg(final boolean b, final Context context, final int n) {
		if (b) {
			makeText(context, getToastView(context, context.getResources().getString(n)));
		}
	}
}
