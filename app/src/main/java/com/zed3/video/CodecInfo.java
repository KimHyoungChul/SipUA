package com.zed3.video;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@TargetApi(16)
public abstract class CodecInfo {
	public static final String MEDIA_CODEC_TYPE_H263 = "video/3gpp";
	public static final String MEDIA_CODEC_TYPE_H264 = "video/avc";
	public static final String MEDIA_CODEC_TYPE_H265 = "video/hevc";
	public static final String MEDIA_CODEC_TYPE_VP8 = "video/x-vnd.on2.vp8";
	private static final List<String> bannedYuvCodecs;
	private static final List<CodecInfo> codecs;
	public static List<String> supportCodecType;
	private boolean banned;
	protected final MediaCodecInfo.CodecCapabilities caps;
	protected final MediaCodecInfo codecInfo;
	protected final ArrayList<CodecColorFormat> colors;
	private final String mediaType;
	private ProfileLevel[] profileLevels;

	static {
		codecs = new ArrayList<CodecInfo>();
		CodecInfo.supportCodecType = new ArrayList<String>();
		(bannedYuvCodecs = new ArrayList<String>()).add("OMX.SEC.avc.enc");
		CodecInfo.bannedYuvCodecs.add("OMX.SEC.h263.enc");
		CodecInfo.bannedYuvCodecs.add("OMX.Nvidia.h264.decode");
		CodecInfo.bannedYuvCodecs.add("OMX.SEC.vp8.dec");
		CodecInfo.bannedYuvCodecs.add("OMX.google.vpx.encoder");
		for (int i = 0; i < MediaCodecList.getCodecCount(); ++i) {
			final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
			final CodecInfo codecInfo2 = getCodecInfo(codecInfo);
			CodecInfo.supportCodecType = getSupportCodecType(codecInfo);
			if (codecInfo2 != null) {
				CodecInfo.codecs.add(codecInfo2);
			}
		}
	}

	public CodecInfo(final MediaCodecInfo codecInfo, final String mediaType) {
		this.codecInfo = codecInfo;
		this.mediaType = mediaType;
		this.caps = codecInfo.getCapabilitiesForType(mediaType);
		this.colors = new ArrayList<CodecColorFormat>();
		final int[] colorFormats = this.caps.colorFormats;
		for (int length = colorFormats.length, i = 0; i < length; ++i) {
			this.colors.add(CodecColorFormat.fromInt(colorFormats[i]));
		}
	}

	public static CodecInfo getCodecForType(final String s, final boolean b) {
		for (final CodecInfo codecInfo : CodecInfo.codecs) {
			if (!codecInfo.isBanned() && codecInfo.mediaType.equals(s) && codecInfo.codecInfo.isEncoder() == b) {
				return codecInfo;
			}
		}
		return null;
	}

	public static CodecInfo getCodecInfo(final MediaCodecInfo mediaCodecInfo) {
		if (CodecInfo.supportCodecType == null) {
			CodecInfo.supportCodecType = new ArrayList<String>();
		}
		final String[] supportedTypes = mediaCodecInfo.getSupportedTypes();
		final int length = supportedTypes.length;
		int i = 0;
		while (i < length) {
			final String s = supportedTypes[i];
			while (true) {
				try {
					if (s.equals("video/3gpp")) {
						break;
					}
					if (s.equals("video/avc")) {
						return new H264CodecInfo(mediaCodecInfo);
					}
					if (s.equals("video/x-vnd.on2.vp8") || s.equals("video/hevc")) {
						break;
					}
					++i;
				} catch (IllegalArgumentException ex) {
					continue;
				}
				break;
			}
		}
		return null;
	}

	private Level getLevel(final int n) {
		final Level[] levelSet = this.getLevelSet();
		for (int length = levelSet.length, i = 0; i < length; ++i) {
			final Level level;
			if ((level = levelSet[i]).value == n) {
				return level;
			}
		}
		return new Level("Unknown", n);
	}

	private Profile getProfile(final int n) {
		final Profile[] profileSet = this.getProfileSet();
		for (int length = profileSet.length, i = 0; i < length; ++i) {
			final Profile profile;
			if ((profile = profileSet[i]).value == n) {
				return profile;
			}
		}
		return new Profile("Unknown", n);
	}

