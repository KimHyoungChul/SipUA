package com.zed3.asynctask;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.zed3.bitmap.BitmapUtil;

public class LoadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
	private int dstHeight;
	private int dstWidth;
	private ImageView imageView;
	private LoadImageCallback mLoadImageCallback;

	public interface LoadImageCallback {
		void afterImageLoad(Bitmap bitmap);

		void beforeImageLoad();
	}

	public LoadImageAsyncTask(int dstWidth, int dstHeight, LoadImageCallback mLoadImageCallback) {
		this.mLoadImageCallback = mLoadImageCallback;
		this.dstWidth = dstWidth;
		this.dstHeight = dstHeight;
	}

	public LoadImageAsyncTask(ImageView imageView, int dstWidth, int dstHeight) {
		this.imageView = imageView;
		this.dstWidth = dstWidth;
		this.dstHeight = dstHeight;
	}

	protected Bitmap doInBackground(String... params) {
		try {
			Bitmap bitmap = new BitmapUtil().loadBitMap(this.dstWidth, this.dstHeight, params[0]);
			if (this.imageView == null) {
				return bitmap;
			}
			this.imageView.setImageBitmap(bitmap);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void onPreExecute() {
		if (this.mLoadImageCallback != null) {
			this.mLoadImageCallback.beforeImageLoad();
		}
		super.onPreExecute();
	}

	protected void onPostExecute(Bitmap result) {
		if (this.mLoadImageCallback != null) {
			this.mLoadImageCallback.afterImageLoad(result);
		}
		super.onPostExecute(result);
	}
}
