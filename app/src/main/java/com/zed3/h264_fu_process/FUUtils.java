package com.zed3.h264_fu_process;

public class FUUtils {
	private static String head = "00000000";

	public static byte getType(byte type) {
		return (byte) (Byte.parseByte("00011111", 2) & type);
	}

	public static byte setType(byte b, byte tYPE) {
		return (byte) (((byte) (Short.parseShort(head + "11100000", 2) & b)) + tYPE);
	}

	public static byte getNri(byte nri) {
		return (byte) (((byte) (Byte.parseByte("01100000", 2) & nri)) >> 5);
	}

	public static byte setNri(byte b, byte nRI) {
		return (byte) ((nRI << 5) + ((byte) (((byte) Short.parseShort(head + "10011111", 2)) & b)));
	}

	public static byte getF(byte f) {
		return (byte) (((byte) (Short.parseShort(head + "10000000", 2) & f)) >> 7);
	}

	public static byte setF(byte b, byte f) {
		return (byte) ((f << 7) + ((byte) (Byte.parseByte("01111111", 2) & b)));
	}

	public static byte getR(byte r) {
		return (byte) (((byte) (Byte.parseByte("00100000", 2) & r)) >> 5);
	}

	public static byte setR(byte b, byte r) {
		return (byte) (((byte) (Short.parseShort(head + "11011111", 2) & b)) + ((byte) (r << 5)));
	}

	public static byte getE(byte e) {
		return (byte) ((Byte.parseByte("01000000", 2) & e) >> 6);
	}

	public static byte setE(byte b, byte e) {
		return (byte) (((byte) (Short.parseShort(head + "10111111", 2) & b)) + ((byte) (e << 6)));
	}

	public static byte getS(byte s) {
		return (byte) ((Byte.parseByte("10000000", 2) & s) >> 7);
	}

	public static byte setS(byte b, byte s) {
		return (byte) (((byte) (Byte.parseByte("01111111", 2) & b)) + ((byte) (s << 7)));
	}
}
