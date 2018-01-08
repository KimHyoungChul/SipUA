package com.zed3.sipua.ui.panel.interpolator;

import android.view.animation.Interpolator;

public class CubicInterpolator implements Interpolator {
	private int type;

	public CubicInterpolator(final int type) {
		this.type = type;
	}

	private float in(final float n) {
		return n * n * n;
	}

	private float inout(float n) {
		n *= 2.0f;
		if (n < 1.0f) {
			return 0.5f * n * n * n;
		}
		n -= 2.0f;
		return (n * n * n + 2.0f) * 0.5f;
	}

	private float out(float n) {
		--n;
		return n * n * n + 1.0f;
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
