package com.zed3.sipua.ui.panel.interpolator;

import android.view.animation.Interpolator;

public class CircInterpolator implements Interpolator {
	private int type;

	public CircInterpolator(final int type) {
		this.type = type;
	}

	private float in(final float n) {
		return (float) (-(Math.sqrt(1.0f - n * n) - 1.0));
	}

	private float inout(float n) {
		n *= 2.0f;
		if (n < 1.0f) {
			return (float) (-0.5 * (Math.sqrt(1.0f - n * n) - 1.0));
		}
		n -= 2.0f;
		return (float) (0.5 * (Math.sqrt(1.0f - n * n) + 1.0));
	}

	private float out(float n) {
		--n;
		return (float) Math.sqrt(1.0f - n * n);
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
