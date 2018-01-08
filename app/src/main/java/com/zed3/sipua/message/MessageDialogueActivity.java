package com.zed3.sipua.message;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zed3.addressbook.DataBaseService;
import com.zed3.groupmessage.GroupMessage;
import com.zed3.log.MyLog;
import com.zed3.net.util.NetChecker;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.SipdroidEngine;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.ui.lowsdk.CallUtil;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.ui.lowsdk.MessageListAdapter;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.toast.MyToast;
import com.zed3.utils.LogUtil;
import com.zed3.utils.Tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MessageDialogueActivity extends Activity implements View.OnClickListener {
	public static String RECEIVE_TEXT_MESSAGE;
	public static String REFRESH_UI;
	public static final int REQUEST_MSG_EDIT_NEW_CONTACT = 2;
	public static final int RESULT_MSG_CHANGED = 1;
	public static String SEND_TEXT_FAIL;
	public static String SEND_TEXT_SUCCEED;
	public static String SEND_TEXT_TIMEOUT;
	public static final String USER_NAME = "userName";
	public static final String USER_NUMBER = "address";
	ArrayList<GroupMessage> ListGroupMessage;
	private String TAG;
	private Button btnSelectMsg;
	private Button btnSendMsg;
	private View btn_home_message;
	private Context context;
	private String draft;
	private EditText edtInputMsg;
	private ImageButton imbMsgCall;
	private ImageButton imbMsgCall2;
	private boolean isContent;
	private boolean isshowing;
	private ListView lsvItemsMsg;
	private String mAddress;
	AlertDialog mAlertDlg;
	private Context mContext;
	private Cursor mCursor;
	private IntentFilter mFilter;
	private SmsMmsDatabase mSmsMmsDatabase;
	private String mUserName;
	private ListView messageList;
	private View messageView;
	private String msgContent;
	private TextView none_message_dialog;
	private PopupWindow popview;
	private BroadcastReceiver refreshBroadcast;
	List<String> strings;
	private TextView txtMsgName;

	static {
		MessageDialogueActivity.RECEIVE_TEXT_MESSAGE = "TEXT_MESSAGE_CHANGED";
		MessageDialogueActivity.SEND_TEXT_FAIL = "SEND_MESSAGE_FAIL";
		MessageDialogueActivity.SEND_TEXT_SUCCEED = "SEND_MESSAGE_SUCCEED";
		MessageDialogueActivity.SEND_TEXT_TIMEOUT = "SEND_MESSAGE_TIMEOUT";
		MessageDialogueActivity.REFRESH_UI = "UI_UPDATE";
	}

	public MessageDialogueActivity() {
		this.TAG = "MessageDialogueActivity";
		this.isContent = true;
		this.isshowing = false;
		this.refreshBroadcast = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				MessageDialogueActivity.this.mRefresh();
			}
		};
	}

	private String getMsgId(final Context context) {
		final StringBuilder sb = new StringBuilder();
		sb.append("00000000");
		sb.append(String.valueOf((System.currentTimeMillis() - SipdroidEngine.serverTimeVal) / 1000L));
		sb.append(Tools.getRandomCharNum(14));
		return sb.toString();
	}

	private void init() {
		this.context = (Context) this;
		this.lsvItemsMsg = (ListView) this.findViewById(R.id.lsvItemsMsg);
		(this.txtMsgName = (TextView) this.findViewById(R.id.txtMsgNames)).setMovementMethod(ScrollingMovementMethod.getInstance());
		this.imbMsgCall = (ImageButton) this.findViewById(R.id.imbMsgCall);
		this.imbMsgCall2 = (ImageButton) this.findViewById(R.id.imbMsgCall2);
		this.btnSelectMsg = (Button) this.findViewById(R.id.btnselectmsg);
		this.btnSendMsg = (Button) this.findViewById(R.id.btnSendMsg);
		this.edtInputMsg = (EditText) this.findViewById(R.id.edtInputMsg);
		if (this.edtInputMsg.getText().toString().length() == 0) {
			this.btnSendMsg.setTextColor(this.getResources().getColor(R.color.disable_color));
			this.isContent = false;
		}
		this.none_message_dialog = (TextView) this.findViewById(R.id.none_message_dialog);
		this.edtInputMsg.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120)});
		this.edtInputMsg.addTextChangedListener((TextWatcher) new TextWatcher() {
			public void afterTextChanged(final Editable editable) {
			}

			public void beforeTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
			}

			public void onTextChanged(final CharSequence charSequence, final int n, final int n2, final int n3) {
				if (charSequence.length() > 0) {
					MessageDialogueActivity.this.btnSendMsg.setTextColor(MessageDialogueActivity.this.getResources().getColor(R.color.tab_wihte));
					return;
				}
				MessageDialogueActivity.this.btnSendMsg.setTextColor(MessageDialogueActivity.this.getResources().getColor(R.color.disable_color));
			}
		});
		this.mSmsMmsDatabase = new SmsMmsDatabase(this.context);
		final Cursor mQuery = this.mSmsMmsDatabase.mQuery("message_draft", "address = '" + this.mAddress + "'", null, null);
		if (mQuery != null && mQuery.getCount() == 1) {
			mQuery.moveToFirst();
			this.edtInputMsg.setText((CharSequence) mQuery.getString(mQuery.getColumnIndex("body")));
		}
		mQuery.close();
		this.imbMsgCall.setOnClickListener((View.OnClickListener) this);
		this.btnSendMsg.setOnClickListener((View.OnClickListener) this);
		this.imbMsgCall2.setOnClickListener((View.OnClickListener) this);
	}

	private void mDelete() {
		final String string = this.mCursor.getString(this.mCursor.getColumnIndex("E_id"));
		final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(this.context);
		smsMmsDatabase.delete("message_talk", "E_id = '" + string + "'");
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		this.mCursor = smsMmsDatabase.mQuery("message_talk", "server_ip = '" + autoConfigManager.fetchLocalServer() + "'" + "and local_number = '" + autoConfigManager.fetchLocalUserName() + "'" + "and address = '" + this.mAddress + "'" + " and type = 'sms'", null, null);
		MyLog.i(String.valueOf(this.TAG) + "---cursor length", new StringBuilder(String.valueOf(this.mCursor.getCount())).toString());
		this.lsvItemsMsg.setAdapter((ListAdapter) new MessageDialogueCursorAdapter(this.context, this.mCursor));
		if (smsMmsDatabase != null) {
			smsMmsDatabase.close();
		}
	}

	private void mRefresh() {
		LogUtil.makeLog(this.TAG, "--++>>mRefresh()");
		this.mSmsMmsDatabase = new SmsMmsDatabase(this.context);
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		final String fetchLocalServer = autoConfigManager.fetchLocalServer();
		final String fetchLocalUserName = autoConfigManager.fetchLocalUserName();
		this.mCursor = this.mSmsMmsDatabase.mQuery("message_talk", "server_ip = '" + fetchLocalServer + "'" + "and local_number = '" + fetchLocalUserName + "'" + "and address = '" + this.mAddress + "'" + " and type = 'sms'", null, "date");
		LogUtil.makeLog(this.TAG, "--++>>mRefresh() cursor length:" + this.mCursor.getCount());
		if (this.mCursor == null || this.mCursor.getCount() < 1) {
			this.none_message_dialog.setVisibility(View.VISIBLE);
		} else {
			this.none_message_dialog.setVisibility(View.GONE);
		}
		this.lsvItemsMsg.setAdapter((ListAdapter) new MessageDialogueCursorAdapter(this.context, this.mCursor));
		final ContentValues contentValues = new ContentValues();
		contentValues.put("status", 1);
		this.mSmsMmsDatabase.update("message_talk", "server_ip = '" + fetchLocalServer + "'" + "and local_number = '" + fetchLocalUserName + "'" + "and address = '" + this.mAddress + "'", contentValues);
		if (this.mSmsMmsDatabase != null) {
			this.mSmsMmsDatabase.close();
		}
	}

	private void packData(final String s, final int n) {
		final GroupMessage groupMessage = new GroupMessage(n);
		groupMessage.addElement(s);
		this.ListGroupMessage.add(groupMessage);
	}

	private void sendMessage(final String s, final String s2) {
		final String sendTextMessage = Receiver.GetCurUA().SendTextMessage(s, s2, this.getMsgId(this.context));
		final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(this.context);
		final ContentValues contentValues = new ContentValues();
		contentValues.put("body", s2);
		contentValues.put("mark", 1);
		contentValues.put("address", s);
		contentValues.put("status", 1);
		contentValues.put("date", this.getCurrentTime());
		contentValues.put("E_id", sendTextMessage);
		contentValues.put("send", 2);
		contentValues.put("type", "sms");
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		contentValues.put("server_ip", autoConfigManager.fetchLocalServer());
		contentValues.put("local_number", autoConfigManager.fetchLocalUserName());
		smsMmsDatabase.insert("message_talk", contentValues);
		LogUtil.makeLog(this.TAG, "--++>>sendMessage()->body:" + s2);
	}

	private void sendMessage(String msgId, final String s, final String s2) {
		msgId = this.getMsgId(this.context);
		Receiver.GetCurUA().SendGroupTextMessage(this.ListGroupMessage, s2, msgId);
		final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(this.context);
		final ContentValues contentValues = new ContentValues();
		contentValues.put("body", s2);
		contentValues.put("mark", 1);
		contentValues.put("address", s);
		contentValues.put("status", 1);
		contentValues.put("date", this.getCurrentTime());
		contentValues.put("send", 2);
		contentValues.put("type", "sms");
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		contentValues.put("server_ip", autoConfigManager.fetchLocalServer());
		contentValues.put("local_number", autoConfigManager.fetchLocalUserName());
		smsMmsDatabase.insert("message_talk", contentValues);
		LogUtil.makeLog(this.TAG, "--++>>sendMessage()->body:" + s2);
	}

	void dismissPop() {
		if (this.popview == null || !this.popview.isShowing()) {
			return;
		}
		while (true) {
			try {
				this.popview.dismiss();
				this.isshowing = false;
			} catch (Exception ex) {
				continue;
			}
			break;
		}
	}

	public String getCurrentTime() {
		try {
			return new SimpleDateFormat(" yyyy-MM-dd HH:mm ").format(System.currentTimeMillis());
		} catch (Exception ex) {
			return null;
		}
	}

	public void onClick(final View view) {
		if (NetChecker.check((Context) this, true)) {
			switch (view.getId()) {
				default: {
				}
				case R.id.imbMsgCall2: {
					CallUtil.makeVideoCall(this.mContext, this.mAddress, null, "videobut");
				}
				case R.id.imbMsgCall: {
					CallUtil.makeAudioCall(this.mContext, this.mAddress, null);
				}
				case R.id.btnSendMsg: {
					if (!this.isContent) {
						break;
					}
					if (this.mAddress.contains("#") || this.mAddress.contains("*")) {
						MyToast.showToast(true, this.mContext, this.getResources().getString(R.string.invalid_char));
						return;
					}
					this.msgContent = this.edtInputMsg.getText().toString();
					if (this.msgContent.equals("")) {
						MyToast.showToast(true, this.mContext, this.getResources().getString(R.string.input_message_text));
						return;
					}
					if (this.mAddress.contains(",") || this.mAddress.contains("\uff0c")) {
						this.sendMessage("members=", this.mAddress, this.msgContent);
					} else {
						this.sendMessage(this.mAddress, this.msgContent);
					}
					this.edtInputMsg.setText((CharSequence) "");
					this.mRefresh();
				}
				case R.id.btnselectmsg: {
					if (this.isshowing) {
						this.dismissPop();
					}
					this.showMessagePopWindow(view);
				}
			}
		}
	}

	protected void onCreate(Bundle extras) {
		super.onCreate(extras);
		this.getWindow().setSoftInputMode(3);
		this.setContentView(R.layout.activity_msg_edit);
		this.mContext = (Context) this;
		(this.mFilter = new IntentFilter()).addAction(MessageDialogueActivity.REFRESH_UI);
		this.registerReceiver(this.refreshBroadcast, this.mFilter);
		extras = this.getIntent().getExtras();
		this.mAddress = extras.getString("address");
		this.mUserName = null;
		if (ContactUtil.getUserName(this.mAddress) != null) {
			this.mUserName = ContactUtil.getUserName(this.mAddress);
		}
		if (this.mUserName == null) {
			this.mUserName = GroupListUtil.getUserName(this.mAddress);
			if (this.mUserName == null) {
				this.mUserName = this.mAddress;
			}
		}
		this.ListGroupMessage = new ArrayList<GroupMessage>();
		if (TextUtils.isEmpty((CharSequence) extras.getString("0")) && !TextUtils.isEmpty((CharSequence) this.mUserName) && (this.mUserName.contains(",") || this.mUserName.contains("\uff0c"))) {
			this.packData(this.mUserName, 0);
		}
		this.init();
		this.mRefresh();
		final Intent intent = new Intent();
//		intent.setAction(MainActivity.READ_MESSAGE);
		this.mContext.sendBroadcast(intent);
		this.txtMsgName.setText((CharSequence) this.mUserName);
		(this.btn_home_message = this.findViewById(R.id.btn_home_message)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				MessageDialogueActivity.this.finish();
			}
		});
		this.btn_home_message.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) MessageDialogueActivity.this.findViewById(R.id.photo_sent_home2);
				final TextView textView2 = (TextView) MessageDialogueActivity.this.findViewById(R.id.left_photo2);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						MessageDialogueActivity.this.btn_home_message.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(MessageDialogueActivity.this.getResources().getColor(R.color.font_color3));
						MessageDialogueActivity.this.btn_home_message.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
		this.registerForContextMenu((View) this.lsvItemsMsg);
		this.btnSelectMsg.setOnClickListener((View.OnClickListener) this);
		if (extras.getString("0") != null && extras.getString("0").equals("compose")) {
			final String string = extras.getString("toValue");
			if (!TextUtils.isEmpty((CharSequence) string) && (string.contains(",") || string.contains("\uff0c"))) {
				this.packData(string, 0);
				this.sendMessage(extras.getString("head"), string, extras.getString("bodyValue"));
				this.mRefresh();
			} else if (!TextUtils.isEmpty((CharSequence) string)) {
				this.sendMessage(string, extras.getString("bodyValue"));
				this.mRefresh();
			}
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		CommonUtil.Log("menu", "MsgEditActivity", "onCreateContextMenu", 'i');
		menu.setHeaderTitle(getResources().getString(R.string.options));
		menu.add(0, 1, 0, getResources().getString(R.string.forward));
		menu.add(0, 2, 1, getResources().getString(R.string.delete_message_one));
	}

	protected void onDestroy() {
		super.onDestroy();
		this.draft = this.edtInputMsg.getText().toString();
		if (this.draft == null || this.draft.length() <= 0) {
			this.mSmsMmsDatabase = new SmsMmsDatabase(this.context);
			this.mSmsMmsDatabase.delete("message_draft", "address = '" + this.mAddress + "'");
		} else {
			this.mSmsMmsDatabase = new SmsMmsDatabase(this.context);
			ContentValues cvs = new ContentValues();
			cvs.put(USER_NUMBER, this.mAddress);
			cvs.put(MmsMessageDetailActivity.MESSAGE_BODY, this.draft);
			this.mSmsMmsDatabase.delete("message_draft", "address = '" + this.mAddress + "'");
			this.mSmsMmsDatabase.insert("message_draft", cvs);
		}
		Intent intent_o = new Intent();
//		intent_o.setAction(MainActivity.READ_MESSAGE);
		sendBroadcast(intent_o);
		if (this.mFilter != null) {
			try {
				unregisterReceiver(this.refreshBroadcast);
			} catch (Exception e) {
				MyLog.e("MessageDialogueActivity", "unregister error");
				e.printStackTrace();
			}
		}
		if (this.mSmsMmsDatabase != null) {
			this.mSmsMmsDatabase.close();
		}
		if (this.mCursor != null) {
			this.mCursor.close();
		}
	}

	public boolean onMenuItemSelected(final int n, final MenuItem menuItem) {
		CommonUtil.Log("menu", "ContactActivity", "onMenuItemSelected", 'i');
		switch (menuItem.getItemId()) {
			case 1: {
				final Intent intent = new Intent(this.context, (Class) MessageComposeActivity.class);
				this.mCursor.getString(this.mCursor.getColumnIndex("body"));
				intent.putExtra("body", this.mCursor.getString(this.mCursor.getColumnIndex("body")));
				this.startActivity(intent);
				this.finish();
				break;
			}
			case 2: {
				this.mDelete();
				break;
			}
		}
		return true;
	}

	void showMessagePopWindow(final View view) {
		if (this.popview == null) {
			this.messageView = View.inflate(this.context, R.layout.message_list, (ViewGroup) null);
			this.messageList = (ListView) this.messageView.findViewById(R.id.messagelistview);
			this.strings = DataBaseService.getInstance().getAllMessages();
			this.messageList.setAdapter((ListAdapter) new MessageListAdapter(this.context, this.strings));
			this.popview = new PopupWindow(this.messageView, -1, 350);
		}
		this.popview.setFocusable(true);
		this.popview.setOutsideTouchable(false);
		this.popview.setTouchable(true);
		this.popview.setBackgroundDrawable((Drawable) new BitmapDrawable());
		Log.i("coder", "xPos:" + (((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth() / 2 - this.popview.getWidth() / 2));
		final int[] array = new int[2];
		view.getLocationOnScreen(array);
		this.popview.showAtLocation(view, 0, array[0], array[1] - this.popview.getHeight() - 20);
		this.messageList.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				MessageDialogueActivity.this.edtInputMsg.setText((CharSequence) MessageDialogueActivity.this.strings.get(n));
				MessageDialogueActivity.this.dismissPop();
			}
		});
		this.isshowing = true;
	}
}
