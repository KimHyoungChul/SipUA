package com.zed3.sipua.ui.panel.interpolator;

import android.view.animation.Interpolator;

public class SineInterpolator implements Interpolator {
	private int type;

	public SineInterpolator(final int type) {
		this.type = type;
	}

	private float in(final float n) {
		return (float) (-Math.cos(n * 1.5707963267948966) + 1.0);
	}

	private float inout(final float n) {
		return (float) (-0.5 * (Math.cos(3.141592653589793 * n) - 1.0));
	}

	private float out(final float n) {
		return (float) Math.sin(n * 1.5707963267948966);
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
