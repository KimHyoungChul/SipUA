package com.zed3.sipua.ui.panel.interpolator;

import android.view.animation.Interpolator;

public class ExpoInterpolator implements Interpolator {
	private int type;

	public ExpoInterpolator(final int type) {
		this.type = type;
	}

	private float in(final float n) {
		double pow;
		if (n == 0.0f) {
			pow = 0.0;
		} else {
			pow = Math.pow(2.0, 10.0f * (n - 1.0f));
		}
		return (float) pow;
	}

	private float inout(float n) {
		if (n == 0.0f) {
			return 0.0f;
		}
		if (n >= 1.0f) {
			return 1.0f;
		}
		n *= 2.0f;
		if (n < 1.0f) {
			return (float) (Math.pow(2.0, 10.0f * (n - 1.0f)) * 0.5);
		}
		return (float) ((-Math.pow(2.0, -10.0f * (n - 1.0f)) + 2.0) * 0.5);
	}

	private float out(final float n) {
		double n2 = 1.0;
		if (n < 1.0f) {
			n2 = 1.0 + -Math.pow(2.0, -10.0f * n);
		}
		return (float) n2;
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
