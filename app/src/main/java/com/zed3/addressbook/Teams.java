package com.zed3.addressbook;

import java.util.ArrayList;
import java.util.List;

public class Teams {
	private String alversion;
	private String id;
	private final List<Team> mParentTeams;
	private String pbxid;
	private String showflag;

	public Teams() {
		this.alversion = "";
		this.id = "";
		this.showflag = "";
		this.pbxid = "";
		this.mParentTeams = new ArrayList<Team>();
	}

	public void addParent(final Team team) {
		this.mParentTeams.add(team);
	}

	public String getAlversion() {
		return this.alversion;
	}

	public String getId() {
		return this.id;
	}

	public List<Team> getParentTeams() {
		return this.mParentTeams;
	}

	public String getPbxid() {
		return this.pbxid;
	}

	public String getShowflag() {
		return this.showflag;
	}

	public void setAlversion(final String alversion) {
		this.alversion = alversion;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setPbxid(final String pbxid) {
		this.pbxid = pbxid;
	}

	public void setShowflag(final String showflag) {
		this.showflag = showflag;
	}

	@Override
	public String toString() {
		return "Teams [alversion=" + this.alversion + "]";
	}
}
