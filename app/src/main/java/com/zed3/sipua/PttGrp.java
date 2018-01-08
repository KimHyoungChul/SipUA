package com.zed3.sipua;

import java.util.HashMap;

public class PttGrp implements Cloneable {
	private static HashMap<String, Integer> grpIsAnswsered;
	public String grpID;
	public String grpName;
	public boolean isCreateSession;
	public long lastRcvTime;
	public int level;
	public Object oVoid;
	public int report_heartbeat;
	public String speaker;
	public String speakerN;
	public E_Grp_State state;
	private int type;
	public int update_heartbeat;

	public PttGrp() {
		this.grpName = "--";
		this.grpID = "--";
		this.speaker = "--";
		this.speakerN = "--";
		this.state = E_Grp_State.GRP_STATE_SHOUDOWN;
		this.type = 0;
		this.grpName = new String();
		this.grpID = new String();
		PttGrp.grpIsAnswsered = new HashMap<String, Integer>();
		this.isCreateSession = false;
		this.oVoid = null;
	}

	static boolean GetGrpAnswerState(final String s, final int n) {
		final Integer value = PttGrp.grpIsAnswsered.get(s);
		return value != null && n == Integer.valueOf(value.toString());
	}

	static void SetGrpAnswerState(final String s, final int n) {
		PttGrp.grpIsAnswsered.put(s, n);
	}

	@Override
	protected PttGrp clone() {
		try {
			return (PttGrp) super.clone();
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return new PttGrp();
		}
	}

	public boolean equals(final PttGrp pttGrp) {
		return this.grpID.equals(pttGrp.getGrpID());
	}

	@Override
	public boolean equals(final Object o) {
		return this.grpID.equals(((PttGrp) o).grpID);
	}

	public String getGrpID() {
		return this.grpID;
	}

	public String getGrpName() {
		return this.grpName;
	}

	public int getLevel() {
		return this.level;
	}

	public int getReport_heartbeat() {
		return this.report_heartbeat;
	}

	public int getType() {
		return this.type;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public void setGrpID(final String grpID) {
		this.grpID = grpID;
	}

	public void setGrpName(final String grpName) {
		this.grpName = grpName;
	}

	public void setLevel(final int level) {
		this.level = level;
	}

	public void setReport_heartbeat(final int report_heartbeat) {
		this.report_heartbeat = report_heartbeat;
	}

	public void setType(final int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "PttGrp [grpName=" + this.grpName + ", grpID=" + this.grpID + ", speaker=" + this.speaker + ", speakerN=" + this.speakerN + ", report_heartbeat=" + this.report_heartbeat + ", update_heartbeat=" + this.update_heartbeat + ", level=" + this.level + ", lastRcvTime=" + this.lastRcvTime + ", state=" + this.state + ", oVoid=" + this.oVoid + ", isCreateSession=" + this.isCreateSession + ", type=" + this.type + "]";
	}

	public enum E_Grp_State {
		GRP_STATE_SHOUDOWN("GRP_STATE_SHOUDOWN", 0),
		GRP_STATE_IDLE("GRP_STATE_IDLE", 1),
		GRP_STATE_INITIATING("GRP_STATE_INITIATING", 2),
		GRP_STATE_TALKING("GRP_STATE_TALKING", 3),
		GRP_STATE_LISTENING("GRP_STATE_LISTENING", 4),
		GRP_STATE_QUEUE("GRP_STATE_QUEUE", 5);

		private E_Grp_State(final String s, final int n) {
		}
	}
}
