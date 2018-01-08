package com.zed3.utils;

import android.support.v4.view.MotionEventCompat;

public class Tea {
	public static Short UBFILTER = Short.decode("0xff");
	public static Long UIFILTER = Long.decode("0xffffffff");

	public static Short[] encipher(Short[] v, Short[] k) {
		Long[] vL = shortToLong(v);
		Long[] kL = shortToLong(k);
		Long[] wL = new Long[vL.length];
		long y = vL[0].longValue();
		long z = vL[1].longValue();
		long a = kL[0].longValue();
		long b = kL[1].longValue();
		long c = kL[2].longValue();
		long d = kL[3].longValue();
		long sum = 0;
		long delta = Long.decode("0x9E3779B9").longValue();
		long n = 16;
		while (true) {
			long n2 = n - 1;
			if (n <= 0) {
				wL[0] = Long.valueOf(y);
				wL[1] = Long.valueOf(z);
				return longToShort(wL);
			}
			sum = (sum + delta) & UIFILTER.longValue();
			y = (y + ((((z << 4) + a) ^ (z + sum)) ^ ((z >> 5) + b))) & UIFILTER.longValue();
			z = (z + ((((y << 4) + c) ^ (y + sum)) ^ ((y >> 5) + d))) & UIFILTER.longValue();
			n = n2;
		}
	}

	public static Short[] decipher(Short[] v, Short[] k) {
		Long[] vL = shortToLong(v);
		Long[] kL = shortToLong(k);
		Long[] wL = new Long[vL.length];
		long y = vL[0].longValue();
		long z = vL[1].longValue();
		long a = kL[0].longValue();
		long b = kL[1].longValue();
		long c = kL[2].longValue();
		long d = kL[3].longValue();
		long sum = Long.decode("0xE3779B90").longValue();
		long delta = Long.decode("0x9E3779B9").longValue();
		long n = 16;
		while (true) {
			long n2 = n - 1;
			if (n <= 0) {
				wL[0] = Long.valueOf(y);
				wL[1] = Long.valueOf(z);
				return longToShort(wL);
			}
			z = (z + ((((((y << 4) + c) ^ (y + sum)) ^ ((y >> 5) + d)) ^ -1) + 1)) & UIFILTER.longValue();
			y = (y + ((((((z << 4) + a) ^ (z + sum)) ^ ((z >> 5) + b)) ^ -1) + 1)) & UIFILTER.longValue();
			sum = (sum + ((-1 ^ delta) + 1)) & UIFILTER.longValue();
			n = n2;
		}
	}

	public static Long[] shortToLong(Short[] source) {
		int sourlen = source.length;
		int turn = sourlen / 4;
		int remainder = sourlen % 4;
		int tarlen = turn + (remainder == 0 ? 0 : 1);
		Long[] target = new Long[tarlen];
		for (int i = 0; i < target.length; i++) {
			target[i] = Long.valueOf(Long.parseLong("0"));
		}
		int turnIter = 0;
		while (turnIter < tarlen) {
			int iter = 0;
			while (iter < 4) {
				target[turnIter] = Long.valueOf(target[turnIter].longValue() << 8);
				if (turnIter != turn - 1 || (turnIter == turn - 1 && (iter < remainder || remainder == 0))) {
					target[turnIter] = Long.valueOf(target[turnIter].longValue() + ((long) source[(turnIter * 4) + iter].shortValue()));
				}
				iter++;
			}
			turnIter++;
		}
		return target;
	}

	public static Short[] longToShort(Long[] source) {
		Short[] target = new Short[(source.length * 4)];
		int turn = target.length % 4;
		for (int iter = 0; iter < target.length; iter++) {
			int move = (3 - (iter % 4)) * 8;
			target[iter] = Short.valueOf(Short.parseShort(Long.toString((source[iter / 4].longValue() & ((long) (UBFILTER.shortValue() << move))) >> move)));
		}
		return target;
	}

	public byte[] encrypt(byte[] content, int offset, int[] key, int times) {
		int[] tempInt = byteToInt(content, offset);
		int y = tempInt[0];
		int z = tempInt[1];
		int sum = 0;
		int a = key[0];
		int b = key[1];
		int c = key[2];
		int d = key[3];
		for (int i = 0; i < 32; i++) {
			sum -= 1640531527;
			y += (((z << 4) + a) ^ (z + sum)) ^ ((z >> 5) + b);
			z += (((y << 4) + c) ^ (y + sum)) ^ ((y >> 5) + d);
		}
		tempInt[0] = y;
		tempInt[1] = z;
		return intToByte(tempInt, 0);
	}

	public byte[] decrypt(byte[] encryptContent, int offset, int[] key, int times) {
		int[] tempInt = byteToInt(encryptContent, offset);
		int y = tempInt[0];
		int z = tempInt[1];
		int sum = -957401312;
		int a = key[0];
		int b = key[1];
		int c = key[2];
		int d = key[3];
		for (int i = 0; i < 32; i++) {
			z -= (((y << 4) + c) ^ (y + sum)) ^ ((y >> 5) + d);
			y -= (((z << 4) + a) ^ (z + sum)) ^ ((z >> 5) + b);
			sum -= -1640531527;
		}
		tempInt[0] = y;
		tempInt[1] = z;
		return intToByte(tempInt, 0);
	}

	public int[] byteToInt(byte[] content, int offset) {
		int[] result = new int[(content.length >> 2)];
		int i = 0;
		for (int j = offset; j < content.length; j += 4) {
			result[i] = ((transform(content[j + 3]) | (transform(content[j + 2]) << 8)) | (transform(content[j + 1]) << 16)) | (content[j] << 24);
			i++;
		}
		return result;
	}

	public byte[] intToByte(int[] content, int offset) {
		byte[] result = new byte[(content.length << 2)];
		int i = 0;
		for (int j = offset; j < result.length; j += 4) {
			result[j + 3] = (byte) (content[i] & MotionEventCompat.ACTION_MASK);
			result[j + 2] = (byte) ((content[i] >> 8) & MotionEventCompat.ACTION_MASK);
			result[j + 1] = (byte) ((content[i] >> 16) & MotionEventCompat.ACTION_MASK);
			result[j] = (byte) ((content[i] >> 24) & MotionEventCompat.ACTION_MASK);
			i++;
		}
		return result;
	}

	public static int transform(byte temp) {
		byte tempInt = temp;
		if (tempInt < (byte) 0) {
			return tempInt + 256;
		}
		return tempInt;
	}
}
