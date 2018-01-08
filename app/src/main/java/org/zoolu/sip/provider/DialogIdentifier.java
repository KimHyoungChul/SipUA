package org.zoolu.sip.provider;

public class DialogIdentifier extends Identifier {
	public DialogIdentifier(final String s, final String s2, final String s3) {
		this.id = String.valueOf(s) + "-" + s2 + "-" + s3;
	}

	public DialogIdentifier(final DialogIdentifier dialogIdentifier) {
		super(dialogIdentifier);
	}
}
