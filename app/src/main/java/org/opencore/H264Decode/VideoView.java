package org.opencore.H264Decode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class VideoView extends View {
	Bitmap bitmap;
	int mH264Height;
	int mH264Width;
	float mRotate;

	public VideoView(final Context context) {
		super(context);
		this.mRotate = 0.0f;
	}

	public VideoView(final Context context, final AttributeSet set) {
		super(context, set);
		this.mRotate = 0.0f;
	}

	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (this.bitmap == null) {
			return;
		}
		final Bitmap bitmap = this.bitmap;
		final Rect rect = new Rect();
		final Rect rect2 = new Rect();
		rect.set(0, 0, this.mH264Width, this.mH264Height);
		rect2.set(0, 0, this.getWidth(), this.getHeight());
		final Matrix matrix = new Matrix();
		if (this.mRotate == 0.0f) {
			matrix.postScale(this.getWidth() * 1.0f / this.mH264Width, this.getHeight() * 1.0f / this.mH264Height);
		} else {
			matrix.postScale(this.getHeight() * 1.0f / this.mH264Width, this.getWidth() * 1.0f / this.mH264Height);
			matrix.postRotate(90.0f, 0.0f, 0.0f);
			matrix.postTranslate((float) this.getWidth(), 0.0f);
		}
		canvas.drawBitmap(bitmap, matrix, (Paint) null);
	}

	public void setBitmap(final Bitmap bitmap, final int mh264Width, final int mh264Height) {
		this.bitmap = bitmap;
		this.mH264Width = mh264Width;
		this.mH264Height = mh264Height;
		this.postInvalidate();
	}
}
