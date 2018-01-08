package org.zoolu.tools;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupListInfo implements Parcelable {
	public static final Parcelable.Creator<GroupListInfo> CREATOR;
	public String GrpName;
	public String GrpNum;
	public String GrpState;

	static {
		CREATOR = (Parcelable.Creator) new Parcelable.Creator<GroupListInfo>() {
			public GroupListInfo createFromParcel(final Parcel parcel) {
				return new GroupListInfo(parcel);
			}

			public GroupListInfo[] newArray(final int n) {
				return new GroupListInfo[n];
			}
		};
	}

	public GroupListInfo() {
		this.GrpName = "";
		this.GrpNum = "";
		this.GrpState = "";
	}

	public GroupListInfo(final Parcel parcel) {
		this.GrpName = "";
		this.GrpNum = "";
		this.GrpState = "";
		this.GrpName = parcel.readString();
		this.GrpNum = parcel.readString();
		this.GrpState = parcel.readString();
	}

	public String GrpName() {
		return this.GrpName;
	}

	public String GrpNum() {
		return this.GrpNum;
	}

	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}

	public String getGrpName() {
		return this.GrpName;
	}

	public String getGrpNum() {
		return this.GrpNum;
	}

	public String getGrpState() {
		return this.GrpState;
	}

	public void setGrpName(final String grpName) {
		this.GrpName = grpName;
	}

	public void setGrpNum(final String grpNum) {
		this.GrpNum = grpNum;
	}

	public void setGrpState(final String grpState) {
		this.GrpState = grpState;
	}

	@Override
	public String toString() {
		return "GroupListInfo [GrpName=" + this.GrpName + ", GrpNum=" + this.GrpNum + ", GrpState=" + this.GrpState + "]";
	}

	public void writeToParcel(final Parcel parcel, final int n) {
		parcel.writeString(this.GrpName);
		parcel.writeString(this.GrpNum);
		parcel.writeString(this.GrpState);
	}
}
