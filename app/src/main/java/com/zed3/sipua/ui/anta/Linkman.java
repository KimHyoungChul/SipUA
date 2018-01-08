package com.zed3.sipua.ui.anta;

public class Linkman {
	public int imgId;
	public boolean isSelected;
	public String name;
	public String number;
	public boolean selectEnabled;

	@Override
	public boolean equals(final Object o) {
		return this.number.equals(((Linkman) o).number);
	}
}
