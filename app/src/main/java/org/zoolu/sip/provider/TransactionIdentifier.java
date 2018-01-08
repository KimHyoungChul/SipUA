package org.zoolu.sip.provider;

public class TransactionIdentifier extends Identifier {
	public TransactionIdentifier(final String id) {
		this.id = id;
	}

	public TransactionIdentifier(final String s, final long n, final String s2, final String s3, String s4) {
		String s5 = s4;
		if (s4 == null) {
			s5 = "";
		}
		s4 = s2;
		if (s2.equals("ACK")) {
			s4 = "INVITE";
		}
		this.id = String.valueOf(s) + "-" + n + "-" + s4 + "-" + s3 + "-" + s5;
	}

	public TransactionIdentifier(final TransactionIdentifier transactionIdentifier) {
		super(transactionIdentifier);
	}
}
