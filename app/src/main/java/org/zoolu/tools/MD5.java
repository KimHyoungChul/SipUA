package org.zoolu.tools;

public class MD5 extends MessageDigest {
	static byte[] zeropadding;
	byte[] block;
	int block_offset;
	long count;
	byte[] message_digest;
	int[] state;

	static {
		final byte[] zeropadding = new byte[64];
		zeropadding[0] = -128;
		MD5.zeropadding = zeropadding;
	}

	public MD5() {
		this.init();
	}

	public MD5(final String s) {
		this.init();
		this.update(s);
	}

	public MD5(final byte[] array) {
		this.init();
		this.update(array);
	}

	public MD5(final byte[] array, final int n, final int n2) {
		this.init();
		this.update(array, n, n2);
	}

	public static byte[] digest(final String s) {
		return new MD5(s).doFinal();
	}

	public static byte[] digest(final byte[] array) {
		return digest(array, 0, array.length);
	}

	public static byte[] digest(final byte[] array, final int n, final int n2) {
		return new MD5(array, n, n2).doFinal();
	}

	private void init() {
		this.count = 0L;
		this.block = new byte[64];
		this.block_offset = 0;
		(this.state = new int[4])[0] = 1732584193;
		this.state[1] = -271733879;
		this.state[2] = -1732584194;
		this.state[3] = 271733878;
		this.message_digest = null;
	}

