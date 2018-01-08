package com.zed3.sipua.phone;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Contacts;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;

public class ContactsAsyncHelper extends Handler {
	private static final boolean DBG = false;
	private static final int DEFAULT_TOKEN = -1;
	private static final int EVENT_LOAD_IMAGE = 1;
	private static final String LOG_TAG = "ContactsAsyncHelper";
	private static ContactsAsyncHelper sInstance;
	private static Handler sThreadHandler;

	static {
		ContactsAsyncHelper.sInstance = new ContactsAsyncHelper();
	}

	private ContactsAsyncHelper() {
		final HandlerThread handlerThread = new HandlerThread("ContactsAsyncWorker");
		handlerThread.start();
		ContactsAsyncHelper.sThreadHandler = new WorkerHandler(handlerThread.getLooper());
	}

	public static final void updateImageViewWithContactPhotoAsync(final Context context, final ImageView imageView, final Uri uri, final int n) {
		updateImageViewWithContactPhotoAsync(null, -1, null, null, context, imageView, uri, n);
	}

	public static final void updateImageViewWithContactPhotoAsync(final CallerInfo info, final int n, final OnImageLoadCompleteListener listener, final Object cookie, final Context context, final ImageView view, final Uri uri, final int imageResource) {
		if (uri == null) {
			view.setVisibility(View.VISIBLE);
			view.setImageResource(imageResource);
			return;
		}
		final WorkerArgs obj = new WorkerArgs();
		obj.cookie = cookie;
		obj.context = context;
		obj.view = view;
		obj.uri = uri;
		obj.defaultResource = imageResource;
		obj.listener = listener;
		obj.info = info;
		final Message obtainMessage = ContactsAsyncHelper.sThreadHandler.obtainMessage(n);
		obtainMessage.arg1 = 1;
		obtainMessage.obj = obj;
		if (imageResource != -1) {
			view.setVisibility(View.VISIBLE);
			view.setImageResource(imageResource);
		} else {
			view.setVisibility(View.INVISIBLE);
		}
		ContactsAsyncHelper.sThreadHandler.sendMessage(obtainMessage);
	}

	public static final void updateImageViewWithContactPhotoAsync(final CallerInfo callerInfo, final Context context, final ImageView imageView, final Uri uri, final int n) {
		updateImageViewWithContactPhotoAsync(callerInfo, -1, null, null, context, imageView, uri, n);
	}

	public void handleMessage(final Message message) {
		final WorkerArgs workerArgs = (WorkerArgs) message.obj;
		switch (message.arg1) {
			case 1: {
				final boolean b = false;
				boolean b2;
				if (workerArgs.result != null) {
					workerArgs.view.setVisibility(View.VISIBLE);
					workerArgs.view.setImageDrawable((Drawable) workerArgs.result);
					if (workerArgs.info != null) {
						workerArgs.info.cachedPhoto = (Drawable) workerArgs.result;
					}
					b2 = true;
				} else {
					b2 = b;
					if (workerArgs.defaultResource != -1) {
						workerArgs.view.setVisibility(View.VISIBLE);
						workerArgs.view.setImageResource(workerArgs.defaultResource);
						b2 = b;
					}
				}
				if (workerArgs.info != null) {
					workerArgs.info.isCachedPhotoCurrent = true;
				}
				if (workerArgs.listener != null) {
					workerArgs.listener.onImageLoadComplete(message.what, workerArgs.cookie, workerArgs.view, b2);
					return;
				}
				break;
			}
		}
	}

	public static class ImageTracker {
		public static final int DISPLAY_DEFAULT = -2;
		public static final int DISPLAY_IMAGE = -1;
		public static final int DISPLAY_UNDEFINED = 0;
		private int displayMode;
		private CallerInfo mCurrentCallerInfo;

		public ImageTracker() {
			this.mCurrentCallerInfo = null;
			this.displayMode = 0;
		}

		public int getPhotoState() {
			return this.displayMode;
		}

		public Uri getPhotoUri() {
			if (this.mCurrentCallerInfo != null) {
				return ContentUris.withAppendedId(Contacts.People.CONTENT_URI, this.mCurrentCallerInfo.person_id);
			}
			return null;
		}

		public boolean isDifferentImageRequest(final CallerInfo callerInfo) {
			return this.mCurrentCallerInfo != callerInfo;
		}

		public boolean isDifferentImageRequest(final Connection connection) {
			if (connection == null) {
				if (this.mCurrentCallerInfo == null) {
					return false;
				}
			} else {
				final Object userData = connection.getUserData();
				if (userData instanceof CallerInfo) {
					return this.isDifferentImageRequest((CallerInfo) userData);
				}
			}
			return true;
		}

		public void setPhotoRequest(final CallerInfo mCurrentCallerInfo) {
			this.mCurrentCallerInfo = mCurrentCallerInfo;
		}

		public void setPhotoState(final int displayMode) {
			this.displayMode = displayMode;
		}
	}

	public interface OnImageLoadCompleteListener {
		void onImageLoadComplete(final int p0, final Object p1, final ImageView p2, final boolean p3);
	}

	private static final class WorkerArgs {
		public Context context;
		public Object cookie;
		public int defaultResource;
		public CallerInfo info;
		public OnImageLoadCompleteListener listener;
		public Object result;
		public Uri uri;
		public ImageView view;
	}

	private class WorkerHandler extends Handler {
		public WorkerHandler(final Looper looper) {
			super(looper);
		}

		public void handleMessage(final Message message) {
			final WorkerArgs workerArgs = (WorkerArgs) message.obj;
			switch (message.arg1) {
				case 1: {
					final InputStream openContactPhotoInputStream = Contacts.People.openContactPhotoInputStream(workerArgs.context.getContentResolver(), workerArgs.uri);
					if (openContactPhotoInputStream != null) {
						workerArgs.result = Drawable.createFromStream(openContactPhotoInputStream, workerArgs.uri.toString());
						break;
					}
					workerArgs.result = null;
					break;
				}
			}
			final Message obtainMessage = ContactsAsyncHelper.this.obtainMessage(message.what);
			obtainMessage.arg1 = message.arg1;
			obtainMessage.obj = message.obj;
			obtainMessage.sendToTarget();
		}
	}
}
