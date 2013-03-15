package com.sociocast.android.model;

public class EntityAttributes {

	public static final int ACTION_ADD = 0;
	public static final int ACTION_DELETE = 1;
	public static final int ACTION_SET = 3;
	
	/*
	 * Entity ID to be modified
	 */
	private String eid;
	
	/*
	 * Update, set, delete, add
	 */
	private int action;

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

}