	public static List<String> getSupportCodecType(final MediaCodecInfo mediaCodecInfo) {
		final String[] supportedTypes = mediaCodecInfo.getSupportedTypes();
		for (int length = supportedTypes.length, i = 0; i < length; ++i) {
			final String s = supportedTypes[i];
			if (!TextUtils.isEmpty((CharSequence) s) && s.contains("video")) {
				if (s.contains("vp8") && !CodecInfo.supportCodecType.contains("vp8")) {
					CodecInfo.supportCodecType.add("vp8");
				} else if (s.contains("vp9") && !CodecInfo.supportCodecType.contains("vp9")) {
					CodecInfo.supportCodecType.add("vp9");
				} else if (s.contains("avc") && !CodecInfo.supportCodecType.contains("h264")) {
					CodecInfo.supportCodecType.add("h264");
				} else if (s.contains("hevc") && !CodecInfo.supportCodecType.contains("h265")) {
					CodecInfo.supportCodecType.add("h265");
				} else if (s.contains("mp4v-es") && !CodecInfo.supportCodecType.contains("MPEG4")) {
					CodecInfo.supportCodecType.add("MPEG4");
				} else if (s.contains("3gpp") && !CodecInfo.supportCodecType.contains("h263")) {
					CodecInfo.supportCodecType.add("h263");
				}
			}
		}
		return CodecInfo.supportCodecType;
	}

	public static List<CodecInfo> getSupportedCodecs() {
		return Collections.unmodifiableList((List<? extends CodecInfo>) CodecInfo.codecs);
	}

	protected abstract Level[] getLevelSet();

	public String getLibjitsiEncoding() {
		if (this.mediaType.equals("video/3gpp")) {
			return "h263";
		}
		if (this.mediaType.equals("video/avc")) {
			return "h264";
		}
		if (this.mediaType.equals("video/x-vnd.on2.vp8")) {
			return "vp8";
		}
		if (this.mediaType.equals("video/hevc")) {
			return "h265";
		}
		return this.mediaType;
	}

	public String getName() {
		return this.codecInfo.getName();
	}

	public ProfileLevel[] getProfileLevels() {
		if (this.profileLevels == null) {
			final MediaCodecInfo.CodecProfileLevel[] profileLevels = this.caps.profileLevels;
			this.profileLevels = new ProfileLevel[profileLevels.length];
			for (int i = 0; i < this.profileLevels.length; ++i) {
				this.profileLevels[i] = new ProfileLevel(this.getProfile(profileLevels[i].profile), this.getLevel(profileLevels[i].level));
			}
		}
		return this.profileLevels;
	}

	protected abstract Profile[] getProfileSet();

	public boolean isBanned() {
		return this.banned;
	}

	public boolean isEncoder() {
		return this.codecInfo.isEncoder();
	}

	public boolean isNominated() {
		return getCodecForType(this.mediaType, this.isEncoder()) == this;
	}

	public void setBanned(final boolean banned) {
		this.banned = banned;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("\ncolors:\n");
		for (int i = 0; i < this.colors.size(); ++i) {
			sb.append(this.colors.get(i));
			if (i != this.colors.size() - 1) {
				sb.append(", \n");
			}
		}
		return String.valueOf(this.codecInfo.getName()) + "(" + this.getLibjitsiEncoding() + ")" + (Object) sb;
	}

	static class H263CodecInfo extends CodecInfo {
		private final Level[] LEVELS;
		private final Profile[] PROFILES;

		public H263CodecInfo(final MediaCodecInfo mediaCodecInfo) {
			super(mediaCodecInfo, "video/3gpp");
			this.PROFILES = new Profile[]{new Profile("Baseline", 1), new Profile("H320Coding", 2), new Profile("BackwardCompatible", 4), new Profile("ISWV2", 8), new Profile("ISWV3", 16), new Profile("HighCompression", 32), new Profile("Internet", 64), new Profile("Interlace", 128), new Profile("HighLatency", 256)};
			this.LEVELS = new Level[]{new Level("Level10", 1), new Level("Level20", 2), new Level("Level30", 4), new Level("Level40", 8), new Level("Level45", 16), new Level("Level50", 32), new Level("Level60", 64), new Level("Level70", 128)};
		}

		@Override
		protected Level[] getLevelSet() {
			return this.LEVELS;
		}

