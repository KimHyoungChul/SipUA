package com.zed3.sipua.message;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.sipua.welcome.AutoConfigManager;
import com.zed3.utils.LogUtil;
import com.zed3.zhejiang.ZhejiangReceivier;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PhotoTransferReceiveActivity extends Activity {
	public static final String ACTION_READ_MMS = "com.zed3.action.READ_MMS";
	public static final String ACTION_RECEIVE_MMS = "com.zed3.action.RECEIVE_MMS";
	private static final String LOG_TAG;
	private static final int ON_CLEAR_DATASET = 4;
	private static final int ON_DATASET_CHANGED = 1;
	private static final int ON_DATASET_LOADED = 2;
	private static PhotoTransferReceiveActivity sInstance;
	private InnerHanler mInnerHanler;
	private PhotoReceiveAdater mPhotoReceiveAdapter;
	private ListView mPhotoReceiveListView;
	TextView none_photo_transfer;

	static {
		LOG_TAG = PhotoTransferActivity.class.getSimpleName();
	}

	public PhotoTransferReceiveActivity() {
		this.mPhotoReceiveAdapter = new PhotoReceiveAdater();
		this.mInnerHanler = new InnerHanler();
	}

	public static PhotoTransferReceiveActivity getInstance() {
		return PhotoTransferReceiveActivity.sInstance;
	}

	private void loadData(final Context context) {
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				SmsMmsDatabase database = new SmsMmsDatabase(context);
				AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
				String server_ip = autoConfigManager.fetchLocalServer();
				String local_number = autoConfigManager.fetchLocalUserName();
				Cursor c = database.mQuery(SmsMmsDatabase.TABLE_MESSAGE_TALK, "type = 'mms' and mark = 0 and server_ip = '" + server_ip + "'" + "and local_number = '" + local_number + "'", null, "date desc");
				ArrayList<PhotoReceiveMessage> list = new ArrayList();
				while (c.moveToNext()) {
					String E_id = c.getString(c.getColumnIndex("E_id"));
					String body = c.getString(c.getColumnIndex(MmsMessageDetailActivity.MESSAGE_BODY));
					String attachment = c.getString(c.getColumnIndex("attachment"));
					String status = c.getString(c.getColumnIndex(ZhejiangReceivier.STATUS));
					String sipName = c.getString(c.getColumnIndex("sip_name"));
					String type = c.getString(c.getColumnIndex("type"));
					String mark = c.getString(c.getColumnIndex("mark"));
					String attachmentName = c.getString(c.getColumnIndex("attachment_name"));
					String date = c.getString(c.getColumnIndex("date"));
					Log.i(PhotoTransferReceiveActivity.LOG_TAG, "attachment name = " + attachmentName + " , " + "date = " + date + " , " + "E_id = " + E_id + " , " + "body = " + body + " , " + "attachment = " + attachment + " , " + "status = " + status + " , " + "type = " + type + " , " + "mark = " + mark);
					PhotoReceiveMessage message = new PhotoReceiveMessage();
					message.mEId = E_id;
					message.mBody = body;
					message.mPhotoPath = attachment;
					message.mSipName = sipName;
					message.mReceiveTime = date;
					list.add(list.size(), message);
				}
				if (c != null) {
					c.close();
				}
				PhotoTransferReceiveActivity.this.mPhotoReceiveAdapter.setList(list);
				PhotoTransferReceiveActivity.this.mInnerHanler.sendEmptyMessage(2);
				return null;
			}
		}.execute(new Void[0]);
	}

	private void showNoDataTip() {
		if (this.none_photo_transfer != null) {
			if (this.mPhotoReceiveAdapter.getCount() != 0) {
				this.none_photo_transfer.setVisibility(View.GONE);
				return;
			}
			this.none_photo_transfer.setVisibility(View.VISIBLE);
		}
	}

	private void testAdapter() {
		new Handler().postDelayed((Runnable) new Runnable() {
			@Override
			public void run() {
				Log.i("dwtag", "run enter");
				final ArrayList<PhotoReceiveMessage> list = new ArrayList<PhotoReceiveMessage>();
				for (int i = 0; i < 10; ++i) {
					final PhotoReceiveMessage photoReceiveMessage = new PhotoReceiveMessage();
					photoReceiveMessage.mBody = "body = " + i;
					photoReceiveMessage.mPhotoPath = "/sdcard/smsmms/1405310837171.jpg";
					list.add(photoReceiveMessage);
				}
				PhotoTransferReceiveActivity.this.mPhotoReceiveAdapter.setList(list);
				PhotoTransferReceiveActivity.this.mInnerHanler.sendEmptyMessage(2);
				Log.i("dwtag", "run exit");
			}
		}, 2000L);
	}

	private void updateMmsStatus() {
		final SmsMmsDatabase smsMmsDatabase = new SmsMmsDatabase((Context) this);
		final AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
		final String fetchLocalServer = autoConfigManager.fetchLocalServer();
		final String fetchLocalUserName = autoConfigManager.fetchLocalUserName();
		final ContentValues contentValues = new ContentValues();
		contentValues.put("status", 1);
		smsMmsDatabase.update("message_talk", "type = 'mms' and mark = 0 and status = 0 and server_ip = '" + fetchLocalServer + "'" + "and local_number = '" + fetchLocalUserName + "'", contentValues);
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		Log.i(PhotoTransferReceiveActivity.LOG_TAG, "PhotoTransferReceiveActivity#onCreate enter");
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_photo_transfer_receive);
		(PhotoTransferReceiveActivity.sInstance = this).updateMmsStatus();
		this.sendReadMms();
		(this.mPhotoReceiveListView = (ListView) this.findViewById(R.id.photo_transfer_receive_lstview)).setAdapter((ListAdapter) this.mPhotoReceiveAdapter);
		this.mPhotoReceiveListView.setOnScrollListener(this.mPhotoReceiveAdapter.getOnScrollListener());
		this.none_photo_transfer = (TextView) this.findViewById(R.id.none_photo_transfer);
		this.mPhotoReceiveListView.setOnItemClickListener((AdapterView.OnItemClickListener) new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				final Object item = PhotoTransferReceiveActivity.this.mPhotoReceiveAdapter.getItem(n);
				if (item != null) {
					final PhotoReceiveMessage photoReceiveMessage = (PhotoReceiveMessage) item;
					final String mPhotoPath = photoReceiveMessage.mPhotoPath;
					final Intent intent = new Intent((Context) PhotoTransferReceiveActivity.this, (Class) MmsMessageDetailActivity.class);
					intent.putExtra("body", photoReceiveMessage.mBody);
					intent.putExtra("pic_path", mPhotoPath);
					PhotoTransferReceiveActivity.this.startActivity(intent);
				}
			}
		});
		this.mPhotoReceiveListView.setOnItemLongClickListener((AdapterView.OnItemLongClickListener) new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int n, final long n2) {
				PhotoTransferReceiveActivity.this.showSelectDialog(PhotoTransferReceiveActivity.this.getString(R.string.options), n);
				return true;
			}
		});
		this.findViewById(R.id.btn_home_photo).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				PhotoTransferReceiveActivity.this.finish();
			}
		});
		this.loadData((Context) PhotoTransferReceiveActivity.sInstance);
		Log.i(PhotoTransferReceiveActivity.LOG_TAG, "PhotoTransferReceiveActivity#onCreate exit");
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.i(PhotoTransferReceiveActivity.LOG_TAG, "PhotoTransferReceiveActivity#onDestroy enter");
		PhotoTransferReceiveActivity.sInstance = null;
		Log.i(PhotoTransferReceiveActivity.LOG_TAG, "PhotoTransferReceiveActivity#onDestroy exit");
	}

	public void sendReadMms() {
		this.sendBroadcast(new Intent("com.zed3.action.READ_MMS"));
	}

	class C11266 implements OnKeyListener {
		C11266() {
		}

		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			switch (keyCode) {
				case 4:
					dialog.dismiss();
					break;
			}
			return false;
		}
	}

	public synchronized void showSelectDialog(String title, final int pos) {
		Builder dialog = new Builder(this);
		dialog.setTitle(title);
		dialog.setOnKeyListener(new C11266());
		dialog.setItems(new String[]{getResources().getString(R.string.delete_message_one), getResources().getString(R.string.delete_all)}, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					ArrayList<PhotoReceiveMessage> list = PhotoTransferReceiveActivity.this.mPhotoReceiveAdapter.getList();
					if (list != null) {
						PhotoReceiveMessage message = (PhotoReceiveMessage) list.get(pos);
						list.remove(pos);
						File file = new File(message.mPhotoPath);
						if (file.exists()) {
							file.delete();
						}
						String eId = message.mEId;
						new SmsMmsDatabase(PhotoTransferReceiveActivity.this).delete(SmsMmsDatabase.TABLE_MESSAGE_TALK, "type = 'mms' and mark = 0 and E_id = '" + eId + "'");
					}
					PhotoTransferReceiveActivity.this.mPhotoReceiveAdapter.notifyDataSetChanged();
				} else if (which == 1) {
					SmsMmsDatabase database = new SmsMmsDatabase(PhotoTransferReceiveActivity.this);
					AutoConfigManager autoConfigManager = new AutoConfigManager(SipUAApp.getAppContext());
					String server_ip = autoConfigManager.fetchLocalServer();
					database.delete(SmsMmsDatabase.TABLE_MESSAGE_TALK, "type = 'mms' and mark = 0 and server_ip = '" + server_ip + "'" + "and local_number = '" + autoConfigManager.fetchLocalUserName() + "'");
					PhotoTransferReceiveActivity.this.mInnerHanler.sendEmptyMessage(4);
				}
			}
		}).show();
	}

	private final class InnerHanler extends Handler {
		public void handleMessage(final Message message) {
			final int what = message.what;
			if (what != 0) {
				final Object obj = message.obj;
				if (obj != null) {
					PhotoTransferReceiveActivity.this.mPhotoReceiveAdapter.addItem((PhotoReceiveMessage) obj);
					PhotoTransferReceiveActivity.this.showNoDataTip();
					PhotoTransferReceiveActivity.this.mPhotoReceiveAdapter.notifyDataSetChanged();
				}
			} else {
				if (2 == what) {
					PhotoTransferReceiveActivity.this.showNoDataTip();
					PhotoTransferReceiveActivity.this.mPhotoReceiveAdapter.notifyDataSetChanged();
					return;
				}
				if (4 == what) {
					PhotoTransferReceiveActivity.this.mPhotoReceiveAdapter.getList().clear();
					PhotoTransferReceiveActivity.this.mPhotoReceiveAdapter.notifyDataSetChanged();
					PhotoTransferReceiveActivity.this.showNoDataTip();
				}
			}
		}
	}

	public class PhotoReceiveAdater extends BaseAdapter {
		protected static final String TAG = "PhotoReceiveAdater";
		private Map<String, SoftReference<Bitmap>> bitmapCache;
		protected boolean flinging;
		private ArrayList<PhotoReceiveMessage> mList;
		AbsListView.OnScrollListener onScrollListener;
		protected boolean scrolling;

		public PhotoReceiveAdater() {
			this.bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
			this.mList = new ArrayList<PhotoReceiveMessage>();
			this.onScrollListener = (AbsListView.OnScrollListener) new AbsListView.OnScrollListener() {
				StringBuilder builder = new StringBuilder();
				private int firstVisibleItem;
				private int totalItemCount;
				private int visibleItemCount;

				private void clearBuilder() {
					if (this.builder.length() > 0) {
						this.builder.delete(0, this.builder.length());
					}
				}

				public void onScroll(final AbsListView absListView, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
					this.clearBuilder();
					this.firstVisibleItem = firstVisibleItem;
					this.visibleItemCount = visibleItemCount;
					this.totalItemCount = totalItemCount;
					this.builder.append(" firstVisibleItem/visibleItemCount/totalItemCount : " + firstVisibleItem + "/" + visibleItemCount + "/" + totalItemCount);
					LogUtil.makeLog("PhotoReceiveAdater", this.builder.toString());
				}

				public void onScrollStateChanged(final AbsListView absListView, int i) {
					this.clearBuilder();
					this.builder.append("OnScrollListener#onScrollStateChanged()");
					final ListView listView = (ListView) absListView;
					switch (i) {
						case 2: {
							this.builder.append(" SCROLL_STATE_FLING");
							PhotoReceiveAdater.this.flinging = true;
							break;
						}
						case 0: {
							this.builder.append(" SCROLL_STATE_IDLE");
							LogUtil.makeLog("PhotoReceiveAdater", this.builder.toString());
							PhotoReceiveAdater.this.flinging = false;
							PhotoReceiveAdater.this.scrolling = false;
							i = listView.getFirstVisiblePosition();
							final int visibleItemCount = this.visibleItemCount;
							this.builder.append(" getFirstVisiblePosition()/count " + i + "/" + this.visibleItemCount);
							this.builder.append(" firstVisibleItem/visibleItemCount/totalItemCount : " + this.firstVisibleItem + "/" + this.visibleItemCount + "/" + this.totalItemCount);
							ViewHolder viewHolder;
							String[] split;
							int n;
							for (i = 0; i < this.visibleItemCount; ++i) {
								this.builder.append(" realPostion " + (this.firstVisibleItem + i));
								viewHolder = (ViewHolder) listView.getChildAt(i).getTag();
								split = ((String) viewHolder.item_transfer_imv.getTag()).split("://");
								if (split.length == 1) {
									n = 0;
								} else {
									n = 1;
								}
								PhotoReceiveAdater.this.loadImage(viewHolder, split[n]);
							}
							break;
						}
						case 1: {
							this.builder.append(" SCROLL_STATE_TOUCH_SCROLL");
							PhotoReceiveAdater.this.scrolling = true;
							break;
						}
					}
					LogUtil.makeLog("PhotoReceiveAdater", this.builder.toString());
				}
			};
		}

		public void addItem(final PhotoReceiveMessage photoReceiveMessage) {
			this.mList.add(photoReceiveMessage);
		}

		public int getCount() {
			return this.mList.size();
		}

		public Object getItem(final int n) {
			return this.mList.get(n);
		}

		public long getItemId(final int n) {
			return 0L;
		}

		public ArrayList<PhotoReceiveMessage> getList() {
			return this.mList;
		}

		public AbsListView.OnScrollListener getOnScrollListener() {
			return this.onScrollListener;
		}

		public View getView(int n, final View view, final ViewGroup viewGroup) {
			final int n2 = 1;
			View view2 = view;
			if (view == null) {
				view2 = this.newView((Context) PhotoTransferReceiveActivity.this, viewGroup);
			}
			final PhotoReceiveMessage photoReceiveMessage = this.mList.get(n);
			final ViewHolder viewHolder = (ViewHolder) view2.getTag();
			viewHolder.item_transfer_tv_content.setText((CharSequence) photoReceiveMessage.mBody);
			viewHolder.item_transfer_tv_time.setText((CharSequence) photoReceiveMessage.mReceiveTime);
			String s;
			if (TextUtils.isEmpty((CharSequence) (s = GroupListUtil.getUserName(photoReceiveMessage.mSipName)))) {
				s = photoReceiveMessage.mSipName;
			}
			viewHolder.item_transfer_tv_person.setText((CharSequence) (String.valueOf(PhotoTransferReceiveActivity.this.getResources().getString(R.string.photo_receive)) + s));
			final String mPhotoPath = photoReceiveMessage.mPhotoPath;
			viewHolder.item_transfer_imv.setTag((Object) mPhotoPath);
			viewHolder.item_transfer_imv.setImageBitmap((Bitmap) null);
			final String[] split = mPhotoPath.split("://");
			n = n2;
			if (split.length == 1) {
				n = 0;
			}
			this.loadImage(viewHolder, split[n]);
			return view2;
		}

		protected void loadImage(final ViewHolder viewHolder, final String s) {
			while (true) {
				StringBuilder sb = null;
				Label_0130:
				while (true) {
					synchronized (this) {
						sb = new StringBuilder(" loadImage()");
						final SoftReference<Bitmap> softReference = this.bitmapCache.get(s);
						if (softReference != null) {
							final Bitmap imageBitmap = softReference.get();
							if (imageBitmap != null) {
								viewHolder.item_transfer_imv.setImageBitmap(imageBitmap);
								sb.append(" use cache bitmap");
								LogUtil.makeLog("PhotoReceiveAdater", sb.toString());
							} else {
								sb.append(" bitmap is null");
								if (!this.flinging && !this.scrolling) {
									break Label_0130;
								}
								sb.append(" scrolling load later");
								LogUtil.makeLog("PhotoReceiveAdater", sb.toString());
							}
							return;
						}
					}
					sb.append(" reference is null");
					continue;
				}
				sb.append(" use new bitmap");
//				final ViewHolder viewHolder2 = null;
//				new LoadImageAsyncTask(200, 200, (LoadImageAsyncTask.LoadImageCallback) new LoadImageAsyncTask.LoadImageCallback() {
//					@Override
//					public void afterImageLoad(final Bitmap imageBitmap) {
//						if (imageBitmap != null) {
//							viewHolder2.item_transfer_imv.setImageBitmap(imageBitmap);
//							PhotoReceiveAdater.this.bitmapCache.put(s, new SoftReference<Bitmap>(imageBitmap));
//							return;
//						}
//						viewHolder2.item_transfer_imv.setImageBitmap((Bitmap) null);
//					}
//
//					@Override
//					public void beforeImageLoad() {
//					}
//				}).execute((Object[]) new String[]{s});
				LogUtil.makeLog("PhotoReceiveAdater", sb.toString());
			}
		}

		public View newView(final Context context, final ViewGroup viewGroup) {
			final View inflate = LayoutInflater.from(context).inflate(R.layout.aa_transfer_item, (ViewGroup) null);
			final ViewHolder tag = new ViewHolder();
			tag.item_transfer_tv_content = (TextView) inflate.findViewById(R.id.item_transfer_tv_content);
			tag.item_transfer_tv_time = (TextView) inflate.findViewById(R.id.item_transfer_tv_time);
			tag.item_transfer_imv = (ImageView) inflate.findViewById(R.id.item_transfer_imv);
			tag.item_transfer_tv_person = (TextView) inflate.findViewById(R.id.item_transfer_tv_person);
			tag.item_transfer_tv_sent = (TextView) inflate.findViewById(R.id.item_transfer_tv_sent);
			inflate.setTag((Object) tag);
			tag.item_transfer_tv_sent.setVisibility(View.GONE);
			return inflate;
		}

		public void setList(final ArrayList<PhotoReceiveMessage> mList) {
			if (mList != null) {
				this.mList = mList;
			}
		}
	}

	public static class PhotoReceiveMessage {
		public String mBody;
		public String mEId;
		public String mPhotoPath;
		public String mReceiveTime;
		public String mSipName;

		public void sendToTarget() {
			PhotoTransferReceiveActivity instance = PhotoTransferReceiveActivity.getInstance();
			if (instance != null) {
				Handler handler = instance.mInnerHanler;
				Message msg = handler.obtainMessage();
				msg.what = 1;
				msg.obj = this;
				Log.i("xxxx", "PhotoReceiveMessage#sendToTarget enter");
				handler.sendMessage(msg);
			}
		}
	}

	private static class ViewHolder {
		ImageView item_transfer_imv;
		TextView item_transfer_tv_content;
		TextView item_transfer_tv_person;
		TextView item_transfer_tv_sent;
		TextView item_transfer_tv_time;

		private ViewHolder() {
		}
	}
}
