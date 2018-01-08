package com.zed3.addressbook;

public class Member {
	private String audio;
	private String dtype;
	private String gps;
	private String mName;
	private String mtype;
	private String number;
	private Team parent;
	private String phone;
	private String pictureupload;
	private String position;
	private String pttmap;
	private String sex;
	private String showflag;
	private String smsswitch;
	private String video;

	public Member() {
		this.number = "";
		this.mName = "";
		this.mtype = "";
		this.dtype = "";
		this.sex = "";
		this.position = "";
		this.phone = "";
		this.video = "";
		this.audio = "";
		this.pttmap = "";
		this.gps = "";
		this.pictureupload = "";
		this.smsswitch = "";
		this.showflag = "";
	}

	public String getAudio() {
		return this.audio;
	}

	public String getDtype() {
		return this.dtype;
	}

	public String getGps() {
		return this.gps;
	}

	public String getMtype() {
		return this.mtype;
	}

	public String getNumber() {
		return this.number;
	}

	public Team getParent() {
		return this.parent;
	}

	public String getPhone() {
		return this.phone;
	}

	public String getPictureupload() {
		return this.pictureupload;
	}

	public String getPosition() {
		return this.position;
	}

	public String getPttmap() {
		return this.pttmap;
	}

	public String getSex() {
		return this.sex;
	}

	public String getShowflag() {
		return this.showflag;
	}

	public String getSmsswitch() {
		return this.smsswitch;
	}

	public String getTitle() {
		if (this.parent != null) {
			return this.parent.getName();
		}
		return null;
	}

	public String getVideo() {
		return this.video;
	}

	public String getmName() {
		return this.mName;
	}

	public void setAudio(final String audio) {
		this.audio = audio;
	}

	public void setDtype(final String dtype) {
		this.dtype = dtype;
	}

	public void setGps(final String gps) {
		this.gps = gps;
	}

	public void setMtype(final String mtype) {
		this.mtype = mtype;
	}

	public void setNumber(final String number) {
		this.number = number;
	}

	public void setParent(final Team parent) {
		this.parent = parent;
	}

	public void setPhone(final String phone) {
		this.phone = phone;
	}

	public void setPictureupload(final String pictureupload) {
		this.pictureupload = pictureupload;
	}

	public void setPosition(final String position) {
		this.position = position;
	}

	public void setPttmap(final String pttmap) {
		this.pttmap = pttmap;
	}

	public void setSex(final String sex) {
		this.sex = sex;
	}

	public void setShowflag(final String showflag) {
		this.showflag = showflag;
	}

	public void setSmsswitch(final String smsswitch) {
		this.smsswitch = smsswitch;
	}

	public void setVideo(final String video) {
		this.video = video;
	}

	public void setmName(final String mName) {
		this.mName = mName;
	}

	@Override
	public String toString() {
		return "Member [number=" + this.number + ", mName=" + this.mName + ", mtype=" + this.mtype + ", parent=" + this.parent.getName() + "]";
	}

	public enum UserType {
		AUDIO_MAIL("AUDIO_MAIL", 11, "4,3"),
		BALANCE_GRP("BALANCE_GRP", 14, "5,2"),
		DMR("DMR", 7, "3,2"),
		EMERGENCY_GRP("EMERGENCY_GRP", 15, "5,3"),
		EXTERNAL_OTHER("EXTERNAL_OTHER", 18, "7,1"),
		EXTERNAL_UC("EXTERNAL_UC", 17, "7,0"),
		GRP_NUM("GRP_NUM", 9, "4,1"),
		GTS("GTS", 5, "3,0"),
		IP_PHONE("IP_PHONE", 16, "6,0"),
		MEET_NUM("MEET_NUM", 10, "4,2"),
		MOBILE_GQT("MOBILE_GQT", 2, "1,0"),
		PDT("PDT", 6, "3,1"),
		PRIORITY_RING_GRP("PRIORITY_RING_GRP", 13, "5,1"),
		RING_GRP("RING_GRP", 12, "5,0"),
		SVP("SVP", 0, "0,0"),
		SVP_OTHER("SVP_OTHER", 1, "0,1"),
		TRIGGER_NUM("TRIGGER_NUM", 8, "4,0"),
		UNKNOW("UNKNOW", 19, ""),
		VIDEO_MONITOR_GB28181("VIDEO_MONITOR_GB28181", 4, "2,1"),
		VIDEO_MONITOR_GVS("VIDEO_MONITOR_GVS", 3, "2,0");

		private String mUtype;

		private UserType(final String s, final int n, final String mUtype) {
			this.mUtype = mUtype;
		}

		public static UserType toUserType(final String s) {
			if (s != null) {
				final UserType[] values = values();
				for (int i = 0; i < values.length; ++i) {
					final UserType unknow;
					if ((unknow = values[i]).convert().equals(s)) {
						return unknow;
					}
				}
				return UserType.UNKNOW;
			}
			return UserType.UNKNOW;
		}

		public String convert() {
			return this.mUtype;
		}
	}
}
