package com.zed3.codecs;

public class ModeChange {
	// TODO
	public static EncodeRate.Mode getmode(final byte b) {
		switch (b) {
			default: {
				return null;
			}
			case 0: {
				return EncodeRate.Mode.MR475;
			}
			case 1: {
				return EncodeRate.Mode.MR515;
			}
			case 2: {
				return EncodeRate.Mode.MR59;
			}
			case 3: {
				return EncodeRate.Mode.MR67;
			}
			case 4: {
				return EncodeRate.Mode.MR74;
			}
			case 5: {
				return EncodeRate.Mode.MR795;
			}
			case 6: {
				return EncodeRate.Mode.MR102;
			}
			case 7: {
				return EncodeRate.Mode.MR122;
			}
			case 15: {
				return EncodeRate.Mode.MR74;
			}
		}
	}

	public static byte judge(final float n, final float n2, final byte b) {
		if (n >= 0.5) {
			if (b == 15) {
				return 0;
			}
			if (b > 0 && b < 8) {
				return (byte) (b - 1);
			}
			return 0;
		} else if (n2 >= 289.0) {
			if (b == 15) {
				return 0;
			}
			if (b > 0 && b < 8) {
				return (byte) (b - 1);
			}
			return 0;
		} else {
			if (b == 15) {
				return 7;
			}
			if (b >= 0 && b < 7) {
				return (byte) (b + 1);
			}
			return 7;
		}
	}
}
