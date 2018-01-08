package com.zed3.video;

public class VideoUtils {
	public static void NV21Rotate180Degree(int i, int j, final byte[] array, final byte[] array2) {
		int n;
		for (n = (j *= i), i = 0; j > 0; --j, ++i) {
			array2[i] = array[j - 1];
		}
		j = 0;
		for (i = n / 2; i > 0; i -= 2) {
			array2[n + j] = array[n + i - 2];
			array2[n + j + 1] = array[n + i - 1];
			j += 2;
		}
	}

	public static void NV21Rotate180DegreeMi(int i, int j, final byte[] array, final byte[] array2) {
		int n;
		for (n = (j *= i), i = 0; j > 0; --j, ++i) {
			array2[i] = array[j - 1];
		}
		j = 0;
		for (i = n / 2; i > 0; i -= 2) {
			array2[n + j] = array[n + i - 1];
			array2[n + j + 1] = array[n + i - 2];
			j += 2;
		}
	}

	public static void NV21Rotate90DegreeLeftwise(final int n, final int n2, final byte[] array, final byte[] array2) {
		final int n3 = n * n2;
		int n4 = 0;
		for (int i = n; i > 0; --i) {
			int n5 = 0;
			for (int j = 0; j < n2; ++j) {
				array2[n4] = array[n5 + i - 1];
				n5 += n;
				++n4;
			}
		}
		int n6 = n4;
		for (int k = n; k > 0; k -= 2) {
			int n7 = 0;
			for (int l = 0; l < n2 / 2; ++l) {
				final int n8 = n6 + 1;
				array2[n6] = array[n3 + n7 + k - 2];
				n6 = n8 + 1;
				array2[n8] = array[n3 + n7 + k - 1];
				n7 += n;
			}
		}
	}

	public static void NV21Rotate90DegreeLeftwiseMi(final int n, final int n2, final byte[] array, final byte[] array2) {
		final int n3 = n * n2;
		int n4 = 0;
		for (int i = n; i > 0; --i) {
			int n5 = 0;
			for (int j = 0; j < n2; ++j) {
				array2[n4] = array[n5 + i - 1];
				n5 += n;
				++n4;
			}
		}
		int n6 = n4;
		for (int k = n; k > 0; k -= 2) {
			int n7 = 0;
			for (int l = 0; l < n2 / 2; ++l) {
				final int n8 = n6 + 1;
				array2[n6] = array[n3 + n7 + k - 1];
				n6 = n8 + 1;
				array2[n8] = array[n3 + n7 + k - 2];
				n7 += n;
			}
		}
	}

	public static void NV21Rotate90DegreeRightwise(final int n, final int n2, final byte[] array, final byte[] array2) {
		final int n3 = n * n2;
		int n4 = 0;
		for (int i = 0; i < n; ++i) {
			int n5 = (n2 - 1) * n;
			for (int j = n2; j > 0; --j) {
				array2[n4] = array[n5 + i];
				n5 -= n;
				++n4;
			}
		}
		final int n6 = n2 / 2;
		for (int k = 0; k < n; k += 2) {
			int n7 = (n6 - 1) * n;
			for (int l = n2 / 2; l > 0; --l) {
				final int n8 = n4 + 1;
				array2[n4] = array[n3 + n7 + k];
				n4 = n8 + 1;
				array2[n8] = array[n3 + n7 + k + 1];
				n7 -= n;
			}
		}
	}

	public static void NV21Rotate90DegreeRightwiseMi(final int n, final int n2, final byte[] array, final byte[] array2) {
		final int n3 = n * n2;
		int n4 = 0;
		for (int i = 0; i < n; ++i) {
			int n5 = (n2 - 1) * n;
			for (int j = n2; j > 0; --j) {
				array2[n4] = array[n5 + i];
				n5 -= n;
				++n4;
			}
		}
		final int n6 = n2 / 2;
		for (int k = 0; k < n; k += 2) {
			int n7 = (n6 - 1) * n;
			for (int l = n2 / 2; l > 0; --l) {
				final int n8 = n4 + 1;
				array2[n4] = array[n3 + n7 + k + 1];
				n4 = n8 + 1;
				array2[n8] = array[n3 + n7 + k];
				n7 -= n;
			}
		}
	}

