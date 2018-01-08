package com.zed3.sipua.baiduMap;

import android.content.Context;
import android.util.Log;

import com.zed3.addressbook.DataBaseService;
import com.zed3.sipua.R;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Controler4Js {
	private Context mContext;
	private ArrayList<String> tempGrpList;

	public Controler4Js(final Context mContext) {
		this.tempGrpList = new ArrayList<String>();
		this.mContext = mContext;
	}

	public String getTime() {
		try {
			return new SimpleDateFormat(" HHmmss ").format(new Date(System.currentTimeMillis()));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void printLog(final String s) {
		Log.i("zdx", "webview : " + s);
	}

	public void toAudioCall(final String s) {
		if ("1".equals(DataBaseService.getInstance().getMemberAudioType(s)) && DeviceInfo.CONFIG_SUPPORT_AUDIO_SINGLE) {
			// TODO
		}
		MyToast.showToast(true, this.mContext, this.mContext.getResources().getString(R.string.vc_service_not));
	}

	public void toVideoCall(final String s) {
		if ("1".equals(DataBaseService.getInstance().getMemberVideoType(s))) {
			CallUtil.makeVideoCall(this.mContext, s, null, "videobut");
			return;
		}
		MyToast.showToast(true, this.mContext, this.mContext.getResources().getString(R.string.ve_service_not));
	}
}
