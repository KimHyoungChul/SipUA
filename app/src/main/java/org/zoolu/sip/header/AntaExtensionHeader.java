package org.zoolu.sip.header;

public class AntaExtensionHeader extends Header {
	private static final String VAPREFIX_VIDEO_CONTROL = "video-control ";
	public static final int VIDEO_PARAMETER_NOT_FOUND = -1;

	public AntaExtensionHeader(final int n) {
		super("Anta-Extension", "video-control " + n);
	}

	public AntaExtensionHeader(final String s) {
		super("Anta-Extension", s);
	}

	public AntaExtensionHeader(final Header header) {
		super(header);
	}

	public static AntaExtensionHeader obtain(final Header header) {
		return new AntaExtensionHeader(header);
	}

	public int getVideoControlParameter() {
		if (this.isVideoControl()) {
			final String value = this.getValue();
			return Integer.valueOf(value.substring("video-control ".length(), value.length()));
		}
		return -1;
	}

	public boolean isVideoControl() {
		final String value = this.getValue();
		return value != null && value.contains("video-control ");
	}
}
