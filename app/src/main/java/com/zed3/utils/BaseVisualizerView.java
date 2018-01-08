package com.zed3.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.audiofx.Visualizer;
import android.view.View;

public class BaseVisualizerView extends View implements Visualizer.OnDataCaptureListener {
	protected static final int CYLINDER_NUM = 25;
	private static final int DN_H = 160;
	private static final int DN_SL = 5;
	private static final int DN_SW = 6;
	private static final int DN_W = 400;
	protected static final int MAX_LEVEL = 9;
	private int hgap;
	private int levelStep;
	protected byte[] mBackgroudData;
	protected byte[] mData;
	boolean mDataEn;
	protected Paint mPaint;
	protected Visualizer mVisualizer;
	private float strokeLength;
	private float strokeWidth;
	private int vgap;

	public BaseVisualizerView(final Context context) {
		super(context);
		this.hgap = 0;
		this.vgap = 0;
		this.levelStep = 0;
		this.strokeWidth = 0.0f;
		this.strokeLength = 0.0f;
		this.mVisualizer = null;
		this.mPaint = null;
		this.mData = new byte[25];
		this.mBackgroudData = new byte[25];
		this.mDataEn = true;
		for (int i = 0; i < 25; ++i) {
			this.mBackgroudData[i] = 11;
		}
		(this.mPaint = new Paint()).setAntiAlias(true);
		this.mPaint.setColor(-2749147);
		this.mPaint.setStrokeJoin(Paint.Join.ROUND);
		this.mPaint.setStrokeCap(Paint.Cap.ROUND);
	}

	protected void drawCylinder(final Canvas canvas, final float n, final byte b) {
		int n2 = b;
		if (b < 0) {
			n2 = 0;
		}
		for (int i = 0; i < n2; ++i) {
			final float n3 = this.getHeight() - this.vgap * i - this.vgap;
			canvas.drawLine(n, n3, n + this.strokeLength, n3, this.mPaint);
		}
	}

	protected void drawCylinderBackGroud(final Canvas canvas, final float n, final byte b) {
		int n2 = b;
		if (b < 0) {
			n2 = 0;
		}
		for (int i = 0; i < n2; ++i) {
			final float n3 = this.getHeight() - this.vgap * i - this.vgap;
			final Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(-10460054);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.ROUND);
			canvas.drawLine(n, n3, n + this.strokeLength, n3, paint);
		}
	}

	public void enableDataProcess(final boolean mDataEn) {
		this.mDataEn = mDataEn;
	}

	public void onDraw(final Canvas canvas) {
		for (int i = 0; i < 25; ++i) {
			this.drawCylinderBackGroud(canvas, this.strokeWidth / 2.0f + this.hgap + i * (this.hgap + this.strokeLength), this.mBackgroudData[i]);
			this.drawCylinder(canvas, this.strokeWidth / 2.0f + this.hgap + i * (this.hgap + this.strokeLength), this.mData[i]);
		}
	}

	public void onFftDataCapture(final Visualizer visualizer, byte[] mData, int i) {
		final byte[] array = new byte[mData.length / 2 + 1];
		if (this.mDataEn) {
			array[0] = (byte) Math.abs(mData[1]);
			i = 1;
			for (int j = 2; j < mData.length; j += 2, ++i) {
				array[i] = (byte) Math.hypot(mData[j], mData[j + 1]);
			}
		} else {
			for (i = 0; i < 25; ++i) {
			}
		}
		byte b;
		byte b2;
		for (i = 0; i < 25; ++i) {
			b = (byte) (Math.abs(array[25 - i]) / this.levelStep);
			b2 = this.mData[i];
			if (b > b2) {
				this.mData[i] = b;
			} else if (b2 > 0) {
				mData = this.mData;
				--mData[i];
			}
		}
		this.postInvalidate();
	}

	protected void onLayout(final boolean b, final int n, final int n2, final int n3, final int n4) {
		super.onLayout(b, n, n2, n3, n4);
		final float n5 = n3 - n;
		final float n6 = n4 - n2;
		final float n7 = n5 / 400.0f;
		this.strokeWidth = 6.0f * (n6 / 160.0f);
		this.strokeLength = 5.0f * n7;
		this.hgap = (int) ((n5 - this.strokeLength * 25.0f) / 26.0f);
		this.vgap = (int) (n6 / 11.0f);
		this.mPaint.setStrokeWidth(this.strokeWidth);
	}

	public void onWaveFormDataCapture(final Visualizer visualizer, final byte[] array, final int n) {
	}

	public void setTimes(int i) {
		if (i <= 0) {
			for (i = 0; i < 25; ++i) {
				this.mData[i] = 0;
			}
			this.postInvalidate();
			return;
		}
		int n;
		if ((n = i) < 30) {
			n = i + 20;
		}
		final int n2 = (n - 20) * 7 / 30;
		for (i = 0; i < 25; ++i) {
			if (i < 5) {
				this.mData[i] = (byte) n2;
			} else if (i < 10) {
				this.mData[i] = (byte) (n2 + 1);
			} else if (i < 15) {
				this.mData[i] = (byte) (n2 + 2);
			} else if (i < 20) {
				this.mData[i] = (byte) (n2 + 1);
			} else {
				this.mData[i] = (byte) n2;
			}
		}
		this.postInvalidate();
	}

	public void setVisualizer(final Visualizer mVisualizer) {
		if (mVisualizer != null) {
			if (!mVisualizer.getEnabled()) {
				mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
			}
			this.levelStep = 14;
			mVisualizer.setDataCaptureListener((Visualizer.OnDataCaptureListener) this, Visualizer.getMaxCaptureRate() / 2, false, true);
		} else if (this.mVisualizer != null) {
			this.mVisualizer.setEnabled(false);
			this.mVisualizer.release();
		}
		this.mVisualizer = mVisualizer;
	}
}
