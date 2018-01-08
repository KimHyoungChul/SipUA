package com.zed3.codecs;

public class EncodeRate {
	public enum Mode {
		MR102("MR102", 6),
		MR122("MR122", 7),
		MR475("MR475", 0),
		MR515("MR515", 1),
		MR59("MR59", 2),
		MR67("MR67", 3),
		MR74("MR74", 4),
		MR795("MR795", 5),
		MRDTX("MRDTX", 8),
		N_MODES("N_MODES", 9);

		private Mode(final String s, final int n) {
		}
	}
}
