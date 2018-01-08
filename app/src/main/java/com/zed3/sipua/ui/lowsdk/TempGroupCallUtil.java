package com.zed3.sipua.ui.lowsdk;

import android.content.Context;
import android.content.Intent;

import com.zed3.net.util.NetChecker;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.Settings;
import com.zed3.toast.MyToast;

import org.zoolu.sip.call.Call;

import java.util.ArrayList;

public class TempGroupCallUtil {
	public static ArrayList<String> arrayListMembers;
	public static Call mCall;
	public static String tmpGrpName;

	static {
		TempGroupCallUtil.tmpGrpName = "";
		TempGroupCallUtil.arrayListMembers = new ArrayList<String>();
	}

	public static boolean isTmpCallClosed() {
		return TempGroupCallUtil.mCall == null || (TempGroupCallUtil.mCall != null && TempGroupCallUtil.mCall.isCanceledOrByed());
	}

	public static void makeTempGroupCall(final Context context, final String s, final ArrayList<String> list, final boolean b) {
		if (!NetChecker.check(context, true)) {
			MyToast.showToast(true, SipUAApp.mContext, R.string.notfast_1);
			return;
		}
		if (CallUtil.checkGsmCallInCall()) {
			MyToast.showToast(true, SipUAApp.mContext, R.string.gsm_in_call);
			return;
		}
		if (!Receiver.mSipdroidEngine.isRegistered()) {
			MyToast.showToast(true, SipUAApp.mContext, R.string.nologin);
			return;
		}
		if (Receiver.call_state == 3 || Receiver.call_state == 2) {
			MyToast.showToast(true, SipUAApp.mContext, R.string.vedio_calling_notify);
			return;
		}
		final Intent intent = new Intent();
		intent.setClass(context, (Class) TempGrpCallActivity.class);
		intent.putExtra("tempGroupName", s);
		intent.putExtra("isCreator", b);
		intent.putStringArrayListExtra("groupMemberList", (ArrayList) list);
		context.startActivity(intent);
		Receiver.GetCurUA().makeTempGrpCall(Settings.getUserName(), s, list);
	}
}