	public static void NV21ToI420p(int i, int n, final byte[] array, final byte[] array2) {
		final int n2 = i * n;
		System.arraycopy(array, 0, array2, 0, n2);
		n = 0;
		for (i = 0; i < n2 / 2; i += 2) {
			array2[n2 + n] = array[n2 + i + 1];
			array2[n2 / 4 + n2 + n] = array[n2 + i];
			++n;
		}
	}

	public static void NV21ToI420pWithRotate180Degree(int i, int j, final byte[] array, final byte[] array2) {
		int n;
		for (n = (j *= i), i = 0; j > 0; --j, ++i) {
			array2[i] = array[j - 1];
		}
		j = 0;
		for (i = n / 2; i > 0; i -= 2) {
			array2[n + j] = array[n + i - 1];
			array2[n / 4 + n + j] = array[n + i - 2];
			++j;
		}
	}

	public static void NV21ToI420pWithRotate90DegreeLeftwise(final int n, final int n2, final byte[] array, final byte[] array2) {
		final int n3 = n * n2;
		final int n4 = n3 * 5 / 4;
		int n5 = 0;
		for (int i = n; i > 0; --i) {
			int n6 = 0;
			for (int j = 0; j < n2; ++j) {
				array2[n5] = array[n6 + i - 1];
				n6 += n;
				++n5;
			}
		}
		int n7 = 0;
		for (int k = n; k > 0; k -= 2) {
			int n8 = 0;
			for (int l = 0; l < n2 / 2; ++l) {
				array2[n3 + n7] = array[n3 + n8 + k - 1];
				array2[n4 + n7] = array[n3 + n8 + k - 2];
				n8 += n;
				++n7;
			}
		}
	}

	public static void NV21ToI420pWithRotate90DegreeRightwise(final int n, final int n2, final byte[] array, final byte[] array2) {
		final int n3 = n * n2;
		int n4 = 0;
		for (int i = 0; i < n; ++i) {
			int n5 = (n2 - 1) * n;
			for (int j = n2; j > 0; --j) {
				array2[n4] = array[n5 + i];
				n5 -= n;
				++n4;
			}
		}
		int n6 = 0;
		final int n7 = n2 / 2;
		for (int k = 0; k < n; k += 2) {
			int n8 = (n7 - 1) * n;
			for (int l = n2 / 2; l > 0; --l) {
				array2[n3 + n6] = array[n3 + n8 + k + 1];
				array2[n3 / 4 + n3 + n6] = array[n3 + n8 + k];
				n8 -= n;
				++n6;
			}
		}
	}

	public static void changeUV(int i, int n, final byte[] array) {
		for (n *= i, i = 0; i < n / 2; i += 2) {
			array[n + i] ^= array[n + i + 1];
			array[n + i + 1] ^= array[n + i];
			array[n + i] ^= array[n + i + 1];
		}
	}

	private void changeUV2(final int n, final int n2, final byte[] array) {
		final int n3 = n * n2;
		final byte[] array2 = new byte[n * n2 / 4];
		System.arraycopy(array, n3, array2, 0, array2.length);
		System.arraycopy(array, n3 / 4 + n3, array, n3, array2.length);
		System.arraycopy(array2, 0, array, n3 / 4 + n3, array2.length);
	}

	private void yuv420spToyuv420p(int i, final int n, final byte[] array) {
		final int n2 = i * n;
		final byte[] array2 = new byte[i * n / 2];
		System.arraycopy(array, n2, array2, 0, i * n / 2);
		for (i = 0; i < n2 / 4; ++i) {
			array[n2 + i] = array2[i * 2 + 1];
			array[n2 + i + n2 / 4] = array2[i * 2];
		}
	}