	private static void transform(final int[] array, final byte[] array2) {
		final int n = array[0];
		final int n2 = array[1];
		final int n3 = array[2];
		final int n4 = array[3];
		final int[] array3 = {(array2[0] & 0xFF) | (array2[1] & 0xFF) << 8 | (array2[2] & 0xFF) << 16 | array2[3] << 24, (array2[4] & 0xFF) | (array2[5] & 0xFF) << 8 | (array2[6] & 0xFF) << 16 | array2[7] << 24, (array2[8] & 0xFF) | (array2[9] & 0xFF) << 8 | (array2[10] & 0xFF) << 16 | array2[11] << 24, (array2[12] & 0xFF) | (array2[13] & 0xFF) << 8 | (array2[14] & 0xFF) << 16 | array2[15] << 24, (array2[16] & 0xFF) | (array2[17] & 0xFF) << 8 | (array2[18] & 0xFF) << 16 | array2[19] << 24, (array2[20] & 0xFF) | (array2[21] & 0xFF) << 8 | (array2[22] & 0xFF) << 16 | array2[23] << 24, (array2[24] & 0xFF) | (array2[25] & 0xFF) << 8 | (array2[26] & 0xFF) << 16 | array2[27] << 24, (array2[28] & 0xFF) | (array2[29] & 0xFF) << 8 | (array2[30] & 0xFF) << 16 | array2[31] << 24, (array2[32] & 0xFF) | (array2[33] & 0xFF) << 8 | (array2[34] & 0xFF) << 16 | array2[35] << 24, (array2[36] & 0xFF) | (array2[37] & 0xFF) << 8 | (array2[38] & 0xFF) << 16 | array2[39] << 24, (array2[40] & 0xFF) | (array2[41] & 0xFF) << 8 | (array2[42] & 0xFF) << 16 | array2[43] << 24, (array2[44] & 0xFF) | (array2[45] & 0xFF) << 8 | (array2[46] & 0xFF) << 16 | array2[47] << 24, (array2[48] & 0xFF) | (array2[49] & 0xFF) << 8 | (array2[50] & 0xFF) << 16 | array2[51] << 24, (array2[52] & 0xFF) | (array2[53] & 0xFF) << 8 | (array2[54] & 0xFF) << 16 | array2[55] << 24, (array2[56] & 0xFF) | (array2[57] & 0xFF) << 8 | (array2[58] & 0xFF) << 16 | array2[59] << 24, (array2[60] & 0xFF) | (array2[61] & 0xFF) << 8 | (array2[62] & 0xFF) << 16 | array2[63] << 24};
		final int n5 = n + (((n2 & n3) | (~n2 & n4)) + array3[0] - 680876936);
		final int n6 = (n5 << 7 | n5 >>> 25) + n2;
		final int n7 = n4 + (((n6 & n2) | (~n6 & n3)) + array3[1] - 389564586);
		final int n8 = (n7 << 12 | n7 >>> 20) + n6;
		final int n9 = n3 + (((n8 & n6) | (~n8 & n2)) + array3[2] + 606105819);
		final int n10 = (n9 << 17 | n9 >>> 15) + n8;
		final int n11 = n2 + (((n10 & n8) | (~n10 & n6)) + array3[3] - 1044525330);
		final int n12 = (n11 << 22 | n11 >>> 10) + n10;
		final int n13 = n6 + (((n12 & n10) | (~n12 & n8)) + array3[4] - 176418897);
		final int n14 = (n13 << 7 | n13 >>> 25) + n12;
		final int n15 = n8 + (((n14 & n12) | (~n14 & n10)) + array3[5] + 1200080426);
		final int n16 = (n15 << 12 | n15 >>> 20) + n14;
		final int n17 = n10 + (((n16 & n14) | (~n16 & n12)) + array3[6] - 1473231341);
		final int n18 = (n17 << 17 | n17 >>> 15) + n16;
		final int n19 = n12 + (((n18 & n16) | (~n18 & n14)) + array3[7] - 45705983);
		final int n20 = (n19 << 22 | n19 >>> 10) + n18;
		final int n21 = n14 + (((n20 & n18) | (~n20 & n16)) + array3[8] + 1770035416);
		final int n22 = (n21 << 7 | n21 >>> 25) + n20;
		final int n23 = n16 + (((n22 & n20) | (~n22 & n18)) + array3[9] - 1958414417);
		final int n24 = (n23 << 12 | n23 >>> 20) + n22;
		final int n25 = n18 + (((n24 & n22) | (~n24 & n20)) + array3[10] - 42063);
		final int n26 = (n25 << 17 | n25 >>> 15) + n24;
		final int n27 = n20 + (((n26 & n24) | (~n26 & n22)) + array3[11] - 1990404162);
		final int n28 = (n27 << 22 | n27 >>> 10) + n26;
		final int n29 = n22 + (((n28 & n26) | (~n28 & n24)) + array3[12] + 1804603682);
		final int n30 = (n29 << 7 | n29 >>> 25) + n28;
		final int n31 = n24 + (((n30 & n28) | (~n30 & n26)) + array3[13] - 40341101);
		final int n32 = (n31 << 12 | n31 >>> 20) + n30;
		final int n33 = n26 + (((n32 & n30) | (~n32 & n28)) + array3[14] - 1502002290);
		final int n34 = (n33 << 17 | n33 >>> 15) + n32;
		final int n35 = n28 + (((n34 & n32) | (~n34 & n30)) + array3[15] + 1236535329);
		final int n36 = (n35 << 22 | n35 >>> 10) + n34;
		final int n37 = n30 + (((n36 & n32) | (~n32 & n34)) + array3[1] - 165796510);
		final int n38 = (n37 << 5 | n37 >>> 27) + n36;
		final int n39 = n32 + (((n38 & n34) | (~n34 & n36)) + array3[6] - 1069501632);
		final int n40 = (n39 << 9 | n39 >>> 23) + n38;
		final int n41 = n34 + (((n40 & n36) | (~n36 & n38)) + array3[11] + 643717713);
		final int n42 = (n41 << 14 | n41 >>> 18) + n40;
		final int n43 = n36 + (((n42 & n38) | (~n38 & n40)) + array3[0] - 373897302);
		final int n44 = (n43 << 20 | n43 >>> 12) + n42;
		final int n45 = n38 + (((n44 & n40) | (~n40 & n42)) + array3[5] - 701558691);
		final int n46 = (n45 << 5 | n45 >>> 27) + n44;
		final int n47 = n40 + (((n46 & n42) | (~n42 & n44)) + array3[10] + 38016083);
		final int n48 = (n47 << 9 | n47 >>> 23) + n46;
		final int n49 = n42 + (((n48 & n44) | (~n44 & n46)) + array3[15] - 660478335);
		final int n50 = (n49 << 14 | n49 >>> 18) + n48;
		final int n51 = n44 + (((n50 & n46) | (~n46 & n48)) + array3[4] - 405537848);
		final int n52 = (n51 << 20 | n51 >>> 12) + n50;
		final int n53 = n46 + (((n52 & n48) | (~n48 & n50)) + array3[9] + 568446438);
		final int n54 = (n53 << 5 | n53 >>> 27) + n52;
		final int n55 = n48 + (((n54 & n50) | (~n50 & n52)) + array3[14] - 1019803690);
		final int n56 = (n55 << 9 | n55 >>> 23) + n54;
		final int n57 = n50 + (((n56 & n52) | (~n52 & n54)) + array3[3] - 187363961);
		final int n58 = (n57 << 14 | n57 >>> 18) + n56;
		final int n59 = n52 + (((n58 & n54) | (~n54 & n56)) + array3[8] + 1163531501);
		final int n60 = (n59 << 20 | n59 >>> 12) + n58;
		final int n61 = n54 + (((n60 & n56) | (~n56 & n58)) + array3[13] - 1444681467);
		final int n62 = (n61 << 5 | n61 >>> 27) + n60;
		final int n63 = n56 + (((n62 & n58) | (~n58 & n60)) + array3[2] - 51403784);
		final int n64 = (n63 << 9 | n63 >>> 23) + n62;
		final int n65 = n58 + (((n64 & n60) | (~n60 & n62)) + array3[7] + 1735328473);
		final int n66 = (n65 << 14 | n65 >>> 18) + n64;
		final int n67 = n60 + (((n66 & n62) | (~n62 & n64)) + array3[12] - 1926607734);
		final int n68 = (n67 << 20 | n67 >>> 12) + n66;
		final int n69 = n62 + ((n68 ^ n66 ^ n64) + array3[5] - 378558);
		final int n70 = (n69 << 4 | n69 >>> 28) + n68;
		final int n71 = n64 + ((n70 ^ n68 ^ n66) + array3[8] - 2022574463);
		final int n72 = (n71 << 11 | n71 >>> 21) + n70;
		final int n73 = n66 + ((n72 ^ n70 ^ n68) + array3[11] + 1839030562);
		final int n74 = (n73 << 16 | n73 >>> 16) + n72;
		final int n75 = n68 + ((n74 ^ n72 ^ n70) + array3[14] - 35309556);
		final int n76 = (n75 << 23 | n75 >>> 9) + n74;
		final int n77 = n70 + ((n76 ^ n74 ^ n72) + array3[1] - 1530992060);
		final int n78 = (n77 << 4 | n77 >>> 28) + n76;
		final int n79 = n72 + ((n78 ^ n76 ^ n74) + array3[4] + 1272893353);
		final int n80 = (n79 << 11 | n79 >>> 21) + n78;
		final int n81 = n74 + ((n80 ^ n78 ^ n76) + array3[7] - 155497632);
		final int n82 = (n81 << 16 | n81 >>> 16) + n80;
		final int n83 = n76 + ((n82 ^ n80 ^ n78) + array3[10] - 1094730640);
		final int n84 = (n83 << 23 | n83 >>> 9) + n82;
		final int n85 = n78 + ((n84 ^ n82 ^ n80) + array3[13] + 681279174);
		final int n86 = (n85 << 4 | n85 >>> 28) + n84;
		final int n87 = n80 + ((n86 ^ n84 ^ n82) + array3[0] - 358537222);
		final int n88 = (n87 << 11 | n87 >>> 21) + n86;
		final int n89 = n82 + ((n88 ^ n86 ^ n84) + array3[3] - 722521979);
		final int n90 = (n89 << 16 | n89 >>> 16) + n88;
		final int n91 = n84 + ((n90 ^ n88 ^ n86) + array3[6] + 76029189);
		final int n92 = (n91 << 23 | n91 >>> 9) + n90;
		final int n93 = n86 + ((n92 ^ n90 ^ n88) + array3[9] - 640364487);
		final int n94 = (n93 << 4 | n93 >>> 28) + n92;
		final int n95 = n88 + ((n94 ^ n92 ^ n90) + array3[12] - 421815835);
		final int n96 = (n95 << 11 | n95 >>> 21) + n94;
		final int n97 = n90 + ((n96 ^ n94 ^ n92) + array3[15] + 530742520);
		final int n98 = (n97 << 16 | n97 >>> 16) + n96;
		final int n99 = n92 + ((n98 ^ n96 ^ n94) + array3[2] - 995338651);
		final int n100 = (n99 << 23 | n99 >>> 9) + n98;
		final int n101 = n94 + (((~n96 | n100) ^ n98) + array3[0] - 198630844);
		final int n102 = (n101 << 6 | n101 >>> 26) + n100;
		final int n103 = n96 + (((~n98 | n102) ^ n100) + array3[7] + 1126891415);
		final int n104 = (n103 << 10 | n103 >>> 22) + n102;
		final int n105 = n98 + (((~n100 | n104) ^ n102) + array3[14] - 1416354905);
		final int n106 = (n105 << 15 | n105 >>> 17) + n104;
		final int n107 = n100 + (((~n102 | n106) ^ n104) + array3[5] - 57434055);
		final int n108 = (n107 << 21 | n107 >>> 11) + n106;
		final int n109 = n102 + (((~n104 | n108) ^ n106) + array3[12] + 1700485571);
		final int n110 = (n109 << 6 | n109 >>> 26) + n108;
		final int n111 = n104 + (((~n106 | n110) ^ n108) + array3[3] - 1894986606);
		final int n112 = (n111 << 10 | n111 >>> 22) + n110;
		final int n113 = n106 + (((~n108 | n112) ^ n110) + array3[10] - 1051523);
		final int n114 = (n113 << 15 | n113 >>> 17) + n112;
		final int n115 = n108 + (((~n110 | n114) ^ n112) + array3[1] - 2054922799);
		final int n116 = (n115 << 21 | n115 >>> 11) + n114;
		final int n117 = n110 + (((~n112 | n116) ^ n114) + array3[8] + 1873313359);
		final int n118 = (n117 << 6 | n117 >>> 26) + n116;
		final int n119 = n112 + (((~n114 | n118) ^ n116) + array3[15] - 30611744);
		final int n120 = (n119 << 10 | n119 >>> 22) + n118;
		final int n121 = n114 + (((~n116 | n120) ^ n118) + array3[6] - 1560198380);
		final int n122 = (n121 << 15 | n121 >>> 17) + n120;
		final int n123 = n116 + (((~n118 | n122) ^ n120) + array3[13] + 1309151649);
		final int n124 = (n123 << 21 | n123 >>> 11) + n122;
		final int n125 = n118 + (((~n120 | n124) ^ n122) + array3[4] - 145523070);
		final int n126 = (n125 << 6 | n125 >>> 26) + n124;
		final int n127 = n120 + (((~n122 | n126) ^ n124) + array3[11] - 1120210379);
		final int n128 = (n127 << 10 | n127 >>> 22) + n126;
		final int n129 = n122 + (((~n124 | n128) ^ n126) + array3[2] + 718787259);
		final int n130 = (n129 << 15 | n129 >>> 17) + n128;
		final int n131 = n124 + (((~n126 | n130) ^ n128) + array3[9] - 343485551);
		array[0] += n126;
		array[1] += (n131 << 21 | n131 >>> 11) + n130;
		array[2] += n130;
		array[3] += n128;
	}

