package com.zed3.sipua.message;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.dialog.DialogUtil;
import com.zed3.log.MyLog;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.welcome.AutoConfigManager;

public class PhotoTransferSentActivity extends BaseActivity {
	final int ACTION_DISSMISS_LOADING_DIALOG;
	final int ACTION_INIT_MESSAGE_LIST;
	final int ACTION_SHOW_LOADING_DIALOG;
	final int ACTION_UPDATE_CURSOR;
	final int ACTION_UPDATE_MESSAGE_LIST;
	String MMS;
	View btn_home_photo;
	Handler delayTaskHandler;
	PhotoTransferCursorAdapter mAdapter;
	Context mContext;
	private Cursor mCursor;
	private IntentFilter mFilter;
	SmsMmsDatabase mSmsMmsDatabase;
	TextView none_photo_transfer;
	private BroadcastReceiver recv;
	ListView transfer_sent_list;

	public PhotoTransferSentActivity() {
		this.MMS = "mms";
		this.recv = new BroadcastReceiver() {
			public void onReceive(final Context context, final Intent intent) {
				if (!intent.getAction().equalsIgnoreCase("ReceiveOK_DataBase") && intent.getAction().equalsIgnoreCase("database_changed")) {
					PhotoTransferSentActivity.this.addDelayTask(2, 100L);
				}
			}
		};
		this.ACTION_INIT_MESSAGE_LIST = 1;
		this.ACTION_UPDATE_MESSAGE_LIST = 2;
		this.ACTION_UPDATE_CURSOR = 3;
		this.ACTION_SHOW_LOADING_DIALOG = 4;
		this.ACTION_DISSMISS_LOADING_DIALOG = 5;
		this.delayTaskHandler = new Handler() {
			ProgressDialog showProcessDailog;

			public void handleMessage(final Message message) {
				switch (message.what) {
					default: {
					}
					case 1: {
						PhotoTransferSentActivity.this.addDelayTask(3, 100L);
					}
					case 2: {
						DialogUtil.dismissProcessDailog(this.showProcessDailog);
						this.showProcessDailog = DialogUtil.showProcessDailog((Context) PhotoTransferSentActivity.this, PhotoTransferSentActivity.this.getResources().getString(R.string.loading));
						PhotoTransferSentActivity.this.addDelayTask(3, 100L);
					}
					case 3: {
						this.updateMessageList();
						PhotoTransferSentActivity.this.addDelayTask(5, 100L);
					}
					case 5: {
						DialogUtil.dismissProcessDailog(this.showProcessDailog);
					}
				}
			}

			public void updateMessageList() {
				if (PhotoTransferSentActivity.this.mSmsMmsDatabase == null) {
					PhotoTransferSentActivity.this.mSmsMmsDatabase = new SmsMmsDatabase(PhotoTransferSentActivity.this.mContext);
				}
				final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
				if (PhotoTransferSentActivity.this.mAdapter == null) {
					PhotoTransferSentActivity.this.mAdapter = new PhotoTransferCursorAdapter(PhotoTransferSentActivity.this.mContext, PhotoTransferSentActivity.this.mCursor, 1);
					PhotoTransferSentActivity.this.transfer_sent_list.setAdapter((ListAdapter) PhotoTransferSentActivity.this.mAdapter);
					PhotoTransferSentActivity.this.transfer_sent_list.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
						public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
							final String path = Uri.parse(PhotoTransferSentActivity.this.mCursor.getString(PhotoTransferSentActivity.this.mCursor.getColumnIndex("attachment"))).getPath();
							final String string = PhotoTransferSentActivity.this.mCursor.getString(PhotoTransferSentActivity.this.mCursor.getColumnIndex("body"));
							final Intent intent = new Intent((Context) PhotoTransferSentActivity.this, (Class) MmsMessageDetailActivity.class);
							intent.putExtra("body", string);
							intent.putExtra("pic_path", path);
							PhotoTransferSentActivity.this.startActivity(intent);
						}
					});
					PhotoTransferSentActivity.this.transfer_sent_list.setOnScrollListener(PhotoTransferSentActivity.this.mAdapter.getOnScrollListener());
				} else {
					PhotoTransferSentActivity.this.mAdapter.changeCursor(PhotoTransferSentActivity.this.mCursor);
				}
				if (PhotoTransferSentActivity.this.mCursor == null || PhotoTransferSentActivity.this.mCursor.getCount() == 0) {
					PhotoTransferSentActivity.this.none_photo_transfer.setVisibility(View.VISIBLE);
					return;
				}
				PhotoTransferSentActivity.this.none_photo_transfer.setVisibility(View.GONE);
			}
		};
	}

	private void addDelayTask(final int what, final long n) {
		final Message obtainMessage = this.delayTaskHandler.obtainMessage();
		obtainMessage.what = what;
		this.delayTaskHandler.sendMessageDelayed(obtainMessage, n);
	}

	@Override
	protected void onCreate(final Bundle bundle) {
		((PhotoTransferSentActivity) (this.mContext = (Context) this)).requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.aa_transfer_sent);
		(this.btn_home_photo = this.findViewById(R.id.btn_home_photo)).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				PhotoTransferSentActivity.this.finish();
			}
		});
		this.btn_home_photo.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
			public boolean onTouch(final View view, final MotionEvent motionEvent) {
				final TextView textView = (TextView) PhotoTransferSentActivity.this.findViewById(R.id.photo_sent_home);
				final TextView textView2 = (TextView) PhotoTransferSentActivity.this.findViewById(R.id.left_photo);
				switch (motionEvent.getAction()) {
					case 0: {
						textView.setTextColor(-1);
						PhotoTransferSentActivity.this.btn_home_photo.setBackgroundResource(R.color.btn_click_bg);
						textView2.setBackgroundResource(R.drawable.map_back_press);
						break;
					}
					case 1: {
						textView.setTextColor(PhotoTransferSentActivity.this.getResources().getColor(R.color.font_color3));
						PhotoTransferSentActivity.this.btn_home_photo.setBackgroundResource(R.color.whole_bg);
						textView2.setBackgroundResource(R.drawable.map_back_release);
						break;
					}
				}
				return false;
			}
		});
		this.none_photo_transfer = (TextView) this.findViewById(R.id.none_photo_transfer);
		this.transfer_sent_list = (ListView) this.findViewById(R.id.transfer_sent_list);
		this.none_photo_transfer.setVisibility(View.VISIBLE);
		this.addDelayTask(1, 100L);
		this.registerForContextMenu((View) this.transfer_sent_list);
		(this.mFilter = new IntentFilter()).addAction("ReceiveOK_DataBase");
		this.mFilter.addAction("database_changed");
		this.registerReceiver(this.recv, this.mFilter);
		super.onCreate(bundle);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		CommonUtil.Log("menu", "MsgEditActivity", "onCreateContextMenu", 'i');
		menu.setHeaderTitle(R.string.options);
		menu.add(0, 2, 2, getResources().getString(R.string.photo_transfer_delete_all));
		menu.add(0, 1, 1, getResources().getString(R.string.photo_transfer_delete));
		if (this.mCursor.getInt(this.mCursor.getColumnIndex("send")) == 1) {
			menu.add(0, 3, 3, getResources().getString(R.string.reupload));
		}
	}

	protected void onDestroy() {
		if (this.mSmsMmsDatabase != null) {
			this.mSmsMmsDatabase.close();
		}
		if (this.mCursor != null) {
			this.mCursor.close();
		}
		if (this.mFilter != null) {
			try {
				unregisterReceiver(this.recv);
			} catch (Exception e) {
				MyLog.e("PhotoTransferSentActivity", "unregister error");
				e.printStackTrace();
			}
		}
		unregisterForContextMenu(this.transfer_sent_list);
		this.delayTaskHandler.removeMessages(2);
		super.onDestroy();
	}

	public boolean onMenuItemSelected(final int n, final MenuItem menuItem) {
		CommonUtil.Log("menu", "ContactActivity", "onMenuItemSelected", 'i');
		final Resources resources = this.getResources();
		switch (menuItem.getItemId()) {
			case 2: {
				DialogUtil.showSelectDialog((Context) this, resources.getString(R.string.photo_transfer_delete_all_title), resources.getString(R.string.photo_transfer_delete_all_message), resources.getString(R.string.photo_transfer_delete_all_ack), (DialogUtil.DialogCallBack) new DialogUtil.DialogCallBack() {
					@Override
					public void onNegativeButtonClick() {
					}

					@Override
					public void onPositiveButtonClick() {
						final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
						final String fetchLocalServer = autoConfigManager.fetchLocalServer();
						final String fetchLocalUserName = autoConfigManager.fetchLocalUserName();
						final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(PhotoTransferSentActivity.this.mContext);
						smsMmsDatabase.delete("message_talk", "type = '" + PhotoTransferSentActivity.this.MMS + "' and mark = 1 and server_ip = '" + fetchLocalServer + "'" + "and local_number = '" + fetchLocalUserName + "'");
//						PhotoTransferSentActivity.access$1(PhotoTransferSentActivity.this, smsMmsDatabase.mQuery("message_talk", "type = '" + PhotoTransferSentActivity.this.MMS + "' and mark = 1 and server_ip = '" + fetchLocalServer + "'" + "and local_number = '" + fetchLocalUserName + "'", null, null));
						PhotoTransferSentActivity.this.mAdapter.changeCursor(PhotoTransferSentActivity.this.mCursor);
						if (PhotoTransferSentActivity.this.mCursor == null || PhotoTransferSentActivity.this.mCursor.getCount() == 0) {
							PhotoTransferSentActivity.this.none_photo_transfer.setVisibility(View.VISIBLE);
						}
						if (smsMmsDatabase != null) {
							smsMmsDatabase.close();
						}
					}
				});
				break;
			}
			case 1: {
				DialogUtil.showSelectDialog((Context) this, resources.getString(R.string.photo_transfer_delete_title), resources.getString(R.string.photo_transfer_delete_message), resources.getString(R.string.photo_transfer_delete_ack), (DialogUtil.DialogCallBack) new DialogUtil.DialogCallBack() {
					@Override
					public void onNegativeButtonClick() {
					}

					@Override
					public void onPositiveButtonClick() {
						final String string = PhotoTransferSentActivity.this.mCursor.getString(PhotoTransferSentActivity.this.mCursor.getColumnIndex("E_id"));
						final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
						final String fetchLocalServer = autoConfigManager.fetchLocalServer();
						final String fetchLocalUserName = autoConfigManager.fetchLocalUserName();
						final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase(PhotoTransferSentActivity.this.mContext);
						smsMmsDatabase.delete("message_talk", "E_id = '" + string + "'");
						PhotoTransferSentActivity.this.mAdapter.changeCursor(PhotoTransferSentActivity.this.mCursor);
						if (PhotoTransferSentActivity.this.mCursor == null || PhotoTransferSentActivity.this.mCursor.getCount() == 0) {
							PhotoTransferSentActivity.this.none_photo_transfer.setVisibility(View.VISIBLE);
						}
						if (smsMmsDatabase != null) {
							smsMmsDatabase.close();
						}
					}
				});
				break;
			}
			case 3: {
				DialogUtil.showSelectDialog((Context) this, resources.getString(R.string.reupload_title), resources.getString(R.string.reupload_message), resources.getString(R.string.reupload_ack), (DialogUtil.DialogCallBack) new DialogUtil.DialogCallBack() {
					@Override
					public void onNegativeButtonClick() {
					}

					@Override
					public void onPositiveButtonClick() {
						new Thread(new Runnable() {
							@Override
							public void run() {
								final String string = PhotoTransferSentActivity.this.mCursor.getString(PhotoTransferSentActivity.this.mCursor.getColumnIndex("E_id"));
								final String string2 = PhotoTransferSentActivity.this.mCursor.getString(PhotoTransferSentActivity.this.mCursor.getColumnIndex("address"));
								final String string3 = PhotoTransferSentActivity.this.mCursor.getString(PhotoTransferSentActivity.this.mCursor.getColumnIndex("body"));
								final Uri parse = Uri.parse(PhotoTransferSentActivity.this.mCursor.getString(PhotoTransferSentActivity.this.mCursor.getColumnIndex("attachment")));
								MessageSender.setSendDataId(string);
								new MessageSender(PhotoTransferSentActivity.this.mContext, string2, string3, parse, "image/jpg", String.valueOf(string.substring(3, 12)) + ".jpg", string).reUploadPhoto(string);
							}
						}).start();
					}
				});
				break;
			}
		}
		return true;
	}
}
