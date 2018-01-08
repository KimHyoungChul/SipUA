package com.zed3.sipua.ui.panel.interpolator;

import android.view.animation.Interpolator;

public class BounceInterpolator implements Interpolator {
	private int type;

	public BounceInterpolator(final int type) {
		this.type = type;
	}

	private float in(final float n) {
		return 1.0f - this.out(1.0f - n);
	}

	private float inout(final float n) {
		if (n < 0.5f) {
			return this.in(n * 2.0f) * 0.5f;
		}
		return this.out(n * 2.0f - 1.0f) * 0.5f + 0.5f;
	}

	private float out(float n) {
		if (n < 0.36363636363636365) {
			return 7.5625f * n * n;
		}
		if (n < 0.7272727272727273) {
			n -= 0.5454545454545454;
			return 7.5625f * n * n + 0.75f;
		}
		if (n < 0.9090909090909091) {
			n -= 0.8181818181818182;
			return 7.5625f * n * n + 0.9375f;
		}
		n -= 0.9545454545454546;
		return 7.5625f * n * n + 0.984375f;
	}

	public float getInterpolation(final float n) {
		if (this.type == 0) {
			return this.in(n);
		}
		if (this.type == 1) {
			return this.out(n);
		}
		if (this.type == 2) {
			return this.inout(n);
		}
		return 0.0f;
	}
}
