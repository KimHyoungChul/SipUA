package org.zoolu.sdp;

public class SessionNameField extends SdpField {
	public SessionNameField() {
		super('s', " ");
	}

	public SessionNameField(final String s) {
		super('s', s);
	}

	public SessionNameField(final SdpField sdpField) {
		super(sdpField);
	}

	public String getSession() {
		return this.value;
	}
}
