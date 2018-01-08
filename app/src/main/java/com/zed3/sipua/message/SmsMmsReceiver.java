package com.zed3.sipua.message;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.zed3.log.MyLog;
import com.zed3.media.TipSoundPlayer;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.Receiver;
import com.zed3.toast.MyToast;
import com.zed3.utils.Systems;
import com.zed3.utils.Tools;

import java.util.ArrayList;

public class SmsMmsReceiver extends BroadcastReceiver {
	public static final String ACTION_DELIVERY_REPORT_REPLY = "com.zed3.sipua.delivery_report";
	public static final String ACTION_GROUP_NUM_TYPE = "com.zed3.sipua.group_num_type";
	public static final String ACTION_KEY_LONG_CLICK = "android.intent.action.NUMBER_KEY_PRESSED";
	public static final String ACTION_LTE_JOIN_EMERGENCY_GROUP = "android.intent.action.LTE_EMERGENCY_CALL";
	public static final String ACTION_OFFLINE_SPACE_FULL = "com.zed3.sipua.mms_offline_space_full";
	public static final String ACTION_READ_REPORT_REPLY = "com.zed3.sipua.read_report";
	public static final String ACTION_RECEIVER_MMS_MESSAGE = "com.zed3.sipua.mms_receive";
	public static final String ACTION_RECEIVE_SMSMMS_ANSWER = "com.zed3.sipua.development_interface_answer";
	public static final String ACTION_RECEIVE_SMSMMS_INTERFACE = "com.zed3.sipua.development_interface";
	public static final String ACTION_RECEIVE_SMS_MESSAGE = "com.zed3.sipua.sms_receive";
	public static final String ACTION_SEND_MESSAGE_FAIL = "com.zed3.sipua.send_message_fail";
	public static final String ACTION_SEND_MESSAGE_OK = "com.zed3.sipua.send_message_ok";
	public static final int FLAG_MMS = 11;
	public static final int FLAG_SMS = 10;
	private static final int GROUP_NUM = 0;
	public static final String GROUP_NUMBER_TYPE = "Group";
	private static final int MESSAGE_READ = 3;
	private static final int MESSAGE_RECEIVE_FAIL = 2;
	private static final int MESSAGE_RECEIVE_OK = 1;
	public static final String MESSAGE_REPORT_READ_OK = "Read";
	public static final String MESSAGE_REPORT_RECEIVE_FAIL = "ReceiveFail";
	public static final String MESSAGE_REPORT_RECEIVE_OK = "ReceiveOK";
	public static final String MESSAGE_REPORT_RECEIVE_OK_DATABASE = "ReceiveOK_DataBase";
	private static final int MESSAGE_SEND_FAIL = 5;
	private static final int MESSAGE_SEND_OK = 4;
	private static final String TABLE_MMS_SENT = "mms_sent";
	private static final String TABLE_SMS_SENT = "sms_sent";
	private static final String TAG = "SmsMmsReceiver";
	private static final int TYPE_ALARM_EMERGENCY = 6;
	private static final String TYPE_EMERGENT_STATUS = "Emergent-Status";
	private static final String TYPE_PREDEFINE_STATUS = "Predefine-Status";
	private String E_id;
	private ArrayList<String> contact_sent_arraylist;
	private String[] contact_sent_list;
	private SmsMmsDatabase database;
	private int flag;
	private Context mContext;
	Handler mHandler;
	private String pre_define_msg;
	private String recipient_num;
	ContentValues values;
	private String where;

	public SmsMmsReceiver() {
		this.values = new ContentValues();
		this.flag = -1;
		this.where = "";
		this.mHandler = new Handler() {
			public void handleMessage(final Message message) {
				// TODO
			}
		};
	}

	private void playMessageAcceptSound(final int n) {
		final MediaPlayer create = MediaPlayer.create(SipUAApp.mContext, n);
		if (create != null) {
			create.start();
		}
	}

	private void pttTextMessageTipSound() {
		this.playMessageAcceptSound(R.raw.imreceive);
	}

