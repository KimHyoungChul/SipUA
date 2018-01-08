package com.zed3.addressbook;

import java.util.ArrayList;

public class Team {
	private String id;
	private final ArrayList<Member> memberList;
	private String name;
	private Team parent;
	private final ArrayList<Team> teamList;

	public Team() {
		this.name = "";
		this.id = "";
		this.memberList = new ArrayList<Member>();
		this.teamList = new ArrayList<Team>();
	}

	public void addMember(final Member member) {
		this.memberList.add(member);
	}

	public void addTeam(final Team team) {
		this.teamList.add(team);
	}

	public String getId() {
		return this.id;
	}

	public ArrayList<Member> getMemberList() {
		return this.memberList;
	}

	public String getName() {
		return this.name;
	}

	public Team getParent() {
		return this.parent;
	}

	public ArrayList<Team> getTeamList() {
		return this.teamList;
	}

	public boolean isParent() {
		return this.parent != null;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setParent(final Team parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "Team [name=" + this.name + ", id=" + this.id + "]";
	}
}
