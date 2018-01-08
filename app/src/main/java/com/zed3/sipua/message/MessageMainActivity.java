package com.zed3.sipua.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zed3.flow.FlowRefreshService;
import com.zed3.log.MyLog;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.OneShotAlarm;
import com.zed3.sipua.ui.Receiver;
import com.zed3.sipua.ui.RegisterService;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.welcome.AutoConfigManager;

import java.util.Map;

public class MessageMainActivity extends BaseActivity implements View.OnClickListener {
	public static final String COLOR_LIGHT = "#FFFFFFF";
	public static final int REQUEST_MSG_EDIT = 1;
	public static final int REQUEST_MSG_NEW_CONTACT = 2;
	private static Activity mContext;
	String GET_NEWEST_MESSAGE;
	String TABLE_NAME;
	public Map<String, Object> clickedItem;
	private ImageView imbNewMsg;
	public boolean isEditMode;
	private ListView lsvMsg;
	private Cursor mCursor;
	private IntentFilter mFilter;
	private MessageMainCursorAdapter mMessageCursorAdapter;
	private View mRootView;
	private SmsMmsDatabase mSmsMmsDatabase;
	private TextView none_message;
	private PopupWindow popupWindow;
	private BroadcastReceiver recv;

	public MessageMainActivity() {
		this.GET_NEWEST_MESSAGE = "select * from message_talk ";
		this.TABLE_NAME = "message_talk";
		this.recv = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				if (intent.getAction().equalsIgnoreCase(MessageDialogueActivity.RECEIVE_TEXT_MESSAGE)) {
					MessageMainActivity.this.refresh();
				}
			}
		};
	}

	private void exitApp() {
		this.finish();
		Receiver.engine((Context) this).expire(-1);
		while (true) {
			try {
				Thread.sleep(800L);
				Receiver.engine((Context) this).halt();
				this.stopService(new Intent((Context) this, (Class) RegisterService.class));
				Receiver.alarm(0, OneShotAlarm.class);
				final Intent intent = new Intent("android.intent.action.MAIN");
				intent.addCategory("android.intent.category.HOME");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent);
				System.exit(0);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				continue;
			}
			break;
		}
	}

	private void mDelete() {
		final String string = this.mCursor.getString(this.mCursor.getColumnIndex("address"));
		final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase((Context) MessageMainActivity.mContext);
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		smsMmsDatabase.delete("message_talk", "address = '" + string + "'" + "and type = 'sms' and server_ip = '" + autoConfigManager.fetchLocalServer() + "'" + "and local_number = '" + autoConfigManager.fetchLocalUserName() + "'");
		this.refresh();
//		MessageMainActivity.mContext.sendBroadcast(new Intent(MainActivity.READ_MESSAGE));
	}

	private void mDeleteAll() {
		final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase((Context) MessageMainActivity.mContext);
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		smsMmsDatabase.delete("message_talk", "type = 'sms' and server_ip = '" + autoConfigManager.fetchLocalServer() + "'" + "and local_number = '" + autoConfigManager.fetchLocalUserName() + "'");
		this.refresh();
//		MessageMainActivity.mContext.sendBroadcast(new Intent(MainActivity.READ_MESSAGE));
	}

	private void refresh() {
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		this.mCursor = this.mSmsMmsDatabase.mQuery("message_talk", "type = 'sms' and server_ip = '" + autoConfigManager.fetchLocalServer() + "'" + "and local_number = '" + autoConfigManager.fetchLocalUserName() + "'", "address", "date desc");
		if (this.mCursor == null || this.mCursor.getCount() < 1) {
			this.none_message.setVisibility(View.VISIBLE);
		} else {
			this.none_message.setVisibility(View.GONE);
		}
		if (this.mMessageCursorAdapter == null) {
			this.mMessageCursorAdapter = new MessageMainCursorAdapter((Context) MessageMainActivity.mContext, this.mCursor);
			this.lsvMsg.setAdapter((ListAdapter) this.mMessageCursorAdapter);
			return;
		}
		this.mMessageCursorAdapter.changeCursor(this.mCursor);
	}

	public boolean dismissMenuPopupWindows() {
		if (this.popupWindow != null && this.popupWindow.isShowing()) {
			this.popupWindow.dismiss();
			return true;
		}
		return false;
	}

	public void onClick(final View view) {
		this.dismissMenuPopupWindows();
		switch (view.getId()) {
			default: {
			}
			case R.id.contact: {
				this.startActivity(new Intent((Context) MessageMainActivity.mContext, (Class) MessageToContact.class));
			}
			case R.id.imbNewMessage: {
				MessageMainActivity.mContext.startActivity(new Intent((Context) MessageMainActivity.mContext, (Class) MessageComposeActivity.class));
			}
		}
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.activity_message);
		MessageMainActivity.mContext = this;
		(this.mRootView = this.getLayoutInflater().inflate(R.layout.activity_message, (ViewGroup) null)).setOnClickListener((View.OnClickListener) this);
		(this.mFilter = new IntentFilter()).addAction(MessageDialogueActivity.RECEIVE_TEXT_MESSAGE);
		MessageMainActivity.mContext.registerReceiver(this.recv, this.mFilter);
		this.registerForContextMenu((View) (this.lsvMsg = (ListView) this.findViewById(R.id.lsvMessage)));
		(this.imbNewMsg = (ImageView) this.findViewById(R.id.imbNewMessage)).setOnClickListener((View.OnClickListener) this);
		this.mSmsMmsDatabase = new SmsMmsDatabase((Context) MessageMainActivity.mContext);
		this.none_message = (TextView) this.findViewById(R.id.none_message);
		this.refresh();
		this.lsvMsg.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				final Intent intent = new Intent((Context) MessageMainActivity.mContext, (Class) MessageDialogueActivity.class);
				intent.putExtra("address", MessageMainActivity.this.mCursor.getString(MessageMainActivity.this.mCursor.getColumnIndex("address")));
				MessageMainActivity.this.startActivity(intent);
			}
		});
		if (GroupListUtil.getGroupListsMap().size() == 0) {
			GroupListUtil.getData4GroupList();
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		CommonUtil.Log("menu", "MsgEditActivity", "onCreateContextMenu", 'i');
		menu.setHeaderTitle(R.string.options);
		menu.add(0, 2, 1, getResources().getString(R.string.delete_message));
		menu.add(0, 3, 2, getResources().getString(R.string.delete_all_message));
	}

	public void onDestroy() {
		if (this.mFilter != null) {
			try {
				mContext.unregisterReceiver(this.recv);
			} catch (Exception e) {
				MyLog.e("MessageMainActivity", "unregister error");
				e.printStackTrace();
			}
		}
		if (this.mCursor != null) {
			this.mCursor.close();
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
		switch (n) {
			case 4: {
				if (this.dismissMenuPopupWindows()) {
					return true;
				}
				break;
			}
			case 82: {
				if (this.dismissMenuPopupWindows()) {
					return true;
				}
				break;
			}
		}
		return super.onKeyDown(n, keyEvent);
	}

	public boolean onMenuItemSelected(final int n, final MenuItem menuItem) {
		CommonUtil.Log("menu", "ContactActivity", "onMenuItemSelected", 'i');
		switch (menuItem.getItemId()) {
			case 1: {
				MessageMainActivity.mContext.stopService(new Intent((Context) MessageMainActivity.mContext, (Class) FlowRefreshService.class));
				this.exitApp();
				break;
			}
			case 2: {
				this.mDelete();
				break;
			}
			case 3: {
				this.mDeleteAll();
				break;
			}
		}
		return true;
	}

	public void onResume() {
		this.refresh();
		super.onResume();
	}

	public void onStart() {
		super.onStart();
	}

	public void onStop() {
		super.onStop();
	}
}