	public void onReceive(final Context mContext, final Intent intent) {
		this.mContext = mContext;
		final String action = intent.getAction();
		Systems.log.print("testsound", "SmsMmsReceiver#onReceive enter action = " + action);
		MyLog.i("SmsMmsReceiver", "intentAction = " + action);
		if (action.equals("com.zed3.sipua.mms_receive")) {
			TipSoundPlayer.getInstance().play(TipSoundPlayer.Sound.MESSAGE_ACCEPT);
			final String stringExtra = intent.getStringExtra("contentType");
			Log.i("xxxx", "SmsMmsReceive#onReceive content type = " + stringExtra);
			if (TextUtils.isEmpty((CharSequence) stringExtra)) {
				mContext.sendBroadcast(new Intent("com.zed3.action.RECEIVE_MMS"));
				return;
			}
			mContext.sendBroadcast(new Intent("com.zed3.action.RECEIVE_MMS"));
		} else {
			if (action.equals("com.zed3.sipua.group_num_type")) {
				this.mHandler.sendMessage(this.mHandler.obtainMessage(0, (Object) intent));
				return;
			}
			if (action.equals("com.zed3.sipua.delivery_report")) {
				MyLog.v("guojunfeng", "REPORT_REPLY");
				final Bundle extras = intent.getExtras();
				if (extras != null) {
					this.E_id = extras.getString("E_id");
					this.recipient_num = extras.getString("recipient_num");
					final String string = extras.getString("reply");
					PreferenceManager.getDefaultSharedPreferences(this.mContext);
					if (string.trim().equals("ReceiveOK")) {
						final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(this.mContext);
						final ContentValues contentValues = new ContentValues();
						contentValues.put("send", 0);
						smsMmsDatabase.update("message_talk", "E_id ='" + this.E_id + "'", contentValues);
						final String language = this.mContext.getResources().getConfiguration().locale.getLanguage();
						String s = "";
						if (language.equals("en")) {
							s = this.mContext.getResources().getString(R.string.sent_success);
						} else if (language.equals("zh")) {
							s = String.valueOf(this.recipient_num) + " " + this.mContext.getResources().getString(R.string.sent_success);
						}
						MyToast.showToast(true, Receiver.mContext, s);
						if (smsMmsDatabase != null) {
							smsMmsDatabase.close();
						}
						this.mContext.sendBroadcast(new Intent("ReceiveOK_DataBase"));
						Tools.deleteFileByE_id(this.E_id);
						return;
					}
					if (string.equals("ReceiveFail")) {
						MyToast.showToast(true, Receiver.mContext, String.valueOf(Receiver.mContext.getResources().getString(R.string.mms_sendFailed_1)) + this.recipient_num + Receiver.mContext.getResources().getString(R.string.mms_sendFailed_2));
						final SmsMmsDatabase smsMmsDatabase2 = new SmsMmsDatabase(SipUAApp.getAppContext());
						final ContentValues contentValues2 = new ContentValues();
						contentValues2.put("send", 1);
						smsMmsDatabase2.update("message_talk", "E_id ='" + this.E_id + "'", contentValues2);
						return;
					}
					if (string.equals("Read")) {
						MyToast.showToast(true, Receiver.mContext, String.valueOf(this.recipient_num) + Receiver.mContext.getResources().getString(R.string.readed));
						return;
					}
					MyLog.i("SmsMmsReceiver", "bail out because of unknow report state: " + string);
				}
			} else if (action.equals("com.zed3.sipua.send_message_ok")) {
				MyLog.v("guojunfeng", "MESSAGE_OK");
				final Bundle extras2 = intent.getExtras();
				if (extras2 != null) {
					this.E_id = extras2.getString("E_id");
					this.mHandler.sendMessage(this.mHandler.obtainMessage(4, (Object) this.E_id));
				}
			} else {
				if (action.equals("com.zed3.sipua.send_message_fail")) {
					this.mHandler.sendMessage(this.mHandler.obtainMessage(5, (Object) intent));
					return;
				}
				if (action.equals("com.zed3.sipua.mms_offline_space_full")) {
					MyToast.showToast(true, Receiver.mContext, String.valueOf(intent.getStringExtra("recipient_num")) + " " + Receiver.mContext.getResources().getString(R.string.upload_offline_space_full));
				}
			}
		}
	}
}
