package com.zed3.sipua.ui.panel.interpolator;

import android.view.animation.Interpolator;

public class BackInterpolator implements Interpolator {
	private float overshot;
	private int type;

	public BackInterpolator(final int type, final float overshot) {
		this.type = type;
		this.overshot = overshot;
	}

	private float in(final float n, final float n2) {
		float n3 = n2;
		if (n2 == 0.0f) {
			n3 = 1.70158f;
		}
		return n * n * ((1.0f + n3) * n - n3);
	}

	private float inout(float n, float n2) {
		float n3 = n2;
		if (n2 == 0.0f) {
			n3 = 1.70158f;
		}
		n *= 2.0f;
		if (n < 1.0f) {
			n2 = (float) (n3 * 1.525);
			return n * n * ((1.0f + n2) * n - n2) * 0.5f;
		}
		n -= 2.0f;
		n2 = (float) (n3 * 1.525);
		return (n * n * ((1.0f + n2) * n + n2) + 2.0f) * 0.5f;
	}

	private float out(float n, final float n2) {
		float n3 = n2;
		if (n2 == 0.0f) {
			n3 = 1.70158f;
		}
		--n;
		return n * n * ((n3 + 1.0f) * n + n3) + 1.0f;
	}

	public float getInterpolation(final float n) {
		if (this.type == 0) {
			return this.in(n, this.overshot);
		}
		if (this.type == 1) {
			return this.out(n, this.overshot);
		}
		if (this.type == 2) {
			return this.inout(n, this.overshot);
		}
		return 0.0f;
	}
}