		@Override
		protected Profile[] getProfileSet() {
			return this.PROFILES;
		}
	}

	static class H264CodecInfo extends CodecInfo {
		private final Level[] LEVELS;
		private final Profile[] PROFILES;

		public H264CodecInfo(final MediaCodecInfo mediaCodecInfo) {
			super(mediaCodecInfo, "video/avc");
			this.PROFILES = new Profile[]{new Profile("ProfileBaseline", 1), new Profile("ProfileMain", 2), new Profile("ProfileExtended", 4), new Profile("ProfileHigh", 8), new Profile("ProfileHigh10", 16), new Profile("ProfileHigh422", 32), new Profile("ProfileHigh444", 64)};
			this.LEVELS = new Level[]{new Level("Level1", 1), new Level("Level1b", 2), new Level("Level11", 4), new Level("Level12", 8), new Level("Level13", 16), new Level("Level2", 32), new Level("Level21", 64), new Level("Level22", 128), new Level("Level3", 256), new Level("Level31", 512), new Level("Level32", 1024), new Level("Level4", 2048), new Level("Level41", 4096), new Level("Level42", 8192), new Level("Level5", 16384), new Level("Level51", 32768)};
		}

		@Override
		protected Level[] getLevelSet() {
			return this.LEVELS;
		}

		@Override
		protected Profile[] getProfileSet() {
			return this.PROFILES;
		}
	}

	static class H265CodecInfo extends CodecInfo {
		private final Level[] LEVELS;
		private final Profile[] PROFILES;

		public H265CodecInfo(final MediaCodecInfo mediaCodecInfo) {
			super(mediaCodecInfo, "video/hevc");
			this.PROFILES = new Profile[]{new Profile("ProfileBaseline", 1), new Profile("ProfileMain", 2), new Profile("ProfileExtended", 4), new Profile("ProfileHigh", 8), new Profile("ProfileHigh10", 16), new Profile("ProfileHigh422", 32), new Profile("ProfileHigh444", 64)};
			this.LEVELS = new Level[]{new Level("Level1", 1), new Level("Level1b", 2), new Level("Level11", 4), new Level("Level12", 8), new Level("Level13", 16), new Level("Level2", 32), new Level("Level21", 64), new Level("Level22", 128), new Level("Level3", 256), new Level("Level31", 512), new Level("Level32", 1024), new Level("Level4", 2048), new Level("Level41", 4096), new Level("Level42", 8192), new Level("Level5", 16384), new Level("Level51", 32768)};
		}

		@Override
		protected Level[] getLevelSet() {
			return this.LEVELS;
		}

		@Override
		protected Profile[] getProfileSet() {
			return this.PROFILES;
		}
	}

	public static class Level {
		private final String name;
		private final int value;

		public Level(final String name, final int value) {
			this.value = value;
			this.name = name;
		}

		@Override
		public String toString() {
			return String.valueOf(this.name) + "(0x" + Integer.toString(this.value, 16) + ")";
		}
	}

	public static class Profile {
		private final String name;
		private final int value;

		public Profile(final String name, final int value) {
			this.value = value;
			this.name = name;
		}

		@Override
		public String toString() {
			return String.valueOf(this.name) + "(0x" + Integer.toString(this.value, 16) + ")";
		}
	}

	public static class ProfileLevel {
		private final Level level;
		private final Profile profile;

		public ProfileLevel(final Profile profile, final Level level) {
			this.profile = profile;
			this.level = level;
		}

		@Override
		public String toString() {
			return "P: " + this.profile.toString() + " L: " + this.level.toString();
		}
	}

	static class VP8CodecInfo extends CodecInfo {
		private final Level[] LEVELS;
		private final Profile[] PROFILES;

		public VP8CodecInfo(final MediaCodecInfo mediaCodecInfo) {
			super(mediaCodecInfo, "video/x-vnd.on2.vp8");
			this.PROFILES = new Profile[]{new Profile("ProfileMain", 1)};
			this.LEVELS = new Level[]{new Level("Version0", 1), new Level("Version1", 2), new Level("Version2", 4), new Level("Version3", 8)};
		}

		@Override
		protected Level[] getLevelSet() {
			return this.LEVELS;
		}

		@Override
		protected Profile[] getProfileSet() {
			return this.PROFILES;
		}
	}
}
