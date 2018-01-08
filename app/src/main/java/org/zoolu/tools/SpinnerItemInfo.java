package org.zoolu.tools;

public class SpinnerItemInfo {
	public String ID;
	public String Name;

	public SpinnerItemInfo() {
		this.ID = "";
		this.Name = "";
	}

	@Override
	public String toString() {
		return this.Name;
	}
}
