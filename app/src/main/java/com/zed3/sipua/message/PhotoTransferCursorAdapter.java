package com.zed3.sipua.message;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zed3.sipua.R;
import com.zed3.sipua.SipUAApp;
import com.zed3.sipua.ui.lowsdk.GroupListUtil;
import com.zed3.utils.LogUtil;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class PhotoTransferCursorAdapter extends CursorAdapter {
	private String TAG;
	private int address;
	private String addressColumnName;
	private String addressTypeStr;
	private StringBuilder bindViewbuilder;
	private Map<String, SoftReference<Bitmap>> bitmapCache;
	protected boolean flinging;
	private Context mContext;
	private int messageType;
	AbsListView.OnScrollListener onScrollListener;
	protected boolean scrolling;

	public PhotoTransferCursorAdapter(final Context mContext, final Cursor cursor, int messageType) {
		super(mContext, cursor);
		this.TAG = "PhotoTransferCursorAdapter";
		this.bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
		this.bindViewbuilder = new StringBuilder();
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
				LogUtil.makeLog(PhotoTransferCursorAdapter.this.TAG, this.builder.toString());
			}

			public void onScrollStateChanged(final AbsListView absListView, int i) {
				this.clearBuilder();
				this.builder.append("OnScrollListener#onScrollStateChanged()");
				final ListView listView = (ListView) absListView;
				switch (i) {
					case 2: {
						this.builder.append(" SCROLL_STATE_FLING");
						PhotoTransferCursorAdapter.this.flinging = true;
						break;
					}
					case 0: {
						this.builder.append(" SCROLL_STATE_IDLE");
						LogUtil.makeLog(PhotoTransferCursorAdapter.this.TAG, this.builder.toString());
						PhotoTransferCursorAdapter.this.flinging = false;
						PhotoTransferCursorAdapter.this.scrolling = false;
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
							PhotoTransferCursorAdapter.this.loadImage(viewHolder, split[n]);
						}
						break;
					}
					case 1: {
						this.builder.append(" SCROLL_STATE_TOUCH_SCROLL");
						PhotoTransferCursorAdapter.this.scrolling = true;
						break;
					}
				}
				LogUtil.makeLog(PhotoTransferCursorAdapter.this.TAG, this.builder.toString());
			}
		};
		this.messageType = messageType;
		this.mContext = mContext;
		String addressColumnName;
		if (messageType == 1) {
			addressColumnName = "address";
		} else {
			addressColumnName = "sip_name";
		}
		this.addressColumnName = addressColumnName;
		final Resources resources = mContext.getResources();
		if (messageType == 1) {
			messageType = R.string.send_to;
		} else {
			messageType = R.string.photo_receive;
		}
		this.addressTypeStr = resources.getString(messageType);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		this.bindViewbuilder = new StringBuilder();
		if (this.bindViewbuilder.length() > 0) {
			this.bindViewbuilder.delete(0, this.bindViewbuilder.length());
		}
		this.bindViewbuilder.append(" bindView()");
		this.bindViewbuilder.append(" position " + cursor.getPosition());
		final String string = cursor.getString(cursor.getColumnIndex(this.addressColumnName));
		String userName;
		if (TextUtils.isEmpty((CharSequence) (userName = GroupListUtil.getUserName(string)))) {
			userName = string;
		}
		final String string2 = cursor.getString(cursor.getColumnIndex("attachment"));
		final String string3 = cursor.getString(cursor.getColumnIndex("body"));
		final String string4 = cursor.getString(cursor.getColumnIndex("date"));
		final ViewHolder viewHolder = (ViewHolder) view.getTag();
		viewHolder.item_transfer_tv_person.setText((CharSequence) (String.valueOf(this.addressTypeStr) + userName));
		if (this.messageType == 1) {
			final int int1 = cursor.getInt(cursor.getColumnIndex("send"));
			if (int1 == 0) {
				viewHolder.item_transfer_tv_sent.setText((CharSequence) (String.valueOf(context.getResources().getString(R.string.status_title)) + context.getResources().getString(R.string.uploaded)));
			} else if (int1 == 2) {
				viewHolder.item_transfer_tv_sent.setText((CharSequence) (String.valueOf(context.getResources().getString(R.string.status_title)) + context.getResources().getString(R.string.uploading)));
			} else if (int1 == 1) {
				viewHolder.item_transfer_tv_sent.setText((CharSequence) (String.valueOf(context.getResources().getString(R.string.status_title)) + context.getResources().getString(R.string.upload_failed)));
			} else if (int1 == 3) {
				viewHolder.item_transfer_tv_sent.setText((CharSequence) (String.valueOf(context.getResources().getString(R.string.status_title)) + context.getResources().getString(R.string.upload_finished)));
			} else if (int1 == 4) {
				viewHolder.item_transfer_tv_sent.setText((CharSequence) (String.valueOf(context.getResources().getString(R.string.status_title)) + context.getResources().getString(R.string.upload_offline_space_full)));
			}
		}
		String string5 = null;
		Label_0307:
		{
			if (string3 != null) {
				string5 = string3;
				if (string3.length() != 0) {
					break Label_0307;
				}
			}
			string5 = SipUAApp.mContext.getResources().getString(R.string.nothing_write);
		}
		viewHolder.item_transfer_tv_content.setText((CharSequence) string5);
		viewHolder.item_transfer_tv_time.setText((CharSequence) string4);
		viewHolder.item_transfer_imv.setTag((Object) string2);
		viewHolder.item_transfer_imv.setImageBitmap((Bitmap) null);
		final String[] split = string2.split("://");
		int n;
		if (split.length == 1) {
			n = 0;
		} else {
			n = 1;
		}
		this.loadImage(viewHolder, split[n]);
		LogUtil.makeLog(this.TAG, this.bindViewbuilder.toString());
	}

	@Override
	public void changeCursor(final Cursor cursor) {
		super.changeCursor(cursor);
	}

	public AbsListView.OnScrollListener getOnScrollListener() {
		return this.onScrollListener;
	}

	protected void loadImage(final ViewHolder viewHolder, final String s) {
		while (true) {
			StringBuilder sb = null;
			Label_0134:
			while (true) {
				synchronized (this) {
					sb = new StringBuilder(" loadImage()");
					final SoftReference<Bitmap> softReference = this.bitmapCache.get(s);
					if (softReference != null) {
						final Bitmap imageBitmap = softReference.get();
						if (imageBitmap != null) {
							viewHolder.item_transfer_imv.setImageBitmap(imageBitmap);
							sb.append(" use cache bitmap");
							LogUtil.makeLog(this.TAG, sb.toString());
						} else {
							sb.append(" bitmap is null");
							if (!this.flinging && !this.scrolling) {
								break Label_0134;
							}
							sb.append(" scrolling load later");
							LogUtil.makeLog(this.TAG, sb.toString());
						}
						return;
					}
				}
				sb.append(" reference is null");
				continue;
			}
			sb.append(" use new bitmap");
//			final ViewHolder viewHolder2;
//			new LoadImageAsyncTask(200, 200, (LoadImageAsyncTask.LoadImageCallback) new LoadImageAsyncTask.LoadImageCallback() {
//				@Override
//				public void afterImageLoad(final Bitmap imageBitmap) {
//					if (imageBitmap != null) {
//						viewHolder2.item_transfer_imv.setImageBitmap(imageBitmap);
//						PhotoTransferCursorAdapter.this.bitmapCache.put(s, new SoftReference<Bitmap>(imageBitmap));
//						return;
//					}
//					viewHolder2.item_transfer_imv.setImageBitmap((Bitmap) null);
//				}
//
//				@Override
//				public void beforeImageLoad() {
//				}
//			}).execute((Object[]) new String[]{s});
			LogUtil.makeLog(this.TAG, sb.toString());
		}
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup viewGroup) {
		final View inflate = LayoutInflater.from(context).inflate(R.layout.aa_transfer_item, (ViewGroup) null);
		final ViewHolder tag = new ViewHolder();
		tag.item_transfer_tv_content = (TextView) inflate.findViewById(R.id.item_transfer_tv_content);
		tag.item_transfer_tv_time = (TextView) inflate.findViewById(R.id.item_transfer_tv_time);
		tag.item_transfer_imv = (ImageView) inflate.findViewById(R.id.item_transfer_imv);
		tag.item_transfer_tv_person = (TextView) inflate.findViewById(R.id.item_transfer_tv_person);
		tag.item_transfer_tv_sent = (TextView) inflate.findViewById(R.id.item_transfer_tv_sent);
		inflate.setTag((Object) tag);
		final TextView item_transfer_tv_sent = tag.item_transfer_tv_sent;
		int visibility;
		if (this.messageType == 1) {
			visibility = 0;
		} else {
			visibility = 8;
		}
		item_transfer_tv_sent.setVisibility(visibility);
		return inflate;
	}

	public void setSelectItem(final int n) {
	}

	private static class ViewHolder {
		ImageView item_transfer_imv;
		TextView item_transfer_tv_content;
		TextView item_transfer_tv_person;
		TextView item_transfer_tv_sent;
		TextView item_transfer_tv_time;
	}
}
