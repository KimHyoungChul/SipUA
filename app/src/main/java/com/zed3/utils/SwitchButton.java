package com.zed3.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.CheckBox;

import com.zed3.sipua.R;

@SuppressLint("AppCompatCustomView")
public class SwitchButton extends CheckBox {
	private final float EXTENDED_OFFSET_Y;
	private final int MAX_ALPHA;
	private final float VELOCITY;
	private int mAlpha;
	private float mAnimatedVelocity;
	private boolean mAnimating;
	private float mAnimationPosition;
	private Bitmap mBottom;
	private boolean mBroadcasting;
	private float mBtnInitPos;
	private Bitmap mBtnNormal;
	private float mBtnOffPos;
	private float mBtnOnPos;
	private float mBtnPos;
	private Bitmap mBtnPressed;
	private float mBtnWidth;
	private boolean mChecked;
	private int mClickTimeout;
	private Bitmap mCurBtnPic;
	private float mExtendOffsetY;
	private float mFirstDownX;
	private float mFirstDownY;
	private Bitmap mFrame;
	private Bitmap mMask;
	private float mMaskHeight;
	private float mMaskWidth;
	private OnCheckedChangeListener mOnCheckedChangeListener;
	private OnCheckedChangeListener mOnCheckedChangeWidgetListener;
	private Paint mPaint;
	private ViewParent mParent;
	private PerformClick mPerformClick;
	private float mRealPos;
	private RectF mSaveLayerRectF;
	private int mTouchSlop;
	private boolean mTurningOn;
	private float mVelocity;
	private PorterDuffXfermode mXfermode;

	private final class PerformClick implements Runnable {
		private PerformClick() {
		}

		public void run() {
			SwitchButton.this.performClick();
		}
	}

	private final class SwitchAnimation implements Runnable {
		private SwitchAnimation() {
		}

		public void run() {
			if (SwitchButton.this.mAnimating) {
				SwitchButton.this.doAnimation();
				FrameAnimationController.requestAnimationFrame(this);
			}
		}
	}

	public SwitchButton(Context context, AttributeSet attrs) {
		this(context, attrs, 16842860);
	}

	public SwitchButton(Context context) {
		this(context, null);
	}

