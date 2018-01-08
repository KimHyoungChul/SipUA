package com.zed3.sipua.ui.panel.interpolator;

import android.view.animation.Interpolator;

public class QuadInterpolator implements Interpolator {
	private int type;

	public QuadInterpolator(final int type) {
		this.type = type;
	}

	private float in(final float n) {
		return n * n;
	}

	private float inout(float n) {
		n *= 2.0f;
		if (n < 1.0f) {
			return 0.5f * n * n;
		}
		--n;
		return -0.5f * ((n - 2.0f) * n - 1.0f);
	}

	private float out(final float n) {
		return -n * (n - 2.0f);
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
