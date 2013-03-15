package com.sociocast.android.model;

public class EntityProfile {

	/*
	 * The Entity ID
	 */
	private String eid;
	
	/*
	 * Retrieve classifications as human-readable values
	 */
	private boolean humRead;

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public boolean isHumRead() {
		return humRead;
	}

	public void setHumRead(boolean humRead) {
		this.humRead = humRead;
	}

}