	void YUV420spRotateClockwise90To420P(final byte[] array, final byte[] array2, final int n, int i) {
		final int n2 = n * i;
		int n3 = 0;
		for (int j = 0; j < n; ++j) {
			int n4 = (i - 1) * n;
			for (int k = i - 1; k >= 0; --k) {
				array[n3] = array2[n4 + j];
				n4 -= n;
				++n3;
			}
		}
		final int n5 = i / 2;
		final int n6 = i / 2;
		int n7 = 0;
		int n8;
		int l;
		for (i = 0; i < n; i += 2) {
			n8 = (n6 - 1) * n;
			for (l = n5 - 1; l >= 0; --l) {
				array[n7 + n2] = array2[n2 + n8 + i + 1];
				array[n2 / 4 + n2 + n7] = array2[n2 + n8 + i];
				n8 -= n;
				++n7;
			}
		}
	}

	void YUV420spRotateClockwise90ToUV(final byte[] array, final byte[] array2, final int n, int i) {
		final int n2 = n * i;
		int n3 = 0;
		for (int j = 0; j < n; ++j) {
			int n4 = (i - 1) * n;
			for (int k = i - 1; k >= 0; --k) {
				array[n3] = array2[n4 + j];
				n4 -= n;
				++n3;
			}
		}
		final int n5 = i / 2;
		final int n6 = i / 2;
		int n7;
		int l;
		for (i = 0; i < n; i += 2) {
			n7 = (n6 - 1) * n;
			for (l = n5 - 1; l >= 0; --l) {
				array[n3] = array2[n2 + n7 + i];
				array[n3 + 1] = array2[n2 + n7 + i + 1];
				n7 -= n;
				n3 += 2;
			}
		}
	}

	byte[] YUV420spRotateNegative90(final byte[] array, final int n, int i) {
		final byte[] array2 = new byte[array.length];
		int n2 = 0;
		int n3 = 0;
		if (n != 0 || i != 0) {
			n2 = n * i;
			n3 = i >> 1;
		}
		int n4 = 0;
		for (int j = 0; j < n; ++j) {
			int n5 = n - 1;
			for (int k = 0; k < i; ++k) {
				array2[n4] = array[n5 - j];
				++n4;
				n5 += n;
			}
		}
		int n6;
		int l;
		for (i = 0; i < n; i += 2) {
			n6 = n2 + n - 1;
			for (l = 0; l < n3; ++l) {
				array2[n4] = array[n6 - i - 1];
				array2[n4 + 1] = array[n6 - i];
				n4 += 2;
				n6 += n;
			}
		}
		return array2;
	}

	byte[] YUV420spRotatePositive90(final byte[] array, final int n, int i) {
		final byte[] array2 = new byte[array.length];
		int n2 = 0;
		int n3 = 0;
		int n4 = 0;
		if (n != 0 || i != 0) {
			n2 = i;
			n3 = n * i;
			n4 = i >> 1;
		}
		int n5 = 0;
		for (int j = 0; j < n; ++j) {
			int n6 = (n2 - 1) * n;
			for (int k = 0; k < i; ++k) {
				array2[n5] = array[n6 - j];
				++n5;
				n6 -= n;
			}
		}
		int n7;
		int l;
		for (i = 0; i < n; i += 2) {
			n7 = n3 + n - 1;
			for (l = 0; l < n4; ++l) {
				array2[n5] = array[n7 - i - 1];
				array2[n5 + 1] = array[n7 - i];
				n5 += 2;
				n7 += n;
			}
		}
		return array2;
	}

	void changeNV21ToY420(final byte[] array, final byte[] array2, int i, int n) {
		for (n *= i, i = 0; i < n; ++i) {
			array2[i] = array[i];
		}
		for (i = 0; i < n / 2; i += 2) {
			array2[i + n] = array[n + i + 1];
			array2[n / 4 + i] = array[n + i];
		}
	}
}