	@Override
	public byte[] doFinal() {
		if (this.message_digest != null) {
			return this.message_digest;
		}
		final int block_offset = this.block_offset;
		long n = this.count * 8L;
		final byte[] array = new byte[8];
		for (int i = 0; i < 8; ++i) {
			array[i] = (byte) (n % 256L);
			n >>= 8;
		}
		this.update(MD5.zeropadding, 0, 64 - (block_offset + 8) % 64);
		this.update(array, 0, 8);
		this.message_digest = new byte[16];
		int j = 0;
		int n2 = 0;
		while (j < 4) {
			final byte[] message_digest = this.message_digest;
			final int n3 = n2 + 1;
			message_digest[n2] = (byte) (this.state[j] & 0xFF);
			final byte[] message_digest2 = this.message_digest;
			final int n4 = n3 + 1;
			message_digest2[n3] = (byte) (this.state[j] >>> 8 & 0xFF);
			final byte[] message_digest3 = this.message_digest;
			final int n5 = n4 + 1;
			message_digest3[n4] = (byte) (this.state[j] >>> 16 & 0xFF);
			final byte[] message_digest4 = this.message_digest;
			n2 = n5 + 1;
			message_digest4[n5] = (byte) (this.state[j] >>> 24 & 0xFF);
			++j;
		}
		return this.message_digest;
	}

	@Override
	public MessageDigest update(final byte[] array, int n, int i) {
		if (this.message_digest != null) {
			return this;
		}
		this.count += i;
		for (int n2 = this.block.length - this.block_offset; i >= n2; i -= n2, this.block_offset = 0, n2 = this.block.length - this.block_offset) {
			for (int j = 0; j < n2; ++j) {
				this.block[this.block_offset + j] = array[n + j];
			}
			transform(this.state, this.block);
			n += n2;
		}
		for (int k = 0; k < i; ++k) {
			this.block[this.block_offset + k] = array[n + k];
		}
		this.block_offset += i;
		return this;
	}
}
