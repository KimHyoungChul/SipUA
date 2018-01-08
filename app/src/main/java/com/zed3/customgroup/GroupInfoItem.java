package com.zed3.customgroup;

public class GroupInfoItem {
	private boolean grp_img;
	private String grp_uDept;
	private String grp_uName;
	private String grp_uNumber;

	public String getGrp_uDept() {
		return this.grp_uDept;
	}

	public String getGrp_uName() {
		return this.grp_uName;
	}

	public String getGrp_uNumber() {
		return this.grp_uNumber;
	}

	public boolean isGrp_img() {
		return this.grp_img;
	}

	public void setGrp_img(final boolean grp_img) {
		this.grp_img = grp_img;
	}

	public void setGrp_uDept(final String grp_uDept) {
		this.grp_uDept = grp_uDept;
	}

	public void setGrp_uName(final String grp_uName) {
		this.grp_uName = grp_uName;
	}

	public void setGrp_uNumber(final String grp_uNumber) {
		this.grp_uNumber = grp_uNumber;
	}

	@Override
	public String toString() {
		return "GroupInfoItem [grp_img=" + this.grp_img + ", grp_uName=" + this.grp_uName + ", grp_uNumber=" + this.grp_uNumber + ", grp_uDept=" + this.grp_uDept + "]";
	}
}
