package com.zed3.sipua.ui.panel.interpolator;

import android.view.animation.Interpolator;

public class ElasticInterpolator implements Interpolator {
	private float amplitude;
	private float period;
	private int type;

	public ElasticInterpolator(final int type, final float amplitude, final float period) {
		this.type = type;
		this.amplitude = amplitude;
		this.period = period;
	}

	private float in(float n, float n2, float n3) {
		if (n == 0.0f) {
			return 0.0f;
		}
		if (n >= 1.0f) {
			return 1.0f;
		}
		float n4 = n3;
		if (n3 == 0.0f) {
			n4 = 0.3f;
		}
		if (n2 == 0.0f || n2 < 1.0f) {
			n2 = 1.0f;
			n3 = n4 / 4.0f;
		} else {
			n3 = (float) (n4 / 6.283185307179586 * Math.asin(1.0f / n2));
		}
		final double n5 = n2;
		--n;
		return (float) (-(Math.pow(2.0, 10.0f * n) * n5 * Math.sin((n - n3) * 6.283185307179586 / n4)));
	}

	private float inout(float n, float n2, float n3) {
		if (n == 0.0f) {
			return 0.0f;
		}
		if (n >= 1.0f) {
			return 1.0f;
		}
		float n4 = n3;
		if (n3 == 0.0f) {
			n4 = 0.45000002f;
		}
		if (n2 == 0.0f || n2 < 1.0f) {
			n2 = 1.0f;
			n3 = n4 / 4.0f;
		} else {
			n3 = (float) (n4 / 6.283185307179586 * Math.asin(1.0f / n2));
		}
		n *= 2.0f;
		if (n < 1.0f) {
			final double n5 = n2;
			--n;
			return (float) (Math.pow(2.0, 10.0f * n) * n5 * Math.sin((n - n3) * 6.283185307179586 / n4) * -0.5);
		}
		final double n6 = n2;
		--n;
		return (float) (Math.pow(2.0, -10.0f * n) * n6 * Math.sin((n - n3) * 6.283185307179586 / n4) * 0.5 + 1.0);
	}

	private float out(final float n, float n2, float n3) {
		if (n == 0.0f) {
			return 0.0f;
		}
		if (n >= 1.0f) {
			return 1.0f;
		}
		float n4 = n3;
		if (n3 == 0.0f) {
			n4 = 0.3f;
		}
		if (n2 == 0.0f || n2 < 1.0f) {
			n2 = 1.0f;
			n3 = n4 / 4.0f;
		} else {
			n3 = (float) (Math.asin(1.0f / n2) * (n4 / 6.283185307179586));
		}
		return (float) (n2 * Math.pow(2.0, -10.0f * n) * Math.sin((n - n3) * 6.283185307179586 / n4) + 1.0);
	}

	public float getInterpolation(final float n) {
		if (this.type == 0) {
			return this.in(n, this.amplitude, this.period);
		}
		if (this.type == 1) {
			return this.out(n, this.amplitude, this.period);
		}
		if (this.type == 2) {
			return this.inout(n, this.amplitude, this.period);
		}
		return 0.0f;
	}
}
