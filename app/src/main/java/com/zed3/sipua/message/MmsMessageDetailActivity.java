package com.zed3.sipua.message;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.sipua.R;

import java.io.File;

public class MmsMessageDetailActivity extends Activity {
	public static final String MESSAGE_BODY = "body";
	public static final String MESSAGE_PIC_PATH = "pic_path";

	private Bitmap getBitmap(String imageFilePath) {
		Options options = new Options();
		options.inJustDecodeBounds = true;
		if (BitmapFactory.decodeFile(imageFilePath, options) == null) {
			System.out.println("bitmap为空");
		}
		float realWidth = (float) options.outWidth;
		float realHeight = (float) options.outHeight;
		System.out.println("真实图片高度：" + realHeight + "宽度:" + realWidth);
		if (realHeight <= realWidth) {
			realHeight = realWidth;
		}
		int scale = (int) (realHeight / 100.0f);
		if (scale <= 0) {
			scale = 1;
		}
		options.inSampleSize = scale;
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(imageFilePath, options);
	}

	protected void onCreate(final Bundle bundle) {
		super.onCreate(bundle);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_mms_message_detail);
		final Intent intent = this.getIntent();
		final String stringExtra = intent.getStringExtra("body");
		final String stringExtra2 = intent.getStringExtra("pic_path");
		if (stringExtra != null) {
			((TextView) this.findViewById(R.id.message_body)).setText((CharSequence) stringExtra);
		}
		if (stringExtra2 != null) {
			final Bitmap bitmap = this.getBitmap(stringExtra2);
			if (bitmap == null) {
				Toast.makeText((Context) this, (CharSequence) "图片已损坏", Toast.LENGTH_SHORT).show();
				this.finish();
			}
			final ImageView imageView = (ImageView) this.findViewById(R.id.message_pic);
			imageView.setImageBitmap(bitmap);
			imageView.setOnClickListener((View.OnClickListener) new View.OnClickListener() {
				public void onClick(final View view) {
					final Intent intent = new Intent("android.intent.action.VIEW");
					intent.setDataAndType(Uri.fromFile(new File(stringExtra2)), "image/*");
					MmsMessageDetailActivity.this.startActivity(intent);
				}
			});
		}
		this.findViewById(R.id.btn_home_photo).setOnClickListener((View.OnClickListener) new View.OnClickListener() {
			public void onClick(final View view) {
				MmsMessageDetailActivity.this.finish();
			}
		});
	}

	protected void onDestroy() {
		super.onDestroy();
	}
}