	public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.MAX_ALPHA = MotionEventCompat.ACTION_MASK;
		this.mAlpha = MotionEventCompat.ACTION_MASK;
		this.mChecked = false;
		this.VELOCITY = 350.0f;
		this.EXTENDED_OFFSET_Y = 15.0f;
		initView(context);
	}

	private void initView(Context context) {
		this.mPaint = new Paint();
		this.mPaint.setColor(-1);
		Resources resources = context.getResources();
		this.mClickTimeout = ViewConfiguration.getPressedStateDuration() + ViewConfiguration.getTapTimeout();
		this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		this.mBottom = BitmapFactory.decodeResource(resources, R.drawable.bottom);
		this.mBtnPressed = BitmapFactory.decodeResource(resources, R.drawable.btn_pressed);
		this.mBtnNormal = BitmapFactory.decodeResource(resources, R.drawable.btn_unpressed);
		this.mFrame = BitmapFactory.decodeResource(resources, R.drawable.frame_on);
		this.mMask = BitmapFactory.decodeResource(resources, R.drawable.mask);
		this.mCurBtnPic = this.mBtnNormal;
		this.mBtnWidth = (float) this.mBtnPressed.getWidth();
		this.mMaskWidth = (float) this.mMask.getWidth();
		this.mMaskHeight = (float) this.mMask.getHeight();
		this.mBtnOffPos = this.mBtnWidth / 2.0f;
		this.mBtnOnPos = this.mMaskWidth - (this.mBtnWidth / 2.0f);
		this.mBtnPos = this.mChecked ? this.mBtnOnPos : this.mBtnOffPos;
		this.mRealPos = getRealPos(this.mBtnPos);
		float density = getResources().getDisplayMetrics().density;
		this.mVelocity = (float) ((int) ((350.0f * density) + 0.5f));
		this.mExtendOffsetY = (float) ((int) ((15.0f * density) + 0.5f));
		this.mSaveLayerRectF = new RectF(0.0f, this.mExtendOffsetY, (float) this.mMask.getWidth(), ((float) this.mMask.getHeight()) + this.mExtendOffsetY);
		this.mXfermode = new PorterDuffXfermode(Mode.SRC_IN);
	}

	public void setEnabled(boolean enabled) {
		this.mAlpha = enabled ? MotionEventCompat.ACTION_MASK : 127;
		super.setEnabled(enabled);
	}

	public boolean isChecked() {
		return this.mChecked;
	}

	public void toggle() {
		setChecked(!this.mChecked);
	}

	private void setCheckedDelayed(final boolean checked) {
		postDelayed(new Runnable() {
			public void run() {
				SwitchButton.this.setChecked(checked);
			}
		}, 10);
	}

	public void setChecked(boolean checked) {
		if (this.mChecked != checked) {
			this.mChecked = checked;
			this.mBtnPos = checked ? this.mBtnOnPos : this.mBtnOffPos;
			this.mRealPos = getRealPos(this.mBtnPos);
			invalidate();
			if (!this.mBroadcasting) {
				this.mBroadcasting = true;
				if (this.mOnCheckedChangeListener != null) {
					this.mOnCheckedChangeListener.onCheckedChanged(this, this.mChecked);
				}
				if (this.mOnCheckedChangeWidgetListener != null) {
					this.mOnCheckedChangeWidgetListener.onCheckedChanged(this, this.mChecked);
				}
				this.mBroadcasting = false;
			}
		}
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		this.mOnCheckedChangeListener = listener;
	}

	void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
		this.mOnCheckedChangeWidgetListener = listener;
	}

	public boolean onTouchEvent(MotionEvent event) {
		boolean z = true;
		boolean z2 = false;
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		float deltaX = Math.abs(x - this.mFirstDownX);
		float deltaY = Math.abs(y - this.mFirstDownY);
		float time;
		switch (action) {
			case 0:
				attemptClaimDrag();
				this.mFirstDownX = x;
				this.mFirstDownY = y;
				this.mCurBtnPic = this.mBtnPressed;
				this.mBtnInitPos = this.mChecked ? this.mBtnOnPos : this.mBtnOffPos;
				break;
			case 1:
				this.mCurBtnPic = this.mBtnNormal;
				time = (float) (event.getEventTime() - event.getDownTime());
				if (deltaY < ((float) this.mTouchSlop) && deltaX < ((float) this.mTouchSlop) && time < ((float) this.mClickTimeout)) {
					if (this.mPerformClick == null) {
						this.mPerformClick = new PerformClick();
					}
					if (!post(this.mPerformClick)) {
						performClick();
						break;
					}
				}
				if (!this.mTurningOn) {
					z2 = true;
				}
				startAnimation(z2);
				break;
			case 2:
				time = (float) (event.getEventTime() - event.getDownTime());
				this.mBtnPos = (this.mBtnInitPos + event.getX()) - this.mFirstDownX;
				if (this.mBtnPos >= this.mBtnOffPos) {
					this.mBtnPos = this.mBtnOffPos;
				}
				if (this.mBtnPos <= this.mBtnOnPos) {
					this.mBtnPos = this.mBtnOnPos;
				}
				if (this.mBtnPos <= ((this.mBtnOffPos - this.mBtnOnPos) / 2.0f) + this.mBtnOnPos) {
					z = false;
				}
				this.mTurningOn = z;
				this.mRealPos = getRealPos(this.mBtnPos);
				break;
		}
		invalidate();
		return isEnabled();
	}

	public boolean performClick() {
		boolean z;
		if (this.mChecked) {
			z = false;
		} else {
			z = true;
		}
		startAnimation(z);
		return true;
	}

	private void attemptClaimDrag() {
		this.mParent = getParent();
		if (this.mParent != null) {
			this.mParent.requestDisallowInterceptTouchEvent(true);
		}
	}

	private float getRealPos(float btnPos) {
		return btnPos - (this.mBtnWidth / 2.0f);
	}

	protected void onDraw(Canvas canvas) {
		canvas.saveLayerAlpha(this.mSaveLayerRectF, this.mAlpha, Canvas.ALL_SAVE_FLAG);
		canvas.drawBitmap(this.mMask, 0.0f, this.mExtendOffsetY, this.mPaint);
		this.mPaint.setXfermode(this.mXfermode);
		canvas.drawBitmap(this.mBottom, this.mRealPos, this.mExtendOffsetY, this.mPaint);
		this.mPaint.setXfermode(null);
		canvas.drawBitmap(this.mFrame, 0.0f, this.mExtendOffsetY, this.mPaint);
		canvas.drawBitmap(this.mCurBtnPic, this.mRealPos, this.mExtendOffsetY, this.mPaint);
		canvas.restore();
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension((int) this.mMaskWidth, (int) (this.mMaskHeight + (2.0f * this.mExtendOffsetY)));
	}

	private void startAnimation(boolean turnOn) {
		this.mAnimating = true;
		this.mAnimatedVelocity = turnOn ? -this.mVelocity : this.mVelocity;
		this.mAnimationPosition = this.mBtnPos;
		new SwitchAnimation().run();
	}

	private void stopAnimation() {
		this.mAnimating = false;
	}

	private void doAnimation() {
		this.mAnimationPosition += (this.mAnimatedVelocity * 16.0f) / 1000.0f;
		if (this.mAnimationPosition <= this.mBtnOnPos) {
			stopAnimation();
			this.mAnimationPosition = this.mBtnOnPos;
			setCheckedDelayed(true);
		} else if (this.mAnimationPosition >= this.mBtnOffPos) {
			stopAnimation();
			this.mAnimationPosition = this.mBtnOffPos;
			setCheckedDelayed(false);
		}
		moveView(this.mAnimationPosition);
	}

	private void moveView(float position) {
		this.mBtnPos = position;
		this.mRealPos = getRealPos(this.mBtnPos);
		invalidate();
	}
}
